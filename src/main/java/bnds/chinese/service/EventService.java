package bnds.chinese.service;

import bnds.chinese.dto.EventRequest;
import bnds.chinese.exception.InvalidOperationException;
import bnds.chinese.exception.NotFoundException;
import bnds.chinese.model.AppState;
import bnds.chinese.model.Event;
import bnds.chinese.model.EventStatus;
import bnds.chinese.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class EventService {
    private final EventRepository repository;

    public EventService(EventRepository repository) {
        this.repository = repository;
    }

    public synchronized List<Event> getAll() {
        return repository.load().getEvents().stream()
                .sorted(Comparator.comparing(Event::getCreatedAt))
                .toList();
    }

    public synchronized Event create(EventRequest request) {
        validatePeople(request);
        AppState state = repository.load();
        Event event = new Event();
        event.setId(UUID.randomUUID());
        applyRequest(event, request);
        event.setStatus(EventStatus.PREPARED);
        event.setCreatedAt(Instant.now());
        state.getEvents().add(event);
        repository.save(state);
        return event;
    }

    public synchronized Event update(UUID id, EventRequest request) {
        validatePeople(request);
        AppState state = repository.load();
        Event event = find(state, id);
        requirePrepared(event, "只有预备事件可以编辑");
        applyRequest(event, request);
        repository.save(state);
        return event;
    }

    public synchronized void delete(UUID id) {
        AppState state = repository.load();
        Event event = find(state, id);
        requirePrepared(event, "只有预备事件可以删除");
        state.getEvents().remove(event);
        repository.save(state);
    }

    public synchronized Event activate(UUID id) {
        AppState state = repository.load();
        Event event = find(state, id);
        requirePrepared(event, "该事件已经发生");
        event.setStatus(EventStatus.OCCURRED);
        event.setOccurredAt(Instant.now());
        repository.save(state);
        return event;
    }

    public synchronized Event revert(UUID id) {
        AppState state = repository.load();
        Event event = find(state, id);
        if (event.getStatus() != EventStatus.OCCURRED) {
            throw new InvalidOperationException("该事件尚未发生");
        }
        event.setStatus(EventStatus.PREPARED);
        event.setOccurredAt(null);
        repository.save(state);
        return event;
    }

    private void applyRequest(Event event, EventRequest request) {
        event.setName(request.name().trim());
        event.setDescription(request.description().trim());
        event.setInitiator(request.initiator());
        event.setAffectedCharacters(request.affectedCharacters());
        event.setAffectionDelta(request.affectionDelta());
    }

    private void validatePeople(EventRequest request) {
        if (request.affectedCharacters().contains(request.initiator())) {
            throw new InvalidOperationException("发起人不能同时作为被影响人");
        }
    }

    private Event find(AppState state, UUID id) {
        return state.getEvents().stream()
                .filter(event -> id.equals(event.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("未找到该事件"));
    }

    private void requirePrepared(Event event, String message) {
        if (event.getStatus() != EventStatus.PREPARED) {
            throw new InvalidOperationException(message);
        }
    }
}
