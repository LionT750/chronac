# Alocacao de Horarios Escolares

Sistema de otimizacao de horarios academicos que utiliza o motor de restricoes do Timefold Solver para alocar automaticamente disciplinas em horarios e salas, respeitando restricoes rigidas (conflitos de horario) e maximizando preferencias (distribuicao semanal, horarios preferenciais).

Tecnologias: Java 21, Spring Boot 3, Timefold Solver 2.2.0, Maven.

## Como executar

1. Instale Java 21 e Maven:

   ```sh
   sdk install java
   sdk install maven
   ```

2. Compile e gere o JAR executavel:

   ```sh
   mvn clean package
   ```

3. Execute a aplicacao (Spring Boot):

   ```sh
   java -jar target/chronac.jar --spring.profiles.active=dev
   ```

O build Maven compila automaticamente a UI (Vite/React) via `frontend-maven-plugin` e a empacota dentro do jar em `target/classes/static`. A UI fica disponivel em `http://localhost:8080` junto com a API (mesma origem, sem CORS).

Ao iniciar, a aplicacao resolve o mesmo problema de demonstracao ("MultiTurma Demo") que a versao anterior resolvia no `main`, rodando por 60 segundos (configuravel em `src/main/resources/application.properties` via `timefold.solver.termination.spent-limit`). A melhor solucao encontrada e mantida em memoria e pode ser consumida pela UI para demonstracao ao vivo.

## Endpoints REST

A aplicacao sobe em `http://localhost:8080`:

- `GET /api/timetable` -- retorna o timetable (problema + melhor solucao encontrada ate o momento) em JSON.
- `GET /api/sayHeyMaster` -- endpoint de demonstracao do servidor legado.

O score e serializado no formato padrao do Timefold como string, ex.: `"0hard/-3soft"`.

## Estrutura do projeto

- `domain/` -- classes do modelo de dominio (Lesson, Timeslot, Room, Semester, Subject, Curriculum, TeacherSchedule, Week)
- `solver/` -- configuracao de restricoes do Timefold (`TimetableConstraintProvider`) e justificativas
- `demo/` -- gerador dos dados de demonstracao (`TimetableDemoData`)
- `service/` -- resolve a demonstracao na inicializacao e guarda a melhor solucao (`TimetableDemoSolver`)
- `rest/` -- controller Spring (`TimetableController`)
- `TimetableSpringBootApp.java` -- ponto de entrada da aplicacao (Spring Boot)

## Frontend (UI)

O guia do frontend está em [ui/README.md](ui/README.md), com links para o estado atual, o histórico de entregas e os próximos passos.

A UI React/Vite e compilada automaticamente durante o `mvn package` e servida pelo proprio Spring Boot em `http://localhost:8080` (sem precisar de servidor separado).

Para desenvolver com hot reload, rode a UI separadamente em `http://localhost:5173` (o `/api` e proxiado pelo Vite para `http://localhost:8080`):

```sh
cd ui
npm install
npm run dev
```

Chamadas cross-origin do dev server sao liberadas via CORS em `application.properties` (`chronac.cors.allowed-origins`, configuravel pela variavel `CHRONAC_CORS_ORIGINS`).

## Site institucional

A apresentação pública do Chronac está em `/chronac` (também aceita `/chronac/`).
Em desenvolvimento, acesse `http://localhost:5173/chronac` após executar `npm run dev`
na pasta `ui`. No pacote Spring Boot, a mesma rota é servida em `http://localhost:8080/chronac`.

A aplicação autenticada permanece em `/`, com `/login` e `/calendar` preservados.
Os botões do site levam ao login existente. A landing page não monta o `AuthGate`
nem consulta a API. O calendário público reutiliza `CalendarView` e `LessonCard`,
com dados fictícios locais, navegação de períodos e filtro por curso. Os estilos
ficam restritos a `.landing-page`; o tema utiliza o `ThemeProvider` existente.

Os componentes e textos estão em `ui/src/features/landing/`. A interface pública
explicita o estágio beta e a evolução das permissões específicas por perfil.

## Autenticação

A tela `/login` é obrigatória antes de montar as páginas do sistema. Todas as rotas
`/api/**` exigem autenticação, exceto a obtenção de CSRF e o login. O calendário e a
navegação interna continuam com a estrutura anterior.

Para desenvolvimento local, execute:

```sh
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

O perfil `dev` disponibiliza `admin123@senac.com` com a senha de teste `admin1234`.
Somente o hash BCrypt (custo 12) fica na configuração do backend; nenhuma credencial
é incorporada ao frontend. Sem esse perfil, não existe conta ou chave padrão.

Para produção, configure no ambiente do backend:

- `CHRONAC_ADMIN_EMAIL`: email do administrador.
- `CHRONAC_ADMIN_PASSWORD_HASH`: hash BCrypt de custo 12 de uma senha própria
  (máximo de 72 bytes UTF-8). Gere com `BCryptPasswordEncoder(12).encode(...)`,
  preservando os caracteres `$` ao definir a variável no ambiente.
- `CHRONAC_JWT_SECRET`: chave aleatória com no mínimo 32 bytes, codificada em Base64.
  Mantenha a mesma chave entre reinicializações. Nunca use uma variável `VITE_*`.

Execute sem o perfil `dev`, por HTTPS. Os cookies usam `Secure` por padrão;
somente `dev` permite HTTP local. A aplicação recusa iniciar sem as configurações
de produção. O perfil `dev` gera uma chave temporária quando não há chave configurada,
portanto reiniciar o backend local encerra as sessões anteriores.

O JWT dura 8 horas e é retornado por `Set-Cookie` com `HttpOnly`, `SameSite=Strict`
e caminho `/api`. O navegador persiste a sessão; o frontend não usa localStorage
nem recebe o token no JSON. Atualizar a página consulta `GET /api/auth/me`.
`GET /api/auth/csrf` fornece o token CSRF, enviado no cabeçalho de login e logout.
`POST /api/auth/login` recebe email/senha e retorna os dados públicos do usuário.
`POST /api/auth/logout` revoga o JWT e remove o cookie.

Os papéis `ADMIN`, `PROFESSOR` e `ALUNO` já existem no backend e são convertidos
em autoridades `ROLE_*`. Apenas a conta administrativa inicial é disponibilizada;
cadastro de usuários e regras de acesso específicas por perfil ficam para a próxima etapa.
O projeto ainda não tem banco: o usuário inicial vem da configuração do servidor.
A lista de tokens revogados fica em memória; para múltiplas instâncias ou revogação
durável após reinícios, migre essa lista para armazenamento compartilhado persistente.

Validação: `mvn test`, `cd ui && npm test` e `npm run build`.

Referências: [JWT no Spring Security](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
e [proteção CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html).

## Git workflow

Instrucoes de como contribuir e enviar seu trabalho estao no arquivo git_workflow.md
