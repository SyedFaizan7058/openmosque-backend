import React, { useState, useEffect } from 'react';
import { eventsApi } from '../../api/events';
import type { MosqueEvent } from '../../types';
import { Calendar, MapPin, User, ExternalLink, CalendarDays } from 'lucide-react';

interface MosqueEventsProps {
  mosqueId: string;
}

export const MosqueEvents: React.FC<MosqueEventsProps> = ({ mosqueId }) => {
  const [events, setEvents] = useState<MosqueEvent[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    eventsApi.getByMosque(mosqueId)
      .then((data) => setEvents(data))
      .finally(() => setIsLoading(false));
  }, [mosqueId]);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-lg font-bold text-slate-900 flex items-center gap-2">
            <CalendarDays className="w-5 h-5 text-emerald-600" />
            Upcoming Programs & Halaqahs
          </h3>
          <p className="text-xs text-slate-500">
            Community lectures, workshops, youth circles, and family activities
          </p>
        </div>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="h-44 bg-white rounded-2xl border border-slate-200 animate-pulse"></div>
          <div className="h-44 bg-white rounded-2xl border border-slate-200 animate-pulse"></div>
        </div>
      ) : events.length === 0 ? (
        <div className="bg-white rounded-2xl p-8 border border-slate-200 text-center text-xs text-slate-500">
          No upcoming scheduled events currently listed for this mosque.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {events.map((evt) => {
            const startDate = new Date(evt.startDateTime);
            const timeStr = startDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
            const dateStr = startDate.toLocaleDateString('en-GB', { weekday: 'short', day: 'numeric', month: 'short' });

            return (
              <div
                key={evt.id}
                className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs flex flex-col justify-between space-y-3 hover:border-emerald-300 transition-colors"
              >
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider bg-emerald-100 text-emerald-800">
                      {evt.eventType.replace('_', ' ')}
                    </span>
                    <span className="text-[10px] font-semibold text-slate-500 bg-slate-100 px-2 py-0.5 rounded-md">
                      Audience: {evt.audience}
                    </span>
                  </div>

                  <h4 className="font-bold text-slate-900 text-base leading-snug">{evt.title}</h4>
                  <p className="text-xs text-slate-600 line-clamp-2 leading-relaxed">{evt.description}</p>
                </div>

                <div className="space-y-1.5 pt-3 border-t border-slate-100 text-xs text-slate-500">
                  <div className="flex items-center gap-2">
                    <Calendar className="w-3.5 h-3.5 text-emerald-600" />
                    <span>{dateStr} at {timeStr}</span>
                  </div>

                  {evt.locationDetails && (
                    <div className="flex items-center gap-2">
                      <MapPin className="w-3.5 h-3.5 text-slate-400" />
                      <span>{evt.locationDetails}</span>
                    </div>
                  )}

                  {evt.speakerName && (
                    <div className="flex items-center gap-2">
                      <User className="w-3.5 h-3.5 text-slate-400" />
                      <span>Speaker: <strong className="text-slate-700">{evt.speakerName}</strong></span>
                    </div>
                  )}
                </div>

                {evt.registrationUrl && (
                  <a
                    href={evt.registrationUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="w-full py-2 px-3 bg-emerald-50 hover:bg-emerald-100 text-emerald-800 text-xs font-semibold rounded-xl transition-colors flex items-center justify-center gap-1.5"
                  >
                    Register / Learn More <ExternalLink className="w-3 h-3" />
                  </a>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
