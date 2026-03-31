# Gestão Acadêmica — API REST

API REST para gestão acadêmica desenvolvida no Projeto de Bloco da Infnet (fase de Elaboração — RUP/TP3).

O sistema cobre o fluxo completo de sala de aula:

- aluno consulta turmas e envia submissões;
- professor cria atividades e corrige com nota e feedback;
- diretor acompanha usuários, turmas e notificações.

Não usa banco de dados: toda a persistência é feita em arquivos CSV locais.

**Stack:** Java 21 · Spring Boot 4.0.2 · OpenCSV 5.9 · Maven

---

## Início rápido

```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. Na primeira execução, a pasta `data/` é criada com os dados iniciais automaticamente.

> Se já existir `data/usuarios.csv` de uma versão anterior (senhas em texto puro), delete o arquivo antes de subir para o DataLoader recriar com senhas hasheadas.

### Credenciais para demonstração

| Perfil | Email | Senha |
|---|---|---|
| ALUNO | carlos@aluno.com | 123456 |
| PROFESSOR | ana@professor.com | 123456 |
| DIRETOR | marcos@diretor.com | 123456 |

### Roteiro de demo (2 minutos)

1. Login como ALUNO e listar turmas / atividades.
2. Enviar uma submissão para uma atividade.
3. Login como PROFESSOR e corrigir a submissão.
4. Login como DIRETOR e listar usuários por perfil.

---

## Atualizações recentes

### Relatórios gerenciais (RF10)

- Exportação CSV por turma para diretor: `/api/relatorios/turmas/{turmaId}/engajamento.csv`.
- O relatório apresenta professor, alunos, entregas, entregas no prazo, média e pontuação de proatividade.

### Notificações por turma

- Professor e diretor podem disparar aviso para todos os alunos da turma via `/api/notificacoes/turma`.

### Filtro de atividades para aluno

- No endpoint da turma, aluno pode usar `status=Pendentes`, `status=Entregues` ou `status=Avaliadas`.
- Exemplo: `/api/atividades/turma/{turmaId}?status=Pendentes`.

### Fórum por turma (tópicos)

- Professor cria tópico (`POST /api/topicos`).
- Aluno responde tópico (`POST /api/topicos/{topicoId}/respostas`).
- Timeline geral e por turma (`GET /api/topicos/timeline` e `GET /api/topicos/turma/{turmaId}/timeline`).

### Grupos e atividade em grupo

- Alunos criam grupos por turma (`POST /api/grupos`).
- Professor/diretor finalizam grupos da turma (`PATCH /api/grupos/turma/{turmaId}/finalizar`).
- Atividade agora pode ser marcada com `emGrupo`.
- Em atividade em grupo, qualquer integrante envia e a submissão é replicada para todos do grupo.
- Na correção, nota e feedback são propagados para os integrantes, sem mudar fluxo de correção do professor.

### Upload

- Limite de arquivo em 20MB.
- Mensagem amigável para excesso: `Arquivo excede o limite permitido de 20MB.`

---

## Tecnologias

| Dependência | Uso |
|---|---|
| `spring-boot-starter-web` | Servidor HTTP embutido, endpoints REST |
| `opencsv 5.9` | Leitura e escrita de CSV mapeados para objetos Java |
| `spring-security-crypto` | BCrypt para hash de senha, sem Spring Security completo |
| `spring-boot-starter-test` | JUnit para testes (não vai no JAR final) |

---

## Estrutura do projeto

```
src/main/java/.../gestao_academica/
│
├── GestaoAcademicaApplication.java   ← ponto de entrada (main)
│
├── auth/
│   ├── AutenticacaoFilter.java       ← intercepta toda requisição, valida o token
│   ├── AutorizacaoHelper.java        ← verifica se o perfil tem permissão na rota
│   └── SessaoStore.java              ← guarda os tokens em memória (HashMap)
│
├── config/
│   ├── DataLoader.java               ← carrega dados iniciais ao subir a aplicação
│   └── SecurityConfig.java           ← declara o bean BCryptPasswordEncoder
│
├── controller/                       ← recebe as requisições HTTP, devolve respostas
│   ├── UsuarioController.java        ← /api/usuarios
│   ├── TurmaController.java          ← /api/turmas
│   ├── AtividadeController.java      ← /api/atividades
│   ├── SubmissaoController.java      ← /api/submissoes
│   └── NotificacaoController.java    ← /api/notificacoes
│
├── service/                          ← regras de negócio
│   ├── UsuarioService.java           ← registro e login com BCrypt
│   ├── TurmaService.java             ← criação de turmas, matrícula de alunos
│   ├── AtividadeService.java         ← criação de atividades, validação de prazo
│   ├── SubmissaoService.java         ← entrega de resposta, correção (nota/feedback)
│   ├── NotificacaoService.java       ← criação e leitura de notificações
│   └── StorageService.java           ← salva arquivos enviados em data/uploads/
│
├── repository/                       ← leitura e escrita nos arquivos CSV
│   ├── UsuarioCsvRepository.java
│   ├── TurmaCsvRepository.java
│   ├── AtividadeCsvRepository.java
│   ├── SubmissaoCsvRepository.java
│   ├── NotificacaoCsvRepository.java
│   └── Csv*Record.java               ← classes intermediárias para o OpenCSV mapear colunas
│
├── model/                            ← entidades do domínio
│   ├── Usuario.java                  ← classe base (id, nome, email, senha, perfil)
│   ├── Aluno.java                    ← extends Usuario — atributo: matrícula
│   ├── Professor.java                ← extends Usuario — atributo: departamento
│   ├── Diretor.java                  ← extends Usuario — atributo: cargo
│   ├── Turma.java                    ← código, disciplina, professor, lista de alunos
│   ├── Atividade.java                ← título, prazo, status, turma, professor
│   ├── Submissao.java                ← arquivo entregue, nota, feedback, aluno, atividade
│   ├── Notificacao.java              ← mensagem, destinatário, flag lida
│   ├── PerfilUsuario.java            ← enum: ALUNO, PROFESSOR, DIRETOR
│   └── StatusAtividade.java          ← enum: PUBLICADA, ENCERRADA
│
├── dto/                              ← objetos de entrada e saída das requisições
│
└── exception/
    ├── GlobalExceptionHandler.java   ← mapeia exceções para os códigos HTTP corretos
    ├── CredenciaisInvalidasException ← → 401
    ├── UsuarioNaoEncontradoException ← → 404
    └── UsuarioJaExisteException      ← → 409
```

---

## Fluxo de uma requisição

```
Requisição HTTP
      ↓
AutenticacaoFilter       ← valida o token no header X-Token
      ↓
Controller               ← recebe os dados, verifica o perfil via AutorizacaoHelper
      ↓
Service                  ← aplica a regra de negócio
      ↓
Repository               ← lê ou escreve no arquivo CSV
      ↓
Resposta JSON
```

---

## Dados pré-cadastrados

Na primeira execução o `DataLoader` insere automaticamente:

| Nome | Email | Senha | Perfil |
|---|---|---|---|
| Ana Lima | ana@professor.com | 123456 | PROFESSOR |
| Bruno Costa | bruno@professor.com | 123456 | PROFESSOR |
| Carlos Silva | carlos@aluno.com | 123456 | ALUNO |
| Diana Souza | diana@aluno.com | 123456 | ALUNO |
| Eduardo Reis | eduardo@aluno.com | 123456 | ALUNO |
| Marcos Diretor | marcos@diretor.com | 123456 | DIRETOR |

E duas turmas pré-criadas com alunos já matriculados.

---

## Endpoints principais

| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| POST | `/api/usuarios/registrar` | público | cria conta (ALUNO ou PROFESSOR) |
| POST | `/api/usuarios/login` | público | autentica e retorna token |
| POST | `/api/usuarios/logout` | qualquer | invalida o token |
| GET | `/api/usuarios/me` | qualquer | dados do usuário logado |
| GET | `/api/turmas/aluno/{id}` | ALUNO | turmas do aluno |
| POST | `/api/turmas` | PROFESSOR, DIRETOR | cria turma |
| POST | `/api/turmas/{id}/matricular/{alunoId}` | PROFESSOR, DIRETOR | matricula aluno |
| POST | `/api/atividades` | PROFESSOR, DIRETOR | cria atividade |
| GET | `/api/atividades/turma/{id}?status=Pendentes` | ALUNO | lista atividades da turma com filtro |
| POST | `/api/submissoes` | ALUNO | entrega resposta (multipart) |
| PATCH | `/api/submissoes/{id}/corrigir` | PROFESSOR, DIRETOR | lança nota e feedback |
| GET | `/api/notificacoes/minhas` | qualquer | notificações do usuário logado |
| PATCH | `/api/notificacoes/{id}/lida` | dono | marca notificação como lida |
| POST | `/api/notificacoes/turma` | PROFESSOR, DIRETOR | envia mensagem para todos os alunos da turma |
| GET | `/api/relatorios/turmas/{turmaId}/engajamento.csv` | DIRETOR | exporta relatório gerencial em CSV |
| POST | `/api/topicos` | PROFESSOR | cria tópico de fórum na turma |
| POST | `/api/topicos/{topicoId}/respostas` | ALUNO | responde tópico da turma |
| GET | `/api/topicos/timeline` | autenticado | timeline geral do fórum |
| GET | `/api/topicos/turma/{turmaId}/timeline` | autenticado | timeline por turma |
| POST | `/api/grupos` | ALUNO | cria grupo na turma |
| GET | `/api/grupos/turma/{turmaId}` | ALUNO, PROFESSOR, DIRETOR | lista grupos da turma |
| PATCH | `/api/grupos/turma/{turmaId}/finalizar` | PROFESSOR, DIRETOR | finaliza formação dos grupos |

---

## Onde ficam os dados

```
data/
├── usuarios.csv       ← contas cadastradas (senhas com BCrypt)
├── turmas.csv         ← turmas e alunos matriculados
├── atividades.csv     ← atividades criadas pelos professores
├── submissoes.csv     ← entregas com nota e feedback
├── notificacoes.csv   ← notificações geradas automaticamente
├── topicos.csv         ← tópicos de discussão por turma
├── mensagens.csv       ← respostas dos tópicos
├── grupos.csv          ← grupos de alunos por turma
└── uploads/           ← arquivos enviados pelos alunos
```

Essa pasta está no `.gitignore` e não vai para o repositório.

---

## Importar no Postman

1. Abra o Postman → **Import**
2. Importe `Gestão Acadêmica API.postman_collection.json` e `gestao-academica.postman_environment.json`
3. Selecione o environment **Gestão Acadêmica — Local**

As variáveis `token`, `alunoId`, `professorId`, `diretorId`, `turmaId`, `atividadeId`, `submissaoId`, `notificacaoId` e `topicoId` são preenchidas automaticamente pelos scripts ao fazer login ou criar recursos.

---

## FAQ

**Por que CSV e não banco de dados?**
Requisito do projeto de bloco. CSV demonstra persistência sem infraestrutura externa — qualquer máquina roda sem instalar nada além do Java.

**Como o OpenCSV funciona?**
Cada repositório tem uma classe `Csv*Record` com anotações `@CsvBindByName`. O OpenCSV lê o cabeçalho do arquivo e mapeia cada coluna para o campo correspondente automaticamente. Na escrita, faz o caminho inverso.

**Como funciona o login?**
1. Cliente envia email e senha em `POST /api/usuarios/login`
2. `UsuarioService` busca o usuário pelo email e compara a senha com BCrypt
3. Se válido, `SessaoStore` gera um UUID, guarda em memória e devolve no campo `token`
4. Nas próximas requisições, o cliente passa esse token no header `X-Token`
5. `AutenticacaoFilter` intercepta toda requisição, valida o token e injeta o usuário na requisição via `request.setAttribute`

**A senha está segura?**
É hasheada com BCrypt antes de salvar no CSV — nunca armazenada em texto puro. Na comparação do login, usa `BCryptPasswordEncoder.matches()`, que compara sem reverter o hash.

**Como funciona o controle de acesso?**
`AutorizacaoHelper.exigirPerfil()` lê o usuário do atributo da requisição e compara o perfil com os permitidos para aquela rota. Se não bater, retorna `403`. Se não estiver autenticado, `401`.

**Por que não usou Spring Security completo?**
O controle de acesso do projeto é simples o suficiente para ser feito manualmente com filter e helper. Spring Security completo adicionaria complexidade sem ganho real para o escopo do trabalho.

**Como um aluno entrega uma atividade?**
`POST /api/submissoes` com o arquivo em multipart/form-data. O serviço verifica se o prazo está em aberto, salva o arquivo em `data/uploads/`, registra a submissão no CSV e cria uma notificação automática para o professor responsável.

**Como o professor corrige?**
`PATCH /api/submissoes/{id}/corrigir` com `{ "nota": 9.5, "feedback": "..." }`. Atualiza os campos `nota` e `feedback` diretamente no registro da submissão no CSV.

**Onde está a hierarquia de classes?**
Em `model/`. `Usuario` é a classe base com os atributos comuns. `Aluno`, `Professor` e `Diretor` estendem `Usuario`, cada um com seu atributo específico. O perfil é determinado pelo enum `PerfilUsuario`.
