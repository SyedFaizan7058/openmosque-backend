import { apiClient, safeApiCall } from './client';
import { MOCK_FACILITIES } from '../data/mockData';
import type { Facility } from '../types';

export const facilitiesApi = {
  getAll: async (): Promise<Facility[]> => {
    return safeApiCall(
      () => apiClient.get('/api/v1/facilities'),
      () => MOCK_FACILITIES
    );
  },
};
