import React, { useState, useEffect } from 'react';
import { adminApi } from '../api/admin';
import { useAuthStore } from '../stores/authStore';
import { useToastStore } from '../stores/toastStore';
import type { MosqueSubmission, MosqueClaim, ContentFlag, User, UserRole, OsmIngestResult } from '../types';
import {
  ShieldCheck,
  Check,
  X,
  FileText,
  UserCheck,
  Flag,
  Globe,
  Users,
  Search,
  CheckCircle2,
  ExternalLink,
  Sparkles,
} from 'lucide-react';

export const AdminDashboardPage: React.FC = () => {
  const { user } = useAuthStore();
  const { addToast } = useToastStore();

  const [activeTab, setActiveTab] = useState<'submissions' | 'claims' | 'flags' | 'users' | 'osm'>('submissions');

  // Submissions queue
  const [submissions, setSubmissions] = useState<MosqueSubmission[]>([]);
  // Claims queue
  const [claims, setClaims] = useState<MosqueClaim[]>([]);
  // Flags queue
  const [flags, setFlags] = useState<ContentFlag[]>([]);
  // Users list
  const [usersList, setUsersList] = useState<User[]>([]);
  const [userSearchTerm, setUserSearchTerm] = useState('');
  // OSM Ingestion
  const [osmCity, setOsmCity] = useState('London');
  const [osmCountry, setOsmCountry] = useState('United Kingdom');
  const [osmDryRun, setOsmDryRun] = useState(false);
  const [osmResult, setOsmResult] = useState<OsmIngestResult | null>(null);
  const [isIngesting, setIsIngesting] = useState(false);

  const [isLoading, setIsLoading] = useState(true);

  const loadData = () => {
    setIsLoading(true);
    Promise.all([
      adminApi.getSubmissions('PENDING'),
      adminApi.getClaims('PENDING'),
      adminApi.getFlags(),
      adminApi.getUsers(),
    ])
      .then(([subs, clms, flgs, usrs]) => {
        setSubmissions(subs);
        setClaims(clms);
        setFlags(flgs);
        setUsersList(usrs);
      })
      .finally(() => {
        setIsLoading(false);
      });
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleApproveSubmission = async (id: string) => {
    try {
      await adminApi.decideSubmission(id, 'APPROVED', 'Verified address, facilities, and coordinates.');
      setSubmissions((prev) => prev.filter((s) => s.id !== id));
      addToast('Mosque proposal approved! Published live & awarded +100 points.', 'success');
    } catch {
      addToast('Error approving submission', 'error');
    }
  };

  const handleRejectSubmission = async (id: string) => {
    const reason = prompt('Please enter a rejection reason for the contributor:', 'Duplicate entry or unverifiable address');
    if (!reason) return;

    try {
      await adminApi.decideSubmission(id, 'REJECTED', reason);
      setSubmissions((prev) => prev.filter((s) => s.id !== id));
      addToast('Submission rejected and notification sent.', 'info');
    } catch {
      addToast('Error rejecting submission', 'error');
    }
  };

  const handleApproveClaim = async (id: string) => {
    try {
      await adminApi.decideClaim(id, 'APPROVED', 'Verified official charity document.');
      setClaims((prev) => prev.filter((c) => c.id !== id));
      addToast('Claim approved! Claimant upgraded to MOSQUE_ADMIN.', 'success');
    } catch {
      addToast('Error approving claim', 'error');
    }
  };

  const handleRejectClaim = async (id: string) => {
    const reason = prompt('Rejection reason:', 'Insufficient authorization proof');
    if (!reason) return;

    try {
      await adminApi.decideClaim(id, 'REJECTED', reason);
      setClaims((prev) => prev.filter((c) => c.id !== id));
      addToast('Claim rejected.', 'info');
    } catch {
      addToast('Error rejecting claim', 'error');
    }
  };

  const handleDecideFlag = async (id: string, decision: 'RESOLVED' | 'DISMISSED') => {
    try {
      await adminApi.decideFlag(id, decision, decision === 'RESOLVED' ? 'Content hidden' : 'No violation found');
      setFlags((prev) => prev.filter((f) => f.id !== id));
      addToast(`Flagged content ${decision.toLowerCase()}!`, 'success');
    } catch {
      addToast('Error handling flag', 'error');
    }
  };

  const handleChangeRole = async (userId: string, newRole: UserRole) => {
    try {
      await adminApi.changeUserRole(userId, newRole);
      setUsersList((prev) =>
        prev.map((u) => (u.id === userId ? { ...u, role: newRole } : u))
      );
      addToast(`User role successfully changed to ${newRole}!`, 'success');
    } catch {
      addToast('Error updating role', 'error');
    }
  };

  const handleOsmIngest = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsIngesting(true);
    setOsmResult(null);

    try {
      const res = await adminApi.ingestOsmCity(osmCity, osmCountry, osmDryRun);
      setOsmResult(res);
      addToast(`OSM Overpass Ingestion complete! Processed in ${res.durationMs}ms`, 'success');
    } catch {
      addToast('Error running OSM ingestion engine', 'error');
    } finally {
      setIsIngesting(false);
    }
  };

  const filteredUsers = usersList.filter(
    (u) =>
      u.email.toLowerCase().includes(userSearchTerm.toLowerCase()) ||
      u.displayName.toLowerCase().includes(userSearchTerm.toLowerCase())
  );

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header */}
      <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <div className="w-14 h-14 rounded-2xl bg-amber-100 text-amber-800 flex items-center justify-center shadow-md">
            <ShieldCheck className="w-8 h-8 text-amber-600" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">
                Platform Moderation & Admin Dashboard
              </h1>
              <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase bg-amber-100 text-amber-900 border border-amber-200">
                {user?.role}
              </span>
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              Review crowdsourced proposals, verify Imam claims, moderate community content, and run OSM batch ingestion
            </p>
          </div>
        </div>

        <button
          onClick={loadData}
          className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-xl transition-colors self-start md:self-center"
        >
          Refresh Queues
        </button>
      </div>

      {/* Tabs */}
      <div className="space-y-6">
        <div className="border-b border-slate-200">
          <nav className="flex space-x-6 overflow-x-auto">
            <button
              onClick={() => setActiveTab('submissions')}
              className={`py-3 px-1 text-sm font-semibold border-b-2 transition-colors flex items-center gap-2 whitespace-nowrap ${
                activeTab === 'submissions'
                  ? 'border-emerald-600 text-emerald-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              <FileText className="w-4 h-4" />
              Mosque Submissions
              {submissions.length > 0 && (
                <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-800">
                  {submissions.length}
                </span>
              )}
            </button>

            <button
              onClick={() => setActiveTab('claims')}
              className={`py-3 px-1 text-sm font-semibold border-b-2 transition-colors flex items-center gap-2 whitespace-nowrap ${
                activeTab === 'claims'
                  ? 'border-emerald-600 text-emerald-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              <UserCheck className="w-4 h-4" />
              Mosque Claims (Imam Portal)
              {claims.length > 0 && (
                <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-800">
                  {claims.length}
                </span>
              )}
            </button>

            <button
              onClick={() => setActiveTab('flags')}
              className={`py-3 px-1 text-sm font-semibold border-b-2 transition-colors flex items-center gap-2 whitespace-nowrap ${
                activeTab === 'flags'
                  ? 'border-emerald-600 text-emerald-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              <Flag className="w-4 h-4" />
              Reported Content
              {flags.length > 0 && (
                <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-rose-100 text-rose-800">
                  {flags.length}
                </span>
              )}
            </button>

            <button
              onClick={() => setActiveTab('users')}
              className={`py-3 px-1 text-sm font-semibold border-b-2 transition-colors flex items-center gap-2 whitespace-nowrap ${
                activeTab === 'users'
                  ? 'border-emerald-600 text-emerald-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              <Users className="w-4 h-4" />
              User Role Management
            </button>

            <button
              onClick={() => setActiveTab('osm')}
              className={`py-3 px-1 text-sm font-semibold border-b-2 transition-colors flex items-center gap-2 whitespace-nowrap ${
                activeTab === 'osm'
                  ? 'border-emerald-600 text-emerald-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              <Globe className="w-4 h-4 text-emerald-600" />
              OSM Batch Ingestion
            </button>
          </nav>
        </div>

        {isLoading && (
          <div className="bg-emerald-50 text-emerald-800 p-4 rounded-2xl text-xs font-semibold animate-pulse text-center">
            Refreshing moderation queues and data...
          </div>
        )}

        {/* Tab 1: Submissions Queue */}
        {activeTab === 'submissions' && (
          <div className="space-y-4">
            {submissions.length === 0 ? (
              <div className="bg-white rounded-3xl p-12 text-center border border-slate-200 space-y-2">
                <CheckCircle2 className="w-10 h-10 text-emerald-500 mx-auto" />
                <h4 className="font-bold text-slate-900 text-sm">Queue is Clear!</h4>
                <p className="text-xs text-slate-500">No pending crowdsourced mosque proposals awaiting review.</p>
              </div>
            ) : (
              submissions.map((sub) => (
                <div
                  key={sub.id}
                  className="bg-white rounded-3xl p-6 border border-slate-200 shadow-sm space-y-4"
                >
                  <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2.5">
                        <h3 className="text-lg font-bold text-slate-900">{sub.name}</h3>
                        <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-800">
                          {sub.status}
                        </span>
                      </div>
                      <p className="text-xs text-slate-500">
                        Submitted by: <strong className="text-slate-700">{sub.submitterEmail || sub.submitterId}</strong> • {new Date(sub.createdAt).toLocaleDateString('en-GB')}
                      </p>
                    </div>

                    <div className="flex items-center gap-2 self-start">
                      <button
                        onClick={() => handleRejectSubmission(sub.id)}
                        className="px-3.5 py-1.5 bg-rose-50 hover:bg-rose-100 text-rose-700 text-xs font-semibold rounded-xl border border-rose-200 transition-colors flex items-center gap-1"
                      >
                        <X className="w-3.5 h-3.5" /> Reject
                      </button>
                      <button
                        onClick={() => handleApproveSubmission(sub.id)}
                        className="px-4 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-xl transition-all shadow-sm flex items-center gap-1.5"
                      >
                        <Check className="w-3.5 h-3.5" /> Approve (+100 pts)
                      </button>
                    </div>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 bg-slate-50 p-4 rounded-2xl border border-slate-200 text-xs text-slate-700">
                    <div>
                      <p><strong>Address:</strong> {sub.address}, {sub.city}, {sub.country}</p>
                      <p className="font-mono text-[11px] mt-1">
                        <strong>Coordinates:</strong> {sub.latitude}, {sub.longitude}
                      </p>
                      <p className="mt-1"><strong>Phone:</strong> {sub.contactPhone || '—'}</p>
                      <p><strong>Website:</strong> {sub.websiteUrl || '—'}</p>
                    </div>

                    <div>
                      <p><strong>Description:</strong> {sub.description || 'No description provided'}</p>
                      <div className="mt-2 flex flex-wrap gap-1">
                        {sub.facilityCodes.map((code) => (
                          <span key={code} className="px-2 py-0.5 bg-white rounded-md border border-slate-200 text-[10px] font-medium">
                            {code}
                          </span>
                        ))}
                      </div>
                    </div>
                  </div>

                  {sub.imageUrls && sub.imageUrls.length > 0 && (
                    <div className="flex items-center gap-3 overflow-x-auto pt-1">
                      {sub.imageUrls.map((url, i) => (
                        <img key={i} src={url} alt="" className="w-20 h-14 object-cover rounded-lg border border-slate-200" />
                      ))}
                    </div>
                  )}
                </div>
              ))
            )}
          </div>
        )}

        {/* Tab 2: Claims Queue */}
        {activeTab === 'claims' && (
          <div className="space-y-4">
            {claims.length === 0 ? (
              <div className="bg-white rounded-3xl p-12 text-center border border-slate-200 space-y-2">
                <CheckCircle2 className="w-10 h-10 text-emerald-500 mx-auto" />
                <h4 className="font-bold text-slate-900 text-sm">No Pending Mosque Claims</h4>
                <p className="text-xs text-slate-500">All Imam and trustee verification requests have been processed.</p>
              </div>
            ) : (
              claims.map((clm) => (
                <div
                  key={clm.id}
                  className="bg-white rounded-3xl p-6 border border-slate-200 shadow-sm space-y-4"
                >
                  <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
                    <div>
                      <h3 className="text-base font-bold text-slate-900">
                        Claim Request for: <strong className="text-emerald-800">{clm.mosqueName || clm.mosqueId}</strong>
                      </h3>
                      <p className="text-xs text-slate-500">
                        Applicant: <strong>{clm.fullName}</strong> ({clm.positionInMosque})
                      </p>
                    </div>

                    <div className="flex items-center gap-2 self-start">
                      <button
                        onClick={() => handleRejectClaim(clm.id)}
                        className="px-3.5 py-1.5 bg-rose-50 hover:bg-rose-100 text-rose-700 text-xs font-semibold rounded-xl border border-rose-200 transition-colors flex items-center gap-1"
                      >
                        <X className="w-3.5 h-3.5" /> Reject Claim
                      </button>
                      <button
                        onClick={() => handleApproveClaim(clm.id)}
                        className="px-4 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-xl transition-all shadow-sm flex items-center gap-1.5"
                      >
                        <Check className="w-3.5 h-3.5" /> Approve & Promote to MOSQUE_ADMIN
                      </button>
                    </div>
                  </div>

                  <div className="bg-slate-50 p-4 rounded-2xl border border-slate-200 text-xs space-y-2">
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                      <p><strong>Official Email:</strong> {clm.officialEmail}</p>
                      <p><strong>Phone Number:</strong> {clm.phoneNumber}</p>
                    </div>

                    <div className="pt-2 border-t border-slate-200 flex items-center gap-2">
                      <FileText className="w-4 h-4 text-emerald-600" />
                      <span>Proof Document:</span>
                      <a
                        href={clm.proofDocumentUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="text-emerald-700 hover:underline font-medium flex items-center gap-1 truncate max-w-sm"
                      >
                        {clm.proofDocumentUrl} <ExternalLink className="w-3 h-3" />
                      </a>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {/* Tab 3: Flags Queue */}
        {activeTab === 'flags' && (
          <div className="space-y-4">
            {flags.length === 0 ? (
              <div className="bg-white rounded-3xl p-12 text-center border border-slate-200">
                <CheckCircle2 className="w-10 h-10 text-emerald-500 mx-auto mb-2" />
                <p className="text-xs text-slate-500">No inappropriate content reported.</p>
              </div>
            ) : (
              flags.map((flg) => (
                <div key={flg.id} className="bg-white rounded-2xl p-5 border border-slate-200 shadow-xs flex items-center justify-between gap-4">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="px-2 py-0.5 rounded-md text-[10px] font-bold bg-rose-100 text-rose-800 uppercase">
                        {flg.targetType}
                      </span>
                      <span className="text-xs font-mono text-slate-400">ID: {flg.targetId}</span>
                    </div>
                    <p className="text-xs text-slate-800 font-medium">Reason: "{flg.reason}"</p>
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => handleDecideFlag(flg.id, 'DISMISSED')}
                      className="px-3 py-1.5 text-xs text-slate-600 hover:bg-slate-100 rounded-xl"
                    >
                      Dismiss Flag
                    </button>
                    <button
                      onClick={() => handleDecideFlag(flg.id, 'RESOLVED')}
                      className="px-3 py-1.5 bg-rose-600 hover:bg-rose-700 text-white text-xs font-semibold rounded-xl"
                    >
                      Resolve & Hide Content
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {/* Tab 4: User Management */}
        {activeTab === 'users' && (
          <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200 shadow-sm space-y-4">
            <div className="flex items-center justify-between gap-4 pb-2">
              <div className="relative flex-1 max-w-sm">
                <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                <input
                  type="text"
                  value={userSearchTerm}
                  onChange={(e) => setUserSearchTerm(e.target.value)}
                  placeholder="Search users by name or email..."
                  className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                />
              </div>
              <span className="text-xs text-slate-500">Total Users: {usersList.length}</span>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-slate-100 text-slate-400 uppercase font-semibold text-[10px]">
                    <th className="pb-3">User</th>
                    <th className="pb-3">Email</th>
                    <th className="pb-3">Points</th>
                    <th className="pb-3">Current Role</th>
                    <th className="pb-3 text-right">Action (Change Role)</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {filteredUsers.map((u) => (
                    <tr key={u.id} className="hover:bg-slate-50">
                      <td className="py-3 font-semibold text-slate-900">{u.displayName}</td>
                      <td className="py-3 text-slate-500">{u.email}</td>
                      <td className="py-3 font-mono font-bold text-amber-700">{u.points} pts</td>
                      <td className="py-3">
                        <span className="px-2 py-0.5 rounded-md text-[10px] font-bold bg-slate-100 text-slate-700">
                          {u.role}
                        </span>
                      </td>
                      <td className="py-3 text-right">
                        <select
                          value={u.role}
                          onChange={(e) => handleChangeRole(u.id, e.target.value as UserRole)}
                          className="px-2 py-1 text-xs bg-white border border-slate-200 rounded-lg focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-700"
                        >
                          <option value="USER">USER</option>
                          <option value="MOSQUE_ADMIN">MOSQUE_ADMIN</option>
                          <option value="MODERATOR">MODERATOR</option>
                          <option value="SUPER_ADMIN">SUPER_ADMIN</option>
                        </select>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Tab 5: OpenStreetMap (OSM) Batch Ingestion */}
        {activeTab === 'osm' && (
          <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200 shadow-sm space-y-6">
            <div className="space-y-1">
              <h3 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                <Globe className="w-5 h-5 text-emerald-600" />
                OpenStreetMap Automated Ingestion Engine
              </h3>
              <p className="text-xs text-slate-500">
                Execute automated Overpass API queries to ingest mosques, coordinates, and facilities by city or region.
              </p>
            </div>

            <form onSubmit={handleOsmIngest} className="bg-slate-50 p-5 rounded-2xl border border-slate-200 space-y-4 max-w-xl">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Target City</label>
                  <input
                    type="text"
                    required
                    value={osmCity}
                    onChange={(e) => setOsmCity(e.target.value)}
                    placeholder="e.g. London"
                    className="w-full px-3 py-2 text-xs bg-white border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Country</label>
                  <input
                    type="text"
                    required
                    value={osmCountry}
                    onChange={(e) => setOsmCountry(e.target.value)}
                    placeholder="e.g. United Kingdom"
                    className="w-full px-3 py-2 text-xs bg-white border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                  />
                </div>
              </div>

              <div className="flex items-center gap-2 pt-1">
                <input
                  type="checkbox"
                  id="dryRun"
                  checked={osmDryRun}
                  onChange={(e) => setOsmDryRun(e.target.checked)}
                  className="accent-emerald-600"
                />
                <label htmlFor="dryRun" className="text-xs font-medium text-slate-700 cursor-pointer">
                  Dry Run Mode (Preview elements without writing to database)
                </label>
              </div>

              <button
                type="submit"
                disabled={isIngesting}
                className="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 text-white font-semibold text-xs rounded-xl shadow-sm flex items-center gap-2"
              >
                <Sparkles className="w-3.5 h-3.5 text-amber-300" />
                {isIngesting ? 'Running Overpass Ingestion...' : 'Execute OSM City Ingest'}
              </button>
            </form>

            {/* Ingestion Results Output */}
            {osmResult && (
              <div className="bg-emerald-50/80 border border-emerald-200 rounded-2xl p-5 space-y-3 animate-in fade-in duration-150">
                <div className="flex items-center justify-between">
                  <h4 className="text-sm font-bold text-emerald-900 flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                    Ingestion Execution Summary
                  </h4>
                  <span className="text-xs font-mono font-semibold text-emerald-700">
                    Execution Time: {osmResult.durationMs}ms
                  </span>
                </div>

                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-center">
                  <div className="bg-white p-3 rounded-xl border border-emerald-200">
                    <span className="text-xl font-bold font-mono text-slate-900 block">
                      {osmResult.totalElementsFetched}
                    </span>
                    <span className="text-[10px] font-semibold text-slate-500 uppercase">Fetched</span>
                  </div>

                  <div className="bg-white p-3 rounded-xl border border-emerald-200">
                    <span className="text-xl font-bold font-mono text-emerald-700 block">
                      {osmResult.mosquesInserted}
                    </span>
                    <span className="text-[10px] font-semibold text-slate-500 uppercase">Inserted</span>
                  </div>

                  <div className="bg-white p-3 rounded-xl border border-emerald-200">
                    <span className="text-xl font-bold font-mono text-amber-700 block">
                      {osmResult.duplicatesSkipped}
                    </span>
                    <span className="text-[10px] font-semibold text-slate-500 uppercase">Duplicates Skipped</span>
                  </div>

                  <div className="bg-white p-3 rounded-xl border border-emerald-200">
                    <span className="text-xl font-bold font-mono text-blue-700 block">
                      {osmResult.facilitiesAttached}
                    </span>
                    <span className="text-[10px] font-semibold text-slate-500 uppercase">Facilities Attached</span>
                  </div>
                </div>

                <div className="pt-2 text-xs text-slate-700">
                  <p className="font-semibold mb-1">Sample Inserted Mosques:</p>
                  <div className="flex flex-wrap gap-1.5">
                    {osmResult.insertedMosqueNames.map((name) => (
                      <span key={name} className="px-2 py-0.5 bg-white border border-emerald-200 rounded-md text-[11px] font-medium text-emerald-950">
                        {name}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
