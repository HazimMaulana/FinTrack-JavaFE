# Design Document

## Overview

Desain ini akan mengintegrasikan frontend FinTrack Java Swing dengan backend socket server. Fokus utama adalah:
1. Membuat komponen SocketClient untuk komunikasi TCP dengan backend
2. Mengimplementasikan SessionManager untuk manajemen session token
3. Membuat AuthPage untuk login dan register
4. Mengubah Store pattern (TransactionStore, AccountStore, CategoryStore) untuk berkomunikasi dengan backend
5. Menangani async operations dengan background threads
6. Implementasi error handling dan reconnection strategy

## Architecture

Frontend akan menggunakan arsitektur berikut:

```
UI Layer (Pages)
    ↓
Store Layer (TransactionStore, AccountStore, CategoryStore)
    ↓
Client Layer (SocketClient)
    ↓
Network Layer (Socket TCP)
    ↓
Backend Server (Port 3000)
```

### Communication Flow
```
User Action (UI)
    ↓
Store Method Call
    ↓
SocketClient.sendCommand()
    ↓
Format Command (pipe-delimited)
    ↓
Send via Socket (with session token)
    ↓
Receive Response
    ↓
Parse Response
    ↓
Update Store State
    ↓
Notify Listeners
    ↓
Update UI
```

## Components and Interfaces

### 1. SocketClient

Komponen utama untuk komunikasi dengan backend server.

```java
public class SocketClient {
    private static final String HOST = "localhost";
    private static final int PORT = 3000;
    private static final int TIMEOUT = 10000; // 10 seconds
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected;
    
    // Singleton pattern
    private static SocketClient instance;
    
    public static synchronized SocketClient getInstance();
    
    // Connection management
    public synchronized void connect() throws IOException;
    public synchronized void disconnect();
    public synchronized boolean isConnected();
    private void reconnect() throws IOException;
    
    // Command execution
    public synchronized String sendCommand(String command) throws IOException;
    private String formatCommand(String... parts);
    private String readResponse() throws IOException;
    
    // Helper methods
    public String[] parseResponse(String response);
    public boolean isErrorResponse(String response);
    public String getErrorMessage(String response);
}
```

### 2. SessionManager

Mengelola session token dan persistensi ke file lokal.

```java
public class SessionManager {
    private static final String SESSION_FILE = ".fintrack_session";
    
    private String sessionToken;
    private String username;
    
    // Singleton pattern
    private static SessionManager instance;
    
    public static synchronized SessionManager getInstance();
    
    // Session management
    public void setSession(String token, String username);
    public String getSessionToken();
    public String getUsername();
    public boolean hasValidSession();
    public void clearSession();
    
    // Persistence
    public void saveToFile();
    public void loadFromFile();
    
    // Validation
    public boolean validateSession() throws IOException;
}
```

### 3. AuthPage

Halaman login dan register.

```java
public class AuthPage extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel errorLabel;
    private boolean isRegisterMode;
    private Runnable onLoginSuccess;
    
    public AuthPage(Runnable onLoginSuccess);
    
    private void handleLogin();
    private void handleRegister();
    private void toggleMode(); // Switch between login and register
    private void showError(String message);
    private void clearError();
    public void resetFields();
}
```

### 4. Updated Store Pattern

Stores akan diubah untuk berkomunikasi dengan backend melalui SocketClient.

#### TransactionStore (Updated)

```java
public final class TransactionStore {
    private static final List<Transaction> transactions = new ArrayList<>();
    private static final List<Consumer<Snapshot>> listeners = new ArrayList<>();
    private static final SocketClient client = SocketClient.getInstance();
    
    // Async operations with background threads
    public static void addTransaction(String date, String type, String category, 
                                     String accountName, String accountType, 
                                     long amount, String desc, 
                                     Consumer<String> onSuccess, 
                                     Consumer<String> onError);
    
    public static void updateTransaction(String id, String date, String type, 
                                        String category, String accountName, 
                                        String accountType, long amount, String desc,
                                        Runnable onSuccess, Consumer<String> onError);
    
    public static void removeTransaction(String id, Runnable onSuccess, 
                                        Consumer<String> onError);
    
    public static void loadFromBackend(Runnable onSuccess, Consumer<String> onError);
    
    // Internal methods
    private static void executeAsync(Runnable task);
    private static void updateUI(Runnable task);
    private static void parseAndUpdateTransactions(String response);
}
```

#### AccountStore (Updated)

```java
public final class AccountStore {
    private static final List<Account> accounts = new ArrayList<>();
    private static final List<Consumer<Snapshot>> listeners = new ArrayList<>();
    private static final SocketClient client = SocketClient.getInstance();
    
    public static void addAccount(String name, String number, long balance, 
                                 String type, Consumer<String> onSuccess, 
                                 Consumer<String> onError);
    
    public static void updateAccount(String id, String name, String number, 
                                    long balance, String type, Runnable onSuccess, 
                                    Consumer<String> onError);
    
    public static void removeAccount(String id, Runnable onSuccess, 
                                    Consumer<String> onError);
    
    public static void loadFromBackend(Runnable onSuccess, Consumer<String> onError);
    
    private static void parseAndUpdateAccounts(String response);
}
```

#### CategoryStore (Updated)

```java
public final class CategoryStore {
    private static final List<String> expenseCategories = new ArrayList<>();
    private static final List<String> incomeCategories = new ArrayList<>();
    private static final List<Consumer<Snapshot>> listeners = new ArrayList<>();
    private static final SocketClient client = SocketClient.getInstance();
    
    public static void addCategory(String type, String name, Runnable onSuccess, 
                                  Consumer<String> onError);
    
    public static void removeCategory(String type, String name, Runnable onSuccess, 
                                     Consumer<String> onError);
    
    public static void loadFromBackend(Runnable onSuccess, Consumer<String> onError);
    
    private static void parseAndUpdateCategories(String response);
}
```

### 5. Updated Main.java

Main akan diupdate untuk menangani session validation dan routing.

```java
public class Main {
    private static CardLayout shellLayout;
    private static JPanel shell;
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowUI);
    }
    
    private static void createAndShowUI() {
        // Initialize SocketClient
        try {
            SocketClient.getInstance().connect();
        } catch (IOException e) {
            showConnectionError();
            return;
        }
        
        // Load session
        SessionManager.getInstance().loadFromFile();
        
        // Create UI
        // ... existing code ...
        
        // Check session validity
        if (SessionManager.getInstance().hasValidSession()) {
            if (SessionManager.getInstance().validateSession()) {
                shellLayout.show(shell, APP);
            } else {
                shellLayout.show(shell, AUTH);
            }
        } else {
            shellLayout.show(shell, AUTH);
        }
    }
    
    private static void showConnectionError() {
        JOptionPane.showMessageDialog(null, 
            "Cannot connect to server. Please ensure the backend is running.",
            "Connection Error", 
            JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}
```

### 6. Backend Protocol Implementation

Frontend harus mengimplementasikan format command yang sesuai dengan backend.

#### Command Formats

**Authentication Commands:**
- `LOGIN|username|password` → `OK|sessionToken` or `ERROR|...`
- `REGISTER|username|password` → `OK` or `ERROR|...`
- `LOGOUT|sessionToken` → `OK`
- `VALIDATE_SESSION|sessionToken` → `OK|username` or `ERROR|SESSION_INVALID`

**Transaction Commands:**
- `ADD|sessionToken|date|description|category|type|amount|accountName|accountType` → `SUMMARY|...`
- `UPDATE|id|sessionToken|date|description|category|type|amount|accountName|accountType` → `SUMMARY|...`
- `DELETE|id|sessionToken` → `OK`
- `GET_ALL|sessionToken` → `DATA_ALL|count|id1|username1|date1|desc1|cat1|type1|amount1|accName1|accType1|...`

**Account Commands:**
- `ADD_ACCOUNT|sessionToken|name|number|balance|type` → `OK|accountId`
- `GET_ACCOUNTS|sessionToken` → `DATA_ACCOUNTS|count|id1|name1|number1|balance1|type1|...`
- `UPDATE_ACCOUNT|sessionToken|id|name|number|balance|type` → `OK`
- `DELETE_ACCOUNT|sessionToken|id` → `OK`

**Category Commands:**
- `ADD_CATEGORY|sessionToken|type|name` → `OK`
- `GET_CATEGORIES|sessionToken` → `DATA_CATEGORIES|count|type1|name1|type2|name2|...`
- `DELETE_CATEGORY|sessionToken|type|name` → `OK`

**Summary Commands:**
- `GET_MONTHLY_SUMMARY|sessionToken|yearMonth` → `MONTHLY_SUMMARY|yearMonth|income|expense|balance`
- `GET_CATEGORY_BREAKDOWN|sessionToken|yearMonth` → `CATEGORY_BREAKDOWN|count|cat1|amount1|cat2|amount2|...`

## Data Models

Frontend akan menggunakan model data yang sama dengan yang sudah ada, namun dengan tambahan parsing dari backend response.

### Response Parsing Examples

#### Parse DATA_ALL Response
```java
private static void parseTransactions(String response) {
    String[] parts = response.split("\\|");
    if (!parts[0].equals("DATA_ALL")) return;
    
    int count = Integer.parseInt(parts[1]);
    transactions.clear();
    
    int index = 2;
    for (int i = 0; i < count; i++) {
        String id = parts[index++];
        String username = parts[index++];
        String date = parts[index++];
        String desc = parts[index++];
        String category = parts[index++];
        String type = parts[index++];
        long amount = Long.parseLong(parts[index++]);
        String accountName = parts[index++];
        String accountType = parts[index++];
        
        transactions.add(new Transaction(id, LocalDate.parse(date), 
                                        type, category, accountName, 
                                        accountType, amount, desc));
    }
}
```

#### Parse DATA_ACCOUNTS Response
```java
private static void parseAccounts(String response) {
    String[] parts = response.split("\\|");
    if (!parts[0].equals("DATA_ACCOUNTS")) return;
    
    int count = Integer.parseInt(parts[1]);
    accounts.clear();
    
    int index = 2;
    for (int i = 0; i < count; i++) {
        String id = parts[index++];
        String name = parts[index++];
        String number = parts[index++];
        long balance = Long.parseLong(parts[index++]);
        String type = parts[index++];
        
        accounts.add(new Account(id, name, number, balance, type));
    }
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

Property 1: Login success creates valid session
*For any* valid username and password, when login is successful, the SessionManager should store a non-null session token that can be used for subsequent authenticated requests
**Validates: Requirements 1.1, 1.2**

Property 2: Socket command formatting consistency
*For any* command sent through SocketClient, the command should be formatted with pipe delimiters and all parameters should be properly escaped
**Validates: Requirements 2.2**

Property 3: Transaction add-retrieve round trip
*For any* transaction added through the UI, when GET_ALL is called, the transaction should appear in the response with all fields preserved exactly
**Validates: Requirements 3.1, 3.4, 12.1, 12.2, 12.4**

Property 4: Account add-retrieve round trip
*For any* account added through the UI, when GET_ACCOUNTS is called, the account should appear in the response with all fields preserved exactly
**Validates: Requirements 4.1, 4.4**

Property 5: Category add-retrieve round trip
*For any* category added through the UI, when GET_CATEGORIES is called, the category should appear in the response
**Validates: Requirements 5.1, 5.3**

Property 6: Session token inclusion in authenticated commands
*For any* authenticated command (except LOGIN and REGISTER), the command should include the session token as the first parameter after the command name
**Validates: Requirements 1.2, 3.1, 4.1, 5.1**

Property 7: Error response handling
*For any* ERROR response from backend, the frontend should parse the error code and description and display an appropriate error message to the user
**Validates: Requirements 7.1**

Property 8: Session expiry handling
*For any* command that returns SESSION_INVALID error, the frontend should clear the session and redirect to login page
**Validates: Requirements 7.2**

Property 9: UI responsiveness during socket operations
*For any* socket operation, the UI should remain responsive and display a loading indicator while the operation is in progress
**Validates: Requirements 9.1, 9.3, 9.4**

Property 10: Store listener notification after backend sync
*For any* successful backend operation that modifies data, all registered store listeners should be notified with the updated snapshot
**Validates: Requirements 8.4**

Property 11: Session persistence across app restarts
*For any* valid session, when the app is closed and reopened, the session token should be loaded from file and validated with the backend
**Validates: Requirements 11.1, 11.2, 11.3, 11.4**

Property 12: Amount type consistency
*For any* transaction amount, when sent to backend and retrieved, the value should remain a long integer without precision loss
**Validates: Requirements 12.1, 12.2**

## Error Handling

### Error Types and Handling Strategy

#### Connection Errors
- **Cannot connect to server**: Show error dialog with retry button
- **Connection lost during operation**: Attempt reconnect with exponential backoff (1s, 2s, 4s, 8s, max 3 attempts)
- **Timeout**: Show timeout message and cancel operation

#### Backend Errors
- **SESSION_INVALID**: Clear session, show "Session expired" message, redirect to login
- **SESSION_REQUIRED**: Clear session, redirect to login
- **INVALID_FORMAT**: Show "Invalid input format" message
- **ACCOUNT_NOT_FOUND**: Show "Account not found" message
- **TRANSACTION_NOT_FOUND**: Show "Transaction not found" message
- **USER_EXISTS**: Show "Username already exists" message (during register)
- **INVALID_CREDENTIALS**: Show "Invalid username or password" message

#### UI Error Display
```java
public class ErrorHandler {
    public static void showError(Component parent, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(parent, message, 
                "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    public static void handleBackendError(Component parent, String response) {
        String[] parts = response.split("\\|");
        if (parts.length >= 3 && parts[0].equals("ERROR")) {
            String errorCode = parts[1];
            String description = parts[2];
            
            if (errorCode.equals("SESSION_INVALID") || 
                errorCode.equals("SESSION_REQUIRED")) {
                SessionManager.getInstance().clearSession();
                // Redirect to login
                showError(parent, "Session expired. Please login again.");
            } else {
                showError(parent, description);
            }
        }
    }
}
```

## Testing Strategy

### Unit Testing
Unit tests will cover:
- SocketClient command formatting and parsing
- SessionManager session persistence and validation
- Store parsing methods for backend responses
- Error handling logic
- Command format generation

### Property-Based Testing
Property-based tests will verify universal properties using a Java PBT library (jqwik). Each test will run a minimum of 100 iterations.

Property-based tests will cover:
- Login/session management (Property 1)
- Command formatting (Property 2)
- Data round trips (Properties 3-5)
- Session token inclusion (Property 6)
- Error handling (Properties 7-8)
- Store notifications (Property 10)
- Session persistence (Property 11)
- Amount type consistency (Property 12)

Each property-based test will be tagged with a comment:
```java
// Feature: socket-client-integration, Property 1: Login success creates valid session
// Validates: Requirements 1.1, 1.2
@Property
void loginSuccessCreatesValidSession(@ForAll String username, @ForAll String password) {
    // test implementation
}
```

### Integration Testing
Integration tests will verify:
- End-to-end flow from UI action to backend and back
- Multiple operations in sequence
- Session management across operations
- Error recovery and reconnection
- UI updates after backend operations

### Manual Testing
Manual testing will cover:
- UI responsiveness during operations
- Loading indicators display correctly
- Error messages are user-friendly
- Session persistence across app restarts
- Reconnection after backend restart

## Implementation Notes

### Threading Strategy

All socket operations will run on background threads to keep UI responsive:

```java
private static void executeAsync(Runnable task) {
    new Thread(() -> {
        try {
            task.run();
        } catch (Exception e) {
            updateUI(() -> {
                ErrorHandler.showError(null, "Operation failed: " + e.getMessage());
            });
        }
    }).start();
}

private static void updateUI(Runnable task) {
    SwingUtilities.invokeLater(task);
}
```

### Loading Indicators

Each page should show loading state during operations:

```java
private void showLoading(boolean show) {
    loadingLabel.setVisible(show);
    saveButton.setEnabled(!show);
}
```

### Reconnection Strategy

SocketClient will implement exponential backoff for reconnection:

```java
private void reconnect() throws IOException {
    int attempts = 0;
    int delay = 1000; // Start with 1 second
    
    while (attempts < 3) {
        try {
            Thread.sleep(delay);
            connect();
            return;
        } catch (IOException | InterruptedException e) {
            attempts++;
            delay *= 2; // Exponential backoff
        }
    }
    throw new IOException("Failed to reconnect after 3 attempts");
}
```

### Session File Format

Session will be saved in simple format:
```
sessionToken
username
```

### Migration from Local Stores

Existing local data in stores will be ignored. On first run with backend:
1. User must login
2. All data will be loaded from backend
3. Local stores will be populated from backend data

### Backward Compatibility

This is a breaking change - the app will no longer work without backend. Consider:
- Adding a "demo mode" flag for testing UI without backend
- Providing clear error messages if backend is not running
- Including backend startup instructions in README

