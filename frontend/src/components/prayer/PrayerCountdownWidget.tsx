import React, { useState, useEffect } from 'react';
import type { PrayerTimesDto } from '../../types';
import { Clock, Bell, Sparkles } from 'lucide-react';

interface PrayerCountdownWidgetProps {
  prayerTimes: PrayerTimesDto;
}

export const PrayerCountdownWidget: React.FC<PrayerCountdownWidgetProps> = ({ prayerTimes }) => {
  const [timeLeft, setTimeLeft] = useState<{ hours: number; minutes: number; seconds: number }>({
    hours: 0,
    minutes: prayerTimes.nextPrayer.remainingMinutes,
    seconds: 0,
  });

  useEffect(() => {
    let totalSeconds = prayerTimes.nextPrayer.remainingMinutes * 60;

    const interval = setInterval(() => {
      totalSeconds = Math.max(0, totalSeconds - 1);
      const hours = Math.floor(totalSeconds / 3600);
      const minutes = Math.floor((totalSeconds % 3600) / 60);
      const seconds = totalSeconds % 60;
      setTimeLeft({ hours, minutes, seconds });
    }, 1000);

    return () => clearInterval(interval);
  }, [prayerTimes]);

  const pad = (n: number) => n.toString().padStart(2, '0');

  return (
    <div className="bg-gradient-to-br from-emerald-900 via-emerald-800 to-teal-900 rounded-3xl p-6 text-white shadow-xl relative overflow-hidden">
      {/* Decorative Islamic geometric pattern watermark */}
      <div className="absolute right-0 top-0 bottom-0 w-1/2 opacity-5 pointer-events-none text-9xl select-none flex items-center justify-end font-arabic">
        صلوة
      </div>

      <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="space-y-2">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-700/60 text-emerald-200 text-xs font-semibold backdrop-blur-md border border-emerald-500/30">
            <Sparkles className="w-3.5 h-3.5 text-amber-400" />
            <span>Next Congregation</span>
          </div>

          <div className="flex items-baseline gap-3">
            <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-white">
              {prayerTimes.nextPrayer.name}
            </h2>
            <span className="text-sm font-semibold text-emerald-200">
              Iqamah at <strong className="text-white text-base">{prayerTimes.nextPrayer.iqamahTime}</strong>
            </span>
          </div>

          <p className="text-xs text-emerald-200/90 flex items-center gap-1.5">
            <Bell className="w-3.5 h-3.5 text-emerald-400" />
            Adhan was/is at {prayerTimes.nextPrayer.adhanTime}
          </p>
        </div>

        {/* Live Countdown Display */}
        <div className="flex items-center gap-2 sm:gap-3 bg-emerald-950/60 backdrop-blur-md p-3.5 sm:p-4 rounded-2xl border border-emerald-500/20 shadow-inner self-start md:self-center">
          <div className="flex items-center gap-1.5 text-emerald-400 mr-2">
            <Clock className="w-5 h-5 animate-pulse" />
          </div>

          <div className="text-center">
            <span className="text-2xl sm:text-3xl font-mono font-bold text-white block">
              {pad(timeLeft.hours)}
            </span>
            <span className="text-[10px] uppercase font-semibold text-emerald-300">Hours</span>
          </div>

          <span className="text-2xl font-bold text-emerald-400 pb-2">:</span>

          <div className="text-center">
            <span className="text-2xl sm:text-3xl font-mono font-bold text-amber-300 block">
              {pad(timeLeft.minutes)}
            </span>
            <span className="text-[10px] uppercase font-semibold text-emerald-300">Mins</span>
          </div>

          <span className="text-2xl font-bold text-emerald-400 pb-2">:</span>

          <div className="text-center">
            <span className="text-2xl sm:text-3xl font-mono font-bold text-emerald-200 block">
              {pad(timeLeft.seconds)}
            </span>
            <span className="text-[10px] uppercase font-semibold text-emerald-300">Secs</span>
          </div>
        </div>
      </div>
    </div>
  );
};
