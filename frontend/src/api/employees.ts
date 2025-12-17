import { XMLParser } from "fast-xml-parser";

export interface EmployeeRow {
    id: number;
    name: string;
    salary: number;
    organizationId: number;
}

const parser = new XMLParser({
    ignoreAttributes: true,
    trimValues: true,
    parseTagValue: true,
});

function toArray<T>(v: T | T[] | undefined | null): T[] {
    if (v == null) return [];
    return Array.isArray(v) ? v : [v];
}

function toNumber(v: unknown, fallback = 0): number {
    const n = typeof v === "number" ? v : Number(v);
    return Number.isFinite(n) ? n : fallback;
}

function toString(v: unknown): string {
    return v == null ? "" : String(v);
}

export async function fetchEmployeesByOrganizationId(id: number, signal?: AbortSignal): Promise<EmployeeRow[]> {
    const res = await fetch(`/soa/api/v1/organizations/${id}/employees`, {
        method: "GET",
        headers: { Accept: "application/xml" },
        signal,
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`HTTP ${res.status} ${res.statusText}${text ? ` — ${text}` : ""}`);
    }

    const xml = await res.text();
    const parsed = parser.parse(xml);

    const root = parsed?.employees;
    // root может быть "" при <employees/>
    if (root == null) throw new Error("Unexpected XML: missing <employees>");

    const raw = root?.employee;
    return toArray(raw).map((e: any) => ({
        id: toNumber(e?.id),
        name: toString(e?.name),
        salary: toNumber(e?.salary),
        // организация может прийти вложенным объектом; показываем только её id
        organizationId: toNumber(e?.organization?.id, id),
    }));
}
