import React from "react";
import { fetchOrganizationsPage } from "../api/organizations";
import { fetchEmployeesByOrganizationId, type EmployeeRow } from "../api/employees";

export default function EmployeesTab() {
    const [orgIds, setOrgIds] = React.useState<number[]>([]);
    const [orgId, setOrgId] = React.useState<number | null>(null);

    const [employees, setEmployees] = React.useState<EmployeeRow[]>([]);
    const [loading, setLoading] = React.useState(false);
    const [error, setError] = React.useState<string | null>(null);

    // Подтянем список id организаций, чтобы было удобно выбирать.
    React.useEffect(() => {
        const controller = new AbortController();
        fetchOrganizationsPage({ page: 1, size: 200, signal: controller.signal })
            .then((p) => {
                const ids = p.organizations.map((o) => o.id).filter((x) => Number.isFinite(x));
                setOrgIds(ids);
            })
            .catch(() => {
                // если не получилось — не критично, можно выбрать id вручную через input ниже
                setOrgIds([]);
            });
        return () => controller.abort();
    }, []);

    // Грузим сотрудников после выбора orgId
    React.useEffect(() => {
        if (orgId == null) {
            setEmployees([]);
            setError(null);
            return;
        }

        const controller = new AbortController();
        setLoading(true);
        setError(null);

        fetchEmployeesByOrganizationId(orgId, controller.signal)
            .then((list) => setEmployees(list))
            .catch((e: unknown) => {
                if ((e as any)?.name === "AbortError") return;
                setEmployees([]);
                setError(e instanceof Error ? e.message : "Unknown error");
            })
            .finally(() => setLoading(false));

        return () => controller.abort();
    }, [orgId]);

    return (
        <section className="card">
            <div className="toolbar">
                <div className="title">
                    <h2 style={{ margin: 0 }}>Employees</h2>
                    <div className="meta">Select organization ID to load employees</div>
                </div>

                <div className="controls">
                    <label className="control">
                        Organization ID:&nbsp;
                        <select
                            value={orgId ?? ""}
                            onChange={(e) => {
                                const v = e.target.value;
                                setOrgId(v ? Number(v) : null);
                            }}
                        >
                            <option value="">Select…</option>
                            {orgIds.map((id) => (
                                <option key={id} value={id}>
                                    {id}
                                </option>
                            ))}
                        </select>
                    </label>
                </div>
            </div>

            {error && (
                <div className="error">
                    <b>Error:</b> {error}
                </div>
            )}

            {orgId == null ? (
                <div className="muted" style={{ padding: 14 }}>
                    Choose an organization ID to show employees
                </div>
            ) : (
                <div className="tableWrap" style={{ minHeight: "220px" }}>
                    {loading && <div className="loadingOverlay">Loading…</div>}

                    <table className="table">
                        <thead>
                        <tr>
                            <th style={{ width: 90 }}>ID</th>
                            <th>Name</th>
                            <th style={{ width: 160 }}>Salary</th>
                            <th style={{ width: 140 }}>Organization ID</th>
                        </tr>
                        </thead>
                        <tbody>
                        {employees.length === 0 && !loading ? (
                            <tr>
                                <td colSpan={4} className="muted">
                                    No employees
                                </td>
                            </tr>
                        ) : (
                            employees.map((e) => (
                                <tr key={e.id}>
                                    <td>{e.id}</td>
                                    <td>{e.name}</td>
                                    <td>{e.salary}</td>
                                    <td>{e.organizationId}</td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </table>
                </div>
            )}
        </section>
    );
}
