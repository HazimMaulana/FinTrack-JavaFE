import { useState } from 'react';
import { Plus, Calendar, Search, RefreshCw, Upload, Bell, Clock, TrendingUp, TrendingDown, Wallet } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';

export function TopToolbar() {
  const [showNotifications, setShowNotifications] = useState(false);
  const currentDate = new Date().toLocaleDateString('id-ID', { 
    weekday: 'long', 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  });
  const currentTime = new Date().toLocaleTimeString('id-ID', { 
    hour: '2-digit', 
    minute: '2-digit' 
  });

  const notifications = [
    { id: 1, type: 'warning', title: 'Tagihan Jatuh Tempo', message: 'Cicilan KPR jatuh tempo 25 Nov 2025', time: '1 jam lalu' },
    { id: 2, type: 'info', title: 'Backup Selesai', message: 'Database berhasil di-backup', time: '2 jam lalu' },
    { id: 3, type: 'success', title: 'Transaksi Ditambahkan', message: 'Gaji Bulanan telah dicatat', time: '3 jam lalu' },
  ];

  return (
    <div className="bg-white border-b border-slate-200">
      {/* Top Info Bar */}
      <div className="px-4 py-2 bg-slate-50 border-b border-slate-200 flex items-center justify-between">
        {/* Left - Date & Time */}
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-2 text-slate-700">
            <Calendar className="h-4 w-4 text-slate-500" />
            <span>{currentDate}</span>
          </div>
          <div className="flex items-center gap-2 text-slate-700">
            <Clock className="h-4 w-4 text-slate-500" />
            <span>{currentTime}</span>
          </div>
        </div>

        {/* Right - Quick Stats */}
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-2">
            <div className="p-1 bg-blue-50 rounded">
              <Wallet className="h-4 w-4 text-blue-600" />
            </div>
            <div>
              <p className="text-slate-600">Total Saldo</p>
              <p className="text-slate-800">Rp 45.250.000</p>
            </div>
          </div>
          
          <div className="h-8 w-px bg-slate-200"></div>
          
          <div className="flex items-center gap-2">
            <div className="p-1 bg-emerald-50 rounded">
              <TrendingUp className="h-4 w-4 text-emerald-600" />
            </div>
            <div>
              <p className="text-slate-600">Pemasukan</p>
              <p className="text-emerald-600">Rp 12.500.000</p>
            </div>
          </div>
          
          <div className="h-8 w-px bg-slate-200"></div>
          
          <div className="flex items-center gap-2">
            <div className="p-1 bg-red-50 rounded">
              <TrendingDown className="h-4 w-4 text-red-600" />
            </div>
            <div>
              <p className="text-slate-600">Pengeluaran</p>
              <p className="text-red-600">Rp 8.750.000</p>
            </div>
          </div>

          <div className="h-8 w-px bg-slate-200"></div>

          {/* Notifications */}
          <div className="relative">
            <button
              className="p-2 hover:bg-slate-100 rounded-lg relative transition-colors"
              onClick={() => setShowNotifications(!showNotifications)}
            >
              <Bell className="h-5 w-5 text-slate-700" />
              {notifications.length > 0 && (
                <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
              )}
            </button>

            {/* Notifications Dropdown */}
            {showNotifications && (
              <>
                <div 
                  className="fixed inset-0 z-10" 
                  onClick={() => setShowNotifications(false)}
                />
                <div className="absolute top-full right-0 mt-2 bg-white border border-slate-200 rounded-lg shadow-lg w-80 z-20">
                  <div className="p-4 border-b border-slate-200">
                    <div className="flex items-center justify-between">
                      <h3 className="text-slate-800">Notifikasi</h3>
                      <span className="px-2 py-1 bg-blue-50 text-blue-600 rounded">
                        {notifications.length}
                      </span>
                    </div>
                  </div>
                  <div className="max-h-96 overflow-auto">
                    {notifications.map((notif) => (
                      <div 
                        key={notif.id}
                        className="p-4 border-b border-slate-100 hover:bg-slate-50 cursor-pointer transition-colors"
                      >
                        <div className="flex items-start gap-3">
                          <div className={`p-2 rounded-lg ${
                            notif.type === 'warning' ? 'bg-amber-50' :
                            notif.type === 'success' ? 'bg-emerald-50' : 'bg-blue-50'
                          }`}>
                            <Bell className={`h-4 w-4 ${
                              notif.type === 'warning' ? 'text-amber-600' :
                              notif.type === 'success' ? 'text-emerald-600' : 'text-blue-600'
                            }`} />
                          </div>
                          <div className="flex-1">
                            <p className="text-slate-800 mb-1">{notif.title}</p>
                            <p className="text-slate-600 mb-1">{notif.message}</p>
                            <p className="text-slate-500">{notif.time}</p>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                  <div className="p-3 border-t border-slate-200">
                    <button className="w-full text-center text-blue-600 hover:text-blue-700 transition-colors">
                      Lihat Semua Notifikasi
                    </button>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Main Toolbar */}
      <div className="px-4 py-3 flex items-center gap-3">
        <Button className="bg-blue-600 hover:bg-blue-700 gap-2">
          <Plus className="h-4 w-4" />
          Transaksi Baru
          <span className="ml-1 opacity-70">Ctrl+N</span>
        </Button>
        
        <div className="h-6 w-px bg-slate-200"></div>
        
        <Button variant="outline" size="icon" title="Calendar">
          <Calendar className="h-4 w-4" />
        </Button>
        
        <div className="flex-1 max-w-md relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
          <Input 
            placeholder="Cari transaksi... (Ctrl+F)" 
            className="pl-9"
          />
        </div>
        
        <Button variant="outline" size="icon" title="Refresh (F5)">
          <RefreshCw className="h-4 w-4" />
        </Button>
        
        <Button variant="outline" className="gap-2">
          <Upload className="h-4 w-4" />
          Export
        </Button>

        {/* Window Controls (Optional - untuk Windows) */}
        <div className="ml-auto flex items-center gap-1">
          <button className="px-3 py-1 hover:bg-slate-100 rounded transition-colors text-slate-700">
            _
          </button>
          <button className="px-3 py-1 hover:bg-slate-100 rounded transition-colors text-slate-700">
            □
          </button>
          <button className="px-3 py-1 hover:bg-red-500 hover:text-white rounded transition-colors text-slate-700">
            ×
          </button>
        </div>
      </div>
    </div>
  );
}
