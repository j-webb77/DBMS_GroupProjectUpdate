import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

public class MembershipManager extends BaseGymManager {
    private JTextField memberNameField;
    private JTextField membershipTypeField;
    private JTextField startDateField;
    private JTextField endDateField;
    private JTextField priceField;
    private JTextField statusField;
    private final MembershipService membershipService;
    private final MembershipTableModel tableModel;
    private JTable membershipTable;
    private final Gym gym;

    public MembershipManager(Frame owner, Gym gym, User currentUser) throws SQLException {
        super(owner, "Manage Memberships", true);
        this.gym = gym;
        this.currentGym = gym;
        this.membershipService = new MembershipService();
        this.tableModel = new MembershipTableModel();
        
        initializeComponents();
        setupTableSelection();
        loadMemberships();
        this.currentUser = currentUser;
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(600, 400);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        memberNameField = new JTextField(20);
        membershipTypeField = new JTextField(20);
        startDateField = new JTextField(20);
        endDateField = new JTextField(20);
        priceField = new JTextField(20);
        statusField = new JTextField(20);

        inputPanel.add(new JLabel("Member Name:"));
        inputPanel.add(memberNameField);
        inputPanel.add(new JLabel("Membership Type:"));
        inputPanel.add(membershipTypeField);
        inputPanel.add(new JLabel("Start Date:"));
        inputPanel.add(startDateField);
        inputPanel.add(new JLabel("End Date:"));
        inputPanel.add(endDateField);
        inputPanel.add(new JLabel("Price:"));
        inputPanel.add(priceField);
        inputPanel.add(new JLabel("Status:"));
        inputPanel.add(statusField);

        // Table
        membershipTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(membershipTable);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        closeButton = new JButton("Close");

        addButton.addActionListener(e -> {
            try {
                addMembership();
            } catch (SQLException e1) {
            }
        });
        updateButton.addActionListener(e -> updateMembership());
        deleteButton.addActionListener(e -> deleteMembership());
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);

        // Add components
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupTableSelection() {
        membershipTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = membershipTable.getSelectedRow();
                if (selectedRow >= 0) {
                    Membership selected = tableModel.getMembershipAt(selectedRow);
                    memberNameField.setText(selected.getMemberName());
                    membershipTypeField.setText(selected.getMembershipType());
                    startDateField.setText(selected.getStartDate().toString());
                    endDateField.setText(selected.getEndDate().toString());
                    priceField.setText(String.valueOf(selected.getPrice()));
                    statusField.setText(selected.getStatus());
                }
            }
        });
    }

    private void loadMemberships() {
        try {
            tableModel.setMemberships(membershipService.getMembershipsByGym(gym.getGymId()));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading memberships: " + e.getMessage());
        }
    }

    
    @Override
    public void validatePermissions() {
        boolean canEdit = checkPermissions();
        
        // Set button states
        addButton.setEnabled(canEdit);
        updateButton.setEnabled(canEdit);
        deleteButton.setEnabled(canEdit);

        // Make fields read-only for users without edit permissions
        memberNameField.setEditable(canEdit);
        membershipTypeField.setEditable(canEdit);
        startDateField.setEditable(canEdit);
        endDateField.setEditable(canEdit);
        priceField.setEditable(canEdit);
        statusField.setEditable(canEdit);
    }

                private boolean checkPermissions() {
                String userRole = currentUser.getRole();
                if (userRole.equals("ADMIN")) {
                    return true;
                } else if (userRole.equals("MNGR")) {
                    return currentUser.getManagedGymId() != null && 
                           currentUser.getManagedGymId().equals(gym.getGymId());
                }
                return false;
            }



    private void addMembership() throws SQLException {
        if (!checkPermissions()) {
            showError("You don't have permission to add memberships.");
            return;
        }

        try {
            // First validate all inputs
            validateInputs();

            // If validation passes, create the membership object
            Membership membership = new Membership(
                memberNameField.getText().trim(),
                membershipTypeField.getText().trim(),
                LocalDate.parse(startDateField.getText().trim()),
                LocalDate.parse(endDateField.getText().trim()),
                Double.parseDouble(priceField.getText().trim()),
                statusField.getText().trim()
            );

            // Add the membership
            membershipService.addMembership(membership, gym.getGymId());
            
            // Refresh the display
            loadMemberships();
            clearFields();
            
            JOptionPane.showMessageDialog(
                this,
                "Membership added successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );
            
        } catch (IllegalArgumentException e) {
            showError("Invalid input: " + e.getMessage());
        } catch (SQLException e) { // For debugging
            showError("Database error: " + e.getMessage());
        }
    }

    private void updateMembership() {
        try {
            if (!checkPermissions()) {
                JOptionPane.showMessageDialog(this, "You don't have permission to update memberships.");
                return;
            }

            int selectedRow = membershipTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a membership to update");
                return;
            }

            validateInputs();
            LocalDate startDate = LocalDate.parse(startDateField.getText());
            LocalDate endDate = LocalDate.parse(endDateField.getText());
            
            // Add date validation
            if (endDate.isBefore(startDate)) {
                throw new IllegalArgumentException("End date cannot be before start date");
            }

            Membership membership = tableModel.getMembershipAt(selectedRow).copy();
            membership.setMemberName(memberNameField.getText().trim());
            membership.setMembershipType(membershipTypeField.getText().trim());
            membership.setStartDate(startDate);
            membership.setEndDate(endDate);
            membership.setPrice(Double.parseDouble(priceField.getText().trim()));
            membership.setStatus(statusField.getText().trim());
            
            membershipService.updateMembership(membership);
            loadMemberships();
            clearFields();
            JOptionPane.showMessageDialog(this, "Membership updated successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Database error while updating membership: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, 
                "Invalid input: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteMembership() {
        int selectedRow = membershipTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a membership to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this membership?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int membershipId = tableModel.getMembershipAt(selectedRow).getMembershipId();
                membershipService.deleteMembership(membershipId);
                loadMemberships();
                clearFields();
                JOptionPane.showMessageDialog(this, "Membership deleted successfully!");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting membership: " + e.getMessage());
            }
        }
    }

    private void validateInputs() {
        try {
            // Validate required fields
            String memberName = memberNameField.getText();
            String membershipType = membershipTypeField.getText();
            String startDateText = startDateField.getText();
            String endDateText = endDateField.getText();
            String priceText = priceField.getText();
            String status = statusField.getText();

            // Check for null or empty fields
            if (memberName == null || memberName.trim().isEmpty()) {
                throw new IllegalArgumentException("Member name is required");
            }
            if (membershipType == null || membershipType.trim().isEmpty()) {
                throw new IllegalArgumentException("Membership type is required");
            }
            if (startDateText == null || startDateText.trim().isEmpty()) {
                throw new IllegalArgumentException("Start date is required");
            }
            if (endDateText == null || endDateText.trim().isEmpty()) {
                throw new IllegalArgumentException("End date is required");
            }
            if (priceText == null || priceText.trim().isEmpty()) {
                throw new IllegalArgumentException("Price is required");
            }
            if (status == null || status.trim().isEmpty()) {
                throw new IllegalArgumentException("Status is required");
            }

            // Validate price format
            try {
                double price = Double.parseDouble(priceText.trim());
                if (price < 0) {
                    throw new IllegalArgumentException("Price cannot be negative");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid price format. Please enter a valid number");
            }

            // Validate dates
            try {
                LocalDate startDate = LocalDate.parse(startDateText.trim());
                LocalDate endDate = LocalDate.parse(endDateText.trim());
                
                if (endDate.isBefore(startDate)) {
                    throw new IllegalArgumentException("End date cannot be before start date");
                }
                
                if (startDate.isBefore(LocalDate.now().minusYears(1))) {
                    throw new IllegalArgumentException("Start date cannot be more than 1 year in the past");
                }
                
                if (endDate.isAfter(LocalDate.now().plusYears(5))) {
                    throw new IllegalArgumentException("End date cannot be more than 5 years in the future");
                }
                
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format. Please use YYYY-MM-DD");
            }
        } catch (IllegalArgumentException e) {
            // Wrap any unexpected exceptions
            throw new IllegalArgumentException("Validation error: " + e.getMessage());
        }
    }

    private void clearFields() {
        memberNameField.setText("");
        membershipTypeField.setText("");
        startDateField.setText("");
        endDateField.setText("");
        priceField.setText("");
        statusField.setText("");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
} 