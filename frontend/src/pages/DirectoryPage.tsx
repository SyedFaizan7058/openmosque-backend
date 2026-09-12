import React, { useState, useEffect } from 'react';
import { mosquesApi } from '../api/mosques';
import { MosqueCard } from '../components/mosque/MosqueCard';
import type { Mosque } from '../types';
import { Search, MapPin, Globe, ChevronLeft, ChevronRight, BookOpen } from 'lucide-react';

export const DirectoryPage: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCity, setSelectedCity] = useState('');
  const [selectedCountry, setSelectedCountry] = useState('');
  const [page, setPage] = useState(0);
  const [pageSize] = useState(6);

  const [mosques, setMosques] = useState<Mosque[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const [totalCount, setTotalCount] = useState(0);
  const [isLoading, setIsLoading] = useState(true);

  const executeSearch = (targetPage = 0) => {
    setIsLoading(true);
    mosquesApi
      .search({
        q: searchTerm || undefined,
        city: selectedCity || undefined,
        country: selectedCountry || undefined,
        page: targetPage,
        size: pageSize,
      })
      .then((res) => {
        setMosques(res.content);
        setTotalPages(res.totalPages);
        setTotalCount(res.totalElements);
        setPage(res.pageNumber);
      })
      .finally(() => {
        setIsLoading(false);
      });
  };

  useEffect(() => {
    executeSearch(0);
  }, [selectedCity, selectedCountry]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    executeSearch(0);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      {/* Header */}
      <div className="space-y-2">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-100 text-emerald-800 text-xs font-semibold">
          <BookOpen className="w-3.5 h-3.5" />
          <span>Global Mosque Catalog</span>
        </div>
        <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">
          Mosque Directory & Search
        </h1>
        <p className="text-sm text-slate-500">
          Browse verified Islamic centres, prayer facilities, and community hubs worldwide.
        </p>
      </div>

      {/* Search & Filter Bar */}
      <form onSubmit={handleSearchSubmit} className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm grid grid-cols-1 sm:grid-cols-12 gap-3">
        <div className="sm:col-span-5 relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3.5" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search by mosque name, keywords, or address..."
            className="w-full pl-9 pr-3 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
          />
        </div>

        <div className="sm:col-span-3 relative">
          <MapPin className="w-4 h-4 text-slate-400 absolute left-3 top-3.5" />
          <select
            value={selectedCity}
            onChange={(e) => setSelectedCity(e.target.value)}
            className="w-full pl-9 pr-3 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 appearance-none text-slate-700"
          >
            <option value="">All Cities</option>
            <option value="London">London, UK</option>
            <option value="Cambridge">Cambridge, UK</option>
            <option value="Birmingham">Birmingham, UK</option>
            <option value="Edinburgh">Edinburgh, UK</option>
            <option value="New York">New York, US</option>
          </select>
        </div>

        <div className="sm:col-span-2 relative">
          <Globe className="w-4 h-4 text-slate-400 absolute left-3 top-3.5" />
          <select
            value={selectedCountry}
            onChange={(e) => setSelectedCountry(e.target.value)}
            className="w-full pl-9 pr-3 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 appearance-none text-slate-700"
          >
            <option value="">All Countries</option>
            <option value="United Kingdom">United Kingdom</option>
            <option value="United States">United States</option>
          </select>
        </div>

        <div className="sm:col-span-2">
          <button
            type="submit"
            className="w-full py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold rounded-xl text-sm transition-all shadow-sm"
          >
            Search
          </button>
        </div>
      </form>

      {/* Results Header */}
      <div className="flex items-center justify-between text-xs text-slate-500">
        <span>
          Found <strong className="text-slate-900">{totalCount}</strong> verified mosques
        </span>
        <span>
          Page {page + 1} of {totalPages}
        </span>
      </div>

      {/* Grid of Mosque Cards */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3, 4, 5, 6].map((n) => (
            <div key={n} className="bg-white rounded-2xl h-80 border border-slate-200 animate-pulse"></div>
          ))}
        </div>
      ) : mosques.length === 0 ? (
        <div className="bg-white rounded-2xl p-12 text-center border border-slate-200 space-y-3">
          <p className="text-slate-700 font-bold text-base">No mosques match your search criteria</p>
          <p className="text-xs text-slate-500">Try adjusting your keywords or clearing the city and country filters.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {mosques.map((mosque) => (
            <MosqueCard key={mosque.id} mosque={mosque} />
          ))}
        </div>
      )}

      {/* Pagination Controls */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-3 pt-6">
          <button
            onClick={() => executeSearch(page - 1)}
            disabled={page === 0}
            className="p-2 rounded-xl border border-slate-200 bg-white text-slate-600 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
          >
            <ChevronLeft className="w-5 h-5" />
          </button>
          <span className="text-xs font-semibold text-slate-700">
            Page {page + 1} of {totalPages}
          </span>
          <button
            onClick={() => executeSearch(page + 1)}
            disabled={page + 1 >= totalPages}
            className="p-2 rounded-xl border border-slate-200 bg-white text-slate-600 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
          >
            <ChevronRight className="w-5 h-5" />
          </button>
        </div>
      )}
    </div>
  );
};
