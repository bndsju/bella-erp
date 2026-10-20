const formatoMoeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

export function formatarMoeda(valor: number): string {
  return formatoMoeda.format(valor);
}

export function formatarData(data: string | null | undefined): string {
  return data ? new Date(`${data}T00:00:00`).toLocaleDateString("pt-BR") : "—";
}

export function dataDeHoje(): string {
  const agora = new Date();
  const mes = String(agora.getMonth() + 1).padStart(2, "0");
  const dia = String(agora.getDate()).padStart(2, "0");
  return `${agora.getFullYear()}-${mes}-${dia}`;
}

/** Soma `meses` a uma data ISO (yyyy-mm-dd), mantendo o dia quando possível (31/01 + 1 mês = 28 ou 29/02). */
export function adicionarMeses(data: string, meses: number): string {
  const [ano, mes, dia] = data.split("-").map(Number);
  const alvo = new Date(ano, mes - 1 + meses, 1);
  const ultimoDia = new Date(alvo.getFullYear(), alvo.getMonth() + 1, 0).getDate();
  alvo.setDate(Math.min(dia, ultimoDia));
  const m = String(alvo.getMonth() + 1).padStart(2, "0");
  const d = String(alvo.getDate()).padStart(2, "0");
  return `${alvo.getFullYear()}-${m}-${d}`;
}

/** Divide um valor em n parcelas em centavos; a última absorve o resto da divisão. */
export function dividirEmParcelas(valorTotal: number, quantidade: number): number[] {
  const totalCentavos = Math.round(valorTotal * 100);
  const base = Math.floor(totalCentavos / quantidade);
  const parcelas = Array.from({ length: quantidade }, () => base);
  parcelas[quantidade - 1] += totalCentavos - base * quantidade;
  return parcelas.map((centavos) => centavos / 100);
}
