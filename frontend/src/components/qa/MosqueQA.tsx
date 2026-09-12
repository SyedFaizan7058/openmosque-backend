import React, { useState, useEffect } from 'react';
import { qaApi } from '../../api/qa';
import { useAuthStore } from '../../stores/authStore';
import { useToastStore } from '../../stores/toastStore';
import type { MosqueQuestion } from '../../types';
import { HelpCircle, Send, Flag, ShieldCheck } from 'lucide-react';

interface MosqueQAProps {
  mosqueId: string;
}

export const MosqueQA: React.FC<MosqueQAProps> = ({ mosqueId }) => {
  const [questions, setQuestions] = useState<MosqueQuestion[]>([]);
  const [newQuestionText, setNewQuestionText] = useState('');
  const [activeReplyQuestionId, setActiveReplyQuestionId] = useState<string | null>(null);
  const [replyText, setReplyText] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { user } = useAuthStore();
  const { addToast } = useToastStore();

  useEffect(() => {
    qaApi.getQuestions(mosqueId).then((data) => {
      setQuestions(data);
    });
  }, [mosqueId]);

  const handleAskQuestion = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newQuestionText.trim()) return;

    setIsSubmitting(true);
    try {
      const q = await qaApi.askQuestion(mosqueId, newQuestionText.trim());
      setQuestions([q, ...questions]);
      setNewQuestionText('');
      addToast('Question posted to community thread!', 'success');
    } catch {
      addToast('Error posting question', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleAnswerQuestion = async (questionId: string) => {
    if (!replyText.trim()) return;

    const isOfficial = user?.role === 'MOSQUE_ADMIN' || user?.role === 'MODERATOR';

    try {
      const ans = await qaApi.answerQuestion(questionId, replyText.trim(), isOfficial);
      setQuestions(
        questions.map((q) => (q.id === questionId ? { ...q, answers: [...q.answers, ans] } : q))
      );
      setReplyText('');
      setActiveReplyQuestionId(null);
      addToast(
        isOfficial ? 'Official Mosque Admin answer published!' : 'Your answer was submitted!',
        'success'
      );
    } catch {
      addToast('Error submitting answer', 'error');
    }
  };

  const handleFlag = async (type: 'QUESTION' | 'ANSWER', id: string) => {
    try {
      await qaApi.flagContent(type, id, 'Offensive or spam content');
      addToast('Content reported for moderation inspection', 'info');
    } catch {
      addToast('Error reporting content', 'error');
    }
  };

  return (
    <div className="space-y-6">
      {/* Ask Question Bar */}
      <form onSubmit={handleAskQuestion} className="bg-white p-5 rounded-3xl border border-slate-200 shadow-sm space-y-3">
        <h4 className="text-sm font-bold text-slate-900 flex items-center gap-2">
          <HelpCircle className="w-4 h-4 text-emerald-600" />
          Ask a Question about this Mosque
        </h4>
        <div className="flex gap-2">
          <input
            type="text"
            required
            value={newQuestionText}
            onChange={(e) => setNewQuestionText(e.target.value)}
            placeholder="e.g. Is wheelchair access available for sisters? What are the Taraweeh timings?"
            className="flex-1 px-4 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
          />
          <button
            type="submit"
            disabled={isSubmitting}
            className="px-4 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-xs rounded-xl transition-all shadow-sm flex items-center gap-1.5"
          >
            <Send className="w-3.5 h-3.5" />
            Ask
          </button>
        </div>
      </form>

      {/* Questions Feed */}
      <div className="space-y-4">
        {questions.map((q) => (
          <div key={q.id} className="bg-white rounded-2xl p-5 border border-slate-200 shadow-xs space-y-3">
            <div className="flex items-start justify-between gap-3">
              <div>
                <h5 className="text-sm font-bold text-slate-900">{q.questionText}</h5>
                <p className="text-[11px] text-slate-400 mt-0.5">
                  Asked by {q.userName} • {new Date(q.createdAt).toLocaleDateString('en-GB')}
                </p>
              </div>

              <button
                onClick={() => handleFlag('QUESTION', q.id)}
                className="text-slate-400 hover:text-rose-600 transition-colors p-1"
                title="Report question"
              >
                <Flag className="w-3.5 h-3.5" />
              </button>
            </div>

            {/* Answers List */}
            <div className="pl-4 border-l-2 border-slate-100 space-y-2.5 my-2">
              {q.answers.length === 0 ? (
                <p className="text-xs text-slate-400 italic">No answers yet. Be the first to reply!</p>
              ) : (
                q.answers.map((ans) => (
                  <div
                    key={ans.id}
                    className={`p-3 rounded-xl border text-xs leading-relaxed space-y-1 ${
                      ans.isOfficialMosqueAdmin
                        ? 'bg-emerald-50/70 border-emerald-200 text-emerald-950'
                        : 'bg-slate-50 border-slate-200 text-slate-700'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-1.5">
                        <span className="font-bold text-slate-900">{ans.userName}</span>
                        {ans.isOfficialMosqueAdmin && (
                          <span className="inline-flex items-center gap-1 px-1.5 py-0.5 rounded-md bg-emerald-600 text-white text-[10px] font-bold">
                            <ShieldCheck className="w-3 h-3" />
                            Official Mosque Response
                          </span>
                        )}
                      </div>
                      <button
                        onClick={() => handleFlag('ANSWER', ans.id)}
                        className="text-slate-400 hover:text-rose-600 transition-colors"
                      >
                        <Flag className="w-3 h-3" />
                      </button>
                    </div>
                    <p>{ans.answerText}</p>
                  </div>
                ))
              )}
            </div>

            {/* Answer Box Toggle */}
            {activeReplyQuestionId === q.id ? (
              <div className="pt-2 space-y-2">
                <textarea
                  rows={2}
                  value={replyText}
                  onChange={(e) => setReplyText(e.target.value)}
                  placeholder="Provide an answer to this community question..."
                  className="w-full px-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                />
                <div className="flex justify-end gap-2">
                  <button
                    onClick={() => {
                      setActiveReplyQuestionId(null);
                      setReplyText('');
                    }}
                    className="px-3 py-1 text-xs text-slate-600 hover:bg-slate-100 rounded-lg"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={() => handleAnswerQuestion(q.id)}
                    className="px-4 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-lg shadow-xs"
                  >
                    Post Answer
                  </button>
                </div>
              </div>
            ) : (
              <div className="pt-1">
                <button
                  onClick={() => {
                    setActiveReplyQuestionId(q.id);
                    setReplyText('');
                  }}
                  className="text-xs font-semibold text-emerald-700 hover:text-emerald-800 transition-colors"
                >
                  Reply to this question →
                </button>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};
