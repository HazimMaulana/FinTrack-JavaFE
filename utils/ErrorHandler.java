package utils;

import javax.swing.*;
import java.awt.*;

/**
 * ErrorHandler provides centralized error handling and display utilities.
 * Handles backend error responses and provides user-friendly error dialogs.
 */
public class ErrorHandler {
    
    /**
     * Display a simple error dialog with the given message.
     * Runs on the Event Dispatch Thread to ensure thread safety.
     * 
     * @param parent parent component for the dialog (can be null)
     * @param message error message to display
     */
    public static void showError(Component parent, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                parent,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        });
    }
    
    /**
     * Handle backend error responses by parsing ERROR responses and displaying
     * appropriate error messages. Handles session-related errors specially by
     * clearing the session and triggering a redirect to login.
     * 
     * Expected error format: ERROR|errorCode|description
     * 
     * @param parent parent component for the dialog (can be null)
     * @param response raw response from backend
     * @param onSessionInvalid callback to execute when session is invalid (e.g., redirect to login)
     */
    public static void handleBackendError(Component parent, String response, Runnable onSessionInvalid) {
        if (response == null || !response.startsWith("ERROR")) {
            showError(parent, "Unknown error occurred");
            return;
        }
        
        String[] parts = response.split("\\|");
        
        // Minimum format: ERROR|errorCode
        if (parts.length < 2) {
            showError(parent, "Invalid error response from server");
            return;
        }
        
        String errorCode = parts[1];
        String description = parts.length >= 3 ? parts[2] : "An error occurred";
        
        // Handle session-related errors
        if (errorCode.equals("SESSION_INVALID") || errorCode.equals("SESSION_REQUIRED")) {
            // Clear session
            SessionManager.getInstance().clearSession();
            
            // Show error message
            showError(parent, "Session expired. Please login again.");
            
            // Trigger redirect to login if callback provided
            if (onSessionInvalid != null) {
                SwingUtilities.invokeLater(onSessionInvalid);
            }
            return;
        }
        
        // Handle other error codes with descriptive messages
        String userMessage = getDescriptiveMessage(errorCode, description);
        showError(parent, userMessage);
    }
    
    /**
     * Overloaded version of handleBackendError without session invalid callback.
     * Use this when session handling is not needed.
     * 
     * @param parent parent component for the dialog (can be null)
     * @param response raw response from backend
     */
    public static void handleBackendError(Component parent, String response) {
        handleBackendError(parent, response, null);
    }
    
    /**
     * Get a user-friendly descriptive message for the given error code.
     * 
     * @param errorCode error code from backend
     * @param description default description from backend
     * @return user-friendly error message
     */
    private static String getDescriptiveMessage(String errorCode, String description) {
        switch (errorCode) {
            case "USER_EXISTS":
                return "Username already exists. Please choose a different username.";
            
            case "INVALID_CREDENTIALS":
                return "Invalid username or password. Please try again.";
            
            case "ACCOUNT_NOT_FOUND":
                return "Account not found. It may have been deleted.";
            
            case "TRANSACTION_NOT_FOUND":
                return "Transaction not found. It may have been deleted.";
            
            case "CATEGORY_NOT_FOUND":
                return "Category not found. It may have been deleted.";
            
            case "INVALID_FORMAT":
                return "Invalid input format. Please check your data and try again.";
            
            case "INVALID_AMOUNT":
                return "Invalid amount. Please enter a valid number.";
            
            case "INVALID_DATE":
                return "Invalid date format. Please use YYYY-MM-DD format.";
            
            case "DATABASE_ERROR":
                return "Database error occurred. Please try again later.";
            
            case "UNKNOWN_COMMAND":
                return "Unknown command sent to server. Please contact support.";
            
            case "MISSING_PARAMETER":
                return "Missing required information. Please fill in all fields.";
            
            case "UPDATE_NOT_FOUND":
                return "Item not found for update. It may have been deleted.";
            
            case "DELETE_NOT_FOUND":
                return "Item not found for deletion. It may have been already deleted.";
            
            default:
                // Use the description from backend if available
                return description != null && !description.isEmpty() 
                    ? description 
                    : "An error occurred: " + errorCode;
        }
    }
    
    /**
     * Show a connection error dialog with retry option.
     * 
     * @param parent parent component for the dialog (can be null)
     * @param errorMessage detailed error message
     * @param onRetry callback to execute when user clicks retry
     */
    public static void showConnectionError(Component parent, String errorMessage, Runnable onRetry) {
        SwingUtilities.invokeLater(() -> {
            String message = "Cannot connect to server. Please ensure the backend is running.\n\n" +
                           "Error: " + errorMessage;
            
            int result = JOptionPane.showConfirmDialog(
                parent,
                message,
                "Connection Error",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.ERROR_MESSAGE
            );
            
            if (result == JOptionPane.OK_OPTION && onRetry != null) {
                onRetry.run();
            }
        });
    }
    
    /**
     * Show a timeout error dialog.
     * 
     * @param parent parent component for the dialog (can be null)
     */
    public static void showTimeoutError(Component parent) {
        showError(parent, "Request timeout. The server took too long to respond. Please try again.");
    }
}
