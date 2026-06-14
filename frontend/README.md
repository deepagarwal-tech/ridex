# Ridex Frontend

A React (Vite) dashboard for the Ridex backend. Lets you create riders,
register drivers, request rides, and watch the route get computed live on
a diagram of the city's road network (the same `CityGraph` your backend
uses for Dijkstra routing).

## Prerequisites

- Node.js (you have v24 - that's fine)
- The Ridex **backend** must be running first, on `http://localhost:8080`
  (see the backend's own README / run.bat / run.sh)

## Running

```bash
npm install
npm run dev
```

This starts a dev server, normally at **http://localhost:5173**. Open that
URL in your browser.

If the page shows a red banner saying it can't reach the backend, make sure
the Spring Boot app (`ridex` folder) is running first.

## What you can do

1. **Add a rider** - name + email, becomes the "current rider".
2. **Add a driver** - gets placed at a random location on the map.
3. **Request a ride** - pick a pickup and drop-off location from the
   dropdowns (these correspond to the 8 nodes in the backend's sample
   city graph). The app calls the backend, which finds the nearest
   available driver and computes the route via Dijkstra.
4. **Watch the map** - the computed route animates along the road network,
   with the pickup node in green and the drop-off node in amber.
5. **Complete or cancel** the trip - frees up the driver again.

## Notes

- The backend uses an in-memory H2 database, so all data resets when you
  restart the backend.
- CORS is configured on the backend to allow `http://localhost:5173`. If
  you run the frontend on a different port, update
  `src/main/java/com/ridex/config/CorsConfig.java` in the backend.
