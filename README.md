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
   java -jar target/chronac.jar
   ```

O build Maven compila automaticamente a UI (Vite/React) via `frontend-maven-plugin` e a empacota dentro do jar em `target/classes/static`. A UI fica disponivel em `http://localhost:8080` junto com a API (mesma origem, sem CORS).

Ao iniciar, a aplicacao resolve o problema de demonstracao ("MultiTurma Demo") com uma **semente fixa** (`TimetableGenerator.DEMO_SEED`, hoje `5`), parando assim que a busca passa 25 segundos sem melhorar (teto de 90 segundos). A semente fixa a trajetoria da busca, entao a demonstracao chega sempre ao mesmo patamar de qualidade -- uma demonstracao precisa de um horario que se possa explicar, nao do que a ultima rodada por acaso encontrou. Ajustavel sem recompilar via `chronac.demo.seed`, `chronac.demo.seconds` e `chronac.demo.unimproved-seconds` em `src/main/resources/application.properties`.

Com a semente 5 o resultado e `0hard/0medium/-69soft`: duas noites vagas no meio de uma faixa ativa, cinco aulas deslocadas do dia proprio da UC e dois buracos por indisponibilidade da professora -- a um deslocamento do melhor arranjo que alguem montou a mao para estes dados (ver `TimetableTargetLayoutTest`). Roda em cerca de 35 segundos.

Um limite por numero de passos foi tentado, porque tornaria a semente reproduzivel bit a bit em qualquer maquina, mas nao se calibra neste problema: o custo de um passo explode conforme o horario se aproxima do viavel (~6500 passos/s ainda em -3hard, ~900/s perto de zero), entao nenhum alvo de passos significa "convergiu".

## Como o modelo funciona

O solver nao aloca aula por aula. Ele decide onde **cada UC comeca**:

- **Track** -- a "faixa" de uma turma num dia da semana: "Jovem Programador nas quartas". Cada track e a lista ordenada de noites (`Slot`) disponiveis naquele dia.
- **Block** -- a unica entidade de planejamento: a corrida de noites de uma parte de UC (`SubjectPart`). Escolhe o slot onde comeca e ocupa os slots seguintes do mesmo track ate cumprir a carga horaria, pulando datas em que o professor esta indisponivel (o que deixa um buraco).
- **Overflow** -- quando o dia escolhido nao cabe a UC inteira (Rodolfo precisa de 23 aulas no Jovem mas nao da aula na quarta, e um track de quinta tem 21 noites), o que sobra vai para um segundo dia como uma corrida curta e contigua: a reposicao de fim de semestre. **Quantas** aulas sobram nao e decisao do solver -- e exatamente o que nao caber, derivado de onde o block comeca. So o dia que absorve a sobra e escolhido.

Por isso "uma UC por dia da semana", "sem buraco no meio da corrida" e "cadencia semanal estavel" sao propriedades estruturais, nao restricoes a serem pontuadas. O score sobrou para os trade-offs reais, e cada peso soft conta **eventos** que se apontam no calendario -- uma noite vazia, uma reposicao, uma troca de professor evitavel -- nunca totais de aulas. E por isso que uma boa solucao pontua na casa das dezenas, e nao das dezenas de milhares.

O score usa tres niveis (`HardMediumSoftScore`):

- **hard** -- fisicamente impossivel (dois blocks no mesmo trecho de um dia, professor em duas salas na mesma noite, sobra que nao cabe em lugar nenhum, partes de uma UC em dias diferentes, professor da turma ausente na primeira semana cheia).
- **medium** -- politica da escola que nao se troca por preferencia (mudar o dia de uma UC no meio do semestre).
- **soft** -- preferencias, com pesos de 1 a 10, ajustaveis em tempo de execucao via `ConstraintWeightOverrides`.

## Endpoints REST

A aplicacao sobe em `http://localhost:8080`:

- `GET /api/timetable` -- retorna o timetable resolvido com a semente fixa da demonstracao (problema puro enquanto a resolucao inicial nao termina).
- `GET /api/generate?seed=&seconds=` -- resolve uma vez com semente e orcamento explicitos. Sem parametros, reproduz o que `/api/timetable` serve.
- `GET /api/find-perfect?maxSeeds=&seconds=` -- varre sementes e devolve a melhor. A semente que este endpoint reportar e a que deve ir para `TimetableGenerator.DEMO_SEED`.
- `GET /api/sayHeyMaster` -- endpoint de demonstracao do servidor legado.

O score e serializado no formato padrao do Timefold como string, ex.: `"0hard/0medium/-56soft"`.

## Estrutura do projeto

- `domain/` -- modelo de dominio. Planejamento: `Block` (entidade), `Slot`/`Track` (valores e faixas), `SubjectPart`. Fatos: `Subject`, `Curriculum`, `Turma`, `Semester`, `Room`, `TeacherSchedule`. Saida: `Lesson` (materializada a partir dos blocks, para a API e a UI)
- `solver/` -- configuracao de restricoes do Timefold (`TimetableConstraintProvider`) e justificativas
- `demo/` -- gerador dos dados de demonstracao (`TimetableDemoData`)
- `service/` -- `TimetableGenerator` (solver com semente fixa, varredura de sementes) e `TimetableDemoSolver` (resolve a demonstracao na inicializacao e guarda o resultado)
- `rest/` -- controller Spring (`TimetableController`)
- `TimetableSpringBootApp.java` -- ponto de entrada da aplicacao (Spring Boot)

## Frontend (UI)

A UI React/Vite e compilada automaticamente durante o `mvn package` e servida pelo proprio Spring Boot em `http://localhost:8080` (sem precisar de servidor separado).

Para desenvolver com hot reload, rode a UI separadamente em `http://localhost:5173` (o `/api` e proxiado pelo Vite para `http://localhost:8080`):

```sh
cd ui
npm install
npm run dev
```

Chamadas cross-origin do dev server sao liberadas via CORS em `application.properties` (`chronac.cors.allowed-origins`, configuravel pela variavel `CHRONAC_CORS_ORIGINS`).

## Git workflow

Instrucoes de como contribuir e enviar seu trabalho estao no arquivo git_workflow.md