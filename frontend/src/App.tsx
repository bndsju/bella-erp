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
import { FornecedoresPage } from "@/pages/FornecedoresPage";
import { ComprasListPage } from "@/pages/ComprasListPage";
import { CompraFormPage } from "@/pages/CompraFormPage";
import { PedidosListPage } from "@/pages/PedidosListPage";
import { PedidoFormPage } from "@/pages/PedidoFormPage";
import { VendasListPage } from "@/pages/VendasListPage";
import { VendaDetalhePage } from "@/pages/VendaDetalhePage";
import { ContasReceberListPage } from "@/pages/ContasReceberListPage";
import { ContaReceberFormPage } from "@/pages/ContaReceberFormPage";
import { ContaReceberDetalhePage } from "@/pages/ContaReceberDetalhePage";
import { ContasPagarListPage } from "@/pages/ContasPagarListPage";
import { ContaPagarFormPage } from "@/pages/ContaPagarFormPage";
import { ContaPagarDetalhePage } from "@/pages/ContaPagarDetalhePage";
import { CategoriasDespesaPage } from "@/pages/CategoriasDespesaPage";
import { EstoquePage } from "@/pages/EstoquePage";
import { MovimentacoesEstoquePage } from "@/pages/MovimentacoesEstoquePage";

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
        <Route path="/fornecedores" element={<FornecedoresPage />} />
        <Route path="/compras" element={<ComprasListPage />} />
        <Route path="/compras/novo" element={<CompraFormPage />} />
        <Route path="/compras/:id" element={<CompraFormPage />} />
        <Route path="/pedidos" element={<PedidosListPage />} />
        <Route path="/pedidos/novo" element={<PedidoFormPage />} />
        <Route path="/pedidos/:id/editar" element={<PedidoFormPage />} />
        <Route path="/vendas" element={<VendasListPage />} />
        <Route path="/vendas/:id" element={<VendaDetalhePage />} />
        <Route path="/contas-receber" element={<ContasReceberListPage />} />
        <Route path="/contas-receber/novo" element={<ContaReceberFormPage />} />
        <Route path="/contas-receber/:id/editar" element={<ContaReceberFormPage />} />
        <Route path="/contas-receber/:id" element={<ContaReceberDetalhePage />} />
        <Route path="/contas-pagar" element={<ContasPagarListPage />} />
        <Route path="/contas-pagar/novo" element={<ContaPagarFormPage />} />
        <Route path="/contas-pagar/:id/editar" element={<ContaPagarFormPage />} />
        <Route path="/contas-pagar/:id" element={<ContaPagarDetalhePage />} />
        <Route path="/categorias-despesa" element={<CategoriasDespesaPage />} />
        <Route path="/estoque" element={<EstoquePage />} />
        <Route path="/estoque/movimentacoes" element={<MovimentacoesEstoquePage />} />
      </Route>
    </Routes>
  );
}

export default App;
