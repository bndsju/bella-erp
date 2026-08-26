export type TipoPessoa = "PF" | "PJ";

export type StatusCliente = "ATIVO" | "INATIVO";

export interface Endereco {
  cep: string;
  logradouro?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  uf?: string;
}

export interface ClienteResponse {
  id: string;
  tipoPessoa: TipoPessoa;
  status: StatusCliente;

  nomeCompleto: string | null;
  dataNascimento: string | null;
  cpf: string | null;

  razaoSocial: string | null;
  nomeFantasia: string | null;
  cnpj: string | null;
  inscricaoEstadual: string | null;
  inscricaoMunicipal: string | null;

  telefone: string | null;
  celular: string;
  email: string;
  endereco: Endereco;
  observacoes: string | null;
  criadoEm: string;
  atualizadoEm: string;
}

export interface ClientePessoaFisicaRequest {
  tipoPessoa: "PF";
  nomeCompleto: string;
  dataNascimento: string;
  cpf: string;
  celular: string;
  email: string;
  telefone?: string;
  endereco: Endereco;
  observacoes?: string;
}

export interface ClientePessoaJuridicaRequest {
  tipoPessoa: "PJ";
  razaoSocial: string;
  nomeFantasia?: string;
  cnpj: string;
  inscricaoEstadual?: string;
  inscricaoMunicipal?: string;
  celular: string;
  email: string;
  telefone?: string;
  endereco: Endereco;
  observacoes?: string;
}

export type ClienteRequest = ClientePessoaFisicaRequest | ClientePessoaJuridicaRequest;

export interface ListarClientesParams {
  busca?: string;
  status?: StatusCliente;
}
