package com.mycompany.libraryapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AdminWindow extends JFrame {
    private Connection connection;
    private JTextField userSearchField;
    private JTextField transactionSearchField;
    private JTable usersTable;
    private JTable transactionsTable;
    private DefaultTableModel overdueTableModel;

 public AdminWindow(Connection connection) {
        this.connection = connection;

        // Create the background panel with your image path
        BackgroundPanel backgroundPanel = new BackgroundPanel("C:/Users/alber/Desktop/Root/college/level7/CS342-java/final_project/LibraryApp/LibraryApp/Background.jpg");
        backgroundPanel.setLayout(new BorderLayout());
        
        setTitle("Admin Panel");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create toolbar with actions
        JToolBar toolBar = new JToolBar("Admin Actions");
        
        // Existing buttons
        JButton viewUsersButton = new JButton("View All Users");
        viewUsersButton.addActionListener(e -> displayUserInfo());
        toolBar.add(viewUsersButton);

        JButton returnBookButton = new JButton("Return Book");
        returnBookButton.addActionListener(e -> showReturnBookWindow());
        toolBar.add(returnBookButton);

        JButton bookInventoryButton = new JButton("Book Inventory");
        bookInventoryButton.addActionListener(e -> showBookInventoryWindow());
        toolBar.add(bookInventoryButton);

        JButton notificationsButton = new JButton("Check Due Notifications");
        notificationsButton.addActionListener(e -> notifyOverdueBooks());
        toolBar.add(notificationsButton);
        
        // New button to display books
        JButton displayBooksButton = new JButton("Display Books");
        displayBooksButton.addActionListener(e -> displayBooks());
        toolBar.add(displayBooksButton);

        backgroundPanel.add(toolBar, BorderLayout.NORTH);

        // Logout button
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        logoutPanel.add(logoutButton);
        backgroundPanel.add(logoutPanel, BorderLayout.SOUTH);

        setContentPane(backgroundPanel);
    }

    private void notifyOverdueBooks() {
        try {
            String query = "SELECT t.transaction_id, u.first_name, u.last_name, b.title, t.due_date " +
                           "FROM Transactions t " +
                           "INNER JOIN Users u ON t.user_id = u.user_id " +
                           "INNER JOIN Books b ON t.book_id = b.book_id " +
                           "WHERE t.due_date < CURRENT_DATE";

            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            overdueTableModel = new DefaultTableModel(new Object[]{"Transaction ID", "User", "Book Title", "Due Date"}, 0);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            boolean hasOverdueBooks = false;

            while (rs.next()) {
                int transactionId = rs.getInt("transaction_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String bookTitle = rs.getString("title");
                String dueDateStr = rs.getString("due_date");

                try {
                    LocalDate dueDate = LocalDate.parse(dueDateStr, formatter);
                    overdueTableModel.addRow(new Object[]{transactionId, firstName + " " + lastName, bookTitle, dueDate});
                    hasOverdueBooks = true;
                } catch (Exception ex) {
                    overdueTableModel.addRow(new Object[]{transactionId, firstName + " " + lastName, bookTitle, "INVALID DATE"});
                }
            }

            if (!hasOverdueBooks) {
                JOptionPane.showMessageDialog(this, "No overdue books found.", "Notifications", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JTable overdueTable = new JTable(overdueTableModel);
                JScrollPane scrollPane = new JScrollPane(overdueTable);
                JOptionPane.showMessageDialog(this, scrollPane, "Overdue Notifications", JOptionPane.INFORMATION_MESSAGE);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching overdue notifications: " + e.getMessage());
        }
    }
private void displayBooks() {
    // Create a new JFrame for displaying books
    JFrame booksFrame = new JFrame("Library Books");
    booksFrame.setSize(800, 600);
    booksFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    booksFrame.setLocationRelativeTo(this);

    // Create a JPanel for search functionality
    JPanel searchPanel = new JPanel();
    JLabel searchLabel = new JLabel("Search By Title, Author, or Genre: ");
    JTextField searchField = new JTextField(15);
    JButton searchButton = new JButton("Search");

    // Set up action listener for the search button
    searchButton.addActionListener(e -> loadBooks(searchField.getText(), booksFrame));

    // Add components to the search panel
    searchPanel.add(searchLabel);
    searchPanel.add(searchField);
    searchPanel.add(searchButton);

    // Create table model and JTable
    DefaultTableModel model = new DefaultTableModel(new String[]{"Title", "Author", "Genre", "Publication Date", "Available Copies"}, 0);
    JTable booksTable = new JTable(model);
    JScrollPane scrollPane = new JScrollPane(booksTable);

    // Set layout and add components to the frame
    booksFrame.setLayout(new BorderLayout());
    booksFrame.add(searchPanel, BorderLayout.NORTH);
    booksFrame.add(scrollPane, BorderLayout.CENTER);

    // Load all books initially
    loadBooks("", booksFrame);

    // Add close button
    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(e -> booksFrame.dispose());
    booksFrame.add(closeButton, BorderLayout.SOUTH);

    // Make the frame visible
    booksFrame.setVisible(true);
}
private void loadBooks(String searchQuery, JFrame booksFrame) {
    // Clear existing rows in the table model
    DefaultTableModel model = (DefaultTableModel) ((JTable)((JScrollPane)booksFrame.getContentPane().getComponent(1)).getViewport().getView()).getModel();
    model.setRowCount(0);

    try {
        String query = "SELECT title, author, genre, publication_date, available_copies FROM Books WHERE title LIKE ? OR author LIKE ? OR genre LIKE ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, "%" + searchQuery + "%");
        stmt.setString(2, "%" + searchQuery + "%");
        stmt.setString(3, "%" + searchQuery + "%");
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("genre"),
                rs.getString("publication_date"),
                rs.getInt("available_copies")
            });
        }

        rs.close();
        stmt.close();
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(booksFrame, "Error loading books.", "Error", JOptionPane.ERROR_MESSAGE);
    }
}
    private void showBookInventoryWindow() {
        String[] options = {"Add Book", "Update Book", "Remove Book"};
        String selection = (String) JOptionPane.showInputDialog(
                this,
                "Select an action:",
                "Book Inventory",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (selection != null) {
            switch (selection) {
                case "Add Book":
                    showAddBookWindow();
                    break;
                case "Update Book":
                    showUpdateBookWindow();
                    break;
                case "Remove Book":
                    showRemoveBookWindow();
                    break;
            }
        }
    }

private void showAddBookWindow() {
    JFrame addBookFrame = new JFrame("Add Book");
    addBookFrame.setSize(400, 300);
    addBookFrame.setLocationRelativeTo(this);

    // Create input fields
    JTextField titleField = new JTextField(20);
    JTextField authorField = new JTextField(20);
    JTextField genreField = new JTextField(20);
    JTextField publicationDateField = new JTextField(10); // Format: YYYY-MM-DD
    JTextField availableCopiesField = new JTextField(5);

    JButton addButton = new JButton("Add Book");
    addButton.addActionListener(e -> {
        try {
            String query = "INSERT INTO Books (title, author, genre, publication_date, available_copies) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, titleField.getText());
            stmt.setString(2, authorField.getText());
            stmt.setString(3, genreField.getText());
            stmt.setDate(4, Date.valueOf(publicationDateField.getText()));
            stmt.setInt(5, Integer.parseInt(availableCopiesField.getText()));
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(addBookFrame, "Book added successfully!");
            clearFields(titleField, authorField, genreField, publicationDateField, availableCopiesField);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(addBookFrame, "Error adding book: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(addBookFrame, "Invalid input: " + ex.getMessage());
        }
    });

    JPanel inputPanel = new JPanel(new GridLayout(6, 2));
    inputPanel.add(new JLabel("Title:"));
    inputPanel.add(titleField);
    inputPanel.add(new JLabel("Author:"));
    inputPanel.add(authorField);
    inputPanel.add(new JLabel("Genre:"));
    inputPanel.add(genreField);
    inputPanel.add(new JLabel("Publication Date (YYYY-MM-DD):"));
    inputPanel.add(publicationDateField);
    inputPanel.add(new JLabel("Available Copies:"));
    inputPanel.add(availableCopiesField);
    inputPanel.add(addButton);

    addBookFrame.add(inputPanel);
    addBookFrame.setVisible(true);
}

private void showUpdateBookWindow() {
    JFrame updateBookFrame = new JFrame("Update Book");
    updateBookFrame.setSize(400, 300);
    updateBookFrame.setLocationRelativeTo(this);

    // Create input fields
    JTextField titleField = new JTextField(20);
    JTextField authorField = new JTextField(20);
    JTextField genreField = new JTextField(20);
    JTextField publicationDateField = new JTextField(10); // Format: YYYY-MM-DD
    JTextField availableCopiesField = new JTextField(5);

    JButton updateButton = new JButton("Update Book");
    updateButton.addActionListener(e -> {
        try {
            String query = "UPDATE Books SET author = ?, genre = ?, publication_date = ?, available_copies = ? WHERE title = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, authorField.getText());
            stmt.setString(2, genreField.getText());
            stmt.setDate(3, Date.valueOf(publicationDateField.getText()));
            stmt.setInt(4, Integer.parseInt(availableCopiesField.getText()));
            stmt.setString(5, titleField.getText());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(updateBookFrame, "Book updated successfully!");
            } else {
                JOptionPane.showMessageDialog(updateBookFrame, "No book found with that title.");
            }
            clearFields(titleField, authorField, genreField, publicationDateField, availableCopiesField);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(updateBookFrame, "Error updating book: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(updateBookFrame, "Invalid input: " + ex.getMessage());
        }
    });

    JPanel inputPanel = new JPanel(new GridLayout(6, 2));
    inputPanel.add(new JLabel("Title:"));
    inputPanel.add(titleField);
    inputPanel.add(new JLabel("New Author:"));
    inputPanel.add(authorField);
    inputPanel.add(new JLabel("New Genre:"));
    inputPanel.add(genreField);
    inputPanel.add(new JLabel("New Publication Date (YYYY-MM-DD):"));
    inputPanel.add(publicationDateField);
    inputPanel.add(new JLabel("New Available Copies:"));
    inputPanel.add(availableCopiesField);
    inputPanel.add(updateButton);

    updateBookFrame.add(inputPanel);
    updateBookFrame.setVisible(true);
}

private void showRemoveBookWindow() {
    JFrame removeBookFrame = new JFrame("Remove Book");
    removeBookFrame.setSize(400, 200);
    removeBookFrame.setLocationRelativeTo(this);

    // Create input fields
    JTextField titleField = new JTextField(20);
    JButton removeButton = new JButton("Remove Book");
    removeButton.addActionListener(e -> {
        try {
            String query = "DELETE FROM Books WHERE title = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, titleField.getText());

            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(removeBookFrame, "Book removed successfully!");
            } else {
                JOptionPane.showMessageDialog(removeBookFrame, "No book found with that title.");
            }
            titleField.setText(""); // Clear the field after operation
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(removeBookFrame, "Error removing book: " + ex.getMessage());
        }
    });

    JPanel inputPanel = new JPanel(new GridLayout(2, 2));
    inputPanel.add(new JLabel("Title:"));
    inputPanel.add(titleField);
    inputPanel.add(removeButton);

    removeBookFrame.add(inputPanel);
    removeBookFrame.setVisible(true);
}

private void clearFields(JTextField... fields) {
    for (JTextField field : fields) {
        field.setText("");
    }
}

    private void displayUserInfo() {
        JFrame userFrame = new JFrame("User Information");
        userFrame.setSize(600, 400);
        userFrame.setLocationRelativeTo(this);

        userSearchField = new JTextField(20);
        JButton searchUserButton = new JButton("Search");
        searchUserButton.addActionListener(e -> searchUsers());

        usersTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(usersTable);
        userFrame.add(scrollPane, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Search User:"));
        searchPanel.add(userSearchField);
        searchPanel.add(searchUserButton);
        userFrame.add(searchPanel, BorderLayout.NORTH);

        loadUserData(""); // Load all users initially

        userFrame.setVisible(true);
    }

    private void loadUserData(String searchCriteria) {
        try {
            String query = "SELECT user_id, username, email, first_name, last_name, membership_date FROM Users WHERE "
                    + "(username LIKE ? OR email LIKE ? OR first_name LIKE ? OR last_name LIKE ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            String searchPattern = "%" + searchCriteria + "%"; // For partial matches
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel model = new DefaultTableModel(new Object[]{"User ID", "Username", "Email", "First Name", "Last Name", "Membership Date"}, 0);
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                Date membershipDate = rs.getDate("membership_date");

                model.addRow(new Object[]{userId, username, email, firstName, lastName, membershipDate});
            }
            usersTable.setModel(model);

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading user data: " + e.getMessage());
        }
    }

    private void searchUsers() {
        String searchCriteria = userSearchField.getText().trim();
        loadUserData(searchCriteria); // Load users based on search criteria
    }

    private void showReturnBookWindow() {
        JFrame returnBookFrame = new JFrame("Return Book");
        returnBookFrame.setSize(600, 400);
        returnBookFrame.setLocationRelativeTo(this);

        transactionSearchField = new JTextField(20);
        JButton searchTransactionButton = new JButton("Search");
        searchTransactionButton.addActionListener(e -> searchTransactions());

        transactionsTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        returnBookFrame.add(scrollPane, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Search by User Info:"));
        searchPanel.add(transactionSearchField);
        searchPanel.add(searchTransactionButton);
        returnBookFrame.add(searchPanel, BorderLayout.NORTH);

        JButton returnButton = new JButton("Return Selected Book");
        returnButton.addActionListener(e -> returnSelectedBook());
        returnBookFrame.add(returnButton, BorderLayout.SOUTH);

        loadTransactionData(""); // Load all transactions initially

        returnBookFrame.setVisible(true);
    }

    private void loadTransactionData(String searchCriteria) {
        try {
            String query = "SELECT t.transaction_id, t.user_id, t.due_date, u.first_name, u.last_name, b.title, t.transaction_type " +
                    "FROM Transactions t " +
                    "INNER JOIN Users u ON t.user_id = u.user_id " +
                    "INNER JOIN Books b ON t.book_id = b.book_id " +
                    "WHERE (u.first_name LIKE ? OR u.last_name LIKE ? OR u.username LIKE ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            String searchPattern = "%" + searchCriteria + "%"; // For partial matches
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel model = new DefaultTableModel(new Object[]{"Transaction ID", "User ID", "Due Date", "First Name", "Last Name", "Book Title", "Transaction Type"}, 0);
            while (rs.next()) {
                int transactionId = rs.getInt("transaction_id");
                int userId = rs.getInt("user_id");
                Date dueDate = rs.getDate("due_date");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String bookTitle = rs.getString("title");
                String transactionType = rs.getString("transaction_type");

                model.addRow(new Object[]{transactionId, userId, dueDate, firstName, lastName, bookTitle, transactionType});
            }
            transactionsTable.setModel(model);

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading transaction data: " + e.getMessage());
        }
    }

    private void searchTransactions() {
        String searchCriteria = transactionSearchField.getText().trim();
        loadTransactionData(searchCriteria); // Load transactions based on search criteria
    }

    private void returnSelectedBook() {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow != -1) {
            int transactionId = (int) transactionsTable.getValueAt(selectedRow, 0); // Get transaction ID from the selected row
            handleReturnBook(transactionId);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a transaction to return.");
        }
    }

    private void handleReturnBook(int transactionId) {
        try {
            // First, get the book_id associated with the transaction
            String bookIdQuery = "SELECT book_id FROM Transactions WHERE transaction_id = ?";
            PreparedStatement bookIdStmt = connection.prepareStatement(bookIdQuery);
            bookIdStmt.setInt(1, transactionId);
            ResultSet rs = bookIdStmt.executeQuery();

            int bookId = -1;
            if (rs.next()) {
                bookId = rs.getInt("book_id");
            }

            rs.close();
            bookIdStmt.close();

            // If a valid book_id is found, proceed to delete the transaction and update available_copies
            if (bookId != -1) {
                // Delete the transaction
                String deleteQuery = "DELETE FROM Transactions WHERE transaction_id = ?";
                PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery);
                deleteStmt.setInt(1, transactionId);
                deleteStmt.executeUpdate();

                // Update the available_copies in the Books table
                String updateQuery = "UPDATE Books SET available_copies = available_copies + 1 WHERE book_id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                updateStmt.setInt(1, bookId);
                updateStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Book returned successfully!");
                loadTransactionData(""); // Reload transactions after returning a book
            } else {
                JOptionPane.showMessageDialog(this, "Transaction not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error returning the book: " + e.getMessage());
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