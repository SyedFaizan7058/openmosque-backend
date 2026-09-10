package com.openmosque.modules.mosque.service;

import com.openmosque.common.exception.ResourceNotFoundException;
import com.openmosque.common.util.GeoUtils;
import com.openmosque.modules.mosque.dto.FavoriteMosqueResponseDto;
import com.openmosque.modules.mosque.dto.FavoriteStatusDto;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.mosque.entity.MosqueFacility;
import com.openmosque.modules.mosque.entity.MosqueImage;
import com.openmosque.modules.mosque.entity.UserFavoriteMosque;
import com.openmosque.modules.mosque.repository.MosqueRepository;
import com.openmosque.modules.mosque.repository.UserFavoriteMosqueRepository;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.service.BadgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MosqueFavoriteService {

    private final UserFavoriteMosqueRepository favoriteRepository;
    private final MosqueRepository mosqueRepository;
    private final BadgeService badgeService;

    /**
     * Adds a mosque to the user's favorites in PostgreSQL.
     * Concurrency-safe: if simultaneous duplicate requests arrive, the unique constraint
     * is caught gracefully without throwing 500 error.
     */
    @Transactional
    public FavoriteStatusDto addFavorite(User user, UUID mosqueId) {
        Mosque mosque = mosqueRepository.findByIdAndDeletedFalse(mosqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Mosque", "id", mosqueId));

        if (!favoriteRepository.existsByUserIdAndMosqueId(user.getId(), mosque.getId())) {
            try {
                UserFavoriteMosque favorite = UserFavoriteMosque.builder()
                        .user(user)
                        .mosque(mosque)
                        .build();
                favoriteRepository.saveAndFlush(favorite);
                log.info("Mosque '{}' favorited by user '{}' (User ID: {})",
                        mosque.getName(), user.getEmail(), user.getId());
            } catch (DataIntegrityViolationException e) {
                log.debug("Concurrent favorite request caught safely by unique constraint: user={}, mosque={}",
                        user.getId(), mosqueId);
            }
        }

        // Evaluate badge eligibility based on total favorites in database
        long totalFavorites = favoriteRepository.countByUserId(user.getId());
        badgeService.evaluateFavoritesBadges(user, totalFavorites);

        return new FavoriteStatusDto(mosque.getId(), true);
    }

    /**
     * Removes a mosque from the user's favorites in PostgreSQL.
     * Safe and idempotent: repeating the call will not corrupt state.
     */
    @Transactional
    public FavoriteStatusDto removeFavorite(User user, UUID mosqueId) {
        // Verify mosque exists (or validate format)
        if (!mosqueRepository.existsById(mosqueId)) {
            throw new ResourceNotFoundException("Mosque", "id", mosqueId);
        }

        favoriteRepository.deleteByUserIdAndMosqueId(user.getId(), mosqueId);
        log.info("Mosque '{}' unfavorited by user '{}'", mosqueId, user.getEmail());

        return new FavoriteStatusDto(mosqueId, false);
    }

    /**
     * Retrieves all favorited mosques for the user directly from PostgreSQL.
     */
    @Transactional(readOnly = true)
    public List<FavoriteMosqueResponseDto> getUserFavorites(User user, Double userLat, Double userLon) {
        List<UserFavoriteMosque> favorites = favoriteRepository.findAllByUserIdWithMosque(user.getId());

        return favorites.stream().map(fav -> {
            Mosque m = fav.getMosque();

            // Resolve cover image
            String coverImage = m.getImages().stream()
                    .filter(MosqueImage::isCover)
                    .map(MosqueImage::getImageUrl)
                    .findFirst()
                    .orElseGet(() -> m.getImages().isEmpty() ? null : m.getImages().get(0).getImageUrl());

            // Resolve facilities
            List<String> facilityCodes = m.getFacilities().stream()
                    .map(MosqueFacility::getFacility)
                    .map(f -> f.getCode())
                    .collect(Collectors.toList());

            // Calculate distance if coordinates supplied
            Double distanceKm = null;
            if (userLat != null && userLon != null) {
                distanceKm = GeoUtils.calculateDistanceKm(userLat, userLon, m.getLatitude(), m.getLongitude());
            }

            return FavoriteMosqueResponseDto.builder()
                    .mosqueId(m.getId())
                    .name(m.getName())
                    .slug(m.getSlug())
                    .description(m.getDescription())
                    .address(m.getAddress())
                    .city(m.getCity())
                    .state(m.getState())
                    .country(m.getCountry())
                    .postalCode(m.getPostalCode())
                    .latitude(m.getLatitude())
                    .longitude(m.getLongitude())
                    .distanceKm(distanceKm)
                    .coverImageUrl(coverImage)
                    .verified(m.isVerified())
                    .facilityCodes(facilityCodes)
                    .favoritedAt(fav.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Checks if a specific mosque is favorited by the user in PostgreSQL.
     */
    @Transactional(readOnly = true)
    public FavoriteStatusDto getFavoriteStatus(User user, UUID mosqueId) {
        boolean isFav = favoriteRepository.existsByUserIdAndMosqueId(user.getId(), mosqueId);
        return new FavoriteStatusDto(mosqueId, isFav);
    }
}
