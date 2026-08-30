import { z } from "zod";
import { validarCnpj, validarCpf } from "./documento";

const enderecoSchema = z.object({
  cep: z.string().min(1, "CEP é obrigatório"),
  logradouro: z.string().optional(),
  numero: z.string().optional(),
  complemento: z.string().optional(),
  bairro: z.string().optional(),
  cidade: z.string().optional(),
  uf: z.string().optional(),
});

const camposComunsSchema = {
  celular: z.string().min(1, "Celular é obrigatório"),
  email: z.string().min(1, "Email é obrigatório").email("Email inválido"),
  telefone: z.string().optional(),
  endereco: enderecoSchema,
  observacoes: z.string().optional(),
};

export const clientePessoaFisicaSchema = z.object({
  tipoPessoa: z.literal("PF"),
  nomeCompleto: z.string().min(1, "Nome completo é obrigatório"),
  dataNascimento: z
    .string()
    .min(1, "Data de nascimento é obrigatória")
    .refine((valor) => new Date(valor) <= new Date(), {
      message: "Data de nascimento não pode ser uma data futura",
    }),
  cpf: z.string().refine(validarCpf, "CPF inválido"),
  ...camposComunsSchema,
});

export const clientePessoaJuridicaSchema = z.object({
  tipoPessoa: z.literal("PJ"),
  razaoSocial: z.string().min(1, "Razão social é obrigatória"),
  nomeFantasia: z.string().optional(),
  cnpj: z.string().refine(validarCnpj, "CNPJ inválido"),
  inscricaoEstadual: z.string().optional(),
  inscricaoMunicipal: z.string().optional(),
  ...camposComunsSchema,
});

export const clienteSchema = z.discriminatedUnion("tipoPessoa", [
  clientePessoaFisicaSchema,
  clientePessoaJuridicaSchema,
]);

export type ClientePessoaFisicaFormValues = z.infer<typeof clientePessoaFisicaSchema>;
export type ClientePessoaJuridicaFormValues = z.infer<typeof clientePessoaJuridicaSchema>;
export type ClienteFormValues = z.infer<typeof clienteSchema>;

/**
 * Valores iniciais compartilhados entre os dois formulários (PF e PJ).
 * Omitimos "tipoPessoa" da interseção porque "PF" & "PJ" colapsaria o tipo para never.
 */
export type ClienteFormValoresIniciais = Partial<
  Omit<ClientePessoaFisicaFormValues, "tipoPessoa"> & Omit<ClientePessoaJuridicaFormValues, "tipoPessoa">
>;
