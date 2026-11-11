import { ROTULO_TIPO, formatarQuantidade } from "@/lib/estoqueFormatos";
import { formatarMoeda } from "@/lib/formatos";
import type { ModuloAuditoria, OperacaoAuditoria, RegistroAuditoria } from "@/types/auditoria";

export const ROTULO_MODULO: Record<ModuloAuditoria, string> = {
  CLIENTE: "Cliente",
  PRODUTO: "Produto",
  ORCAMENTO: "Orçamento",
  PEDIDO: "Pedido",
  VENDA: "Venda",
  COMPRA: "Compra",
  ESTOQUE: "Estoque",
  CONTA_RECEBER: "Conta a receber",
  CONTA_PAGAR: "Conta a pagar",
};

export const MODULOS = Object.keys(ROTULO_MODULO) as ModuloAuditoria[];

const ROTULO_OPERACAO: Record<OperacaoAuditoria, string> = {
  CRIACAO: "Criação",
  ALTERACAO: "Alteração",
  INATIVACAO: "Inativação",
  REATIVACAO: "Reativação",
  CANCELAMENTO: "Cancelamento",
  ALTERACAO_STATUS: "Mudança de status",
  REGISTRO_PAGAMENTO: "Pagamento",
  MOVIMENTACAO_ESTOQUE: "Movimentação de estoque",
};

export const OPERACOES = Object.keys(ROTULO_OPERACAO) as OperacaoAuditoria[];

export function rotuloOperacao(operacao: OperacaoAuditoria, modulo?: ModuloAuditoria): string {
  if (operacao === "REGISTRO_PAGAMENTO") return modulo === "CONTA_RECEBER" ? "Recebimento" : "Pagamento";
  return ROTULO_OPERACAO[operacao];
}

/** Cor da etiqueta: o que cria/reativa é "ok"; o que cancela/inativa é "erro"; o resto é neutro. */
export function varianteOperacao(operacao: OperacaoAuditoria): "default" | "destructive" | "outline" {
  if (operacao === "CRIACAO" || operacao === "REATIVACAO") return "default";
  if (operacao === "CANCELAMENTO" || operacao === "INATIVACAO") return "destructive";
  return "outline";
}

/** Tela do registro afetado, quando existe (movimentações de estoque não têm tela própria). */
export function rotaDoRegistro(modulo: ModuloAuditoria, entidadeId: string): string | null {
  switch (modulo) {
    case "CLIENTE":
      return `/clientes/${entidadeId}/editar`;
    case "PRODUTO":
      return `/produtos/${entidadeId}/editar`;
    case "ORCAMENTO":
      return `/orcamentos/${entidadeId}/editar`;
    case "PEDIDO":
      return `/pedidos/${entidadeId}/editar`;
    case "VENDA":
      return `/vendas/${entidadeId}`;
    case "COMPRA":
      return `/compras/${entidadeId}`;
    case "CONTA_RECEBER":
      return `/contas-receber/${entidadeId}`;
    case "CONTA_PAGAR":
      return `/contas-pagar/${entidadeId}`;
    default:
      return null;
  }
}

// ---------- campos e valores ----------

const ROTULO_CAMPO: Record<string, string> = {
  status: "Status",
  nome: "Nome",
  nomeCompleto: "Nome completo",
  razaoSocial: "Razão social",
  nomeFantasia: "Nome fantasia",
  cpf: "CPF",
  cnpj: "CNPJ",
  email: "E-mail",
  celular: "Celular",
  telefone: "Telefone",
  tipoPessoa: "Tipo de pessoa",
  endereco: "Endereço",
  uf: "UF",
  cep: "CEP",
  bairro: "Bairro",
  cidade: "Cidade",
  logradouro: "Logradouro",
  numero: "Número",
  complemento: "Complemento",
  descricao: "Descrição",
  codigoInterno: "Código interno",
  codigoBarras: "Código de barras",
  precoCusto: "Preço de custo",
  precoVenda: "Preço de venda",
  estoqueMinimo: "Estoque mínimo",
  categoriaId: "Categoria",
  unidadeMedidaId: "Unidade de medida",
  clienteId: "Cliente",
  fornecedorId: "Fornecedor",
  produtoId: "Produto",
  transportadoraId: "Transportadora",
  itens: "Item",
  parcelas: "Parcela",
  pagamentos: "Pagamento",
  recebimentos: "Recebimento",
  quantidade: "Quantidade",
  valorUnitario: "Valor unitário",
  custoUnitario: "Custo unitário",
  valorBruto: "Valor bruto",
  subtotal: "Subtotal",
  valor: "Valor",
  valorTotal: "Valor total",
  valorProdutos: "Valor dos produtos",
  desconto: "Desconto",
  descontoTotal: "Desconto total",
  frete: "Frete",
  outrasDespesas: "Outras despesas",
  juros: "Juros",
  multa: "Multa",
  valorPago: "Valor pago",
  valorRecebido: "Valor recebido",
  valorAbatido: "Valor abatido",
  saldoPendente: "Saldo pendente",
  vencimento: "Vencimento",
  dataCompra: "Data da compra",
  previsaoRecebimento: "Previsão de recebimento",
  dataRecebimento: "Data do recebimento",
  confirmadaEm: "Confirmada em",
  criadoEm: "Criado em",
  atualizadoEm: "Atualizado em",
  dataHora: "Data e hora",
  condicaoPagamento: "Condição de pagamento",
  formaPagamento: "Forma de pagamento",
  observacao: "Observação",
  observacoes: "Observações",
  motivo: "Motivo",
  motivoCancelamento: "Motivo do cancelamento",
  tipo: "Tipo",
  origem: "Origem",
  origemOperacaoId: "Operação de origem",
  origemReferenciaId: "Documento de origem",
  categoriaDespesaId: "Categoria de despesa",
  dataLancamento: "Data do lançamento",
  canceladaEm: "Cancelada em",
};

/** Campos técnicos que só atrapalham a leitura (o histórico já mostra quem e quando). */
const CAMPOS_OCULTOS = new Set(["id", "atualizadoEm"]);

function humanizar(texto: string): string {
  const separado = texto.replace(/([a-z0-9])([A-Z])/g, "$1 $2").replace(/_/g, " ").toLowerCase();
  return separado.charAt(0).toUpperCase() + separado.slice(1);
}

function rotuloCampo(chave: string): string {
  if (ROTULO_CAMPO[chave]) return ROTULO_CAMPO[chave];
  // "categoriaDespesaId" -> "Categoria despesa": o valor é um código, o campo é o que ele aponta.
  return humanizar(chave.length > 2 && chave.endsWith("Id") ? chave.slice(0, -2) : chave);
}

const REGEX_INSTANTE = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/;
const REGEX_DATA = /^\d{4}-\d{2}-\d{2}$/;
const REGEX_UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const REGEX_ENUM = /^[A-Z][A-Z0-9_]+$/;
const REGEX_DINHEIRO = /valor|preco|saldo|total|frete|desconto|juros|multa|custo|subtotal|despesas|abatido|pago|recebido/i;

const ROTULO_VALOR: Record<string, string> = {
  ...ROTULO_MODULO,
  ATIVO: "Ativo",
  INATIVO: "Inativo",
  MANUAL: "Manual",
  PF: "Pessoa física",
  PJ: "Pessoa jurídica",
  CONCLUIDA: "Concluída",
  ENTREGUE: "Entregue",
  EM_SEPARACAO: "Em separação",
  PRONTO_PARA_ENTREGA: "Pronto para entrega",
  ...ROTULO_TIPO,
};

function formatarTexto(valor: string): string {
  if (REGEX_INSTANTE.test(valor)) {
    return new Date(valor).toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });
  }
  if (REGEX_DATA.test(valor)) return valor.split("-").reverse().join("/");
  if (REGEX_UUID.test(valor)) return `#${valor.slice(0, 8)}`;
  if (REGEX_ENUM.test(valor)) return ROTULO_VALOR[valor] ?? humanizar(valor);
  return valor;
}

export function formatarValorAuditoria(chave: string, valor: unknown): string {
  if (valor === null || valor === undefined) return "—";
  if (typeof valor === "boolean") return valor ? "Sim" : "Não";
  if (typeof valor === "number") {
    return REGEX_DINHEIRO.test(chave) && !/quantidade/i.test(chave) ? formatarMoeda(valor) : formatarQuantidade(valor);
  }
  if (typeof valor === "string") return valor === "" ? "—" : formatarTexto(valor);
  if (Array.isArray(valor)) return valor.length === 0 ? "(vazio)" : `${valor.length} registro(s)`;
  return "(vazio)";
}

// ---------- comparação ----------

interface CampoAchatado {
  /** Texto do campo para o leitor, ex.: "Parcela 1 › Saldo pendente". */
  rotulo: string;
  /** Último nome do caminho (decide como formatar o valor). */
  chave: string;
  valor: unknown;
}

function achatar(valor: unknown, caminho: string[], saida: Map<string, CampoAchatado>): void {
  const eVazio = (v: unknown) =>
    (Array.isArray(v) && v.length === 0) || (typeof v === "object" && v !== null && !Array.isArray(v) && Object.keys(v).length === 0);

  if (valor !== null && typeof valor === "object" && !eVazio(valor)) {
    if (Array.isArray(valor)) {
      valor.forEach((item, indice) => achatar(item, [...caminho, String(indice + 1)], saida));
    } else {
      for (const [chave, item] of Object.entries(valor as Record<string, unknown>)) {
        achatar(item, [...caminho, chave], saida);
      }
    }
    return;
  }

  const ultimo = [...caminho].reverse().find((parte) => !/^\d+$/.test(parte)) ?? "";
  if (CAMPOS_OCULTOS.has(caminho[caminho.length - 1])) return;

  const partes: string[] = [];
  for (const parte of caminho) {
    if (/^\d+$/.test(parte) && partes.length > 0) partes[partes.length - 1] += ` ${parte}`;
    else partes.push(rotuloCampo(parte));
  }
  saida.set(caminho.join("."), {
    rotulo: partes.join(" › "),
    chave: ultimo,
    valor: eVazio(valor) ? [] : valor,
  });
}

function mapaDe(valores: Record<string, unknown> | null): Map<string, CampoAchatado> {
  const saida = new Map<string, CampoAchatado>();
  if (valores) achatar(valores, [], saida);
  return saida;
}

export interface LinhaComparacao {
  campo: string;
  antes: string;
  depois: string;
}

/** Só os campos que mudaram, lado a lado. */
export function compararValores(registro: RegistroAuditoria): LinhaComparacao[] {
  const antes = mapaDe(registro.valoresAnteriores);
  const depois = mapaDe(registro.novosValores);
  const linhas: LinhaComparacao[] = [];
  for (const caminho of new Set([...depois.keys(), ...antes.keys()])) {
    const a = antes.get(caminho);
    const d = depois.get(caminho);
    if (JSON.stringify(a?.valor) === JSON.stringify(d?.valor)) continue;
    const base = d ?? a!;
    const textoAntes = a ? formatarValorAuditoria(base.chave, a.valor) : "—";
    const textoDepois = d ? formatarValorAuditoria(base.chave, d.valor) : "—";
    // "ausente" e "vazio" são a mesma coisa para quem lê: não é mudança.
    const vazio = (texto: string) => texto === "—" || texto === "(vazio)";
    if (vazio(textoAntes) && vazio(textoDepois)) continue;
    linhas.push({ campo: base.rotulo, antes: textoAntes, depois: textoDepois });
  }
  return linhas;
}

/** Dados gravados na criação (ou no estado completo), um campo por linha. */
export function listarValores(valores: Record<string, unknown> | null): { campo: string; valor: string }[] {
  return [...mapaDe(valores).values()]
    .filter((campo) => !(Array.isArray(campo.valor) && campo.valor.length === 0))
    .filter((campo) => campo.valor !== null && campo.valor !== undefined && campo.valor !== "")
    .map((campo) => ({ campo: campo.rotulo, valor: formatarValorAuditoria(campo.chave, campo.valor) }));
}

// ---------- resumo de uma linha ----------

function valorTopo(valores: Record<string, unknown> | null, chave: string): unknown {
  return valores ? valores[chave] : undefined;
}

/** Frase curta para a lista: o que mudou sem abrir os detalhes. */
export function resumoDoRegistro(registro: RegistroAuditoria): string {
  const { operacao, valoresAnteriores: antes, novosValores: depois, motivo } = registro;

  const statusAntes = valorTopo(antes, "status");
  const statusDepois = valorTopo(depois, "status");
  const mudancaStatus =
    typeof statusDepois === "string" && statusAntes !== statusDepois
      ? `${typeof statusAntes === "string" ? formatarTexto(statusAntes) : "—"} → ${formatarTexto(statusDepois)}`
      : null;

  switch (operacao) {
    case "CRIACAO": {
      const nome = ["nome", "nomeCompleto", "razaoSocial", "descricao"]
        .map((chave) => valorTopo(depois, chave))
        .find((valor) => typeof valor === "string" && valor !== "");
      const total = valorTopo(depois, "valorTotal");
      const partes = [
        typeof nome === "string" ? nome : null,
        typeof total === "number" ? formatarMoeda(total) : null,
      ].filter(Boolean);
      return partes.length > 0 ? partes.join(" · ") : "Registro criado";
    }
    case "ALTERACAO_STATUS":
    case "CANCELAMENTO":
    case "INATIVACAO":
    case "REATIVACAO": {
      const base = mudancaStatus ? `Status: ${mudancaStatus}` : "Status alterado";
      return motivo ? `${base} · ${motivo}` : base;
    }
    case "REGISTRO_PAGAMENTO": {
      const saldoAntes = valorTopo(antes, "saldoPendente");
      const saldoDepois = valorTopo(depois, "saldoPendente");
      const saldo =
        typeof saldoAntes === "number" && typeof saldoDepois === "number"
          ? `Saldo ${formatarMoeda(saldoAntes)} → ${formatarMoeda(saldoDepois)}`
          : null;
      return [mudancaStatus ? `Status: ${mudancaStatus}` : null, saldo].filter(Boolean).join(" · ") || "Pagamento registrado";
    }
    case "MOVIMENTACAO_ESTOQUE": {
      const tipo = valorTopo(depois, "tipo");
      const quantidade = valorTopo(depois, "quantidade");
      const base =
        typeof tipo === "string" && typeof quantidade === "number"
          ? `${ROTULO_TIPO[tipo as keyof typeof ROTULO_TIPO] ?? humanizar(tipo)} de ${formatarQuantidade(quantidade)}`
          : "Movimentação registrada";
      return motivo ? `${base} · ${motivo}` : base;
    }
    default: {
      const quantidade = compararValores(registro).length;
      return quantidade > 0 ? `${quantidade} campo(s) alterado(s)` : "Registro alterado";
    }
  }
}
