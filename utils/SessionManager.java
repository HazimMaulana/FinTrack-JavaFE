package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * SessionManager manages user session token and persistence.
 * Implements singleton pattern for centralized session management.
 * Handles session validation with backend server.
 */
public class SessionManager {
    private static final String SESSION_FILE = ".fintrack_session";
    
    private static SessionManager instance;
    
    private String sessionToken;
    private String username;
    
    private SessionManager() {
        this.sessionToken = null;
        this.username = null;
    }
    
    /**
     * Get singleton instance of SessionManager.
     * @return SessionManager instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Set session token and username.
     * @param token session token from backend
     * @param username logged in username
     */
    public synchronized void setSession(String token, String username) {
        this.sessionToken = token;
        this.username = username;
    }
    
    /**
     * Get current session token.
     * @return session token or null if not set
     */
    public synchronized String getSessionToken() {
        return sessionToken;
    }
    
    /**
     * Get current username.
     * @return username or null if not set
     */
    public synchronized String getUsername() {
        return username;
    }
    
    /**
     * Check if a valid session exists.
     * @return true if session token is not null and not empty
     */
    public synchronized boolean hasValidSession() {
        return sessionToken != null && !sessionToken.trim().isEmpty();
    }
    
    /**
     * Clear session token and username.
     */
    public synchronized void clearSession() {
        this.sessionToken = null;
        this.username = null;
    }
    
    /**
     * Save session to file.
     * Writes token and username to SESSION_FILE.
     */
    public synchronized void saveToFile() {
        if (!hasValidSession()) {
            return; // Nothing to save
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SESSION_FILE))) {
            writer.write(sessionToken);
            writer.newLine();
            writer.write(username != null ? username : "");
            writer.newLine();
        } catch (IOException e) {
            // Silently fail - session will not persist
            System.err.println("Warning: Failed to save session to file: " + e.getMessage());
        }
    }
    
    /**
     * Load session from file.
     * Reads token and username from SESSION_FILE.
     * Handles file not found gracefully (no error thrown).
     */
    public synchronized void loadFromFile() {
        File file = new File(SESSION_FILE);
        
        if (!file.exists()) {
            // File doesn't exist - no session to load
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String token = reader.readLine();
            String user = reader.readLine();
            
            if (token != null && !token.trim().isEmpty()) {
                this.sessionToken = token.trim();
                this.username = (user != null && !user.trim().isEmpty()) ? user.trim() : null;
            }
        } catch (IOException e) {
            // Silently fail - no session loaded
            System.err.println("Warning: Failed to load session from file: " + e.getMessage());
        }
    }
    
    /**
     * Validate session with backend server.
     * Sends VALIDATE_SESSION command to check if token is still valid.
     * @return true if session is valid, false otherwise
     */
    public synchronized boolean validateSession() {
        if (!hasValidSession()) {
            return false;
        }
        
        try {
            SocketClient client = SocketClient.getInstance();
            String command = client.formatCommand("VALIDATE_SESSION", sessionToken);
            String response = client.sendCommand(command);
            
            // Check if response is OK
            if (response != null && response.startsWith("OK")) {
                return true;
            }
            
            // Check for SESSION_INVALID or ERROR
            if (client.isErrorResponse(response)) {
                String[] parts = client.parseResponse(response);
                if (parts.length >= 2 && parts[1].equals("SESSION_INVALID")) {
                    clearSession();
                }
                return false;
            }
            
            return false;
        } catch (IOException e) {
            // Connection error - consider session invalid
            System.err.println("Warning: Failed to validate session: " + e.getMessage());
            return false;
        }
    }
}
