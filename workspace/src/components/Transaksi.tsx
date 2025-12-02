import { useState } from 'react';
import { Card } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Filter, ChevronLeft, ChevronRight, Edit, Trash2 } from 'lucide-react';

export function Transaksi() {
  const [currentPage, setCurrentPage] = useState(1);
  const [formMode, setFormMode] = useState<'add' | 'edit'>('add');

  const transactions = [
    { id: 1, date: '18/11/2025', desc: 'Gaji Bulanan', category: 'Pemasukan', account: 'BCA', debit: 8500000, credit: 0, balance: 45250000 },
    { id: 2, date: '17/11/2025', desc: 'Belanja Bulanan', category: 'Belanja', account: 'BCA', debit: 0, credit: 1200000, balance: 36750000 },
    { id: 3, date: '16/11/2025', desc: 'Bayar Listrik', category: 'Tagihan', account: 'BCA', debit: 0, credit: 450000, balance: 37950000 },
    { id: 4, date: '15/11/2025', desc: 'Makan Siang', category: 'Makanan', account: 'Cash', debit: 0, credit: 75000, balance: 38400000 },
    { id: 5, date: '14/11/2025', desc: 'Freelance Project', category: 'Pemasukan', account: 'BCA', debit: 2500000, credit: 0, balance: 38475000 },
    { id: 6, date: '13/11/2025', desc: 'Transportasi', category: 'Transportasi', account: 'Cash', debit: 0, credit: 150000, balance: 35975000 },
    { id: 7, date: '12/11/2025', desc: 'Bayar Internet', category: 'Tagihan', account: 'BCA', debit: 0, credit: 350000, balance: 36125000 },
    { id: 8, date: '11/11/2025', desc: 'Cicilan Usaha', category: 'Pemasukan', account: 'BCA', debit: 1500000, credit: 0, balance: 36475000 },
    { id: 9, date: '10/11/2025', desc: 'Belanja Groceries', category: 'Belanja', account: 'BCA', debit: 0, credit: 850000, balance: 34975000 },
    { id: 10, date: '09/11/2025', desc: 'Penjualan Produk', category: 'Pemasukan', account: 'BCA', debit: 3200000, credit: 0, balance: 35825000 },
  ];

  return (
    <div className="p-8">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-slate-800 mb-1">Transaksi</h1>
        <p className="text-slate-600">Kelola semua transaksi keuangan Anda</p>
      </div>

      {/* Dual Panel Layout */}
      <div className="grid grid-cols-5 gap-6">
        {/* Left Panel - Table View (60%) */}
        <Card className="col-span-3 p-6 shadow-sm">
          {/* Toolbar */}
          <div className="flex items-center gap-3 mb-4">
            <Button variant="outline" className="gap-2">
              <Filter className="h-4 w-4" />
              Filter
            </Button>
            
            <Input 
              placeholder="Filter by date..." 
              type="date"
              className="max-w-[180px]"
            />
            
            <Select defaultValue="all">
              <SelectTrigger className="max-w-[150px]">
                <SelectValue placeholder="Kategori" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Semua Kategori</SelectItem>
                <SelectItem value="pemasukan">Pemasukan</SelectItem>
                <SelectItem value="belanja">Belanja</SelectItem>
                <SelectItem value="tagihan">Tagihan</SelectItem>
                <SelectItem value="makanan">Makanan</SelectItem>
                <SelectItem value="transportasi">Transportasi</SelectItem>
              </SelectContent>
            </Select>

            <Select defaultValue="all">
              <SelectTrigger className="max-w-[150px]">
                <SelectValue placeholder="Akun" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Semua Akun</SelectItem>
                <SelectItem value="bca">BCA</SelectItem>
                <SelectItem value="mandiri">Mandiri</SelectItem>
                <SelectItem value="cash">Cash</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {/* Table */}
          <div className="overflow-auto" style={{ maxHeight: '500px' }}>
            <table className="w-full">
              <thead className="border-b border-slate-200 sticky top-0 bg-white">
                <tr>
                  <th className="text-left text-slate-700 pb-3 pr-2">Tanggal</th>
                  <th className="text-left text-slate-700 pb-3 pr-2">Keterangan</th>
                  <th className="text-left text-slate-700 pb-3 pr-2">Kategori</th>
                  <th className="text-right text-slate-700 pb-3 pr-2">Debit</th>
                  <th className="text-right text-slate-700 pb-3 pr-2">Kredit</th>
                  <th className="text-right text-slate-700 pb-3 pr-2">Saldo</th>
                  <th className="text-center text-slate-700 pb-3">Aksi</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((tx, index) => (
                  <tr 
                    key={tx.id} 
                    className={`border-b border-slate-100 hover:bg-slate-50 cursor-pointer ${index % 2 === 0 ? 'bg-slate-50/50' : ''}`}
                  >
                    <td className="py-3 pr-2 text-slate-600">{tx.date}</td>
                    <td className="py-3 pr-2 text-slate-800">{tx.desc}</td>
                    <td className="py-3 pr-2 text-slate-600">{tx.category}</td>
                    <td className="py-3 pr-2 text-right text-emerald-600">
                      {tx.debit > 0 ? `Rp ${tx.debit.toLocaleString('id-ID')}` : '-'}
                    </td>
                    <td className="py-3 pr-2 text-right text-red-600">
                      {tx.credit > 0 ? `Rp ${tx.credit.toLocaleString('id-ID')}` : '-'}
                    </td>
                    <td className="py-3 pr-2 text-right text-slate-800">
                      Rp {tx.balance.toLocaleString('id-ID')}
                    </td>
                    <td className="py-3 text-center">
                      <div className="flex items-center justify-center gap-1">
                        <Button 
                          variant="ghost" 
                          size="icon"
                          className="h-8 w-8"
                          onClick={() => setFormMode('edit')}
                        >
                          <Edit className="h-4 w-4 text-blue-600" />
                        </Button>
                        <Button variant="ghost" size="icon" className="h-8 w-8">
                          <Trash2 className="h-4 w-4 text-red-600" />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          <div className="flex items-center justify-between mt-4 pt-4 border-t border-slate-200">
            <p className="text-slate-600">Showing 1-10 of 1,247 transaksi</p>
            <div className="flex items-center gap-2">
              <Button 
                variant="outline" 
                size="icon"
                onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                disabled={currentPage === 1}
              >
                <ChevronLeft className="h-4 w-4" />
              </Button>
              <span className="text-slate-700 px-3">Page {currentPage} of 125</span>
              <Button 
                variant="outline" 
                size="icon"
                onClick={() => setCurrentPage(p => p + 1)}
              >
                <ChevronRight className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </Card>

        {/* Right Panel - Form (40%) */}
        <Card className="col-span-2 p-6 shadow-sm">
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-slate-800">
              {formMode === 'add' ? 'Tambah Transaksi Baru' : 'Edit Transaksi'}
            </h3>
            <Button 
              variant="ghost" 
              size="sm"
              onClick={() => setFormMode('add')}
            >
              Reset
            </Button>
          </div>

          <form className="space-y-4">
            <div>
              <Label htmlFor="date">Tanggal</Label>
              <Input 
                id="date" 
                type="date" 
                defaultValue="2025-11-18"
              />
            </div>

            <div>
              <Label htmlFor="type">Tipe Transaksi</Label>
              <Select defaultValue="expense">
                <SelectTrigger id="type">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="income">Pemasukan (Debit)</SelectItem>
                  <SelectItem value="expense">Pengeluaran (Kredit)</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div>
              <Label htmlFor="account">Akun</Label>
              <Select defaultValue="bca">
                <SelectTrigger id="account">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="bca">BCA - Rp 45.250.000</SelectItem>
                  <SelectItem value="mandiri">Mandiri - Rp 12.500.000</SelectItem>
                  <SelectItem value="cash">Cash - Rp 2.150.000</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div>
              <Label htmlFor="category">Kategori</Label>
              <Select defaultValue="makanan">
                <SelectTrigger id="category">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="pemasukan">Pemasukan</SelectItem>
                  <SelectItem value="makanan">Makanan & Minuman</SelectItem>
                  <SelectItem value="transportasi">Transportasi</SelectItem>
                  <SelectItem value="belanja">Belanja</SelectItem>
                  <SelectItem value="tagihan">Tagihan</SelectItem>
                  <SelectItem value="lainnya">Lainnya</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div>
              <Label htmlFor="amount">Nominal</Label>
              <Input 
                id="amount" 
                type="number" 
                placeholder="0"
              />
            </div>

            <div>
              <Label htmlFor="description">Keterangan</Label>
              <Input 
                id="description" 
                placeholder="Deskripsi transaksi..."
              />
            </div>

            <div>
              <Label htmlFor="notes">Catatan (Optional)</Label>
              <textarea 
                id="notes"
                className="w-full px-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-600"
                rows={3}
                placeholder="Tambahkan catatan..."
              ></textarea>
            </div>

            <div className="flex gap-2 pt-4">
              <Button className="flex-1 bg-blue-600 hover:bg-blue-700">
                {formMode === 'add' ? 'Simpan' : 'Update'}
              </Button>
              <Button 
                type="button" 
                variant="outline" 
                className="flex-1"
                onClick={() => setFormMode('add')}
              >
                Batal
              </Button>
            </div>
          </form>
        </Card>
      </div>
    </div>
  );
}
