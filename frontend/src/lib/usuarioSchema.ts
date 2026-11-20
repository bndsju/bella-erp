import { z } from "zod";

const PERFIS = ["ADMINISTRADOR", "GERENTE", "FINANCEIRO", "VENDEDOR"] as const;

export const TAMANHO_MINIMO_SENHA = 8;

const senhaNova = z
  .string()
  .min(TAMANHO_MINIMO_SENHA, `A senha deve ter pelo menos ${TAMANHO_MINIMO_SENHA} caracteres`)
  .max(72, "A senha deve ter no máximo 72 caracteres");

export const usuarioCadastroSchema = z.object({
  nome: z.string().trim().min(1, "Nome é obrigatório").max(100, "Nome muito longo"),
  login: z
    .string()
    .trim()
    .regex(/^[A-Za-z0-9._-]{3,50}$/, "Use de 3 a 50 caracteres: letras, números, ponto, hífen ou sublinhado"),
  senha: senhaNova,
  perfil: z.enum(PERFIS, { message: "Perfil é obrigatório" }),
});

export const usuarioEdicaoSchema = z.object({
  nome: z.string().trim().min(1, "Nome é obrigatório").max(100, "Nome muito longo"),
  perfil: z.enum(PERFIS, { message: "Perfil é obrigatório" }),
});

export const novaSenhaSchema = z.object({ novaSenha: senhaNova });

export const alterarMinhaSenhaSchema = z
  .object({
    senhaAtual: z.string().min(1, "Informe a senha atual"),
    novaSenha: senhaNova,
    confirmacao: z.string().min(1, "Repita a nova senha"),
  })
  .refine((v) => v.novaSenha === v.confirmacao, { path: ["confirmacao"], message: "As senhas não são iguais" });

export type UsuarioCadastroValues = z.infer<typeof usuarioCadastroSchema>;
export type UsuarioEdicaoValues = z.infer<typeof usuarioEdicaoSchema>;
export type NovaSenhaValues = z.infer<typeof novaSenhaSchema>;
export type AlterarMinhaSenhaValues = z.infer<typeof alterarMinhaSenhaSchema>;
