import { useState } from "react";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ClienteFormFields } from "./ClienteFormFields";
import type { ClienteFormValoresIniciais } from "@/lib/clienteSchema";
import type { ClienteRequest, TipoPessoa } from "@/types/cliente";

interface ClienteFormProps {
  modo: "novo" | "editar";
  tipoPessoaInicial?: TipoPessoa;
  valoresIniciais?: ClienteFormValoresIniciais;
  enviando: boolean;
  onSubmit: (dados: ClienteRequest) => void;
}

export function ClienteForm({
  modo,
  tipoPessoaInicial,
  valoresIniciais,
  enviando,
  onSubmit,
}: ClienteFormProps) {
  const [tipoPessoa, setTipoPessoa] = useState<TipoPessoa>(tipoPessoaInicial ?? "PF");
  const bloquearTipo = modo === "editar";

  return (
    <div className="space-y-6">
      <Tabs
        value={tipoPessoa}
        onValueChange={(valor) => setTipoPessoa(valor as TipoPessoa)}
      >
        <TabsList>
          <TabsTrigger value="PF" disabled={bloquearTipo}>
            Pessoa física
          </TabsTrigger>
          <TabsTrigger value="PJ" disabled={bloquearTipo}>
            Pessoa jurídica
          </TabsTrigger>
        </TabsList>
      </Tabs>

      <ClienteFormFields
        key={tipoPessoa}
        tipoPessoa={tipoPessoa}
        valoresIniciais={valoresIniciais}
        documentoBloqueado={modo === "editar"}
        enviando={enviando}
        onSubmit={onSubmit}
      />
    </div>
  );
}
