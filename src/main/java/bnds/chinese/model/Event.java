package bnds.chinese.model;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class Event {
    private UUID id;
    private String name;
    private String description;
    private CharacterId initiator;
    private Set<CharacterId> affectedCharacters = new LinkedHashSet<>();
    private int affectionDelta;
    private EventStatus status;
    private Instant createdAt;
    private Instant occurredAt;

    public Event() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public CharacterId getInitiator() { return initiator; }
    public void setInitiator(CharacterId initiator) { this.initiator = initiator; }
    public Set<CharacterId> getAffectedCharacters() { return affectedCharacters; }
    public void setAffectedCharacters(Set<CharacterId> affectedCharacters) {
        this.affectedCharacters = affectedCharacters == null ? new LinkedHashSet<>() : new LinkedHashSet<>(affectedCharacters);
    }
    public int getAffectionDelta() { return affectionDelta; }
    public void setAffectionDelta(int affectionDelta) { this.affectionDelta = affectionDelta; }
    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
