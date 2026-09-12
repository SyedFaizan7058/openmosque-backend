import { describe, it, expect, beforeEach } from 'vitest';
import { useAuthStore, MOCK_PERSONAS } from '../stores/authStore';

describe('Phase 1: Auth Store & Dev Mock Switcher', () => {
  beforeEach(() => {
    useAuthStore.getState().logout();
  });

  it('initializes with default state after logout', () => {
    const state = useAuthStore.getState();
    expect(state.user).toBeNull();
    expect(state.isAuthenticated).toBe(false);
    expect(state.token).toBeNull();
    expect(Object.keys(MOCK_PERSONAS)).toEqual(['contributor', 'imam', 'moderator', 'super_admin']);
  });

  it('logs in successfully with mock personas', () => {
    useAuthStore.getState().loginWithMockPersona('contributor');
    let state = useAuthStore.getState();
    expect(state.user?.displayName).toBe('Ahmad Al-Mansoor');
    expect(state.user?.role).toBe('USER');
    expect(state.user?.points).toBe(180);
    expect(state.isAuthenticated).toBe(true);

    // Switch to Imam
    useAuthStore.getState().loginWithMockPersona('imam');
    state = useAuthStore.getState();
    expect(state.user?.displayName).toBe('Imam Tariq Al-Banna');
    expect(state.user?.role).toBe('MOSQUE_ADMIN');
    expect(state.user?.points).toBe(480);

    // Switch to Super Admin
    useAuthStore.getState().loginWithMockPersona('super_admin');
    state = useAuthStore.getState();
    expect(state.user?.role).toBe('SUPER_ADMIN');
  });

  it('manages mosque bookmarks correctly', () => {
    const mosqueId = '18558e48-a2aa-4219-9583-54cd9a84a76c';
    const isInit = useAuthStore.getState().isBookmarked(mosqueId);

    useAuthStore.getState().toggleBookmark(mosqueId);
    expect(useAuthStore.getState().isBookmarked(mosqueId)).toBe(!isInit);

    useAuthStore.getState().toggleBookmark(mosqueId);
    expect(useAuthStore.getState().isBookmarked(mosqueId)).toBe(isInit);
  });

  it('correctly awards contributor points', () => {
    useAuthStore.getState().loginWithMockPersona('contributor');
    const initialPoints = useAuthStore.getState().user?.points || 0;

    useAuthStore.getState().awardPoints(100);
    expect(useAuthStore.getState().user?.points).toBe(initialPoints + 100);
  });

  it('updates 2FA status in store', () => {
    useAuthStore.getState().loginWithMockPersona('contributor');
    expect(useAuthStore.getState().user?.twoFactorEnabled).toBe(false);

    useAuthStore.getState().toggle2FA(true);
    expect(useAuthStore.getState().user?.twoFactorEnabled).toBe(true);
  });
});
