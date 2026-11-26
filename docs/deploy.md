# Colocando o Bella no ar (VPS com Docker + Caddy)

O site e a API ficam no mesmo endereço. O Caddy cuida do HTTPS (certificado automático), serve o frontend e repassa
`/api` para o backend. Só as portas 80 e 443 ficam abertas; banco e API ficam dentro da rede do Docker.

> **Use só dados fictícios** na demonstração. Nada de clientes reais enquanto os itens da Fase 4 (LGPD, backup testado)
> não estiverem prontos.

## 1. Servidor

- Ubuntu 24.04, com IPv4 próprio. Mínimo: 1 vCPU, 2 GB de RAM (o plano de 4 GB sobra).
- No painel do provedor, **desligue a renovação automática** se for um plano de curto prazo.
- Se o provedor tiver firewall no painel, libere as portas **22, 80, 443 (TCP) e 443 (UDP)**.

## 2. Endereço do site

- **Com domínio:** aponte um registro `A` (por exemplo `bella.seudominio.com.br`) para o IP do servidor e use esse
  nome em `SITE_ADDRESS`.
- **Sem domínio:** use o IP com hífens no lugar dos pontos, mais `.sslip.io`. Para o IP `203.0.113.10`, o endereço
  é `203-0-113-10.sslip.io` (funciona sem cadastro nenhum). Se o certificado falhar por limite de emissões do
  serviço, o Caddy tenta outra autoridade sozinho; em último caso, crie um subdomínio grátis no DuckDNS e use-o.

## 3. Preparar o servidor

Entre por SSH (`ssh root@IP-DO-SERVIDOR`) e rode:

```bash
apt update && apt -y upgrade
curl -fsSL https://get.docker.com | sh
ufw allow OpenSSH && ufw allow 80/tcp && ufw allow 443/tcp && ufw allow 443/udp && ufw --force enable
```

Recomendado: entrar só por chave SSH. Depois de confirmar que a chave funciona, desligue a senha em
`/etc/ssh/sshd_config` (`PasswordAuthentication no`) e rode `systemctl restart ssh`.

## 4. Baixar o projeto e configurar

```bash
git clone https://github.com/bndsju/bella-erp.git
cd bella-erp
cp .env.exemplo .env
nano .env
```

Preencha o `.env` (o arquivo explica cada valor):

| Variável | O que colocar |
| --- | --- |
| `SITE_ADDRESS` | endereço do passo 2, sem `https://` |
| `DB_PASSWORD` | saída de `openssl rand -hex 24` |
| `JWT_SEGREDO` | saída de `openssl rand -base64 48 \| tr -d '/+=\n'` |
| `ADMIN_SENHA` | senha que você escolher para o login `admin` (10+ caracteres, letra e número, sem a palavra "admin") |

Se o repositório for privado, o `git clone` pede autenticação: use o `scp` para enviar a pasta, ou crie uma chave de
deploy somente leitura no GitHub. Nunca cole senha ou token em conversa, issue ou commit.

## 5. Subir

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

A primeira vez demora alguns minutos (compila o backend e o frontend). Acompanhe com:

```bash
docker compose -f docker-compose.prod.yml logs -f --tail=50
```

Enquanto o backend não termina de subir (pode levar alguns minutos numa máquina de 1 vCPU), o site responde
`502 Bad Gateway`: é só esperar.

Quando o backend mostrar `Started BellaBackendApplication` e o Caddy mostrar `certificate obtained successfully`,
abra `https://SEU-ENDERECO` e entre com `admin` e a `ADMIN_SENHA`.

## 6. Conferir

- O cadeado aparece e `http://SEU-ENDERECO` redireciona para `https://`.
- Do seu computador, a porta do banco não responde: `Test-NetConnection IP-DO-SERVIDOR -Port 5432` deve falhar.
- Cabeçalhos: `curl -sI https://SEU-ENDERECO` deve mostrar `Strict-Transport-Security` e `Content-Security-Policy`.
- Faça login, crie um cliente e exporte um PDF.
- Em **Auditoria**, módulo "Segurança", o evento de login mostra o **seu IP real** (não um `172.x`). Se aparecer `172.x`,
  o limite por IP está tratando todo mundo como uma pessoa só: avise antes de divulgar o endereço.
- Em **Auditoria** com o módulo "Segurança" aparecem o seu login e as tentativas erradas.
- Troque a senha do admin em "Minha conta" e crie os usuários de teste do professor (cada perfil).

## 7. Atualizar, parar e tirar do ar

```bash
cd bella-erp
git pull
docker compose -f docker-compose.prod.yml --env-file .env up -d --build   # atualizar
docker compose -f docker-compose.prod.yml logs --tail=100 backend         # ver erros
docker compose -f docker-compose.prod.yml down                            # parar (os dados ficam)
docker compose -f docker-compose.prod.yml down -v                         # parar e APAGAR banco e certificados
```

## Backup rápido do banco (até a Fase 4 automatizar)

```bash
docker compose -f docker-compose.prod.yml exec -T postgres pg_dump -U bella bella | gzip > backup-$(date +%F).sql.gz
```

Copie o arquivo para fora do servidor (`scp`), senão ele se perde junto com a máquina.
