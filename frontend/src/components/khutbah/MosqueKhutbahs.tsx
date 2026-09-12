import React, { useState, useEffect } from 'react';
import { khutbahsApi } from '../../api/khutbahs';
import type { MosqueKhutbah } from '../../types';
import { Radio, Video, Mic, User } from 'lucide-react';

interface MosqueKhutbahsProps {
  mosqueId: string;
}

export const MosqueKhutbahs: React.FC<MosqueKhutbahsProps> = ({ mosqueId }) => {
  const [khutbahs, setKhutbahs] = useState<MosqueKhutbah[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    khutbahsApi.getByMosque(mosqueId)
      .then((data) => setKhutbahs(data))
      .finally(() => setIsLoading(false));
  }, [mosqueId]);

  return (
    <div className="space-y-4">
      <div>
        <h3 className="text-lg font-bold text-slate-900 flex items-center gap-2">
          <Radio className="w-5 h-5 text-rose-600" />
          Friday Jumu'ah Khutbah Archive & Streams
        </h3>
        <p className="text-xs text-slate-500">
          Weekly sermon topics, invited scholars, and livestream recordings
        </p>
      </div>

      {isLoading ? (
        <div className="space-y-3">
          <div className="h-28 bg-white rounded-2xl border border-slate-200 animate-pulse"></div>
          <div className="h-28 bg-white rounded-2xl border border-slate-200 animate-pulse"></div>
        </div>
      ) : khutbahs.length === 0 ? (
        <div className="bg-white rounded-2xl p-8 border border-slate-200 text-center text-xs text-slate-500">
          No Friday khutbah recordings currently uploaded for this mosque.
        </div>
      ) : (
        <div className="space-y-3">
          {khutbahs.map((khut) => (
            <div
              key={khut.id}
              className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:border-emerald-300 transition-colors"
            >
              <div className="space-y-1.5">
                <div className="flex items-center gap-2">
                  <span className="px-2 py-0.5 rounded-md text-[10px] font-bold bg-amber-100 text-amber-900 uppercase">
                    Batch #{khut.batchNumber}
                  </span>
                  <span className="text-xs font-semibold text-slate-400 font-mono">
                    {khut.khutbahDate}
                  </span>
                  <span className="text-xs text-slate-500 bg-slate-100 px-2 py-0.5 rounded-md">
                    {khut.language}
                  </span>
                </div>

                <h4 className="font-bold text-slate-900 text-base">{khut.topic}</h4>

                <div className="flex items-center gap-2 text-xs text-slate-500">
                  <User className="w-3.5 h-3.5 text-emerald-600" />
                  <span>Khatib: <strong className="text-slate-800">{khut.khatibName}</strong></span>
                </div>
              </div>

              <div className="flex items-center gap-2 self-start sm:self-center">
                {khut.recordingUrl && (
                  <a
                    href={khut.recordingUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-xl transition-all shadow-sm flex items-center gap-1.5"
                  >
                    <Video className="w-3.5 h-3.5" />
                    Watch Recording
                  </a>
                )}
                {khut.streamUrl && (
                  <a
                    href={khut.streamUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="px-4 py-2 bg-rose-50 hover:bg-rose-100 text-rose-700 text-xs font-semibold rounded-xl border border-rose-200 transition-all flex items-center gap-1.5"
                  >
                    <Mic className="w-3.5 h-3.5" />
                    Audio Stream
                  </a>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
