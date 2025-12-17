// src/api/organizations.ts
import { XMLParser } from "fast-xml-parser";

export type OrganizationType =
    | "COMMERCIAL"
    | "GOVERNMENT"
    | "PRIVATE_LIMITED_COMPANY"
    | "OPEN_JOINT_STOCK_COMPANY";

export interface Location {
    x: number;
    y: number;
    name: string;
}

export interface Address {
    street: string;
    town: Location;
}

export interface Coordinates {
    x: number;
    y: number;
}

export interface Organization {
    id: number;
    name: string;
    creationDate: string; // "2025-12-14"
    annualTurnover: number;
    fullName: string;
    coordinates: Coordinates;
    type: OrganizationType;
    officialAddress: Address;
}

export interface OrganizationsPage {
    organizations: Organization[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

const parser = new XMLParser({
    ignoreAttributes: true,
    trimValues: true,
    parseTagValue: true, // превращает "1" -> 1, "150000.0" -> 150000 и т.д.
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

export async function fetchOrganizationsPage(params?: {
    page?: number;
    size?: number;
    signal?: AbortSignal;
}): Promise<OrganizationsPage> {
    const page = params?.page ?? 1;
    const size = params?.size ?? 20;

    const url = new URL("/soa/api/v1/organizations", window.location.origin);
    url.searchParams.set("page", String(page));
    url.searchParams.set("size", String(size));

    const res = await fetch(url.toString(), {
        method: "GET",
        headers: { Accept: "application/xml" },
        signal: params?.signal,
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`HTTP ${res.status} ${res.statusText}${text ? ` — ${text}` : ""}`);
    }

    const xml = await res.text();
    const parsed = parser.parse(xml);

    // Строго под твой пример
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
        page: toNumber(root?.page, page),
        size: toNumber(root?.size, size),
        totalElements: toNumber(root?.totalElements, 0),
        totalPages: toNumber(root?.totalPages, 1),
    };
}

export interface OrganizationCreateRequest {
    name: string;
    coordinates: { x: number; y: number };
    annualTurnover: number;
    fullName?: string; // optional
    type: OrganizationType;
    officialAddress?: {
        street: string;
        town: { x: number; y: number; name: string };
    }; // optional
}

function xmlEscape(s: string): string {
    return s
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&apos;");
}

// Если вдруг бэк ждёт другой root-тег — поменяй тут на "OrganizationRequest" или "organization"
const CREATE_ROOT = "organization";

function buildCreateOrganizationXml(req: OrganizationCreateRequest): string {
    const parts: string[] = [];
    parts.push(`<${CREATE_ROOT}>`);

    parts.push(`<name>${xmlEscape(req.name)}</name>`);

    parts.push(`<coordinates>`);
    parts.push(`<x>${req.coordinates.x}</x>`);
    parts.push(`<y>${req.coordinates.y}</y>`);
    parts.push(`</coordinates>`);

    parts.push(`<annualTurnover>${req.annualTurnover}</annualTurnover>`);

    if (req.fullName != null && req.fullName !== "") {
        parts.push(`<fullName>${xmlEscape(req.fullName)}</fullName>`);
    }

    parts.push(`<type>${xmlEscape(req.type)}</type>`);

    // officialAddress опционален: если не задан — не добавляем узел => на бэке будет null
    if (req.officialAddress) {
        parts.push(`<officialAddress>`);
        parts.push(`<street>${xmlEscape(req.officialAddress.street)}</street>`);
        parts.push(`<town>`);
        parts.push(`<x>${req.officialAddress.town.x}</x>`);
        parts.push(`<y>${req.officialAddress.town.y}</y>`);
        parts.push(`<name>${xmlEscape(req.officialAddress.town.name)}</name>`);
        parts.push(`</town>`);
        parts.push(`</officialAddress>`);
    }

    parts.push(`</${CREATE_ROOT}>`);
    return parts.join("");
}

export async function createOrganization(req: OrganizationCreateRequest): Promise<void> {
    const xml = buildCreateOrganizationXml(req);

    const res = await fetch("/soa/api/v1/organizations", {
        method: "POST",
        headers: {
            "Content-Type": "application/xml",
            Accept: "application/xml",
        },
        body: xml,
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`HTTP ${res.status} ${res.statusText}${text ? ` — ${text}` : ""}`);
    }
}

export async function updateOrganization(id: number, req: OrganizationCreateRequest): Promise<void> {
    const xml = buildCreateOrganizationXml(req); // тот же XML, что и для create

    const res = await fetch(`/soa/api/v1/organizations/${id}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/xml",
            Accept: "application/xml",
        },
        body: xml,
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`HTTP ${res.status} ${res.statusText}${text ? ` — ${text}` : ""}`);
    }
}


export type SortField =
    | "id"
    | "name"
    | "fullName"
    | "creationDate"
    | "annualTurnover"
    | "type"
    | "coordinates"
    | "officialAddress";

function buildOrganizationsQueryXml(sort?: { field: SortField; desc: boolean } | null): string {
    const sortValue = sort ? `${sort.desc ? "-" : ""}${sort.field}` : null;

    return [
        "<query>",
        "<sort>",
        ...(sortValue ? [`<sort>${sortValue}</sort>`] : []),
        "</sort>",
        "</query>",
    ].join("");
}

export async function fetchOrganizationsPageQuery(params?: {
    page?: number;
    size?: number;
    sort?: { field: SortField; desc: boolean } | null;
    signal?: AbortSignal;
}): Promise<OrganizationsPage> {
    const page = params?.page ?? 1;
    const size = params?.size ?? 20;

    const url = new URL("/soa/api/v1/organizations/query", window.location.origin);
    url.searchParams.set("page", String(page));
    url.searchParams.set("size", String(size));

    const body = buildOrganizationsQueryXml(params?.sort ?? null);

    const res = await fetch(url.toString(), {
        method: "POST",
        headers: {
            "Content-Type": "application/xml",
            Accept: "application/xml",
        },
        body,
        signal: params?.signal,
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`HTTP ${res.status} ${res.statusText}${text ? ` — ${text}` : ""}`);
    }

    const xml = await res.text();
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
        page: toNumber(root?.page, page),
        size: toNumber(root?.size, size),
        totalElements: toNumber(root?.totalElements, 0),
        totalPages: toNumber(root?.totalPages, 1),
    };
}

export async function deleteOrganization(id: number): Promise<void> {
    const res = await fetch(`/soa/api/v1/organizations/${id}`, {
        method: "DELETE",
        headers: { Accept: "application/xml" },
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`HTTP ${res.status} ${res.statusText}${text ? ` — ${text}` : ""}`);
    }
}

export type QueryNumOp = "eq" | "gt" | "ge" | "lt" | "le";
export type QueryStrOp = "eq" | "contains" | "startsWith" | "endsWith";
export type QueryDateOp = "eq" | "before" | "after";

export type OrgQuerySortField =
    | "id"
    | "name"
    | "fullName"
    | "creationDate"
    | "annualTurnover"
    | "type"
    | "coordinates"
    | "officialAddress";

export type OrganizationQueryState = {
    sort?: { field: OrgQuerySortField; desc: boolean } | null;

    id?: { op: QueryNumOp; value: number } | null;
    name?: { op: QueryStrOp; value: string } | null;

    coordX?: { op: QueryNumOp; value: number } | null;
    coordY?: { op: QueryNumOp; value: number } | null;

    creationDate?: { op: QueryDateOp; valueIso: string } | null; // YYYY-MM-DD

    annualTurnover?: { op: QueryNumOp; value: number } | null;

    fullName?: { op: QueryStrOp; value: string } | null;

    type?: { value: OrganizationType } | null;

    addressStreet?: { op: QueryStrOp; value: string } | null;
    addressTownName?: { op: QueryStrOp; value: string } | null;
    addressTownX?: { op: QueryNumOp; value: number } | null; // float ok
    addressTownY?: { op: QueryNumOp; value: number } | null; // integer на бэке, но передаём как число
};

function buildOpValueXml(tag: string, op: string, value: string): string {
    return `<${tag}><${op}>${xmlEscape(value)}</${op}></${tag}>`;
}

function buildOrganizationQueryXml(q: OrganizationQueryState): string {
    const parts: string[] = [];
    parts.push("<organizationQuery>");

    if (q.sort) {
        const s = `${q.sort.desc ? "-" : ""}${q.sort.field}`;
        parts.push(`<sort><sort>${xmlEscape(s)}</sort></sort>`);
    }

    if (q.id) parts.push(buildOpValueXml("idFilter", q.id.op, String(q.id.value)));
    if (q.name) parts.push(buildOpValueXml("nameFilter", q.name.op, q.name.value));

    // coordinatesFilter только если есть x/y
    if (q.coordX || q.coordY) {
        parts.push("<coordinatesFilter>");
        if (q.coordX) parts.push(buildOpValueXml("xFilter", q.coordX.op, String(q.coordX.value)));
        if (q.coordY) parts.push(buildOpValueXml("yFilter", q.coordY.op, String(q.coordY.value)));
        parts.push("</coordinatesFilter>");
    }

    if (q.creationDate) parts.push(buildOpValueXml("creationDateFilter", q.creationDate.op, q.creationDate.valueIso));

    if (q.annualTurnover)
        parts.push(buildOpValueXml("annualTurnoverFilter", q.annualTurnover.op, String(q.annualTurnover.value)));

    if (q.fullName) parts.push(buildOpValueXml("fullNameFilter", q.fullName.op, q.fullName.value));

    if (q.type) parts.push(`<typeFilter><eq>${xmlEscape(q.type.value)}</eq></typeFilter>`);

    // addressFilter только если что-то из адреса задано
    if (q.addressStreet || q.addressTownName || q.addressTownX || q.addressTownY) {
        parts.push("<officialAddressFilter>");

        if (q.addressStreet) parts.push(buildOpValueXml("streetFilter", q.addressStreet.op, q.addressStreet.value));

        // locationFilter только если что-то из town задано
        if (q.addressTownName || q.addressTownX || q.addressTownY) {
            parts.push("<townFilter>");
            if (q.addressTownX) parts.push(buildOpValueXml("xFilter", q.addressTownX.op, String(q.addressTownX.value)));
            if (q.addressTownY) parts.push(buildOpValueXml("yFilter", q.addressTownY.op, String(q.addressTownY.value)));
            if (q.addressTownName) parts.push(buildOpValueXml("nameFilter", q.addressTownName.op, q.addressTownName.value));
            parts.push("</townFilter>");
        }

        parts.push("</officialAddressFilter>");
    }

    parts.push("</organizationQuery>");
    return parts.join("");
}

export async function fetchOrganizationsByOrganizationQuery(params: {
    page: number;
    size: number;
    query: OrganizationQueryState;
    signal?: AbortSignal;
}): Promise<OrganizationsPage> {
    const url = new URL("/soa/api/v1/organizations/query", window.location.origin);
    url.searchParams.set("page", String(params.page));
    url.searchParams.set("size", String(params.size));

    const body = buildOrganizationQueryXml(params.query);

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
        page: toNumber(root?.page, params.page),
        size: toNumber(root?.size, params.size),
        totalElements: toNumber(root?.totalElements, 0),
        totalPages: toNumber(root?.totalPages, 1),
    };
}
