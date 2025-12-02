package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public final class CategoryStore {
    public static final String EXPENSE = "Pengeluaran";
    public static final String INCOME = "Pemasukan";

    private static final List<String> expenseCategories = new ArrayList<>();
    private static final List<String> incomeCategories = new ArrayList<>();
    private static final List<Consumer<Snapshot>> listeners = new ArrayList<>();

    static {
        // Seed with existing categories so UI stays in sync
        expenseCategories.addAll(List.of(
            "Makanan & Minuman",
            "Transportasi",
            "Belanja",
            "Tagihan",
            "Kesehatan",
            "Pendidikan",
            "Entertainment",
            "Cicilan",
            "Lainnya"
        ));
        incomeCategories.addAll(List.of(
            "Gaji",
            "Freelance",
            "Investasi",
            "Bisnis",
            "Lainnya"
        ));
    }

    private CategoryStore() {}

    public static synchronized void addListener(Consumer<Snapshot> listener) {
        listeners.add(listener);
        listener.accept(snapshot());
    }

    public static synchronized void addCategory(String type, String name) {
        if (name == null || name.trim().isEmpty()) return;
        String normalized = name.trim();
        if (INCOME.equalsIgnoreCase(type)) {
            if (!containsIgnoreCase(incomeCategories, normalized)) {
                incomeCategories.add(normalized);
                notifyListeners();
            }
        } else {
            if (!containsIgnoreCase(expenseCategories, normalized)) {
                expenseCategories.add(normalized);
                notifyListeners();
            }
        }
    }

    public static synchronized Snapshot snapshot() {
        return new Snapshot(
            List.copyOf(expenseCategories),
            List.copyOf(incomeCategories)
        );
    }

    public static synchronized void removeCategory(String type, String name) {
        if (name == null) return;
        String target = name.trim();
        boolean removed = false;
        if (INCOME.equalsIgnoreCase(type)) {
            removed = incomeCategories.removeIf(s -> s.equalsIgnoreCase(target));
        } else {
            removed = expenseCategories.removeIf(s -> s.equalsIgnoreCase(target));
        }
        if (removed) notifyListeners();
    }

    private static boolean containsIgnoreCase(List<String> list, String value) {
        String cmp = value.toLowerCase(Locale.ROOT);
        for (String s : list) {
            if (s.toLowerCase(Locale.ROOT).equals(cmp)) return true;
        }
        return false;
    }

    private static void notifyListeners() {
        Snapshot snap = snapshot();
        for (Consumer<Snapshot> l : listeners) {
            l.accept(snap);
        }
    }

    public record Snapshot(List<String> expenses, List<String> incomes) {}
}
