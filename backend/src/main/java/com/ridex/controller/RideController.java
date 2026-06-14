package com.ridex.controller;

import com.ridex.dto.RideRequestDto;
import com.ridex.model.Trip;
import com.ridex.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping("/request")
    public ResponseEntity<Trip> requestRide(@Valid @RequestBody RideRequestDto request) {
        Trip trip = rideService.requestRide(
                request.getUserId(),
                request.getPickupLatitude(),
                request.getPickupLongitude(),
                request.getDropLatitude(),
                request.getDropLongitude());

        return ResponseEntity.status(HttpStatus.CREATED).body(trip);
    }

    @GetMapping("/{id}")
    public Trip getTrip(@PathVariable Long id) {
        return rideService.getTripById(id);
    }

    @GetMapping
    public List<Trip> getAllTrips() {
        return rideService.getAllTrips();
    }

    @GetMapping("/user/{userId}")
    public List<Trip> getTripsForUser(@PathVariable Long userId) {
        return rideService.getTripsForUser(userId);
    }

    @PutMapping("/{id}/complete")
    public Trip completeTrip(@PathVariable Long id) {
        return rideService.completeTrip(id);
    }

    @PutMapping("/{id}/cancel")
    public Trip cancelTrip(@PathVariable Long id) {
        return rideService.cancelTrip(id);
    }
}
