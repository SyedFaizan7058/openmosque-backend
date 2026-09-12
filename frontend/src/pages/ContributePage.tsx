import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { mosquesApi } from '../api/mosques';
import { facilitiesApi } from '../api/facilities';
import { useAuthStore } from '../stores/authStore';
import { useToastStore } from '../stores/toastStore';
import type { Facility } from '../types';
import {
  PlusCircle,
  MapPin,
  Building,
  Layers,
  Image as ImageIcon,
  CheckCircle,
  Award,
  ArrowRight,
  ArrowLeft,
  Sparkles,
} from 'lucide-react';

export const ContributePage: React.FC = () => {
  const navigate = useNavigate();
  const { user, awardPoints } = useAuthStore();
  const { addToast } = useToastStore();

  const [step, setStep] = useState<1 | 2 | 3 | 4>(1);
  const [facilities, setFacilities] = useState<Facility[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);

  // Wizard form state
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    address: '',
    city: '',
    country: 'United Kingdom',
    latitude: 51.5074,
    longitude: -0.1278,
    contactPhone: '',
    websiteUrl: '',
    liveStreamUrl: '',
    facilityCodes: [] as string[],
    imageUrls: ['https://images.unsplash.com/photo-1542838132-92c53300491e?w=800&auto=format&fit=crop&q=80'],
  });

  const [newImageUrl, setNewImageUrl] = useState('');

  useEffect(() => {
    facilitiesApi.getAll().then((data) => setFacilities(data));
  }, []);

  const handleFacilityToggle = (code: string) => {
    setFormData((prev) => ({
      ...prev,
      facilityCodes: prev.facilityCodes.includes(code)
        ? prev.facilityCodes.filter((c) => c !== code)
        : [...prev.facilityCodes, code],
    }));
  };

  const handleAddImage = () => {
    if (!newImageUrl.trim()) return;
    setFormData((prev) => ({
      ...prev,
      imageUrls: [...prev.imageUrls, newImageUrl.trim()],
    }));
    setNewImageUrl('');
  };

  const handleRemoveImage = (idx: number) => {
    setFormData((prev) => ({
      ...prev,
      imageUrls: prev.imageUrls.filter((_, i) => i !== idx),
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      await mosquesApi.submitNew(formData);
      awardPoints(100);
      setIsSuccess(true);
      addToast('Alhamdulillah! Mosque proposal submitted (+100 pts)', 'success');
    } catch {
      addToast('Error submitting proposal. Please check fields and try again.', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isSuccess) {
    return (
      <div className="max-w-xl mx-auto px-4 py-16 text-center space-y-6">
        <div className="w-20 h-20 bg-emerald-100 text-emerald-600 rounded-3xl mx-auto flex items-center justify-center shadow-lg">
          <CheckCircle className="w-10 h-10" />
        </div>

        <div className="space-y-2">
          <h2 className="text-3xl font-extrabold text-slate-900 tracking-tight">
            Submission Under Review!
          </h2>
          <p className="text-sm text-slate-600 leading-relaxed max-w-md mx-auto">
            Your proposal for <strong>{formData.name}</strong> has been routed to our community moderation queue. You have earned <strong>+100 reward points</strong>!
          </p>
        </div>

        <div className="p-4 bg-emerald-50 rounded-2xl border border-emerald-200 inline-flex items-center gap-3 text-emerald-900 text-xs font-semibold">
          <Award className="w-5 h-5 text-amber-500" />
          <span>New Points Balance: {user?.points || 100} points</span>
        </div>

        <div className="pt-4 flex justify-center gap-3">
          <button
            onClick={() => navigate('/profile')}
            className="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-xl transition-all shadow-sm"
          >
            View My Profile & Tiers
          </button>
          <button
            onClick={() => {
              setIsSuccess(false);
              setStep(1);
              setFormData({
                name: '',
                description: '',
                address: '',
                city: '',
                country: 'United Kingdom',
                latitude: 51.5074,
                longitude: -0.1278,
                contactPhone: '',
                websiteUrl: '',
                liveStreamUrl: '',
                facilityCodes: [],
                imageUrls: ['https://images.unsplash.com/photo-1542838132-92c53300491e?w=800&auto=format&fit=crop&q=80'],
              });
            }}
            className="px-5 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-xl transition-colors"
          >
            Submit Another Mosque
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header */}
      <div className="space-y-2 text-center">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-100 text-emerald-800 text-xs font-semibold">
          <PlusCircle className="w-3.5 h-3.5" />
          <span>Crowdsourced Verification</span>
        </div>
        <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">
          Add a New Mosque to the Global Directory
        </h1>
        <p className="text-xs text-slate-500 max-w-lg mx-auto">
          Help brothers and sisters find congregation prayers, sisters' facilities, and accurate schedules worldwide. Earn <strong>+100 reward points</strong>!
        </p>
      </div>

      {/* Step Indicator */}
      <div className="flex items-center justify-between max-w-2xl mx-auto">
        {[
          { num: 1, label: 'Coordinates', icon: <MapPin className="w-4 h-4" /> },
          { num: 2, label: 'Information', icon: <Building className="w-4 h-4" /> },
          { num: 3, label: 'Facilities', icon: <Layers className="w-4 h-4" /> },
          { num: 4, label: 'Photos & Submit', icon: <ImageIcon className="w-4 h-4" /> },
        ].map((s) => (
          <div key={s.num} className="flex flex-col items-center gap-1.5 flex-1 relative">
            <button
              onClick={() => setStep(s.num as any)}
              className={`w-10 h-10 rounded-2xl flex items-center justify-center text-xs font-bold transition-all shadow-xs ${
                step === s.num
                  ? 'bg-emerald-600 text-white ring-4 ring-emerald-500/20 shadow-md'
                  : step > s.num
                  ? 'bg-emerald-100 text-emerald-800'
                  : 'bg-slate-100 text-slate-400'
              }`}
            >
              {s.icon}
            </button>
            <span
              className={`text-[11px] font-semibold ${
                step === s.num ? 'text-slate-900' : 'text-slate-400'
              }`}
            >
              {s.label}
            </span>
          </div>
        ))}
      </div>

      {/* Wizard Card */}
      <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200 shadow-sm">
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Step 1: Location & Coordinates */}
          {step === 1 && (
            <div className="space-y-4 animate-in fade-in duration-150">
              <div className="space-y-1">
                <h3 className="text-lg font-bold text-slate-900">Step 1: Mosque Location & Coordinates</h3>
                <p className="text-xs text-slate-500">
                  Enter precise GPS latitude and longitude so worshippers can navigate using Google Maps and discover via proximity radius search.
                </p>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Latitude (SRID 4326)
                  </label>
                  <input
                    type="number"
                    step="0.0001"
                    required
                    value={formData.latitude}
                    onChange={(e) => setFormData({ ...formData, latitude: parseFloat(e.target.value) })}
                    className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                  />
                  <span className="text-[10px] text-slate-400">e.g. 51.5173 for London</span>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Longitude (SRID 4326)
                  </label>
                  <input
                    type="number"
                    step="0.0001"
                    required
                    value={formData.longitude}
                    onChange={(e) => setFormData({ ...formData, longitude: parseFloat(e.target.value) })}
                    className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                  />
                  <span className="text-[10px] text-slate-400">e.g. -0.0658 for London</span>
                </div>
              </div>

              <div className="bg-emerald-50/70 border border-emerald-200 rounded-2xl p-4 flex items-center gap-3">
                <MapPin className="w-5 h-5 text-emerald-600 flex-shrink-0" />
                <p className="text-xs text-emerald-900">
                  Tip: You can get exact coordinates by right-clicking the mosque on Google Maps or OpenStreetMap and copying the coordinates.
                </p>
              </div>

              <div className="flex justify-end pt-4">
                <button
                  type="button"
                  onClick={() => setStep(2)}
                  className="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-xl flex items-center gap-2"
                >
                  Continue to Basic Info <ArrowRight className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          )}

          {/* Step 2: Information */}
          {step === 2 && (
            <div className="space-y-4 animate-in fade-in duration-150">
              <div className="space-y-1">
                <h3 className="text-lg font-bold text-slate-900">Step 2: Mosque Profile & Contact</h3>
                <p className="text-xs text-slate-500">Provide official details, address, and community links.</p>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Mosque Official Name</label>
                <input
                  type="text"
                  required
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="e.g. Al-Farooq Community Mosque"
                  className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Street Address</label>
                <input
                  type="text"
                  required
                  value={formData.address}
                  onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                  placeholder="e.g. 14 High Street"
                  className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">City</label>
                  <input
                    type="text"
                    required
                    value={formData.city}
                    onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                    placeholder="e.g. London"
                    className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Country</label>
                  <input
                    type="text"
                    required
                    value={formData.country}
                    onChange={(e) => setFormData({ ...formData, country: e.target.value })}
                    placeholder="e.g. United Kingdom"
                    className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Description & Community Background</label>
                <textarea
                  rows={3}
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  placeholder="Brief history, congregation capacity, services offered..."
                  className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Phone Number</label>
                  <input
                    type="text"
                    value={formData.contactPhone}
                    onChange={(e) => setFormData({ ...formData, contactPhone: e.target.value })}
                    placeholder="+44 20 1234 5678"
                    className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Website URL</label>
                  <input
                    type="url"
                    value={formData.websiteUrl}
                    onChange={(e) => setFormData({ ...formData, websiteUrl: e.target.value })}
                    placeholder="https://..."
                    className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Live Khutbah Stream URL</label>
                  <input
                    type="url"
                    value={formData.liveStreamUrl}
                    onChange={(e) => setFormData({ ...formData, liveStreamUrl: e.target.value })}
                    placeholder="https://youtube.com/..."
                    className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                  />
                </div>
              </div>

              <div className="flex justify-between pt-4">
                <button
                  type="button"
                  onClick={() => setStep(1)}
                  className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl flex items-center gap-1.5"
                >
                  <ArrowLeft className="w-3.5 h-3.5" /> Back
                </button>
                <button
                  type="button"
                  onClick={() => setStep(3)}
                  disabled={!formData.name || !formData.address || !formData.city}
                  className="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 text-white text-xs font-semibold rounded-xl flex items-center gap-2"
                >
                  Continue to Facilities <ArrowRight className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          )}

          {/* Step 3: Facilities */}
          {step === 3 && (
            <div className="space-y-4 animate-in fade-in duration-150">
              <div className="space-y-1">
                <h3 className="text-lg font-bold text-slate-900">Step 3: Islamic Amenities & Facilities</h3>
                <p className="text-xs text-slate-500">Check all amenities that are confirmed to be available on-site.</p>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
                {facilities.map((fac) => {
                  const checked = formData.facilityCodes.includes(fac.code);

                  return (
                    <div
                      key={fac.id}
                      onClick={() => handleFacilityToggle(fac.code)}
                      className={`p-3.5 rounded-2xl border cursor-pointer transition-all flex items-start gap-3 ${
                        checked
                          ? 'bg-emerald-50/70 border-emerald-400 ring-1 ring-emerald-400'
                          : 'bg-slate-50 border-slate-200 hover:border-slate-300'
                      }`}
                    >
                      <input
                        type="checkbox"
                        checked={checked}
                        onChange={() => {}}
                        className="mt-1 accent-emerald-600"
                      />
                      <div>
                        <h4 className="text-xs font-bold text-slate-900">{fac.name}</h4>
                        <p className="text-[11px] text-slate-500 leading-tight mt-0.5">{fac.description}</p>
                      </div>
                    </div>
                  );
                })}
              </div>

              <div className="flex justify-between pt-4">
                <button
                  type="button"
                  onClick={() => setStep(2)}
                  className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl flex items-center gap-1.5"
                >
                  <ArrowLeft className="w-3.5 h-3.5" /> Back
                </button>
                <button
                  type="button"
                  onClick={() => setStep(4)}
                  className="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-xl flex items-center gap-2"
                >
                  Continue to Photos & Submit <ArrowRight className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          )}

          {/* Step 4: Photos & Submission */}
          {step === 4 && (
            <div className="space-y-4 animate-in fade-in duration-150">
              <div className="space-y-1">
                <h3 className="text-lg font-bold text-slate-900">Step 4: Photo URLs & Final Review</h3>
                <p className="text-xs text-slate-500">
                  Attach photo links of the mosque facade and main prayer hall.
                </p>
              </div>

              <div className="space-y-3">
                <div className="flex gap-2">
                  <input
                    type="url"
                    value={newImageUrl}
                    onChange={(e) => setNewImageUrl(e.target.value)}
                    placeholder="https://images.unsplash.com/photo-..."
                    className="flex-1 px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                  />
                  <button
                    type="button"
                    onClick={handleAddImage}
                    className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold text-xs rounded-xl"
                  >
                    Add Image
                  </button>
                </div>

                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                  {formData.imageUrls.map((url, idx) => (
                    <div key={idx} className="relative h-24 rounded-xl overflow-hidden border border-slate-200 group">
                      <img src={url} alt="" className="w-full h-full object-cover" />
                      <button
                        type="button"
                        onClick={() => handleRemoveImage(idx)}
                        className="absolute top-1 right-1 bg-rose-600 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity text-xs"
                      >
                        ✕
                      </button>
                    </div>
                  ))}
                </div>
              </div>

              {/* Summary card */}
              <div className="bg-slate-50 p-4 rounded-2xl border border-slate-200 space-y-2 text-xs">
                <h4 className="font-bold text-slate-800">Proposal Summary:</h4>
                <p>
                  <strong>Name:</strong> {formData.name}
                </p>
                <p>
                  <strong>Address:</strong> {formData.address}, {formData.city}, {formData.country}
                </p>
                <p>
                  <strong>Coordinates:</strong> {formData.latitude}, {formData.longitude}
                </p>
                <p>
                  <strong>Facilities:</strong> {formData.facilityCodes.length} amenities selected
                </p>
              </div>

              <div className="bg-emerald-50 border border-emerald-200 rounded-2xl p-4 flex items-center gap-3">
                <Award className="w-6 h-6 text-amber-500 flex-shrink-0" />
                <div>
                  <h5 className="text-xs font-bold text-emerald-900">Reward Points</h5>
                  <p className="text-[11px] text-emerald-800">
                    You will automatically receive <strong>+100 points</strong> once verified by a community moderator.
                  </p>
                </div>
              </div>

              <div className="flex justify-between pt-4">
                <button
                  type="button"
                  onClick={() => setStep(3)}
                  className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl flex items-center gap-1.5"
                >
                  <ArrowLeft className="w-3.5 h-3.5" /> Back
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="px-6 py-2.5 bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 text-white text-xs font-semibold rounded-xl shadow-md shadow-emerald-600/20 flex items-center gap-2"
                >
                  <Sparkles className="w-3.5 h-3.5 text-amber-300" />
                  {isSubmitting ? 'Submitting Proposal...' : 'Submit Mosque Proposal (+100 pts)'}
                </button>
              </div>
            </div>
          )}
        </form>
      </div>
    </div>
  );
};
