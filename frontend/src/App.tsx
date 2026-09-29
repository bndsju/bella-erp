import { Navigate, Route, Routes } from "react-router-dom";
import { AppLayout } from "@/components/layout/AppLayout";
import { ClientesListPage } from "@/pages/ClientesListPage";
import { ClienteFormPage } from "@/pages/ClienteFormPage";
import { ProdutosListPage } from "@/pages/ProdutosListPage";
import { ProdutoFormPage } from "@/pages/ProdutoFormPage";
import { CategoriasPage } from "@/pages/CategoriasPage";
import { UnidadesMedidaPage } from "@/pages/UnidadesMedidaPage";
import { OrcamentosListPage } from "@/pages/OrcamentosListPage";
import { OrcamentoFormPage } from "@/pages/OrcamentoFormPage";
import { TransportadorasPage } from "@/pages/TransportadorasPage";
import { PedidosListPage } from "@/pages/PedidosListPage";
import { PedidoFormPage } from "@/pages/PedidoFormPage";
import { VendasListPage } from "@/pages/VendasListPage";
import { VendaDetalhePage } from "@/pages/VendaDetalhePage";

function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="/" element={<Navigate to="/clientes" replace />} />
        <Route path="/clientes" element={<ClientesListPage />} />
        <Route path="/clientes/novo" element={<ClienteFormPage />} />
        <Route path="/clientes/:id/editar" element={<ClienteFormPage />} />
        <Route path="/produtos" element={<ProdutosListPage />} />
        <Route path="/produtos/novo" element={<ProdutoFormPage />} />
        <Route path="/produtos/:id/editar" element={<ProdutoFormPage />} />
        <Route path="/categorias" element={<CategoriasPage />} />
        <Route path="/unidades-medida" element={<UnidadesMedidaPage />} />
        <Route path="/orcamentos" element={<OrcamentosListPage />} />
        <Route path="/orcamentos/novo" element={<OrcamentoFormPage />} />
        <Route path="/orcamentos/:id/editar" element={<OrcamentoFormPage />} />
        <Route path="/transportadoras" element={<TransportadorasPage />} />
        <Route path="/pedidos" element={<PedidosListPage />} />
        <Route path="/pedidos/novo" element={<PedidoFormPage />} />
        <Route path="/pedidos/:id/editar" element={<PedidoFormPage />} />
        <Route path="/vendas" element={<VendasListPage />} />
        <Route path="/vendas/:id" element={<VendaDetalhePage />} />
      </Route>
    </Routes>
  );
}

export default App;
