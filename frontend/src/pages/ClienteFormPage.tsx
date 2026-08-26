import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { toast } from "sonner";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { ClienteForm } from "@/components/clientes/ClienteForm";
import { clienteApi } from "@/lib/clienteApi";
import { ApiError } from "@/lib/api";
import type { ClienteResponse, ClienteRequest, TipoPessoa } from "@/types/cliente";
import type { ClienteFormValoresIniciais } from "@/lib/clienteSchema";

function paraValoresIniciais(cliente: ClienteResponse): ClienteFormValoresIniciais {
  return {
    nomeCompleto: cliente.nomeCompleto ?? "",
    dataNascimento: cliente.dataNascimento ?? "",
    cpf: cliente.cpf ?? "",
    razaoSocial: cliente.razaoSocial ?? "",
    nomeFantasia: cliente.nomeFantasia ?? "",
    cnpj: cliente.cnpj ?? "",
    inscricaoEstadual: cliente.inscricaoEstadual ?? "",
    inscricaoMunicipal: cliente.inscricaoMunicipal ?? "",
    celular: cliente.celular,
    email: cliente.email,
    telefone: cliente.telefone ?? "",
    endereco: {
      cep: cliente.endereco.cep ?? "",
      logradouro: cliente.endereco.logradouro ?? "",
      numero: cliente.endereco.numero ?? "",
      complemento: cliente.endereco.complemento ?? "",
      bairro: cliente.endereco.bairro ?? "",
      cidade: cliente.endereco.cidade ?? "",
      uf: cliente.endereco.uf ?? "",
    },
    observacoes: cliente.observacoes ?? "",
  };
}

export function ClienteFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const modo = id ? "editar" : "novo";

  const [carregando, setCarregando] = useState(modo === "editar");
  const [enviando, setEnviando] = useState(false);
  const [tipoPessoa, setTipoPessoa] = useState<TipoPessoa>();
  const [valoresIniciais, setValoresIniciais] = useState<ClienteFormValoresIniciais>();

  useEffect(() => {
    if (!id) return;

    clienteApi
      .buscarPorId(id)
      .then((cliente) => {
        setTipoPessoa(cliente.tipoPessoa);
        setValoresIniciais(paraValoresIniciais(cliente));
      })
      .catch(() => {
        toast.error("Não foi possível carregar o cliente");
        navigate("/clientes");
      })
      .finally(() => setCarregando(false));
  }, [id, navigate]);

  async function handleSubmit(dados: ClienteRequest) {
    setEnviando(true);
    try {
      if (id) {
        await clienteApi.editar(id, dados);
        toast.success("Cliente atualizado");
      } else {
        await clienteApi.cadastrar(dados);
        toast.success("Cliente cadastrado");
      }
      navigate("/clientes");
    } catch (erro) {
      if (erro instanceof ApiError) {
        toast.error(erro.message, {
          description: erro.erros?.join(", "),
        });
      } else {
        toast.error("Erro inesperado ao salvar o cliente");
      }
    } finally {
      setEnviando(false);
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>{modo === "editar" ? "Editar cliente" : "Novo cliente"}</CardTitle>
      </CardHeader>
      <CardContent>
        {carregando ? (
          <div className="space-y-4">
            <Skeleton className="h-9 w-full" />
            <Skeleton className="h-9 w-full" />
            <Skeleton className="h-9 w-full" />
          </div>
        ) : (
          <ClienteForm
            modo={modo}
            tipoPessoaInicial={tipoPessoa}
            valoresIniciais={valoresIniciais}
            enviando={enviando}
            onSubmit={handleSubmit}
          />
        )}
      </CardContent>
    </Card>
  );
}
