import React, { useState } from 'react';
import { useAuthStore, MOCK_PERSONAS } from '../../stores/authStore';
import { useToastStore } from '../../stores/toastStore';
import { Shield, ChevronUp, ChevronDown } from 'lucide-react';

export const DevMockSwitcher: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const { user, activePersonaKey, loginWithMockPersona } = useAuthStore();
  const { addToast } = useToastStore();

  const handleSelectPersona = (key: keyof typeof MOCK_PERSONAS) => {
    loginWithMockPersona(key);
    const selected = MOCK_PERSONAS[key];
    addToast(`Switched persona to ${selected.displayName} (${selected.role})`, 'success');
    setIsOpen(false);
  };

  const personaMeta: Record<string, { label: string; icon: string; badge: string; color: string }> = {
    contributor: { label: 'Contributor', icon: '👤', badge: '180 pts', color: 'text-blue-700 bg-blue-50 border-blue-200' },
    imam: { label: 'Imam / Admin', icon: '🕌', badge: '480 pts', color: 'text-emerald-700 bg-emerald-50 border-emerald-200' },
    moderator: { label: 'Moderator', icon: '🛡️', badge: '820 pts', color: 'text-amber-700 bg-amber-50 border-amber-200' },
    super_admin: { label: 'Super Admin', icon: '👑', badge: '1540 pts', color: 'text-purple-700 bg-purple-50 border-purple-200' },
  };

  const currentMeta = activePersonaKey && personaMeta[activePersonaKey]
    ? personaMeta[activePersonaKey]
    : { label: user?.role || 'Custom', icon: '👤', badge: `${user?.points || 0} pts`, color: 'text-slate-700 bg-slate-50 border-slate-200' };

  return (
    <div className="fixed bottom-4 left-4 z-50">
      {isOpen && (
        <div className="mb-2 p-3 bg-white rounded-2xl border border-slate-200 shadow-2xl w-72 animate-in fade-in slide-in-from-bottom-2 duration-150">
          <div className="flex items-center justify-between pb-2 mb-2 border-b border-slate-100">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-500 flex items-center gap-1.5">
              <Shield className="w-3.5 h-3.5 text-emerald-600" />
              Dev Mock Switcher
            </span>
            <span className="text-[10px] bg-slate-100 text-slate-500 font-semibold px-2 py-0.5 rounded-full">
              4 Personas
            </span>
          </div>

          <div className="space-y-1.5">
            {(Object.keys(MOCK_PERSONAS) as Array<keyof typeof MOCK_PERSONAS>).map((key) => {
              const p = MOCK_PERSONAS[key];
              const meta = personaMeta[key];
              const isSelected = activePersonaKey === key;

              return (
                <button
                  key={key}
                  onClick={() => handleSelectPersona(key)}
                  className={`w-full text-left p-2 rounded-xl transition-all flex items-center justify-between border ${
                    isSelected
                      ? 'bg-emerald-50/80 border-emerald-400 ring-1 ring-emerald-400 shadow-xs'
                      : 'hover:bg-slate-50 border-transparent hover:border-slate-200'
                  }`}
                >
                  <div className="flex items-center gap-2.5">
                    <span className="text-lg">{meta.icon}</span>
                    <div>
                      <p className="text-xs font-semibold text-slate-900 leading-none">{p.displayName}</p>
                      <p className="text-[10px] text-slate-500 mt-0.5">{meta.label} • {p.role}</p>
                    </div>
                  </div>
                  <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded-full border ${meta.color}`}>
                    {meta.badge}
                  </span>
                </button>
              );
            })}
          </div>

          <p className="text-[10px] text-slate-400 mt-2.5 text-center leading-tight">
            Click any persona to test RBAC, permissions, and moderation workflows instantly.
          </p>
        </div>
      )}

      {/* Trigger Button */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-2 bg-white/95 hover:bg-white border border-slate-200 hover:border-slate-300 text-slate-800 px-3.5 py-2 rounded-full shadow-lg hover:shadow-xl transition-all text-xs font-semibold backdrop-blur-md group"
      >
        <div className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></div>
        <span className="text-sm">{currentMeta.icon}</span>
        <span className="truncate max-w-[120px]">{user?.displayName || 'Select Persona'}</span>
        <span className="text-[10px] bg-slate-100 text-slate-600 px-1.5 py-0.5 rounded font-mono">
          {user?.role}
        </span>
        {isOpen ? <ChevronDown className="w-3.5 h-3.5 text-slate-400" /> : <ChevronUp className="w-3.5 h-3.5 text-slate-400" />}
      </button>
    </div>
  );
};
