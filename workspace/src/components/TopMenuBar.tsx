import { useState } from 'react';
import { ChevronDown, User, Settings, LogOut, HelpCircle } from 'lucide-react';

export function TopMenuBar() {
  const [activeMenu, setActiveMenu] = useState<string | null>(null);
  const [showProfileMenu, setShowProfileMenu] = useState(false);

  const menuItems = {
    File: ['New', 'Open', 'Save', 'Export', 'Exit'],
    Edit: ['Add Transaction', 'Delete', 'Edit'],
    View: ['Dashboard', 'Reports', 'Settings'],
    Tools: ['Calculator', 'Import Data', 'Backup'],
    Help: ['Tutorial', 'About'],
  };

  return (
    <div className="bg-white border-b border-slate-200 flex items-center relative z-50">
      {/* Menu Items */}
      <div className="flex items-center gap-1 px-4 py-1">
        {Object.entries(menuItems).map(([menu, items]) => (
          <div key={menu} className="relative">
            <button
              className={`px-3 py-1 hover:bg-slate-100 rounded transition-colors ${
                activeMenu === menu ? 'bg-slate-100' : ''
              }`}
              onClick={() => setActiveMenu(activeMenu === menu ? null : menu)}
            >
              {menu}
            </button>
            
            {/* Dropdown Menu */}
            {activeMenu === menu && (
              <>
                <div 
                  className="fixed inset-0 z-10" 
                  onClick={() => setActiveMenu(null)}
                />
                <div className="absolute top-full left-0 mt-1 bg-white border border-slate-200 rounded-md shadow-lg py-1 min-w-[180px] z-20">
                  {items.map((item) => (
                    <button
                      key={item}
                      className="w-full px-4 py-2 text-left hover:bg-slate-100 text-slate-800 transition-colors"
                      onClick={() => setActiveMenu(null)}
                    >
                      {item}
                    </button>
                  ))}
                </div>
              </>
            )}
          </div>
        ))}
      </div>

      {/* App Title - Center */}
      <div className="flex-1 text-center">
        <span className="text-slate-800">FinTrack - Financial Tracker</span>
      </div>

      {/* User Profile - Right */}
      <div className="px-4 py-1 relative">
        <button
          className="flex items-center gap-2 px-3 py-1 hover:bg-slate-100 rounded transition-colors"
          onClick={() => setShowProfileMenu(!showProfileMenu)}
        >
          <div className="w-6 h-6 bg-blue-600 rounded-full flex items-center justify-center">
            <User className="h-4 w-4 text-white" />
          </div>
          <span className="text-slate-800">John Doe</span>
          <ChevronDown className="h-4 w-4 text-slate-600" />
        </button>

        {/* Profile Dropdown */}
        {showProfileMenu && (
          <>
            <div 
              className="fixed inset-0 z-10" 
              onClick={() => setShowProfileMenu(false)}
            />
            <div className="absolute top-full right-4 mt-1 bg-white border border-slate-200 rounded-md shadow-lg py-1 min-w-[200px] z-20">
              <div className="px-4 py-3 border-b border-slate-200">
                <p className="text-slate-800">John Doe</p>
                <p className="text-slate-600">john@example.com</p>
              </div>
              <button className="w-full px-4 py-2 text-left hover:bg-slate-100 text-slate-800 transition-colors flex items-center gap-2">
                <User className="h-4 w-4" />
                Profile
              </button>
              <button className="w-full px-4 py-2 text-left hover:bg-slate-100 text-slate-800 transition-colors flex items-center gap-2">
                <Settings className="h-4 w-4" />
                Settings
              </button>
              <button className="w-full px-4 py-2 text-left hover:bg-slate-100 text-slate-800 transition-colors flex items-center gap-2">
                <HelpCircle className="h-4 w-4" />
                Help
              </button>
              <div className="border-t border-slate-200 mt-1 pt-1">
                <button className="w-full px-4 py-2 text-left hover:bg-red-50 text-red-600 transition-colors flex items-center gap-2">
                  <LogOut className="h-4 w-4" />
                  Logout
                </button>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
