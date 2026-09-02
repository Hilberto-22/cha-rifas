# Chá Rifa do José Lucca

Aplicação em repositório único com Angular, Spring Boot e PostgreSQL. O participante escolhe números, cria uma reserva temporária e continua o atendimento pelo WhatsApp. O PostgreSQL garante que um mesmo número não seja reservado por duas pessoas.

## Estrutura

```text
backend/       API Spring Boot 4 / Java 21 / JPA / Flyway
frontend/      Angular 21 servido por Nginx
deploy/        Exemplo de proxy reverso para a VPS
compose.yaml   Aplicação completa com PostgreSQL
```

O sistema é um monólito de implantação com três contêineres: frontend, backend e banco. Apenas o frontend publica uma porta no host. O backend e o PostgreSQL ficam isolados na rede interna do Docker.

## Executar com Docker

1. Copie o arquivo de ambiente:

```bash
cp .env.example .env
```

2. Edite `.env`, principalmente a senha do PostgreSQL e o WhatsApp. O telefone deve conter somente país, DDD e número, por exemplo `5596999999999`.

3. Construa e inicie:

```bash
docker compose up -d --build
```

4. Acesse `http://localhost:8082`. Verifique a API com:

```bash
curl http://localhost:8082/api/v1/raffles/active
```

As migrations do Flyway criam as tabelas e cadastram uma rifa inicial com números de 1 a 100. Os dados ficam no volume `postgres_data` e permanecem depois que os contêineres forem reiniciados.

## Desenvolvimento sem Docker

### Windows

Com Java 21, Maven, Node e PostgreSQL instalados, copie e configure o ambiente:

```powershell
Copy-Item .env.example .env
notepad .env
```

Prepare o banco uma única vez:

```powershell
.\scripts\setup-database.ps1
```

Abra dois terminais PowerShell. No primeiro:

```powershell
.\scripts\run-backend.ps1
```

No segundo:

```powershell
.\scripts\run-frontend.ps1
```

Acesse `http://localhost:4200`. O Angular encaminha automaticamente as chamadas `/api` para o Spring Boot em `http://localhost:8081`.

Se a política do PowerShell impedir a execução dos scripts, libere apenas o processo atual:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
```

### Linux ou execução manual

Inicie um PostgreSQL e exporte `DB_URL`, `DB_USER`, `DB_PASSWORD` e `WHATSAPP_NUMBER`.

Backend:

```bash
cd backend
mvn spring-boot:run
```

Frontend, em outro terminal:

```bash
cd frontend
npm install
npm start
```

O frontend de desenvolvimento abre em `http://localhost:4200` e encaminha `/api` para `http://localhost:8081`.

## Concorrência

`RaffleService.reserve` executa toda a reserva dentro de uma transação. O repositório carrega os números solicitados com bloqueio pessimista (`SELECT ... FOR UPDATE`) em ordem numérica. Enquanto a primeira transação está aberta, outra requisição que contenha o mesmo número aguarda. Depois do commit, a segunda requisição encontra o número reservado e recebe HTTP `409 Conflict`.

A restrição única `(raffle_id, number)` no PostgreSQL oferece uma garantia adicional de integridade. Reservas vencem após 15 minutos; números vencidos são considerados disponíveis pela leitura, liberados ao serem novamente reservados e também limpos a cada minuto pelo backend.

## Publicar na VPS Hostinger

Pré-requisitos: Docker Engine, plugin Docker Compose e Nginx já instalados.

```bash
cd ~
git clone URL_DO_REPOSITORIO workspace-rifa
cd workspace-rifa
cp .env.example .env
nano .env
docker compose up -d --build
docker compose ps
```

Antes de subir, confirme se a porta escolhida está livre. O exemplo usa `8082` para evitar conflito com outro projeto que já esteja usando `8080`:

```bash
sudo ss -ltnp | grep ':8082' || true
```

O Compose usa o nome de projeto `cha-rifa` e expõe a aplicação somente em `127.0.0.1:${APP_PORT}`. Banco e backend não publicam portas na VPS. O arquivo [deploy/hostinger-nginx.conf](deploy/hostinger-nginx.conf) já está configurado para o IP `148.230.72.85` e encaminha para a porta `8082`:

```bash
sudo cp deploy/hostinger-nginx.conf /etc/nginx/sites-available/cha-rifa
sudo ln -s /etc/nginx/sites-available/cha-rifa /etc/nginx/sites-enabled/cha-rifa
sudo nginx -t
sudo systemctl reload nginx
```

Depois de apontar o domínio, emita o certificado HTTPS com o Certbot já configurado na VPS.

### Deploy automático com GitHub Actions

O workflow `.github/workflows/deploy.yml` valida o Compose e atualiza a aplicação após cada push na branch `main`. Cadastre estes secrets em **Settings > Secrets and variables > Actions** no GitHub:

| Secret | Valor esperado |
|---|---|
| `VPS_HOST` | `148.230.72.85` |
| `VPS_PORT` | Porta SSH, normalmente `22` |
| `VPS_USER` | Usuário Linux proprietário de `workspace-rifa` |
| `VPS_PROJECT_PATH` | Caminho absoluto, por exemplo `/home/usuario/workspace-rifa` |
| `VPS_SSH_PRIVATE_KEY` | Chave SSH privada exclusiva do deploy |
| `VPS_KNOWN_HOSTS` | Linha da chave pública do host SSH da VPS |

O repositório deve ser clonado uma vez manualmente na VPS, e o usuário do deploy precisa conseguir executar Docker sem `sudo`. Se o repositório GitHub for privado, configure também uma deploy key de leitura na VPS para que o `git pull` funcione sem interação.

Depois do primeiro deploy manual, crie a variável de repositório `ENABLE_VPS_DEPLOY` com o valor `true` em **Settings > Secrets and variables > Actions > Variables**. Até essa variável existir, pushes na `main` não tentam acessar uma VPS ainda não preparada. O botão **Run workflow** continua disponível para disparos manuais.

Valide a publicação antes e depois de configurar o domínio:

```bash
curl http://127.0.0.1:8082/actuator/health
curl http://127.0.0.1:8082/api/v1/raffles/active
docker compose logs --tail=100 backend frontend
```

Para atualizar uma publicação:

```bash
git pull
docker compose up -d --build
```

Não execute `docker compose down -v` em produção: a opção `-v` remove o volume do PostgreSQL.

## Configurações

| Variável | Finalidade | Padrão |
|---|---|---|
| `POSTGRES_DB` | Nome do banco | `rifa` |
| `POSTGRES_USER` | Usuário do banco | `rifa` |
| `POSTGRES_PASSWORD` | Senha obrigatória | sem padrão |
| `WHATSAPP_NUMBER` | WhatsApp com país e DDD | obrigatório |
| `PIX_KEY` | Chave usada no QR Code e Pix Copia e Cola | obrigatório para Pix |
| `PIX_RECEIVER_NAME` | Nome do recebedor, com até 25 caracteres no BR Code | obrigatório para Pix |
| `PIX_RECEIVER_CITY` | Cidade do recebedor, com até 15 caracteres no BR Code | obrigatório para Pix |
| `RESERVATION_MINUTES` | Tempo de reserva | `15` |
| `APP_PORT` | Porta local usada pelo Nginx do host | `8082` |
| `ADMIN_USERNAME` | Usuário do painel administrativo | `admin` |
| `ADMIN_PASSWORD` | Senha do painel | obrigatório |
| `JWT_SECRET` | Chave de assinatura, mínimo 32 caracteres | obrigatório |
| `JWT_EXPIRATION_MINUTES` | Duração da sessão administrativa | `480` |

Dados da rifa inicial, como prêmios, data e preço, ficam na migration `backend/src/main/resources/db/migration/V2__seed_raffle.sql`.

## API pública

```text
GET  /api/v1/raffles/active
GET  /api/v1/raffles/{raffleId}/numbers
POST /api/v1/raffles/{raffleId}/reservations
PATCH /api/v1/raffles/{raffleId}/reservations/{reservationId}/payment-reported
GET  /actuator/health
```

## Área administrativa

Acesse `http://localhost:4200/admin/login`. O primeiro administrador é criado automaticamente a partir de `ADMIN_USERNAME` e `ADMIN_PASSWORD`. Se a senha do `.env` mudar, o hash BCrypt armazenado no banco será atualizado na próxima inicialização do backend.

O painel apresenta participantes, telefones, números, forma de pagamento, situação, valor, criação e vencimento. Reservas pendentes podem ser confirmadas ou canceladas. A confirmação fixa os números como vendidos; o cancelamento libera os números novamente.

Nas reservas Pix, a aplicação gera um payload BR Code com valor e identificador da reserva, apresenta Pix Copia e Cola e QR Code e permite ao participante informar que realizou o pagamento. Isso altera a situação para `PAYMENT_REPORTED` (Pagamento informado) e mantém os números reservados até a conferência administrativa. Essa declaração não confirma automaticamente o recebimento: o administrador ainda deve verificar o extrato e confirmar ou cancelar a reserva.

O login devolve um JWT assinado por `JWT_SECRET`. O Angular mantém esse token no `sessionStorage`, adiciona o cabeçalho `Authorization: Bearer` às chamadas administrativas e remove a sessão ao receber HTTP 401. O backend exige o papel `ADMIN` independentemente da proteção de rota do frontend.

Endpoints administrativos:

```text
POST  /api/v1/admin/auth/login
GET   /api/v1/admin/reservations
PATCH /api/v1/admin/reservations/{id}/confirm
PATCH /api/v1/admin/reservations/{id}/cancel
```
