import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../stores/authStore';
import { Compass, BookOpen, PlusCircle, ShieldCheck, User as UserIcon, LogOut, ChevronDown, Award } from 'lucide-react';
import { AuthModal } from '../auth/AuthModal';

export const Navbar: React.FC = () => {
  const location = useLocation();
  const { user, isAuthenticated, logout } = useAuthStore();
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  const isActive = (path: string) => location.pathname === path;

  const isModOrAdmin = user?.role === 'MODERATOR' || user?.role === 'SUPER_ADMIN';

  return (
    <>
      <header className="sticky top-0 z-40 bg-white/95 backdrop-blur-md border-b border-slate-200">
        {/* Spiritual Bismillah Bar */}
        <div className="bg-emerald-800 text-emerald-100 text-xs py-1 px-4 text-center font-arabic tracking-widest select-none">
          بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ • In the name of Allah, Most Gracious, Most Merciful
        </div>

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          {/* Brand Logo */}
          <Link to="/" className="flex items-center gap-3 group">
            <div className="w-10 h-10 rounded-xl bg-emerald-600 flex items-center justify-center text-white shadow-md shadow-emerald-600/20 group-hover:bg-emerald-700 transition-all">
              <Compass className="w-6 h-6" />
            </div>
            <div>
              <span className="text-xl font-bold tracking-tight text-slate-900 block leading-tight">
                Open<span className="text-emerald-600">Mosque</span>
              </span>
              <span className="text-[10px] uppercase font-semibold tracking-wider text-slate-400 block -mt-0.5">
                Global Directory
              </span>
            </div>
          </Link>

          {/* Navigation Links */}
          <nav className="hidden md:flex items-center gap-1">
            <Link
              to="/"
              className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-1.5 ${
                isActive('/')
                  ? 'text-emerald-700 bg-emerald-50 font-semibold'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
              }`}
            >
              <Compass className="w-4 h-4" />
              Explore Map
            </Link>

            <Link
              to="/mosques"
              className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-1.5 ${
                isActive('/mosques')
                  ? 'text-emerald-700 bg-emerald-50 font-semibold'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
              }`}
            >
              <BookOpen className="w-4 h-4" />
              Directory
            </Link>

            <Link
              to="/contribute"
              className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-1.5 ${
                isActive('/contribute')
                  ? 'text-emerald-700 bg-emerald-50 font-semibold'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
              }`}
            >
              <PlusCircle className="w-4 h-4 text-emerald-600" />
              Add Mosque
            </Link>

            {isModOrAdmin && (
              <Link
                to="/admin"
                className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-1.5 ${
                  isActive('/admin')
                    ? 'text-emerald-700 bg-emerald-50 font-semibold'
                    : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                }`}
              >
                <ShieldCheck className="w-4 h-4 text-amber-600" />
                Moderation
              </Link>
            )}
          </nav>

          {/* User Auth Section */}
          <div className="flex items-center gap-3">
            {isAuthenticated && user ? (
              <div className="relative">
                <button
                  onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                  className="flex items-center gap-2.5 p-1.5 pl-3 rounded-full border border-slate-200 bg-slate-50 hover:bg-slate-100 transition-colors focus:outline-none"
                >
                  <div className="flex items-center gap-1.5 text-xs font-semibold text-amber-700 bg-amber-50 px-2 py-0.5 rounded-full border border-amber-200">
                    <Award className="w-3.5 h-3.5" />
                    <span>{user.points} pts</span>
                  </div>

                  <img
                    src={user.photoUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop&q=80'}
                    alt={user.displayName}
                    className="w-7 h-7 rounded-full object-cover border border-emerald-500"
                  />
                  <ChevronDown className="w-3.5 h-3.5 text-slate-500 mr-1" />
                </button>

                {isDropdownOpen && (
                  <div
                    className="absolute right-0 mt-2 w-64 rounded-xl bg-white border border-slate-200 shadow-xl py-2 z-50 animate-in fade-in slide-in-from-top-2 duration-150"
                    onClick={() => setIsDropdownOpen(false)}
                  >
                    <div className="px-4 py-2 border-b border-slate-100">
                      <p className="text-sm font-semibold text-slate-900 truncate">{user.displayName}</p>
                      <p className="text-xs text-slate-500 truncate">{user.email}</p>
                      <span className="inline-block mt-1 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider rounded-md bg-emerald-100 text-emerald-800">
                        {user.role}
                      </span>
                    </div>

                    <Link
                      to="/profile"
                      className="w-full text-left px-4 py-2 text-sm text-slate-700 hover:bg-slate-50 flex items-center gap-2"
                    >
                      <UserIcon className="w-4 h-4 text-slate-400" />
                      Profile & Bookmarks
                    </Link>

                    {isModOrAdmin && (
                      <Link
                        to="/admin"
                        className="w-full text-left px-4 py-2 text-sm text-slate-700 hover:bg-slate-50 flex items-center gap-2"
                      >
                        <ShieldCheck className="w-4 h-4 text-amber-600" />
                        Admin Dashboard
                      </Link>
                    )}

                    <div className="border-t border-slate-100 my-1"></div>

                    <button
                      onClick={logout}
                      className="w-full text-left px-4 py-2 text-sm text-rose-600 hover:bg-rose-50 flex items-center gap-2"
                    >
                      <LogOut className="w-4 h-4" />
                      Sign Out
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <button
                onClick={() => setIsAuthModalOpen(true)}
                className="px-4 py-2 text-sm font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl transition-all shadow-sm shadow-emerald-600/20 flex items-center gap-1.5"
              >
                <UserIcon className="w-4 h-4" />
                Sign In
              </button>
            )}
          </div>
        </div>
      </header>

      <AuthModal isOpen={isAuthModalOpen} onClose={() => setIsAuthModalOpen(false)} />
    </>
  );
};
