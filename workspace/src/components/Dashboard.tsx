import { Card } from './ui/card';
import { ArrowUpCircle, ArrowDownCircle, TrendingUp, Wallet } from 'lucide-react';
import { LineChart, Line, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

export function Dashboard() {
  const summaryCards = [
    {
      title: 'Total Saldo',
      value: 'Rp 45.250.000',
      icon: Wallet,
      color: 'text-blue-600',
      bgColor: 'bg-blue-50',
    },
    {
      title: 'Pemasukan Bulan Ini',
      value: 'Rp 12.500.000',
      icon: ArrowUpCircle,
      color: 'text-emerald-600',
      bgColor: 'bg-emerald-50',
    },
    {
      title: 'Pengeluaran Bulan Ini',
      value: 'Rp 8.750.000',
      icon: ArrowDownCircle,
      color: 'text-red-600',
      bgColor: 'bg-red-50',
    },
    {
      title: 'Selisih (Profit/Loss)',
      value: 'Rp 3.750.000',
      icon: TrendingUp,
      color: 'text-emerald-600',
      bgColor: 'bg-emerald-50',
      subtitle: '+30.0% dari bulan lalu',
    },
  ];

  const trendData = [
    { month: 'Jun', pemasukan: 10000000, pengeluaran: 7500000 },
    { month: 'Jul', pemasukan: 11500000, pengeluaran: 8200000 },
    { month: 'Agu', pemasukan: 9800000, pengeluaran: 7800000 },
    { month: 'Sep', pemasukan: 12000000, pengeluaran: 8500000 },
    { month: 'Okt', pemasukan: 11200000, pengeluaran: 9000000 },
    { month: 'Nov', pemasukan: 12500000, pengeluaran: 8750000 },
  ];

  const categoryData = [
    { name: 'Makanan & Minuman', value: 2500000, color: '#2563EB' },
    { name: 'Transportasi', value: 1500000, color: '#059669' },
    { name: 'Belanja', value: 1800000, color: '#DC2626' },
    { name: 'Tagihan', value: 2000000, color: '#F59E0B' },
    { name: 'Lainnya', value: 950000, color: '#64748B' },
  ];

  const recentTransactions = [
    { id: 1, date: '18 Nov 2025', desc: 'Gaji Bulanan', category: 'Pemasukan', type: 'in', amount: 8500000, balance: 45250000 },
    { id: 2, date: '17 Nov 2025', desc: 'Belanja Bulanan', category: 'Belanja', type: 'out', amount: 1200000, balance: 36750000 },
    { id: 3, date: '16 Nov 2025', desc: 'Bayar Listrik', category: 'Tagihan', type: 'out', amount: 450000, balance: 37950000 },
    { id: 4, date: '15 Nov 2025', desc: 'Makan Siang', category: 'Makanan', type: 'out', amount: 75000, balance: 38400000 },
    { id: 5, date: '14 Nov 2025', desc: 'Freelance Project', category: 'Pemasukan', type: 'in', amount: 2500000, balance: 38475000 },
    { id: 6, date: '13 Nov 2025', desc: 'Transportasi', category: 'Transportasi', type: 'out', amount: 150000, balance: 35975000 },
    { id: 7, date: '12 Nov 2025', desc: 'Bayar Internet', category: 'Tagihan', type: 'out', amount: 350000, balance: 36125000 },
    { id: 8, date: '11 Nov 2025', desc: 'Cicilan Usaha', category: 'Pemasukan', type: 'in', amount: 1500000, balance: 36475000 },
  ];

  const upcomingBills = [
    { name: 'Cicilan KPR', amount: 'Rp 3.500.000', due: '25 Nov 2025' },
    { name: 'Asuransi Kesehatan', amount: 'Rp 500.000', due: '28 Nov 2025' },
    { name: 'Kartu Kredit', amount: 'Rp 1.250.000', due: '30 Nov 2025' },
  ];

  return (
    <div className="p-8">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-slate-800 mb-1">Dashboard</h1>
        <p className="text-slate-600">Overview keuangan Anda</p>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-4 gap-4 mb-6">
        {summaryCards.map((card, index) => {
          const Icon = card.icon;
          return (
            <Card key={index} className="p-6 shadow-sm">
              <div className="flex items-start justify-between mb-4">
                <div className={`p-3 rounded-lg ${card.bgColor}`}>
                  <Icon className={`h-6 w-6 ${card.color}`} />
                </div>
              </div>
              <div>
                <p className="text-slate-600 mb-1">{card.title}</p>
                <p className="text-slate-800">{card.value}</p>
                {card.subtitle && (
                  <p className="text-emerald-600 mt-1">{card.subtitle}</p>
                )}
              </div>
            </Card>
          );
        })}
      </div>

      {/* Charts */}
      <div className="grid grid-cols-3 gap-6 mb-6">
        {/* Line Chart */}
        <Card className="col-span-2 p-6 shadow-sm">
          <h3 className="text-slate-800 mb-4">Trend 6 Bulan Terakhir</h3>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={trendData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#E2E8F0" />
              <XAxis dataKey="month" stroke="#64748B" />
              <YAxis stroke="#64748B" />
              <Tooltip 
                formatter={(value: number) => `Rp ${value.toLocaleString('id-ID')}`}
                contentStyle={{ backgroundColor: '#FFF', border: '1px solid #E2E8F0', borderRadius: '8px' }}
              />
              <Legend />
              <Line type="monotone" dataKey="pemasukan" stroke="#059669" strokeWidth={2} name="Pemasukan" />
              <Line type="monotone" dataKey="pengeluaran" stroke="#DC2626" strokeWidth={2} name="Pengeluaran" />
            </LineChart>
          </ResponsiveContainer>
        </Card>

        {/* Pie Chart */}
        <Card className="p-6 shadow-sm">
          <h3 className="text-slate-800 mb-4">Kategori Pengeluaran</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={categoryData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                {categoryData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip formatter={(value: number) => `Rp ${value.toLocaleString('id-ID')}`} />
            </PieChart>
          </ResponsiveContainer>
        </Card>
      </div>

      {/* Bottom Section */}
      <div className="grid grid-cols-1 gap-6">
        {/* Recent Transactions */}
        <Card className="p-6 shadow-sm">
          <h3 className="text-slate-800 mb-4">10 Transaksi Terakhir</h3>
          <div className="overflow-auto" style={{ maxHeight: '300px' }}>
            <table className="w-full">
              <thead className="border-b border-slate-200 sticky top-0 bg-white">
                <tr>
                  <th className="text-left text-slate-700 pb-3 pr-4">Tanggal</th>
                  <th className="text-left text-slate-700 pb-3 pr-4">Keterangan</th>
                  <th className="text-left text-slate-700 pb-3 pr-4">Kategori</th>
                  <th className="text-right text-slate-700 pb-3 pr-4">Nominal</th>
                  <th className="text-right text-slate-700 pb-3">Saldo</th>
                </tr>
              </thead>
              <tbody>
                {recentTransactions.map((tx, index) => (
                  <tr 
                    key={tx.id} 
                    className={`border-b border-slate-100 hover:bg-slate-50 ${index % 2 === 0 ? 'bg-slate-50/50' : ''}`}
                  >
                    <td className="py-3 pr-4 text-slate-600">{tx.date}</td>
                    <td className="py-3 pr-4 text-slate-800">{tx.desc}</td>
                    <td className="py-3 pr-4 text-slate-600">{tx.category}</td>
                    <td className={`py-3 pr-4 text-right ${tx.type === 'in' ? 'text-emerald-600' : 'text-red-600'}`}>
                      {tx.type === 'in' ? '+' : '-'} Rp {tx.amount.toLocaleString('id-ID')}
                    </td>
                    <td className="py-3 text-right text-slate-800">
                      Rp {tx.balance.toLocaleString('id-ID')}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      </div>
    </div>
  );
}