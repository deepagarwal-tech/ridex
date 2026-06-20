#!/usr/bin/env bash
#
# start.sh
#
# Starts both the Ridex backend and frontend with one command.
# Each runs in the background; logs go to backend.log and frontend.log
# in this folder. Press Ctrl+C to stop both.
#
# Usage (from the ridex-app folder):
#   ./start.sh

set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

cleanup() {
    echo ""
    echo "Stopping backend and frontend..."
    kill "$BACKEND_PID" "$FRONTEND_PID" 2>/dev/null || true
    exit 0
}
trap cleanup INT TERM

echo "Starting Ridex backend (logging to backend.log)..."
(cd "$DIR/backend" && ./run.sh > "$DIR/backend.log" 2>&1) &
BACKEND_PID=$!

echo "Waiting a few seconds for the backend to begin starting..."
sleep 5

echo "Starting Ridex frontend (logging to frontend.log)..."
(cd "$DIR/frontend" && npm run dev > "$DIR/frontend.log" 2>&1) &
FRONTEND_PID=$!

echo ""
echo "Both processes are starting."
echo "Backend:  http://localhost:8080   (log: backend.log)"
echo "Frontend: http://localhost:5173   (log: frontend.log)"
echo ""
echo "Press Ctrl+C to stop both."

wait
