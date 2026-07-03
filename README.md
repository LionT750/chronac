# Alocacao de Horarios Escolares

Sistema de otimizacao de horarios academicos que utiliza o motor de restricoes do Timefold Solver para alocar automaticamente disciplinas em horarios e salas, respeitando restricoes rigidas (conflitos de horario) e maximizando preferencias (distribuicao semanal, horarios preferenciais).

Tecnologias: Java 21, Timefold Solver 2.2.0, Maven.

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

3. Execute a aplicacao:

   ```sh
   java -jar target/hello-world-run.jar
   ```

O solver executa por 5 segundos e exibe a grade de horarios otimizada no console.

## Estrutura do projeto

- `domain/` -- classes do modelo de dominio (Lesson, Timeslot, Room, Semester, Subject)
- `solver/` -- configuracao de restricoes do Timefold (`TimetableConstraintProvider`)
- `demo/` -- geracao de dados de exemplo para um semestre letivo
- `TimetableApp.java` -- ponto de entrada da aplicacao
