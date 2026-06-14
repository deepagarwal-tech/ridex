package com.ridex.algo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple weighted, undirected graph representing a city's road network.
 * Nodes represent intersections/landmarks (with coordinates) and edges
 * represent roads connecting them, weighted by distance in kilometers.
 */
public class CityGraph {

    public static class Node {
        private final String id;
        private final String name;
        private final double latitude;
        private final double longitude;

        public Node(String id, String name, double latitude, double longitude) {
            this.id = id;
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }

    public static class Edge {
        private final String target;
        private final double weightKm;

        public Edge(String target, double weightKm) {
            this.target = target;
            this.weightKm = weightKm;
        }

        public String getTarget() {
            return target;
        }

        public double getWeightKm() {
            return weightKm;
        }
    }

    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final Map<String, List<Edge>> adjacency = new HashMap<>();

    public void addNode(String id, String name, double latitude, double longitude) {
        nodes.put(id, new Node(id, name, latitude, longitude));
        adjacency.putIfAbsent(id, new ArrayList<>());
    }

    /**
     * Adds an undirected edge between two existing nodes. The weight is
     * automatically derived from the Haversine distance between them.
     */
    public void addEdge(String fromId, String toId) {
        Node from = requireNode(fromId);
        Node to = requireNode(toId);
        double distance = GeoUtils.haversineDistance(
                from.getLatitude(), from.getLongitude(),
                to.getLatitude(), to.getLongitude());
        addEdge(fromId, toId, distance);
    }

    /**
     * Adds an undirected edge between two existing nodes with an explicit weight.
     */
    public void addEdge(String fromId, String toId, double weightKm) {
        requireNode(fromId);
        requireNode(toId);
        adjacency.get(fromId).add(new Edge(toId, weightKm));
        adjacency.get(toId).add(new Edge(fromId, weightKm));
    }

    public Node getNode(String id) {
        return nodes.get(id);
    }

    public boolean hasNode(String id) {
        return nodes.containsKey(id);
    }

    public Collection<Node> getNodes() {
        return nodes.values();
    }

    public List<Edge> getNeighbors(String nodeId) {
        return adjacency.getOrDefault(nodeId, Collections.emptyList());
    }

    /**
     * Finds the id of the graph node closest (straight-line) to the given coordinates.
     * Returns null if the graph has no nodes.
     */
    public String findNearestNode(double latitude, double longitude) {
        String nearestId = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Node node : nodes.values()) {
            double distance = GeoUtils.haversineDistance(latitude, longitude, node.getLatitude(), node.getLongitude());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestId = node.getId();
            }
        }

        return nearestId;
    }

    private Node requireNode(String id) {
        Node node = nodes.get(id);
        if (node == null) {
            throw new IllegalArgumentException("Node does not exist in graph: " + id);
        }
        return node;
    }
}
