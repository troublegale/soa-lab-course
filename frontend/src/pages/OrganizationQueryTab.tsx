import React from "react";
import type { Organization, OrganizationType } from "../api/organizations";
import {
    fetchOrganizationsByOrganizationQuery,
    type OrganizationQueryState,
    type OrgQuerySortField,
    type QueryNumOp,
    type QueryStrOp,
    type QueryDateOp,
} from "../api/organizations";
import { CreateOrganizationModal } from "../components/CreateOrganizationModal";
import { DeleteOrganizationModal } from "../components/DeleteOrganizationModal";
import { deleteOrganization } from "../api/organizations";

type PageItem = number | "…";

function buildPageItems(current: number, total: number, siblingCount = 1): PageItem[] {
    if (total <= 1) return [1];
    const clamp = (n: number) => Math.max(1, Math.min(total, n));
    current = clamp(current);

    const left = Math.max(2, current - siblingCount);
    const right = Math.min(total - 1, current + siblingCount);

    const items: PageItem[] = [1];
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

function formatOrgType(type: string): string {
    if (!type) return "";
    const words = type.toLowerCase().split("_").filter(Boolean);
    return words.map((w, i) => (i === 0 ? w[0].toUpperCase() + w.slice(1) : w)).join(" ");
}

function formatDateDDMMYYYY(isoDate: string): string {
    if (!isoDate) return "";
    const [y, m, d] = isoDate.split("-").map(Number);
    if (!y || !m || !d) return isoDate;
    return `${String(d).padStart(2, "0")}.${String(m).padStart(2, "0")}.${y}`;
}

function parseDDMMYYYYToISO(s: string): string | null {
    const v = s.trim();
    if (!v) return null;
    const m = v.match(/^(\d{2})\.(\d{2})\.(\d{4})$/);
    if (!m) return null;
    const dd = Number(m[1]);
    const mm = Number(m[2]);
    const yyyy = Number(m[3]);
    if (!yyyy || mm < 1 || mm > 12 || dd < 1 || dd > 31) return null;
    const iso = `${String(yyyy).padStart(4, "0")}-${String(mm).padStart(2, "0")}-${String(dd).padStart(2, "0")}`;
    return iso;
}

function isBlank(s: string) {
    return s.trim().length === 0;
}

function parseNumberOrNull(s: string): number | null {
    if (s.trim() === "") return null;
    const n = Number(s);
    return Number.isFinite(n) ? n : null;
}

function parseIntOrNull(s: string): number | null {
    const n = parseNumberOrNull(s);
    if (n === null) return null;
    return Number.isInteger(n) ? n : null;
}

const ORG_TYPES: OrganizationType[] = [
    "COMMERCIAL",
    "GOVERNMENT",
    "PRIVATE_LIMITED_COMPANY",
    "OPEN_JOINT_STOCK_COMPANY",
];

const NUM_OPS: { value: QueryNumOp; label: string }[] = [
    { value: "eq", label: "eq" },
    { value: "gt", label: "gt" },
    { value: "ge", label: "ge" },
    { value: "lt", label: "lt" },
    { value: "le", label: "le" },
];

const STR_OPS: { value: QueryStrOp; label: string }[] = [
    { value: "eq", label: "eq" },
    { value: "contains", label: "contains" },
    { value: "startsWith", label: "starts with" },
    { value: "endsWith", label: "ends with" },
];

const DATE_OPS: { value: QueryDateOp; label: string }[] = [
    { value: "eq", label: "eq" },
    { value: "before", label: "before" },
    { value: "after", label: "after" },
];

type SortState = { field: OrgQuerySortField; desc: boolean } | null;

type FilterRow = {
    op: string;
    value: string;
};

type Errors = Record<string, string | undefined>;

export default function OrganizationQueryTab() {
    // filters
    const [idF, setIdF] = React.useState<FilterRow>({ op: "", value: "" });
    const [nameF, setNameF] = React.useState<FilterRow>({ op: "", value: "" });

    const [cxF, setCxF] = React.useState<FilterRow>({ op: "", value: "" });
    const [cyF, setCyF] = React.useState<FilterRow>({ op: "", value: "" });

    const [cdF, setCdF] = React.useState<FilterRow>({ op: "", value: "" }); // DD.MM.YYYY
    const [turnF, setTurnF] = React.useState<FilterRow>({ op: "", value: "" });

    const [fullNameF, setFullNameF] = React.useState<FilterRow>({ op: "", value: "" });
    const [typeVal, setTypeVal] = React.useState<string>("");

    const [streetF, setStreetF] = React.useState<FilterRow>({ op: "", value: "" });
    const [townNameF, setTownNameF] = React.useState<FilterRow>({ op: "", value: "" });
    const [townXF, setTownXF] = React.useState<FilterRow>({ op: "", value: "" });
    const [townYF, setTownYF] = React.useState<FilterRow>({ op: "", value: "" });

    const [errors, setErrors] = React.useState<Errors>({});

    // search/table state
    const [submittedQuery, setSubmittedQuery] = React.useState<OrganizationQueryState | null>(null);
    const [sort, setSort] = React.useState<SortState>(null);

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
    const [fetchError, setFetchError] = React.useState<string | null>(null);

    // update/delete modals
    const [updateOpen, setUpdateOpen] = React.useState(false);
    const [selectedOrg, setSelectedOrg] = React.useState<Organization | null>(null);

    const [deleteOpen, setDeleteOpen] = React.useState(false);
    const [deleteId, setDeleteId] = React.useState<number | null>(null);
    const [deleting, setDeleting] = React.useState(false);
    const [deleteError, setDeleteError] = React.useState<string | null>(null);

    const refreshTokenRef = React.useRef(0);
    const bumpRefresh = () => {
        refreshTokenRef.current += 1;
        // триггерим перезапрос “на месте”
        setSubmittedQuery((q) => (q ? { ...q } : q));
    };

    const toggleSort = (field: OrgQuerySortField) => {
        setPage(1);
        setSort((prev) => {
            if (!prev || prev.field !== field) return { field, desc: false };
            if (prev.desc === false) return { field, desc: true };
            return null;
        });
    };

    const validateAndBuildQuery = (): { ok: boolean; query?: OrganizationQueryState; errs?: Errors } => {
        const e: Errors = {};
        const q: OrganizationQueryState = {};

        const handleNum = (key: string, row: FilterRow, allowFloat: boolean) => {
            const hasValue = !isBlank(row.value);
            const hasOp = !isBlank(row.op);

            if (!hasValue && !hasOp) return null;
            if (!hasOp) {
                e[key] = "Choose operation";
                return null;
            }
            if (!hasValue) {
                e[key] = "Enter value";
                return null;
            }

            const n = allowFloat ? parseNumberOrNull(row.value) : parseIntOrNull(row.value);
            if (n === null) {
                e[key] = allowFloat ? "Invalid number" : "Invalid integer";
                return null;
            }
            return { op: row.op as QueryNumOp, value: n };
        };

        const handleStr = (key: string, row: FilterRow) => {
            const hasValue = !isBlank(row.value);
            const hasOp = !isBlank(row.op);

            if (!hasValue && !hasOp) return null;
            if (!hasOp) {
                e[key] = "Choose operation";
                return null;
            }
            if (!hasValue) {
                e[key] = "Enter value";
                return null;
            }
            return { op: row.op as QueryStrOp, value: row.value.trim() };
        };

        // ID
        const id = handleNum("id", idF, false);
        if (id) q.id = id;

        // Name
        const name = handleStr("name", nameF);
        if (name) q.name = name;

        // Coordinates
        const cx = handleNum("coordX", cxF, false);
        const cy = handleNum("coordY", cyF, false);
        if (cx) q.coordX = cx;
        if (cy) q.coordY = cy;

        // Creation date
        {
            const hasValue = !isBlank(cdF.value);
            const hasOp = !isBlank(cdF.op);
            if (hasValue || hasOp) {
                if (!hasOp) e.creationDate = "Choose operation";
                else if (!hasValue) e.creationDate = "Enter date";
                else {
                    const iso = parseDDMMYYYYToISO(cdF.value);
                    if (!iso) e.creationDate = "Invalid date (DD.MM.YYYY)";
                    else q.creationDate = { op: cdF.op as QueryDateOp, valueIso: iso };
                }
            }
        }

        // Annual turnover (float)
        const t = handleNum("annualTurnover", turnF, true);
        if (t) q.annualTurnover = t;

        // Full name
        const fn = handleStr("fullName", fullNameF);
        if (fn) q.fullName = fn;

        // Type
        if (!isBlank(typeVal)) q.type = { value: typeVal as OrganizationType };

        // Address
        const street = handleStr("addressStreet", streetF);
        if (street) q.addressStreet = street;

        const townName = handleStr("addressTownName", townNameF);
        if (townName) q.addressTownName = townName;

        const townX = handleNum("addressTownX", townXF, true);
        if (townX) q.addressTownX = townX;

        const townY = handleNum("addressTownY", townYF, false);
        if (townY) q.addressTownY = townY;

        // sort
        q.sort = sort ? { field: sort.field, desc: sort.desc } : null;

        const ok = Object.keys(e).length === 0;
        return ok ? { ok: true, query: q } : { ok: false, errs: e };
    };

    const onSearch = () => {
        setFetchError(null);
        const r = validateAndBuildQuery();
        if (!r.ok) {
            setErrors(r.errs ?? {});
            return;
        }
        setErrors({});
        setPage(1);
        setSubmittedQuery(r.query!);
    };

    // refetch on search / page / size / sort
    React.useEffect(() => {
        if (!submittedQuery) {
            setData(null);
            setFetchError(null);
            return;
        }

        const controller = new AbortController();
        setIsFetching(true);
        setFetchError(null);

        const q: OrganizationQueryState = {
            ...submittedQuery,
            sort: sort ? { field: sort.field, desc: sort.desc } : null,
        };

        fetchOrganizationsByOrganizationQuery({ page, size, query: q, signal: controller.signal })
            .then((d) => setData(d))
            .catch((e: unknown) => {
                if ((e as any)?.name === "AbortError") return;
                setFetchError(e instanceof Error ? e.message : "Unknown error");
            })
            .finally(() => setIsFetching(false));

        return () => controller.abort();
    }, [submittedQuery, page, size, sort]);

    const openUpdate = (org: Organization) => {
        setSelectedOrg(org);
        setUpdateOpen(true);
    };

    const openDelete = (id: number) => {
        setDeleteId(id);
        setDeleteError(null);
        setDeleteOpen(true);
    };

    const confirmDelete = () => {
        if (deleteId == null) return;
        setDeleting(true);
        setDeleteError(null);

        deleteOrganization(deleteId)
            .then(() => {
                setDeleteOpen(false);
                setDeleteId(null);
                bumpRefresh();
            })
            .catch((e: unknown) => setDeleteError(e instanceof Error ? e.message : "Unknown error"))
            .finally(() => setDeleting(false));
    };

    const totalPages = data?.totalPages ?? 1;
    const organizations = data?.organizations ?? [];

    return (
        <div className="opsWrap">
            <section className="card">
                <div className="filtersHeader">
                    <div className="meta">Filters (operation + value). Empty filter is ignored.</div>
                    <button onClick={onSearch} disabled={isFetching}>
                        {isFetching ? "Searching…" : "Search"}
                    </button>
                </div>

                <div className="filtersGrid">
                    {/* ID */}
                    <div className="filterItem">
                        <div className="filterLabel">ID</div>
                        <select value={idF.op} onChange={(e) => setIdF((p) => ({ ...p, op: e.target.value }))}>
                            <option value="">—</option>
                            {NUM_OPS.map((o) => (
                                <option key={o.value} value={o.value}>
                                    {o.label}
                                </option>
                            ))}
                        </select>
                        <input
                            inputMode="numeric"
                            placeholder="integer"
                            value={idF.value}
                            onChange={(e) => setIdF((p) => ({ ...p, value: e.target.value }))}
                        />
                        {errors.id && <div className="fieldError">{errors.id}</div>}
                    </div>

                    {/* Name */}
                    <div className="filterItem">
                        <div className="filterLabel">Name</div>
                        <select value={nameF.op} onChange={(e) => setNameF((p) => ({ ...p, op: e.target.value }))}>
                            <option value="">—</option>
                            {STR_OPS.map((o) => (
                                <option key={o.value} value={o.value}>
                                    {o.label}
                                </option>
                            ))}
                        </select>
                        <input value={nameF.value} onChange={(e) => setNameF((p) => ({ ...p, value: e.target.value }))} />
                        {errors.name && <div className="fieldError">{errors.name}</div>}
                    </div>

                    {/* Coordinate X */}
                    <div className="filterItem">
                        <div className="filterLabel">Coordinate X</div>
                        <select value={cxF.op} onChange={(e) => setCxF((p) => ({ ...p, op: e.target.value }))}>
                            <option value="">—</option>
                            {NUM_OPS.map((o) => (
                                <option key={o.value} value={o.value}>
                                    {o.label}
                                </option>
                            ))}
                        </select>
                        <input
                            inputMode="numeric"
                            placeholder="integer"
                            value={cxF.value}
                            onChange={(e) => setCxF((p) => ({ ...p, value: e.target.value }))}
                        />
                        {errors.coordX && <div className="fieldError">{errors.coordX}</div>}
                    </div>

                    {/* Coordinate Y */}
                    <div className="filterItem">
                        <div className="filterLabel">Coordinate Y</div>
                        <select value={cyF.op} onChange={(e) => setCyF((p) => ({ ...p, op: e.target.value }))}>
                            <option value="">—</option>
                            {NUM_OPS.map((o) => (
                                <option key={o.value} value={o.value}>
                                    {o.label}
                                </option>
                            ))}
                        </select>
                        <input
                            inputMode="numeric"
                            placeholder="integer"
                            value={cyF.value}
                            onChange={(e) => setCyF((p) => ({ ...p, value: e.target.value }))}
                        />
                        {errors.coordY && <div className="fieldError">{errors.coordY}</div>}
                    </div>

                    {/* Creation date */}
                    <div className="filterItem">
                        <div className="filterLabel">Creation date</div>
                        <select value={cdF.op} onChange={(e) => setCdF((p) => ({ ...p, op: e.target.value }))}>
                            <option value="">—</option>
                            {DATE_OPS.map((o) => (
                                <option key={o.value} value={o.value}>
                                    {o.label}
                                </option>
                            ))}
                        </select>
                        <input
                            placeholder="DD.MM.YYYY"
                            value={cdF.value}
                            onChange={(e) => setCdF((p) => ({ ...p, value: e.target.value }))}
                        />
                        {errors.creationDate && <div className="fieldError">{errors.creationDate}</div>}
                    </div>

                    {/* Annual Turnover */}
                    <div className="filterItem">
                        <div className="filterLabel">Annual turnover</div>
                        <select value={turnF.op} onChange={(e) => setTurnF((p) => ({ ...p, op: e.target.value }))}>
                            <option value="">—</option>
                            {NUM_OPS.map((o) => (
                                <option key={o.value} value={o.value}>
                                    {o.label}
                                </option>
                            ))}
                        </select>
                        <input
                            inputMode="decimal"
                            placeholder="float"
                            value={turnF.value}
                            onChange={(e) => setTurnF((p) => ({ ...p, value: e.target.value }))}
                        />
                        {errors.annualTurnover && <div className="fieldError">{errors.annualTurnover}</div>}
                    </div>

                    {/* Full Name */}
                    <div className="filterItem">
                        <div className="filterLabel">Full name</div>
                        <select value={fullNameF.op} onChange={(e) => setFullNameF((p) => ({ ...p, op: e.target.value }))}>
                            <option value="">—</option>
                            {STR_OPS.map((o) => (
                                <option key={o.value} value={o.value}>
                                    {o.label}
                                </option>
                            ))}
                        </select>
                        <input
                            value={fullNameF.value}
                            onChange={(e) => setFullNameF((p) => ({ ...p, value: e.target.value }))}
                        />
                        {errors.fullName && <div className="fieldError">{errors.fullName}</div>}
                    </div>

                    {/* Type */}
                    <div className="filterItem">
                        <div className="filterLabel">Type</div>
                        <select value={typeVal} onChange={(e) => setTypeVal(e.target.value)}>
                            <option value="">—</option>
                            {ORG_TYPES.map((t) => (
                                <option key={t} value={t}>
                                    {formatOrgType(t)}
                                </option>
                            ))}
                        </select>
                        <div />
                    </div>

                    {/* Address Street */}
                    <div className="filterItem">
                        <div className="filterLabel">Address street</div>
                        <select value={streetF.op} onChange={(e) => setStreetF((p) => ({ ...p, op: e.target.value }))}>
                            <option value="">—</option>
                            {STR_OPS.map((o) => (
                                <option key={o.value} value={o.value}>
                                    {o.label}
                                </option>
                            ))}
                        </select>
                        <input value={streetF.value} onChange={(e) => setStreetF((p) => ({ ...p, value: e.target.value }))} />
                        {errors.addressStreet && <div className="fieldError">{errors.addressStreet}</div>}
                    </div>

                    {/* Town Name */}
                    <div className="filterItem">
                        <div className="filterLabel">Address town name</div>
                        <select value={townNameF.op} onChange={(e) => setTownNameF((p) => ({ ...p, op: e.target.value }))}>
                            <option value="">—</option>
                            {STR_OPS.map((o) => (
                                <option key={o.value} value={o.value}>
                                    {o.label}
                                </option>
                            ))}
                        </select>
                        <input
                            value={townNameF.value}
                            onChange={(e) => setTownNameF((p) => ({ ...p, value: e.target.value }))}
                        />
                        {errors.addressTownName && <div className="fieldError">{errors.addressTownName}</div>}
                    </div>

                    {/* Town X (float) */}
                    <div className="filterItem">
                        <div className="filterLabel">Address town X</div>
                        <select value={townXF.op} onChange={(e) => setTownXF((p) => ({ ...p, op: e.target.value }))}>
                            <option value="">—</option>
                            {NUM_OPS.map((o) => (
                                <option key={o.value} value={o.value}>
                                    {o.label}
                                </option>
                            ))}
                        </select>
                        <input
                            inputMode="decimal"
                            placeholder="float"
                            value={townXF.value}
                            onChange={(e) => setTownXF((p) => ({ ...p, value: e.target.value }))}
                        />
                        {errors.addressTownX && <div className="fieldError">{errors.addressTownX}</div>}
                    </div>

                    {/* Town Y (int) */}
                    <div className="filterItem">
                        <div className="filterLabel">Address town Y</div>
                        <select value={townYF.op} onChange={(e) => setTownYF((p) => ({ ...p, op: e.target.value }))}>
                            <option value="">—</option>
                            {NUM_OPS.map((o) => (
                                <option key={o.value} value={o.value}>
                                    {o.label}
                                </option>
                            ))}
                        </select>
                        <input
                            inputMode="numeric"
                            placeholder="integer"
                            value={townYF.value}
                            onChange={(e) => setTownYF((p) => ({ ...p, value: e.target.value }))}
                        />
                        {errors.addressTownY && <div className="fieldError">{errors.addressTownY}</div>}
                    </div>
                </div>
            </section>

            <section className="card">
                <div className="toolbar">
                    <div className="title">
                        <div className="meta">
                            {data ? (
                                <>
                                    total: <b>{data.totalElements}</b> · pages: <b>{data.totalPages}</b>
                                </>
                            ) : (
                                "—"
                            )}
                            {sort ? (
                                <>
                                    {" "}· sort: <b>{`${sort.desc ? "-" : ""}${sort.field}`}</b>
                                </>
                            ) : null}
                        </div>
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
                            <button onClick={() => setPage((p) => Math.max(1, p - 1))} disabled={isFetching || page <= 1}>
                                ‹ Prev
                            </button>

                            <div className="pages" aria-label="Pagination">
                                {buildPageItems(data?.page ?? page, totalPages, 1).map((it, idx) =>
                                        it === "…" ? (
                                            <span key={`dots-${idx}`} className="dots">
                      …
                    </span>
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

                {fetchError && (
                    <div className="error">
                        <b>Error:</b> {fetchError}
                    </div>
                )}

                <div className="tableWrap" style={{ ["--rows" as any]: size }}>
                    {isFetching && data && <div className="loadingOverlay">Loading…</div>}

                    <table className="table">
                        <thead>
                        <tr>
                            <th className="thSortable" onClick={() => toggleSort("id")} style={{ width: 90 }}>
                                ID {sort?.field === "id" ? (sort.desc ? "▼" : "▲") : ""}
                            </th>
                            <th className="thSortable" onClick={() => toggleSort("name")}>
                                Name {sort?.field === "name" ? (sort.desc ? "▼" : "▲") : ""}
                            </th>
                            <th className="thSortable" onClick={() => toggleSort("fullName")}>
                                Full name {sort?.field === "fullName" ? (sort.desc ? "▼" : "▲") : ""}
                            </th>
                            <th className="thSortable" onClick={() => toggleSort("creationDate")} style={{ width: 140 }}>
                                Created {sort?.field === "creationDate" ? (sort.desc ? "▼" : "▲") : ""}
                            </th>
                            <th className="thSortable" onClick={() => toggleSort("annualTurnover")} style={{ width: 150 }}>
                                Turnover {sort?.field === "annualTurnover" ? (sort.desc ? "▼" : "▲") : ""}
                            </th>
                            <th className="thSortable" onClick={() => toggleSort("type")} style={{ width: 150 }}>
                                Type {sort?.field === "type" ? (sort.desc ? "▼" : "▲") : ""}
                            </th>
                            <th className="thSortable" onClick={() => toggleSort("coordinates")}>
                                Coordinates {sort?.field === "coordinates" ? (sort.desc ? "▼" : "▲") : ""}
                            </th>
                            <th className="thSortable" onClick={() => toggleSort("officialAddress")}>
                                Official address {sort?.field === "officialAddress" ? (sort.desc ? "▼" : "▲") : ""}
                            </th>
                            <th style={{ width: 170 }}>Actions</th>
                        </tr>
                        </thead>

                        <tbody>
                        {organizations.length === 0 && !isFetching ? (
                            <tr>
                                <td colSpan={9} className="muted">
                                    {submittedQuery ? "No organizations" : "Fill filters and press Search"}
                                </td>
                            </tr>
                        ) : (
                            organizations.map((o) => (
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
                                        <div style={{ display: "flex", gap: 8 }}>
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

                <CreateOrganizationModal
                    open={updateOpen}
                    onClose={() => setUpdateOpen(false)}
                    mode="update"
                    initialOrganization={selectedOrg}
                    onCreated={bumpRefresh}
                />

                <DeleteOrganizationModal
                    open={deleteOpen}
                    organizationId={deleteId}
                    loading={deleting}
                    error={deleteError}
                    onClose={() => (deleting ? null : setDeleteOpen(false))}
                    onConfirm={confirmDelete}
                />
            </section>
        </div>
    );
}
