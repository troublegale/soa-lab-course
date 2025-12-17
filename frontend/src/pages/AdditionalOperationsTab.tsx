import React from "react";
import type {Organization} from "../api/organizations";
import {
    fetchOrganizationsLtFullName,
    fetchTotalTurnover,
    fetchTypeCounts,
    fireAllEmployees,
    acquireOrganizations,
    type TypeCount,
} from "../api/additionalOperations";
import {fetchOrganizationsPage} from "../api/organizations";
import {CreateOrganizationModal} from "../components/CreateOrganizationModal";
import {DeleteOrganizationModal} from "../components/DeleteOrganizationModal";
import {deleteOrganization} from "../api/organizations";


function formatOrgType(type: string): string {
    if (!type) return "";
    const words = type
        .toLowerCase()
        .split("_")
        .filter(Boolean);
    return words.map((w, i) => (i === 0 ? w[0].toUpperCase() + w.slice(1) : w)).join(" ");
}

function formatDateDDMMYYYY(isoDate: string): string {
    if (!isoDate) return "";
    const [y, m, d] = isoDate.split("-").map(Number);
    if (!y || !m || !d) return isoDate;
    return `${String(d).padStart(2, "0")}.${String(m).padStart(2, "0")}.${y}`;
}

type PageItem = number | "…";

function buildPageItems(current: number, total: number, siblingCount = 1): PageItem[] {
    if (total <= 1) return [1];
    const clamp = (n: number) => Math.max(1, Math.min(total, n));
    current = clamp(current);

    const left = Math.max(2, current - siblingCount);
    const right = Math.min(total - 1, current + siblingCount);

    const items: PageItem[] = [];
    items.push(1);
    if (left > 2) items.push("…");
    for (let p = left; p <= right; p++) items.push(p);
    if (right < total - 1) items.push("…");
    items.push(total);

    const cleaned: PageItem[] = [];
    for (const it of items) {
        const last = cleaned[cleaned.length - 1];
        if (it === last) continue;
        if (it === "…" && last === "…") continue;
        cleaned.push(it);
    }
    return cleaned;
}

export default function AdditionalOperationsTab() {
    // Aggregation 1: Total Turnover
    const [turnoverLoading, setTurnoverLoading] = React.useState(false);
    const [turnoverError, setTurnoverError] = React.useState<string | null>(null);
    const [turnover, setTurnover] = React.useState<{ totalTurnover: number; organizationCount: number } | null>(null);

    // Aggregation 2: Count types
    const [typesLoading, setTypesLoading] = React.useState(false);
    const [typesError, setTypesError] = React.useState<string | null>(null);
    const [typeCounts, setTypeCounts] = React.useState<TypeCount[] | null>(null);

    // Fire all employees
    const [fireOrgId, setFireOrgId] = React.useState<number | null>(null);
    const [fireLoading, setFireLoading] = React.useState(false);
    const [fireError, setFireError] = React.useState<string | null>(null);
    const [firedCount, setFiredCount] = React.useState<number | null>(null);

    // Acquire
    const [acquirerId, setAcquirerId] = React.useState<number | null>(null);
    const [acquiredId, setAcquiredId] = React.useState<number | null>(null);
    const [acqLoading, setAcqLoading] = React.useState(false);
    const [acqError, setAcqError] = React.useState<string | null>(null);
    const [acqMsg, setAcqMsg] = React.useState<string | null>(null);

    const [orgIds, setOrgIds] = React.useState<number[]>([]);

    React.useEffect(() => {
        const controller = new AbortController();
        fetchOrganizationsPage({page: 1, size: 500, signal: controller.signal})
            .then((p) => {
                const ids = p.organizations.map((o) => o.id).filter((x) => Number.isFinite(x));
                setOrgIds(ids);
            })
            .catch(() => setOrgIds([]));
        return () => controller.abort();
    }, []);

    // Operation: lt full name
    const [value, setValue] = React.useState("");
    const [submittedValue, setSubmittedValue] = React.useState<string | null>(null);

    const [page, setPage] = React.useState(1);
    const [size, setSize] = React.useState(5);

    const [data, setData] = React.useState<{
        organizations: Organization[];
        page: number;
        size: number;
        totalElements: number;
        totalPages: number;
    } | null>(null);

    const [isFetching, setIsFetching] = React.useState(false);
    const [opError, setOpError] = React.useState<string | null>(null);

    const [ltRefreshToken, setLtRefreshToken] = React.useState(0);

// Update modal
    const [updateOpen, setUpdateOpen] = React.useState(false);
    const [selectedOrg, setSelectedOrg] = React.useState<Organization | null>(null);
    const openUpdate = (org: Organization) => {
        setSelectedOrg(org);
        setUpdateOpen(true);
    };

// Delete modal
    const [deleteOpen, setDeleteOpen] = React.useState(false);
    const [deleteId, setDeleteId] = React.useState<number | null>(null);
    const [deleting, setDeleting] = React.useState(false);
    const [deleteError, setDeleteError] = React.useState<string | null>(null);

    const openDelete = (id: number) => {
        setDeleteId(id);
        setDeleteError(null);
        setDeleteOpen(true);
    };

    const refreshLt = () => setLtRefreshToken((x) => x + 1);

    const confirmDelete = () => {
        if (deleteId == null) return;
        setDeleting(true);
        setDeleteError(null);

        deleteOrganization(deleteId)
            .then(() => {
                setDeleteOpen(false);
                setDeleteId(null);
                refreshLt();
            })
            .catch((e: unknown) => setDeleteError(e instanceof Error ? e.message : "Unknown error"))
            .finally(() => setDeleting(false));
    };


    const totalPages = data?.totalPages ?? 1;

    const prettyTurnover =
        turnover == null
            ? null
            : new Intl.NumberFormat(undefined, {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            }).format(turnover.totalTurnover);

    const runTurnover = () => {
        const controller = new AbortController();
        setTurnoverLoading(true);
        setTurnoverError(null);

        fetchTotalTurnover(controller.signal)
            .then(setTurnover)
            .catch((e: unknown) => setTurnoverError(e instanceof Error ? e.message : "Unknown error"))
            .finally(() => setTurnoverLoading(false));
    };

    const runTypeCounts = () => {
        const controller = new AbortController();
        setTypesLoading(true);
        setTypesError(null);

        fetchTypeCounts(controller.signal)
            .then(setTypeCounts)
            .catch((e: unknown) => setTypesError(e instanceof Error ? e.message : "Unknown error"))
            .finally(() => setTypesLoading(false));
    };

    const runFireAll = () => {
        if (fireOrgId == null) {
            setFireError("Organization ID is required");
            return;
        }

        setFireLoading(true);
        setFireError(null);
        setFiredCount(null);

        fireAllEmployees(fireOrgId)
            .then((count) => setFiredCount(count))
            .catch((e: unknown) => setFireError(e instanceof Error ? e.message : "Unknown error"))
            .finally(() => setFireLoading(false));
    };

    const runAcquire = () => {
        if (acquirerId == null || acquiredId == null) {
            setAcqError("Both IDs are required");
            return;
        }

        if (acquirerId === acquiredId) {
            setAcqError("Acquirer ID and Acquired ID must be different");
            return;
        }

        setAcqLoading(true);
        setAcqError(null);
        setAcqMsg(null);

        acquireOrganizations(acquirerId, acquiredId)
            .then((r) => {
                setAcqMsg(
                    `Organization ${r.acquirer.id} ("${r.acquirer.name}") acquired Organization ${r.acquired.id} ("${r.acquired.name}"). ${r.moved} employees were moved.`
                );
            })
            .catch((e: unknown) => setAcqError(e instanceof Error ? e.message : "Unknown error"))
            .finally(() => setAcqLoading(false));
    };

    const onSubmitLt = (e: React.FormEvent) => {
        e.preventDefault();
        const v = value.trim();
        setSubmittedValue(v.length ? v : null);
        setPage(1);
    };

    const onClearLt = () => {
        setValue("");
        setSubmittedValue(null);
        setPage(1);
        setData(null);
        setOpError(null);
    };

    React.useEffect(() => {
        if (!submittedValue) {
            setData(null);
            setOpError(null);
            return;
        }

        const controller = new AbortController();
        setIsFetching(true);
        setOpError(null);

        fetchOrganizationsLtFullName({value: submittedValue, page, size, signal: controller.signal})
            .then((d) => setData(d))
            .catch((e: unknown) => {
                if ((e as any)?.name === "AbortError") return;
                setOpError(e instanceof Error ? e.message : "Unknown error");
            })
            .finally(() => setIsFetching(false));

        return () => controller.abort();
    }, [submittedValue, page, size, ltRefreshToken]);

    return (
        <div className="opsWrap">
            <section className="card">
                <div className="opsRow">
                    <div className="opsLeft">
                        <button onClick={runTurnover} disabled={turnoverLoading}>
                            {turnoverLoading ? "Loading…" : "Total Turnover"}
                        </button>
                    </div>

                    <div className="opsRight">
                        {turnoverError && <div className="error"><b>Error:</b> {turnoverError}</div>}
                        {turnover && (
                            <div className="opsResult">
                                <div>Total: <b>{prettyTurnover}</b></div>
                                <div>Organizations: <b>{turnover.organizationCount}</b></div>
                            </div>
                        )}
                        {!turnover && !turnoverError && <div className="muted">Click to calculate total turnover</div>}
                    </div>
                </div>
            </section>

            <section className="card">
                <div className="opsRow">
                    <div className="opsLeft">
                        <button onClick={runTypeCounts} disabled={typesLoading}>
                            {typesLoading ? "Loading…" : "Count types"}
                        </button>
                    </div>

                    <div className="opsRight">
                        {typesError && <div className="error"><b>Error:</b> {typesError}</div>}
                        {typeCounts && (
                            <div className="opsResult">
                                {typeCounts.map((t) => (
                                    <div key={t.type} className="kv">
                                        <span>{formatOrgType(t.type)}</span>
                                        <b>{t.count}</b>
                                    </div>
                                ))}
                            </div>
                        )}
                        {!typeCounts && !typesError &&
                            <div className="muted">Click to count organizations by type</div>}
                    </div>
                </div>
            </section>

            <section className="card">
                <div className="opsRow">
                    <div className="opsLeft">
                        <button onClick={runFireAll} disabled={fireLoading}>
                            {fireLoading ? "Running…" : "Fire all employees"}
                        </button>
                    </div>

                    <div className="opsRight">
                        <div className="opsInline">
                            <label className="control">
                                Organization ID:&nbsp;
                                <select
                                    value={fireOrgId ?? ""}
                                    onChange={(e) => {
                                        const v = e.target.value;
                                        setFireOrgId(v ? Number(v) : null);
                                    }}
                                    disabled={fireLoading}
                                >
                                    <option value="">Select…</option>
                                    {orgIds.map((id) => (
                                        <option key={id} value={id}>
                                            {id}
                                        </option>
                                    ))}
                                </select>
                            </label>


                            {firedCount != null && (
                                <div className="opsResultCompact">
                                    Fired employees: <b>{firedCount}</b>
                                </div>
                            )}
                        </div>

                        {fireError && (
                            <div className="error" style={{marginTop: 10}}>
                                <b>Error:</b> {fireError}
                            </div>
                        )}
                    </div>
                </div>
            </section>

            <section className="card">
                <div className="opsRow">
                    <div className="opsLeft">
                        <button onClick={runAcquire} disabled={acqLoading}>
                            {acqLoading ? "Running…" : "Acquiring"}
                        </button>
                    </div>

                    <div className="opsRight">
                        <div className="opsInline">
                            <label className="control">
                                Acquirer ID:&nbsp;
                                <select
                                    value={acquirerId ?? ""}
                                    onChange={(e) => {
                                        const v = e.target.value;
                                        setAcquirerId(v ? Number(v) : null);
                                    }}
                                    disabled={acqLoading}
                                >
                                    <option value="">Select…</option>
                                    {orgIds.map((id) => (
                                        <option key={id} value={id}>
                                            {id}
                                        </option>
                                    ))}
                                </select>
                            </label>

                            <label className="control">
                                Acquired ID:&nbsp;
                                <select
                                    value={acquiredId ?? ""}
                                    onChange={(e) => {
                                        const v = e.target.value;
                                        setAcquiredId(v ? Number(v) : null);
                                    }}
                                    disabled={acqLoading}
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

                        {acqMsg && <div className="opsResultCompact" style={{marginTop: 10}}>{acqMsg}</div>}

                        {acqError && (
                            <div className="error" style={{marginTop: 10}}>
                                <b>Error:</b> {acqError}
                            </div>
                        )}
                    </div>
                </div>
            </section>

            <section className="card">
                <div className="opsStack">
                    <div className="opsLtHeader">
                        <div className="field" style={{margin: 0}}>
                            <label>Search Organizations with full name less than value</label>

                            <form onSubmit={onSubmitLt} className="opsForm">
                                <input
                                    value={value}
                                    onChange={(e) => setValue(e.target.value)}
                                    placeholder="Enter full name…"
                                />
                                <button type="submit" disabled={isFetching}>
                                    Search
                                </button>
                                <button
                                    type="button"
                                    onClick={onClearLt}
                                    disabled={isFetching && !!submittedValue}
                                >
                                    Clear
                                </button>
                            </form>
                        </div>

                        {submittedValue && (
                            <div className="ltMeta">
                                <div className="meta">
                                    Value: <b>{submittedValue}</b>
                                    {data ? (
                                        <>
                                            {" "}· total: <b>{data.totalElements}</b> · pages: <b>{data.totalPages}</b>
                                        </>
                                    ) : null}
                                </div>

                                <div className="controls">
                                    <label className="control">
                                        Size:&nbsp;
                                        <select
                                            value={size}
                                            onChange={(e) => {
                                                setPage(1);
                                                setSize(Number(e.target.value));
                                            }}
                                            disabled={isFetching}
                                        >
                                            {[5, 10, 20, 100].map((n) => (
                                                <option key={n} value={n}>
                                                    {n}
                                                </option>
                                            ))}
                                        </select>
                                    </label>

                                    <div className="pager">
                                        <button onClick={() => setPage((p) => Math.max(1, p - 1))}
                                                disabled={isFetching || page <= 1}>
                                            ‹ Prev
                                        </button>

                                        <div className="pages" aria-label="Pagination">
                                            {buildPageItems(data?.page ?? page, totalPages, 1).map((it, idx) =>
                                                it === "…" ? (
                                                    <span key={`dots-${idx}`} className="dots">…</span>
                                                ) : (
                                                    <button
                                                        key={it}
                                                        className={`pageBtn ${it === (data?.page ?? page) ? "active" : ""}`}
                                                        onClick={() => setPage(it)}
                                                        disabled={isFetching || it === (data?.page ?? page)}
                                                    >
                                                        {it}
                                                    </button>
                                                )
                                            )}
                                        </div>

                                        <button
                                            onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                                            disabled={isFetching || page >= totalPages}
                                        >
                                            Next ›
                                        </button>
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>

                    {opError && (
                        <div className="error">
                            <b>Error:</b> {opError}
                        </div>
                    )}

                    {submittedValue ? (
                        <div className="tableWrap" style={{["--rows" as any]: size}}>
                            {isFetching && data && <div className="loadingOverlay">Loading…</div>}

                            <table className="table">
                                <thead>
                                <tr>
                                    <th style={{width: 90}}>ID</th>
                                    <th>Name</th>
                                    <th>Full name</th>
                                    <th style={{width: 140}}>Created</th>
                                    <th style={{width: 150}}>Turnover</th>
                                    <th style={{width: 150}}>Type</th>
                                    <th>Coordinates</th>
                                    <th>Official address</th>
                                    <th style={{width: 170}}>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                {(data?.organizations ?? []).length === 0 && !isFetching ? (
                                    <tr>
                                        <td colSpan={9} className="muted">
                                            No organizations
                                        </td>
                                    </tr>
                                ) : (
                                    (data?.organizations ?? []).map((o) => (
                                        <tr key={o.id}>
                                            <td>{o.id}</td>
                                            <td>{o.name}</td>
                                            <td>{o.fullName}</td>
                                            <td>{formatDateDDMMYYYY(o.creationDate)}</td>
                                            <td>{Number.isFinite(o.annualTurnover) ? o.annualTurnover : ""}</td>
                                            <td>{formatOrgType(o.type)}</td>
                                            <td>
                                                ({o.coordinates?.x ?? ""}; {o.coordinates?.y ?? ""})
                                            </td>
                                            <td>
                                                {o.officialAddress?.street}
                                                {o.officialAddress?.town?.name ? `, ${o.officialAddress.town.name}` : ""}
                                                {Number.isFinite(o.officialAddress?.town?.x) && Number.isFinite(o.officialAddress?.town?.y)
                                                    ? ` (${o.officialAddress.town.x}; ${o.officialAddress.town.y})`
                                                    : ""}
                                            </td>
                                            <td>
                                                <div style={{display: "flex", gap: 8}}>
                                                    <button onClick={() => openUpdate(o)}>Update</button>
                                                    <button onClick={() => openDelete(o.id)}>Delete</button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))
                                )}
                                </tbody>
                            </table>
                        </div>
                    ) : (
                        <div className="muted">Enter a value and press Search to load organizations</div>
                    )}
                    <CreateOrganizationModal
                        open={updateOpen}
                        onClose={() => setUpdateOpen(false)}
                        mode="update"
                        initialOrganization={selectedOrg}
                        onCreated={refreshLt}
                    />
                    <DeleteOrganizationModal
                        open={deleteOpen}
                        organizationId={deleteId}
                        loading={deleting}
                        error={deleteError}
                        onClose={() => (deleting ? null : setDeleteOpen(false))}
                        onConfirm={confirmDelete}
                    />
                </div>
            </section>
        </div>
    );
}
