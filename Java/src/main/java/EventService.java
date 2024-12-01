import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventService {
    public EventService() {
        // Remove dbConnection initialization as we'll get fresh connections for each operation
    }

    public List<Event> getEventsForGym(int gymId) throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE gym_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gymId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    events.add(createEventFromResultSet(rs));
                }
            }
        }
        return events;
    }

    // Add other CRUD operations
    // ... (implementation of addEvent, updateEvent, deleteEvent)
    private Event createEventFromResultSet(ResultSet rs) throws SQLException {
        return new Event(
            rs.getInt("event_id"),
            rs.getString("name"),
            rs.getDate("date_time").toLocalDate().atStartOfDay(),
            rs.getString("description"),
            rs.getInt("gym_id"),
            rs.getString("sponsor")
        );
    }

    public void addEvent(Event event) throws SQLException {
        String sql = "INSERT INTO events (gym_id, name, date_time, description, sponsor) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, event.getGymId());
            stmt.setString(2, event.getName());
            stmt.setDate(3, Date.valueOf(event.getDateTime().toLocalDate()));
            stmt.setString(4, event.getDescription());
            stmt.setString(5, event.getSponsor());
            
            stmt.executeUpdate();
        }
    }

    public void updateEvent(Event event) throws SQLException {
        String sql = "UPDATE events SET name = ?, date_time = ?, description = ?, sponsor = ? WHERE event_id = ? AND gym_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, event.getName());
            stmt.setDate(2, Date.valueOf(event.getDateTime().toLocalDate()));
            stmt.setString(3, event.getDescription());
            stmt.setString(4, event.getSponsor());
            stmt.setInt(5, event.getEventId());
            stmt.setInt(6, event.getGymId());
            
            stmt.executeUpdate();
        }
    }

    public void deleteEvent(int eventId, int gymId) throws SQLException {
        String sql = "DELETE FROM events WHERE event_id = ? AND gym_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eventId);
            stmt.setInt(2, gymId);
            stmt.executeUpdate();
        }
    }
}