import type { Pagina } from "@/types/relatorio";

export interface TabelaPdf {
  titulo?: string;
  colunas: string[];
  linhas: string[][];
  /** Índices das colunas alinhadas à direita (valores e quantidades). */
  direita?: number[];
}

export interface DocumentoPdf {
  titulo: string;
  /** Texto do período (ex.: "01/11/2026 a 30/11/2026"). */
  periodo?: string;
  /** Filtros aplicados, um por item (ex.: "Cliente: Maria Silva"). */
  filtros?: string[];
  /** Totais em destaque, como pares rótulo/valor. */
  resumo?: [string, string][];
  tabelas: TabelaPdf[];
  nomeArquivo: string;
}

const VERDE_ESCURO: [number, number, number] = [61, 89, 25];
const TEXTO: [number, number, number] = [35, 31, 32];
const AUXILIAR: [number, number, number] = [110, 110, 110];

/** Junta todas as páginas de um relatório paginado (200 por vez, o máximo da API). */
export async function todasAsPaginas<R, T>(
  buscar: (pagina: number, tamanho: number) => Promise<R>,
  pegar: (resposta: R) => Pagina<T>,
): Promise<{ primeira: R; itens: T[] }> {
  const TAMANHO = 200;
  const primeira = await buscar(0, TAMANHO);
  const itens = [...pegar(primeira).itens];
  const total = pegar(primeira).totalPaginas;
  for (let pagina = 1; pagina < total; pagina++) {
    itens.push(...pegar(await buscar(pagina, TAMANHO)).itens);
  }
  return { primeira, itens };
}

export function textoDoPeriodo(inicio: string, fim: string): string {
  const formatar = (data: string) => data.split("-").reverse().join("/");
  return `${formatar(inicio)} a ${formatar(fim)}`;
}

/** Gera o PDF no navegador e baixa o arquivo. A biblioteca só é carregada quando alguém exporta. */
export async function gerarPdf(documento: DocumentoPdf): Promise<void> {
  const [{ jsPDF }, modulo] = await Promise.all([import("jspdf"), import("jspdf-autotable")]);
  // O pacote é CommonJS: conforme o empacotador, a função vem em `default`, em `default.default` ou em `autoTable`.
  const candidato = modulo as unknown as { default?: unknown; autoTable?: unknown };
  const autoTable = (
    typeof candidato.autoTable === "function"
      ? candidato.autoTable
      : typeof candidato.default === "function"
        ? candidato.default
        : (candidato.default as { default: unknown }).default
  ) as typeof import("jspdf-autotable").default;
  const pdf = new jsPDF({ orientation: "landscape", unit: "pt", format: "a4" });
  const larguraPagina = pdf.internal.pageSize.getWidth();
  const alturaPagina = pdf.internal.pageSize.getHeight();
  const margem = 40;

  pdf.setFont("helvetica", "bold");
  pdf.setFontSize(10);
  pdf.setTextColor(...VERDE_ESCURO);
  pdf.text("BELLA", margem, 36);

  pdf.setFontSize(18);
  pdf.setTextColor(...TEXTO);
  pdf.text(documento.titulo, margem, 60);

  let y = 76;
  pdf.setFont("helvetica", "normal");
  pdf.setFontSize(10);
  pdf.setTextColor(...AUXILIAR);
  if (documento.periodo) {
    pdf.text(`Período: ${documento.periodo}`, margem, y);
    y += 14;
  }
  if (documento.filtros && documento.filtros.length > 0) {
    pdf.text(`Filtros: ${documento.filtros.join(" · ")}`, margem, y);
    y += 14;
  }
  pdf.text(`Gerado em ${new Date().toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" })}`, margem, y);
  y += 18;

  if (documento.resumo && documento.resumo.length > 0) {
    autoTable(pdf, {
      startY: y,
      margin: { left: margem, right: margem },
      body: [documento.resumo.map(([rotulo, valor]) => `${rotulo}\n${valor}`)],
      theme: "plain",
      styles: { fontSize: 10, textColor: TEXTO, cellPadding: 8, lineColor: [220, 220, 220], lineWidth: 0.5 },
    });
    y = (pdf as unknown as { lastAutoTable: { finalY: number } }).lastAutoTable.finalY + 16;
  }

  for (const tabela of documento.tabelas) {
    if (tabela.titulo) {
      if (y > alturaPagina - 90) {
        pdf.addPage();
        y = margem;
      }
      pdf.setFont("helvetica", "bold");
      pdf.setFontSize(12);
      pdf.setTextColor(...TEXTO);
      pdf.text(tabela.titulo, margem, y + 4);
      y += 12;
    }
    const direita = new Set(tabela.direita ?? []);
    autoTable(pdf, {
      startY: y,
      margin: { left: margem, right: margem, bottom: 40 },
      head: [tabela.colunas],
      body: tabela.linhas.length > 0 ? tabela.linhas : [[{ content: "Sem registros", colSpan: tabela.colunas.length }]],
      headStyles: { fillColor: VERDE_ESCURO, textColor: 255, fontSize: 9 },
      styles: { fontSize: 9, textColor: TEXTO, cellPadding: 5 },
      alternateRowStyles: { fillColor: [247, 247, 244] },
      columnStyles: Object.fromEntries([...direita].map((indice) => [indice, { halign: "right" as const }])),
      didParseCell: (dados) => {
        if (dados.section === "head" && direita.has(dados.column.index)) dados.cell.styles.halign = "right";
      },
    });
    y = (pdf as unknown as { lastAutoTable: { finalY: number } }).lastAutoTable.finalY + 20;
  }

  const total = pdf.getNumberOfPages();
  for (let pagina = 1; pagina <= total; pagina++) {
    pdf.setPage(pagina);
    pdf.setFont("helvetica", "normal");
    pdf.setFontSize(8);
    pdf.setTextColor(...AUXILIAR);
    pdf.text(`Página ${pagina} de ${total}`, larguraPagina - margem, alturaPagina - 20, { align: "right" });
  }

  pdf.save(`${documento.nomeArquivo}.pdf`);
}
