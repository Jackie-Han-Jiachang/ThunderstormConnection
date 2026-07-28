package bnds.chinese.dto;

import java.util.List;

public record GraphResponse(List<Node> nodes, List<Link> links) {
    public record Node(String id, String name) {}
    public record Link(String source, String target, int score, double distance) {}
}
