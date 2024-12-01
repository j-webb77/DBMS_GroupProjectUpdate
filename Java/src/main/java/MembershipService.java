import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MembershipService {
    private static final String INSERT_MEMBERSHIP = 
        "INSERT INTO memberships (member_name, membership_type, start_date, end_date, gym_id, price, status) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_MEMBERSHIP = 
        "UPDATE memberships SET member_name=?, membership_type=?, start_date=?, end_date=?, " +
        "price=?, status=? WHERE membership_id=?";
    
    private static final String DELETE_MEMBERSHIP = 
        "DELETE FROM memberships WHERE membership_id=?";
    
    private static final String SELECT_MEMBERSHIPS_BY_GYM = 
        "SELECT * FROM memberships WHERE gym_id=?";

    public void addMembership(Membership membership, int gymId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_MEMBERSHIP, 
                 PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, membership.getMemberName());
            stmt.setString(2, membership.getMembershipType());
            stmt.setDate(3, membership.getStartDate() != null ? 
                Date.valueOf(membership.getStartDate()) : null);
            stmt.setDate(4, membership.getEndDate() != null ? 
                Date.valueOf(membership.getEndDate()) : null);
            stmt.setInt(5, gymId);
            stmt.setDouble(6, membership.getPrice());
            stmt.setString(7, membership.getStatus());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating membership failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    System.out.println("Successfully added membership with ID: " + generatedId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding membership: " + e.getMessage());
            throw e;
        }
    }

    public void updateMembership(Membership membership) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_MEMBERSHIP)) {
            
            stmt.setString(1, membership.getMemberName());
            stmt.setString(2, membership.getMembershipType());
            stmt.setDate(3, Date.valueOf(membership.getStartDate()));
            stmt.setDate(4, Date.valueOf(membership.getEndDate()));
            stmt.setDouble(5, membership.getPrice());
            stmt.setString(6, membership.getStatus());
            stmt.setInt(7, membership.getMembershipId());
            
            stmt.executeUpdate();
        }
    }

    public void deleteMembership(int membershipId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_MEMBERSHIP)) {
            
            stmt.setInt(1, membershipId);
            stmt.executeUpdate();
        }
    }

    public List<Membership> getMembershipsByGym(int gymId) throws SQLException {
        List<Membership> memberships = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_MEMBERSHIPS_BY_GYM)) {
            
            stmt.setInt(1, gymId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Membership membership = new Membership(
                        rs.getInt("membership_id"),
                        rs.getString("member_name"),
                        rs.getString("membership_type"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        rs.getInt("gym_id"),
                        rs.getDouble("price"),
                        rs.getString("status")
                    );
                    memberships.add(membership);
                }
            }
        }
        return memberships;
    }

    public boolean membershipExists(Connection conn, int membershipId) throws SQLException {
        String query = "SELECT 1 FROM memberships WHERE membership_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, membershipId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
} 