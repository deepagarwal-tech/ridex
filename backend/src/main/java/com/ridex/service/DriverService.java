package com.ridex.service;

import com.ridex.algo.GeoUtils;
import com.ridex.model.Driver;
import com.ridex.repository.DriverRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public Driver registerDriver(Driver driver) {
        driver.setAvailable(true);
        return driverRepository.save(driver);
    }

    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    public Driver getDriverById(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found: " + id));
    }

    public Driver updateLocation(Long id, double latitude, double longitude) {
        Driver driver = getDriverById(id);
        driver.setCurrentLatitude(latitude);
        driver.setCurrentLongitude(longitude);
        return driverRepository.save(driver);
    }

    public Driver setAvailability(Long id, boolean available) {
        Driver driver = getDriverById(id);
        driver.setAvailable(available);
        return driverRepository.save(driver);
    }

    /**
     * Finds the nearest available driver to a pickup location using the
     * Haversine straight-line distance.
     */
    public Optional<Driver> findNearestAvailableDriver(double pickupLatitude, double pickupLongitude) {
        List<Driver> availableDrivers = driverRepository.findByAvailableTrue();

        Driver nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Driver driver : availableDrivers) {
            double distance = GeoUtils.haversineDistance(
                    pickupLatitude, pickupLongitude,
                    driver.getCurrentLatitude(), driver.getCurrentLongitude());

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = driver;
            }
        }

        return Optional.ofNullable(nearest);
    }
}
