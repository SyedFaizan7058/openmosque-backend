import { describe, it, expect } from 'vitest';
import { reviewsApi } from '../api/reviews';
import { qaApi } from '../api/qa';

describe('Phase 4: Reviews, Ratings & Community Q&A', () => {
  const mosqueId = '18558e48-a2aa-4219-9583-54cd9a84a76c';

  it('fetches community reviews and category rating averages', async () => {
    const reviews = await reviewsApi.getReviews(mosqueId);
    expect(reviews.length).toBeGreaterThan(0);
    expect(reviews[0].ratingOverall).toBeGreaterThanOrEqual(1);
    expect(reviews[0].ratingOverall).toBeLessThanOrEqual(5);

    const ratings = await reviewsApi.getRatings(mosqueId);
    expect(ratings.averageOverall).toBeGreaterThan(0);
    expect(ratings.averageCleanliness).toBeGreaterThan(0);
    expect(ratings.averageFacilities).toBeGreaterThan(0);
    expect(ratings.averageWomensArea).toBeGreaterThan(0);
    expect(ratings.averageParking).toBeGreaterThan(0);
  });

  it('posts a new review with 1-5 star categories', async () => {
    const newRev = await reviewsApi.create(mosqueId, {
      ratingOverall: 5,
      ratingCleanliness: 5,
      ratingFacilities: 4,
      ratingWomensArea: 5,
      ratingParking: 4,
      reviewText: 'Excellent atmosphere and pristine wudu area.',
    });

    expect(newRev.id).toBeDefined();
    expect(newRev.ratingOverall).toBe(5);
    expect(newRev.status).toBe('PUBLISHED');
  });

  it('fetches community questions and validates official Imam answer tagging', async () => {
    const questions = await qaApi.getQuestions(mosqueId);
    expect(questions.length).toBeGreaterThan(0);

    const qWithAnswers = questions.find((q) => q.answers.length > 0);
    expect(qWithAnswers).toBeDefined();

    const officialAnswer = qWithAnswers?.answers.find((a) => a.isOfficialMosqueAdmin);
    expect(officialAnswer).toBeDefined();
    expect(officialAnswer?.isOfficialMosqueAdmin).toBe(true);
  });

  it('allows answering a question and flags inappropriate content', async () => {
    const ans = await qaApi.answerQuestion('q-1', 'Entrance A has ramp access.', true);
    expect(ans.isOfficialMosqueAdmin).toBe(true);
    expect(ans.answerText).toBe('Entrance A has ramp access.');

    const flagRes = await qaApi.flagContent('REVIEW', 'rev-1', 'Spam content');
    expect(flagRes.success).toBe(true);
  });
});
