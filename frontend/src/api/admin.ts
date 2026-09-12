import { apiClient, safeApiCall } from './client';
import { MOCK_SUBMISSIONS, MOCK_CLAIMS, MOCK_FLAGS, MOCK_ADMIN_USERS } from '../data/mockData';
import type { MosqueSubmission, MosqueClaim, ContentFlag, User, UserRole, OsmIngestResult } from '../types';

export const adminApi = {
  getSubmissions: async (status = 'PENDING'): Promise<MosqueSubmission[]> => {
    return safeApiCall(
      () => apiClient.get(`/api/v1/admin/moderation/submissions?status=${status}`),
      () => MOCK_SUBMISSIONS.filter((s) => s.status === status)
    );
  },

  decideSubmission: async (id: string, status: 'APPROVED' | 'REJECTED', reviewComments?: string) => {
    return safeApiCall(
      () => apiClient.patch(`/api/v1/admin/moderation/submissions/${id}/decision`, { status, reviewComments }),
      () => ({ success: true, id, status, reviewComments })
    );
  },

  getClaims: async (status = 'PENDING'): Promise<MosqueClaim[]> => {
    return safeApiCall(
      () => apiClient.get(`/api/v1/admin/mosques/claims?status=${status}`),
      () => MOCK_CLAIMS.filter((c) => c.status === status)
    );
  },

  decideClaim: async (id: string, status: 'APPROVED' | 'REJECTED', reviewComments?: string) => {
    return safeApiCall(
      () => apiClient.patch(`/api/v1/admin/mosques/claims/${id}/decision`, { status, reviewComments }),
      () => ({ success: true, id, status, reviewComments })
    );
  },

  getUsers: async (role?: string): Promise<User[]> => {
    const q = role ? `?role=${role}` : '';
    return safeApiCall(
      () => apiClient.get(`/api/v1/admin/users${q}`),
      () => (role ? MOCK_ADMIN_USERS.filter((u) => u.role === role) : MOCK_ADMIN_USERS)
    );
  },

  changeUserRole: async (userId: string, role: UserRole) => {
    return safeApiCall(
      () => apiClient.patch(`/api/v1/admin/users/${userId}/role`, { role }),
      () => ({ success: true, userId, role })
    );
  },

  getFlags: async (): Promise<ContentFlag[]> => {
    return safeApiCall(
      () => apiClient.get('/api/v1/admin/community/flags'),
      () => MOCK_FLAGS
    );
  },

  decideFlag: async (id: string, status: 'RESOLVED' | 'DISMISSED', reviewerNotes?: string) => {
    return safeApiCall(
      () => apiClient.patch(`/api/v1/admin/community/flags/${id}/decision`, { status, reviewerNotes }),
      () => ({ success: true, id, status, reviewerNotes })
    );
  },

  ingestOsmCity: async (city: string, country: string, dryRun = false): Promise<OsmIngestResult> => {
    return safeApiCall(
      () => apiClient.post('/api/v1/admin/ingest/osm/city', { city, country, dryRun }),
      () => ({
        totalElementsFetched: 42,
        mosquesInserted: dryRun ? 0 : 38,
        duplicatesSkipped: 4,
        facilitiesAttached: 112,
        insertedMosqueNames: ['East London Mosque', 'Brick Lane Jamme Masjid', 'Burdett Estate Mosque', 'Stepney Shahjalal Mosque'],
        durationMs: 1420,
      })
    );
  },
};
