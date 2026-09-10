package com.openmosque.modules.prayer.service;

import com.openmosque.common.config.RedisCacheConfig;
import com.openmosque.common.exception.ForbiddenException;
import com.openmosque.common.exception.ResourceNotFoundException;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.mosque.repository.MosqueRepository;
import com.openmosque.modules.mosque.service.MosqueService;
import com.openmosque.modules.prayer.client.AladhanApiClient;
import com.openmosque.modules.prayer.client.AladhanApiClient.AladhanTimingsData;
import com.openmosque.modules.prayer.client.AladhanApiClient.AladhanTimingsResponse;
import com.openmosque.modules.prayer.dto.*;
import com.openmosque.modules.prayer.entity.*;
import com.openmosque.modules.prayer.mapper.PrayerTimesMapper;
import com.openmosque.modules.prayer.repository.MosqueIqamahScheduleRepository;
import com.openmosque.modules.prayer.repository.MosquePrayerConfigRepository;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service orchestrating prayer times calculation, Aladhan API integration, Iqamah overrides, and caching.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrayerTimesService {

    private final MosqueRepository mosqueRepository;
    private final MosqueService mosqueService;
    private final MosquePrayerConfigRepository prayerConfigRepository;
    private final MosqueIqamahScheduleRepository iqamahScheduleRepository;
    private final AladhanApiClient aladhanApiClient;
    private final PrayerTimesMapper prayerTimesMapper;
    private final com.openmosque.modules.notification.service.NotificationService notificationService;
    private final com.openmosque.modules.claim.repository.MosqueClaimRequestRepository claimRequestRepository;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Retrieves the daily prayer schedule, Iqamah congregation times, and next prayer countdown for a mosque.
     * Results are cached by mosque and date in Redis.
     */
    @Transactional
    @Cacheable(value = RedisCacheConfig.PRAYER_TIMES_CACHE, key = "#idOrSlug + '_' + (#requestedDate != null ? #requestedDate.toString() : 'today')", unless = "#result == null")
    public PrayerTimesDayResponseDto getPrayerTimesForMosque(String idOrSlug, LocalDate requestedDate) {
        Mosque mosque = findMosqueByIdOrSlug(idOrSlug);

        // 1. Get or initialize Prayer Config & Iqamah Schedule
        MosquePrayerConfig config = prayerConfigRepository.findByMosqueIdAndDeletedFalse(mosque.getId())
                .orElseGet(() -> createDefaultPrayerConfig(mosque));

        MosqueIqamahSchedule iqamahSchedule = iqamahScheduleRepository.findByMosqueIdAndDeletedFalse(mosque.getId())
                .orElseGet(() -> createDefaultIqamahSchedule(mosque));

        // 2. Resolve Target Date & Time Zone
        ZoneId zoneId = resolveZoneId(config.getTimeZone(), mosque.getCountry());
        LocalDate date = (requestedDate != null) ? requestedDate : LocalDate.now(zoneId);
        LocalTime now = LocalTime.now(zoneId);

        // 3. Fetch Astronomical Timings from Aladhan API
        AladhanTimingsResponse aladhanResponse = aladhanApiClient.fetchTimings(
                date,
                mosque.getLatitude(),
                mosque.getLongitude(),
                config.getCalculationMethod().getAladhanMethodId(),
                config.getJuristicSchool().getAladhanSchoolId()
        );

        AladhanTimingsData rawTimings = aladhanResponse.getTimings();

        // 4. Compute Clean Adhan & Iqamah Time Strings (custom Adhan override if set by admin, else astronomical)
        String fajrAdhan = (iqamahSchedule.getFajrAdhanTime() != null)
                ? iqamahSchedule.getFajrAdhanTime().format(TIME_FORMAT)
                : cleanTime(rawTimings.getFajr());
        String sunriseTime = cleanTime(rawTimings.getSunrise());
        String dhuhrAdhan = (iqamahSchedule.getDhuhrAdhanTime() != null)
                ? iqamahSchedule.getDhuhrAdhanTime().format(TIME_FORMAT)
                : cleanTime(rawTimings.getDhuhr());
        String asrAdhan = (iqamahSchedule.getAsrAdhanTime() != null)
                ? iqamahSchedule.getAsrAdhanTime().format(TIME_FORMAT)
                : cleanTime(rawTimings.getAsr());
        String maghribAdhan = (iqamahSchedule.getMaghribAdhanTime() != null)
                ? iqamahSchedule.getMaghribAdhanTime().format(TIME_FORMAT)
                : cleanTime(rawTimings.getMaghrib());
        String ishaAdhan = (iqamahSchedule.getIshaAdhanTime() != null)
                ? iqamahSchedule.getIshaAdhanTime().format(TIME_FORMAT)
                : cleanTime(rawTimings.getIsha());

        String fajrIqamah = computeIqamahTime(fajrAdhan, iqamahSchedule.getFajrType(), iqamahSchedule.getFajrOffsetMinutes(), iqamahSchedule.getFajrFixedTime());
        String dhuhrIqamah = computeIqamahTime(dhuhrAdhan, iqamahSchedule.getDhuhrType(), iqamahSchedule.getDhuhrOffsetMinutes(), iqamahSchedule.getDhuhrFixedTime());
        String asrIqamah = computeIqamahTime(asrAdhan, iqamahSchedule.getAsrType(), iqamahSchedule.getAsrOffsetMinutes(), iqamahSchedule.getAsrFixedTime());
        String maghribIqamah = computeIqamahTime(maghribAdhan, iqamahSchedule.getMaghribType(), iqamahSchedule.getMaghribOffsetMinutes(), iqamahSchedule.getMaghribFixedTime());
        String ishaIqamah = computeIqamahTime(ishaAdhan, iqamahSchedule.getIshaType(), iqamahSchedule.getIshaOffsetMinutes(), iqamahSchedule.getIshaFixedTime());

        // 5. Build Individual Prayer Slot DTOs
        List<SinglePrayerTimeDto> slots = new ArrayList<>();
        slots.add(SinglePrayerTimeDto.builder().prayerName("FAJR").adhanTime(fajrAdhan).iqamahTime(fajrIqamah).build());
        slots.add(SinglePrayerTimeDto.builder().prayerName("SUNRISE").adhanTime(sunriseTime).iqamahTime(null).build());
        slots.add(SinglePrayerTimeDto.builder().prayerName("DHUHR").adhanTime(dhuhrAdhan).iqamahTime(dhuhrIqamah).build());
        slots.add(SinglePrayerTimeDto.builder().prayerName("ASR").adhanTime(asrAdhan).iqamahTime(asrIqamah).build());
        slots.add(SinglePrayerTimeDto.builder().prayerName("MAGHRIB").adhanTime(maghribAdhan).iqamahTime(maghribIqamah).build());
        slots.add(SinglePrayerTimeDto.builder().prayerName("ISHA").adhanTime(ishaAdhan).iqamahTime(ishaIqamah).build());

        // 6. Calculate Next Prayer Countdown (if requested for today)
        CountdownState countdown = calculateCountdown(slots, now, (requestedDate == null || requestedDate.isEqual(LocalDate.now(zoneId))));

        // 7. Hijri Date formatting
        String hijriFormatted = formatHijri(aladhanResponse.getDate());

        // 8. Friday Jumu'ah Timings
        FridayJummahScheduleDto jummah = FridayJummahScheduleDto.builder()
                .firstJummahTime(iqamahSchedule.getJummah1Time() != null ? iqamahSchedule.getJummah1Time().format(TIME_FORMAT) : "13:15")
                .secondJummahTime(iqamahSchedule.getJummah2Time() != null ? iqamahSchedule.getJummah2Time().format(TIME_FORMAT) : null)
                .khutbahLanguage(iqamahSchedule.getJummahKhutbahLanguage())
                .build();

        return PrayerTimesDayResponseDto.builder()
                .mosqueId(mosque.getId())
                .mosqueName(mosque.getName())
                .mosqueSlug(mosque.getSlug())
                .date(date)
                .hijriDate(hijriFormatted)
                .timeZone(zoneId.getId())
                .calculationMethod(config.getCalculationMethod().name())
                .calculationMethodName(config.getCalculationMethod().getDisplayName())
                .juristicSchool(config.getJuristicSchool().name())
                .currentPrayer(countdown.currentPrayer)
                .nextPrayer(countdown.nextPrayer)
                .nextPrayerTime(countdown.nextPrayerTime)
                .timeRemainingMinutes(countdown.minutesRemaining)
                .timeRemainingFormatted(countdown.timeRemainingFormatted)
                .timings(slots)
                .jummahSchedule(jummah)
                .build();
    }

    /**
     * Lists all supported calculation methods.
     */
    public List<CalculationMethodDto> getSupportedCalculationMethods() {
        return Arrays.stream(CalculationMethod.values())
                .map(m -> CalculationMethodDto.builder()
                        .code(m.name())
                        .name(m.getDisplayName())
                        .aladhanMethodId(m.getAladhanMethodId())
                        .build())
                .toList();
    }

    /**
     * Gets prayer configuration for a specific mosque.
     */
    @Transactional
    public PrayerConfigDto getPrayerConfig(UUID mosqueId) {
        Mosque mosque = mosqueRepository.findByIdAndDeletedFalse(mosqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Mosque", "id", mosqueId));
        MosquePrayerConfig config = prayerConfigRepository.findByMosqueIdAndDeletedFalse(mosqueId)
                .orElseGet(() -> createDefaultPrayerConfig(mosque));
        return prayerTimesMapper.toConfigDto(config);
    }

    /**
     * Updates prayer configuration for a mosque (Restricted to Mosque Admin / Super Admin).
     */
    @Transactional
    @CacheEvict(value = RedisCacheConfig.PRAYER_TIMES_CACHE, allEntries = true)
    public PrayerConfigDto updatePrayerConfig(UUID mosqueId, PrayerConfigUpdateDto dto, User user) {
        validateMosqueAdminPermission(mosqueId, user);

        Mosque mosque = mosqueRepository.findByIdAndDeletedFalse(mosqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Mosque", "id", mosqueId));

        MosquePrayerConfig config = prayerConfigRepository.findByMosqueIdAndDeletedFalse(mosqueId)
                .orElseGet(() -> MosquePrayerConfig.builder().mosque(mosque).build());

        config.setCalculationMethod(dto.getCalculationMethod());
        config.setJuristicSchool(dto.getJuristicSchool());
        if (dto.getTimeZone() != null) config.setTimeZone(dto.getTimeZone());
        if (dto.getFajrAngle() != null) config.setFajrAngle(dto.getFajrAngle());
        if (dto.getIshaAngle() != null) config.setIshaAngle(dto.getIshaAngle());
        if (dto.getHighLatitudeRule() != null) config.setHighLatitudeRule(dto.getHighLatitudeRule());

        MosquePrayerConfig saved = prayerConfigRepository.save(config);
        log.info("Updated prayer config for mosque '{}' by user '{}'", mosque.getName(), user.getEmail());
        return prayerTimesMapper.toConfigDto(saved);
    }

    /**
     * Gets Iqamah schedule for a specific mosque.
     */
    @Transactional
    public IqamahScheduleDto getIqamahSchedule(UUID mosqueId) {
        Mosque mosque = mosqueRepository.findByIdAndDeletedFalse(mosqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Mosque", "id", mosqueId));
        MosqueIqamahSchedule schedule = iqamahScheduleRepository.findByMosqueIdAndDeletedFalse(mosqueId)
                .orElseGet(() -> createDefaultIqamahSchedule(mosque));
        return prayerTimesMapper.toIqamahDto(schedule);
    }

    /**
     * Updates Iqamah schedule and Friday Jumu'ah batches for a mosque.
     */
    @Transactional
    @CacheEvict(value = RedisCacheConfig.PRAYER_TIMES_CACHE, allEntries = true)
    public IqamahScheduleDto updateIqamahSchedule(UUID mosqueId, IqamahScheduleUpdateDto dto, User user) {
        validateMosqueAdminPermission(mosqueId, user);

        Mosque mosque = mosqueRepository.findByIdAndDeletedFalse(mosqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Mosque", "id", mosqueId));

        MosqueIqamahSchedule schedule = iqamahScheduleRepository.findByMosqueIdAndDeletedFalse(mosqueId)
                .orElseGet(() -> MosqueIqamahSchedule.builder().mosque(mosque).build());

        schedule.setFajrType(dto.getFajrType());
        schedule.setFajrOffsetMinutes(dto.getFajrOffsetMinutes());
        schedule.setFajrFixedTime(dto.getFajrFixedTime());

        schedule.setDhuhrType(dto.getDhuhrType());
        schedule.setDhuhrOffsetMinutes(dto.getDhuhrOffsetMinutes());
        schedule.setDhuhrFixedTime(dto.getDhuhrFixedTime());

        schedule.setAsrType(dto.getAsrType());
        schedule.setAsrOffsetMinutes(dto.getAsrOffsetMinutes());
        schedule.setAsrFixedTime(dto.getAsrFixedTime());

        schedule.setMaghribType(dto.getMaghribType());
        schedule.setMaghribOffsetMinutes(dto.getMaghribOffsetMinutes());
        schedule.setMaghribFixedTime(dto.getMaghribFixedTime());

        schedule.setIshaType(dto.getIshaType());
        schedule.setIshaOffsetMinutes(dto.getIshaOffsetMinutes());
        schedule.setIshaFixedTime(dto.getIshaFixedTime());

        // Custom Adhan Overrides
        schedule.setFajrAdhanTime(dto.getFajrAdhanTime());
        schedule.setDhuhrAdhanTime(dto.getDhuhrAdhanTime());
        schedule.setAsrAdhanTime(dto.getAsrAdhanTime());
        schedule.setMaghribAdhanTime(dto.getMaghribAdhanTime());
        schedule.setIshaAdhanTime(dto.getIshaAdhanTime());

        if (dto.getJummah1Time() != null) schedule.setJummah1Time(dto.getJummah1Time());
        schedule.setJummah2Time(dto.getJummah2Time());
        if (dto.getJummahKhutbahLanguage() != null) schedule.setJummahKhutbahLanguage(dto.getJummahKhutbahLanguage());

        MosqueIqamahSchedule saved = iqamahScheduleRepository.save(schedule);
        log.info("Updated Iqamah schedule for mosque '{}' by user '{}'", mosque.getName(), user.getEmail());

        // Dispatch in-app notification & alert to all worshippers who favorited this mosque
        notificationService.notifyMosqueFavoriters(
                mosque.getId(),
                "Iqamah Schedule Updated: " + mosque.getName(),
                "The prayer congregation and Iqamah timings have been updated by the mosque administration.",
                com.openmosque.modules.notification.entity.NotificationType.IQAMAH_CHANGE,
                "/mosques/" + (mosque.getSlug() != null ? mosque.getSlug() : mosque.getId()),
                null
        );

        return prayerTimesMapper.toIqamahDto(saved);
    }

    // --- Helper Methods ---

    private Mosque findMosqueByIdOrSlug(String idOrSlug) {
        return mosqueService.findEntityByIdOrSlug(idOrSlug);
    }

    private MosquePrayerConfig createDefaultPrayerConfig(Mosque mosque) {
        MosquePrayerConfig config = MosquePrayerConfig.builder()
                .mosque(mosque)
                .calculationMethod(CalculationMethod.MUSLIM_WORLD_LEAGUE)
                .juristicSchool(JuristicSchool.STANDARD)
                .timeZone("UTC")
                .build();
        return prayerConfigRepository.save(config);
    }

    private MosqueIqamahSchedule createDefaultIqamahSchedule(Mosque mosque) {
        MosqueIqamahSchedule schedule = MosqueIqamahSchedule.builder()
                .mosque(mosque)
                .fajrType(IqamahCalculationType.OFFSET_AFTER_ADHAN)
                .fajrOffsetMinutes(20)
                .dhuhrType(IqamahCalculationType.OFFSET_AFTER_ADHAN)
                .dhuhrOffsetMinutes(15)
                .dhuhrFixedTime(LocalTime.of(13, 30))
                .asrType(IqamahCalculationType.OFFSET_AFTER_ADHAN)
                .asrOffsetMinutes(15)
                .maghribType(IqamahCalculationType.OFFSET_AFTER_ADHAN)
                .maghribOffsetMinutes(0)
                .ishaType(IqamahCalculationType.OFFSET_AFTER_ADHAN)
                .ishaOffsetMinutes(15)
                .jummah1Time(LocalTime.of(13, 15))
                .jummahKhutbahLanguage("Arabic")
                .build();
        return iqamahScheduleRepository.save(schedule);
    }

    private String cleanTime(String raw) {
        if (raw == null) return "00:00";
        // Extract "HH:mm" from raw strings that might have "(BST)" or timezone suffix
        return raw.replaceAll("[^0-9:]", "").substring(0, Math.min(5, raw.replaceAll("[^0-9:]", "").length()));
    }

    private String computeIqamahTime(String adhanTimeStr, IqamahCalculationType type, Integer offsetMinutes, LocalTime fixedTime) {
        try {
            if (type == IqamahCalculationType.FIXED_TIME && fixedTime != null) {
                return fixedTime.format(TIME_FORMAT);
            }
            LocalTime adhan = LocalTime.parse(adhanTimeStr, TIME_FORMAT);
            int offset = (offsetMinutes != null) ? offsetMinutes : 15;
            return adhan.plusMinutes(offset).format(TIME_FORMAT);
        } catch (Exception e) {
            return adhanTimeStr;
        }
    }

    private CountdownState calculateCountdown(List<SinglePrayerTimeDto> slots, LocalTime now, boolean isToday) {
        if (!isToday) {
            return new CountdownState("N/A", slots.get(0).getPrayerName(), slots.get(0).getAdhanTime(), null, null);
        }

        for (int i = 0; i < slots.size(); i++) {
            SinglePrayerTimeDto slot = slots.get(i);
            try {
                LocalTime targetTime = LocalTime.parse(slot.getAdhanTime(), TIME_FORMAT);
                if (now.isBefore(targetTime)) {
                    slot.setNext(true);
                    long minutes = Duration.between(now, targetTime).toMinutes();
                    String timeFormatted = formatDuration(minutes);
                    slot.setTimeRemainingFormatted(timeFormatted);

                    String current = (i > 0) ? slots.get(i - 1).getPrayerName() : "ISHA (Prev Night)";
                    return new CountdownState(current, slot.getPrayerName(), slot.getAdhanTime(), minutes, timeFormatted);
                }
            } catch (Exception ignored) {}
        }

        // After Isha -> Next is tomorrow Fajr
        LocalTime fajrTime = LocalTime.of(5, 0);
        try {
            fajrTime = LocalTime.parse(slots.get(0).getAdhanTime(), TIME_FORMAT);
        } catch (Exception ignored) {}
        long minutesToMidnight = Duration.between(now, LocalTime.MAX).toMinutes() + 1;
        long minutesFromMidnightToFajr = Duration.between(LocalTime.MIN, fajrTime).toMinutes();
        long minutes = minutesToMidnight + minutesFromMidnightToFajr;
        String formatted = formatDuration(minutes);
        return new CountdownState("ISHA", "FAJR", slots.get(0).getAdhanTime(), minutes, formatted);
    }

    private String formatDuration(long minutes) {
        if (minutes < 60) {
            return "in " + minutes + " mins";
        }
        long hours = minutes / 60;
        long remainingMins = minutes % 60;
        return "in " + hours + "h " + remainingMins + "m";
    }

    private String formatHijri(AladhanApiClient.AladhanDateData dateData) {
        if (dateData == null || dateData.getHijri() == null) {
            return "Islamic Calendar";
        }
        AladhanApiClient.AladhanHijriData h = dateData.getHijri();
        String month = (h.getMonth() != null) ? h.getMonth().getEn() : "Month";
        return h.getDay() + " " + month + " " + h.getYear() + " AH";
    }

    private ZoneId resolveZoneId(String configuredZone, String country) {
        try {
            if (configuredZone != null && !configuredZone.equalsIgnoreCase("UTC")) {
                return ZoneId.of(configuredZone);
            }
        } catch (Exception ignored) {}
        return ZoneId.systemDefault();
    }

    private void validateMosqueAdminPermission(UUID mosqueId, User user) {
        if (user == null) {
            throw new com.openmosque.common.exception.UnauthorizedException("Authentication required.");
        }
        if (user.getRole() == com.openmosque.modules.user.entity.UserRole.SUPER_ADMIN) {
            return;
        }
        if (user.getRole() == com.openmosque.modules.user.entity.UserRole.MOSQUE_ADMIN) {
            boolean hasApprovedClaim = claimRequestRepository.findByMosqueIdAndClaimantIdAndStatus(
                    mosqueId, user.getId(), com.openmosque.modules.claim.entity.ClaimStatus.APPROVED).isPresent();
            if (hasApprovedClaim) {
                return;
            }
        }
        throw new com.openmosque.common.exception.ForbiddenException(
                "Access denied: You do not have permission to manage prayer timings for this mosque. Only verified administrators for this mosque or super admins may update settings.");
    }

    private record CountdownState(
            String currentPrayer,
            String nextPrayer,
            String nextPrayerTime,
            Long minutesRemaining,
            String timeRemainingFormatted
    ) {}
}
