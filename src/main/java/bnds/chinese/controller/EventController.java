package bnds.chinese.controller;

import bnds.chinese.dto.EventRequest;
import bnds.chinese.dto.GraphResponse;
import bnds.chinese.model.CharacterId;
import bnds.chinese.model.Event;
import bnds.chinese.service.EventService;
import bnds.chinese.service.RelationshipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class EventController {
    private final EventService eventService;
    private final RelationshipService relationshipService;

    public EventController(EventService eventService, RelationshipService relationshipService) {
        this.eventService = eventService;
        this.relationshipService = relationshipService;
    }

    @GetMapping("/events")
    public List<Event> events() {
        return eventService.getAll();
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public Event create(@Valid @RequestBody EventRequest request) {
        return eventService.create(request);
    }

    @PutMapping("/events/{id}")
    public Event update(@PathVariable UUID id, @Valid @RequestBody EventRequest request) {
        return eventService.update(id, request);
    }

    @DeleteMapping("/events/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        eventService.delete(id);
    }

    @PostMapping("/events/{id}/activate")
    public Event activate(@PathVariable UUID id) {
        return eventService.activate(id);
    }

    @PostMapping("/events/{id}/revert")
    public Event revert(@PathVariable UUID id) {
        return eventService.revert(id);
    }

    @GetMapping("/graph")
    public GraphResponse graph() {
        return relationshipService.buildGraph(eventService.getAll());
    }

    @GetMapping("/characters")
    public List<Map<String, String>> characters() {
        return Arrays.stream(CharacterId.values())
                .map(character -> Map.of("id", character.name(), "name", character.getDisplayName()))
                .toList();
    }
}
