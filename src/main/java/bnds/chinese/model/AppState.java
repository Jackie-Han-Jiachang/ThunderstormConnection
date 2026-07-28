package bnds.chinese.model;

import java.util.ArrayList;
import java.util.List;

public class AppState {
    private int schemaVersion = 1;
    private List<Event> events = new ArrayList<>();

    public int getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(int schemaVersion) { this.schemaVersion = schemaVersion; }
    public List<Event> getEvents() { return events; }
    public void setEvents(List<Event> events) {
        this.events = events == null ? new ArrayList<>() : new ArrayList<>(events);
    }
}
