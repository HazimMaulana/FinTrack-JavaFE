import { useState } from 'react';
import { Card } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { FileDown, Printer } from 'lucide-react';
import { BarChart, Bar, LineChart, Line, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

export function Laporan() {
  const [activeTab, setActiveTab] = useState('grafik');

  const monthlyData = [
    { month: 'Jan', pemasukan: 9500000, pengeluaran: 7200000 },
    { month: 'Feb', pemasukan: 10200000, pengeluaran: 7800000 },
    { month: 'Mar', pemasukan: 11000000, pengeluaran: 8500000 },
    { month: 'Apr', pemasukan: 9800000, pengeluaran: 7600000 },
    { month: 'Mei', pemasukan: 10500000, pengeluaran: 8200000 },
    { month: 'Jun', pemasukan: 10000000, pengeluaran: 7500000 },
    { month: 'Jul', pemasukan: 11500000, pengeluaran: 8200000 },
    { month: 'Agu', pemasukan: 9800000, pengeluaran: 7800000 },
    { month: 'Sep', pemasukan: 12000000, pengeluaran: 8500000 },
    { month: 'Okt', pemasukan: 11200000, pengeluaran: 9000000 },
    { month: 'Nov', pemasukan: 12500000, pengeluaran: 8750000 },
  ];

  const categoryBreakdown = [
    { name: 'Makanan & Minuman', value: 2500000, color: '#2563EB' },
    { name: 'Transportasi', value: 1500000, color: '#059669' },
    { name: 'Belanja', value: 1800000, color: '#DC2626' },
    { name: 'Tagihan', value: 2000000, color: '#F59E0B' },
    { name: 'Lainnya', value: 950000, color: '#64748B' },
  ];

  const comparisonData = [
    { category: 'Makanan', thisMonth: 2500000, lastMonth: 2200000 },
    { category: 'Transportasi', thisMonth: 1500000, lastMonth: 1800000 },
    { category: 'Belanja', thisMonth: 1800000, lastMonth: 1600000 },
    { category: 'Tagihan', thisMonth: 2000000, lastMonth: 2000000 },
    { category: 'Lainnya', thisMonth: 950000, lastMonth: 1100000 },
  ];

  const tableData = [
    { category: 'Pemasukan', transactions: 45, amount: 12500000, percentage: 100 },
    { category: 'Makanan & Minuman', transactions: 87, amount: 2500000, percentage: 20 },
    { category: 'Transportasi', transactions: 52, amount: 1500000, percentage: 12 },
    { category: 'Belanja', transactions: 34, amount: 1800000, percentage: 14.4 },
    { category: 'Tagihan', transactions: 8, amount: 2000000, percentage: 16 },
    { category: 'Lainnya', transactions: 23, amount: 950000, percentage: 7.6 },
  ];

  return (
    <div className="p-8">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-slate-800 mb-1">Laporan Keuangan</h1>
        <p className="text-slate-600">Analisis dan visualisasi data keuangan Anda</p>
      </div>

      {/* Filter Panel */}
      <Card className="p-6 mb-6 shadow-sm">
        <div className="grid grid-cols-5 gap-4">
          <div>
            <Label htmlFor="dateFrom">Dari Tanggal</Label>
            <Input 
              id="dateFrom" 
              type="date" 
              defaultValue="2025-01-01"
            />
          </div>
          
          <div>
            <Label htmlFor="dateTo">Sampai Tanggal</Label>
            <Input 
              id="dateTo" 
              type="date" 
              defaultValue="2025-11-18"
            />
          </div>

          <div>
            <Label htmlFor="accountFilter">Akun</Label>
            <Select defaultValue="all">
              <SelectTrigger id="accountFilter">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Semua Akun</SelectItem>
                <SelectItem value="bca">BCA</SelectItem>
                <SelectItem value="mandiri">Mandiri</SelectItem>
                <SelectItem value="cash">Cash</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div>
            <Label htmlFor="categoryFilter">Kategori</Label>
            <Select defaultValue="all">
              <SelectTrigger id="categoryFilter">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Semua Kategori</SelectItem>
                <SelectItem value="pemasukan">Pemasukan</SelectItem>
                <SelectItem value="makanan">Makanan</SelectItem>
                <SelectItem value="transportasi">Transportasi</SelectItem>
                <SelectItem value="belanja">Belanja</SelectItem>
                <SelectItem value="tagihan">Tagihan</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="flex items-end gap-2">
            <Button className="flex-1 bg-blue-600 hover:bg-blue-700">
              Tampilkan
            </Button>
          </div>
        </div>
      </Card>

      {/* Summary Cards */}
      <div className="grid grid-cols-4 gap-4 mb-6">
        <Card className="p-6 shadow-sm">
          <p className="text-slate-600 mb-2">Total Pemasukan</p>
          <p className="text-emerald-600">Rp 125.000.000</p>
        </Card>
        <Card className="p-6 shadow-sm">
          <p className="text-slate-600 mb-2">Total Pengeluaran</p>
          <p className="text-red-600">Rp 87.500.000</p>
        </Card>
        <Card className="p-6 shadow-sm">
          <p className="text-slate-600 mb-2">Net Income</p>
          <p className="text-blue-600">Rp 37.500.000</p>
        </Card>
        <Card className="p-6 shadow-sm">
          <p className="text-slate-600 mb-2">Total Transaksi</p>
          <p className="text-slate-800">1,247</p>
        </Card>
      </div>

      {/* Visualization Area with Tabs */}
      <Card className="p-6 shadow-sm">
        <div className="flex items-center justify-between mb-6">
          <Tabs value={activeTab} onValueChange={setActiveTab} className="flex-1">
            <TabsList>
              <TabsTrigger value="grafik">Grafik</TabsTrigger>
              <TabsTrigger value="tabel">Tabel</TabsTrigger>
              <TabsTrigger value="komparasi">Komparasi</TabsTrigger>
            </TabsList>
          </Tabs>

          <div className="flex gap-2">
            <Button variant="outline" className="gap-2">
              <FileDown className="h-4 w-4" />
              Export PDF
            </Button>
            <Button variant="outline" className="gap-2">
              <FileDown className="h-4 w-4" />
              Export Excel
            </Button>
            <Button variant="outline" className="gap-2">
              <Printer className="h-4 w-4" />
              Print
            </Button>
          </div>
        </div>

        {/* Grafik Tab */}
        {activeTab === 'grafik' && (
          <div className="space-y-6">
            {/* Bar Chart */}
            <div>
              <h3 className="text-slate-800 mb-4">Pemasukan vs Pengeluaran Bulanan</h3>
              <ResponsiveContainer width="100%" height={350}>
                <BarChart data={monthlyData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E2E8F0" />
                  <XAxis dataKey="month" stroke="#64748B" />
                  <YAxis stroke="#64748B" />
                  <Tooltip 
                    formatter={(value: number) => `Rp ${value.toLocaleString('id-ID')}`}
                    contentStyle={{ backgroundColor: '#FFF', border: '1px solid #E2E8F0', borderRadius: '8px' }}
                  />
                  <Legend />
                  <Bar dataKey="pemasukan" fill="#059669" name="Pemasukan" />
                  <Bar dataKey="pengeluaran" fill="#DC2626" name="Pengeluaran" />
                </BarChart>
              </ResponsiveContainer>
            </div>

            {/* Pie Chart */}
            <div>
              <h3 className="text-slate-800 mb-4">Breakdown Pengeluaran per Kategori</h3>
              <ResponsiveContainer width="100%" height={350}>
                <PieChart>
                  <Pie
                    data={categoryBreakdown}
                    cx="50%"
                    cy="50%"
                    labelLine={true}
                    label={({ name, value }) => `${name}: Rp ${value.toLocaleString('id-ID')}`}
                    outerRadius={120}
                    fill="#8884d8"
                    dataKey="value"
                  >
                    {categoryBreakdown.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value: number) => `Rp ${value.toLocaleString('id-ID')}`} />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>
        )}

        {/* Tabel Tab */}
        {activeTab === 'tabel' && (
          <div>
            <h3 className="text-slate-800 mb-4">Ringkasan per Kategori</h3>
            <table className="w-full">
              <thead className="border-b border-slate-200">
                <tr>
                  <th className="text-left text-slate-700 pb-3 pr-4">Kategori</th>
                  <th className="text-right text-slate-700 pb-3 pr-4">Jumlah Transaksi</th>
                  <th className="text-right text-slate-700 pb-3 pr-4">Total Nominal</th>
                  <th className="text-right text-slate-700 pb-3">% dari Total</th>
                </tr>
              </thead>
              <tbody>
                {tableData.map((row, index) => (
                  <tr 
                    key={index} 
                    className={`border-b border-slate-100 ${index % 2 === 0 ? 'bg-slate-50/50' : ''}`}
                  >
                    <td className="py-4 pr-4 text-slate-800">{row.category}</td>
                    <td className="py-4 pr-4 text-right text-slate-600">{row.transactions}</td>
                    <td className={`py-4 pr-4 text-right ${row.category === 'Pemasukan' ? 'text-emerald-600' : 'text-red-600'}`}>
                      Rp {row.amount.toLocaleString('id-ID')}
                    </td>
                    <td className="py-4 text-right text-slate-600">{row.percentage}%</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Komparasi Tab */}
        {activeTab === 'komparasi' && (
          <div>
            <h3 className="text-slate-800 mb-4">Perbandingan Bulan Ini vs Bulan Lalu</h3>
            <ResponsiveContainer width="100%" height={400}>
              <BarChart data={comparisonData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#E2E8F0" />
                <XAxis dataKey="category" stroke="#64748B" />
                <YAxis stroke="#64748B" />
                <Tooltip 
                  formatter={(value: number) => `Rp ${value.toLocaleString('id-ID')}`}
                  contentStyle={{ backgroundColor: '#FFF', border: '1px solid #E2E8F0', borderRadius: '8px' }}
                />
                <Legend />
                <Bar dataKey="lastMonth" fill="#94A3B8" name="Bulan Lalu" />
                <Bar dataKey="thisMonth" fill="#2563EB" name="Bulan Ini" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </Card>
    </div>
  );
}
