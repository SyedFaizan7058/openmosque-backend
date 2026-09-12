import React, { useState } from 'react';
import { mosquesApi } from '../../api/mosques';
import { useToastStore } from '../../stores/toastStore';
import { useAuthStore } from '../../stores/authStore';
import type { MosqueDetail } from '../../types';
import { X, Edit3, Award } from 'lucide-react';

interface SuggestEditModalProps {
  isOpen: boolean;
  onClose: () => void;
  mosque: MosqueDetail;
}

export const SuggestEditModal: React.FC<SuggestEditModalProps> = ({ isOpen, onClose, mosque }) => {
  const [address, setAddress] = useState(mosque.address);
  const [contactPhone, setContactPhone] = useState(mosque.contactPhone || '');
  const [websiteUrl, setWebsiteUrl] = useState(mosque.websiteUrl || '');
  const [liveStreamUrl, setLiveStreamUrl] = useState(mosque.liveStreamUrl || '');
  const [correctionNotes, setCorrectionNotes] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { addToast } = useToastStore();
  const { awardPoints } = useAuthStore();

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      await mosquesApi.suggestEdit(mosque.id, {
        address,
        contactPhone,
        websiteUrl,
        liveStreamUrl,
      });
      awardPoints(50);
      addToast('Thank you! Your correction was submitted for moderation (+50 pts)', 'success');
      onClose();
    } catch {
      addToast('Error submitting edit suggestion. Please try again.', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-xs animate-in fade-in duration-150">
      <div className="bg-white rounded-3xl max-w-lg w-full p-6 border border-slate-200 shadow-2xl relative">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-slate-400 hover:text-slate-600 p-1.5 rounded-xl transition-colors"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center gap-3 mb-4">
          <div className="w-10 h-10 rounded-2xl bg-amber-100 text-amber-800 flex items-center justify-center">
            <Edit3 className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-lg font-bold text-slate-900">Suggest Mosque Edit</h3>
            <p className="text-xs text-slate-500">
              Help keep {mosque.name} accurate for the community
            </p>
          </div>
        </div>

        <div className="bg-amber-50 border border-amber-200 rounded-xl p-3 mb-5 flex items-center gap-2.5 text-xs text-amber-800 font-medium">
          <Award className="w-4 h-4 text-amber-600 flex-shrink-0" />
          <span>Earn +50 contributor reward points upon moderator approval!</span>
        </div>

        <form onSubmit={handleSubmit} className="space-y-3.5">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Mosque Address</label>
            <input
              type="text"
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              required
              className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Contact Phone</label>
              <input
                type="text"
                value={contactPhone}
                onChange={(e) => setContactPhone(e.target.value)}
                placeholder="+44 20 7654 3210"
                className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Official Website</label>
              <input
                type="url"
                value={websiteUrl}
                onChange={(e) => setWebsiteUrl(e.target.value)}
                placeholder="https://mosquename.org"
                className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Live Khutbah / YouTube Stream Link</label>
            <input
              type="url"
              value={liveStreamUrl}
              onChange={(e) => setLiveStreamUrl(e.target.value)}
              placeholder="https://youtube.com/live/..."
              className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Correction Details & Sources</label>
            <textarea
              rows={3}
              value={correctionNotes}
              onChange={(e) => setCorrectionNotes(e.target.value)}
              placeholder="Briefly explain what changed (e.g. new phone number or updated website)..."
              className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
            />
          </div>

          <div className="flex items-center justify-end gap-3 pt-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-5 py-2 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl transition-all shadow-md shadow-emerald-600/20"
            >
              {isSubmitting ? 'Submitting...' : 'Submit Suggestion'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
