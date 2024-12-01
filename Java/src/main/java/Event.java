import java.sql.Date;
import java.time.LocalDateTime;

public class Event {
    private final int eventId;
    private String name;
    private LocalDateTime dateTime;
    private String description;
    private int gymId;
    private String sponsor;

    public Event(int eventId, String name, LocalDateTime dateTime, String description, 
                 int gymId, String sponsor) {
        this.eventId = eventId;
        this.name = name;
        this.dateTime = dateTime;
        this.description = description;
        this.gymId = gymId;
        this.sponsor = sponsor;
    }

    // Getters and setters
    public int getEventId() { return eventId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getGymId() { return gymId; }
    public void setGymId(int gymId) { this.gymId = gymId; }
    public String getSponsor() { return sponsor; }
    public void setSponsor(String sponsor) { this.sponsor = sponsor; }

    public String getEventName() {
        return getName();
    }

    public LocalDateTime getEventDate() {
        return getDateTime();
    }

	public void setEventId(int eventId) {
		throw new UnsupportedOperationException("Cannot modify event ID after creation");
	}

	public void setEventName(String eventName) {
		this.name = eventName;
	}

	public void setEventDate(Date date) {
		this.dateTime = date.toLocalDate().atStartOfDay();
	}
} 