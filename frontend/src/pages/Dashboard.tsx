import React from "react";
import { OrganizationsTable } from "../components/OrganizationsTable";
import { CreateOrganizationModal } from "../components/CreateOrganizationModal";
import EmployeesTab from "./EmployeesTab";
import AdditionalOperationsTab from "./AdditionalOperationsTab";
import OrganizationQueryTab from "./OrganizationQueryTab";

type TabKey = "all" | "query" | "employees" | "ops";

const TABS: { key: TabKey; label: string }[] = [
    { key: "all", label: "All Organizations" },
    { key: "query", label: "Organization Query" },
    { key: "employees", label: "Employees" },
    { key: "ops", label: "Additional Operations" },
];

export default function Dashboard() {
    const [tab, setTab] = React.useState<TabKey>("all");
    const [createOpen, setCreateOpen] = React.useState(false);
    const [refreshToken, setRefreshToken] = React.useState(0);

    return (
        <main className="page">
            <div className="tabs">
                {TABS.map((t) => (
                    <button
                        key={t.key}
                        className={`tabBtn ${tab === t.key ? "active" : ""}`}
                        onClick={() => setTab(t.key)}
                    >
                        {t.label}
                    </button>
                ))}
            </div>

            <div className="tabPanel">
                {tab === "all" && (
                    <>
                        <div className="allToolbar">
                            <button onClick={() => setCreateOpen(true)}>Create</button>
                        </div>

                        <OrganizationsTable
                            refreshToken={refreshToken}
                            onMutate={() => setRefreshToken((x) => x + 1)}
                        />

                        <CreateOrganizationModal
                            open={createOpen}
                            onClose={() => setCreateOpen(false)}
                            onCreated={() => setRefreshToken((x) => x + 1)}
                        />
                    </>
                )}

                {tab === "query" && <OrganizationQueryTab />}
                {tab === "employees" && <EmployeesTab />}
                {tab === "ops" && <AdditionalOperationsTab />}
            </div>
        </main>
    );
}
