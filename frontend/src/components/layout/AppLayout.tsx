import { Outlet, NavLink } from "react-router-dom";
import { cn } from "@/lib/utils";

const navItems = [
  { to: "/clientes", label: "Clientes" },
  { to: "/produtos", label: "Produtos" },
  { to: "/categorias", label: "Categorias" },
  { to: "/unidades-medida", label: "Unidades de medida" },
  { to: "/orcamentos", label: "Orçamentos" },
  { to: "/transportadoras", label: "Transportadoras" },
  { to: "/fornecedores", label: "Fornecedores" },
  { to: "/compras", label: "Compras" },
  { to: "/pedidos", label: "Pedidos" },
  { to: "/vendas", label: "Vendas" },
  { to: "/contas-receber", label: "Contas a receber" },
  { to: "/contas-pagar", label: "Contas a pagar" },
  { to: "/categorias-despesa", label: "Categorias de despesa" },
  { to: "/estoque", label: "Estoque" },
];

export function AppLayout() {
  return (
    <div className="min-h-screen bg-background">
      <header className="border-b border-border bg-card">
        <div className="mx-auto flex min-h-16 max-w-6xl flex-wrap items-center gap-x-8 gap-y-1 px-4 py-2">
          <span className="text-lg font-medium">
            <span className="text-primary">Bella</span>
          </span>
          <nav className="flex flex-wrap gap-1">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  cn(
                    "whitespace-nowrap rounded-md px-3 py-2 text-sm font-medium transition-colors",
                    isActive
                      ? "bg-accent text-accent-foreground"
                      : "text-muted-foreground hover:bg-muted hover:text-foreground",
                  )
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  );
}
