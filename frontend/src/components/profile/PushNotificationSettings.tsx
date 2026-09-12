import React, { useState } from 'react';
import { useToastStore } from '../../stores/toastStore';
import { Bell, Smartphone } from 'lucide-react';

export const PushNotificationSettings: React.FC = () => {
  const [prayerAlerts, setPrayerAlerts] = useState(true);
  const [jummahAlerts, setJummahAlerts] = useState(true);
  const [moderationAlerts, setModerationAlerts] = useState(true);
  const [announcements, setAnnouncements] = useState(false);
  const { addToast } = useToastStore();

  const handleToggle = (name: string, val: boolean, setter: (v: boolean) => void) => {
    setter(!val);
    addToast(`${name} notifications ${!val ? 'enabled' : 'disabled'}`, 'info');
  };

  return (
    <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200 shadow-sm space-y-6">
      <div className="flex items-center justify-between pb-4 border-b border-slate-100">
        <div>
          <h3 className="text-lg font-bold text-slate-900 flex items-center gap-2">
            <Bell className="w-5 h-5 text-emerald-600" />
            Push Notification Preferences
          </h3>
          <p className="text-xs text-slate-500">
            Customize reminders for prayer congregations, Friday khutbahs, and community updates
          </p>
        </div>
        <div className="flex items-center gap-1 text-[11px] font-semibold text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-full border border-emerald-200">
          <Smartphone className="w-3.5 h-3.5" />
          <span>Device Active</span>
        </div>
      </div>

      <div className="space-y-4">
        {[
          {
            title: 'Congregation Iqamah Reminders',
            desc: 'Receive alerts 15 minutes before daily Iqamah for bookmarked mosques',
            state: prayerAlerts,
            setter: setPrayerAlerts,
          },
          {
            title: 'Friday Jumuah Khutbah Alert',
            desc: 'Early morning notification reminding you of Friday prayer batch schedules',
            state: jummahAlerts,
            setter: setJummahAlerts,
          },
          {
            title: 'Community Contribution & Moderation Status',
            desc: 'Updates when your proposed mosques or edit suggestions are reviewed (+points)',
            state: moderationAlerts,
            setter: setModerationAlerts,
          },
          {
            title: 'Emergency Community Broadcasts',
            desc: 'Direct announcements from official Imams for urgent weather or Eid prayer updates',
            state: announcements,
            setter: setAnnouncements,
          },
        ].map((item) => (
          <div key={item.title} className="flex items-center justify-between gap-4 p-3 rounded-2xl hover:bg-slate-50 transition-colors">
            <div className="space-y-0.5">
              <h4 className="text-sm font-bold text-slate-800">{item.title}</h4>
              <p className="text-xs text-slate-500">{item.desc}</p>
            </div>
            <button
              onClick={() => handleToggle(item.title, item.state, item.setter)}
              className={`w-12 h-6 flex items-center rounded-full p-1 transition-colors ${
                item.state ? 'bg-emerald-600 justify-end' : 'bg-slate-200 justify-start'
              }`}
            >
              <span className="w-4 h-4 rounded-full bg-white shadow-md transform transition-transform"></span>
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};
