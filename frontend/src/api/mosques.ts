import { apiClient, safeApiCall } from './client';
import { MOCK_MOSQUES } from '../data/mockData';
import type { Mosque, MosqueDetail, PageResponse } from '../types';

export interface NearbyParams {
  latitude: number;
  longitude: number;
  radiusKm: number;
  facilities?: string[];
}

export interface SearchParams {
  q?: string;
  city?: string;
  country?: string;
  page?: number;
  size?: number;
}

export interface MosqueSubmissionPayload {
  name: string;
  description: string;
  address: string;
  city: string;
  country: string;
  latitude: number;
  longitude: number;
  contactPhone?: string;
  websiteUrl?: string;
  liveStreamUrl?: string;
  facilityCodes: string[];
  imageUrls: string[];
}

export interface MosqueClaimPayload {
  fullName: string;
  phoneNumber: string;
  officialEmail: string;
  positionInMosque: string;
  proofDocumentUrl: string;
}

export const mosquesApi = {
  getNearby: async (params: NearbyParams): Promise<Mosque[]> => {
    const queryParams = new URLSearchParams();
    queryParams.append('latitude', params.latitude.toString());
    queryParams.append('longitude', params.longitude.toString());
    queryParams.append('radiusKm', params.radiusKm.toString());
    if (params.facilities && params.facilities.length > 0) {
      queryParams.append('facilities', params.facilities.join(','));
    }

    return safeApiCall(
      () => apiClient.get(`/api/v1/mosques/nearby?${queryParams.toString()}`),
      () => {
        // Filter mock mosques based on radius and facilities
        return MOCK_MOSQUES.filter((m) => {
          if (m.distanceKm && m.distanceKm > params.radiusKm) return false;
          if (params.facilities && params.facilities.length > 0) {
            const hasAll = params.facilities.every((f) => m.facilities.includes(f));
            if (!hasAll) return false;
          }
          return true;
        });
      }
    );
  },

  search: async (params: SearchParams): Promise<PageResponse<Mosque>> => {
    const queryParams = new URLSearchParams();
    if (params.q) queryParams.append('q', params.q);
    if (params.city) queryParams.append('city', params.city);
    if (params.country) queryParams.append('country', params.country);
    queryParams.append('page', (params.page ?? 0).toString());
    queryParams.append('size', (params.size ?? 12).toString());

    return safeApiCall(
      () => apiClient.get(`/api/v1/mosques/search?${queryParams.toString()}`),
      () => {
        const filtered = MOCK_MOSQUES.filter((m) => {
          if (params.q) {
            const q = params.q.toLowerCase();
            const matchName = m.name.toLowerCase().includes(q);
            const matchAddress = m.address.toLowerCase().includes(q);
            const matchCity = m.city.toLowerCase().includes(q);
            if (!matchName && !matchAddress && !matchCity) return false;
          }
          if (params.city && !m.city.toLowerCase().includes(params.city.toLowerCase())) return false;
          if (params.country && !m.country.toLowerCase().includes(params.country.toLowerCase())) return false;
          return true;
        });

        const page = params.page ?? 0;
        const size = params.size ?? 12;
        const paged = filtered.slice(page * size, (page + 1) * size);

        return {
          content: paged,
          pageNumber: page,
          pageSize: size,
          totalElements: filtered.length,
          totalPages: Math.ceil(filtered.length / size) || 1,
          isLast: (page + 1) * size >= filtered.length,
        };
      }
    );
  },

  getByIdOrSlug: async (idOrSlug: string): Promise<MosqueDetail> => {
    return safeApiCall(
      () => apiClient.get(`/api/v1/mosques/${idOrSlug}`),
      () => {
        const match = MOCK_MOSQUES.find((m) => m.id === idOrSlug || m.slug === idOrSlug);
        if (match) return match;
        return MOCK_MOSQUES[0];
      }
    );
  },

  submitNew: async (payload: MosqueSubmissionPayload) => {
    return safeApiCall(
      () => apiClient.post('/api/v1/mosques/submissions', payload),
      () => ({
        id: 'sub-' + Math.random().toString(36).substring(2, 7),
        ...payload,
        status: 'PENDING',
        createdAt: new Date().toISOString(),
      })
    );
  },

  suggestEdit: async (mosqueId: string, payload: Partial<MosqueSubmissionPayload>) => {
    return safeApiCall(
      () => apiClient.post(`/api/v1/mosques/${mosqueId}/suggest-edit`, payload),
      () => ({
        id: 'edit-' + Math.random().toString(36).substring(2, 7),
        mosqueId,
        status: 'PENDING',
        createdAt: new Date().toISOString(),
      })
    );
  },

  claim: async (mosqueId: string, payload: MosqueClaimPayload) => {
    return safeApiCall(
      () => apiClient.post(`/api/v1/mosques/${mosqueId}/claim`, payload),
      () => ({
        id: 'clm-' + Math.random().toString(36).substring(2, 7),
        mosqueId,
        ...payload,
        status: 'PENDING',
        createdAt: new Date().toISOString(),
      })
    );
  },
};
