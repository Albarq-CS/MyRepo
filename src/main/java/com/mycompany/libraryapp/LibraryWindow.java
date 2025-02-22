package com.mycompany.libraryapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;

public class LibraryWindow extends JFrame {
    private Connection connection;
    private int userId;
    private String username;
    private JTable booksTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public LibraryWindow(Connection connection, int userId, String username) {
        this.connection = connection;
        this.userId = userId;
        this.username = username;

        initializeUI();
        loadBooks(""); // Load all books initially
        setVisible(true);
    }

    private void initializeUI() {
        setTitle("Library Books");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel northPanel = new JPanel(new BorderLayout());
        JToolBar toolBar = createToolBar();
        northPanel.add(toolBar, BorderLayout.NORTH);
        northPanel.add(createSearchPanel(), BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Book ID", "Title", "Author", "Genre", "Available Copies"}, 0);
        booksTable = new JTable(tableModel);
        add(new JScrollPane(booksTable), BorderLayout.CENTER);

        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        logoutPanel.add(logoutButton);
        add(logoutPanel, BorderLayout.SOUTH);
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.add(createButton("My Information", e -> showUserInformation()));
        toolBar.add(createButton("Edit Information", e -> editUserInformation()));
        toolBar.add(createButton("Borrowed Books", e -> showBorrowedBooks()));
        toolBar.add(createButton("Borrow", e -> borrowSelectedBook()));
        toolBar.add(new JLabel("Logged in as: " + username));
        return toolBar;
    }

    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        return button;
    }

private JPanel createSearchPanel() {
    JPanel searchPanel = new JPanel();
    JLabel searchLabel = new JLabel("Search By Title, Author, or Genre: "); // Updated label
    searchField = new JTextField(15);
    searchField.addActionListener(e -> loadBooks(searchField.getText()));
    JButton searchButton = new JButton("Search");
    searchButton.addActionListener(e -> loadBooks(searchField.getText()));
    searchPanel.add(searchLabel);
    searchPanel.add(searchField);
    searchPanel.add(searchButton);
    return searchPanel;
}

  private void loadBooks(String searchQuery) {
    tableModel.setRowCount(0); // Clear existing rows
    try {
        String query = "SELECT * FROM Books WHERE title LIKE ? OR author LIKE ? OR genre LIKE ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, "%" + searchQuery + "%");
        stmt.setString(2, "%" + searchQuery + "%");
        stmt.setString(3, "%" + searchQuery + "%"); // Add genre search
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            tableModel.addRow(new Object[]{
                rs.getInt("book_id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("genre"),
                rs.getInt("available_copies")
            });
        }
        rs.close();
        stmt.close();
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error loading books.", "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private boolean borrowBook(int userId, int bookId, String dueDate) {
        try {
            // Check if the book is available
            String checkAvailabilityQuery = "SELECT available_copies FROM Books WHERE book_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkAvailabilityQuery);
            checkStmt.setInt(1, bookId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt("available_copies") > 0) {
                // Get the current date for transaction_date
                LocalDate transactionDate = LocalDate.now();

                // Insert the transaction
                String insertQuery = "INSERT INTO Transactions (user_id, book_id, transaction_date, due_date, transaction_type) VALUES (?, ?, ?, ?, 'borrow')";
                PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, bookId);
                insertStmt.setDate(3, Date.valueOf(transactionDate));  // Set transaction date
                insertStmt.setString(4, dueDate);
                insertStmt.executeUpdate();

                // Update available copies in the Books table
                String updateQuery = "UPDATE Books SET available_copies = available_copies - 1 WHERE book_id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                updateStmt.setInt(1, bookId);
                updateStmt.executeUpdate();

                insertStmt.close();
                updateStmt.close();
                checkStmt.close();
                return true; // Borrowing was successful
            } else {
                JOptionPane.showMessageDialog(null, "The book is not available for borrowing.");
                checkStmt.close();
                return false; // Book not available
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // An error occurred
        }
    }

    private void borrowSelectedBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow != -1) {
            int bookId = (int) tableModel.getValueAt(selectedRow, 0);
            String dueDate = JOptionPane.showInputDialog(this, "Enter due date (YYYY-MM-DD):");

            if (dueDate != null && !dueDate.isEmpty()) {
                boolean success = borrowBook(userId, bookId, dueDate);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Book borrowed successfully!");
                    loadBooks(""); // Reload all books after borrowing
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to borrow book. Please try again.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a book to borrow!");
        }
    }

    private void showUserInformation() {
        try {
            String query = "SELECT first_name, last_name, email, membership_date FROM Users WHERE user_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            JFrame userInfoFrame = new JFrame("My Information");
            userInfoFrame.setSize(500, 150);
            userInfoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            userInfoFrame.setLocationRelativeTo(this);

            String[] columnNames = {"Username", "First Name", "Last Name", "Email", "Membership Date"};
            DefaultTableModel userTableModel = new DefaultTableModel(columnNames, 0);
            JTable userTable = new JTable(userTableModel);
            userTable.setFillsViewportHeight(true);

            if (rs.next()) {
                userTableModel.addRow(new Object[]{
                    username,
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("membership_date")
                });
            } else {
                userTableModel.addRow(new Object[]{"Error: User information not found."});
            }

            userInfoFrame.add(new JScrollPane(userTable), BorderLayout.CENTER);
            userInfoFrame.setVisible(true);
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void editUserInformation() {
        JDialog editDialog = new JDialog(this, "Edit Information", true);
        editDialog.setSize(400, 300);
        editDialog.setLayout(new GridLayout(6, 2));
        editDialog.setLocationRelativeTo(this);

        JTextField usernameField = new JTextField(username);
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        try {
            String query = "SELECT first_name, last_name, email FROM Users WHERE user_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                firstNameField.setText(rs.getString("first_name"));
                lastNameField.setText(rs.getString("last_name"));
                emailField.setText(rs.getString("email"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        editDialog.add(new JLabel("Username:"));
        editDialog.add(usernameField);
        editDialog.add(new JLabel("First Name:"));
        editDialog.add(firstNameField);
        editDialog.add(new JLabel("Last Name:"));
        editDialog.add(lastNameField);
        editDialog.add(new JLabel("Email:"));
        editDialog.add(emailField);
        editDialog.add(new JLabel("Password:"));
        editDialog.add(passwordField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            String newUsername = usernameField.getText();
            String newFirstName = firstNameField.getText();
            String newLastName = lastNameField.getText();
            String newEmail = emailField.getText();
            String newPassword = new String(passwordField.getPassword());

            try {
                String updateQuery = "UPDATE Users SET username = ?, first_name = ?, last_name = ?, email = ?, password = ? WHERE user_id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                updateStmt.setString(1, newUsername);
                updateStmt.setString(2, newFirstName);
                updateStmt.setString(3, newLastName);
                updateStmt.setString(4, newEmail);
                updateStmt.setString(5, newPassword);
                updateStmt.setInt(6, userId);
                updateStmt.executeUpdate();
                updateStmt.close();
                JOptionPane.showMessageDialog(editDialog, "Information updated successfully!");
                editDialog.dispose(); // Close the dialog
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(editDialog, "Error updating information.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        editDialog.add(saveButton);
        editDialog.setVisible(true); // Show the dialog
    }

    private void showBorrowedBooks() {
        try {
            String query = "SELECT t.transaction_id, b.title, b.author, b.genre, b.publication_date, t.transaction_date, t.due_date " +
                           "FROM Transactions t " +
                           "JOIN Books b ON t.book_id = b.book_id " +
                           "WHERE t.user_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            JFrame borrowedBooksFrame = new JFrame("Borrowed Books");
            borrowedBooksFrame.setSize(700, 400);
            borrowedBooksFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            borrowedBooksFrame.setLocationRelativeTo(this);

            String[] columnNames = {"Transaction ID", "Title", "Author", "Genre", "Publication Date", "Transaction Date", "Due Date"};
            DefaultTableModel borrowedTableModel = new DefaultTableModel(columnNames, 0);
            JTable borrowedTable = new JTable(borrowedTableModel);
            JScrollPane scrollPane = new JScrollPane(borrowedTable);

            while (rs.next()) {
                borrowedTableModel.addRow(new Object[]{
                    rs.getInt("transaction_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("genre"),
                    rs.getString("publication_date"),
                    rs.getDate("transaction_date"), // Display transaction date
                    rs.getString("due_date")
                });
            }

            borrowedBooksFrame.add(scrollPane, BorderLayout.CENTER);
            borrowedBooksFrame.setVisible(true);
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

   private void logout() {
    int confirmed = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
    if (confirmed == JOptionPane.YES_OPTION) {
        this.dispose(); // Close the current AdminWindow
        // Open the LibraryApp (or a specific login view if needed)
        LibraryApp libraryApp = new LibraryApp(); // Create a new instance of LibraryApp
        libraryApp.setVisible(true); // Show the LibraryApp
    }
}
}
