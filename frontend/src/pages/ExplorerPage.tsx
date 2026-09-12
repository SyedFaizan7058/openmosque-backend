import React, { useState, useEffect } from 'react';
import { mosquesApi } from '../api/mosques';
import { facilitiesApi } from '../api/facilities';
import { MosqueCard } from '../components/mosque/MosqueCard';
import { MosqueMap } from '../components/map/MosqueMap';
import { useToastStore } from '../stores/toastStore';
import type { Mosque, Facility } from '../types';
import {
  Compass,
  Navigation,
  Sliders,
  Filter,
  RotateCcw,
  CheckCircle,
  Building2,
} from 'lucide-react';

export const ExplorerPage: React.FC = () => {
  // Default to London center coordinates (matches initial mock dataset)
  const [userLocation, setUserLocation] = useState<{ latitude: number; longitude: number }>({
    latitude: 51.5173,
    longitude: -0.0658,
  });
  const [locationLabel, setLocationLabel] = useState('London, UK (GPS Default)');
  const [isLocating, setIsLocating] = useState(false);

  const [radiusKm, setRadiusKm] = useState<number>(15);
  const [facilities, setFacilities] = useState<Facility[]>([]);
  const [selectedFacilityCodes, setSelectedFacilityCodes] = useState<string[]>([]);

  const [mosques, setMosques] = useState<Mosque[]>([]);
  const [selectedMosque, setSelectedMosque] = useState<Mosque | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const { addToast } = useToastStore();

  // Load facility catalog on mount
  useEffect(() => {
    facilitiesApi.getAll().then((data) => {
      setFacilities(data);
    });
  }, []);

  // Fetch nearby mosques when location, radius, or facility filter changes
  useEffect(() => {
    setIsLoading(true);
    mosquesApi
      .getNearby({
        latitude: userLocation.latitude,
        longitude: userLocation.longitude,
        radiusKm,
        facilities: selectedFacilityCodes,
      })
      .then((data) => {
        setMosques(data);
        if (data.length > 0 && !selectedMosque) {
          setSelectedMosque(data[0]);
        }
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, [userLocation, radiusKm, selectedFacilityCodes]);

  const handleGetCurrentLocation = () => {
    if (!navigator.geolocation) {
      addToast('Geolocation is not supported by your browser', 'error');
      return;
    }

    setIsLocating(true);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setUserLocation({
          latitude: pos.coords.latitude,
          longitude: pos.coords.longitude,
        });
        setLocationLabel(`Current GPS (${pos.coords.latitude.toFixed(4)}, ${pos.coords.longitude.toFixed(4)})`);
        setIsLocating(false);
        addToast('Updated search to your current GPS position', 'success');
      },
      (err) => {
        setIsLocating(false);
        addToast(`GPS Error: ${err.message}. Using default center.`, 'warning');
      },
      { timeout: 8000 }
    );
  };

  const toggleFacilityFilter = (code: string) => {
    setSelectedFacilityCodes((prev) =>
      prev.includes(code) ? prev.filter((c) => c !== code) : [...prev, code]
    );
  };

  const handleResetFilters = () => {
    setSelectedFacilityCodes([]);
    setRadiusKm(15);
    addToast('Filters reset to default', 'info');
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      {/* Top Banner / Hero */}
      <div className="bg-gradient-to-r from-emerald-800 to-teal-900 rounded-3xl p-6 sm:p-8 text-white shadow-xl relative overflow-hidden">
        <div className="absolute right-0 top-0 bottom-0 w-1/3 bg-white/5 transform skew-x-12 pointer-events-none"></div>
        <div className="relative z-10 max-w-2xl space-y-3">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-700/60 text-emerald-200 text-xs font-semibold backdrop-blur-md border border-emerald-500/30">
            <Compass className="w-3.5 h-3.5" />
            <span>PostGIS Proximity Engine</span>
          </div>
          <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">
            Find Nearby Mosques & Real-time Iqamah Times
          </h1>
          <p className="text-sm text-emerald-100/90 leading-relaxed">
            Locate verified congregation halls, live prayer schedules, dedicated sisters' sections, and parking within your radius.
          </p>

          <div className="pt-2 flex flex-wrap items-center gap-3">
            <button
              onClick={handleGetCurrentLocation}
              disabled={isLocating}
              className="px-4 py-2.5 rounded-xl bg-white text-emerald-900 hover:bg-emerald-50 font-semibold text-xs transition-all shadow-md flex items-center gap-2"
            >
              <Navigation className={`w-4 h-4 text-emerald-600 ${isLocating ? 'animate-spin' : ''}`} />
              {isLocating ? 'Locating...' : 'Use My Current GPS'}
            </button>
            <span className="text-xs text-emerald-200 font-mono">
              Active: <strong>{locationLabel}</strong>
            </span>
          </div>
        </div>
      </div>

      {/* Filter Control Bar */}
      <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm space-y-4">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-3 border-b border-slate-100">
          {/* Radius Slider */}
          <div className="flex-1 max-w-md space-y-1.5">
            <div className="flex items-center justify-between text-xs font-semibold text-slate-700">
              <span className="flex items-center gap-1.5">
                <Sliders className="w-4 h-4 text-emerald-600" />
                Search Radius:
              </span>
              <span className="text-emerald-700 font-bold font-mono bg-emerald-50 px-2 py-0.5 rounded-md border border-emerald-200">
                {radiusKm} km
              </span>
            </div>
            <input
              type="range"
              min="1"
              max="50"
              step="1"
              value={radiusKm}
              onChange={(e) => setRadiusKm(Number(e.target.value))}
              className="w-full accent-emerald-600 cursor-pointer"
            />
            <div className="flex justify-between text-[10px] text-slate-400 font-mono">
              <span>1 km</span>
              <span>10 km</span>
              <span>25 km</span>
              <span>50 km</span>
            </div>
          </div>

          {/* Quick Filter Reset */}
          <div className="flex items-center gap-2 self-end md:self-center">
            {selectedFacilityCodes.length > 0 && (
              <button
                onClick={handleResetFilters}
                className="px-3 py-1.5 text-xs text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-lg transition-colors flex items-center gap-1 font-medium"
              >
                <RotateCcw className="w-3.5 h-3.5" />
                Reset Filters ({selectedFacilityCodes.length})
              </button>
            )}
            <div className="text-xs text-slate-500 font-medium">
              Showing <strong className="text-slate-900">{mosques.length}</strong> mosques
            </div>
          </div>
        </div>

        {/* Facility Pill Filter Drawer */}
        <div>
          <div className="flex items-center gap-1.5 text-xs font-bold uppercase tracking-wider text-slate-400 mb-2.5">
            <Filter className="w-3.5 h-3.5" />
            <span>Islamic Amenities & Facilities</span>
          </div>
          <div className="flex flex-wrap gap-2">
            {facilities.map((facility) => {
              const active = selectedFacilityCodes.includes(facility.code);
              return (
                <button
                  key={facility.id}
                  onClick={() => toggleFacilityFilter(facility.code)}
                  className={`px-3 py-1.5 rounded-xl text-xs font-semibold transition-all border flex items-center gap-1.5 ${
                    active
                      ? 'bg-emerald-600 border-emerald-600 text-white shadow-xs'
                      : 'bg-slate-50 border-slate-200 text-slate-700 hover:bg-slate-100 hover:border-slate-300'
                  }`}
                >
                  {active && <CheckCircle className="w-3 h-3 text-white" />}
                  <span>{facility.name}</span>
                </button>
              );
            })}
          </div>
        </div>
      </div>

      {/* Main Split View: Left List, Right Map */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Column: Mosque List */}
        <div className="lg:col-span-5 space-y-4 max-h-[780px] overflow-y-auto pr-1">
          {isLoading ? (
            <div className="space-y-4">
              {[1, 2, 3].map((n) => (
                <div key={n} className="bg-white rounded-2xl p-4 border border-slate-200 shadow-xs animate-pulse">
                  <div className="h-40 bg-slate-200 rounded-xl mb-3"></div>
                  <div className="h-5 bg-slate-200 rounded w-3/4 mb-2"></div>
                  <div className="h-4 bg-slate-200 rounded w-1/2"></div>
                </div>
              ))}
            </div>
          ) : mosques.length === 0 ? (
            <div className="bg-white rounded-2xl p-8 border border-slate-200 text-center space-y-3">
              <Building2 className="w-12 h-12 text-slate-300 mx-auto" />
              <h3 className="font-bold text-slate-800 text-base">No Mosques Found in this Radius</h3>
              <p className="text-xs text-slate-500 max-w-xs mx-auto">
                Try expanding your radius slider up to 50 km or clearing specific amenity filters.
              </p>
              <button
                onClick={handleResetFilters}
                className="px-4 py-2 bg-emerald-600 text-white text-xs font-semibold rounded-xl hover:bg-emerald-700 transition-colors"
              >
                Expand Radius to 50 km
              </button>
            </div>
          ) : (
            mosques.map((mosque) => (
              <MosqueCard
                key={mosque.id}
                mosque={mosque}
                isSelected={selectedMosque?.id === mosque.id}
                onSelect={(m) => setSelectedMosque(m)}
              />
            ))
          )}
        </div>

        {/* Right Column: Sticky Interactive Leaflet Map */}
        <div className="lg:col-span-7 sticky top-24 h-[650px] sm:h-[780px]">
          <MosqueMap
            userLocation={userLocation}
            radiusKm={radiusKm}
            mosques={mosques}
            selectedMosque={selectedMosque}
            onSelectMosque={(m) => setSelectedMosque(m)}
          />
        </div>
      </div>
    </div>
  );
};
