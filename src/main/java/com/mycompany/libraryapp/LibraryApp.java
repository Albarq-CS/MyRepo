package com.mycompany.libraryapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LibraryApp extends JFrame {
    private static final String DB_URL = "jdbc:ucanaccess://C://Users//alber//Desktop//Root//college//level7//CS342-java//final_project//LibraryApp//LibraryApp//Laibrary.accdb";
    private Connection connection;

    public LibraryApp() {
        // Set up the frame
        setTitle("Library Management System");
        setSize(400, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Connect to the database
        connectDatabase();

        // Set up layout
        setLayout(new BorderLayout());

        // Create a panel to hold the buttons
        JPanel buttonPanel = new JPanel();
        JButton adminButton = new JButton("Admin");
        JButton userButton = new JButton("User");

        // Add action listeners to buttons
        adminButton.addActionListener(e -> openLoginView("Admin"));
        userButton.addActionListener(e -> openLoginView("User"));

        buttonPanel.add(adminButton);
        buttonPanel.add(userButton);

        // Add the button panel to the center of the frame
        add(buttonPanel, BorderLayout.CENTER);
        
    }

    private void connectDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Connected to Microsoft Access database successfully!");
        } catch (SQLException e) {
            System.err.println("Failed to connect to the Access database.");
            e.printStackTrace();
        }
    }

    private void openLoginView(String role) {
        Log_Rig_View loginView = new Log_Rig_View(connection, role);
        loginView.setVisible(true);
        this.dispose();  // Close LibraryApp
    }

public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        LibraryApp app = new LibraryApp();
        app.setVisible(true); // Make the frame visible here
    });
}
}