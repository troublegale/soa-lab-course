import React from "react";

type Props = {
    open: boolean;
    organizationId: number | null;
    loading?: boolean;
    error?: string | null;
    onClose: () => void;
    onConfirm: () => void;
};

export function DeleteOrganizationModal({
                                            open,
                                            organizationId,
                                            loading = false,
                                            error = null,
                                            onClose,
                                            onConfirm,
                                        }: Props) {
    React.useEffect(() => {
        if (!open) return;
        const onKeyDown = (e: KeyboardEvent) => {
            if (e.key === "Escape") onClose();
        };
        window.addEventListener("keydown", onKeyDown);
        return () => window.removeEventListener("keydown", onKeyDown);
    }, [open, onClose]);

    if (!open || organizationId == null) return null;

    return (
        <div className="modalBackdrop" onMouseDown={onClose} role="presentation">
            <div
                className="modalCard"
                onMouseDown={(e) => e.stopPropagation()}
                role="dialog"
                aria-modal="true"
                aria-label="Delete organization"
                style={{ width: "min(520px, 100%)" }}
            >
                <div className="modalHeader">
                    <h3 className="modalTitle">{`Delete Organization ${organizationId}?`}</h3>
                    <button type="button" className="iconBtn" onClick={onClose} aria-label="Close" disabled={loading}>
                        ✕
                    </button>
                </div>

                <div className="modalBody">
                    {error && (
                        <div className="error" style={{ marginTop: 12 }}>
                            <b>Error:</b> {error}
                        </div>
                    )}

                    <div className="modalFooter">
                        <button type="button" onClick={onConfirm} disabled={loading}>
                            {loading ? "Deleting…" : "Yes"}
                        </button>
                        <button type="button" onClick={onClose} disabled={loading}>
                            No
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
