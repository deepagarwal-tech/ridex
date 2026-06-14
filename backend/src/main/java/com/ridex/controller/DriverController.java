package com.ridex.controller;

import com.ridex.dto.AvailabilityDto;
import com.ridex.dto.LocationUpdateDto;
import com.ridex.model.Driver;
import com.ridex.service.DriverService;
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
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping
    public ResponseEntity<Driver> registerDriver(@Valid @RequestBody Driver driver) {
        return ResponseEntity.status(HttpStatus.CREATED).body(driverService.registerDriver(driver));
    }

    @GetMapping
    public List<Driver> getAllDrivers() {
        return driverService.getAllDrivers();
    }

    @GetMapping("/{id}")
    public Driver getDriver(@PathVariable Long id) {
        return driverService.getDriverById(id);
    }

    @PutMapping("/{id}/location")
    public Driver updateLocation(@PathVariable Long id, @Valid @RequestBody LocationUpdateDto location) {
        return driverService.updateLocation(id, location.getLatitude(), location.getLongitude());
    }

    @PutMapping("/{id}/availability")
    public Driver updateAvailability(@PathVariable Long id, @RequestBody AvailabilityDto availability) {
        return driverService.setAvailability(id, availability.isAvailable());
    }
}
