package com.ridex.dto;

public class CityGraphEdgeDto {

    private String from;
    private String to;
    private double weightKm;

    public CityGraphEdgeDto(String from, String to, double weightKm) {
        this.from = from;
        this.to = to;
        this.weightKm = weightKm;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public double getWeightKm() {
        return weightKm;
    }
}
