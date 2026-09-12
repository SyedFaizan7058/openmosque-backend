import { apiClient, safeApiCall } from './client';
import { getMockPrayerTimes } from '../data/mockData';
import type { PrayerTimesDto, IqamahScheduleDto } from '../types';

export const prayerApi = {
  getPrayerTimes: async (idOrSlug: string, date?: string): Promise<PrayerTimesDto> => {
    const query = date ? `?date=${date}` : '';
    return safeApiCall(
      () => apiClient.get(`/api/v1/mosques/${idOrSlug}/prayer-times${query}`),
      () => getMockPrayerTimes(idOrSlug, date)
    );
  },

  getMethods: async () => {
    return safeApiCall(
      () => apiClient.get('/api/v1/prayer-times/methods'),
      () => [
        { code: 'MUSLIM_WORLD_LEAGUE', name: 'Muslim World League (MWL)' },
        { code: 'ISNA', name: 'Islamic Society of North America (ISNA)' },
        { code: 'UMM_AL_QURA', name: 'Umm Al-Qura University, Makkah' },
        { code: 'EGYPT', name: 'Egyptian General Authority of Survey' },
        { code: 'KARACHI', name: 'University of Islamic Sciences, Karachi' },
      ]
    );
  },

  getIqamahSchedule: async (mosqueId: string): Promise<IqamahScheduleDto> => {
    return safeApiCall(
      () => apiClient.get(`/api/v1/mosque-admin/mosques/${mosqueId}/iqamah-schedule`),
      () => ({
        mosqueId,
        fajrType: 'OFFSET_AFTER_ADHAN',
        fajrOffsetMinutes: 20,
        dhuhrType: 'FIXED_TIME',
        dhuhrFixedTime: '13:30:00',
        asrType: 'OFFSET_AFTER_ADHAN',
        asrOffsetMinutes: 15,
        maghribType: 'OFFSET_AFTER_ADHAN',
        maghribOffsetMinutes: 10,
        ishaType: 'OFFSET_AFTER_ADHAN',
        ishaOffsetMinutes: 15,
        jummah1Time: '13:15:00',
        jummah2Time: '14:00:00',
        jummahKhutbahLanguage: 'English & Arabic',
      })
    );
  },

  updateIqamahSchedule: async (mosqueId: string, payload: Partial<IqamahScheduleDto>) => {
    return safeApiCall(
      () => apiClient.put(`/api/v1/mosque-admin/mosques/${mosqueId}/iqamah-schedule`, payload),
      () => ({ success: true, ...payload })
    );
  },
};
