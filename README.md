# Ridex — Full App (Backend + Frontend)

> Want to deploy this online (e.g. for a resume link)? See **[DEPLOYMENT.md](./DEPLOYMENT.md)**.

This package contains two folders:

- **`backend/`** — the Spring Boot API (Java 17, Maven, H2 file-based database — data persists between restarts)
- **`frontend/`** — the React dashboard (Vite, Node.js)

You need **both running at the same time** — the frontend talks to the
backend over HTTP.

## Quick start (one command)

**Windows:**
```
start.bat
```

**Mac/Linux:**
```
./start.sh
```

This starts both the backend and frontend for you — on Windows it opens
two separate windows (one per process) so you can see their logs; on
Mac/Linux it runs both in the background and writes `backend.log` /
`frontend.log` in this folder (press Ctrl+C in that terminal to stop both).

Wait about 10-15 seconds, then open **http://localhost:5173**.

If you'd rather start them manually (e.g. to see backend output more
clearly), see the step-by-step instructions below.

## Manual start (two terminals)

## 1. Start the backend first

```
cd backend
run.bat        (Windows)
./run.sh       (Mac/Linux)
```

Wait until you see a line like:

```
Started RidexApplication in X.XXX seconds
```

Leave this terminal open. The backend is now running on
**http://localhost:8080**.

## 2. Start the frontend (in a NEW terminal)

```
cd frontend
npm install
npm run dev
```

`npm install` only needs to be run once (the first time). After it
finishes, `npm run dev` starts the dev server — you'll see something like:

```
  VITE vX.X.X  ready in XXX ms

  ➜  Local:   http://localhost:5173/
```

## 3. Open the app

Go to **http://localhost:5173** in your browser.

You should see the Ridex dashboard. From here you can:

- Add a rider (name + email)
- Add a driver (gets placed on the city map)
- Pick a pickup and drop-off location and request a ride
- Watch the route get drawn on the city map, with the fare and distance
  computed by the backend's Dijkstra/Haversine algorithms
- Complete or cancel the trip

## Troubleshooting

- **Red banner: "Could not reach the backend"** → make sure step 1 finished
  successfully and the terminal running it is still open.
- **Port 8080 or 5173 already in use** → close whatever else is using that
  port, or change the port (backend: `application.properties`, frontend:
  `vite` will usually offer an alternative port automatically — but then
  update `BASE_URL` in `frontend/src/api.js` and the allowed origin in
  `backend/src/main/java/com/ridex/config/CorsConfig.java` to match).
- **Want a clean slate** → stop the backend and delete the `backend/data/`
  folder. It will be recreated empty next time you start the backend.
