import { useState } from 'react';
import { Sidebar } from './components/Sidebar';
import { TopMenuBar } from './components/TopMenuBar';
import { TopToolbar } from './components/TopToolbar';
import { Dashboard } from './components/Dashboard';
import { Transaksi } from './components/Transaksi';
import { Laporan } from './components/Laporan';
import { Kategori } from './components/Kategori';
import { StatusBar } from './components/StatusBar';

export default function App() {
  const [activeMenu, setActiveMenu] = useState('dashboard');

  const renderContent = () => {
    switch (activeMenu) {
      case 'dashboard':
        return <Dashboard />;
      case 'transaksi':
        return <Transaksi />;
      case 'laporan':
        return <Laporan />;
      case 'kategori':
        return <Kategori />;
      default:
        return <Dashboard />;
    }
  };

  return (
    <div className="h-screen flex flex-col bg-slate-50 overflow-hidden">
      {/* Top Menu Bar */}
      <TopMenuBar />
      
      {/* Top Toolbar */}
      <TopToolbar />

      {/* Main Layout */}
      <div className="flex flex-1 overflow-hidden">
        {/* Sidebar */}
        <Sidebar activeMenu={activeMenu} onMenuChange={setActiveMenu} />
        
        {/* Main Content Area */}
        <main className="flex-1 overflow-auto">
          {renderContent()}
        </main>
      </div>

      {/* Status Bar */}
      <StatusBar />
    </div>
  );
}