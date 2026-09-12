import React, { useState } from 'react';
import { useAuthStore } from '../../stores/authStore';
import { useToastStore } from '../../stores/toastStore';
import { X, ShieldCheck, QrCode, Key, Copy, CheckCircle } from 'lucide-react';

interface TwoFactorModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const TwoFactorModal: React.FC<TwoFactorModalProps> = ({ isOpen, onClose }) => {
  const [step, setStep] = useState<'setup' | 'backup'>('setup');
  const [totpCode, setTotpCode] = useState('');
  const [isVerifying, setIsVerifying] = useState(false);
  const [copiedKey, setCopiedKey] = useState(false);
  const [copiedCodes, setCopiedCodes] = useState(false);

  const secretKey = 'JBSWY3DPEHPK3PXP';
  const backupCodes = [
    '8492-1029',
    '3910-4820',
    '7192-5039',
    '2840-1940',
    '6920-5829',
    '4019-3810',
    '9582-1039',
    '1849-2049',
  ];

  const { toggle2FA } = useAuthStore();
  const { addToast } = useToastStore();

  if (!isOpen) return null;

  const handleCopyKey = () => {
    navigator.clipboard.writeText(secretKey);
    setCopiedKey(true);
    setTimeout(() => setCopiedKey(false), 2000);
    addToast('Secret key copied to clipboard', 'info');
  };

  const handleCopyBackupCodes = () => {
    navigator.clipboard.writeText(backupCodes.join('\n'));
    setCopiedCodes(true);
    setTimeout(() => setCopiedCodes(false), 2000);
    addToast('Backup recovery codes copied!', 'success');
  };

  const handleVerify = (e: React.FormEvent) => {
    e.preventDefault();
    if (totpCode.length < 6) {
      addToast('Please enter a valid 6-digit TOTP verification code', 'warning');
      return;
    }

    setIsVerifying(true);
    setTimeout(() => {
      setIsVerifying(false);
      toggle2FA(true);
      setStep('backup');
      addToast('Two-Factor Authentication verified and enabled!', 'success');
    }, 600);
  };

  const handleFinish = () => {
    onClose();
    setStep('setup');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-xs animate-in fade-in duration-200">
      <div className="bg-white rounded-3xl max-w-md w-full p-6 border border-slate-200 shadow-2xl relative">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-slate-400 hover:text-slate-600 p-1.5 rounded-xl transition-colors"
        >
          <X className="w-5 h-5" />
        </button>

        {step === 'setup' ? (
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-2xl bg-emerald-100 text-emerald-800 flex items-center justify-center">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-lg font-bold text-slate-900">Set Up Two-Factor Auth (TOTP)</h3>
                <p className="text-xs text-slate-500">
                  Protect your OpenMosque contributor account
                </p>
              </div>
            </div>

            <div className="bg-slate-50 p-4 rounded-2xl border border-slate-200 flex flex-col items-center justify-center space-y-3">
              {/* Simulated QR Code representation */}
              <div className="w-36 h-36 bg-white p-2.5 rounded-xl border border-slate-300 shadow-inner flex items-center justify-center relative">
                <QrCode className="w-28 h-28 text-slate-800" />
                <div className="absolute inset-0 flex items-center justify-center">
                  <div className="w-6 h-6 bg-emerald-600 rounded-md flex items-center justify-center text-white text-[9px] font-bold">
                    OM
                  </div>
                </div>
              </div>
              <p className="text-[11px] text-slate-500 text-center">
                Scan with Google Authenticator, Authy, or 1Password
              </p>
            </div>

            {/* Secret Key fallback */}
            <div className="space-y-1">
              <label className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider block">
                Manual Entry Secret Key:
              </label>
              <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-50 border border-slate-200 text-xs font-mono text-slate-800">
                <span>{secretKey}</span>
                <button
                  type="button"
                  onClick={handleCopyKey}
                  className="text-emerald-700 hover:text-emerald-800 p-1 rounded transition-colors flex items-center gap-1 font-sans text-[11px]"
                >
                  {copiedKey ? <CheckCircle className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
                  {copiedKey ? 'Copied' : 'Copy'}
                </button>
              </div>
            </div>

            {/* Code Verification Form */}
            <form onSubmit={handleVerify} className="space-y-3 pt-2">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Enter 6-digit Code from Authenticator App:
                </label>
                <input
                  type="text"
                  maxLength={6}
                  required
                  value={totpCode}
                  onChange={(e) => setTotpCode(e.target.value.replace(/\D/g, ''))}
                  placeholder="123456"
                  className="w-full text-center text-xl tracking-widest font-mono py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                />
              </div>

              <button
                type="submit"
                disabled={isVerifying || totpCode.length !== 6}
                className="w-full py-2.5 bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 text-white font-semibold rounded-xl text-sm transition-all shadow-sm"
              >
                {isVerifying ? 'Verifying...' : 'Verify & Enable 2FA'}
              </button>
            </form>
          </div>
        ) : (
          /* Step 2: Emergency Recovery Backup Codes */
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-2xl bg-amber-100 text-amber-800 flex items-center justify-center">
                <Key className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-lg font-bold text-slate-900">Save Your Recovery Codes</h3>
                <p className="text-xs text-slate-500">
                  Single-use codes if you ever lose your phone
                </p>
              </div>
            </div>

            <div className="bg-amber-50 border border-amber-200 p-3 rounded-xl text-xs text-amber-900">
              Save these codes in a secure password manager. Each code can only be used once.
            </div>

            <div className="grid grid-cols-2 gap-2 bg-slate-50 p-3.5 rounded-2xl border border-slate-200 font-mono text-xs text-slate-800">
              {backupCodes.map((code) => (
                <div key={code} className="p-1.5 bg-white rounded-lg border border-slate-200 text-center font-bold">
                  {code}
                </div>
              ))}
            </div>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={handleCopyBackupCodes}
                className="flex-1 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-xl transition-colors flex items-center justify-center gap-1.5"
              >
                {copiedCodes ? <CheckCircle className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
                {copiedCodes ? 'Copied Codes!' : 'Copy All Codes'}
              </button>
              <button
                type="button"
                onClick={handleFinish}
                className="flex-1 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-xl transition-colors"
              >
                Done
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
