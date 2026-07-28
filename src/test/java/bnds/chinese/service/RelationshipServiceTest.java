package bnds.chinese.service;

import bnds.chinese.dto.GraphResponse;
import bnds.chinese.model.CharacterId;
import bnds.chinese.model.Event;
import bnds.chinese.model.EventStatus;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RelationshipServiceTest {
    private final RelationshipService service = new RelationshipService();

    @Test
    void returnsEmptyGraphBeforeAnEventOccurs() {
        GraphResponse graph = service.buildGraph(List.of(event(EventStatus.PREPARED, 8, CharacterId.ZHOU_CHONG)));
        assertThat(graph.nodes()).isEmpty();
        assertThat(graph.links()).isEmpty();
    }

    @Test
    void accumulatesScoresForTheSameUndirectedPair() {
        GraphResponse graph = service.buildGraph(List.of(
                event(EventStatus.OCCURRED, 7, CharacterId.ZHOU_CHONG),
                event(EventStatus.OCCURRED, -2, CharacterId.ZHOU_CHONG)
        ));
        assertThat(graph.links()).singleElement().satisfies(link -> {
            assertThat(link.score()).isEqualTo(5);
            assertThat(link.source()).isEqualTo(CharacterId.LU_SIFENG.name());
            assertThat(link.target()).isEqualTo(CharacterId.ZHOU_CHONG.name());
        });
    }

    @Test
    void higherAffectionProducesShorterDistance() {
        assertThat(service.scoreToDistance(10)).isLessThan(service.scoreToDistance(-10));
    }

    private Event event(EventStatus status, int score, CharacterId initiator) {
        Event event = new Event();
        event.setStatus(status);
        event.setInitiator(initiator);
        event.setAffectedCharacters(new LinkedHashSet<>(List.of(CharacterId.LU_SIFENG)));
        event.setAffectionDelta(score);
        return event;
    }
}
