export function paraIso(data: Date): string {
  const mes = String(data.getMonth() + 1).padStart(2, "0");
  const dia = String(data.getDate()).padStart(2, "0");
  return `${data.getFullYear()}-${mes}-${dia}`;
}

export interface PeriodoPredefinido {
  rotulo: string;
  intervalo: () => [string, string];
}

export const PERIODOS: PeriodoPredefinido[] = [
  {
    rotulo: "Este mês",
    intervalo: () => {
      const h = new Date();
      return [paraIso(new Date(h.getFullYear(), h.getMonth(), 1)), paraIso(new Date(h.getFullYear(), h.getMonth() + 1, 0))];
    },
  },
  {
    rotulo: "Mês passado",
    intervalo: () => {
      const h = new Date();
      return [paraIso(new Date(h.getFullYear(), h.getMonth() - 1, 1)), paraIso(new Date(h.getFullYear(), h.getMonth(), 0))];
    },
  },
  {
    rotulo: "Últimos 30 dias",
    intervalo: () => {
      const h = new Date();
      return [paraIso(new Date(h.getFullYear(), h.getMonth(), h.getDate() - 29)), paraIso(h)];
    },
  },
  {
    rotulo: "Este ano",
    intervalo: () => {
      const ano = new Date().getFullYear();
      return [`${ano}-01-01`, `${ano}-12-31`];
    },
  },
];
