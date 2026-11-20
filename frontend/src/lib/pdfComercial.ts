import { toast } from "sonner";
import { clienteApi } from "@/lib/clienteApi";
import { formatarCnpj, formatarCpf } from "@/lib/documento";
import { dataDoInstante, formatarData, formatarMoeda } from "@/lib/formatos";
import { formatarCep, formatarTelefone } from "@/lib/masks";
import { carregarBibliotecasPdf } from "@/lib/pdf";
import { ROTULO_STATUS_ORCAMENTO, ROTULO_STATUS_PEDIDO } from "@/lib/relatorioRotulos";
import type { ClienteResponse } from "@/types/cliente";
import type { OrcamentoResponse } from "@/types/orcamento";
import type { PedidoResponse } from "@/types/pedido";

/** Dados do cliente que aparecem no documento. */
interface DadosCliente {
  nome: string;
  documento?: string;
  contatos: string[];
  endereco?: string;
}

interface ItemDocumento {
  codigo: string;
  nome: string;
  quantidade: number;
  valorUnitario: number;
  subtotal: number;
}

/** Tudo o que o PDF mostra, já em texto: o desenho em si não conhece orçamento nem pedido. */
interface DocumentoComercial {
  tipo: "ORÇAMENTO" | "PEDIDO";
  codigo: string;
  meta: [string, string][];
  cliente: DadosCliente;
  itens: ItemDocumento[];
  subtotal: number;
  percentualDesconto: number;
  valorDesconto: number;
  valorFrete: number;
  valorTotal: number;
  /** Blocos de texto no fim (condição de pagamento, observações...); os vazios não aparecem. */
  secoes: [string, string | null | undefined][];
  nomeArquivo: string;
}

type Cor = [number, number, number];
const VERDE_ESCURO: Cor = [61, 89, 25];
const TEXTO: Cor = [35, 31, 32];
const AUXILIAR: Cor = [110, 110, 110];

/** Os documentos não têm numeração sequencial: o código é o começo do identificador, o mesmo da auditoria. */
function codigoCurto(id: string): string {
  return id.slice(0, 8).toUpperCase();
}

function paraNomeDeArquivo(texto: string): string {
  return texto
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, 40);
}

function dadosDoCliente(cliente: ClienteResponse): DadosCliente {
  const ehEmpresa = cliente.tipoPessoa === "PJ";
  const razao = cliente.razaoSocial ?? "";
  const fantasia = cliente.nomeFantasia ?? "";
  const nome = ehEmpresa
    ? fantasia && fantasia !== razao
      ? `${razao} (${fantasia})`
      : razao
    : (cliente.nomeCompleto ?? "");
  const documentoPrincipal = ehEmpresa
    ? cliente.cnpj
      ? `CNPJ ${formatarCnpj(cliente.cnpj)}`
      : ""
    : cliente.cpf
      ? `CPF ${formatarCpf(cliente.cpf)}`
      : "";
  const documento = [documentoPrincipal, ehEmpresa && cliente.inscricaoEstadual ? `IE ${cliente.inscricaoEstadual}` : ""]
    .filter(Boolean)
    .join(" · ");
  const e = cliente.endereco;
  const rua = [e.logradouro, e.numero].filter(Boolean).join(", ");
  const endereco = [
    [rua, e.complemento].filter(Boolean).join(" - "),
    e.bairro,
    [e.cidade, e.uf].filter(Boolean).join("/"),
    e.cep ? `CEP ${formatarCep(e.cep)}` : "",
  ]
    .filter(Boolean)
    .join(" - ");
  return {
    nome,
    documento: documento || undefined,
    contatos: [
      cliente.celular ? formatarTelefone(cliente.celular) : "",
      cliente.telefone ? formatarTelefone(cliente.telefone) : "",
      cliente.email,
    ].filter(Boolean),
    endereco: endereco || undefined,
  };
}

/** Sem permissão para ver clientes (ou sem rede), o documento sai só com o nome que já vem no registro. */
async function buscarCliente(clienteId: string, nomeDoRegistro: string): Promise<DadosCliente> {
  try {
    return dadosDoCliente(await clienteApi.buscarPorId(clienteId));
  } catch {
    return { nome: nomeDoRegistro, contatos: [] };
  }
}

export function montarDocumentoOrcamento(orcamento: OrcamentoResponse, cliente: DadosCliente): DocumentoComercial {
  const codigo = codigoCurto(orcamento.id);
  return {
    tipo: "ORÇAMENTO",
    codigo,
    meta: [
      ["Emitido em", formatarData(dataDoInstante(orcamento.criadoEm))],
      ["Válido até", formatarData(orcamento.prazoValidade)],
      ["Situação", ROTULO_STATUS_ORCAMENTO[orcamento.status]],
    ],
    cliente,
    itens: orcamento.itens.map((i) => ({
      codigo: i.produtoCodigoInterno,
      nome: i.produtoNome,
      quantidade: i.quantidade,
      valorUnitario: i.valorUnitario,
      subtotal: i.subtotal,
    })),
    subtotal: orcamento.subtotal,
    percentualDesconto: orcamento.percentualDesconto,
    valorDesconto: orcamento.valorDesconto,
    valorFrete: orcamento.valorFrete,
    valorTotal: orcamento.valorTotal,
    secoes: [
      ["Condição de pagamento", orcamento.condicaoPagamento],
      ["Observações", orcamento.observacoes],
    ],
    nomeArquivo: `orcamento-${codigo.toLowerCase()}-${paraNomeDeArquivo(cliente.nome)}`,
  };
}

export function montarDocumentoPedido(pedido: PedidoResponse, cliente: DadosCliente): DocumentoComercial {
  const codigo = codigoCurto(pedido.id);
  return {
    tipo: "PEDIDO",
    codigo,
    meta: [
      ["Emitido em", formatarData(dataDoInstante(pedido.criadoEm))],
      ["Situação", ROTULO_STATUS_PEDIDO[pedido.status]],
      ["Origem", pedido.orcamentoOrigemId ? `Orçamento Nº ${codigoCurto(pedido.orcamentoOrigemId)}` : "Pedido direto"],
    ],
    cliente,
    itens: pedido.itens.map((i) => ({
      codigo: i.produtoCodigoInterno,
      nome: i.produtoNome,
      quantidade: i.quantidade,
      valorUnitario: i.valorUnitario,
      subtotal: i.subtotal,
    })),
    subtotal: pedido.subtotal,
    percentualDesconto: pedido.percentualDesconto,
    valorDesconto: pedido.valorDesconto,
    valorFrete: pedido.valorFrete,
    valorTotal: pedido.valorTotal,
    secoes: [
      ["Condição de pagamento", pedido.condicaoPagamento],
      ["Transportadora", pedido.transportadoraNome],
    ],
    nomeArquivo: `pedido-${codigo.toLowerCase()}-${paraNomeDeArquivo(cliente.nome)}`,
  };
}

interface Logo {
  dados: string;
  largura: number;
  altura: number;
}

/** O logo vem do próprio site; se não carregar, o documento sai com o nome "BELLA" em texto. */
async function carregarLogo(): Promise<Logo | null> {
  try {
    const resposta = await fetch("/logo-bella.png");
    if (!resposta.ok) return null;
    const arquivo = await resposta.blob();
    if (!arquivo.type.startsWith("image/")) return null;
    const dados = await new Promise<string>((ok, erro) => {
      const leitor = new FileReader();
      leitor.onload = () => ok(leitor.result as string);
      leitor.onerror = () => erro(leitor.error);
      leitor.readAsDataURL(arquivo);
    });
    const medidas = await new Promise<{ largura: number; altura: number }>((ok, erro) => {
      const imagem = new Image();
      imagem.onload = () => ok({ largura: imagem.naturalWidth, altura: imagem.naturalHeight });
      imagem.onerror = () => erro(new Error("logo ilegível"));
      imagem.src = dados;
    });
    return { dados, ...medidas };
  } catch {
    return null;
  }
}

function formatarQuantidade(valor: number): string {
  return valor.toLocaleString("pt-BR", { maximumFractionDigits: 3 });
}

async function desenhar(documento: DocumentoComercial): Promise<void> {
  const [{ jsPDF, autoTable }, logo] = await Promise.all([carregarBibliotecasPdf(), carregarLogo()]);
  const pdf = new jsPDF({ orientation: "portrait", unit: "pt", format: "a4" });
  const largura = pdf.internal.pageSize.getWidth();
  const altura = pdf.internal.pageSize.getHeight();
  const margem = 40;
  const util = largura - 2 * margem;
  const fimDaTabela = () => (pdf as unknown as { lastAutoTable: { finalY: number } }).lastAutoTable.finalY;

  let y = 0;
  const garantirEspaco = (necessario: number) => {
    if (y + necessario > altura - 50) {
      pdf.addPage();
      y = margem;
    }
  };
  const escrever = (texto: string, opcoes: { negrito?: boolean; tamanho?: number; cor?: Cor } = {}) => {
    const tamanho = opcoes.tamanho ?? 10;
    pdf.setFont("helvetica", opcoes.negrito ? "bold" : "normal");
    pdf.setFontSize(tamanho);
    pdf.setTextColor(...(opcoes.cor ?? TEXTO));
    for (const linha of pdf.splitTextToSize(texto, util) as string[]) {
      garantirEspaco(tamanho + 4);
      pdf.text(linha, margem, y);
      y += tamanho + 4;
    }
  };

  // Cabeçalho: logo à esquerda, tipo e código à direita
  if (logo) {
    const alturaLogo = 36;
    pdf.addImage(logo.dados, "PNG", margem, 28, (alturaLogo * logo.largura) / logo.altura, alturaLogo);
  } else {
    pdf.setFont("helvetica", "bold");
    pdf.setFontSize(18);
    pdf.setTextColor(...VERDE_ESCURO);
    pdf.text("BELLA", margem, 54);
  }
  pdf.setFont("helvetica", "bold");
  pdf.setFontSize(20);
  pdf.setTextColor(...VERDE_ESCURO);
  pdf.text(documento.tipo, largura - margem, 46, { align: "right" });
  pdf.setFont("helvetica", "normal");
  pdf.setFontSize(11);
  pdf.setTextColor(...AUXILIAR);
  pdf.text(`Nº ${documento.codigo}`, largura - margem, 64, { align: "right" });
  pdf.setDrawColor(...VERDE_ESCURO);
  pdf.setLineWidth(1.2);
  pdf.line(margem, 78, largura - margem, 78);
  y = 94;

  // Datas e situação
  autoTable(pdf, {
    startY: y,
    margin: { left: margem, right: margem },
    body: [documento.meta.map(([rotulo, valor]) => `${rotulo}\n${valor}`)],
    theme: "plain",
    styles: { fontSize: 10, textColor: TEXTO, cellPadding: 8, lineColor: [220, 220, 220], lineWidth: 0.5 },
  });
  y = fimDaTabela() + 22;

  // Cliente
  escrever("Cliente", { negrito: true, tamanho: 12 });
  y += 2;
  escrever(documento.cliente.nome, { negrito: true, tamanho: 11 });
  if (documento.cliente.documento) escrever(documento.cliente.documento, { cor: AUXILIAR });
  if (documento.cliente.contatos.length > 0) escrever(documento.cliente.contatos.join("  ·  "), { cor: AUXILIAR });
  if (documento.cliente.endereco) escrever(documento.cliente.endereco, { cor: AUXILIAR });
  y += 14;

  // Itens
  garantirEspaco(80);
  escrever("Itens", { negrito: true, tamanho: 12 });
  autoTable(pdf, {
    startY: y,
    margin: { left: margem, right: margem, bottom: 60 },
    head: [["Código", "Produto", "Qtd", "Valor unit.", "Subtotal"]],
    body: documento.itens.map((i) => [
      i.codigo,
      i.nome,
      formatarQuantidade(i.quantidade),
      formatarMoeda(i.valorUnitario),
      formatarMoeda(i.subtotal),
    ]),
    headStyles: { fillColor: VERDE_ESCURO, textColor: 255, fontSize: 9 },
    styles: { fontSize: 9, textColor: TEXTO, cellPadding: 5 },
    alternateRowStyles: { fillColor: [247, 247, 244] },
    columnStyles: {
      0: { cellWidth: 70 },
      2: { halign: "right", cellWidth: 50 },
      3: { halign: "right", cellWidth: 80 },
      4: { halign: "right", cellWidth: 80 },
    },
    didParseCell: (dados) => {
      if (dados.section === "head" && dados.column.index >= 2) dados.cell.styles.halign = "right";
    },
  });
  y = fimDaTabela() + 14;

  // Totais, no canto direito
  garantirEspaco(110);
  const percentual = documento.percentualDesconto.toLocaleString("pt-BR", { maximumFractionDigits: 2 });
  const linhasTotais: [string, string][] = [["Subtotal", formatarMoeda(documento.subtotal)]];
  if (documento.valorDesconto > 0) {
    linhasTotais.push([`Desconto (${percentual}%)`, `- ${formatarMoeda(documento.valorDesconto)}`]);
  }
  if (documento.valorFrete > 0) linhasTotais.push(["Frete", formatarMoeda(documento.valorFrete)]);
  linhasTotais.push(["Total", formatarMoeda(documento.valorTotal)]);
  const indiceTotal = linhasTotais.length - 1;
  autoTable(pdf, {
    startY: y,
    margin: { left: largura - margem - 240, right: margem },
    body: linhasTotais,
    theme: "plain",
    styles: { fontSize: 10, textColor: TEXTO, cellPadding: { top: 4, bottom: 4, left: 6, right: 6 } },
    columnStyles: { 1: { halign: "right" } },
    didParseCell: (dados) => {
      if (dados.section === "body" && dados.row.index === indiceTotal) {
        dados.cell.styles.fontStyle = "bold";
        dados.cell.styles.fontSize = 13;
        dados.cell.styles.textColor = VERDE_ESCURO;
      }
    },
    didDrawCell: (dados) => {
      if (dados.section === "body" && dados.row.index === indiceTotal) {
        pdf.setDrawColor(...VERDE_ESCURO);
        pdf.setLineWidth(0.8);
        pdf.line(dados.cell.x, dados.cell.y, dados.cell.x + dados.cell.width, dados.cell.y);
      }
    },
  });
  y = fimDaTabela() + 22;

  // Condições e observações
  for (const [titulo, texto] of documento.secoes) {
    if (!texto || !texto.trim()) continue;
    garantirEspaco(50);
    escrever(titulo, { negrito: true, tamanho: 11 });
    y += 1;
    escrever(texto.trim());
    y += 12;
  }

  const total = pdf.getNumberOfPages();
  const geradoEm = new Date().toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });
  for (let pagina = 1; pagina <= total; pagina++) {
    pdf.setPage(pagina);
    pdf.setFont("helvetica", "normal");
    pdf.setFontSize(8);
    pdf.setTextColor(...AUXILIAR);
    pdf.text(`Documento gerado pelo sistema Bella em ${geradoEm}`, margem, altura - 20);
    pdf.text(`Página ${pagina} de ${total}`, largura - margem, altura - 20, { align: "right" });
  }

  pdf.save(`${documento.nomeArquivo}.pdf`);
}

export async function baixarPdfOrcamento(orcamento: OrcamentoResponse): Promise<void> {
  const cliente = await buscarCliente(orcamento.clienteId, orcamento.clienteNome);
  await desenhar(montarDocumentoOrcamento(orcamento, cliente));
}

export async function baixarPdfPedido(pedido: PedidoResponse): Promise<void> {
  const cliente = await buscarCliente(pedido.clienteId, pedido.clienteNome);
  await desenhar(montarDocumentoPedido(pedido, cliente));
}

/** Roda a geração e avisa se falhar, para o botão não ficar mudo. */
export async function tentarBaixarPdf(gerar: () => Promise<void>): Promise<void> {
  try {
    await gerar();
  } catch {
    toast.error("Não foi possível gerar o PDF. Tente de novo.");
  }
}
