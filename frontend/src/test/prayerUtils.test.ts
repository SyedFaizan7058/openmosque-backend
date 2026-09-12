import { describe, it, expect } from 'vitest';
import { getMockPrayerTimes } from '../data/mockData';

describe('Phase 3: Prayer Times Engine & Countdown Calculation', () => {
  const mosqueId = '18558e48-a2aa-4219-9583-54cd9a84a76c';

  it('generates valid prayer times for all 5 daily prayers plus Sunrise', () => {
    const times = getMockPrayerTimes(mosqueId);
    expect(times).toBeDefined();
    expect(times.fajrAdhan).toMatch(/^\d{2}:\d{2}$/);
    expect(times.fajrIqamah).toMatch(/^\d{2}:\d{2}$/);
    expect(times.sunrise).toMatch(/^\d{2}:\d{2}$/);
    expect(times.dhuhrAdhan).toMatch(/^\d{2}:\d{2}$/);
    expect(times.dhuhrIqamah).toMatch(/^\d{2}:\d{2}$/);
    expect(times.asrAdhan).toMatch(/^\d{2}:\d{2}$/);
    expect(times.asrIqamah).toMatch(/^\d{2}:\d{2}$/);
    expect(times.maghribAdhan).toMatch(/^\d{2}:\d{2}$/);
    expect(times.maghribIqamah).toMatch(/^\d{2}:\d{2}$/);
    expect(times.ishaAdhan).toMatch(/^\d{2}:\d{2}$/);
    expect(times.ishaIqamah).toMatch(/^\d{2}:\d{2}$/);
  });

  it('determines the next prayer and valid remaining countdown minutes', () => {
    const times = getMockPrayerTimes(mosqueId);
    expect(times.nextPrayer).toBeDefined();
    expect(['Fajr', 'Dhuhr', 'Asr', 'Maghrib', 'Isha']).toContain(times.nextPrayer.name);
    expect(times.nextPrayer.remainingMinutes).toBeGreaterThan(0);
    expect(times.nextPrayer.iqamahTime).toMatch(/^\d{2}:\d{2}$/);
  });

  it('includes Friday Jumuah schedule with batch times', () => {
    const times = getMockPrayerTimes(mosqueId);
    expect(times.jummahSchedule).toBeDefined();
    expect(times.jummahSchedule?.batch1Time).toBe('13:15');
    expect(times.jummahSchedule?.batch2Time).toBe('14:00');
    expect(times.jummahSchedule?.language).toBe('English & Arabic');
    expect(times.jummahSchedule?.khatibName).toBeDefined();
  });
});
