// Cabeçalhos de segurança do frontend. Aqui servem ao `vite preview` (para testar o build como vai para o ar);
// em produção os mesmos valores ficam na configuração do servidor web (Caddy). Mantenha os dois iguais.
export const csp = [
  "default-src 'self'",
  "script-src 'self'",
  // estilos inline: o React e os componentes de interface aplicam `style` direto nos elementos
  "style-src 'self' 'unsafe-inline'",
  // data: e blob: para o logo e as imagens geradas ao montar o PDF
  "img-src 'self' data: blob:",
  "font-src 'self'",
  // a única chamada externa é a consulta de CEP
  "connect-src 'self' https://viacep.com.br",
  "object-src 'none'",
  "base-uri 'self'",
  "form-action 'self'",
  "frame-ancestors 'none'",
].join("; ");

export const cabecalhosSeguranca = {
  "Content-Security-Policy": csp,
  "Referrer-Policy": "strict-origin-when-cross-origin",
  "Permissions-Policy": "camera=(), microphone=(), geolocation=(), payment=(), usb=()",
  "X-Content-Type-Options": "nosniff",
  "X-Frame-Options": "DENY",
};
