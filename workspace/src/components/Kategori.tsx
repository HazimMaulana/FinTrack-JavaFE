import { useState } from 'react';
import { Card } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { Plus, Edit, Trash2, TrendingUp, TrendingDown, Tag } from 'lucide-react';

export function Kategori() {
  const [showForm, setShowForm] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [selectedType, setSelectedType] = useState<'income' | 'expense'>('expense');

  const incomeCategories = [
    { id: 1, name: 'Gaji', icon: '💼', color: '#059669', transactions: 12, total: 102000000 },
    { id: 2, name: 'Freelance', icon: '💻', color: '#0EA5E9', transactions: 8, total: 18500000 },
    { id: 3, name: 'Investasi', icon: '📈', color: '#8B5CF6', transactions: 5, total: 6200000 },
    { id: 4, name: 'Bisnis', icon: '🏪', color: '#F59E0B', transactions: 24, total: 45800000 },
    { id: 5, name: 'Lainnya', icon: '💰', color: '#64748B', transactions: 6, total: 3500000 },
  ];

  const expenseCategories = [
    { id: 1, name: 'Makanan & Minuman', icon: '🍔', color: '#2563EB', transactions: 87, total: 27500000 },
    { id: 2, name: 'Transportasi', icon: '🚗', color: '#059669', transactions: 52, total: 16500000 },
    { id: 3, name: 'Belanja', icon: '🛍️', color: '#DC2626', transactions: 34, total: 19800000 },
    { id: 4, name: 'Tagihan', icon: '📄', color: '#F59E0B', transactions: 18, total: 22000000 },
    { id: 5, name: 'Kesehatan', icon: '🏥', color: '#EC4899', transactions: 12, total: 8500000 },
    { id: 6, name: 'Pendidikan', icon: '📚', color: '#8B5CF6', transactions: 6, total: 5200000 },
    { id: 7, name: 'Entertainment', icon: '🎮', color: '#06B6D4', transactions: 28, total: 4800000 },
    { id: 8, name: 'Cicilan', icon: '🏦', color: '#EF4444', transactions: 11, total: 38500000 },
    { id: 9, name: 'Lainnya', icon: '💸', color: '#64748B', transactions: 23, total: 10450000 },
  ];

  const categories = selectedType === 'income' ? incomeCategories : expenseCategories;

  return (
    <div className="p-8">
      {/* Header */}
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-slate-800 mb-1">Kategori</h1>
          <p className="text-slate-600">Kelola kategori pemasukan dan pengeluaran</p>
        </div>
        <Button 
          className="bg-blue-600 hover:bg-blue-700 gap-2"
          onClick={() => {
            setShowForm(true);
            setEditMode(false);
          }}
        >
          <Plus className="h-4 w-4" />
          Tambah Kategori Baru
        </Button>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-4 gap-4 mb-6">
        <Card className="p-6 shadow-sm">
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2 bg-emerald-50 rounded-lg">
              <TrendingUp className="h-5 w-5 text-emerald-600" />
            </div>
            <p className="text-slate-600">Kategori Pemasukan</p>
          </div>
          <p className="text-slate-800">{incomeCategories.length} kategori</p>
        </Card>
        <Card className="p-6 shadow-sm">
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2 bg-red-50 rounded-lg">
              <TrendingDown className="h-5 w-5 text-red-600" />
            </div>
            <p className="text-slate-600">Kategori Pengeluaran</p>
          </div>
          <p className="text-slate-800">{expenseCategories.length} kategori</p>
        </Card>
        <Card className="p-6 shadow-sm">
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2 bg-blue-50 rounded-lg">
              <Tag className="h-5 w-5 text-blue-600" />
            </div>
            <p className="text-slate-600">Total Kategori</p>
          </div>
          <p className="text-slate-800">{incomeCategories.length + expenseCategories.length} kategori</p>
        </Card>
        <Card className="p-6 shadow-sm">
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2 bg-amber-50 rounded-lg">
              <TrendingUp className="h-5 w-5 text-amber-600" />
            </div>
            <p className="text-slate-600">Paling Aktif</p>
          </div>
          <p className="text-slate-800">Makanan & Minuman</p>
        </Card>
      </div>

      {/* Main Content */}
      <div className="grid grid-cols-3 gap-6">
        {/* Categories List */}
        <div className="col-span-2">
          <Card className="p-6 shadow-sm">
            <Tabs value={selectedType} onValueChange={(v) => setSelectedType(v as 'income' | 'expense')}>
              <div className="flex items-center justify-between mb-6">
                <TabsList>
                  <TabsTrigger value="expense" className="gap-2">
                    <TrendingDown className="h-4 w-4" />
                    Pengeluaran
                  </TabsTrigger>
                  <TabsTrigger value="income" className="gap-2">
                    <TrendingUp className="h-4 w-4" />
                    Pemasukan
                  </TabsTrigger>
                </TabsList>
              </div>

              <TabsContent value="expense" className="mt-0">
                <div className="space-y-3">
                  {expenseCategories.map((category) => (
                    <div 
                      key={category.id}
                      className="flex items-center justify-between p-4 border border-slate-200 rounded-lg hover:border-slate-300 hover:bg-slate-50 transition-all"
                    >
                      <div className="flex items-center gap-4">
                        <div 
                          className="w-12 h-12 rounded-lg flex items-center justify-center text-2xl"
                          style={{ backgroundColor: `${category.color}20` }}
                        >
                          {category.icon}
                        </div>
                        <div>
                          <p className="text-slate-800 mb-1">{category.name}</p>
                          <p className="text-slate-600">
                            {category.transactions} transaksi
                          </p>
                        </div>
                      </div>
                      <div className="flex items-center gap-4">
                        <div className="text-right">
                          <p className="text-slate-600 mb-1">Total</p>
                          <p className="text-red-600">
                            Rp {category.total.toLocaleString('id-ID')}
                          </p>
                        </div>
                        <div className="flex gap-1">
                          <Button 
                            variant="ghost" 
                            size="icon"
                            onClick={() => {
                              setShowForm(true);
                              setEditMode(true);
                            }}
                          >
                            <Edit className="h-4 w-4 text-blue-600" />
                          </Button>
                          <Button variant="ghost" size="icon">
                            <Trash2 className="h-4 w-4 text-red-600" />
                          </Button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </TabsContent>

              <TabsContent value="income" className="mt-0">
                <div className="space-y-3">
                  {incomeCategories.map((category) => (
                    <div 
                      key={category.id}
                      className="flex items-center justify-between p-4 border border-slate-200 rounded-lg hover:border-slate-300 hover:bg-slate-50 transition-all"
                    >
                      <div className="flex items-center gap-4">
                        <div 
                          className="w-12 h-12 rounded-lg flex items-center justify-center text-2xl"
                          style={{ backgroundColor: `${category.color}20` }}
                        >
                          {category.icon}
                        </div>
                        <div>
                          <p className="text-slate-800 mb-1">{category.name}</p>
                          <p className="text-slate-600">
                            {category.transactions} transaksi
                          </p>
                        </div>
                      </div>
                      <div className="flex items-center gap-4">
                        <div className="text-right">
                          <p className="text-slate-600 mb-1">Total</p>
                          <p className="text-emerald-600">
                            Rp {category.total.toLocaleString('id-ID')}
                          </p>
                        </div>
                        <div className="flex gap-1">
                          <Button 
                            variant="ghost" 
                            size="icon"
                            onClick={() => {
                              setShowForm(true);
                              setEditMode(true);
                            }}
                          >
                            <Edit className="h-4 w-4 text-blue-600" />
                          </Button>
                          <Button variant="ghost" size="icon">
                            <Trash2 className="h-4 w-4 text-red-600" />
                          </Button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </TabsContent>
            </Tabs>
          </Card>
        </div>

        {/* Form Panel */}
        <div className="col-span-1">
          <Card className="p-6 shadow-sm sticky top-8">
            {showForm ? (
              <>
                <div className="mb-4 flex items-center justify-between">
                  <h3 className="text-slate-800">
                    {editMode ? 'Edit Kategori' : 'Tambah Kategori'}
                  </h3>
                  <Button 
                    variant="ghost" 
                    size="sm"
                    onClick={() => setShowForm(false)}
                  >
                    Tutup
                  </Button>
                </div>

                <form className="space-y-4">
                  <div>
                    <Label htmlFor="categoryType">Tipe Kategori</Label>
                    <Select defaultValue="expense">
                      <SelectTrigger id="categoryType">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="income">Pemasukan</SelectItem>
                        <SelectItem value="expense">Pengeluaran</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label htmlFor="categoryName">Nama Kategori</Label>
                    <Input 
                      id="categoryName" 
                      placeholder="Contoh: Makanan & Minuman"
                    />
                  </div>

                  <div>
                    <Label htmlFor="categoryIcon">Icon (Emoji)</Label>
                    <Input 
                      id="categoryIcon" 
                      placeholder="Contoh: 🍔"
                      maxLength={2}
                    />
                    <p className="text-slate-600 mt-1">
                      Pilih emoji untuk kategori
                    </p>
                  </div>

                  <div>
                    <Label htmlFor="categoryColor">Warna</Label>
                    <div className="grid grid-cols-5 gap-2 mt-2">
                      {['#2563EB', '#059669', '#DC2626', '#F59E0B', '#8B5CF6', '#EC4899', '#06B6D4', '#EF4444', '#64748B', '#0EA5E9'].map(color => (
                        <button
                          key={color}
                          type="button"
                          className="w-10 h-10 rounded-lg border-2 border-slate-200 hover:border-slate-400 transition-colors"
                          style={{ backgroundColor: color }}
                        />
                      ))}
                    </div>
                  </div>

                  <div>
                    <Label htmlFor="categoryDesc">Deskripsi (Optional)</Label>
                    <textarea 
                      id="categoryDesc"
                      className="w-full px-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-600"
                      rows={3}
                      placeholder="Tambahkan deskripsi..."
                    ></textarea>
                  </div>

                  <div className="flex gap-2 pt-4">
                    <Button className="flex-1 bg-blue-600 hover:bg-blue-700">
                      Simpan
                    </Button>
                    <Button 
                      type="button" 
                      variant="outline" 
                      className="flex-1"
                      onClick={() => setShowForm(false)}
                    >
                      Batal
                    </Button>
                  </div>
                </form>
              </>
            ) : (
              <>
                <div className="mb-4">
                  <h3 className="text-slate-800">Statistik Kategori</h3>
                </div>

                <div className="space-y-4">
                  <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg">
                    <p className="text-slate-600 mb-1">Kategori Paling Aktif</p>
                    <p className="text-slate-800">Makanan & Minuman</p>
                    <p className="text-slate-600 mt-2">87 transaksi bulan ini</p>
                  </div>

                  <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
                    <p className="text-slate-600 mb-1">Pengeluaran Terbesar</p>
                    <p className="text-slate-800">Cicilan</p>
                    <p className="text-red-600 mt-2">Rp 38.500.000</p>
                  </div>

                  <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-lg">
                    <p className="text-slate-600 mb-1">Pemasukan Terbesar</p>
                    <p className="text-slate-800">Gaji</p>
                    <p className="text-emerald-600 mt-2">Rp 102.000.000</p>
                  </div>

                  <div className="p-4 bg-slate-50 border border-slate-200 rounded-lg">
                    <p className="text-slate-600 mb-1">Tips</p>
                    <p className="text-slate-700 mt-2">
                      Gunakan kategori yang spesifik untuk tracking yang lebih baik. Anda dapat menambahkan emoji untuk mempermudah identifikasi.
                    </p>
                  </div>
                </div>
              </>
            )}
          </Card>
        </div>
      </div>
    </div>
  );
}
