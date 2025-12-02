package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * SocketClient manages TCP connection to backend server.
 * Implements singleton pattern for centralized socket communication.
 * Thread-safe with synchronized methods.
 */
public class SocketClient {
    private static final String HOST = "localhost";
    private static final int PORT = 3000;
    private static final int TIMEOUT = 10000; // 10 seconds
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    private static final int INITIAL_BACKOFF_MS = 1000; // 1 second

    private static SocketClient instance;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected;

    // Subscription channel for server-pushed events (separate socket to avoid read
    // conflicts)
    private Socket subSocket;
    private PrintWriter subOut;
    private BufferedReader subIn;
    private Thread subThread;
    private volatile boolean subRunning = false;

    private SocketClient() {
        this.connected = false;
    }

    /**
     * Get singleton instance of SocketClient.
     * 
     * @return SocketClient instance
     */
    public static synchronized SocketClient getInstance() {
        if (instance == null) {
            instance = new SocketClient();
        }
        return instance;
    }

    /**
     * Establish TCP connection to backend server.
     * 
     * @throws IOException if connection fails
     */
    public synchronized void connect() throws IOException {
        if (connected && socket != null && !socket.isClosed()) {
            return; // Already connected
        }

        try {
            socket = new Socket(HOST, PORT);
            socket.setSoTimeout(TIMEOUT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
        } catch (IOException e) {
            connected = false;
            throw new IOException("Failed to connect to server at " + HOST + ":" + PORT, e);
        }
    }

    /**
     * Close socket connection gracefully.
     */
    public synchronized void disconnect() {
        connected = false;

        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception e) {
            // Ignore
        }

        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception e) {
            // Ignore
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            // Ignore
        }

        out = null;
        in = null;
        socket = null;

        // Stop subscription channel if running
        stopSubscription();
    }

    /**
     * Check if socket is currently connected.
     * 
     * @return true if connected, false otherwise
     */
    public synchronized boolean isConnected() {
        return connected && socket != null && !socket.isClosed() && socket.isConnected();
    }

    /**
     * Attempt to reconnect with exponential backoff.
     * Tries up to MAX_RECONNECT_ATTEMPTS times with delays: 1s, 2s, 4s.
     * 
     * @throws IOException if all reconnection attempts fail
     */
    private synchronized void reconnect() throws IOException {
        disconnect(); // Clean up existing connection

        int delay = INITIAL_BACKOFF_MS;
        IOException lastException = null;

        for (int attempt = 1; attempt <= MAX_RECONNECT_ATTEMPTS; attempt++) {
            try {
                Thread.sleep(delay);
                connect();
                return; // Success
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Reconnection interrupted", e);
            } catch (IOException e) {
                lastException = e;
                delay *= 2; // Exponential backoff
            }
        }

        throw new IOException("Failed to reconnect after " + MAX_RECONNECT_ATTEMPTS + " attempts", lastException);
    }

    /**
     * Send command to backend and return response.
     * Automatically attempts reconnection if connection is lost.
     * 
     * @param command formatted command string
     * @return response from backend
     * @throws IOException if communication fails
     */
    public synchronized String sendCommand(String command) throws IOException {
        if (!isConnected()) {
            reconnect();
        }

        try {
            out.println(command);
            if (out.checkError()) {
                throw new IOException("Failed to send command");
            }

            return readResponse();
        } catch (IOException e) {
            // Try to reconnect once
            try {
                reconnect();
                out.println(command);
                if (out.checkError()) {
                    throw new IOException("Failed to send command after reconnect");
                }
                return readResponse();
            } catch (IOException reconnectException) {
                throw new IOException("Communication failed and reconnection failed", reconnectException);
            }
        }
    }

    /**
     * Read response from socket with timeout.
     * 
     * @return response string
     * @throws IOException if read fails or times out
     */
    private String readResponse() throws IOException {
        try {
            String response = in.readLine();
            if (response == null) {
                throw new IOException("Server closed connection");
            }
            return response;
        } catch (SocketTimeoutException e) {
            throw new IOException("Request timeout after " + TIMEOUT + "ms", e);
        }
    }

    /**
     * Start background subscription to server events for current session.
     * Opens a dedicated socket and listens for EVENT messages.
     */
    public synchronized void startSubscription() throws IOException {
        if (subRunning)
            return;

        String sessionToken = SessionManager.getInstance().getSessionToken();
        if (sessionToken == null || sessionToken.isEmpty()) {
            throw new IOException("No session token available for subscription");
        }

        try {
            subSocket = new Socket(HOST, PORT);
            // Keep subscription responsive but not too aggressive
            subSocket.setSoTimeout(0); // blocking read
            subOut = new PrintWriter(subSocket.getOutputStream(), true);
            subIn = new BufferedReader(new InputStreamReader(subSocket.getInputStream()));
            // Send SUBSCRIBE command
            subOut.println(formatCommand("SUBSCRIBE", sessionToken));
            subRunning = true;
        } catch (IOException e) {
            subRunning = false;
            cleanupSub();
            throw e;
        }

        subThread = new Thread(() -> {
            try {
                String line;
                while (subRunning && (line = subIn.readLine()) != null) {
                    handleEventLine(line);
                }
            } catch (IOException ex) {
                // Subscription ended or failed; attempt silent stop
            } finally {
                cleanupSub();
                subRunning = false;
            }
        }, "FinTrack-EventListener");
        subThread.setDaemon(true);
        subThread.start();
    }

    /**
     * Stop background subscription and release resources.
     */
    public synchronized void stopSubscription() {
        subRunning = false;
        if (subThread != null) {
            try {
                subThread.interrupt();
            } catch (Exception ignored) {
            }
        }
        cleanupSub();
    }

    private void cleanupSub() {
        try {
            if (subOut != null)
                subOut.close();
        } catch (Exception ignored) {
        }
        try {
            if (subIn != null)
                subIn.close();
        } catch (Exception ignored) {
        }
        try {
            if (subSocket != null && !subSocket.isClosed())
                subSocket.close();
        } catch (Exception ignored) {
        }
        subOut = null;
        subIn = null;
        subSocket = null;
        subThread = null;
    }

    // Dispatch server-sent events
    private void handleEventLine(String line) {
        if (line == null || line.isEmpty())
            return;
        if (line.startsWith("EVENT|")) {
            String[] parts = parseResponse(line);
            String eventType = parts.length >= 2 ? parts[1] : "";
            switch (eventType) {
                case "DATA_CHANGED" -> {
                    // Refresh all stores in background
                    TransactionStore.loadFromBackend(() -> {
                    }, err -> System.err.println("Txn refresh error: " + err));
                    AccountStore.loadFromBackend(() -> {
                    }, err -> System.err.println("Acct refresh error: " + err));
                    CategoryStore.loadFromBackend(() -> {
                    }, err -> System.err.println("Cat refresh error: " + err));
                }
                default -> {
                    // Unknown event types can be logged
                    System.out.println("Unhandled event: " + line);
                }
            }
        }
    }

    /**
     * Format command with pipe delimiters.
     * 
     * @param parts command parts to join
     * @return pipe-delimited command string
     */
    public String formatCommand(String... parts) {
        return String.join("|", parts);
    }

    /**
     * Parse response by splitting on pipe delimiter.
     * 
     * @param response response string to parse
     * @return array of response parts
     */
    public String[] parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            return new String[0];
        }
        return response.split("\\|");
    }

    /**
     * Check if response indicates an error.
     * 
     * @param response response string
     * @return true if response starts with ERROR
     */
    public boolean isErrorResponse(String response) {
        return response != null && response.startsWith("ERROR");
    }

    /**
     * Extract error message from error response.
     * 
     * @param response error response string
     * @return error message or full response if format is unexpected
     */
    public String getErrorMessage(String response) {
        if (!isErrorResponse(response)) {
            return response;
        }

        String[] parts = parseResponse(response);
        if (parts.length >= 3) {
            // Format: ERROR|CODE|DESCRIPTION
            return parts[2];
        } else if (parts.length == 2) {
            // Format: ERROR|DESCRIPTION
            return parts[1];
        } else {
            return response;
        }
    }
}
