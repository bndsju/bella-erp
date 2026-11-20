/**
 * Token de acesso guardado neste navegador. Fica em localStorage para a sessão sobreviver a recarregar a página;
 * se o navegador bloquear o armazenamento, o login vale só enquanto a aba estiver aberta.
 */
const CHAVE = "bella.token";

let emMemoria: string | null = null;

export function lerToken(): string | null {
  if (emMemoria) return emMemoria;
  try {
    return localStorage.getItem(CHAVE);
  } catch {
    return null;
  }
}

export function guardarToken(token: string): void {
  emMemoria = token;
  try {
    localStorage.setItem(CHAVE, token);
  } catch {
    // sem armazenamento: segue só em memória
  }
}

export function limparToken(): void {
  emMemoria = null;
  try {
    localStorage.removeItem(CHAVE);
  } catch {
    // nada a limpar
  }
}

/** Disparado quando o servidor recusa o token (sessão vencida, usuário inativado, senha trocada em outro lugar). */
export const EVENTO_SESSAO_EXPIRADA = "bella:sessao-expirada";
