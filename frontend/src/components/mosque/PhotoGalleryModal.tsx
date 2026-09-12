import React, { useState } from 'react';
import type { MosqueImage } from '../../types';
import { X, ChevronLeft, ChevronRight, Image as ImageIcon } from 'lucide-react';

interface PhotoGalleryModalProps {
  isOpen: boolean;
  onClose: () => void;
  images: MosqueImage[];
  initialIndex?: number;
}

export const PhotoGalleryModal: React.FC<PhotoGalleryModalProps> = ({
  isOpen,
  onClose,
  images,
  initialIndex = 0,
}) => {
  const [currentIndex, setCurrentIndex] = useState(initialIndex);

  if (!isOpen || images.length === 0) return null;

  const currentImage = images[currentIndex];

  const handlePrev = () => {
    setCurrentIndex((prev) => (prev > 0 ? prev - 1 : images.length - 1));
  };

  const handleNext = () => {
    setCurrentIndex((prev) => (prev < images.length - 1 ? prev + 1 : 0));
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/80 backdrop-blur-md animate-in fade-in duration-200">
      <div className="relative max-w-4xl w-full bg-white rounded-3xl overflow-hidden shadow-2xl border border-slate-200 flex flex-col max-h-[90vh]">
        {/* Modal Header */}
        <div className="flex items-center justify-between p-4 border-b border-slate-100 bg-white">
          <div className="flex items-center gap-2 text-sm font-semibold text-slate-800">
            <ImageIcon className="w-4 h-4 text-emerald-600" />
            <span>Mosque Photo Gallery ({currentIndex + 1} of {images.length})</span>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-xl text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Main Image View */}
        <div className="relative flex-1 bg-slate-950 flex items-center justify-center overflow-hidden min-h-[380px]">
          <img
            src={currentImage.imageUrl}
            alt={currentImage.caption || 'Mosque Gallery'}
            className="max-h-[65vh] w-auto object-contain transition-all duration-300"
          />

          {images.length > 1 && (
            <>
              <button
                onClick={handlePrev}
                className="absolute left-4 p-2.5 rounded-full bg-black/40 text-white hover:bg-black/60 backdrop-blur-md transition-all"
                aria-label="Previous image"
              >
                <ChevronLeft className="w-6 h-6" />
              </button>
              <button
                onClick={handleNext}
                className="absolute right-4 p-2.5 rounded-full bg-black/40 text-white hover:bg-black/60 backdrop-blur-md transition-all"
                aria-label="Next image"
              >
                <ChevronRight className="w-6 h-6" />
              </button>
            </>
          )}
        </div>

        {/* Caption & Thumbnails */}
        <div className="p-4 bg-white border-t border-slate-100 flex items-center justify-between gap-4">
          <p className="text-xs text-slate-600 font-medium truncate">
            {currentImage.caption || 'Mosque interior & architectural features'}
          </p>

          <div className="flex items-center gap-2">
            {images.map((img, idx) => (
              <button
                key={img.id}
                onClick={() => setCurrentIndex(idx)}
                className={`w-12 h-10 rounded-lg overflow-hidden border-2 transition-all ${
                  idx === currentIndex ? 'border-emerald-600 ring-2 ring-emerald-500/20' : 'border-transparent opacity-60 hover:opacity-100'
                }`}
              >
                <img src={img.imageUrl} alt="" className="w-full h-full object-cover" />
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
