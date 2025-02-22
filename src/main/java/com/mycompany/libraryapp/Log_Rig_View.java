package com.mycompany.libraryapp;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class Log_Rig_View extends JFrame {
    private Connection connection;
    private String role;

    public Log_Rig_View(Connection connection, String role) {
        this.connection = connection;
        this.role = role;

        setTitle(role + " Login");
        setSize(400, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(4, 2));

        loginPanel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField();
        loginPanel.add(usernameField);

        loginPanel.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField();
        loginPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin(usernameField.getText(), new String(passwordField.getPassword())));
        loginPanel.add(loginButton);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> openRegisterView());
        loginPanel.add(registerButton);

        add(loginPanel, BorderLayout.CENTER);
    }

 private void handleLogin(String username, String password) {
    if (role.equals("Admin")) {
        if (loginAdmin(username, password)) {
            JOptionPane.showMessageDialog(this, "Admin Login Successful!");
            AdminWindow adminWindow = new AdminWindow(connection);
            adminWindow.setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Admin username or password!");
        }
    } else {
        if (loginUser(username, password)) {
            JOptionPane.showMessageDialog(this, "User Login Successful!");

            // Get userId and open Library Window
            int userId = getUserId(username);
            if (userId != -1) {
                LibraryWindow libraryWindow = new LibraryWindow(connection, userId, username); // Pass username
                libraryWindow.setVisible(true);
                this.dispose();  // Close the login window
            } else {
                JOptionPane.showMessageDialog(this, "Could not retrieve user ID.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid User username or password!");
        }
    }
}

    private void openRegisterView() {
        RegisterView registerView = new RegisterView(connection, role);
        registerView.setVisible(true);
        this.dispose();
    }

    private boolean loginAdmin(String username, String password) {
        try {
            String query = "SELECT * FROM Admins WHERE username = ? AND password = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean loginUser(String username, String password) {
        try {
            String query = "SELECT * FROM Users WHERE username = ? AND password = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            return rs.next();  // Returns true if user found
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int getUserId(String username) {
        try {
            String query = "SELECT user_id FROM Users WHERE username = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("user_id"); // Return the actual user ID
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 or some indicator if the user is not found
    }

    // Nested RegisterView class
   public static class RegisterView extends JFrame {
    private Connection connection;
    private String role;

    public RegisterView(Connection connection, String role) {
        this.connection = connection;
        this.role = role;

        setTitle(role + " Registration");
        setSize(400, 300);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel registerPanel = new JPanel();
        registerPanel.setLayout(new GridLayout(6, 2));

        registerPanel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField();
        registerPanel.add(usernameField);

        registerPanel.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField();
        registerPanel.add(passwordField);

        registerPanel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField();
        registerPanel.add(emailField);

        registerPanel.add(new JLabel("First Name:"));
        JTextField firstNameField = new JTextField();
        registerPanel.add(firstNameField);

        registerPanel.add(new JLabel("Last Name:"));
        JTextField lastNameField = new JTextField();
        registerPanel.add(lastNameField);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> {
            // Validate email format
            String email = emailField.getText();
            if (!isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Invalid email format. Must be a @gmail.com address.");
                return;
            }

            // Get the current date for membership_date
            Date membershipDate = new Date(System.currentTimeMillis());
    
            handleRegistration(
                usernameField.getText(),
                new String(passwordField.getPassword()),
                email,
                firstNameField.getText(),
                lastNameField.getText(),
                membershipDate
            );
        });
        registerPanel.add(registerButton);

        add(registerPanel);
    }

    private boolean isValidEmail(String email) {
        // Regex to check if email ends with @gmail.com
        String regex = "^[a-zA-Z0-9._%+-]+@gmail\\.com$";
        return Pattern.matches(regex, email);
    }

    private void handleRegistration(String username, String password, String email, String firstName, String lastName, Date membershipDate) {
        try {
            String query;
            if (role.equals("Admin")) {
                query = "INSERT INTO Admins (username, password, email, first_name, last_name, membership_date) VALUES (?, ?, ?, ?, ?, ?)";
            } else {
                query = "INSERT INTO Users (username, password, email, first_name, last_name, membership_date) VALUES (?, ?, ?, ?, ?, ?)";
            }

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password); // Consider hashing the password
            stmt.setString(3, email);
            stmt.setString(4, firstName);
            stmt.setString(5, lastName);
            stmt.setDate(6, new java.sql.Date(membershipDate.getTime())); // Set the membership date
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Registration Successful!");

            // Reset the fields after successful registration
           // usernameField.setText("");
           // passwordField.setText("");
           // emailField.setText("");
           // firstNameField.setText("");
           // lastNameField.setText("");

            // Return to login view
            this.setVisible(false);
            new Log_Rig_View(connection, role).setVisible(true);
            this.dispose();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Registration failed: " + e.getMessage());
        }
    }
}}