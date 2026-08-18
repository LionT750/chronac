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

A UI React/Vite consome `GET /api/timetable` (proxy `/api` -> `http://localhost:8080`):

```sh
cd ui
npm install
npm run dev
```

## Git workflow

Instrucoes de como contribuir e enviar seu trabalho estao no arquivo git_workflow.md