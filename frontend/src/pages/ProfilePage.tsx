import React, { useState, useEffect } from 'react';
import { useAuthStore } from '../stores/authStore';
import { mosquesApi } from '../api/mosques';
import { adminApi } from '../api/admin';
import { MosqueCard } from '../components/mosque/MosqueCard';
import { TwoFactorModal } from '../components/profile/TwoFactorModal';
import { PushNotificationSettings } from '../components/profile/PushNotificationSettings';
import type { Mosque, MosqueSubmission } from '../types';
import {
  Award,
  ShieldCheck,
  Bookmark,
  Send,
  Lock,
} from 'lucide-react';
import { Link } from 'react-router-dom';

export const ProfilePage: React.FC = () => {
  const { user, bookmarks, isAuthenticated } = useAuthStore();
  const [bookmarkedMosques, setBookmarkedMosques] = useState<Mosque[]>([]);
  const [submissions, setSubmissions] = useState<MosqueSubmission[]>([]);
  const [is2FAModalOpen, setIs2FAModalOpen] = useState(false);
  const [activeTab, setActiveTab] = useState<'bookmarks' | 'submissions' | 'security'>('bookmarks');

  useEffect(() => {
    // Load bookmarked mosques
    mosquesApi.search({ size: 20 }).then((res) => {
      const filtered = res.content.filter((m) => bookmarks.includes(m.id));
      setBookmarkedMosques(filtered);
    });

    // Load user's submissions
    adminApi.getSubmissions().then((subs) => {
      setSubmissions(subs);
    });
  }, [bookmarks]);

  if (!isAuthenticated || !user) {
    return (
      <div className="max-w-md mx-auto px-4 py-20 text-center space-y-4">
        <h2 className="text-2xl font-bold text-slate-900">Please Sign In</h2>
        <p className="text-xs text-slate-500">Sign in to view your contributor points, bookmarks, and submissions.</p>
      </div>
    );
  }

  // Gamification Tier logic adhering to PRD Section 5.6
  let tierName = 'Neighborhood Scout';
  let tierBadgeColor = 'bg-amber-100 text-amber-900 border-amber-300';
  let nextTierPoints = 200;
  let prevTierPoints = 0;

  if (user.points >= 500) {
    tierName = 'Master Chronicler';
    tierBadgeColor = 'bg-purple-100 text-purple-900 border-purple-300';
    nextTierPoints = 1000;
    prevTierPoints = 500;
  } else if (user.points >= 200) {
    tierName = 'Community Pillar';
    tierBadgeColor = 'bg-blue-100 text-blue-900 border-blue-300';
    nextTierPoints = 500;
    prevTierPoints = 200;
  }

  const progressPercent = Math.min(
    100,
    Math.max(5, ((user.points - prevTierPoints) / (nextTierPoints - prevTierPoints)) * 100)
  );

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Profile Overview & Contributor Gamification Header */}
      <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200 shadow-sm grid grid-cols-1 lg:grid-cols-12 gap-6 items-center">
        {/* User Card */}
        <div className="lg:col-span-6 flex items-center gap-4">
          <img
            src={
              user.photoUrl ||
              'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80'
            }
            alt={user.displayName}
            className="w-20 h-20 rounded-2xl object-cover border-2 border-emerald-500 shadow-md"
          />
          <div className="space-y-1">
            <div className="flex items-center gap-2 flex-wrap">
              <h1 className="text-xl font-bold text-slate-900">{user.displayName}</h1>
              <span className="px-2.5 py-0.5 rounded-md text-[10px] font-bold uppercase tracking-wider bg-emerald-100 text-emerald-800">
                {user.role}
              </span>
            </div>
            <p className="text-xs text-slate-500">{user.email}</p>
            {user.phoneNumber && <p className="text-xs text-slate-400">{user.phoneNumber}</p>}
          </div>
        </div>

        {/* Contributor Gamification Tier Card */}
        <div className="lg:col-span-6 bg-slate-50 rounded-2xl p-5 border border-slate-200 space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Award className="w-5 h-5 text-amber-500" />
              <span className="text-xs font-bold uppercase tracking-wider text-slate-600">
                Contributor Level
              </span>
            </div>
            <span className={`px-2.5 py-0.5 rounded-full text-xs font-bold border ${tierBadgeColor}`}>
              {tierName}
            </span>
          </div>

          <div className="flex items-baseline justify-between">
            <span className="text-2xl font-extrabold font-mono text-slate-900">
              {user.points} <span className="text-xs font-normal text-slate-500 font-sans">Points</span>
            </span>
            <span className="text-[11px] text-slate-500 font-medium">
              Next Goal: {nextTierPoints} pts
            </span>
          </div>

          {/* Progress bar */}
          <div className="h-2 w-full bg-slate-200 rounded-full overflow-hidden">
            <div
              className="h-full bg-emerald-600 rounded-full transition-all duration-500"
              style={{ width: `${progressPercent}%` }}
            ></div>
          </div>

          <p className="text-[11px] text-slate-400 leading-tight">
            Earn <strong>+100 pts</strong> for submitting a new mosque and <strong>+50 pts</strong> for verified corrections.
          </p>
        </div>
      </div>

      {/* Profile Section Tabs */}
      <div className="space-y-6">
        <div className="border-b border-slate-200">
          <nav className="flex space-x-6">
            <button
              onClick={() => setActiveTab('bookmarks')}
              className={`py-3 px-1 text-sm font-semibold border-b-2 transition-colors flex items-center gap-2 ${
                activeTab === 'bookmarks'
                  ? 'border-emerald-600 text-emerald-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              <Bookmark className="w-4 h-4" />
              Bookmarked Mosques ({bookmarkedMosques.length})
            </button>

            <button
              onClick={() => setActiveTab('submissions')}
              className={`py-3 px-1 text-sm font-semibold border-b-2 transition-colors flex items-center gap-2 ${
                activeTab === 'submissions'
                  ? 'border-emerald-600 text-emerald-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              <Send className="w-4 h-4" />
              My Submissions ({submissions.length})
            </button>

            <button
              onClick={() => setActiveTab('security')}
              className={`py-3 px-1 text-sm font-semibold border-b-2 transition-colors flex items-center gap-2 ${
                activeTab === 'security'
                  ? 'border-emerald-600 text-emerald-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              <Lock className="w-4 h-4" />
              Security & Notifications
            </button>
          </nav>
        </div>

        {/* Tab 1: Bookmarks */}
        {activeTab === 'bookmarks' && (
          <div className="space-y-4">
            {bookmarkedMosques.length === 0 ? (
              <div className="bg-white rounded-3xl p-12 text-center border border-slate-200 space-y-3">
                <Bookmark className="w-10 h-10 text-slate-300 mx-auto" />
                <h4 className="text-slate-800 font-bold text-sm">No Bookmarked Mosques Yet</h4>
                <p className="text-xs text-slate-500 max-w-sm mx-auto">
                  Click the bookmark icon on any mosque card to pin your favourite congregation places for quick prayer timetable access.
                </p>
                <Link
                  to="/"
                  className="inline-block px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-xl"
                >
                  Explore Map Directory
                </Link>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {bookmarkedMosques.map((m) => (
                  <MosqueCard key={m.id} mosque={m} />
                ))}
              </div>
            )}
          </div>
        )}

        {/* Tab 2: My Submissions */}
        {activeTab === 'submissions' && (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-slate-900">Your Mosque Proposals</h3>
              <Link
                to="/contribute"
                className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-xl transition-all shadow-sm"
              >
                + Propose Another Mosque
              </Link>
            </div>

            <div className="space-y-3">
              {submissions.map((sub) => (
                <div
                  key={sub.id}
                  className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4"
                >
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <h4 className="font-bold text-slate-900 text-base">{sub.name}</h4>
                      <span
                        className={`px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider ${
                          sub.status === 'APPROVED'
                            ? 'bg-emerald-100 text-emerald-800'
                            : sub.status === 'REJECTED'
                            ? 'bg-rose-100 text-rose-800'
                            : 'bg-amber-100 text-amber-800'
                        }`}
                      >
                        {sub.status}
                      </span>
                    </div>

                    <p className="text-xs text-slate-500">
                      {sub.address}, {sub.city}, {sub.country}
                    </p>

                    {sub.reviewComments && (
                      <p className="text-xs text-slate-600 italic bg-slate-50 p-2 rounded-lg border border-slate-100 mt-1">
                        Reviewer feedback: "{sub.reviewComments}"
                      </p>
                    )}
                  </div>

                  <div className="text-xs text-slate-400 self-start sm:self-center font-mono">
                    {new Date(sub.createdAt).toLocaleDateString('en-GB')}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Tab 3: Security & Notifications */}
        {activeTab === 'security' && (
          <div className="space-y-6">
            {/* 2FA/TOTP Setup Card */}
            <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-6">
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <ShieldCheck className="w-5 h-5 text-emerald-600" />
                  <h3 className="text-base font-bold text-slate-900">
                    Two-Factor Authentication (TOTP)
                  </h3>
                </div>
                <p className="text-xs text-slate-500 max-w-md">
                  Secure your account using time-based one-time codes generated by Google Authenticator or 1Password.
                </p>
                <div className="pt-1">
                  <span
                    className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold ${
                      user.twoFactorEnabled
                        ? 'bg-emerald-100 text-emerald-800'
                        : 'bg-slate-100 text-slate-600'
                    }`}
                  >
                    {user.twoFactorEnabled ? '✓ 2FA Enabled' : '○ 2FA Not Configured'}
                  </span>
                </div>
              </div>

              <button
                onClick={() => setIs2FAModalOpen(true)}
                className="px-4 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-xl transition-all shadow-sm self-start sm:self-center"
              >
                {user.twoFactorEnabled ? 'Reconfigure 2FA' : 'Enable 2FA Protection'}
              </button>
            </div>

            {/* Push Notifications Card */}
            <PushNotificationSettings />
          </div>
        )}
      </div>

      <TwoFactorModal isOpen={is2FAModalOpen} onClose={() => setIs2FAModalOpen(false)} />
    </div>
  );
};
