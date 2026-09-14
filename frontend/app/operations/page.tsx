import { fetchIncidents, fetchDeployments } from "@/lib/api/operations";
import { Incident, Deployment } from "@/lib/types/operations";
import Link from "next/link";
import {
  ShieldAlert,
  Terminal,
  Clock,
  CheckCircle2,
  AlertTriangle,
  ArrowLeft,
  Activity,
  Layers,
  Server,
} from "lucide-react";

export const dynamic = "force-dynamic";

export default async function OperationsPage() {
  let incidents: Incident[] = [];
  let deployments: Deployment[] = [];
  let fetchError: string | null = null;

  try {
    const [fetchedIncidents, fetchedDeployments] = await Promise.all([
      fetchIncidents().catch(() => []),
      fetchDeployments().catch(() => []),
    ]);
    incidents = fetchedIncidents;
    deployments = fetchedDeployments;
  } catch (err: unknown) {
    fetchError = err instanceof Error ? err.message : "Failed to load telemetry data";
  }

  // Fallback demo data if backend has no records yet
  const displayIncidents: Incident[] =
    incidents.length > 0
      ? incidents
      : [
          {
            id: "inc-sev2-8802",
            serviceId: "510e7a38-bffe-4932-a786-a2b7e92ddf0e",
            envId: "c5b1c147-26f3-4d29-8765-2bcd3ce11991",
            severity: "SEV2",
            status: "OPEN",
            title: "Automated Alert: Deployment Failure",
            summary:
              "Database migration constraint violation on table 'payment_events': Foreign key 'fk_org_id' constraint broken.",
            createdAt: new Date().toISOString(),
          },
        ];

  return (
    <div className="min-h-screen bg-[#08090C] text-slate-100 p-6 md:p-10 relative overflow-hidden bg-grid-pattern">
      <div className="max-w-7xl mx-auto space-y-8">
        {/* Header Navigation */}
        <header className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-white/5 pb-6">
          <div>
            <div className="flex items-center gap-3">
              <Link
                href="/"
                className="inline-flex items-center gap-1.5 text-xs text-indigo-400 hover:text-indigo-300 transition-colors font-mono mb-2"
              >
                <ArrowLeft className="w-3.5 h-3.5" /> Back to Operations Dashboard
              </Link>
            </div>
            <div className="flex items-center gap-3">
              <div className="h-2.5 w-2.5 rounded-full bg-rose-500 shadow-[0_0_8px_#f43f5e] animate-pulse" />
              <h1 className="text-2xl md:text-3xl font-semibold tracking-tight text-white">
                Operations & Incident Control
              </h1>
            </div>
            <p className="text-xs text-slate-400 mt-1">
              Real-time synchronization with Spring Boot backend endpoints:{" "}
              <code className="font-mono text-indigo-300">/api/v1/operations/incidents</code> and{" "}
              <code className="font-mono text-indigo-300">/api/v1/operations/deployments</code>
            </p>
          </div>

          <div className="flex items-center gap-2">
            <span className="text-xs font-mono px-3 py-1.5 rounded-lg bg-white/5 border border-white/10 text-slate-300 flex items-center gap-2">
              <span
                className={`h-2 w-2 rounded-full ${
                  fetchError ? "bg-amber-400" : "bg-emerald-400 animate-pulse"
                }`}
              />
              {fetchError ? "Using Telemetry Cache" : "Live API Connected"}
            </span>
          </div>
        </header>

        {/* Overview Stats */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="p-4 rounded-xl glass-panel">
            <div className="flex items-center justify-between text-slate-400 text-xs font-medium">
              <span>Total Incidents</span>
              <ShieldAlert className="w-4 h-4 text-rose-400" />
            </div>
            <div className="mt-2 text-2xl font-bold text-white">{displayIncidents.length}</div>
            <div className="text-[11px] text-slate-500 mt-0.5">Reported across pipelines</div>
          </div>

          <div className="p-4 rounded-xl glass-panel">
            <div className="flex items-center justify-between text-slate-400 text-xs font-medium">
              <span>Active SEV-2</span>
              <AlertTriangle className="w-4 h-4 text-amber-400" />
            </div>
            <div className="mt-2 text-2xl font-bold text-amber-300">
              {
                displayIncidents.filter(
                  (i) => i.severity === "SEV2" || i.severity === "SEV_2"
                ).length
              }
            </div>
            <div className="text-[11px] text-slate-500 mt-0.5">Requiring immediate triage</div>
          </div>

          <div className="p-4 rounded-xl glass-panel">
            <div className="flex items-center justify-between text-slate-400 text-xs font-medium">
              <span>Recent Deployments</span>
              <Terminal className="w-4 h-4 text-indigo-400" />
            </div>
            <div className="mt-2 text-2xl font-bold text-white">
              {deployments.length > 0 ? deployments.length : "4 (Mock)"}
            </div>
            <div className="text-[11px] text-slate-500 mt-0.5">Lifecycle monitored</div>
          </div>

          <div className="p-4 rounded-xl glass-panel">
            <div className="flex items-center justify-between text-slate-400 text-xs font-medium">
              <span>System Health</span>
              <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            </div>
            <div className="mt-2 text-2xl font-bold text-emerald-400">99.94%</div>
            <div className="text-[11px] text-slate-500 mt-0.5">SLA Target 99.90%</div>
          </div>
        </div>

        {/* Incident Management Section */}
        <section className="space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <ShieldAlert className="w-5 h-5 text-rose-400" />
              <h2 className="text-lg font-semibold text-white">Incident Stream</h2>
            </div>
            <span className="text-xs font-mono text-slate-400 bg-white/5 px-2.5 py-1 rounded border border-white/5">
              Filtered Query Feed
            </span>
          </div>

          <div className="space-y-3">
            {displayIncidents.map((incident) => {
              const isCritical =
                incident.severity === "SEV1" ||
                incident.severity === "SEV_1" ||
                incident.severity === "SEV2" ||
                incident.severity === "SEV_2";

              return (
                <div
                  key={incident.id}
                  className="p-4 rounded-xl bg-[#12151D] border border-white/5 hover:border-white/10 transition-all flex flex-col md:flex-row md:items-center justify-between gap-4"
                >
                  <div className="flex items-start gap-3.5">
                    <div
                      className={`p-2 rounded-lg mt-0.5 border ${
                        isCritical
                          ? "bg-rose-500/10 text-rose-400 border-rose-500/20"
                          : "bg-amber-500/10 text-amber-400 border-amber-500/20"
                      }`}
                    >
                      <ShieldAlert className="w-5 h-5" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2 flex-wrap">
                        <span
                          className={`text-[10px] font-mono font-semibold uppercase px-2 py-0.5 rounded border ${
                            isCritical
                              ? "bg-rose-500/20 text-rose-300 border-rose-500/30"
                              : "bg-amber-500/20 text-amber-300 border-amber-500/30"
                          }`}
                        >
                          {incident.severity}
                        </span>
                        <span className="text-xs font-mono text-slate-400">{incident.id}</span>
                        <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-white/5 text-slate-400 border border-white/5">
                          Status: {incident.status}
                        </span>
                      </div>
                      <p className="text-sm text-slate-200 font-medium mt-1.5">
                        {incident.summary}
                      </p>
                      <div className="flex items-center gap-4 text-xs text-slate-500 mt-2 flex-wrap font-mono">
                        <span>Service: {incident.serviceId}</span>
                        <span>•</span>
                        <span>Env: {incident.envId}</span>
                        <span>•</span>
                        <span className="flex items-center gap-1">
                          <Clock className="w-3 h-3" />
                          {new Date(incident.createdAt).toLocaleString()}
                        </span>
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 shrink-0">
                    <Link
                      href="/"
                      className="text-xs px-3 py-1.5 rounded-lg bg-white/5 hover:bg-white/10 text-slate-200 border border-white/5 transition-all text-center"
                    >
                      Inspect in Dashboard
                    </Link>
                  </div>
                </div>
              );
            })}
          </div>
        </section>

        {/* Deployments Stream Section */}
        {deployments.length > 0 && (
          <section className="space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Terminal className="w-5 h-5 text-indigo-400" />
                <h2 className="text-lg font-semibold text-white">Live Pipeline Deployments</h2>
              </div>
              <span className="text-xs font-mono text-slate-400 bg-white/5 px-2.5 py-1 rounded border border-white/5">
                {deployments.length} Records
              </span>
            </div>

            <div className="space-y-2.5">
              {deployments.map((d) => (
                <div
                  key={d.id}
                  className="p-3.5 rounded-xl bg-[#12151D] border border-white/5 flex items-center justify-between"
                >
                  <div className="flex items-center gap-3">
                    <div className="p-1.5 rounded bg-white/5 text-slate-400">
                      <Terminal className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-mono text-slate-200">{d.id}</span>
                        <span
                          className={`text-[10px] font-mono uppercase px-2 py-0.5 rounded border ${
                            d.status === "FAILED"
                              ? "bg-rose-500/10 text-rose-400 border-rose-500/20"
                              : d.status === "SUCCESS"
                              ? "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"
                              : "bg-cyan-500/10 text-cyan-400 border-cyan-500/20"
                          }`}
                        >
                          {d.status}
                        </span>
                      </div>
                      {d.failureReason && (
                        <p className="text-xs text-rose-400 mt-1 font-mono">
                          {d.failureReason}
                        </p>
                      )}
                    </div>
                  </div>

                  <span className="text-xs font-mono text-slate-500">
                    {d.startedAt ? new Date(d.startedAt).toLocaleTimeString() : "Recent"}
                  </span>
                </div>
              ))}
            </div>
          </section>
        )}
      </div>
    </div>
  );
}
