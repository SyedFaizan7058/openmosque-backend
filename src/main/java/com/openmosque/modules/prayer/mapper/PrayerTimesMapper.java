package com.openmosque.modules.prayer.mapper;

import com.openmosque.modules.prayer.dto.IqamahScheduleDto;
import com.openmosque.modules.prayer.dto.PrayerConfigDto;
import com.openmosque.modules.prayer.entity.MosqueIqamahSchedule;
import com.openmosque.modules.prayer.entity.MosquePrayerConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PrayerTimesMapper {

    @Mapping(target = "mosqueId", source = "mosque.id")
    @Mapping(target = "calculationMethodDisplayName", source = "calculationMethod.displayName")
    PrayerConfigDto toConfigDto(MosquePrayerConfig entity);

    @Mapping(target = "mosqueId", source = "mosque.id")
    IqamahScheduleDto toIqamahDto(MosqueIqamahSchedule entity);
}
