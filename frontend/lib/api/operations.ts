import { Incident, IncidentFilterParams, Deployment } from '../types/operations';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export async function fetchIncidents(filters?: IncidentFilterParams): Promise<Incident[]> {
  const params = new URLSearchParams();
  if (filters?.serviceId) params.append('serviceId', filters.serviceId);
  if (filters?.envId) params.append('envId', filters.envId);
  if (filters?.severity) params.append('severity', filters.severity);

  const query = params.toString() ? `?${params.toString()}` : '';
  const res = await fetch(`${API_URL}/api/v1/operations/incidents${query}`, {
    cache: 'no-store',
  });

  if (!res.ok) {
    throw new Error(`Error fetching incidents: ${res.statusText}`);
  }
  return res.json();
}

export async function fetchDeployments(): Promise<Deployment[]> {
  const res = await fetch(`${API_URL}/api/v1/operations/deployments`, {
    cache: 'no-store',
  });

  if (!res.ok) {
    throw new Error(`Error fetching deployments: ${res.statusText}`);
  }
  return res.json();
}

export async function updateDeploymentStatus(
  id: string,
  status: string,
  triggeredBy: string
): Promise<void> {
  const res = await fetch(`${API_URL}/api/v1/operations/deployments/${id}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status, triggeredBy }),
  });

  if (!res.ok) {
    const errorBody = await res.json().catch(() => ({}));
    throw new Error(errorBody.detail || 'Failed to update deployment status');
  }
}
