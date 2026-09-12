import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Navbar } from './components/layout/Navbar';
import { Footer } from './components/layout/Footer';
import { ToastContainer } from './components/common/ToastContainer';
import { DevMockSwitcher } from './components/common/DevMockSwitcher';
import { ExplorerPage } from './pages/ExplorerPage';
import { DirectoryPage } from './pages/DirectoryPage';
import { MosqueDetailPage } from './pages/MosqueDetailPage';
import { ClaimMosquePage } from './pages/ClaimMosquePage';
import { ContributePage } from './pages/ContributePage';
import { ProfilePage } from './pages/ProfilePage';
import { AdminDashboardPage } from './pages/AdminDashboardPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5, // 5 minutes cache
      refetchOnWindowFocus: false,
    },
  },
});

export const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900 selection:bg-emerald-200 selection:text-emerald-900">
          <Navbar />

          <main className="flex-1">
            <Routes>
              <Route path="/" element={<ExplorerPage />} />
              <Route path="/mosques" element={<DirectoryPage />} />
              <Route path="/mosques/:idOrSlug" element={<MosqueDetailPage />} />
              <Route path="/mosques/:id/claim" element={<ClaimMosquePage />} />
              <Route path="/contribute" element={<ContributePage />} />
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/admin" element={<AdminDashboardPage />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </main>

          <Footer />

          {/* Persistent global UI overlays */}
          <ToastContainer />
          <DevMockSwitcher />
        </div>
      </BrowserRouter>
    </QueryClientProvider>
  );
};

export default App;
