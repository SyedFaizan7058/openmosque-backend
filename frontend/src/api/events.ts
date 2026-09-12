import { apiClient, safeApiCall } from './client';
import { MOCK_EVENTS } from '../data/mockData';
import type { MosqueEvent } from '../types';

export const eventsApi = {
  getByMosque: async (idOrSlug: string): Promise<MosqueEvent[]> => {
    return safeApiCall(
      () => apiClient.get(`/api/v1/mosques/${idOrSlug}/events`),
      () => MOCK_EVENTS
    );
  },

  getById: async (id: string): Promise<MosqueEvent | null> => {
    return safeApiCall(
      () => apiClient.get(`/api/v1/events/${id}`),
      () => MOCK_EVENTS.find((e) => e.id === id) || null
    );
  },

  create: async (mosqueId: string, payload: Partial<MosqueEvent>): Promise<MosqueEvent> => {
    return safeApiCall(
      () => apiClient.post(`/api/v1/mosque-admin/mosques/${mosqueId}/events`, payload),
      () => ({
        id: 'evt-' + Math.random().toString(36).substring(2, 7),
        mosqueId,
        title: payload.title || 'Mosque Program',
        description: payload.description || '',
        eventType: payload.eventType || 'HALAQAH',
        audience: payload.audience || 'ALL',
        startDateTime: payload.startDateTime || new Date().toISOString(),
        endDateTime: payload.endDateTime || new Date().toISOString(),
        isCancelled: false,
        ...payload,
      })
    );
  },
};
