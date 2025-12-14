import React from "react";
import {fetchOrganizationsPage, fetchOrganizationsPageQuery, type OrganizationsPage, type Organization, type SortField} from "../api/organizations";

function formatDate(isoDate: string): string {
    if (!isoDate) return "";
    // ожидаем "YYYY-MM-DD"
    const [y, m, d] = isoDate.split("-").map(Number);
    if (!y || !m || !d) return isoDate;

    const dd = String(d).padStart(2, "0");
    const mm = String(m).padStart(2, "0");
    return `${dd}.${mm}.${y}`;
}

function formatOrgType(type: string): string {
    if (!type) return "";
    // "OPEN_JOINT_STOCK_COMPANY" -> "Open joint stock company"
    const words = type
        .toLowerCase()
        .split("_")
        .filter(Boolean);

    if (words.length === 0) return "";

    return words
        .map((w, i) => (i === 0 ? w[0].toUpperCase() + w.slice(1) : w))
        .join(" ");
}

function clamp(n: number, min: number, max: number) {
    return Math.max(min, Math.min(max, n));
}

type PageItem = number | "…";

function buildPageItems(current: number, total: number, siblingCount = 1): PageItem[] {
    if (total <= 1) return [1];

    const clamp = (n: number) => Math.max(1, Math.min(total, n));

    current = clamp(current);

    // Показываем: 1 ... (current-1) current (current+1) ... total
    const left = Math.max(2, current - siblingCount);
    const right = Math.min(total - 1, current + siblingCount);

    const items: PageItem[] = [];

    // Всегда первая
    items.push(1);

    // Левый разрыв
    if (left > 2) items.push("…");

    // Средние страницы
    for (let p = left; p <= right; p++) items.push(p);

    // Правый разрыв
    if (right < total - 1) items.push("…");

    // Всегда последняя (если total > 1)
    items.push(total);

    // Уберём дубликаты в случаях типа total=2 или current рядом с краями
    const cleaned: PageItem[] = [];
    for (const it of items) {
        const last = cleaned[cleaned.length - 1];
        if (it === last) continue;
        // не ставим "…" рядом с "…"
        if (it === "…" && last === "…") continue;
        cleaned.push(it);
    }
    return cleaned;
}

export function OrganizationsTable({ refreshToken = 0 }: { refreshToken?: number }) {
    const [page, setPage] = React.useState(1);
    const [size, setSize] = React.useState(5);
    type SortState = { field: SortField; desc: boolean } | null;
    const [sort, setSort] = React.useState<SortState>(null);

    const toggleSort = (field: SortField) => {
        setPage(1);
        setSort((prev) => {
            if (!prev || prev.field !== field) return { field, desc: false }; // ASC
            if (prev.desc === false) return { field, desc: true };           // DESC
            return null;                                                     // DEFAULT
        });
    };

    const [data, setData] = React.useState<OrganizationsPage | null>(null);
    const [isFetching, setIsFetching] = React.useState(false);
    const [error, setError] = React.useState<string | null>(null);

    React.useEffect(() => {
        const controller = new AbortController();
        setIsFetching(true);
        setError(null);

        const load = sort ? fetchOrganizationsPageQuery : fetchOrganizationsPage;

        load({ page, size, sort, signal: controller.signal } as any)
            .then((d) => setData(d))
            .catch((e: unknown) => {
                if ((e as any)?.name === "AbortError") return;
                setError(e instanceof Error ? e.message : "Unknown error");
            })
            .finally(() => setIsFetching(false));

        return () => controller.abort();
    }, [page, size, sort, refreshToken]);

    const totalPages = data?.totalPages ?? 1;
    const safePage = clamp(page, 1, Math.max(1, totalPages));

    // Если бэк вернул totalPages меньше/больше — выравниваем page
    React.useEffect(() => {
        if (safePage !== page) setPage(safePage);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [totalPages]);

    const organizations: Organization[] = data?.organizations ?? [];

    return (
        <section className="card">
            <div className="toolbar">
                <div className="title">
                    <h2>Organizations</h2>
                    <div className="meta">
                        {data ? (
                            <>
                                Всего: <b>{data.totalElements}</b>, страниц: <b>{data.totalPages}</b>
                            </>
                        ) : (
                            "—"
                        )}
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

            {error && (
                <div className="error">
                    <b>Ошибка:</b> {error}
                </div>
            )}

            <div className="tableWrap" style={{ ["--rows" as any]: size }}>
                {isFetching && data && <div className="loadingOverlay">Loading…</div>}
                <table className="table">
                    <thead>
                    <tr>
                        <th className="thSortable" onClick={() => toggleSort("id")}>
                            ID {sort?.field === "id" ? (sort.desc ? "▼" : "▲") : ""}
                        </th>

                        <th className="thSortable" onClick={() => toggleSort("name")}>
                            Name {sort?.field === "name" ? (sort.desc ? "▼" : "▲") : ""}
                        </th>

                        <th className="thSortable" onClick={() => toggleSort("fullName")}>
                            Full name {sort?.field === "fullName" ? (sort.desc ? "▼" : "▲") : ""}
                        </th>

                        <th className="thSortable" onClick={() => toggleSort("creationDate")}>
                            Created {sort?.field === "creationDate" ? (sort.desc ? "▼" : "▲") : ""}
                        </th>

                        <th className="thSortable" onClick={() => toggleSort("annualTurnover")}>
                            Turnover {sort?.field === "annualTurnover" ? (sort.desc ? "▼" : "▲") : ""}
                        </th>

                        <th className="thSortable" onClick={() => toggleSort("type")}>
                            Type {sort?.field === "type" ? (sort.desc ? "▼" : "▲") : ""}
                        </th>

                        <th className="thSortable" onClick={() => toggleSort("coordinates")}>
                            Coordinates {sort?.field === "coordinates" ? (sort.desc ? "▼" : "▲") : ""}
                        </th>

                        <th className="thSortable" onClick={() => toggleSort("officialAddress")}>
                            Official address {sort?.field === "officialAddress" ? (sort.desc ? "▼" : "▲") : ""}
                        </th>
                    </tr>
                    </thead>


                    <tbody>
                    {organizations.length === 0 ? (
                        <tr>
                            <td colSpan={8} className="muted">
                                {data ? "Нет данных" : "Загрузка…"}
                            </td>
                        </tr>
                    ) : (
                        organizations.map((o) => (
                            <tr key={o.id}>
                                <td>{o.id}</td>
                                <td>{o.name}</td>
                                <td>{o.fullName}</td>
                                <td>{formatDate(o.creationDate)}</td>
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
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>
        </section>
    );
}
