package com.ridex.controller;

import com.ridex.algo.CityGraph;
import com.ridex.dto.CityGraphDto;
import com.ridex.dto.CityGraphEdgeDto;
import com.ridex.dto.CityGraphNodeDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Exposes the in-memory city road network so clients can visualize it
 * (nodes = intersections/landmarks, edges = roads with distances).
 */
@RestController
@RequestMapping("/api/city-graph")
public class CityGraphController {

    private final CityGraph cityGraph;

    public CityGraphController(CityGraph cityGraph) {
        this.cityGraph = cityGraph;
    }

    @GetMapping
    public CityGraphDto getCityGraph() {
        List<CityGraphNodeDto> nodes = new ArrayList<>();
        for (CityGraph.Node node : cityGraph.getNodes()) {
            nodes.add(new CityGraphNodeDto(node.getId(), node.getName(), node.getLatitude(), node.getLongitude()));
        }

        List<CityGraphEdgeDto> edges = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (CityGraph.Node node : cityGraph.getNodes()) {
            for (CityGraph.Edge edge : cityGraph.getNeighbors(node.getId())) {
                String key1 = node.getId() + "-" + edge.getTarget();
                String key2 = edge.getTarget() + "-" + node.getId();
                if (seen.contains(key1) || seen.contains(key2)) {
                    continue;
                }
                seen.add(key1);
                edges.add(new CityGraphEdgeDto(node.getId(), edge.getTarget(), edge.getWeightKm()));
            }
        }

        return new CityGraphDto(nodes, edges);
    }
}
