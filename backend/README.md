# Ridex — Ride-Hailing Backend (Spring Boot)

A from-scratch Spring Boot backend for a simple ride-hailing app, using:

- **Maven** for build/dependency management
- **H2** in-memory database (no setup required)
- **Spring Data JPA** for persistence
- Custom **algorithms** package:
  - `GeoUtils` — Haversine formula for great-circle distance
  - `CityGraph` — weighted graph model of a city's roads
  - `Dijkstra` — shortest-path algorithm over `CityGraph`

## Project Structure

```
src/main/java/com/ridex/
├── RidexApplication.java
├── controller/
│   ├── UserController.java
│   ├── DriverController.java
│   └── RideController.java
├── service/
│   ├── RideService.java
│   └── DriverService.java
├── model/
│   ├── User.java
│   ├── Driver.java
│   ├── Trip.java
│   └── TripStatus.java
├── algo/
│   ├── GeoUtils.java       # Haversine
│   ├── CityGraph.java      # Graph model
│   ├── Dijkstra.java        # Shortest path algorithm
│   └── CityGraphConfig.java # Sample city graph bean
├── dto/
│   ├── RideRequestDto.java
│   ├── LocationUpdateDto.java
│   └── AvailabilityDto.java
└── repository/
    ├── UserRepository.java
    ├── DriverRepository.java
    └── TripRepository.java
```

## How the algorithms fit together

When a ride is requested:

1. **`DriverService.findNearestAvailableDriver`** scans all available drivers
   and uses **`GeoUtils.haversineDistance`** to find the closest one to the
   pickup point.
2. **`RideService.calculateRouteDistance`**:
   - Snaps the pickup and drop coordinates to the nearest nodes in the
     `CityGraph` (again via Haversine).
   - Runs **`Dijkstra.shortestPath`** between those two nodes to estimate the
     in-city road distance.
   - Adds the "last mile" Haversine distance from the actual pickup/drop
     points to their snapped graph nodes.
3. The resulting distance feeds into a simple fare formula:
   `fare = BASE_FARE + RATE_PER_KM * distance`.

The sample city graph (8 nodes, ~10 roads) is defined in `CityGraphConfig`
and loaded as a Spring bean — replace it with real map data as needed.

## Running the app

Requires **Java 17+**. You do **not** need Maven installed separately —
use one of the run scripts below, which download a local copy of Maven
automatically on first run (no admin rights or PATH changes needed).

**Mac / Linux:**
```bash
./run.sh
```

**Windows:**
```bat
run.bat
```

If you already have Maven installed and prefer to use it directly:
```bash
mvn spring-boot:run
```

The app starts on **http://localhost:8080**. The H2 console is available at
**http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:mem:ridexdb`,
username `sa`, no password).

## API Reference

### Users

```bash
# Create a user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice", "email": "alice@example.com", "phone": "9990001111"}'

# List users
curl http://localhost:8080/api/users

# Get a user
curl http://localhost:8080/api/users/1
```

### Drivers

```bash
# Register a driver (somewhere near "Central Station" node A: 28.6139, 77.2090)
curl -X POST http://localhost:8080/api/drivers \
  -H "Content-Type: application/json" \
  -d '{"name": "Bob", "phone": "8880002222", "vehicleNumber": "DL01AB1234", "currentLatitude": 28.6140, "currentLongitude": 77.2095}'

# List all drivers
curl http://localhost:8080/api/drivers

# Get a driver
curl http://localhost:8080/api/drivers/1

# Update a driver's live location
curl -X PUT http://localhost:8080/api/drivers/1/location \
  -H "Content-Type: application/json" \
  -d '{"latitude": 28.6200, "longitude": 77.2150}'

# Toggle availability
curl -X PUT http://localhost:8080/api/drivers/1/availability \
  -H "Content-Type: application/json" \
  -d '{"available": true}'
```

### Rides

```bash
# Request a ride (pickup near node A, drop near node H)
curl -X POST http://localhost:8080/api/rides/request \
  -H "Content-Type: application/json" \
  -d '{
        "userId": 1,
        "pickupLatitude": 28.6139,
        "pickupLongitude": 77.2090,
        "dropLatitude": 28.6350,
        "dropLongitude": 77.2100
      }'

# Get a trip
curl http://localhost:8080/api/rides/1

# List all trips
curl http://localhost:8080/api/rides

# List trips for a user
curl http://localhost:8080/api/rides/user/1

# Complete a trip (frees up the driver)
curl -X PUT http://localhost:8080/api/rides/1/complete

# Cancel a trip (frees up the driver)
curl -X PUT http://localhost:8080/api/rides/1/cancel
```

## Notes / Next steps

- `spring.jpa.hibernate.ddl-auto=update` auto-creates tables from the
  entities — fine for development, but use migrations (Flyway/Liquibase) for
  production.
- The H2 database is **in-memory**: all data is lost on restart.
- The `CityGraph` is currently a hardcoded sample network — swap in real
  road/intersection data for production use.
- Fare constants (`BASE_FARE`, `RATE_PER_KM`) live in `RideService` — extract
  to configuration if they need to vary by city or vehicle type.
