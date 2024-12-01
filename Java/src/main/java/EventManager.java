import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public final class EventManager extends BaseGymManager {
    private final Gym gym;
    private JTable eventTable;
    private JTextField eventNameField;
    private JDChooser dateChooser;
    private JTextField descriptionField;
    private final EventService eventService;
    private JTextField sponsorField;


    public static class JDChooser extends JTextField {
        private SimpleDateFormat dateFormat;
        
        public JDChooser(String datePattern) {
            super();
            initialize();
        }
        
        private void initialize() {
            dateFormat = new SimpleDateFormat("MM-dd-yyyy");
            setToolTipText("Format: MM-dd-yyyy");
        }
        
        public Date getDate() {
            try {
                String text = getText().trim();
                if (text.isEmpty()) return null;
                return dateFormat.parse(text);
            } catch (java.text.ParseException e) {
                JOptionPane.showMessageDialog(null, 
                    "Please enter date in format MM-DD-YYYY", 
                    "Invalid Date", 
                    JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        
        public void setDate(Date date) {
            if (date == null) {
                setText("");
            } else {
                setText(dateFormat.format(date));
            }
        }
    }

    public EventManager(Frame owner, Gym gym) throws SQLException {
        super(owner, "Manage Events", true);
        this.gym = gym;
        this.currentGym = gym;
        this.eventService = new EventService();
        this.sponsorField = new JTextField(60);
        
        initializeComponents();
        loadEvents();
        validatePermissions();
    }
                    
                    
                        @Override
                        public void setFont(Font font) {
                            super.setFont(font);
                        }
            
            
                        private void initializeComponents() {
                setLayout(new BorderLayout(10, 10));
                setSize(600, 400);
        
                // Input Panel with same dimensions as MembershipManager
                JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
                
                // Initialize components with same size as MembershipManager
                eventNameField = new JTextField(60);  // Match MembershipManager width
                dateChooser = new JDChooser("mm-dd-yyyy");
                descriptionField = new JTextField(60);
                sponsorField = new JTextField(60);  // Match MembershipManager width
                
                // Set uniform dimensions to match MembershipManager
                Dimension fieldDimension = new Dimension(400, 20);  // Adjusted to match MembershipManager
                eventNameField.setPreferredSize(fieldDimension);
                dateChooser.setPreferredSize(fieldDimension);
                descriptionField.setPreferredSize(fieldDimension);
                sponsorField.setPreferredSize(fieldDimension);
                
                // Add components to input panel
                inputPanel.add(new JLabel("Name:"));
                inputPanel.add(eventNameField);
                inputPanel.add(new JLabel("Date:"));
                inputPanel.add(dateChooser);
                inputPanel.add(new JLabel("Description:"));
                inputPanel.add(descriptionField);
                inputPanel.add(new JLabel("Sponsor:"));
                inputPanel.add(sponsorField);

                // Table with same dimensions as MembershipManager
                eventTable = new JTable(new EventTableModel());
                eventTable.getTableHeader().setReorderingAllowed(false);
                eventTable.getTableHeader().setResizingAllowed(false);
                eventTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                JScrollPane scrollPane = new JScrollPane(eventTable);
                scrollPane.setPreferredSize(new Dimension(580, 250));  // Match MembershipManager table size

                // Use buttons from base class
                addButton = new JButton("Add");
                updateButton = new JButton("Update");
                deleteButton = new JButton("Delete");
                closeButton = new JButton("Close");

                closeButton.addActionListener(e -> dispose());

                // Button Panel
                JPanel buttonPanel = new JPanel();
                addButton.addActionListener(e -> {
                    try {
                        addEvent();
                    } catch (SQLException e1) {
                        JOptionPane.showMessageDialog(this, "Error adding event: " + e1.getMessage());
                    }
                });
                updateButton.addActionListener(e -> {
                    try {
                        updateEvent();
                    } catch (SQLException e1) {
                        JOptionPane.showMessageDialog(this, "Error updating event: " + e1.getMessage());
                    }
                });
                deleteButton.addActionListener(e -> {
                    try {
                        deleteEvent();
                    } catch (SQLException e1) {
                        JOptionPane.showMessageDialog(this, "Error deleting event: " + e1.getMessage());
                    }
                });

                buttonPanel.add(addButton);
                buttonPanel.add(updateButton);
                buttonPanel.add(deleteButton);
                buttonPanel.add(closeButton);

                // Add components
                add(inputPanel, BorderLayout.NORTH);
                add(scrollPane, BorderLayout.CENTER);
                add(buttonPanel, BorderLayout.SOUTH);

                // Add this at the end of initializeComponents method
                eventTable.getSelectionModel().addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        int selectedRow = eventTable.getSelectedRow();
                        if (selectedRow >= 0) {
                            Event selected = ((EventTableModel)eventTable.getModel()).getEventAt(selectedRow);
                            populateFields(selected);
                        }
                    }
                });
            }
        
            private void populateFields(Event event) {
                eventNameField.setText(event.getName());
                dateChooser.setDate(Date.from(event.getDateTime().atZone(ZoneId.systemDefault()).toInstant()));
                descriptionField.setText(event.getDescription());
                sponsorField.setText(event.getSponsor());
            }
        
            @SuppressWarnings("unused")
            private void addButton(JPanel buttonPanel, String text, ActionListener listener) {
                JButton button = new JButton(text);
                button.addActionListener(listener);
                buttonPanel.add(button);
            }
        
            private void loadEvents() throws SQLException {
                EventTableModel model = (EventTableModel) eventTable.getModel();
                model.setEvents(eventService.getEventsForGym(gym.getGymId()));
            }
        
            private void addEvent() throws SQLException {
                if (!checkPermissions()) {
                    JOptionPane.showMessageDialog(this, "You don't have permission to add events.");
                    return;
                }

                try {
                    // Parse the date correctly
                    Date date = dateChooser.getDate();
                    if (date == null) {
                        JOptionPane.showMessageDialog(this, "Please enter a valid date");
                        return;
                    }

                    LocalDateTime dateTime = date.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                    Event event = new Event(0, 
                        eventNameField.getText(),
                        dateTime,
                        descriptionField.getText(),
                        gym.getGymId(),
                        sponsorField.getText()
                    );

                    eventService.addEvent(event);
                    loadEvents();
                    clearInputFields();
                } catch (HeadlessException | SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                }
            }
        
            private boolean checkPermissions() {
                if (currentUser == null) return false;
                
                String userRole = currentUser.getRole();
                if (userRole.equals("ADMIN")) {
                    return true;
                } else if (userRole.equals("MNGR")) {
                    return currentUser.getManagedGymId() != null && 
                           currentUser.getManagedGymId().equals(gym.getGymId());
                }
                return false;
            }
        
                
                            private void updateEvent() throws SQLException {
                int selectedRow = eventTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(this, "Please select an event to update", "No Selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }
        
                EventTableModel model = (EventTableModel) eventTable.getModel();
                Event event = model.getEventAt(selectedRow);
                event.setName(eventNameField.getText());
                event.setDateTime(dateChooser.getDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
                event.setDescription(descriptionField.getText());
                event.setSponsor(sponsorField.getText());
        
                eventService.updateEvent(event);
                loadEvents();
                clearInputFields();
            }
        
            private void deleteEvent() throws SQLException {
                int selectedRow = eventTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(this, "Please select an event to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }
        
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Are you sure you want to delete this event?", 
                    "Confirm Delete", 
                    JOptionPane.YES_NO_OPTION);
                    
                if (confirm == JOptionPane.YES_OPTION) {
                    EventTableModel model = (EventTableModel) eventTable.getModel();
                    Event event = model.getEventAt(selectedRow);
                    eventService.deleteEvent(event.getEventId(), event.getGymId());
                    loadEvents();
                    clearInputFields();
                }
            }
        
            private void clearInputFields() {
                eventNameField.setText("");
                dateChooser.setDate(null);
                descriptionField.setText("");
                sponsorField.setText("");
            }

    public JDChooser getDateChooser() {
        return dateChooser;
    }

    public void setDateChooser(JDChooser dateChooser) {
        this.dateChooser = dateChooser;
    }
    public EventService getEventService() {
        return eventService;
    }

    @Override
    public void validatePermissions() {
        setButtonPermissions();
        boolean canEdit = ((PermissionManager)permissionManager).canEdit();
        eventNameField.setEditable(canEdit);
        dateChooser.setEditable(canEdit);
        descriptionField.setEditable(canEdit);
        sponsorField.setEditable(canEdit);
    }

    @SuppressWarnings("unused")
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return button;
    }
} 