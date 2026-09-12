import React from 'react';
import type { PrayerTimesDto } from '../../types';
import { Calendar, Users } from 'lucide-react';

interface DailyPrayerTimetableProps {
  prayerTimes: PrayerTimesDto;
}

export const DailyPrayerTimetable: React.FC<DailyPrayerTimetableProps> = ({ prayerTimes }) => {
  const prayers = [
    { name: 'Fajr', arabic: 'الفجر', adhan: prayerTimes.fajrAdhan, iqamah: prayerTimes.fajrIqamah },
    { name: 'Sunrise (Shuruq)', arabic: 'الشروق', adhan: prayerTimes.sunrise, iqamah: '—', isSunrise: true },
    { name: 'Dhuhr', arabic: 'الظهر', adhan: prayerTimes.dhuhrAdhan, iqamah: prayerTimes.dhuhrIqamah },
    { name: 'Asr', arabic: 'العصر', adhan: prayerTimes.asrAdhan, iqamah: prayerTimes.asrIqamah },
    { name: 'Maghrib', arabic: 'المغرب', adhan: prayerTimes.maghribAdhan, iqamah: prayerTimes.maghribIqamah },
    { name: 'Isha', arabic: 'العشاء', adhan: prayerTimes.ishaAdhan, iqamah: prayerTimes.ishaIqamah },
  ];

  return (
    <div className="bg-white rounded-3xl p-6 border border-slate-200 shadow-sm space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-4 border-b border-slate-100">
        <div>
          <h3 className="text-lg font-bold text-slate-900">Daily Prayer & Iqamah Timetable</h3>
          <p className="text-xs text-slate-500">
            Astronomical calculations paired with official congregation overrides
          </p>
        </div>
        <div className="flex items-center gap-1.5 text-xs text-slate-500 font-medium bg-slate-50 px-3 py-1.5 rounded-xl border border-slate-200">
          <Calendar className="w-3.5 h-3.5 text-emerald-600" />
          <span>{new Date().toLocaleDateString('en-GB', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}</span>
        </div>
      </div>

      {/* Timetable Grid */}
      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="border-b border-slate-100 text-[11px] font-bold uppercase tracking-wider text-slate-400">
              <th className="pb-3 font-semibold">Prayer</th>
              <th className="pb-3 font-semibold font-arabic">الصلاة</th>
              <th className="pb-3 font-semibold">Beginning (Adhan)</th>
              <th className="pb-3 font-semibold text-emerald-700">Congregation (Iqamah)</th>
              <th className="pb-3 font-semibold text-right">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {prayers.map((prayer) => {
              const isNext = prayerTimes.nextPrayer.name === prayer.name;

              return (
                <tr
                  key={prayer.name}
                  className={`transition-colors ${
                    isNext ? 'bg-emerald-50/70 font-semibold' : 'hover:bg-slate-50'
                  }`}
                >
                  <td className="py-3.5 text-slate-900 flex items-center gap-2">
                    {isNext && <span className="w-2 h-2 rounded-full bg-emerald-600"></span>}
                    {prayer.name}
                  </td>
                  <td className="py-3.5 text-slate-500 font-arabic text-base">{prayer.arabic}</td>
                  <td className="py-3.5 text-slate-600 font-mono text-xs">{prayer.adhan}</td>
                  <td className="py-3.5 font-mono text-sm">
                    {prayer.isSunrise ? (
                      <span className="text-slate-400 text-xs">—</span>
                    ) : (
                      <span className="text-emerald-700 font-bold bg-emerald-100/60 px-2 py-0.5 rounded-md">
                        {prayer.iqamah}
                      </span>
                    )}
                  </td>
                  <td className="py-3.5 text-right">
                    {isNext ? (
                      <span className="inline-flex items-center gap-1 text-[10px] font-bold uppercase bg-emerald-600 text-white px-2 py-0.5 rounded-full shadow-xs">
                        Next Prayer
                      </span>
                    ) : (
                      <span className="text-slate-400 text-xs">—</span>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Friday Jumu'ah Card */}
      {prayerTimes.jummahSchedule && (
        <div className="bg-amber-50/60 border border-amber-200 rounded-2xl p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex items-start gap-3">
            <div className="p-2.5 rounded-xl bg-amber-100 text-amber-800 flex-shrink-0">
              <Users className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-slate-900">Friday Jumu'ah Prayers</h4>
              <p className="text-xs text-slate-600 mt-0.5">
                Khatib: <strong>{prayerTimes.jummahSchedule.khatibName || 'Imam'}</strong> • Language: {prayerTimes.jummahSchedule.language || 'English & Arabic'}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="text-center px-3 py-1.5 bg-white rounded-xl border border-amber-200 shadow-xs">
              <span className="text-[10px] font-bold text-slate-400 uppercase block">1st Khutbah</span>
              <span className="text-sm font-bold font-mono text-amber-900">{prayerTimes.jummahSchedule.batch1Time}</span>
            </div>
            {prayerTimes.jummahSchedule.batch2Time && (
              <div className="text-center px-3 py-1.5 bg-white rounded-xl border border-amber-200 shadow-xs">
                <span className="text-[10px] font-bold text-slate-400 uppercase block">2nd Khutbah</span>
                <span className="text-sm font-bold font-mono text-amber-900">{prayerTimes.jummahSchedule.batch2Time}</span>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
