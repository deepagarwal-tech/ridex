package com.ridex.dto;

import java.util.List;

public class CityGraphDto {

    private List<CityGraphNodeDto> nodes;
    private List<CityGraphEdgeDto> edges;

    public CityGraphDto(List<CityGraphNodeDto> nodes, List<CityGraphEdgeDto> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<CityGraphNodeDto> getNodes() {
        return nodes;
    }

    public List<CityGraphEdgeDto> getEdges() {
        return edges;
    }
}
