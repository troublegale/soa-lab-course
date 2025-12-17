import { XMLParser } from "fast-xml-parser";
import type { Organization, OrganizationType, OrganizationsPage } from "./organizations";

export type TypeCount = { type: OrganizationType; count: number };

const parser = new XMLParser({
    ignoreAttributes: true,
    trimValues: true,
    parseTagValue: true, // научная нотация тоже в Number()
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

function xmlEscape(s: string): string {
    return s
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&apos;");
}

function parseOrganizationsPage(xml: string): OrganizationsPage {
    const parsed = parser.parse(xml);
    const root = parsed?.organizationsPage;
    if (!root) throw new Error("Unexpected XML: missing <organizationsPage>");

    const orgRaw = root?.organizations?.organization;
    const organizations: Organization[] = toArray(orgRaw).map((o: any) => ({
        id: toNumber(o?.id),
        name: toString(o?.name),
        creationDate: toString(o?.creationDate),
        annualTurnover: toNumber(o?.annualTurnover),
        fullName: toString(o?.fullName),
        coordinates: {
            x: toNumber(o?.coordinates?.x),
            y: toNumber(o?.coordinates?.y),
        },
        type: (toString(o?.type) || "COMMERCIAL") as OrganizationType,
        officialAddress: {
            street: toString(o?.officialAddress?.street),
            town: {
                x: toNumber(o?.officialAddress?.town?.x),
                y: toNumber(o?.officialAddress?.town?.y),
                name: toString(o?.officialAddress?.town?.name),
            },
        },
    }));

    return {
        organizations,
        page: toNumber(root?.page, 1),
        size: toNumber(root?.size, 20),
        totalElements: toNumber(root?.totalElements, 0),
        totalPages: toNumber(root?.totalPages, 1),
    };
}

export async function fetchTotalTurnover(signal?: AbortSignal): Promise<{
    totalTurnover: number;
    organizationCount: number;
}> {
    const res = await fetch("/soa/api/v1/organizations/turnover", {
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
    const root = parsed?.turnoverResponse;
    if (!root) throw new Error("Unexpected XML: missing <turnoverResponse>");

    return {
        totalTurnover: toNumber(root?.totalTurnover),
        organizationCount: toNumber(root?.organizationCount),
    };
}

export async function fetchTypeCounts(signal?: AbortSignal): Promise<TypeCount[]> {
    const res = await fetch("/soa/api/v1/organizations/types", {
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

    const root = parsed?.typeCounts;
    if (!root) throw new Error("Unexpected XML: missing <typeCounts>");

    const raw = root?.typeCount;
    return toArray(raw).map((t: any) => ({
        type: (toString(t?.type) || "COMMERCIAL") as OrganizationType,
        count: toNumber(t?.count),
    }));
}

export async function fetchOrganizationsLtFullName(params: {
    value: string;
    page?: number;
    size?: number;
    signal?: AbortSignal;
}): Promise<OrganizationsPage> {
    const page = params.page ?? 1;
    const size = params.size ?? 20;

    const url = new URL("/soa/api/v1/organizations/lt-full-name", window.location.origin);
    url.searchParams.set("page", String(page));
    url.searchParams.set("size", String(size));

    const body = `<fullNameValue><value>${xmlEscape(params.value)}</value></fullNameValue>`;

    const res = await fetch(url.toString(), {
        method: "POST",
        headers: {
            "Content-Type": "application/xml",
            Accept: "application/xml",
        },
        body,
        signal: params.signal,
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`HTTP ${res.status} ${res.statusText}${text ? ` — ${text}` : ""}`);
    }

    const xml = await res.text();
    return parseOrganizationsPage(xml);
}

export async function fireAllEmployees(orgId: number, signal?: AbortSignal): Promise<number> {
    const res = await fetch(`/orgmanager/api/v1/fire/all/${orgId}`, {
        method: "POST",
        headers: { Accept: "application/xml" },
        signal,
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`HTTP ${res.status} ${res.statusText}${text ? ` — ${text}` : ""}`);
    }

    const xml = await res.text();
    const parsed = parser.parse(xml);

    // <employeeCount>4</employeeCount>
    const count = parsed?.employeeCount;
    return toNumber(count);
}

export async function acquireOrganizations(
    acquirerId: number,
    acquiredId: number,
    signal?: AbortSignal
): Promise<{
    acquirer: { id: number; name: string };
    acquired: { id: number; name: string };
    moved: number;
}> {
    const res = await fetch(`/orgmanager/api/v1/acquire/${acquirerId}/${acquiredId}`, {
        method: "POST",
        headers: { Accept: "application/xml" },
        signal,
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`HTTP ${res.status} ${res.statusText}${text ? ` — ${text}` : ""}`);
    }

    const xml = await res.text();
    const parsed = parser.parse(xml);

    const root = parsed?.acquiring;
    if (!root) throw new Error("Unexpected XML: missing <acquiring>");

    const acq = root?.acquirerOrganization;
    const acd = root?.acquiredOrganization;

    return {
        acquirer: { id: toNumber(acq?.id), name: toString(acq?.name) },
        acquired: { id: toNumber(acd?.id), name: toString(acd?.name) },
        moved: toNumber(root?.numberOfEmployeesMoved),
    };
}

