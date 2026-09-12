import React from 'react';
import { Link } from 'react-router-dom';
import type { Mosque } from '../../types';
import { useAuthStore } from '../../stores/authStore';
import {
  MapPin,
  CheckCircle2,
  Bookmark,
  Clock,
  Navigation,
  Droplets,
  Users,
  Car,
  Accessibility,
  BookOpen,
  Wind,
} from 'lucide-react';

interface MosqueCardProps {
  mosque: Mosque;
  isSelected?: boolean;
  onSelect?: (mosque: Mosque) => void;
}

export const MosqueCard: React.FC<MosqueCardProps> = ({ mosque, isSelected, onSelect }) => {
  const { isBookmarked, toggleBookmark } = useAuthStore();
  const bookmarked = isBookmarked(mosque.id);

  const renderFacilityIcon = (code: string) => {
    switch (code) {
      case 'WUDU_AREA':
        return <span title="Wudu Area"><Droplets className="w-3.5 h-3.5" /></span>;
      case 'WOMENS_SECTION':
        return <span title="Women's Section"><Users className="w-3.5 h-3.5" /></span>;
      case 'PARKING':
        return <span title="Parking Available"><Car className="w-3.5 h-3.5" /></span>;
      case 'WHEELCHAIR':
        return <span title="Wheelchair Accessible"><Accessibility className="w-3.5 h-3.5" /></span>;
      case 'LIBRARY':
        return <span title="Library"><BookOpen className="w-3.5 h-3.5" /></span>;
      case 'AIR_CONDITIONING':
        return <span title="Air Conditioning"><Wind className="w-3.5 h-3.5" /></span>;
      default:
        return null;
    }
  };

  return (
    <div
      onClick={() => onSelect?.(mosque)}
      className={`group bg-white rounded-2xl border transition-all duration-200 overflow-hidden cursor-pointer flex flex-col justify-between ${
        isSelected
          ? 'border-emerald-500 ring-2 ring-emerald-500/20 shadow-lg'
          : 'border-slate-200 hover:border-slate-300 hover:shadow-md'
      }`}
    >
      <div>
        {/* Card Header / Image */}
        <div className="relative h-44 w-full bg-slate-100 overflow-hidden">
          <img
            src={
              mosque.coverImageUrl ||
              'https://images.unsplash.com/photo-1542838132-92c53300491e?w=800&auto=format&fit=crop&q=80'
            }
            alt={mosque.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            loading="lazy"
          />

          {/* Top Badges */}
          <div className="absolute top-3 inset-x-3 flex items-center justify-between">
            {mosque.distanceKm !== undefined ? (
              <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-white/95 text-slate-800 backdrop-blur-md shadow-xs">
                <Navigation className="w-3 h-3 text-emerald-600" />
                {mosque.distanceKm.toFixed(1)} km away
              </span>
            ) : (
              <span />
            )}

            <button
              onClick={(e) => {
                e.stopPropagation();
                toggleBookmark(mosque.id);
              }}
              className="p-2 rounded-full bg-white/95 text-slate-700 hover:text-amber-600 transition-colors backdrop-blur-md shadow-xs"
              aria-label={bookmarked ? 'Remove bookmark' : 'Bookmark mosque'}
            >
              <Bookmark className={`w-4 h-4 ${bookmarked ? 'fill-amber-500 text-amber-500' : ''}`} />
            </button>
          </div>

          {/* Next Prayer Banner */}
          {mosque.nextPrayerName && (
            <div className="absolute bottom-2.5 left-3 bg-emerald-900/90 backdrop-blur-md text-white text-xs px-2.5 py-1 rounded-lg flex items-center gap-1.5 font-medium shadow-xs">
              <Clock className="w-3.5 h-3.5 text-emerald-300" />
              <span>
                {mosque.nextPrayerName} Iqamah: <strong className="text-emerald-200">{mosque.nextPrayerTime}</strong>
              </span>
            </div>
          )}
        </div>

        {/* Card Body */}
        <div className="p-4 space-y-2.5">
          <div className="flex items-start justify-between gap-2">
            <h3 className="font-bold text-slate-900 text-base leading-snug group-hover:text-emerald-700 transition-colors line-clamp-1">
              {mosque.name}
            </h3>
            {mosque.isVerified && (
              <span className="flex-shrink-0" title="Officially Verified Mosque">
                <CheckCircle2 className="w-4 h-4 text-emerald-600" />
              </span>
            )}
          </div>

          <p className="text-xs text-slate-500 flex items-center gap-1.5 line-clamp-1">
            <MapPin className="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
            <span>
              {mosque.address}, {mosque.city}
            </span>
          </p>

          <p className="text-xs text-slate-600 line-clamp-2 leading-relaxed">
            {mosque.description}
          </p>

          {/* Amenities pill icons */}
          <div className="flex items-center gap-1.5 pt-1 flex-wrap">
            {mosque.facilities?.slice(0, 5).map((f) => (
              <span
                key={f}
                className="p-1 rounded-md bg-slate-100 text-slate-600 hover:bg-emerald-50 hover:text-emerald-700 transition-colors"
              >
                {renderFacilityIcon(f)}
              </span>
            ))}
            {mosque.facilities && mosque.facilities.length > 5 && (
              <span className="text-[10px] font-semibold text-slate-400 self-center">
                +{mosque.facilities.length - 5}
              </span>
            )}
          </div>
        </div>
      </div>

      {/* Card Footer */}
      <div className="p-4 pt-0">
        <Link
          to={`/mosques/${mosque.slug || mosque.id}`}
          onClick={(e) => e.stopPropagation()}
          className="w-full py-2 px-3 bg-slate-50 hover:bg-emerald-50 text-slate-700 hover:text-emerald-800 text-xs font-semibold rounded-xl border border-slate-200 hover:border-emerald-200 transition-colors flex items-center justify-center gap-1"
        >
          View Timetable & Profile →
        </Link>
      </div>
    </div>
  );
};
