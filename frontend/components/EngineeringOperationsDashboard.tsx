"use client";

import React, { useState, useEffect, useCallback } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  Activity,
  AlertTriangle,
  CheckCircle2,
  Clock,
  ExternalLink,
  GitCommit,
  Layers,
  RefreshCw,
  Server,
  ShieldAlert,
  Terminal,
  X,
  XCircle,
  Zap,
} from "lucide-react";

// --- Types & Domain Interfaces ---
export type DataState = "loading" | "empty" | "error" | "success";

export interface ServiceItem {
  id: string;
  name: string;
  projectKey: string;
  tier: "Tier-1" | "Tier-2" | "Tier-3";
  language: string;
  environments: Array<{ name: string; health: "healthy" | "degraded" | "unhealthy" }>;
  upstreamCount: number;
  downstreamCount: number;
}

export interface DeploymentRecord {
  id: string;
  serviceName: string;
  environmentName: string;
  commitSha: string;
  commitMessage: string;
  status: "QUEUED" | "RUNNING" | "SUCCESS" | "FAILED";
  relativeTime: string;
  durationMs: number;
  failureReason?: string;
  linkedIncidentId?: string;
}

export interface ApiIncidentResponse {
  id: string;
  serviceId?: string;
  envId?: string;
  deployId?: string;
  severity?: string;
  status?: string;
  title?: string;
  summary?: string;
  createdAt: string;
}

export interface ApiDeploymentResponse {
  id: string;
  buildId?: string;
  serviceId?: string;
  envId?: string;
  status: string;
  failureReason?: string;
  startedAt: string;
  completedAt?: string;
}

// --- Helper Functions ---
function formatRelativeTime(isoString?: string): string {
  if (!isoString) return "recently";
  try {
    const diffMs = Date.now() - new Date(isoString).getTime();
    const diffSec = Math.floor(diffMs / 1000);
    if (diffSec < 60) return `${Math.max(1, diffSec)}s ago`;
    const diffMin = Math.floor(diffSec / 60);
    if (diffMin < 60) return `${diffMin}m ago`;
    const diffHour = Math.floor(diffMin / 60);
    if (diffHour < 24) return `${diffHour}h ago`;
    const diffDays = Math.floor(diffHour / 24);
    return `${diffDays}d ago`;
  } catch {
    return "recently";
  }
}

// --- Mock Dataset for Phase 0/1 MVP Workflow Fallback ---
const MOCK_SERVICES: ServiceItem[] = [
  {
    id: "srv-payment-001",
    name: "payment-service",
    projectKey: "FINTECH",
    tier: "Tier-1",
    language: "Java 21",
    environments: [
      { name: "Production", health: "degraded" },
      { name: "Staging", health: "healthy" },
    ],
    upstreamCount: 4,
    downstreamCount: 9,
  },
  {
    id: "srv-auth-002",
    name: "identity-service",
    projectKey: "SEC",
    tier: "Tier-1",
    language: "Go",
    environments: [
      { name: "Production", health: "healthy" },
      { name: "Staging", health: "healthy" },
    ],
    upstreamCount: 1,
    downstreamCount: 18,
  },
  {
    id: "srv-order-003",
    name: "order-dispatch-service",
    projectKey: "LOGISTICS",
    tier: "Tier-2",
    language: "TypeScript",
    environments: [{ name: "Production", health: "healthy" }],
    upstreamCount: 2,
    downstreamCount: 5,
  },
];

const MOCK_DEPLOYMENTS: DeploymentRecord[] = [
  {
    id: "dep-9481-fail",
    serviceName: "payment-service",
    environmentName: "Production",
    commitSha: "8a4c1f9",
    commitMessage: "feat: add stripe webhook idempotency lock",
    status: "FAILED",
    relativeTime: "4 mins ago",
    durationMs: 42300,
    failureReason: "Database migration constraint violation on table 'payment_events': Foreign key 'fk_org_id' constraint broken.",
    linkedIncidentId: "INC-SEV2-8802",
  },
  {
    id: "dep-9480-run",
    serviceName: "order-dispatch-service",
    environmentName: "Production",
    commitSha: "1f33b7e",
    commitMessage: "refactor: optimize batch dispatch queue processing",
    status: "RUNNING",
    relativeTime: "12 mins ago",
    durationMs: 64100,
  },
  {
    id: "dep-9479-suc",
    serviceName: "identity-service",
    environmentName: "Production",
    commitSha: "5c88b0a",
    commitMessage: "chore: bump security token refresh TTL to 7200s",
    status: "SUCCESS",
    relativeTime: "1 hour ago",
    durationMs: 112000,
  },
  {
    id: "dep-9478-que",
    serviceName: "payment-service",
    environmentName: "Staging",
    commitSha: "d9e4a11",
    commitMessage: "test: mock payment processor gateway circuit breaker",
    status: "QUEUED",
    relativeTime: "2 hours ago",
    durationMs: 0,
  },
];

export default function EngineeringOperationsDashboard() {
  const [dataState, setDataState] = useState<DataState>("success");
  const [selectedDeployment, setSelectedDeployment] = useState<DeploymentRecord | null>(null);

  // Live Backend State
  const [deployments, setDeployments] = useState<DeploymentRecord[]>(MOCK_DEPLOYMENTS);
  const [incidents, setIncidents] = useState<ApiIncidentResponse[]>([]);
  const [isLiveConnected, setIsLiveConnected] = useState<boolean>(false);
  const [isLoadingApi, setIsLoadingApi] = useState<boolean>(false);

  const fetchOperationsData = useCallback(async () => {
    setIsLoadingApi(true);
    try {
      const [depRes, incRes] = await Promise.all([
        fetch("/api/v1/operations/deployments"),
        fetch("/api/v1/operations/incidents"),
      ]);

      if (depRes.ok && incRes.ok) {
        const liveDeployments: ApiDeploymentResponse[] = await depRes.json();
        const liveIncidents: ApiIncidentResponse[] = await incRes.json();

        setIncidents(liveIncidents);
        setIsLiveConnected(true);

        if (Array.isArray(liveDeployments) && liveDeployments.length > 0) {
          const mapped: DeploymentRecord[] = liveDeployments.map((d) => {
            const matchedIncident = liveIncidents.find((inc) => inc.deployId === d.id);
            const matchedService = MOCK_SERVICES.find((s) => s.id === d.serviceId);
            const serviceName =
              matchedService?.name ||
              (d.serviceId ? `service-${d.serviceId.slice(0, 8)}` : "payment-service");
            const durationMs =
              d.completedAt && d.startedAt
                ? Math.max(0, new Date(d.completedAt).getTime() - new Date(d.startedAt).getTime())
                : d.startedAt
                ? Math.max(0, Date.now() - new Date(d.startedAt).getTime())
                : 0;

            const normalizedStatus = (
              d.status?.toUpperCase() || "QUEUED"
            ) as DeploymentRecord["status"];

            return {
              id: d.id,
              serviceName,
              environmentName: "Production",
              commitSha: d.buildId ? d.buildId.slice(0, 7) : "8a4c1f9",
              commitMessage: d.failureReason
                ? `Pipeline Failure: ${d.failureReason.slice(0, 50)}...`
                : `CI/CD automated release build`,
              status: normalizedStatus,
              relativeTime: formatRelativeTime(d.startedAt),
              durationMs,
              failureReason: d.failureReason,
              linkedIncidentId: matchedIncident
                ? `INC-${matchedIncident.severity || "SEV2"}-${matchedIncident.id.slice(0, 4)}`
                : undefined,
            };
          });
          setDeployments(mapped);
        } else {
          setDeployments(MOCK_DEPLOYMENTS);
        }
      } else {
        setIsLiveConnected(false);
      }
    } catch {
      // Fall back gracefully to mock telemetry cache
      setIsLiveConnected(false);
    } finally {
      setIsLoadingApi(false);
    }
  }, []);

  useEffect(() => {
    fetchOperationsData();
  }, [fetchOperationsData]);

  // Derive active critical incident
  const activeIncident =
    incidents.find(
      (inc) =>
        inc.status !== "RESOLVED" &&
        (inc.severity === "SEV_2" ||
          inc.severity === "SEV2" ||
          inc.severity === "SEV_1" ||
          inc.severity === "SEV1")
    ) || (incidents.length > 0 ? incidents[0] : null);

  const activeDeploymentsCount = deployments.filter((d) => d.status === "RUNNING").length;
  const failedDeploymentsCount = deployments.filter((d) => d.status === "FAILED").length;

  return (
    <div className="min-h-screen bg-[#08090C] text-slate-100 p-6 md:p-10 relative overflow-hidden bg-grid-pattern">
      {/* Top Controls / Realism State Switcher */}
      <header className="max-w-7xl mx-auto mb-8 flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-white/5 pb-6">
        <div>
          <div className="flex items-center gap-3">
            <div
              className={`h-2.5 w-2.5 rounded-full ${
                isLiveConnected
                  ? "bg-emerald-400 shadow-[0_0_8px_#34d399] animate-pulse"
                  : "bg-cyan-400 shadow-[0_0_8px_#22d3ee]"
              }`}
            />
            <span className="text-xs font-mono uppercase tracking-wideBadge text-slate-400">
              Operations Control Plane
            </span>
            <span className="text-[11px] font-mono px-2 py-0.5 rounded-full bg-white/5 border border-white/10 text-slate-400 flex items-center gap-1.5">
              <span
                className={`h-1.5 w-1.5 rounded-full ${
                  isLiveConnected ? "bg-emerald-400" : "bg-amber-400"
                }`}
              />
              {isLiveConnected ? "Live API (8080)" : "Mock Pipeline Data"}
              <button
                onClick={fetchOperationsData}
                disabled={isLoadingApi}
                title="Refresh from /api/v1/operations"
                className="hover:text-white transition-colors cursor-pointer ml-1"
              >
                <RefreshCw className={`w-3 h-3 ${isLoadingApi ? "animate-spin text-indigo-400" : ""}`} />
              </button>
            </span>
          </div>
          <h1 className="text-2xl md:text-3xl font-semibold tracking-tightest mt-1 text-white">
            Engineering Operations MVP
          </h1>
        </div>

        {/* State Toggle Bar for UI Demonstration */}
        <div className="flex items-center bg-[#10131B] border border-white/10 rounded-lg p-1 text-xs">
          <span className="px-2.5 py-1 text-slate-500 font-mono">View State:</span>
          {(["success", "loading", "empty", "error"] as DataState[]).map((state) => (
            <button
              key={state}
              onClick={() => setDataState(state)}
              className={`px-3 py-1 rounded-md capitalize transition-all font-medium cursor-pointer ${
                dataState === state
                  ? "bg-indigo-600 text-white shadow-sm"
                  : "text-slate-400 hover:text-white"
              }`}
            >
              {state}
            </button>
          ))}
        </div>
      </header>

      {/* Main Content Area Rendering Given State */}
      <main className="max-w-7xl mx-auto">
        {dataState === "loading" && <LoadingSkeletonState />}
        {dataState === "empty" && <EmptyDataState onRetry={() => setDataState("success")} />}
        {dataState === "error" && <ErrorDataState onRetry={() => setDataState("success")} />}
        {dataState === "success" && (
          <div className="space-y-6">
            {/* Active Incident Banner Alert */}
            {activeIncident ? (
              <motion.div
                initial={{ opacity: 0, y: -8 }}
                animate={{ opacity: 1, y: 0 }}
                className="p-4 rounded-xl border border-rose-500/30 bg-rose-500/10 backdrop-blur-md flex items-center justify-between shadow-incident-pulse"
              >
                <div className="flex items-center gap-3.5">
                  <div className="p-2 rounded-lg bg-rose-500/20 text-rose-400 border border-rose-500/30">
                    <ShieldAlert className="w-5 h-5 animate-pulse" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-semibold uppercase tracking-wideBadge px-2 py-0.5 rounded bg-rose-500/30 text-rose-200 border border-rose-500/40">
                        {activeIncident.severity ? `${activeIncident.severity.replace("_", "-")} Incident Triggered` : "Sev-2 Incident Triggered"}
                      </span>
                      <span className="text-xs text-rose-300/70 font-mono">
                        #INC-{activeIncident.severity?.replace("_", "") || "SEV2"}-{activeIncident.id.slice(0, 8)}
                      </span>
                    </div>
                    <p className="text-sm text-slate-200 mt-1">
                      {activeIncident.summary || (
                        <>
                          Automated operation: Deployment failure in{" "}
                          <span className="font-semibold text-white">monitored production service</span>.
                        </>
                      )}
                    </p>
                  </div>
                </div>
                <button
                  onClick={() => {
                    const linked = deployments.find(
                      (d) =>
                        d.id === activeIncident.deployId ||
                        d.linkedIncidentId?.includes(activeIncident.id.slice(0, 4))
                    );
                    setSelectedDeployment(linked || deployments[0]);
                  }}
                  className="px-3.5 py-1.5 rounded-lg bg-rose-600 hover:bg-rose-500 text-white text-xs font-medium transition-all shadow-md active:scale-95 cursor-pointer shrink-0"
                >
                  Inspect Incident
                </button>
              </motion.div>
            ) : (
              <motion.div
                initial={{ opacity: 0, y: -8 }}
                animate={{ opacity: 1, y: 0 }}
                className="p-4 rounded-xl border border-rose-500/30 bg-rose-500/10 backdrop-blur-md flex items-center justify-between shadow-incident-pulse"
              >
                <div className="flex items-center gap-3.5">
                  <div className="p-2 rounded-lg bg-rose-500/20 text-rose-400 border border-rose-500/30">
                    <ShieldAlert className="w-5 h-5 animate-pulse" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-semibold uppercase tracking-wideBadge px-2 py-0.5 rounded bg-rose-500/30 text-rose-200 border border-rose-500/40">
                        Sev-2 Incident Triggered
                      </span>
                      <span className="text-xs text-rose-300/70 font-mono">#INC-SEV2-8802</span>
                    </div>
                    <p className="text-sm text-slate-200 mt-1">
                      Automated operation: Deployment #dep-9481-fail failed in{" "}
                      <span className="font-semibold text-white">payment-service (Production)</span>.
                    </p>
                  </div>
                </div>
                <button
                  onClick={() => setSelectedDeployment(deployments[0])}
                  className="px-3.5 py-1.5 rounded-lg bg-rose-600 hover:bg-rose-500 text-white text-xs font-medium transition-all shadow-md active:scale-95 cursor-pointer shrink-0"
                >
                  Inspect Incident
                </button>
              </motion.div>
            )}

            {/* Bento Grid Layout */}
            <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-5">
              {/* Bento Tile 1: Top Metrics */}
              <div className="md:col-span-3 lg:col-span-4 grid grid-cols-2 md:grid-cols-4 gap-4">
                <MetricCard
                  label="Registered Services"
                  value={String(MOCK_SERVICES.length)}
                  sublabel="Across Monitored Projects"
                  icon={<Server className="w-4 h-4 text-indigo-400" />}
                />
                <MetricCard
                  label="Active Deployments"
                  value={String(activeDeploymentsCount)}
                  sublabel="In Execution Pipeline"
                  icon={<Activity className="w-4 h-4 text-cyan-400" />}
                />
                <MetricCard
                  label="Failed Deployments"
                  value={String(failedDeploymentsCount)}
                  sublabel="Auto-generated SEV-2"
                  alert={failedDeploymentsCount > 0}
                  icon={<AlertTriangle className="w-4 h-4 text-rose-400" />}
                />
                <MetricCard
                  label="Incident MTTR"
                  value="18m"
                  sublabel="-42% vs previous week"
                  icon={<Zap className="w-4 h-4 text-emerald-400" />}
                />
              </div>

              {/* Bento Tile 2: Service Catalog (2 Cols on lg) */}
              <motion.div
                layout
                className="lg:col-span-2 glass-panel rounded-2xl p-5 flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center gap-2">
                      <Layers className="w-4 h-4 text-slate-400" />
                      <h2 className="text-base font-semibold text-white tracking-tight">
                        Service Catalog
                      </h2>
                    </div>
                    <span className="text-xs font-mono text-slate-400 bg-white/5 px-2 py-0.5 rounded border border-white/5">
                      {MOCK_SERVICES.length} Active Monitored
                    </span>
                  </div>

                  <div className="space-y-2.5">
                    {MOCK_SERVICES.map((svc) => (
                      <ServiceCatalogCard key={svc.id} service={svc} />
                    ))}
                  </div>
                </div>
                <div className="pt-4 mt-4 border-t border-white/5 flex items-center justify-between text-xs text-slate-400">
                  <span>Topology: Strict Relational FK Enforced</span>
                  <button className="text-indigo-400 hover:text-indigo-300 flex items-center gap-1 transition-colors cursor-pointer">
                    Manage Topology <ExternalLink className="w-3 h-3" />
                  </button>
                </div>
              </motion.div>

              {/* Bento Tile 3: Deployment Lifecycle Feed (2 Cols on lg) */}
              <motion.div
                layout
                className="lg:col-span-2 glass-panel rounded-2xl p-5 flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center gap-2">
                      <Terminal className="w-4 h-4 text-slate-400" />
                      <h2 className="text-base font-semibold text-white tracking-tight">
                        Deployment Activity
                      </h2>
                    </div>
                    <span className="text-xs font-mono text-slate-400 bg-white/5 px-2 py-0.5 rounded border border-white/5">
                      Live State Machine
                    </span>
                  </div>

                  <div className="space-y-2.5">
                    {deployments.map((deploy) => (
                      <DeploymentRowCard
                        key={deploy.id}
                        deployment={deploy}
                        onInspect={() => setSelectedDeployment(deploy)}
                      />
                    ))}
                  </div>
                </div>
                <div className="pt-4 mt-4 border-t border-white/5 flex items-center justify-between text-xs text-slate-400">
                  <span>Synchronous Spring Event Pipeline</span>
                  <span className="text-slate-500 font-mono">
                    {isLiveConnected ? "REST Connected" : "Local Telemetry Mode"}
                  </span>
                </div>
              </motion.div>
            </div>
          </div>
        )}
      </main>

      {/* Non-Modal Inspection Drawer (Side Sheet) */}
      <AnimatePresence>
        {selectedDeployment && (
          <DeploymentDetailDrawer
            deployment={selectedDeployment}
            onClose={() => setSelectedDeployment(null)}
          />
        )}
      </AnimatePresence>
    </div>
  );
}

// --- Subcomponents ---

function MetricCard({
  label,
  value,
  sublabel,
  icon,
  alert = false,
}: {
  label: string;
  value: string;
  sublabel: string;
  icon: React.ReactNode;
  alert?: boolean;
}) {
  return (
    <div
      className={`glass-panel p-4 rounded-xl relative overflow-hidden transition-all ${
        alert ? "border-rose-500/30" : ""
      }`}
    >
      <div className="flex items-center justify-between">
        <span className="text-xs text-slate-400 font-medium tracking-tight">{label}</span>
        {icon}
      </div>
      <div className="mt-2 text-2xl font-bold tracking-tight text-white">{value}</div>
      <div className="text-[11px] text-slate-500 mt-0.5">{sublabel}</div>
    </div>
  );
}

function ServiceCatalogCard({ service }: { service: ServiceItem }) {
  return (
    <div className="p-3 rounded-xl bg-[#12151D] border border-white/5 hover:border-white/10 transition-all flex items-center justify-between">
      <div className="flex items-center gap-3">
        <div className="h-8 w-8 rounded-lg bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400 font-mono text-xs font-semibold">
          {service.projectKey.slice(0, 2)}
        </div>
        <div>
          <div className="flex items-center gap-2">
            <span className="text-sm font-medium text-slate-100">{service.name}</span>
            <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-white/5 text-slate-400 border border-white/5">
              {service.tier}
            </span>
          </div>
          <div className="flex items-center gap-2 text-[11px] text-slate-400 mt-0.5">
            <span>{service.language}</span>
            <span>•</span>
            <span>{service.downstreamCount} downstream dependencies</span>
          </div>
        </div>
      </div>

      <div className="flex items-center gap-2">
        {service.environments.map((env) => (
          <span
            key={env.name}
            className={`text-[11px] font-mono px-2 py-0.5 rounded border ${
              env.health === "healthy"
                ? "bg-emerald-500/10 text-emerald-300 border-emerald-500/20"
                : "bg-rose-500/10 text-rose-300 border-rose-500/20"
            }`}
          >
            {env.name}
          </span>
        ))}
      </div>
    </div>
  );
}

function DeploymentRowCard({
  deployment,
  onInspect,
}: {
  deployment: DeploymentRecord;
  onInspect: () => void;
}) {
  const getBadgeStyle = (status: DeploymentRecord["status"]) => {
    switch (status) {
      case "SUCCESS":
        return "bg-emerald-500/10 text-emerald-400 border-emerald-500/20";
      case "FAILED":
        return "bg-rose-500/10 text-rose-400 border-rose-500/30 font-semibold";
      case "RUNNING":
        return "bg-cyan-500/10 text-cyan-400 border-cyan-500/20 animate-pulse";
      case "QUEUED":
        return "bg-slate-500/10 text-slate-400 border-slate-500/20";
    }
  };

  const getStatusIcon = (status: DeploymentRecord["status"]) => {
    switch (status) {
      case "SUCCESS":
        return <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />;
      case "FAILED":
        return <XCircle className="w-3.5 h-3.5 text-rose-400" />;
      case "RUNNING":
        return <RefreshCw className="w-3.5 h-3.5 text-cyan-400 animate-spin" />;
      case "QUEUED":
        return <Clock className="w-3.5 h-3.5 text-slate-400" />;
    }
  };

  return (
    <div className="p-3 rounded-xl bg-[#12151D] border border-white/5 hover:border-indigo-500/30 transition-all flex items-center justify-between">
      <div className="flex items-center gap-3">
        <div className="mt-0.5">{getStatusIcon(deployment.status)}</div>
        <div>
          <div className="flex items-center gap-2">
            <span className="text-sm font-medium text-slate-100">{deployment.serviceName}</span>
            <span className="text-[10px] font-mono text-slate-500">[{deployment.environmentName}]</span>
          </div>
          <div className="flex items-center gap-2 text-[11px] text-slate-400 mt-0.5">
            <span className="flex items-center gap-1 font-mono">
              <GitCommit className="w-3 h-3 text-slate-500" />
              {deployment.commitSha}
            </span>
            <span>•</span>
            <span className="truncate max-w-[200px] text-slate-400">{deployment.commitMessage}</span>
          </div>
        </div>
      </div>

      <div className="flex items-center gap-3">
        <span
          className={`text-xs font-mono uppercase px-2 py-0.5 rounded border ${getBadgeStyle(
            deployment.status
          )}`}
        >
          {deployment.status}
        </span>
        <button
          onClick={onInspect}
          className="text-xs px-2.5 py-1 rounded bg-white/5 hover:bg-white/10 text-slate-300 hover:text-white transition-all border border-white/5 cursor-pointer"
        >
          Details
        </button>
      </div>
    </div>
  );
}

function DeploymentDetailDrawer({
  deployment,
  onClose,
}: {
  deployment: DeploymentRecord;
  onClose: () => void;
}) {
  return (
    <>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        onClick={onClose}
        className="fixed inset-0 bg-black/60 backdrop-blur-sm z-40"
      />

      <motion.aside
        initial={{ x: "100%" }}
        animate={{ x: 0 }}
        exit={{ x: "100%" }}
        transition={{ type: "spring", stiffness: 350, damping: 30 }}
        className="fixed top-0 right-0 bottom-0 w-full max-w-xl bg-[#0E1015] border-l border-white/10 p-6 z-50 overflow-y-auto shadow-2xl flex flex-col justify-between"
      >
        <div>
          <div className="flex items-center justify-between border-b border-white/10 pb-4">
            <div className="flex items-center gap-2.5">
              <div
                className={`p-1.5 rounded-md ${
                  deployment.status === "FAILED"
                    ? "bg-rose-500/20 text-rose-400"
                    : "bg-emerald-500/20 text-emerald-400"
                }`}
              >
                <Terminal className="w-4 h-4" />
              </div>
              <div>
                <h3 className="text-base font-semibold text-white">Deployment Diagnostics</h3>
                <span className="text-xs font-mono text-slate-400">{deployment.id}</span>
              </div>
            </div>
            <button
              onClick={onClose}
              className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-white/5 transition-all cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Deployment Attributes */}
          <div className="grid grid-cols-2 gap-3 my-5 text-xs">
            <div className="p-3 rounded-lg bg-white/5 border border-white/5">
              <span className="text-slate-400">Target Service</span>
              <p className="font-semibold text-white mt-0.5">{deployment.serviceName}</p>
            </div>
            <div className="p-3 rounded-lg bg-white/5 border border-white/5">
              <span className="text-slate-400">Environment</span>
              <p className="font-semibold text-white mt-0.5">{deployment.environmentName}</p>
            </div>
            <div className="p-3 rounded-lg bg-white/5 border border-white/5">
              <span className="text-slate-400">Commit SHA</span>
              <p className="font-mono text-indigo-300 mt-0.5">{deployment.commitSha}</p>
            </div>
            <div className="p-3 rounded-lg bg-white/5 border border-white/5">
              <span className="text-slate-400">Duration</span>
              <p className="font-mono text-slate-200 mt-0.5">{deployment.durationMs / 1000}s</p>
            </div>
          </div>

          {/* Incident Association Card */}
          {deployment.linkedIncidentId && (
            <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/30 mb-5">
              <div className="flex items-center gap-2 text-rose-400 font-semibold text-xs uppercase tracking-wide">
                <ShieldAlert className="w-4 h-4" />
                Linked SEV-2 Incident Generated
              </div>
              <p className="text-xs text-slate-300 mt-1">
                Incident <span className="font-mono font-bold text-white">{deployment.linkedIncidentId}</span> was
                automatically initialized via synchronous internal application event.
              </p>
              <div className="mt-3 flex items-center gap-2">
                <span className="text-[10px] px-2 py-0.5 rounded bg-rose-500/20 text-rose-300 border border-rose-500/30">
                  Status: OPEN
                </span>
                <span className="text-[10px] px-2 py-0.5 rounded bg-white/10 text-slate-300">
                  Assignee: On-Call SRE
                </span>
              </div>
            </div>
          )}

          {/* Diagnostic Log Console */}
          <div>
            <span className="text-xs font-medium text-slate-400 uppercase tracking-wider">
              Failure Log Snippet
            </span>
            <div className="mt-2 p-4 rounded-xl bg-black/80 border border-white/10 font-mono text-xs text-rose-300 overflow-x-auto leading-relaxed">
              <p className="text-slate-500">
                [Telemetry Log] Deployment status: {deployment.status}
              </p>
              <p className="text-rose-400 mt-2 font-semibold">
                {deployment.failureReason ||
                  "org.postgresql.util.PSQLException: ERROR: insert or update on table 'deployments' violates foreign key constraint 'fk_deploy_service'"}
              </p>
              <p className="text-slate-400 mt-1 pl-4">
                Detail: Key (service_id)=({deployment.serviceName}) is referenced from partition table.
              </p>
              {deployment.linkedIncidentId && (
                <p className="text-amber-400 mt-2">
                  [AUTOMATION] Triggered AutomatedIncidentCreationService: Linked {deployment.linkedIncidentId}
                </p>
              )}
            </div>
          </div>
        </div>

        {/* Footer Actions */}
        <div className="pt-4 border-t border-white/10 flex items-center justify-end gap-3">
          <button
            onClick={onClose}
            className="px-4 py-2 rounded-lg bg-white/5 hover:bg-white/10 text-slate-300 text-xs font-medium transition-all cursor-pointer"
          >
            Dismiss
          </button>
          <button className="px-4 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-medium transition-all shadow-md cursor-pointer">
            Open Runbook in AI Assistant
          </button>
        </div>
      </motion.aside>
    </>
  );
}

// --- Empty, Loading & Error States ---

function LoadingSkeletonState() {
  return (
    <div className="space-y-6 animate-pulse">
      <div className="h-14 rounded-xl bg-white/5 border border-white/5" />
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="h-24 rounded-xl bg-white/5 border border-white/5" />
        ))}
      </div>
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        <div className="h-64 rounded-2xl bg-white/5 border border-white/5" />
        <div className="h-64 rounded-2xl bg-white/5 border border-white/5" />
      </div>
    </div>
  );
}

function EmptyDataState({ onRetry }: { onRetry: () => void }) {
  return (
    <div className="glass-panel rounded-2xl p-12 text-center max-w-lg mx-auto my-12">
      <div className="h-12 w-12 rounded-full bg-white/5 border border-white/10 flex items-center justify-center mx-auto text-slate-400 mb-4">
        <Layers className="w-6 h-6" />
      </div>
      <h3 className="text-lg font-semibold text-white">No Monitored Topology Found</h3>
      <p className="text-xs text-slate-400 mt-2 leading-relaxed">
        Your organization workspace is connected, but no services or CI/CD delivery pipelines have
        reported telemetry yet.
      </p>
      <button
        onClick={onRetry}
        className="mt-6 px-4 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-medium transition-all shadow-lg cursor-pointer"
      >
        Bootstrap Demo Topology
      </button>
    </div>
  );
}

function ErrorDataState({ onRetry }: { onRetry: () => void }) {
  return (
    <div className="p-8 rounded-2xl border border-rose-500/30 bg-rose-500/10 backdrop-blur-md max-w-md mx-auto my-12 text-center">
      <div className="h-12 w-12 rounded-full bg-rose-500/20 border border-rose-500/30 flex items-center justify-center mx-auto text-rose-400 mb-4">
        <AlertTriangle className="w-6 h-6" />
      </div>
      <h3 className="text-lg font-semibold text-white">Control Plane Disconnected</h3>
      <p className="text-xs text-rose-300/80 mt-2">
        Failed to fetch real-time state from Spring Boot modular backend:{" "}
        <span className="font-mono text-white">503 Service Unavailable</span>.
      </p>
      <button
        onClick={onRetry}
        className="mt-6 px-4 py-2 rounded-lg bg-rose-600 hover:bg-rose-500 text-white text-xs font-medium transition-all shadow-md cursor-pointer"
      >
        Reconnect Telemetry
      </button>
    </div>
  );
}
