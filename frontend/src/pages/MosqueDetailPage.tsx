import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { mosquesApi } from '../api/mosques';
import { prayerApi } from '../api/prayer';
import { facilitiesApi } from '../api/facilities';
import { useAuthStore } from '../stores/authStore';
import { useToastStore } from '../stores/toastStore';
import { PrayerCountdownWidget } from '../components/prayer/PrayerCountdownWidget';
import { DailyPrayerTimetable } from '../components/prayer/DailyPrayerTimetable';
import { MosqueReviews } from '../components/reviews/MosqueReviews';
import { MosqueQA } from '../components/qa/MosqueQA';
import { MosqueEvents } from '../components/events/MosqueEvents';
import { MosqueKhutbahs } from '../components/khutbah/MosqueKhutbahs';
import { PhotoGalleryModal } from '../components/mosque/PhotoGalleryModal';
import { SuggestEditModal } from '../components/mosque/SuggestEditModal';
import type { MosqueDetail, PrayerTimesDto, Facility } from '../types';
import {
  MapPin,
  CheckCircle2,
  Bookmark,
  Share2,
  Edit3,
  Shield,
  Phone,
  Mail,
  Globe,
  Navigation2,
  Images,
  Radio,
  Star,
  MessageSquare,
  Calendar,
  Layers,
} from 'lucide-react';

export const MosqueDetailPage: React.FC = () => {
  const { idOrSlug } = useParams<{ idOrSlug: string }>();
  const [mosque, setMosque] = useState<MosqueDetail | null>(null);
  const [prayerTimes, setPrayerTimes] = useState<PrayerTimesDto | null>(null);
  const [allFacilities, setAllFacilities] = useState<Facility[]>([]);
  const [activeTab, setActiveTab] = useState<'reviews' | 'qa' | 'events' | 'khutbahs'>('reviews');

  const [isGalleryOpen, setIsGalleryOpen] = useState(false);
  const [isSuggestEditOpen, setIsSuggestEditOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  const { isBookmarked, toggleBookmark } = useAuthStore();
  const { addToast } = useToastStore();

  useEffect(() => {
    if (!idOrSlug) return;
    setIsLoading(true);

    Promise.all([
      mosquesApi.getByIdOrSlug(idOrSlug),
      prayerApi.getPrayerTimes(idOrSlug),
      facilitiesApi.getAll(),
    ])
      .then(([mosqueData, prayerData, facs]) => {
        setMosque(mosqueData);
        setPrayerTimes(prayerData);
        setAllFacilities(facs);
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, [idOrSlug]);

  if (isLoading || !mosque) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="bg-white rounded-3xl h-96 border border-slate-200 animate-pulse"></div>
      </div>
    );
  }

  const bookmarked = isBookmarked(mosque.id);

  const handleShare = () => {
    navigator.clipboard.writeText(window.location.href);
    addToast('Mosque profile link copied to clipboard!', 'success');
  };

  const googleMapsUrl = `https://www.google.com/maps/search/?api=1&query=${mosque.latitude},${mosque.longitude}`;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Top Breadcrumbs & Actions Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-2 text-xs font-medium text-slate-500">
          <Link to="/" className="hover:text-emerald-700 transition-colors">Home</Link>
          <span>/</span>
          <Link to="/mosques" className="hover:text-emerald-700 transition-colors">Directory</Link>
          <span>/</span>
          <span className="text-slate-900 font-semibold truncate max-w-xs">{mosque.name}</span>
        </div>

        <div className="flex items-center gap-2 flex-wrap">
          <button
            onClick={() => toggleBookmark(mosque.id)}
            className={`px-3.5 py-2 rounded-xl text-xs font-semibold border transition-all flex items-center gap-1.5 ${
              bookmarked
                ? 'bg-amber-50 border-amber-300 text-amber-800'
                : 'bg-white border-slate-200 text-slate-700 hover:bg-slate-50'
            }`}
          >
            <Bookmark className={`w-3.5 h-3.5 ${bookmarked ? 'fill-amber-500 text-amber-500' : ''}`} />
            {bookmarked ? 'Bookmarked' : 'Bookmark'}
          </button>

          <button
            onClick={handleShare}
            className="px-3.5 py-2 rounded-xl text-xs font-semibold bg-white border border-slate-200 text-slate-700 hover:bg-slate-50 transition-all flex items-center gap-1.5"
          >
            <Share2 className="w-3.5 h-3.5" />
            Share
          </button>

          <button
            onClick={() => setIsSuggestEditOpen(true)}
            className="px-3.5 py-2 rounded-xl text-xs font-semibold bg-white border border-slate-200 text-slate-700 hover:bg-slate-50 transition-all flex items-center gap-1.5"
          >
            <Edit3 className="w-3.5 h-3.5 text-amber-600" />
            Suggest Edit (+50 pts)
          </button>

          <Link
            to={`/mosques/${mosque.id}/claim`}
            className="px-3.5 py-2 rounded-xl text-xs font-semibold bg-emerald-50 border border-emerald-300 text-emerald-800 hover:bg-emerald-100 transition-all flex items-center gap-1.5"
          >
            <Shield className="w-3.5 h-3.5 text-emerald-700" />
            Imam / Trustee Claim
          </Link>
        </div>
      </div>

      {/* Mosque Hero Banner */}
      <div className="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden">
        {/* Photo Gallery Grid Preview */}
        <div className="relative h-64 sm:h-80 w-full bg-slate-900 group cursor-pointer" onClick={() => setIsGalleryOpen(true)}>
          <img
            src={mosque.coverImageUrl || (mosque.images && mosque.images[0]?.imageUrl) || ''}
            alt={mosque.name}
            className="w-full h-full object-cover group-hover:scale-102 transition-transform duration-300 opacity-95"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent"></div>

          {/* Floating Photo Gallery Trigger */}
          <button
            onClick={(e) => {
              e.stopPropagation();
              setIsGalleryOpen(true);
            }}
            className="absolute bottom-4 right-4 bg-white/90 hover:bg-white text-slate-900 text-xs font-bold px-3.5 py-2 rounded-xl shadow-lg backdrop-blur-md flex items-center gap-2 transition-all"
          >
            <Images className="w-4 h-4 text-emerald-600" />
            View Photo Gallery ({mosque.images?.length || 1})
          </button>
        </div>

        {/* Header Details */}
        <div className="p-6 sm:p-8 space-y-4">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
              <div className="flex items-center gap-2.5 flex-wrap">
                <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
                  {mosque.name}
                </h1>
                {mosque.isVerified && (
                  <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800 border border-emerald-200">
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                    Verified Official Mosque
                  </span>
                )}
              </div>

              <p className="text-sm text-slate-500 mt-1 flex items-center gap-1.5">
                <MapPin className="w-4 h-4 text-slate-400 flex-shrink-0" />
                <span>{mosque.address}, {mosque.city}, {mosque.country} {mosque.postalCode}</span>
              </p>
            </div>

            <a
              href={googleMapsUrl}
              target="_blank"
              rel="noreferrer"
              className="px-4 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-xl transition-all shadow-md shadow-emerald-600/20 flex items-center gap-2 self-start md:self-center"
            >
              <Navigation2 className="w-4 h-4" />
              Navigate with Maps
            </a>
          </div>

          <p className="text-sm text-slate-600 leading-relaxed max-w-4xl">
            {mosque.description}
          </p>

          {/* Contact Details Bar */}
          <div className="pt-4 border-t border-slate-100 flex flex-wrap items-center gap-4 sm:gap-6 text-xs text-slate-600">
            {mosque.contactPhone && (
              <a href={`tel:${mosque.contactPhone}`} className="flex items-center gap-1.5 hover:text-emerald-700 transition-colors font-medium">
                <Phone className="w-3.5 h-3.5 text-emerald-600" />
                <span>{mosque.contactPhone}</span>
              </a>
            )}
            {mosque.contactEmail && (
              <a href={`mailto:${mosque.contactEmail}`} className="flex items-center gap-1.5 hover:text-emerald-700 transition-colors font-medium">
                <Mail className="w-3.5 h-3.5 text-emerald-600" />
                <span>{mosque.contactEmail}</span>
              </a>
            )}
            {mosque.websiteUrl && (
              <a href={mosque.websiteUrl} target="_blank" rel="noreferrer" className="flex items-center gap-1.5 hover:text-emerald-700 transition-colors font-medium">
                <Globe className="w-3.5 h-3.5 text-emerald-600" />
                <span>Official Website</span>
              </a>
            )}
            {mosque.liveStreamUrl && (
              <a href={mosque.liveStreamUrl} target="_blank" rel="noreferrer" className="flex items-center gap-1.5 text-rose-600 hover:text-rose-700 transition-colors font-bold">
                <Radio className="w-3.5 h-3.5" />
                <span>Live Khutbah Stream</span>
              </a>
            )}
          </div>
        </div>
      </div>

      {/* Dynamic Prayer Countdown Clock */}
      {prayerTimes && <PrayerCountdownWidget prayerTimes={prayerTimes} />}

      {/* Daily Prayer & Iqamah Timetable */}
      {prayerTimes && <DailyPrayerTimetable prayerTimes={prayerTimes} />}

      {/* Facilities & Amenities Checklist */}
      <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200 shadow-sm space-y-4">
        <h3 className="text-lg font-bold text-slate-900 flex items-center gap-2">
          <Layers className="w-5 h-5 text-emerald-600" />
          Facilities & Islamic Amenities
        </h3>
        <p className="text-xs text-slate-500">
          Verified features available for worshippers and visiting families
        </p>

        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-3 pt-2">
          {allFacilities.map((facility) => {
            const hasFacility = mosque.facilities.includes(facility.code);

            return (
              <div
                key={facility.id}
                className={`p-3.5 rounded-2xl border flex items-center gap-3 transition-colors ${
                  hasFacility
                    ? 'bg-emerald-50/60 border-emerald-200 text-slate-900'
                    : 'bg-slate-50/50 border-slate-100 text-slate-400 opacity-60'
                }`}
              >
                <div
                  className={`w-8 h-8 rounded-xl flex items-center justify-center font-bold text-xs ${
                    hasFacility ? 'bg-emerald-600 text-white' : 'bg-slate-200 text-slate-400'
                  }`}
                >
                  {hasFacility ? '✓' : '—'}
                </div>
                <div>
                  <h4 className="text-xs font-bold leading-tight">{facility.name}</h4>
                  <span className="text-[10px] text-slate-500">{hasFacility ? 'Available on-site' : 'Not available'}</span>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Tabbed Community Engagement Section (Phase 4) */}
      <div className="space-y-6">
        <div className="border-b border-slate-200">
          <nav className="flex space-x-6">
            <button
              onClick={() => setActiveTab('reviews')}
              className={`py-3 px-1 text-sm font-semibold border-b-2 transition-colors flex items-center gap-2 ${
                activeTab === 'reviews'
                  ? 'border-emerald-600 text-emerald-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              <Star className="w-4 h-4" />
              Community Reviews
            </button>

            <button
              onClick={() => setActiveTab('qa')}
              className={`py-3 px-1 text-sm font-semibold border-b-2 transition-colors flex items-center gap-2 ${
                activeTab === 'qa'
                  ? 'border-emerald-600 text-emerald-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              <MessageSquare className="w-4 h-4" />
              Verified Q&A
            </button>

            <button
              onClick={() => setActiveTab('events')}
              className={`py-3 px-1 text-sm font-semibold border-b-2 transition-colors flex items-center gap-2 ${
                activeTab === 'events'
                  ? 'border-emerald-600 text-emerald-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              <Calendar className="w-4 h-4" />
              Events & Halaqahs
            </button>

            <button
              onClick={() => setActiveTab('khutbahs')}
              className={`py-3 px-1 text-sm font-semibold border-b-2 transition-colors flex items-center gap-2 ${
                activeTab === 'khutbahs'
                  ? 'border-emerald-600 text-emerald-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              <Radio className="w-4 h-4" />
              Friday Khutbahs
            </button>
          </nav>
        </div>

        {/* Tab Content */}
        {activeTab === 'reviews' && <MosqueReviews mosqueId={mosque.id} />}
        {activeTab === 'qa' && <MosqueQA mosqueId={mosque.id} />}
        {activeTab === 'events' && <MosqueEvents mosqueId={mosque.id} />}
        {activeTab === 'khutbahs' && <MosqueKhutbahs mosqueId={mosque.id} />}
      </div>

      {/* Lightbox Photo Gallery Modal */}
      {mosque.images && (
        <PhotoGalleryModal
          isOpen={isGalleryOpen}
          onClose={() => setIsGalleryOpen(false)}
          images={mosque.images}
        />
      )}

      {/* Suggest Edit Modal */}
      <SuggestEditModal
        isOpen={isSuggestEditOpen}
        onClose={() => setIsSuggestEditOpen(false)}
        mosque={mosque}
      />
    </div>
  );
};
