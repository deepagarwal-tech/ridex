const BASE_URL = 'http://localhost:8080/api';

async function request(path, options = {}) {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });

  if (!response.ok) {
    const text = await response.text();
    let message = text;
    try {
      const json = JSON.parse(text);
      message = json.message || json.error || text;
    } catch {
      // not JSON, use raw text
    }
    throw new Error(message || `Request failed with status ${response.status}`);
  }

  if (response.status === 204) return null;
  return response.json();
}

export const api = {
  createUser: (user) => request('/users', { method: 'POST', body: JSON.stringify(user) }),
  getUsers: () => request('/users'),

  registerDriver: (driver) => request('/drivers', { method: 'POST', body: JSON.stringify(driver) }),
  getDrivers: () => request('/drivers'),
  setDriverAvailability: (id, available) =>
    request(`/drivers/${id}/availability`, { method: 'PUT', body: JSON.stringify({ available }) }),

  requestRide: (payload) => request('/rides/request', { method: 'POST', body: JSON.stringify(payload) }),
  getTrips: () => request('/rides'),
  getTripsForUser: (userId) => request(`/rides/user/${userId}`),
  completeTrip: (id) => request(`/rides/${id}/complete`, { method: 'PUT' }),
  cancelTrip: (id) => request(`/rides/${id}/cancel`, { method: 'PUT' }),

  getCityGraph: () => request('/city-graph'),
};
