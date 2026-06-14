package com.ridex.service;

import com.ridex.algo.CityGraph;
import com.ridex.algo.Dijkstra;
import com.ridex.algo.GeoUtils;
import com.ridex.model.Driver;
import com.ridex.model.Trip;
import com.ridex.model.TripStatus;
import com.ridex.model.User;
import com.ridex.repository.TripRepository;
import com.ridex.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RideService {

    private static final double BASE_FARE = 40.0;   // flat starting fare
    private static final double RATE_PER_KM = 12.0; // fare per kilometer travelled

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final DriverService driverService;
    private final CityGraph cityGraph;

    public RideService(TripRepository tripRepository,
                        UserRepository userRepository,
                        DriverService driverService,
                        CityGraph cityGraph) {
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.driverService = driverService;
        this.cityGraph = cityGraph;
    }

    /**
     * Requests a new ride: finds the nearest available driver, estimates the
     * route distance/fare, marks the driver busy, and persists the trip.
     */
    public Trip requestRide(Long userId, double pickupLat, double pickupLon, double dropLat, double dropLon) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        Driver driver = driverService.findNearestAvailableDriver(pickupLat, pickupLon)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No drivers available right now"));

        double routeDistance = calculateRouteDistance(pickupLat, pickupLon, dropLat, dropLon);
        double fare = round2(BASE_FARE + RATE_PER_KM * routeDistance);
        java.util.List<String> routePath = calculateRoutePath(pickupLat, pickupLon, dropLat, dropLon);

        driverService.setAvailability(driver.getId(), false);

        Trip trip = new Trip();
        trip.setUser(user);
        trip.setDriver(driver);
        trip.setPickupLatitude(pickupLat);
        trip.setPickupLongitude(pickupLon);
        trip.setDropLatitude(dropLat);
        trip.setDropLongitude(dropLon);
        trip.setDistanceKm(round2(routeDistance));
        trip.setFare(fare);
        trip.setStatus(TripStatus.ONGOING);
        trip.setCreatedAt(LocalDateTime.now());

        Trip saved = tripRepository.save(trip);
        saved.setRoutePath(routePath);
        return saved;
    }

    /**
     * Returns the ordered list of CityGraph node ids forming the shortest
     * path between the pickup and drop points (for visualizing the route).
     */
    private java.util.List<String> calculateRoutePath(double pickupLat, double pickupLon, double dropLat, double dropLon) {
        String pickupNode = cityGraph.findNearestNode(pickupLat, pickupLon);
        String dropNode = cityGraph.findNearestNode(dropLat, dropLon);

        if (pickupNode == null || dropNode == null) {
            return java.util.Collections.emptyList();
        }

        Dijkstra.Result result = Dijkstra.shortestPath(cityGraph, pickupNode, dropNode);
        return result.isReachable() ? result.getPath() : java.util.Collections.emptyList();
    }

    /**
     * Estimates road distance between two coordinates:
     * 1. Snap pickup/drop to the nearest CityGraph nodes.
     * 2. Run Dijkstra's algorithm between those nodes for the in-network distance.
     * 3. Add the "last mile" Haversine distance from each point to its snapped node.
     * Falls back to a direct Haversine estimate if the graph has no nodes.
     */
    private double calculateRouteDistance(double pickupLat, double pickupLon, double dropLat, double dropLon) {
        String pickupNode = cityGraph.findNearestNode(pickupLat, pickupLon);
        String dropNode = cityGraph.findNearestNode(dropLat, dropLon);

        if (pickupNode == null || dropNode == null) {
            return GeoUtils.haversineDistance(pickupLat, pickupLon, dropLat, dropLon);
        }

        double lastMilePickup = GeoUtils.haversineDistance(
                pickupLat, pickupLon,
                cityGraph.getNode(pickupNode).getLatitude(), cityGraph.getNode(pickupNode).getLongitude());

        double lastMileDrop = GeoUtils.haversineDistance(
                dropLat, dropLon,
                cityGraph.getNode(dropNode).getLatitude(), cityGraph.getNode(dropNode).getLongitude());

        Dijkstra.Result result = Dijkstra.shortestPath(cityGraph, pickupNode, dropNode);
        double graphDistance = result.isReachable() ? result.getDistanceKm() : 0.0;

        return lastMilePickup + graphDistance + lastMileDrop;
    }

    public Trip getTripById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found: " + id));
    }

    public List<Trip> getTripsForUser(Long userId) {
        return tripRepository.findByUserId(userId);
    }

    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    public Trip completeTrip(Long tripId) {
        Trip trip = getTripById(tripId);

        if (trip.getStatus() != TripStatus.ONGOING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trip is not currently ongoing");
        }

        trip.setStatus(TripStatus.COMPLETED);
        trip.setCompletedAt(LocalDateTime.now());

        if (trip.getDriver() != null) {
            driverService.setAvailability(trip.getDriver().getId(), true);
        }

        return tripRepository.save(trip);
    }

    public Trip cancelTrip(Long tripId) {
        Trip trip = getTripById(tripId);

        if (trip.getStatus() == TripStatus.COMPLETED || trip.getStatus() == TripStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trip has already finished");
        }

        trip.setStatus(TripStatus.CANCELLED);
        trip.setCompletedAt(LocalDateTime.now());

        if (trip.getDriver() != null) {
            driverService.setAvailability(trip.getDriver().getId(), true);
        }

        return tripRepository.save(trip);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
