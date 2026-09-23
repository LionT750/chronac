# Chronac — frontend

Interface React do Chronac: calendário acadêmico, autenticação, cadastros e apresentação pública. Esta pasta concentra o código e a documentação do frontend.

## Documentação

- [Estado atual](docs/ESTADO_ATUAL.md): o que existe, como está organizado, quais integrações funcionam e quais limitações foram encontradas.
- [Próximos passos](docs/PROXIMOS_PASSOS.md): tarefas priorizadas, dependências e critérios de aceite.
- [Histórico de entregas](docs/HISTORICO.md): registro do que já foi feito e das próximas entregas concluídas.
- [Visão geral do projeto](../README.md): backend, autenticação e empacotamento.

## Situação do produto

Calendário e login estão conectados à API. A grade é a demonstração calculada pelo backend. Os cadastros de professores e turmas ainda usam dados locais; o site público também tem dados demonstrativos próprios. A integração dos cadastros e a geração com dados reais estão no plano de próximos passos.

## Estrutura

- `src/main.jsx`: inicialização e providers.
- `src/app/`: aplicação ativa, layout e menu.
- `src/features/auth/`: login e sessão.
- `src/features/calendar/`: calendário, filtros, detalhes, estado e serviço da grade.
- `src/features/registrations/`: cadastros de professores e turmas.
- `src/features/landing/`: site público em `/chronac`.
- `src/components/`, `src/hooks/`, `src/lib/` e `src/styles/`: recursos compartilhados.
- `tests/`: testes de utilitários e serviços.
- `docs/`: documentação e acompanhamento do trabalho.

A entrada utilizada é `src/app/App.jsx`. O arquivo `src/App.jsx` é legado e sua revisão está prevista no plano.

## Executar em desenvolvimento

Use Node/npm compatíveis com as dependências do projeto. O build Maven configura Node `v24.11.1`. Na pasta `ui`, execute:

```powershell
npm ci
npm run dev
```

Acesse `http://localhost:5173` para o sistema ou `http://localhost:5173/chronac` para o site público. Para login e calendário, inicie o backend na raiz do repositório em outro terminal:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

O proxy do Vite encaminha `/api` para `http://localhost:8080`. A configuração da conta de desenvolvimento está no README da raiz. A landing page funciona sem o backend.

## Verificar e empacotar

Na pasta `ui`:

```powershell
npm test
npm run lint
npm run build
```

O build gera `dist/`. `npm run preview` permite inspecionar o build estático, mas não há proxy da API configurado para esse modo. Para validar o pacote integrado, execute `mvn clean package` na raiz e inicie `target/chronac.jar` conforme o README principal.

Os resultados e limites da última verificação estão em [Estado atual](docs/ESTADO_ATUAL.md#verificações-disponíveis). A existência dos comandos não significa que todos estejam passando.

## Manter a documentação

1. Antes de iniciar uma mudança, registre ou atualize a tarefa em `docs/PROXIMOS_PASSOS.md`, incluindo o resultado esperado e as dependências.
2. Durante o trabalho, atualize o status e registre bloqueios concretos.
3. Ao concluir, valide o aceite, marque a tarefa e registre comportamento, testes e commit/PR em `docs/HISTORICO.md`.
4. Atualize `docs/ESTADO_ATUAL.md` quando houver mudança de arquitetura, integração ou limitação.

Use “implementado” para código existente e explicite quando ele depende de mocks. Use “validado” apenas para o comportamento efetivamente verificado. Preserve itens concluídos no plano para manter a rastreabilidade.

O arquivo `CHANGELOG.md` desta pasta pertence ao Node.js; o histórico do Chronac é `docs/HISTORICO.md`.
