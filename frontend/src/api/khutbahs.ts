import { apiClient, safeApiCall } from './client';
import { MOCK_KHUTBAHS } from '../data/mockData';
import type { MosqueKhutbah } from '../types';

export const khutbahsApi = {
  getByMosque: async (idOrSlug: string): Promise<MosqueKhutbah[]> => {
    return safeApiCall(
      () => apiClient.get(`/api/v1/mosques/${idOrSlug}/khutbahs`),
      () => MOCK_KHUTBAHS
    );
  },

  create: async (mosqueId: string, payload: Partial<MosqueKhutbah>): Promise<MosqueKhutbah> => {
    return safeApiCall(
      () => apiClient.post(`/api/v1/mosque-admin/mosques/${mosqueId}/khutbahs`, payload),
      () => ({
        id: 'khut-' + Math.random().toString(36).substring(2, 7),
        mosqueId,
        khutbahDate: payload.khutbahDate || new Date().toISOString().split('T')[0],
        topic: payload.topic || 'Friday Khutbah',
        khatibName: payload.khatibName || 'Imam',
        batchNumber: payload.batchNumber || 1,
        khutbahTime: payload.khutbahTime || '13:00:00',
        language: payload.language || 'English & Arabic',
        ...payload,
      })
    );
  },
};
