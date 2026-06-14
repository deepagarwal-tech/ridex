package com.ridex.algo;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Dijkstra's shortest-path algorithm for finding the lowest-cost route
 * between two nodes of a {@link CityGraph}.
 */
public final class Dijkstra {

    private Dijkstra() {
    }

    public static class Result {
        private final double distanceKm;
        private final List<String> path;

        public Result(double distanceKm, List<String> path) {
            this.distanceKm = distanceKm;
            this.path = path;
        }

        /** Total distance of the path in kilometers, or Double.MAX_VALUE if unreachable. */
        public double getDistanceKm() {
            return distanceKm;
        }

        /** Ordered list of node ids from source to target. Empty if unreachable. */
        public List<String> getPath() {
            return path;
        }

        public boolean isReachable() {
            return distanceKm != Double.MAX_VALUE;
        }
    }

    private static class NodeDistance implements Comparable<NodeDistance> {
        final String nodeId;
        final double distance;

        NodeDistance(String nodeId, double distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodeDistance other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    /**
     * Computes the shortest path between sourceId and targetId in the given graph.
     */
    public static Result shortestPath(CityGraph graph, String sourceId, String targetId) {
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        Set<String> visited = new HashSet<>();

        for (CityGraph.Node node : graph.getNodes()) {
            distances.put(node.getId(), Double.MAX_VALUE);
        }

        if (!distances.containsKey(sourceId) || !distances.containsKey(targetId)) {
            return new Result(Double.MAX_VALUE, Collections.emptyList());
        }

        distances.put(sourceId, 0.0);

        PriorityQueue<NodeDistance> queue = new PriorityQueue<>();
        queue.add(new NodeDistance(sourceId, 0.0));

        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();

            if (!visited.add(current.nodeId)) {
                continue;
            }

            if (current.nodeId.equals(targetId)) {
                break;
            }

            for (CityGraph.Edge edge : graph.getNeighbors(current.nodeId)) {
                if (visited.contains(edge.getTarget())) {
                    continue;
                }

                double newDistance = distances.get(current.nodeId) + edge.getWeightKm();
                if (newDistance < distances.get(edge.getTarget())) {
                    distances.put(edge.getTarget(), newDistance);
                    previous.put(edge.getTarget(), current.nodeId);
                    queue.add(new NodeDistance(edge.getTarget(), newDistance));
                }
            }
        }

        double finalDistance = distances.get(targetId);
        if (finalDistance == Double.MAX_VALUE) {
            return new Result(Double.MAX_VALUE, Collections.emptyList());
        }

        LinkedList<String> path = new LinkedList<>();
        String step = targetId;
        path.add(step);
        while (previous.containsKey(step)) {
            step = previous.get(step);
            path.addFirst(step);
        }

        return new Result(finalDistance, path);
    }
}
