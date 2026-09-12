import React, { useState, useEffect } from 'react';
import { reviewsApi } from '../../api/reviews';
import { qaApi } from '../../api/qa';
import { useAuthStore } from '../../stores/authStore';
import { useToastStore } from '../../stores/toastStore';
import type { MosqueReview, RatingSummary } from '../../types';
import {
  Star,
  MessageSquare,
  Sparkles,
  Flag,
} from 'lucide-react';

interface MosqueReviewsProps {
  mosqueId: string;
}

export const MosqueReviews: React.FC<MosqueReviewsProps> = ({ mosqueId }) => {
  const [reviews, setReviews] = useState<MosqueReview[]>([]);
  const [summary, setSummary] = useState<RatingSummary | null>(null);
  const [isWriting, setIsWriting] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  // Form state
  const [ratingOverall, setRatingOverall] = useState(5);
  const [ratingCleanliness, setRatingCleanliness] = useState(5);
  const [ratingFacilities, setRatingFacilities] = useState(5);
  const [ratingWomensArea, setRatingWomensArea] = useState(5);
  const [ratingParking, setRatingParking] = useState(4);
  const [reviewText, setReviewText] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { isAuthenticated } = useAuthStore();
  const { addToast } = useToastStore();

  const loadReviews = () => {
    setIsLoading(true);
    Promise.all([reviewsApi.getReviews(mosqueId), reviewsApi.getRatings(mosqueId)])
      .then(([revs, sum]) => {
        setReviews(revs);
        setSummary(sum);
      })
      .finally(() => {
        setIsLoading(false);
      });
  };

  useEffect(() => {
    loadReviews();
  }, [mosqueId]);

  const handleSubmitReview = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!reviewText.trim()) {
      addToast('Please write a brief comment with your review', 'warning');
      return;
    }

    setIsSubmitting(true);
    try {
      const created = await reviewsApi.create(mosqueId, {
        ratingOverall,
        ratingCleanliness,
        ratingFacilities,
        ratingWomensArea,
        ratingParking,
        reviewText,
      });
      setReviews([created, ...reviews]);
      setIsWriting(false);
      setReviewText('');
      addToast('Your review has been published!', 'success');
    } catch {
      addToast('Error submitting review. Please try again.', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleFlagReview = async (reviewId: string) => {
    try {
      await qaApi.flagContent('REVIEW', reviewId, 'Inappropriate or spam review');
      addToast('Review reported to community moderators for inspection', 'info');
    } catch {
      addToast('Error flagging review', 'error');
    }
  };

  const renderStarPicker = (val: number, setVal: (v: number) => void, label: string) => {
    return (
      <div className="flex items-center justify-between py-1 text-xs">
        <span className="font-medium text-slate-700">{label}</span>
        <div className="flex items-center gap-1">
          {[1, 2, 3, 4, 5].map((star) => (
            <button
              type="button"
              key={star}
              onClick={() => setVal(star)}
              className="p-0.5 text-slate-300 hover:text-amber-400 focus:outline-none transition-colors"
            >
              <Star
                className={`w-4 h-4 ${
                  star <= val ? 'text-amber-500 fill-amber-500' : 'text-slate-300'
                }`}
              />
            </button>
          ))}
          <span className="w-5 text-right font-mono font-bold text-slate-700 ml-1">{val}</span>
        </div>
      </div>
    );
  };

  return (
    <div className="space-y-6">
      {/* Category Breakdown & Overall Rating Card */}
      {summary && (
        <div className="bg-white rounded-3xl p-6 border border-slate-200 shadow-sm grid grid-cols-1 md:grid-cols-12 gap-6">
          <div className="md:col-span-4 flex flex-col items-center justify-center border-b md:border-b-0 md:border-r border-slate-100 pb-6 md:pb-0 md:pr-6 text-center">
            <span className="text-5xl font-extrabold text-slate-900 font-mono">
              {summary.averageOverall.toFixed(1)}
            </span>
            <div className="flex items-center gap-1 my-2">
              {[1, 2, 3, 4, 5].map((s) => (
                <Star
                  key={s}
                  className={`w-5 h-5 ${
                    s <= Math.round(summary.averageOverall)
                      ? 'text-amber-500 fill-amber-500'
                      : 'text-slate-200'
                  }`}
                />
              ))}
            </div>
            <p className="text-xs text-slate-500">Based on {summary.totalReviews} worshipper reviews</p>
          </div>

          <div className="md:col-span-8 space-y-2.5">
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">
              Category Breakdown
            </h4>

            {[
              { label: 'Cleanliness & Wudu', score: summary.averageCleanliness },
              { label: 'Facilities & Prayer Space', score: summary.averageFacilities },
              { label: "Women's Area & Access", score: summary.averageWomensArea },
              { label: 'Parking & Accessibility', score: summary.averageParking },
            ].map((cat) => (
              <div key={cat.label} className="space-y-1">
                <div className="flex justify-between text-xs font-medium text-slate-700">
                  <span>{cat.label}</span>
                  <span className="font-mono font-bold text-slate-900">{cat.score.toFixed(1)} / 5</span>
                </div>
                <div className="h-2 w-full bg-slate-100 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-amber-500 rounded-full transition-all duration-500"
                    style={{ width: `${(cat.score / 5) * 100}%` }}
                  ></div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Review Action Bar */}
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-bold text-slate-900 flex items-center gap-2">
          <MessageSquare className="w-5 h-5 text-emerald-600" />
          Community Reviews ({reviews.length})
        </h3>

        {!isWriting && (
          <button
            onClick={() => {
              if (!isAuthenticated) {
                addToast('Please sign in to write a review', 'info');
                return;
              }
              setIsWriting(true);
            }}
            className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-xl transition-all shadow-sm flex items-center gap-1.5"
          >
            <Sparkles className="w-3.5 h-3.5" />
            Write a Review
          </button>
        )}
      </div>

      {/* Write Review Form Card */}
      {isWriting && (
        <div className="bg-white rounded-3xl p-6 border border-emerald-300 ring-2 ring-emerald-500/10 shadow-md animate-in fade-in duration-200">
          <h4 className="text-base font-bold text-slate-900 mb-3">Post Your Mosque Review</h4>

          <form onSubmit={handleSubmitReview} className="space-y-4">
            <div className="bg-slate-50 p-4 rounded-2xl border border-slate-200 divide-y divide-slate-200">
              {renderStarPicker(ratingOverall, setRatingOverall, 'Overall Experience')}
              {renderStarPicker(ratingCleanliness, setRatingCleanliness, 'Cleanliness & Ablution Area')}
              {renderStarPicker(ratingFacilities, setRatingFacilities, 'Facilities & Carpet Comfort')}
              {renderStarPicker(ratingWomensArea, setRatingWomensArea, "Women's Section Experience")}
              {renderStarPicker(ratingParking, setRatingParking, 'Parking & Transit Ease')}
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Your Review Comments
              </label>
              <textarea
                rows={3}
                required
                value={reviewText}
                onChange={(e) => setReviewText(e.target.value)}
                placeholder="Share your experience (e.g. prayer space, Friday crowd, parking tips, atmosphere)..."
                className="w-full px-3 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
              />
            </div>

            <div className="flex justify-end gap-3">
              <button
                type="button"
                onClick={() => setIsWriting(false)}
                className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={isSubmitting}
                className="px-5 py-2 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl transition-all shadow-sm"
              >
                {isSubmitting ? 'Posting...' : 'Submit Review'}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Reviews Feed */}
      {isLoading ? (
        <div className="space-y-3">
          <div className="h-28 bg-white rounded-2xl border border-slate-200 animate-pulse" />
          <div className="h-28 bg-white rounded-2xl border border-slate-200 animate-pulse" />
        </div>
      ) : (
        <div className="space-y-3.5">
          {reviews.map((rev) => (
          <div
            key={rev.id}
            className="bg-white rounded-2xl p-5 border border-slate-200 shadow-xs space-y-3"
          >
            <div className="flex items-start justify-between gap-3">
              <div className="flex items-center gap-3">
                <img
                  src={
                    rev.userPhotoUrl ||
                    'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop&q=80'
                  }
                  alt={rev.userName}
                  className="w-9 h-9 rounded-full object-cover border border-slate-200"
                />
                <div>
                  <h5 className="text-sm font-bold text-slate-900">{rev.userName}</h5>
                  <p className="text-[11px] text-slate-400">
                    {new Date(rev.createdAt).toLocaleDateString('en-GB', {
                      day: 'numeric',
                      month: 'short',
                      year: 'numeric',
                    })}
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-1">
                {[1, 2, 3, 4, 5].map((s) => (
                  <Star
                    key={s}
                    className={`w-3.5 h-3.5 ${
                      s <= rev.ratingOverall ? 'text-amber-500 fill-amber-500' : 'text-slate-200'
                    }`}
                  />
                ))}
              </div>
            </div>

            <p className="text-xs text-slate-700 leading-relaxed">{rev.reviewText}</p>

            <div className="pt-2 border-t border-slate-100 flex items-center justify-between text-[11px] text-slate-400">
              <div className="flex items-center gap-3">
                <span>Cleanliness: {rev.ratingCleanliness}/5</span>
                <span>•</span>
                <span>Women's Area: {rev.ratingWomensArea}/5</span>
              </div>

              <button
                onClick={() => handleFlagReview(rev.id)}
                className="hover:text-rose-600 transition-colors flex items-center gap-1"
                title="Report inappropriate review"
              >
                <Flag className="w-3 h-3" />
                Report
              </button>
            </div>
          </div>
        ))}
        </div>
      )}
    </div>
  );
};
