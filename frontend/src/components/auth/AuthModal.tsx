import React, { useState } from 'react';
import { useAuthStore, MOCK_PERSONAS } from '../../stores/authStore';
import { useToastStore } from '../../stores/toastStore';
import { X, Mail, Lock, LogIn, Sparkles } from 'lucide-react';

interface AuthModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const AuthModal: React.FC<AuthModalProps> = ({ isOpen, onClose }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { loginWithUser, loginWithMockPersona } = useAuthStore();
  const { addToast } = useToastStore();

  if (!isOpen) return null;

  const handleEmailSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      addToast('Please enter both email and password', 'warning');
      return;
    }

    setIsSubmitting(true);
    setTimeout(() => {
      // Create or log in user
      loginWithUser(
        {
          id: 'usr-' + Math.random().toString(36).substring(2, 7),
          firebaseUid: 'custom-uid-' + Date.now(),
          email,
          displayName: email.split('@')[0],
          role: 'USER',
          points: 10,
          isActive: true,
          isVerified: true,
          twoFactorEnabled: false,
          createdAt: new Date().toISOString(),
        },
        'token-' + Date.now()
      );
      setIsSubmitting(false);
      addToast(`Welcome back, ${email.split('@')[0]}!`, 'success');
      onClose();
    }, 600);
  };

  const handleGoogleLogin = () => {
    setIsSubmitting(true);
    setTimeout(() => {
      loginWithMockPersona('contributor');
      setIsSubmitting(false);
      addToast('Successfully authenticated via Google!', 'success');
      onClose();
    }, 500);
  };

  const handleSelectQuickPersona = (key: keyof typeof MOCK_PERSONAS) => {
    loginWithMockPersona(key);
    addToast(`Logged in as ${MOCK_PERSONAS[key].displayName}`, 'success');
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-xs animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl shadow-2xl border border-slate-200 max-w-md w-full p-6 relative">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-slate-400 hover:text-slate-600 p-1.5 rounded-lg transition-colors"
          aria-label="Close"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="text-center mb-6">
          <div className="w-12 h-12 rounded-2xl bg-emerald-100 text-emerald-700 mx-auto flex items-center justify-center mb-3">
            <LogIn className="w-6 h-6" />
          </div>
          <h2 className="text-xl font-bold text-slate-900">Sign in to OpenMosque</h2>
          <p className="text-xs text-slate-500 mt-1">
            Access prayer timetables, community reviews, and earn contributor points
          </p>
        </div>

        {/* Google One-Click Button */}
        <button
          onClick={handleGoogleLogin}
          disabled={isSubmitting}
          className="w-full py-2.5 px-4 border border-slate-300 hover:bg-slate-50 text-slate-700 font-semibold rounded-xl text-sm flex items-center justify-center gap-3 transition-colors shadow-xs mb-4"
        >
          <svg className="w-4 h-4" viewBox="0 0 24 24">
            <path
              fill="#4285F4"
              d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
            />
            <path
              fill="#34A853"
              d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
            />
            <path
              fill="#FBBC05"
              d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
            />
            <path
              fill="#EA4335"
              d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
            />
          </svg>
          Continue with Google
        </button>

        <div className="relative my-4">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-slate-200"></div>
          </div>
          <div className="relative flex justify-center text-xs uppercase">
            <span className="bg-white px-2 text-slate-400 font-semibold">Or sign in with email</span>
          </div>
        </div>

        {/* Email & Password Form */}
        <form onSubmit={handleEmailSubmit} className="space-y-3">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Email address</label>
            <div className="relative">
              <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="worshipper@example.com"
                className="w-full pl-9 pr-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Password</label>
            <div className="relative">
              <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full pl-9 pr-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold rounded-xl text-sm transition-all shadow-md shadow-emerald-600/20 mt-2"
          >
            {isSubmitting ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        {/* Quick Demo Persona Shortcuts */}
        <div className="mt-5 pt-4 border-t border-slate-100">
          <p className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider mb-2 flex items-center gap-1">
            <Sparkles className="w-3 h-3 text-amber-500" />
            Quick Demo Persona Sign In:
          </p>
          <div className="grid grid-cols-2 gap-1.5">
            <button
              onClick={() => handleSelectQuickPersona('contributor')}
              className="px-2.5 py-1.5 text-xs bg-slate-50 hover:bg-emerald-50 hover:text-emerald-700 border border-slate-200 rounded-lg text-left transition-colors font-medium"
            >
              👤 Contributor (180 pts)
            </button>
            <button
              onClick={() => handleSelectQuickPersona('imam')}
              className="px-2.5 py-1.5 text-xs bg-slate-50 hover:bg-emerald-50 hover:text-emerald-700 border border-slate-200 rounded-lg text-left transition-colors font-medium"
            >
              🕌 Imam (Admin)
            </button>
            <button
              onClick={() => handleSelectQuickPersona('moderator')}
              className="px-2.5 py-1.5 text-xs bg-slate-50 hover:bg-emerald-50 hover:text-emerald-700 border border-slate-200 rounded-lg text-left transition-colors font-medium"
            >
              🛡️ Moderator (Queue)
            </button>
            <button
              onClick={() => handleSelectQuickPersona('super_admin')}
              className="px-2.5 py-1.5 text-xs bg-slate-50 hover:bg-emerald-50 hover:text-emerald-700 border border-slate-200 rounded-lg text-left transition-colors font-medium"
            >
              👑 Super Admin (Full)
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
