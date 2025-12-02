package utils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class TransactionStore {
    public static final String TYPE_INCOME = "Pemasukan";
    public static final String TYPE_EXPENSE = "Pengeluaran";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final List<Transaction> transactions = new ArrayList<>();
    private static final List<Consumer<Snapshot>> listeners = new ArrayList<>();

    private TransactionStore() {}

    public static synchronized void addListener(Consumer<Snapshot> listener) {
        listeners.add(listener);
        listener.accept(snapshot());
    }

    public static synchronized String addTransaction(String date, String type, String category, String accountName, String accountType, long amount, String desc) {
        String id = UUID.randomUUID().toString();
        addInternal(id, parseDate(date), type, category, accountName, accountType, amount, desc);
        notifyListeners();
        return id;
    }

    public static synchronized void updateTransaction(String id, String date, String type, String category, String accountName, String accountType, long amount, String desc) {
        removeTransaction(id);
        addInternal(id, parseDate(date), type, category, accountName, accountType, amount, desc);
        notifyListeners();
    }

    public static synchronized void removeTransaction(String id) {
        transactions.removeIf(t -> t.id().equals(id));
        notifyListeners();
    }

    public static synchronized Snapshot snapshot() {
        List<Transaction> copy = new ArrayList<>(transactions);
        copy.sort(Comparator.comparing(Transaction::date).reversed());
        return new Snapshot(copy);
    }

    private static void addInternal(String id, LocalDate date, String type, String category, String accountName, String accountType, long amount, String desc) {
        transactions.add(new Transaction(id, date, type, category, accountName, accountType, amount, desc));
    }

    private static void seed(String date, String type, String category, String accountName, String accountType, long amount, String desc) {
        addInternal(UUID.randomUUID().toString(), parseDate(date), type, category, accountName, accountType, amount, desc);
    }

    private static LocalDate parseDate(String input) {
        try {
            return LocalDate.parse(input, FMT);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    public record Transaction(String id, LocalDate date, String type, String category, String accountName, String accountType, long amount, String description) {
        public boolean isIncome() { return TYPE_INCOME.equalsIgnoreCase(type); }
        public YearMonth yearMonth() { return YearMonth.from(date); }
    }

    public record Snapshot(List<Transaction> transactions) {}

    private static void notifyListeners() {
        Snapshot snap = snapshot();
        for (Consumer<Snapshot> l : listeners) {
            l.accept(snap);
        }
    }
}
