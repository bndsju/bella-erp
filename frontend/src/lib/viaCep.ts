import { apenasDigitos } from "./documento";
import type { Endereco } from "@/types/cliente";

interface ViaCepResposta {
  cep: string;
  logradouro: string;
  complemento: string;
  bairro: string;
  localidade: string;
  uf: string;
  erro?: boolean;
}

export class ViaCepError extends Error {}

/**
 * Busca o endereço de um CEP na API pública do ViaCEP.
 * Retorna null quando o CEP tem formato válido mas não foi encontrado.
 * Lança ViaCepError em caso de CEP mal formatado ou falha de rede.
 */
export async function buscarEnderecoPorCep(
  cep: string,
): Promise<Pick<Endereco, "logradouro" | "complemento" | "bairro" | "cidade" | "uf"> | null> {
  const digitos = apenasDigitos(cep);

  if (digitos.length !== 8) {
    throw new ViaCepError("CEP deve ter 8 dígitos");
  }

  let resposta: Response;
  try {
    resposta = await fetch(`https://viacep.com.br/ws/${digitos}/json/`);
  } catch {
    throw new ViaCepError("Não foi possível consultar o CEP agora");
  }

  if (!resposta.ok) {
    throw new ViaCepError("Não foi possível consultar o CEP agora");
  }

  const dados = (await resposta.json()) as ViaCepResposta;

  if (dados.erro) {
    return null;
  }

  return {
    logradouro: dados.logradouro,
    complemento: dados.complemento,
    bairro: dados.bairro,
    cidade: dados.localidade,
    uf: dados.uf,
  };
}
