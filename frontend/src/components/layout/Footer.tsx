import React from 'react';
import { Link } from 'react-router-dom';
import { Compass, Heart, Globe, Shield, ExternalLink } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-white border-t border-slate-200 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Column 1: Brand & Spiritual mission */}
          <div className="md:col-span-2 space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-emerald-600 flex items-center justify-center text-white shadow-md shadow-emerald-600/20">
                <Compass className="w-5 h-5" />
              </div>
              <span className="text-xl font-bold tracking-tight text-slate-900">
                Open<span className="text-emerald-600">Mosque</span>
              </span>
            </div>
            <p className="text-sm text-slate-600 max-w-md leading-relaxed">
              OpenMosque is a community-driven, open-source global platform connecting worshippers worldwide to verified mosques, accurate prayer and Iqamah times, and local community events.
            </p>
            <div className="flex items-center gap-4 text-slate-400">
              <a href="https://github.com" target="_blank" rel="noreferrer" className="hover:text-slate-700 transition-colors" aria-label="GitHub Repository">
                <svg className="w-5 h-5 fill-current" viewBox="0 0 24 24">
                  <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/>
                </svg>
              </a>
              <a href="http://localhost:8080/swagger-ui.html" target="_blank" rel="noreferrer" className="hover:text-slate-700 transition-colors flex items-center gap-1 text-xs">
                <Globe className="w-4 h-4" /> Swagger Backend Docs <ExternalLink className="w-3 h-3" />
              </a>
            </div>
          </div>

          {/* Column 2: Quick Links */}
          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-4">Platform</h4>
            <ul className="space-y-2.5 text-sm">
              <li>
                <Link to="/" className="text-slate-600 hover:text-emerald-600 transition-colors">
                  Interactive Map Explorer
                </Link>
              </li>
              <li>
                <Link to="/mosques" className="text-slate-600 hover:text-emerald-600 transition-colors">
                  Mosque Directory
                </Link>
              </li>
              <li>
                <Link to="/contribute" className="text-slate-600 hover:text-emerald-600 transition-colors">
                  Submit New Mosque (+100 pts)
                </Link>
              </li>
              <li>
                <Link to="/profile" className="text-slate-600 hover:text-emerald-600 transition-colors">
                  Contributor Leaderboard
                </Link>
              </li>
            </ul>
          </div>

          {/* Column 3: Administration & Community */}
          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-4">Administration</h4>
            <ul className="space-y-2.5 text-sm">
              <li>
                <Link to="/admin" className="text-slate-600 hover:text-emerald-600 transition-colors flex items-center gap-1.5">
                  <Shield className="w-3.5 h-3.5 text-amber-600" />
                  Moderation Portal
                </Link>
              </li>
              <li>
                <span className="text-slate-400 text-xs block">
                  Imams & Trustees can claim official profiles directly on their mosque detail page.
                </span>
              </li>
            </ul>
          </div>
        </div>

        <div className="border-t border-slate-100 mt-8 pt-6 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-500">
          <p>© {new Date().getFullYear()} OpenMosque Global Platform. Open Source under MIT License.</p>
          <div className="flex items-center gap-1">
            <span>Built with sincere effort</span>
            <Heart className="w-3.5 h-3.5 text-rose-500 fill-rose-500" />
            <span>for the global Ummah</span>
          </div>
        </div>
      </div>
    </footer>
  );
};
