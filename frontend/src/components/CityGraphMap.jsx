import { useMemo } from 'react';

// Project lat/lon onto a simple SVG canvas using min/max bounds.
function useProjection(nodes, width, height, padding) {
  return useMemo(() => {
    if (!nodes || nodes.length === 0) return () => [0, 0];

    const lats = nodes.map((n) => n.latitude);
    const lons = nodes.map((n) => n.longitude);
    const minLat = Math.min(...lats);
    const maxLat = Math.max(...lats);
    const minLon = Math.min(...lons);
    const maxLon = Math.max(...lons);

    const latRange = maxLat - minLat || 1;
    const lonRange = maxLon - minLon || 1;

    return (lat, lon) => {
      const x = padding + ((lon - minLon) / lonRange) * (width - padding * 2);
      // Invert latitude so north is up
      const y = height - padding - ((lat - minLat) / latRange) * (height - padding * 2);
      return [x, y];
    };
  }, [nodes, width, height, padding]);
}

export default function CityGraphMap({ graph, routePath, pickupNode, dropNode }) {
  const width = 520;
  const height = 380;
  const padding = 48;

  const project = useProjection(graph?.nodes, width, height, padding);

  if (!graph || graph.nodes.length === 0) {
    return (
      <div className="map-empty">
        <p>Loading city map…</p>
      </div>
    );
  }

  const nodeById = Object.fromEntries(graph.nodes.map((n) => [n.id, n]));

  const routeEdges = [];
  if (routePath && routePath.length > 1) {
    for (let i = 0; i < routePath.length - 1; i++) {
      routeEdges.push([routePath[i], routePath[i + 1]]);
    }
  }

  const isRouteEdge = (a, b) =>
    routeEdges.some(([x, y]) => (x === a && y === b) || (x === b && y === a));

  return (
    <svg
      className="city-map"
      viewBox={`0 0 ${width} ${height}`}
      role="img"
      aria-label="City road network with current route highlighted"
    >
      {/* base roads */}
      {graph.edges.map((edge, i) => {
        const from = nodeById[edge.from];
        const to = nodeById[edge.to];
        if (!from || !to) return null;
        const [x1, y1] = project(from.latitude, from.longitude);
        const [x2, y2] = project(to.latitude, to.longitude);
        const onRoute = isRouteEdge(edge.from, edge.to);

        return (
          <line
            key={`edge-${i}`}
            x1={x1}
            y1={y1}
            x2={x2}
            y2={y2}
            className={onRoute ? 'road road-active' : 'road'}
          />
        );
      })}

      {/* nodes */}
      {graph.nodes.map((node) => {
        const [x, y] = project(node.latitude, node.longitude);
        const onRoute = routePath?.includes(node.id);
        const isPickup = node.id === pickupNode;
        const isDrop = node.id === dropNode;

        let nodeClass = 'node';
        if (onRoute) nodeClass += ' node-active';
        if (isPickup) nodeClass += ' node-pickup';
        if (isDrop) nodeClass += ' node-drop';

        return (
          <g key={node.id} className="node-group">
            <circle cx={x} cy={y} r={isPickup || isDrop ? 8 : 5} className={nodeClass} />
            <text x={x} y={y - 12} className="node-label" textAnchor="middle">
              {node.name}
            </text>
          </g>
        );
      })}
    </svg>
  );
}
