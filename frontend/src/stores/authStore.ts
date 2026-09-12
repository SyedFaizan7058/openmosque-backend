import { create } from 'zustand';
import type { User, UserRole } from '../types';

export const MOCK_PERSONAS: Record<string, User> = {
  contributor: {
    id: 'usr-contributor-101',
    firebaseUid: 'mock-contributor-1',
    email: 'ahmad.contributor@example.org',
    displayName: 'Ahmad Al-Mansoor',
    phoneNumber: '+1 415 555 2671',
    photoUrl: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80',
    role: 'USER',
    points: 180,
    isActive: true,
    isVerified: true,
    twoFactorEnabled: false,
    createdAt: '2026-01-15T10:00:00Z',
  },
  imam: {
    id: 'usr-imam-202',
    firebaseUid: 'mock-imam-1',
    email: 'imam.tariq@eastlondonmosque.org',
    displayName: 'Imam Tariq Al-Banna',
    phoneNumber: '+44 20 7654 3210',
    photoUrl: 'https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150&auto=format&fit=crop&q=80',
    role: 'MOSQUE_ADMIN',
    points: 480,
    isActive: true,
    isVerified: true,
    twoFactorEnabled: true,
    createdAt: '2025-11-20T08:30:00Z',
  },
  moderator: {
    id: 'usr-moderator-303',
    firebaseUid: 'mock-moderator-1',
    email: 'fatima.moderator@openmosque.org',
    displayName: 'Fatima Zahra',
    phoneNumber: '+44 121 440 9988',
    photoUrl: 'https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150&auto=format&fit=crop&q=80',
    role: 'MODERATOR',
    points: 820,
    isActive: true,
    isVerified: true,
    twoFactorEnabled: true,
    createdAt: '2025-08-10T14:15:00Z',
  },
  super_admin: {
    id: 'usr-admin-404',
    firebaseUid: 'mock-admin-1',
    email: 'admin.zayd@openmosque.org',
    displayName: 'Zayd Ibn Haritha (Super Admin)',
    phoneNumber: '+1 650 555 0199',
    photoUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80',
    role: 'SUPER_ADMIN',
    points: 1540,
    isActive: true,
    isVerified: true,
    twoFactorEnabled: true,
    createdAt: '2025-01-01T00:00:00Z',
  },
};

interface AuthStore {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  activePersonaKey: string | null;
  bookmarks: string[];

  // Actions
  loginWithMockPersona: (personaKey: keyof typeof MOCK_PERSONAS) => void;
  loginWithUser: (user: User, token: string) => void;
  updateUserRole: (role: UserRole) => void;
  awardPoints: (pts: number) => void;
  toggle2FA: (enabled: boolean) => void;
  logout: () => void;
  toggleBookmark: (mosqueId: string) => void;
  isBookmarked: (mosqueId: string) => boolean;
}

const STORAGE_KEY_AUTH = 'openmosque_auth_user';
const STORAGE_KEY_TOKEN = 'openmosque_auth_token';
const STORAGE_KEY_BOOKMARKS = 'openmosque_bookmarks';
const STORAGE_KEY_PERSONA = 'openmosque_persona_key';

const getInitialUser = (): User | null => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY_AUTH);
    return raw ? JSON.parse(raw) : MOCK_PERSONAS.contributor;
  } catch {
    return MOCK_PERSONAS.contributor;
  }
};

const getInitialToken = (): string | null => {
  try {
    return localStorage.getItem(STORAGE_KEY_TOKEN) || 'mock-jwt-token-contributor';
  } catch {
    return 'mock-jwt-token-contributor';
  }
};

const getInitialBookmarks = (): string[] => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY_BOOKMARKS);
    return raw ? JSON.parse(raw) : ['18558e48-a2aa-4219-9583-54cd9a84a76c'];
  } catch {
    return ['18558e48-a2aa-4219-9583-54cd9a84a76c'];
  }
};

export const useAuthStore = create<AuthStore>((set, get) => ({
  user: getInitialUser(),
  token: getInitialToken(),
  isAuthenticated: true,
  isLoading: false,
  activePersonaKey: localStorage.getItem(STORAGE_KEY_PERSONA) || 'contributor',
  bookmarks: getInitialBookmarks(),

  loginWithMockPersona: (personaKey) => {
    const persona = MOCK_PERSONAS[personaKey];
    if (!persona) return;
    const mockToken = `mock-jwt-token-${personaKey}`;
    try {
      localStorage.setItem(STORAGE_KEY_AUTH, JSON.stringify(persona));
      localStorage.setItem(STORAGE_KEY_TOKEN, mockToken);
      localStorage.setItem(STORAGE_KEY_PERSONA, String(personaKey));
    } catch {
      // Ignore in environments without localStorage
    }
    set({
      user: persona,
      token: mockToken,
      isAuthenticated: true,
      activePersonaKey: String(personaKey),
    });
  },

  loginWithUser: (user, token) => {
    try {
      localStorage.setItem(STORAGE_KEY_AUTH, JSON.stringify(user));
      localStorage.setItem(STORAGE_KEY_TOKEN, token);
      localStorage.removeItem(STORAGE_KEY_PERSONA);
    } catch {
      // Ignore
    }
    set({
      user,
      token,
      isAuthenticated: true,
      activePersonaKey: null,
    });
  },

  updateUserRole: (role) => {
    const currentUser = get().user;
    if (!currentUser) return;
    const updated = { ...currentUser, role };
    try {
      localStorage.setItem(STORAGE_KEY_AUTH, JSON.stringify(updated));
    } catch {
      // Ignore
    }
    set({ user: updated });
  },

  awardPoints: (pts) => {
    const currentUser = get().user;
    if (!currentUser) return;
    const updated = { ...currentUser, points: currentUser.points + pts };
    try {
      localStorage.setItem(STORAGE_KEY_AUTH, JSON.stringify(updated));
    } catch {
      // Ignore
    }
    set({ user: updated });
  },

  toggle2FA: (enabled) => {
    const currentUser = get().user;
    if (!currentUser) return;
    const updated = { ...currentUser, twoFactorEnabled: enabled };
    try {
      localStorage.setItem(STORAGE_KEY_AUTH, JSON.stringify(updated));
    } catch {
      // Ignore
    }
    set({ user: updated });
  },

  logout: () => {
    try {
      localStorage.removeItem(STORAGE_KEY_AUTH);
      localStorage.removeItem(STORAGE_KEY_TOKEN);
      localStorage.removeItem(STORAGE_KEY_PERSONA);
    } catch {
      // Ignore
    }
    set({
      user: null,
      token: null,
      isAuthenticated: false,
      activePersonaKey: null,
    });
  },

  toggleBookmark: (mosqueId) => {
    const { bookmarks } = get();
    const exists = bookmarks.includes(mosqueId);
    const updated = exists ? bookmarks.filter((id) => id !== mosqueId) : [...bookmarks, mosqueId];
    try {
      localStorage.setItem(STORAGE_KEY_BOOKMARKS, JSON.stringify(updated));
    } catch {
      // Ignore
    }
    set({ bookmarks: updated });
  },

  isBookmarked: (mosqueId) => {
    return get().bookmarks.includes(mosqueId);
  },
}));
