package bnds.chinese.service;

import bnds.chinese.dto.GraphResponse;
import bnds.chinese.model.CharacterId;
import bnds.chinese.model.Event;
import bnds.chinese.model.EventStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RelationshipService {
    private static final double MIN_DISTANCE = 70;
    private static final double MAX_DISTANCE = 300;

    public GraphResponse buildGraph(List<Event> events) {
        Map<Pair, Integer> scores = new LinkedHashMap<>();
        for (Event event : events) {
            if (event.getStatus() != EventStatus.OCCURRED) continue;
            for (CharacterId affected : event.getAffectedCharacters()) {
                Pair pair = Pair.of(event.getInitiator(), affected);
                scores.merge(pair, event.getAffectionDelta(), Integer::sum);
            }
        }

        Set<CharacterId> participants = new LinkedHashSet<>();
        List<GraphResponse.Link> links = new ArrayList<>();
        scores.forEach((pair, rawScore) -> {
            int score = Math.max(-100, Math.min(100, rawScore));
            participants.add(pair.first());
            participants.add(pair.second());
            links.add(new GraphResponse.Link(pair.first().name(), pair.second().name(), score, scoreToDistance(score)));
        });

        List<GraphResponse.Node> nodes = participants.stream()
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .map(character -> new GraphResponse.Node(character.name(), character.getDisplayName()))
                .toList();
        return new GraphResponse(nodes, links);
    }

    double scoreToDistance(int score) {
        int clamped = Math.max(-100, Math.min(100, score));
        double normalized = (clamped + 100.0) / 200.0;
        return MAX_DISTANCE - normalized * (MAX_DISTANCE - MIN_DISTANCE);
    }

    private record Pair(CharacterId first, CharacterId second) {
        static Pair of(CharacterId left, CharacterId right) {
            return left.ordinal() <= right.ordinal() ? new Pair(left, right) : new Pair(right, left);
        }
    }
}
