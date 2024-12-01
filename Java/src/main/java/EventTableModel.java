import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;

import javax.swing.table.AbstractTableModel;

public class EventTableModel extends AbstractTableModel {
    private List<Event> events;
    private final String[] columnNames = {"Event ID", "Event Name", "Date", "Description", "Sponsor"};
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    public EventTableModel(List<Event> events) {
        this.events = events;
    }

    public EventTableModel() {
        this.events = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return events != null ? events.size() : 0;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Event event = events.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> event.getEventId();
            case 1 -> event.getName();
            case 2 -> event.getDateTime().format(dateFormatter);
            case 3 -> event.getDescription();
            case 4 -> event.getSponsor();
            default -> null;
        };
    }

    public void updateData(List<Event> newEvents) {
        this.events = newEvents;
        fireTableDataChanged();
    }

    public Event getEventAt(int rowIndex) {
        return events.get(rowIndex);
    }

	public void setEvents(List<Event> events) {
		this.events = events;
		fireTableDataChanged();
	}
}