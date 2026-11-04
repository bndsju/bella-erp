import { Card } from "@/components/ui/card";

export function Indicador({ rotulo, valor, nota }: { rotulo: string; valor: string; nota?: string }) {
  return (
    <Card className="indicador">
      <div className="indicador__rotulo">{rotulo}</div>
      <div className="indicador__valor">{valor}</div>
      {nota && <div className="indicador__nota">{nota}</div>}
    </Card>
  );
}

/** Quando a lista volta vazia: mesma mensagem do restante do sistema. */
export function SemRegistros({ texto = "Nada encontrado neste período." }: { texto?: string }) {
  return (
    <div className="vazio">
      <h3>Sem registros</h3>
      <p>{texto}</p>
    </div>
  );
}
