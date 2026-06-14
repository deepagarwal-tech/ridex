import { useEffect, useState } from 'react';
import { api } from './api';
import CityGraphMap from './components/CityGraphMap';
import './app.css';

const SAMPLE_LOCATIONS = [
  { label: 'Central Station', lat: 28.6139, lon: 77.2090 },
  { label: 'City Mall', lat: 28.6200, lon: 77.2150 },
  { label: 'Tech Park', lat: 28.6300, lon: 77.2200 },
  { label: 'Airport Road', lat: 28.6050, lon: 77.2300 },
  { label: 'Old Town', lat: 28.6100, lon: 77.1950 },
  { label: 'University', lat: 28.6250, lon: 77.1900 },
  { label: 'Hospital Junction', lat: 28.6180, lon: 77.2050 },
  { label: 'Stadium', lat: 28.6350, lon: 77.2100 },
];

export default function App() {
  const [graph, setGraph] = useState(null);
  const [users, setUsers] = useState([]);
  const [drivers, setDrivers] = useState([]);
  const [trips, setTrips] = useState([]);

  const [currentUserId, setCurrentUserId] = useState(null);
  const [pickup, setPickup] = useState(SAMPLE_LOCATIONS[0]);
  const [drop, setDrop] = useState(SAMPLE_LOCATIONS[7]);
  const [activeTrip, setActiveTrip] = useState(null);

  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const [newUserName, setNewUserName] = useState('');
  const [newUserEmail, setNewUserEmail] = useState('');
  const [newDriverName, setNewDriverName] = useState('');
  const [newDriverVehicle, setNewDriverVehicle] = useState('');

  const refreshAll = async () => {
    try {
      const [g, u, d, t] = await Promise.all([
        api.getCityGraph(),
        api.getUsers(),
        api.getDrivers(),
        api.getTrips(),
      ]);
      setGraph(g);
      setUsers(u);
      setDrivers(d);
      setTrips(t);
      if (!currentUserId && u.length > 0) setCurrentUserId(u[0].id);
    } catch (err) {
      setError('Could not reach the backend. Is it running on http://localhost:8080?');
    }
  };

  useEffect(() => {
    refreshAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleCreateUser = async (e) => {
    e.preventDefault();
    if (!newUserName.trim() || !newUserEmail.trim()) return;
    setError(null);
    try {
      const user = await api.createUser({ name: newUserName.trim(), email: newUserEmail.trim(), phone: '' });
      setUsers((prev) => [...prev, user]);
      setCurrentUserId(user.id);
      setNewUserName('');
      setNewUserEmail('');
    } catch (err) {
      setError(err.message);
    }
  };

  const handleRegisterDriver = async (e) => {
    e.preventDefault();
    if (!newDriverName.trim() || !newDriverVehicle.trim()) return;
    setError(null);
    try {
      // Place new drivers at a random sample location so they're spread across the map
      const loc = SAMPLE_LOCATIONS[Math.floor(Math.random() * SAMPLE_LOCATIONS.length)];
      const driver = await api.registerDriver({
        name: newDriverName.trim(),
        phone: '',
        vehicleNumber: newDriverVehicle.trim(),
        currentLatitude: loc.lat,
        currentLongitude: loc.lon,
      });
      setDrivers((prev) => [...prev, driver]);
      setNewDriverName('');
      setNewDriverVehicle('');
    } catch (err) {
      setError(err.message);
    }
  };

  const handleRequestRide = async (e) => {
    e.preventDefault();
    if (!currentUserId) {
      setError('Create a rider account first.');
      return;
    }
    setError(null);
    setLoading(true);
    try {
      const trip = await api.requestRide({
        userId: currentUserId,
        pickupLatitude: pickup.lat,
        pickupLongitude: pickup.lon,
        dropLatitude: drop.lat,
        dropLongitude: drop.lon,
      });
      setActiveTrip(trip);
      await refreshAll();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleCompleteTrip = async () => {
    if (!activeTrip) return;
    setError(null);
    try {
      const trip = await api.completeTrip(activeTrip.id);
      setActiveTrip(trip);
      await refreshAll();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleCancelTrip = async () => {
    if (!activeTrip) return;
    setError(null);
    try {
      const trip = await api.cancelTrip(activeTrip.id);
      setActiveTrip(trip);
      await refreshAll();
    } catch (err) {
      setError(err.message);
    }
  };

  const availableDrivers = drivers.filter((d) => d.available);
  const pickupNode = graph?.nodes && pickup ? nearestNodeId(graph.nodes, pickup) : null;
  const dropNode = graph?.nodes && drop ? nearestNodeId(graph.nodes, drop) : null;

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="brand">
          <span className="brand-mark">⟁</span>
          <div>
            <h1>Ridex</h1>
            <p className="brand-tag">Routing console</p>
          </div>
        </div>
        <div className="header-stats">
          <Stat label="Riders" value={users.length} />
          <Stat label="Drivers free" value={`${availableDrivers.length} / ${drivers.length}`} />
          <Stat label="Trips" value={trips.length} />
        </div>
      </header>

      {error && <div className="banner banner-error">{error}</div>}

      <main className="layout">
        <section className="panel panel-controls">
          <div className="card">
            <h2>01 — Rider</h2>
            {currentUserId ? (
              <p className="muted">
                Riding as <strong>{users.find((u) => u.id === currentUserId)?.name ?? `User #${currentUserId}`}</strong>
              </p>
            ) : (
              <p className="muted">No rider selected yet.</p>
            )}

            {users.length > 0 && (
              <select
                className="select"
                value={currentUserId ?? ''}
                onChange={(e) => setCurrentUserId(Number(e.target.value))}
              >
                {users.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.name} ({u.email})
                  </option>
                ))}
              </select>
            )}

            <form onSubmit={handleCreateUser} className="inline-form">
              <input
                type="text"
                placeholder="Name"
                value={newUserName}
                onChange={(e) => setNewUserName(e.target.value)}
              />
              <input
                type="email"
                placeholder="Email"
                value={newUserEmail}
                onChange={(e) => setNewUserEmail(e.target.value)}
              />
              <button type="submit" className="btn btn-secondary">Add rider</button>
            </form>
          </div>

          <div className="card">
            <h2>02 — Drivers</h2>
            <p className="muted">
              {drivers.length === 0
                ? 'No drivers registered yet.'
                : `${availableDrivers.length} of ${drivers.length} currently available.`}
            </p>

            <form onSubmit={handleRegisterDriver} className="inline-form">
              <input
                type="text"
                placeholder="Driver name"
                value={newDriverName}
                onChange={(e) => setNewDriverName(e.target.value)}
              />
              <input
                type="text"
                placeholder="Vehicle number"
                value={newDriverVehicle}
                onChange={(e) => setNewDriverVehicle(e.target.value)}
              />
              <button type="submit" className="btn btn-secondary">Add driver</button>
            </form>
          </div>

          <div className="card">
            <h2>03 — Request a ride</h2>
            <form onSubmit={handleRequestRide} className="ride-form">
              <label>
                Pickup
                <select
                  className="select"
                  value={pickup.label}
                  onChange={(e) => setPickup(SAMPLE_LOCATIONS.find((l) => l.label === e.target.value))}
                >
                  {SAMPLE_LOCATIONS.map((loc) => (
                    <option key={loc.label} value={loc.label}>{loc.label}</option>
                  ))}
                </select>
              </label>

              <label>
                Drop-off
                <select
                  className="select"
                  value={drop.label}
                  onChange={(e) => setDrop(SAMPLE_LOCATIONS.find((l) => l.label === e.target.value))}
                >
                  {SAMPLE_LOCATIONS.map((loc) => (
                    <option key={loc.label} value={loc.label}>{loc.label}</option>
                  ))}
                </select>
              </label>

              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Finding a driver…' : 'Request ride'}
              </button>
            </form>
          </div>
        </section>

        <section className="panel panel-map">
          <div className="card card-map">
            <h2>City road network</h2>
            <CityGraphMap graph={graph} routePath={activeTrip?.routePath} pickupNode={pickupNode} dropNode={dropNode} />
          </div>

          {activeTrip && (
            <div className="card trip-card">
              <div className="trip-card-header">
                <h2>Trip #{activeTrip.id}</h2>
                <span className={`status status-${activeTrip.status.toLowerCase()}`}>{activeTrip.status}</span>
              </div>

              <dl className="trip-grid">
                <div>
                  <dt>Driver</dt>
                  <dd>{activeTrip.driver?.name} · {activeTrip.driver?.vehicleNumber}</dd>
                </div>
                <div>
                  <dt>Distance</dt>
                  <dd className="mono">{activeTrip.distanceKm.toFixed(2)} km</dd>
                </div>
                <div>
                  <dt>Fare</dt>
                  <dd className="mono">₹{activeTrip.fare.toFixed(2)}</dd>
                </div>
                <div>
                  <dt>Route</dt>
                  <dd className="mono">{activeTrip.routePath?.join(' → ') || '—'}</dd>
                </div>
              </dl>

              {activeTrip.status === 'ONGOING' && (
                <div className="trip-actions">
                  <button className="btn btn-primary" onClick={handleCompleteTrip}>Complete trip</button>
                  <button className="btn btn-ghost" onClick={handleCancelTrip}>Cancel</button>
                </div>
              )}
            </div>
          )}
        </section>
      </main>
    </div>
  );
}

function Stat({ label, value }) {
  return (
    <div className="stat">
      <span className="stat-value mono">{value}</span>
      <span className="stat-label">{label}</span>
    </div>
  );
}

function nearestNodeId(nodes, point) {
  let nearest = null;
  let nearestDist = Infinity;
  for (const node of nodes) {
    const d = (node.latitude - point.lat) ** 2 + (node.longitude - point.lon) ** 2;
    if (d < nearestDist) {
      nearestDist = d;
      nearest = node.id;
    }
  }
  return nearest;
}
