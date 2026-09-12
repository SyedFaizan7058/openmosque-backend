import { describe, it, expect } from 'vitest';
import { mosquesApi } from '../api/mosques';
import { facilitiesApi } from '../api/facilities';

describe('Phase 2: Spatial Explorer & Mosque Search', () => {
  it('retrieves active facilities catalog', async () => {
    const facilities = await facilitiesApi.getAll();
    expect(facilities.length).toBeGreaterThan(0);
    const codes = facilities.map((f) => f.code);
    expect(codes).toContain('WUDU_AREA');
    expect(codes).toContain('WOMENS_SECTION');
    expect(codes).toContain('PARKING');
    expect(codes).toContain('WHEELCHAIR');
  });

  it('filters nearby mosques within a given radius', async () => {
    // 5 km radius around London center
    const mosques5km = await mosquesApi.getNearby({
      latitude: 51.5173,
      longitude: -0.0658,
      radiusKm: 5,
    });

    expect(mosques5km.length).toBeGreaterThan(0);
    mosques5km.forEach((m) => {
      if (m.distanceKm !== undefined) {
        expect(m.distanceKm).toBeLessThanOrEqual(5);
      }
    });

    // 25 km radius should return more or equal mosques
    const mosques25km = await mosquesApi.getNearby({
      latitude: 51.5173,
      longitude: -0.0658,
      radiusKm: 25,
    });
    expect(mosques25km.length).toBeGreaterThanOrEqual(mosques5km.length);
  });

  it('filters mosques by Islamic facility requirements', async () => {
    const mosquesWithParking = await mosquesApi.getNearby({
      latitude: 51.5173,
      longitude: -0.0658,
      radiusKm: 50,
      facilities: ['PARKING'],
    });

    mosquesWithParking.forEach((m) => {
      expect(m.facilities).toContain('PARKING');
    });
  });

  it('searches mosques by text query with pagination', async () => {
    const result = await mosquesApi.search({ q: 'London', page: 0, size: 10 });
    expect(result.content.length).toBeGreaterThan(0);
    expect(result.content[0].name).toContain('London');
    expect(result.totalElements).toBeGreaterThan(0);
    expect(result.pageNumber).toBe(0);
  });
});
