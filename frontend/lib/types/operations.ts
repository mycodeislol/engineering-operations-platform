export interface Deployment {
  id: string;
  serviceId: string;
  envId: string;
  status: string;
  updatedAt?: string;
  startedAt?: string;
  completedAt?: string;
  failureReason?: string;
}

export interface Incident {
  id: string;
  serviceId: string;
  envId: string;
  severity: 'SEV1' | 'SEV2' | 'SEV3' | string;
  status: string;
  summary: string;
  createdAt: string;
  deployId?: string;
  title?: string;
}

export interface IncidentFilterParams {
  serviceId?: string;
  envId?: string;
  severity?: string;
}
