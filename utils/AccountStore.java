package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class AccountStore {
    public static final String TYPE_BANK = "Bank";
    public static final String TYPE_WALLET = "Dompet Digital";
    public static final String TYPE_CASH = "Cash";
    public static final String TYPE_CREDIT = "Kredit";

    private static final List<Account> accounts = new ArrayList<>();
    private static final List<Consumer<Snapshot>> listeners = new ArrayList<>();

    private AccountStore(){}

    public static synchronized void addListener(Consumer<Snapshot> listener){
        listeners.add(listener);
        listener.accept(snapshot());
    }

    public static synchronized void addAccount(String name, String number, long balance, String type){
        Account acc = new Account(UUID.randomUUID().toString(), name, number, balance, type);
        accounts.add(acc);
        notifyListeners();
    }

    public static synchronized void removeAccount(String id){
        accounts.removeIf(a -> a.id().equals(id));
        notifyListeners();
    }

    public static synchronized Snapshot snapshot(){
        return new Snapshot(List.copyOf(accounts));
    }

    private static void notifyListeners(){
        Snapshot snap = snapshot();
        for(Consumer<Snapshot> l : listeners){
            l.accept(snap);
        }
    }

    public record Account(String id, String name, String number, long balance, String type) {}
    public record Snapshot(List<Account> accounts){}
}
