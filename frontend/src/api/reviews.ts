import { apiClient, safeApiCall } from './client';
import { MOCK_REVIEWS, MOCK_RATING_SUMMARY } from '../data/mockData';
import type { MosqueReview, RatingSummary } from '../types';

export interface CreateReviewPayload {
  ratingOverall: number;
  ratingCleanliness: number;
  ratingFacilities: number;
  ratingWomensArea: number;
  ratingParking: number;
  reviewText: string;
}

export const reviewsApi = {
  getReviews: async (idOrSlug: string): Promise<MosqueReview[]> => {
    return safeApiCall(
      () => apiClient.get(`/api/v1/mosques/${idOrSlug}/reviews`),
      () => MOCK_REVIEWS
    );
  },

  getRatings: async (mosqueId: string): Promise<RatingSummary> => {
    return safeApiCall(
      () => apiClient.get(`/api/v1/mosques/${mosqueId}/ratings`),
      () => MOCK_RATING_SUMMARY
    );
  },

  create: async (mosqueId: string, payload: CreateReviewPayload): Promise<MosqueReview> => {
    return safeApiCall(
      () => apiClient.post(`/api/v1/mosques/${mosqueId}/reviews`, payload),
      () => ({
        id: 'rev-' + Math.random().toString(36).substring(2, 7),
        mosqueId,
        userId: 'usr-current',
        userName: 'Ahmad Al-Mansoor',
        userPhotoUrl: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80',
        ...payload,
        status: 'PUBLISHED',
        createdAt: new Date().toISOString(),
      })
    );
  },

  delete: async (mosqueId: string, reviewId: string): Promise<void> => {
    return safeApiCall(
      () => apiClient.delete(`/api/v1/mosques/${mosqueId}/reviews/${reviewId}`),
      () => undefined
    );
  },
};
