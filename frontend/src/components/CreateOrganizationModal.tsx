import React from "react";
import type {OrganizationType} from "../api/organizations";
import {createOrganization} from "../api/organizations";


type Props = {
    open: boolean;
    onClose: () => void;
    onCreated?: () => void; // чтобы потом можно было обновить таблицу
};

const ORG_TYPES: OrganizationType[] = [
    "COMMERCIAL",
    "GOVERNMENT",
    "PRIVATE_LIMITED_COMPANY",
    "OPEN_JOINT_STOCK_COMPANY",
];

function formatOrgType(type: string): string {
    if (!type) return "";
    const words = type
        .toLowerCase()
        .split("_")
        .filter(Boolean);
    return words
        .map((w, i) => (i === 0 ? w[0].toUpperCase() + w.slice(1) : w))
        .join(" ");
}

type FormState = {
    name: string;
    coordinatesX: string;
    coordinatesY: string;
    annualTurnover: string;
    fullName: string;
    type: string;

    street: string;
    townX: string;
    townY: string;
    townName: string;
};

type Errors = Partial<Record<keyof FormState, string>>;

const initialState: FormState = {
    name: "",
    coordinatesX: "",
    coordinatesY: "",
    annualTurnover: "",
    fullName: "",
    type: "",

    street: "",
    townX: "",
    townY: "",
    townName: "",
};

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

export function CreateOrganizationModal({open, onClose, onCreated}: Props) {
    const [form, setForm] = React.useState<FormState>(initialState);
    const [errors, setErrors] = React.useState<Errors>({});
    const [submitting, setSubmitting] = React.useState(false);
    const [submitError, setSubmitError] = React.useState<string | null>(null);

    // Закрытие по ESC
    React.useEffect(() => {
        if (!open) return;
        const onKeyDown = (e: KeyboardEvent) => {
            if (e.key === "Escape") onClose();
        };
        window.addEventListener("keydown", onKeyDown);
        return () => window.removeEventListener("keydown", onKeyDown);
    }, [open, onClose]);

    // При открытии — чистим ошибки (и можно форму, если хочешь)
    React.useEffect(() => {
        if (open) setErrors({});
    }, [open]);

    if (!open) return null;

    const setField = <K extends keyof FormState>(key: K, value: string) => {
        setForm((prev) => ({...prev, [key]: value}));
        setErrors((prev) => ({...prev, [key]: undefined}));
    };

    const validate = (): { ok: boolean; errs: Errors } => {
        const e: Errors = {};

        // name: @NotNull @NotBlank
        if (isBlank(form.name)) e.name = "Name is required";

        // coordinates: required
        const cx = parseIntOrNull(form.coordinatesX);
        if (cx === null) {
            e.coordinatesX = form.coordinatesX.trim() === "" ? "X is required" : "X must be an integer";
        } else if (cx < -826) {
            e.coordinatesX = "X must be ≥ -826";
        }

        const cy = parseNumberOrNull(form.coordinatesY);
        if (cy === null) {
            e.coordinatesY = "Y is required";
        } else if (cy < -143) {
            e.coordinatesY = "Y must be ≥ -143";
        }

        // annualTurnover: @NotNull @Positive
        const at = parseNumberOrNull(form.annualTurnover);
        if (at === null) {
            e.annualTurnover = "Annual turnover is required";
        } else if (at <= 0) {
            e.annualTurnover = "Annual turnover must be > 0";
        }

        // fullName: @Pattern(^(?!\s*$).+) => optional, но если задано — не пробелы
        if (form.fullName.length > 0 && isBlank(form.fullName)) {
            e.fullName = "Full name cannot be blank";
        }

        // type: @NotNull
        if (!form.type) e.type = "Type is required";
        else if (!ORG_TYPES.includes(form.type as OrganizationType)) e.type = "Invalid type";

        // officialAddress: optional, но если начали заполнять — валидируем целиком
        const anyAddressFilled =
            !isBlank(form.street) ||
            !isBlank(form.townX) ||
            !isBlank(form.townY) ||
            !isBlank(form.townName);

        if (anyAddressFilled) {
            // street: @NotNull @Length(max=147)
            if (isBlank(form.street)) e.street = "Street is required";
            else if (form.street.trim().length > 147) e.street = "Street must be ≤ 147 chars";

            // town.x: @NotNull Float
            const tx = parseNumberOrNull(form.townX);
            if (tx === null) e.townX = "Town X is required";

            // town.y: @NotNull Long (integer)
            const ty = parseIntOrNull(form.townY);
            if (ty === null) {
                e.townY = form.townY.trim() === "" ? "Town Y is required" : "Town Y must be an integer";
            }

            // town.name: @NotNull String
            if (isBlank(form.townName)) e.townName = "Town name is required";
        }

        return {ok: Object.keys(e).length === 0, errs: e};
    };

    const onSubmit = (ev: React.FormEvent) => {
        ev.preventDefault();
        const v = validate();
        if (!v.ok) {
            setErrors(v.errs);
            return;
        }

        const cx = parseIntOrNull(form.coordinatesX)!;
        const cy = parseNumberOrNull(form.coordinatesY)!;
        const at = parseNumberOrNull(form.annualTurnover)!;

        // Собираем объект для бэка (id и creationDate НЕ отправляем)
        const anyAddressFilled =
            !isBlank(form.street) || !isBlank(form.townX) || !isBlank(form.townY) || !isBlank(form.townName);

        const payload = {
            name: form.name.trim(),
            coordinates: {x: cx, y: cy},
            annualTurnover: at,
            fullName: form.fullName.length ? form.fullName.trim() : undefined,
            type: form.type as OrganizationType,
            officialAddress: anyAddressFilled
                ? {
                    street: form.street.trim(),
                    town: {
                        x: parseNumberOrNull(form.townX)!,
                        y: parseIntOrNull(form.townY)!,
                        name: form.townName.trim(),
                    },
                }
                : undefined,
        };

        setSubmitting(true);
        setSubmitError(null);

        createOrganization(payload)
            .then(() => {
                setForm(initialState);
                onClose();
                onCreated?.(); // обновить таблицу
            })
            .catch((e: unknown) => {
                setSubmitError(e instanceof Error ? e.message : "Unknown error");
            })
            .finally(() => setSubmitting(false));
    };

    return (
        <div className="modalBackdrop" onMouseDown={onClose} role="presentation">
            <div
                className="modalCard"
                role="dialog"
                aria-modal="true"
                aria-label="Create organization"
                onMouseDown={(e) => e.stopPropagation()}
            >
                <div className="modalHeader">
                    <h3 className="modalTitle">Create organization</h3>
                    <button type="button" className="iconBtn" onClick={onClose} aria-label="Close">
                        ✕
                    </button>
                </div>

                <form onSubmit={onSubmit} className="modalBody">
                    <div className="grid2">
                        <div className="field">
                            <label>Name *</label>
                            <input value={form.name} onChange={(e) => setField("name", e.target.value)}/>
                            {errors.name && <div className="fieldError">{errors.name}</div>}
                        </div>

                        <div className="field">
                            <label>Type *</label>
                            <select value={form.type} onChange={(e) => setField("type", e.target.value)}>
                                <option value="" disabled>
                                    Select…
                                </option>
                                {ORG_TYPES.map((t) => (
                                    <option key={t} value={t}>
                                        {formatOrgType(t)}
                                    </option>
                                ))}
                            </select>
                            {errors.type && <div className="fieldError">{errors.type}</div>}
                        </div>

                        <div className="field">
                            <label>Coordinates X (integer, ≥ -826) *</label>
                            <input
                                inputMode="numeric"
                                value={form.coordinatesX}
                                onChange={(e) => setField("coordinatesX", e.target.value)}
                            />
                            {errors.coordinatesX && <div className="fieldError">{errors.coordinatesX}</div>}
                        </div>

                        <div className="field">
                            <label>Coordinates Y (≥ -143) *</label>
                            <input
                                inputMode="decimal"
                                value={form.coordinatesY}
                                onChange={(e) => setField("coordinatesY", e.target.value)}
                            />
                            {errors.coordinatesY && <div className="fieldError">{errors.coordinatesY}</div>}
                        </div>

                        <div className="field">
                            <label>Annual turnover (&gt; 0) *</label>
                            <input
                                inputMode="decimal"
                                value={form.annualTurnover}
                                onChange={(e) => setField("annualTurnover", e.target.value)}
                            />
                            {errors.annualTurnover && <div className="fieldError">{errors.annualTurnover}</div>}
                        </div>

                        <div className="field">
                            <label>Full name (optional, not blank)</label>
                            <input value={form.fullName} onChange={(e) => setField("fullName", e.target.value)}/>
                            {errors.fullName && <div className="fieldError">{errors.fullName}</div>}
                        </div>
                    </div>

                    <div className="sectionTitle">Official address (optional)</div>

                    <div className="grid2">
                        <div className="field">
                            <label>Street (≤ 147)</label>
                            <input value={form.street} onChange={(e) => setField("street", e.target.value)}/>
                            {errors.street && <div className="fieldError">{errors.street}</div>}
                        </div>

                        <div className="field">
                            <label>Town name</label>
                            <input value={form.townName} onChange={(e) => setField("townName", e.target.value)}/>
                            {errors.townName && <div className="fieldError">{errors.townName}</div>}
                        </div>

                        <div className="field">
                            <label>Town X</label>
                            <input inputMode="decimal" value={form.townX}
                                   onChange={(e) => setField("townX", e.target.value)}/>
                            {errors.townX && <div className="fieldError">{errors.townX}</div>}
                        </div>

                        <div className="field">
                            <label>Town Y (integer)</label>
                            <input inputMode="numeric" value={form.townY}
                                   onChange={(e) => setField("townY", e.target.value)}/>
                            {errors.townY && <div className="fieldError">{errors.townY}</div>}
                        </div>
                    </div>
                    {submitError && (
                        <div className="error" style={{marginTop: 12}}>
                            <b>Ошибка:</b> {submitError}
                        </div>
                    )}
                    <div className="modalFooter">
                        <button type="button" onClick={onClose} disabled={submitting}>
                            Cancel
                        </button>
                        <button type="submit" disabled={submitting}>
                            {submitting ? "Creating…" : "Create"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
