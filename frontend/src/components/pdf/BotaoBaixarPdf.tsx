import { useState } from "react";
import { FileDown } from "lucide-react";
import { Button } from "@/components/ui/button";
import { tentarBaixarPdf } from "@/lib/pdfComercial";

/** Botão "Baixar PDF" do documento aberto na tela. Fica desabilitado enquanto o arquivo é montado. */
export function BotaoBaixarPdf({ gerar }: { gerar: () => Promise<void> }) {
  const [gerando, setGerando] = useState(false);

  async function aoClicar() {
    setGerando(true);
    await tentarBaixarPdf(gerar);
    setGerando(false);
  }

  return (
    <Button type="button" variant="outline" disabled={gerando} onClick={aoClicar}>
      <FileDown aria-hidden="true" />
      {gerando ? "Gerando PDF..." : "Baixar PDF"}
    </Button>
  );
}
