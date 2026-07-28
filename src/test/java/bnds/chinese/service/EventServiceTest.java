package bnds.chinese.service;

import bnds.chinese.dto.EventRequest;
import bnds.chinese.exception.InvalidOperationException;
import bnds.chinese.model.AppState;
import bnds.chinese.model.CharacterId;
import bnds.chinese.model.Event;
import bnds.chinese.model.EventStatus;
import bnds.chinese.repository.EventRepository;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventServiceTest {
    private final MemoryRepository repository = new MemoryRepository();
    private final EventService service = new EventService(repository);

    @Test
    void createsPreparedEventThenActivatesIt() {
        Event created = service.create(request());
        assertThat(created.getStatus()).isEqualTo(EventStatus.PREPARED);
        assertThat(created.getCreatedAt()).isNotNull();

        Event activated = service.activate(created.getId());
        assertThat(activated.getStatus()).isEqualTo(EventStatus.OCCURRED);
        assertThat(activated.getOccurredAt()).isNotNull();
    }

    @Test
    void rejectsInitiatorAsAffectedCharacter() {
        EventRequest invalid = new EventRequest("事件", "描述", CharacterId.ZHOU_CHONG,
                Set.of(CharacterId.ZHOU_CHONG), 2);
        assertThatThrownBy(() -> service.create(invalid))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void occurredEventCannotBeEdited() {
        Event created = service.create(request());
        service.activate(created.getId());
        assertThatThrownBy(() -> service.update(created.getId(), request()))
                .isInstanceOf(InvalidOperationException.class);
    }

    private EventRequest request() {
        return new EventRequest("花园相遇", "周冲向四凤表达关心", CharacterId.ZHOU_CHONG,
                new LinkedHashSet<>(Set.of(CharacterId.LU_SIFENG)), 4);
    }

    private static class MemoryRepository implements EventRepository {
        private AppState state = new AppState();
        public AppState load() { return state; }
        public void save(AppState state) { this.state = state; }
    }
}
