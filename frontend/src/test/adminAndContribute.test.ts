import { describe, it, expect } from 'vitest';
import { mosquesApi } from '../api/mosques';
import { adminApi } from '../api/admin';

describe('Phase 5 & 6: Crowdsourcing, Mosque Claims, and Moderation Queues', () => {
  it('submits a new crowdsourced mosque proposal', async () => {
    const res = await mosquesApi.submitNew({
      name: 'Leeds Grand Mosque',
      description: 'Community mosque in Woodsley Road',
      address: '9 Woodsley Rd',
      city: 'Leeds',
      country: 'United Kingdom',
      latitude: 53.8067,
      longitude: -1.5623,
      contactPhone: '+44 113 246 8777',
      facilityCodes: ['WUDU_AREA', 'PARKING', 'WOMENS_SECTION'],
      imageUrls: ['https://images.unsplash.com/photo-mosque.jpg'],
    });

    expect(res.id).toBeDefined();
    expect(res.name).toBe('Leeds Grand Mosque');
    expect(res.status).toBe('PENDING');
  });

  it('submits a mosque claim application by an Imam', async () => {
    const res = await mosquesApi.claim('18558e48-a2aa-4219-9583-54cd9a84a76c', {
      fullName: 'Imam Tariq Al-Banna',
      officialEmail: 'imam@eastlondonmosque.org',
      phoneNumber: '+44 20 7654 3210',
      positionInMosque: 'Head Imam',
      proofDocumentUrl: 'https://docs.openmosque.org/proof.pdf',
    });

    expect(res.id).toBeDefined();
    expect(res.positionInMosque).toBe('Head Imam');
    expect(res.status).toBe('PENDING');
  });

  it('allows moderators to approve submissions and claims', async () => {
    const subDecision = await adminApi.decideSubmission('sub-001', 'APPROVED', 'Verified by committee');
    expect(subDecision.success).toBe(true);
    expect(subDecision.status).toBe('APPROVED');

    const claimDecision = await adminApi.decideClaim('clm-001', 'APPROVED', 'Verified trustee');
    expect(claimDecision.success).toBe(true);
    expect(claimDecision.status).toBe('APPROVED');
  });

  it('allows super admin to change user roles', async () => {
    const roleChange = await adminApi.changeUserRole('usr-contributor-101', 'MODERATOR');
    expect(roleChange.success).toBe(true);
    expect(roleChange.role).toBe('MODERATOR');
  });

  it('executes OpenStreetMap Overpass ingestion dry run', async () => {
    const osmRes = await adminApi.ingestOsmCity('London', 'United Kingdom', true);
    expect(osmRes.totalElementsFetched).toBeGreaterThan(0);
    expect(osmRes.duplicatesSkipped).toBeGreaterThanOrEqual(0);
    expect(osmRes.durationMs).toBeGreaterThan(0);
    expect(osmRes.insertedMosqueNames.length).toBeGreaterThan(0);
  });
});
