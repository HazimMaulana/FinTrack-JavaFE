# Implementation Plan

- [x] 1. Create SocketClient component





- [x] 1.1 Implement SocketClient class with connection management


  - Create SocketClient.java in utils/ directory
  - Implement singleton pattern with getInstance()
  - Implement connect() method to establish TCP connection to localhost:3000
  - Implement disconnect() method to close socket gracefully
  - Implement isConnected() method to check connection status
  - Add reconnect() method with exponential backoff (1s, 2s, 4s, max 3 attempts)
  - Use synchronized methods for thread safety
  - _Requirements: 2.1, 2.4, 2.5, 2.6_

- [x] 1.2 Implement command sending and response parsing


  - Implement sendCommand(String command) method
  - Implement formatCommand(String... parts) to create pipe-delimited commands
  - Implement readResponse() to read from socket with 10-second timeout
  - Implement parseResponse(String response) to split by pipe delimiter
  - Implement isErrorResponse(String response) to check for ERROR prefix
  - Implement getErrorMessage(String response) to extract error details
  - _Requirements: 2.2, 2.3, 7.1, 7.5_

- [ ]* 1.3 Write property test for command formatting
  - **Property 2: Socket command formatting consistency**
  - **Validates: Requirements 2.2**

- [ ]* 1.4 Write property test for response parsing
  - **Property 3: Response parsing correctness**
  - **Validates: Requirements 2.3**

- [x] 2. Create SessionManager component







- [x] 2.1 Implement SessionManager class

  - Create SessionManager.java in utils/ directory
  - Implement singleton pattern with getInstance()
  - Add fields: sessionToken (String), username (String)
  - Implement setSession(String token, String username)
  - Implement getSessionToken() and getUsername()
  - Implement hasValidSession() to check if token exists
  - Implement clearSession() to reset token and username
  - _Requirements: 1.2, 1.5_

- [x] 2.2 Implement session persistence

  - Define SESSION_FILE constant as ".fintrack_session"
  - Implement saveToFile() to write token and username to file
  - Implement loadFromFile() to read from file on startup
  - Handle file not found gracefully (return without error)
  - _Requirements: 11.1, 11.2, 11.6_


- [ ] 2.3 Implement session validation
  - Implement validateSession() method
  - Send VALIDATE_SESSION command with token to backend
  - Return true if backend responds with OK
  - Return false if backend responds with ERROR or SESSION_INVALID
  - _Requirements: 11.3, 11.4, 11.5_

- [ ]* 2.4 Write property test for session persistence
  - **Property 11: Session persistence across app restarts**
  - **Validates: Requirements 11.1, 11.2, 11.3, 11.4**



- [x] 3. Create AuthPage component





- [x] 3.1 Implement AuthPage UI layout

  - Create AuthPage.java in pages/ directory (create pages/ if not exists)
  - Extend JPanel with BorderLayout
  - Add usernameField (JTextField)
  - Add passwordField (JPasswordField)
  - Add loginButton (JButton)
  - Add registerButton or toggle link
  - Add errorLabel (JLabel) for displaying errors
  - Add isRegisterMode boolean flag
  - Style with rounded panels and buttons consistent with existing UI
  - _Requirements: 10.1, 10.2_


- [ ] 3.2 Implement login functionality
  - Implement handleLogin() method
  - Get username and password from fields
  - Validate fields are not empty
  - Send LOGIN command via SocketClient in background thread
  - Parse response for session token
  - On success: save session via SessionManager, call onLoginSuccess callback
  - On error: display error message in errorLabel
  - _Requirements: 1.1, 1.2, 1.3_


- [ ] 3.3 Implement register functionality
  - Implement handleRegister() method
  - Get username and password from fields
  - Validate fields are not empty
  - Send REGISTER command via SocketClient in background thread
  - On success: automatically call handleLogin()
  - On error: display error message in errorLabel

  - _Requirements: 1.4, 10.3, 10.4_

- [ ] 3.4 Implement logout functionality
  - Add logout method to be called from TopBar
  - Send LOGOUT command with session token
  - Clear session via SessionManager

  - Call callback to return to AuthPage
  - _Requirements: 1.5_

- [ ] 3.5 Add mode toggle between login and register
  - Implement toggleMode() method
  - Update form title and button text based on mode
  - Implement resetFields() to clear form
  - _Requirements: 10.2_

- [ ]* 3.6 Write property test for login success
  - **Property 1: Login success creates valid session**
  - **Validates: Requirements 1.1, 1.2**
-

- [x] 4. Update TransactionStore to use SocketClient





- [x] 4.1 Refactor TransactionStore for backend communication


  - Add SocketClient instance field
  - Add SessionManager instance field
  - Remove seed data (will load from backend)
  - Keep transactions list and listeners as-is
  - _Requirements: 8.1_

- [x] 4.2 Implement async addTransaction method


  - Update addTransaction signature to include callbacks: Consumer<String> onSuccess, Consumer<String> onError
  - Execute in background thread using executeAsync()
  - Format ADD command with session token and all transaction fields
  - Send command via SocketClient
  - On success: parse response, update local list, notify listeners, call onSuccess with transaction ID
  - On error: call onError with error message
  - _Requirements: 3.1, 9.1, 9.2, 12.1, 12.3, 12.4_

- [x] 4.3 Implement async updateTransaction method


  - Update signature to include callbacks: Runnable onSuccess, Consumer<String> onError
  - Execute in background thread
  - Format UPDATE command with session token and transaction data
  - Send command via SocketClient
  - On success: update local list, notify listeners, call onSuccess
  - On error: call onError with error message
  - _Requirements: 3.2, 9.1, 9.2_

- [x] 4.4 Implement async removeTransaction method


  - Update signature to include callbacks: Runnable onSuccess, Consumer<String> onError
  - Execute in background thread
  - Format DELETE command with session token and transaction ID
  - Send command via SocketClient
  - On success: remove from local list, notify listeners, call onSuccess
  - On error: call onError with error message
  - _Requirements: 3.3, 9.1, 9.2_

- [x] 4.5 Implement loadFromBackend method


  - Add method signature: loadFromBackend(Runnable onSuccess, Consumer<String> onError)
  - Execute in background thread
  - Format GET_ALL command with session token
  - Send command via SocketClient
  - Parse DATA_ALL response (format: DATA_ALL|count|id1|username1|date1|desc1|cat1|type1|amount1|accName1|accType1|...)
  - Update transactions list with parsed data
  - Notify all listeners with new snapshot
  - Call onSuccess callback
  - On error: call onError with error message
  - _Requirements: 3.4, 3.5, 8.4, 12.2, 12.5_

- [x] 4.6 Add helper methods for threading


  - Implement executeAsync(Runnable task) to run on background thread
  - Implement updateUI(Runnable task) to run on EDT using SwingUtilities.invokeLater
  - _Requirements: 9.1, 9.2_

- [ ]* 4.7 Write property test for transaction add-retrieve round trip
  - **Property 3: Transaction add-retrieve round trip**
  - **Validates: Requirements 3.1, 3.4, 12.1, 12.2, 12.4**

- [ ]* 4.8 Write property test for store listener notification
  - **Property 10: Store listener notification after backend sync**
  - **Validates: Requirements 8.4**

- [ ]* 4.9 Write property test for amount type consistency
  - **Property 12: Amount type consistency**
  - **Validates: Requirements 12.1, 12.2**

- [x] 5. Update AccountStore to use SocketClient






- [x] 5.1 Refactor AccountStore for backend communication


  - Add SocketClient instance field
  - Add SessionManager instance field
  - Remove seed data
  - Keep accounts list and listeners as-is
  - _Requirements: 8.2_

- [x] 5.2 Implement async addAccount method


  - Update signature to include callbacks: Consumer<String> onSuccess, Consumer<String> onError
  - Execute in background thread
  - Format ADD_ACCOUNT command with session token, name, number, balance, type
  - Send command via SocketClient
  - Parse response for account ID
  - On success: add to local list, notify listeners, call onSuccess with account ID
  - On error: call onError with error message
  - _Requirements: 4.1, 9.1, 9.2_

- [x] 5.3 Implement async updateAccount method


  - Update signature to include id and callbacks: Runnable onSuccess, Consumer<String> onError
  - Execute in background thread
  - Format UPDATE_ACCOUNT command with session token and account data
  - Send command via SocketClient
  - On success: update local list, notify listeners, call onSuccess
  - On error: call onError with error message
  - _Requirements: 4.2, 9.1, 9.2_

- [x] 5.4 Implement async removeAccount method


  - Update signature to include callbacks: Runnable onSuccess, Consumer<String> onError
  - Execute in background thread
  - Format DELETE_ACCOUNT command with session token and account ID
  - Send command via SocketClient
  - On success: remove from local list, notify listeners, call onSuccess
  - On error: call onError with error message
  - _Requirements: 4.3, 9.1, 9.2_

- [x] 5.5 Implement loadFromBackend method


  - Add method signature: loadFromBackend(Runnable onSuccess, Consumer<String> onError)
  - Execute in background thread
  - Format GET_ACCOUNTS command with session token
  - Send command via SocketClient
  - Parse DATA_ACCOUNTS response (format: DATA_ACCOUNTS|count|id1|name1|number1|balance1|type1|...)
  - Update accounts list with parsed data
  - Notify all listeners with new snapshot
  - Call onSuccess callback
  - On error: call onError with error message
  - _Requirements: 4.4, 4.5, 8.4_

- [ ]* 5.6 Write property test for account add-retrieve round trip
  - **Property 4: Account add-retrieve round trip**
  - **Validates: Requirements 4.1, 4.4**
-

- [x] 6. Update CategoryStore to use SocketClient




- [x] 6.1 Refactor CategoryStore for backend communication


  - Add SocketClient instance field
  - Add SessionManager instance field
  - Remove static seed data
  - Keep category lists and listeners as-is
  - _Requirements: 8.3_

- [x] 6.2 Implement async addCategory method


  - Update signature to include callbacks: Runnable onSuccess, Consumer<String> onError
  - Execute in background thread
  - Format ADD_CATEGORY command with session token, type, name
  - Send command via SocketClient
  - On success: add to local list, notify listeners, call onSuccess
  - On error: call onError with error message
  - _Requirements: 5.1, 9.1, 9.2_

- [x] 6.3 Implement async removeCategory method


  - Update signature to include callbacks: Runnable onSuccess, Consumer<String> onError
  - Execute in background thread
  - Format DELETE_CATEGORY command with session token, type, name
  - Send command via SocketClient
  - On success: remove from local list, notify listeners, call onSuccess
  - On error: call onError with error message
  - _Requirements: 5.2, 9.1, 9.2_

- [x] 6.4 Implement loadFromBackend method


  - Add method signature: loadFromBackend(Runnable onSuccess, Consumer<String> onError)
  - Execute in background thread
  - Format GET_CATEGORIES command with session token
  - Send command via SocketClient
  - Parse DATA_CATEGORIES response (format: DATA_CATEGORIES|count|type1|name1|type2|name2|...)
  - Separate categories by type into expenseCategories and incomeCategories
  - Notify all listeners with new snapshot
  - Call onSuccess callback
  - On error: call onError with error message
  - _Requirements: 5.3, 5.4, 8.4_

- [ ]* 6.5 Write property test for category add-retrieve round trip
  - **Property 5: Category add-retrieve round trip**
  - **Validates: Requirements 5.1, 5.3**


- [x] 7. Update UI pages to use async Store methods




- [x] 7.1 Update TransaksiPage for async operations


  - Update handleSave() to use new addTransaction/updateTransaction signatures
  - Add loading indicator (JLabel or progress bar)
  - Show loading indicator before operation, hide after completion
  - Pass onSuccess callback to refresh UI
  - Pass onError callback to display error dialog
  - Call loadFromBackend() when page is initialized
  - _Requirements: 3.1, 3.2, 3.4, 3.6, 9.3, 9.4_

- [x] 7.2 Update AkunWalletPage for async operations


  - Update add/update/delete account handlers to use new signatures
  - Add loading indicator
  - Show/hide loading indicator during operations
  - Pass callbacks for success and error handling
  - Call loadFromBackend() when page is initialized
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 9.3, 9.4_

- [x] 7.3 Update KategoriPage for async operations


  - Update add/delete category handlers to use new signatures
  - Add loading indicator
  - Show/hide loading indicator during operations
  - Pass callbacks for success and error handling
  - Call loadFromBackend() when page is initialized
  - _Requirements: 5.1, 5.2, 5.3, 9.3, 9.4_

- [x] 7.4 Update HomePage for summary data


  - Add method to load monthly summary for last 6 months
  - Call GET_MONTHLY_SUMMARY for each month via SocketClient
  - Parse MONTHLY_SUMMARY responses
  - Update chart with trend data
  - Add method to load category breakdown
  - Call GET_CATEGORY_BREAKDOWN via SocketClient
  - Parse CATEGORY_BREAKDOWN response
  - Update pie chart with category data
  - Add loading indicators for charts
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 9.3, 9.4_

- [x] 8. Update Main.java for authentication flow





- [x] 8.1 Initialize SocketClient on startup


  - Call SocketClient.getInstance().connect() in createAndShowUI()
  - Wrap in try-catch to handle connection errors
  - Show error dialog if connection fails
  - Exit application if cannot connect to backend
  - _Requirements: 2.1, 7.3_

- [x] 8.2 Implement session validation on startup


  - Call SessionManager.getInstance().loadFromFile()
  - Check if session exists with hasValidSession()
  - If session exists, call validateSession()
  - If valid, show APP shell directly
  - If invalid or no session, show AUTH page
  - _Requirements: 11.2, 11.3, 11.4, 11.5_

- [x] 8.3 Wire AuthPage to Main navigation


  - Create AuthPage instance with onLoginSuccess callback
  - Callback should switch to APP shell
  - Add AuthPage to CardLayout shell
  - Ensure logout from TopBar returns to AUTH page
  - _Requirements: 1.1, 1.5_
-

- [ ] 9. Implement error handling utilities


- [x] 9.1 Create ErrorHandler utility class



  - Create ErrorHandler.java in utils/ directory
  - Implement showError(Component parent, String message) to display error dialog
  - Implement handleBackendError(Component parent, String response) to parse ERROR responses
  - Handle SESSION_INVALID and SESSION_REQUIRED by clearing session and redirecting to login
  - Handle other error codes by showing descriptive messages
  - _Requirements: 7.1, 7.2, 7.3, 7.4_

- [ ]* 9.2 Write property test for error response handling
  - **Property 7: Error response handling**
  - **Validates: Requirements 7.1**

- [x] 10. Checkpoint - Test end-to-end flow



  - Ensure backend server is running
  - Test login flow with valid credentials
  - Test register flow with new user
  - Test adding, updating, deleting transactions
  - Test adding, updating, deleting accounts
  - Test adding, deleting categories
  - Test session persistence (close and reopen app)
  - Test logout flow
  - Test error handling (invalid credentials, session expiry, connection loss)
  - Verify UI remains responsive during operations
  - Verify loading indicators appear and disappear correctly






- [ ] 11. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
