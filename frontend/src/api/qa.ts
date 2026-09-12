import { apiClient, safeApiCall } from './client';
import { MOCK_QUESTIONS } from '../data/mockData';
import type { MosqueQuestion, MosqueAnswer } from '../types';

export const qaApi = {
  getQuestions: async (idOrSlug: string): Promise<MosqueQuestion[]> => {
    return safeApiCall(
      () => apiClient.get(`/api/v1/mosques/${idOrSlug}/questions`),
      () => MOCK_QUESTIONS
    );
  },

  askQuestion: async (mosqueId: string, questionText: string): Promise<MosqueQuestion> => {
    return safeApiCall(
      () => apiClient.post(`/api/v1/mosques/${mosqueId}/questions`, { questionText }),
      () => ({
        id: 'q-' + Math.random().toString(36).substring(2, 7),
        mosqueId,
        userId: 'usr-current',
        userName: 'Ahmad Al-Mansoor',
        questionText,
        status: 'PUBLISHED',
        createdAt: new Date().toISOString(),
        answers: [],
      })
    );
  },

  answerQuestion: async (questionId: string, answerText: string, isOfficial = false): Promise<MosqueAnswer> => {
    return safeApiCall(
      () => apiClient.post(`/api/v1/community/questions/${questionId}/answers`, { answerText }),
      () => ({
        id: 'ans-' + Math.random().toString(36).substring(2, 7),
        questionId,
        userId: 'usr-current',
        userName: isOfficial ? 'Imam Tariq Al-Banna' : 'Community Member',
        answerText,
        isOfficialMosqueAdmin: isOfficial,
        status: 'PUBLISHED',
        createdAt: new Date().toISOString(),
      })
    );
  },

  flagContent: async (targetType: 'REVIEW' | 'QUESTION' | 'ANSWER', targetId: string, reason: string) => {
    return safeApiCall(
      () => apiClient.post('/api/v1/community/flag', { targetType, targetId, reason }),
      () => ({ success: true, message: 'Content reported to moderators' })
    );
  },
};
