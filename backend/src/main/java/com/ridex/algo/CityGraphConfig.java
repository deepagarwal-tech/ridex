package com.ridex.algo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds the in-memory road network ({@link CityGraph}) used to estimate
 * route distances. In a real system these nodes/edges would come from map
 * data; here a small sample network is used so the routing algorithms have
 * something to work with.
 */
@Configuration
public class CityGraphConfig {

    @Bean
    public CityGraph cityGraph() {
        CityGraph graph = new CityGraph();

        // Sample landmarks/intersections (id, name, latitude, longitude)
        graph.addNode("A", "Central Station", 28.6139, 77.2090);
        graph.addNode("B", "City Mall", 28.6200, 77.2150);
        graph.addNode("C", "Tech Park", 28.6300, 77.2200);
        graph.addNode("D", "Airport Road", 28.6050, 77.2300);
        graph.addNode("E", "Old Town", 28.6100, 77.1950);
        graph.addNode("F", "University", 28.6250, 77.1900);
        graph.addNode("G", "Hospital Junction", 28.6180, 77.2050);
        graph.addNode("H", "Stadium", 28.6350, 77.2100);

        // Road connections - weights auto-derived from Haversine distance
        graph.addEdge("A", "G");
        graph.addEdge("A", "E");
        graph.addEdge("G", "B");
        graph.addEdge("G", "F");
        graph.addEdge("B", "C");
        graph.addEdge("B", "D");
        graph.addEdge("C", "H");
        graph.addEdge("F", "H");
        graph.addEdge("E", "F");
        graph.addEdge("D", "A");

        return graph;
    }
}
