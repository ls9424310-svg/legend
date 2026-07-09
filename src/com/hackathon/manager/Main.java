package com.hackathon.manager;

import javax.swing.SwingUtilities;
import com.hackathon.manager.db.DatabaseHelper;
import com.hackathon.manager.ui.LoginFrame;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize SQLite tables and sample seed data
        DatabaseHelper.initializeDatabase();

        // 2. Open Login Frame on Event Dispatcher Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system default look and feel, but keep custom theme layout
                javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Could not set System Look and Feel: " + e.getMessage());
            }
            
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
