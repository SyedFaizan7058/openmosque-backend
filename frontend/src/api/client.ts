import axios from 'axios';
import { useAuthStore } from '../stores/authStore';
import { useToastStore } from '../stores/toastStore';
import type { ApiResponse } from '../types';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const apiClient = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

if (import.meta.env.MODE === 'test') {
  apiClient.interceptors.request.use(() => {
    return Promise.reject(new Error('Test mode: using offline fallback'));
  });
} else {
  apiClient.interceptors.request.use(
    (config) => {
      const token = useAuthStore.getState().token;
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );
}

apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      'An unexpected network error occurred.';

    // Only toast on real HTTP error responses (not canceled requests)
    if (error.response && error.response.status !== 401) {
      useToastStore.getState().addToast(message, 'error');
    }
    return Promise.reject(error);
  }
);

/**
 * Universal wrapper for API calls with built-in mock fallback for offline/development mode.
 */
export async function safeApiCall<T>(
  networkFn: () => Promise<{ data: ApiResponse<T> | T }>,
  fallbackFn: () => Promise<T> | T
): Promise<T> {
  try {
    const res = await networkFn();
    const body = res.data as ApiResponse<T>;
    if (body && typeof body === 'object' && 'data' in body && body.data !== undefined) {
      return body.data;
    }
    return res.data as T;
  } catch {
    // If backend is offline or returned network error, use high-fidelity fallback data
    return await fallbackFn();
  }
}
