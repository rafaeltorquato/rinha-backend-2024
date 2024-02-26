# rinha-backend-2024 (Death Fast Version)
![death-punch.png](death-punch.png)

Projeto Java para a [Rinha de Backend 2024 Q1](https://github.com/zanfranceschi/rinha-de-backend-2024-q1).

[Repositório](https://github.com/rafaeltorquato/rinha-backend-2024) com os fontes **branch death-fast**.

**Death Fast Version** - Versão que prioriza velocidade em desfavor da durabilidade dos dados.

## Antes de começar
Dada as premissas da rinha, algumas decisões foram tomadas para deixar o projeto mais performático possível. 
Certas decisões não seriam tomadas em um projeto real, principalmente em projetos que tratam transações financeiras.

## Stack

* Java 21.0.2 Native image GraalVM;
  * Virtual Threads;
* Framework Quarkus (latest);
* PostgreSQL (latest);
  * Stored Procedures;
* JDBC;

## Executando o projeto
Build:
```shell script
./mvnw clean package -Pnative && docker build -f src/main/docker/Dockerfile.native-micro --platform linux/amd64 -t rafaeltorquato/rinha-backend-2024-q1-fast .
```
Run:
```shell script
docker compose up
```


[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
