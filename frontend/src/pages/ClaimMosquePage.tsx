import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { mosquesApi } from '../api/mosques';
import { useAuthStore } from '../stores/authStore';
import { useToastStore } from '../stores/toastStore';
import type { MosqueDetail } from '../types';
import { Shield, CheckCircle, FileText, Lock, Building, ArrowLeft } from 'lucide-react';

export const ClaimMosquePage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuthStore();
  const { addToast } = useToastStore();

  const [mosque, setMosque] = useState<MosqueDetail | null>(null);
  const [fullName, setFullName] = useState(user?.displayName || '');
  const [officialEmail, setOfficialEmail] = useState(user?.email || '');
  const [phoneNumber, setPhoneNumber] = useState(user?.phoneNumber || '');
  const [positionInMosque, setPositionInMosque] = useState('Head Imam');
  const [proofDocumentUrl, setProofDocumentUrl] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSubmitted, setIsSubmitted] = useState(false);

  useEffect(() => {
    if (id) {
      mosquesApi.getByIdOrSlug(id).then((data) => setMosque(data));
    }
  }, [id]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!id) return;

    if (!proofDocumentUrl.trim()) {
      addToast('Please provide a document proof URL (e.g. charity registration or utility bill)', 'warning');
      return;
    }

    setIsSubmitting(true);
    try {
      await mosquesApi.claim(id, {
        fullName,
        officialEmail,
        phoneNumber,
        positionInMosque,
        proofDocumentUrl,
      });
      setIsSubmitted(true);
      addToast('Claim application submitted to moderation committee!', 'success');
    } catch {
      addToast('Error submitting claim. Please check details.', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="max-w-md mx-auto px-4 py-20 text-center space-y-4">
        <h2 className="text-2xl font-bold text-slate-900">Sign in Required</h2>
        <p className="text-xs text-slate-500">You must be signed in to submit an official mosque ownership claim.</p>
      </div>
    );
  }

  if (isSubmitted) {
    return (
      <div className="max-w-lg mx-auto px-4 py-16 text-center space-y-6">
        <div className="w-16 h-16 bg-emerald-100 text-emerald-600 rounded-3xl mx-auto flex items-center justify-center shadow-lg">
          <CheckCircle className="w-8 h-8" />
        </div>
        <div className="space-y-2">
          <h2 className="text-2xl font-extrabold text-slate-900">Claim Application Received</h2>
          <p className="text-xs text-slate-600 leading-relaxed max-w-sm mx-auto">
            Your verification request to administer <strong>{mosque?.name}</strong> is now under inspection by platform moderators.
          </p>
        </div>
        <div className="p-4 bg-slate-50 rounded-2xl border border-slate-200 text-xs text-slate-600">
          Upon approval, your account role will be promoted to <strong>MOSQUE_ADMIN</strong>, allowing you to edit Iqamah schedules, add Friday Khutbahs, and post announcements.
        </div>
        <button
          onClick={() => navigate(`/mosques/${id}`)}
          className="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-xl"
        >
          Return to Mosque Profile
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      <button
        onClick={() => navigate(-1)}
        className="text-xs font-semibold text-slate-500 hover:text-slate-800 transition-colors flex items-center gap-1.5"
      >
        <ArrowLeft className="w-4 h-4" /> Back to Mosque Profile
      </button>

      {/* Header */}
      <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200 shadow-sm space-y-4">
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 rounded-2xl bg-emerald-100 text-emerald-700 flex items-center justify-center">
            <Shield className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl sm:text-2xl font-extrabold text-slate-900 tracking-tight">
              Mosque Administration Claim Portal
            </h1>
            <p className="text-xs text-slate-500">
              For Imams, Trustees, and Official Mosque Committee Members
            </p>
          </div>
        </div>

        {mosque && (
          <div className="p-3.5 bg-slate-50 rounded-2xl border border-slate-200 flex items-center gap-3">
            <Building className="w-5 h-5 text-emerald-600 flex-shrink-0" />
            <div>
              <h4 className="text-sm font-bold text-slate-900">{mosque.name}</h4>
              <p className="text-xs text-slate-500">{mosque.address}, {mosque.city}</p>
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4 pt-2">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Your Full Name</label>
            <input
              type="text"
              required
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="e.g. Imam Tariq Al-Banna"
              className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Official Mosque Email</label>
              <input
                type="email"
                required
                value={officialEmail}
                onChange={(e) => setOfficialEmail(e.target.value)}
                placeholder="imam@mosque-domain.org"
                className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Official Contact Phone</label>
              <input
                type="tel"
                required
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
                placeholder="+44 20 7654 3210"
                className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Position / Role at this Mosque</label>
            <select
              value={positionInMosque}
              onChange={(e) => setPositionInMosque(e.target.value)}
              className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
            >
              <option value="Head Imam">Head Imam</option>
              <option value="Senior Trustee & Chairman">Senior Trustee & Chairman</option>
              <option value="Secretary & Administrator">Secretary & Administrator</option>
              <option value="Committee Member">Executive Committee Member</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Verification Proof Document URL
            </label>
            <div className="relative">
              <FileText className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="url"
                required
                value={proofDocumentUrl}
                onChange={(e) => setProofDocumentUrl(e.target.value)}
                placeholder="https://docs.openmosque.org/charity-cert.pdf or cloud link"
                className="w-full pl-9 pr-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
              />
            </div>
            <p className="text-[10px] text-slate-400 mt-1">
              Upload a copy of charity registration certificate, utility bill under mosque name, or trustee letterhead.
            </p>
          </div>

          <div className="p-3 bg-amber-50 rounded-xl border border-amber-200 text-xs text-amber-900 flex items-center gap-2">
            <Lock className="w-4 h-4 text-amber-600 flex-shrink-0" />
            <span>Fraudulent claims are strictly investigated and result in platform suspension.</span>
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-xs rounded-xl shadow-md shadow-emerald-600/20 transition-all"
          >
            {isSubmitting ? 'Submitting Application...' : 'Submit Claim for Review'}
          </button>
        </form>
      </div>
    </div>
  );
};
