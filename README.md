# Monitoramento da Infraestrutura Elétrica

**UniCesumar – Engenharia de Software – AEP 4º Semestre (2026.2) – 2ª Entrega**

| Integrante | RA |
|---|---|
| Guilherme Duque Carneiro de Souza | 25000303-2 |
| João Victor Dalbem | 25000170-2 |
| Vitor Hugo Rodrigues Garcia | 25000197-2 |

## Descoberta e alinhamento com a ODS 9

As concessionárias trabalham com manutenção majoritariamente **reativa**: falhas só são descobertas depois que equipamentos queimam ou que clientes reclamam, e é difícil localizar a origem exata das interrupções. O sistema moderniza a infraestrutura elétrica urbana integrando sensores (IoT) e inteligência de software, antecipando falhas, apontando consumo anômalo e acelerando o restabelecimento do serviço. Alinhado à **ODS 9 – Indústria, Inovação e Infraestrutura**.

## Requisitos Funcionais

- **RF01** O sistema deve permitir o cadastro de transformadores, com identificador único, coordenadas de localização (latitude e longitude), capacidade nominal e data da última manutenção.
- **RF02** O sistema deve permitir o monitoramento contínuo da rede, recebendo e armazenando dados de temperatura, consumo instantâneo e status de energia enviados pelos sensores nos postes e transformadores.
- **RF03** O sistema deve emitir alertas automáticos para o painel da equipe de operação sempre que a temperatura ou consumo de um transformador cadastrado ultrapassar o limite de segurança preestabelecido.
- **RF04** O sistema deve registrar automaticamente incidentes de "locais sem energia", associando a queda ao transformador mais próximo e marcando o horário e local exato da interrupção do serviço.
- **RF05** O sistema deve gerar relatórios de consumo excessivo, permitindo filtrar por bairro ou período, exibindo quais equipamentos operaram acima da capacidade ideal.

## Cronograma de Execução / Backlog

| Sprint / Data | Épico | User Story | Responsável |
|---|---|---|---|
| Sprint 1 (05/09 a 19/09) | Infraestrutura e Cadastros | Como Operador, eu quero cadastrar transformadores e postes (RF01) para que o sistema possa mapear a rede de distribuição física. | Guilherme |
| Sprint 1 (05/09 a 19/09) | Infraestrutura e Cadastros | Como Desenvolvedor, eu quero estruturar a arquitetura em camadas (MVC) e o Banco de Dados relacional para que a equipe tenha a base do projeto pronta para as regras de negócio. | João Victor |
| Sprint 1 (05/09 a 19/09) | Infraestrutura e Cadastros | Como Usuário, eu quero me autenticar no sistema com matrícula e senha para que o acesso ao Painel de Operação seja seguro. | Vitor Hugo |
| Sprint 2 (20/09 a 04/10) | Coleta de Dados (IoT) | Como Operador, eu quero que o sistema receba dados contínuos de temperatura e consumo dos sensores (RF02) para que a rede seja monitorada em tempo real. | João Victor |
| Sprint 2 (20/09 a 04/10) | Coleta de Dados (IoT) | Como Desenvolvedor, eu quero implementar um mock (simulador) de envio de dados dos sensores para que possamos testar a recepção das informações no backend em Java. | Guilherme |
| Sprint 2 (20/09 a 04/10) | Coleta de Dados (IoT) | Como Operador, eu quero visualizar um Dashboard no Painel de Operação para que eu possa acompanhar o status de energia ativo/inativo dos equipamentos. | Vitor Hugo |
| Sprint 3 (05/10 a 19/10) | Alertas e Incidentes | Como Operador de Painel, eu quero receber alertas automáticos ao ultrapassar limites de segurança (RF03) para que eu possa antecipar a queima de equipamentos. | Vitor Hugo |
| Sprint 3 (05/10 a 19/10) | Alertas e Incidentes | Como Sistema, eu quero registrar quedas de energia automaticamente associando ao transformador mais próximo (RF04) para que o local exato da falha seja mapeado. | João Victor |
| Sprint 3 (05/10 a 19/10) | Alertas e Incidentes | Como Operador, eu quero despachar uma equipe técnica vinculada a um incidente aberto para que o restabelecimento do serviço seja iniciado fisicamente. | Guilherme |
| Sprint 4 (20/10 a 03/11) | Relatórios e Entrega | Como Gestor, eu quero gerar relatórios de consumo excessivo com filtros por bairro e período (RF05) para que eu identifique sobrecargas constantes. | Guilherme |
| Sprint 4 (20/10 a 03/11) | Relatórios e Entrega | Como Gestor, eu quero exportar o relatório gerado para formato PDF para que eu possa arquivar e apresentar os dados em reuniões estratégicas. | Vitor Hugo |
| Sprint 4 (20/10 a 03/11) | Relatórios e Entrega | Como Desenvolvedor, eu quero realizar a revisão do código, testes de integração e o push final no GitHub para que o projeto atenda a todos os requisitos da AEP. | João Victor |

## Rastreabilidade: requisito → código

| Requisito | Onde está implementado |
|---|---|
| RF01 | `TransformadorService`, `TransformadorDAO` (CRUD completo), `PosteService`/`PosteDAO`; menus 1 e 2 |
| RF02 | `MonitoramentoService.processarLeitura`, `LeituraSensorDAO`, `SimuladorSensor`; menu 4 |
| RF03 | `Transformador.verificarLimitesSeguranca`, `MonitoramentoService`, `Alerta.emitirAlertaPainel`, `PainelOperacao`; menu 5 |
| RF04 | `IncidenteService.registrarQuedaEnergia` (Haversine em `GeoUtil`), `IncidenteDAO`; menu 6 |
| RF05 | `RelatorioService`, `RelatorioDAO`, `Relatorio.exportarPDF`; menu 7 |
| Login | `AuthService`, `Usuario.autenticar` (hash PBKDF2) |

## Arquitetura

Arquitetura em **camadas**: `view` (console) → `service` (regras de negócio) → `dao` (JDBC) → banco PostgreSQL no **Supabase**. O pacote `model` contém as entidades do diagrama de classes.

```
├── src/main/java/br/com/unicesumar/monitoramento
│   ├── Main.java
│   ├── config/    DatabaseConnection
│   ├── model/     Usuario*, Operador, Equipamento*, Poste, Transformador, Sensor, LeituraSensor,
│   │              Alerta, Incidente, StatusIncidente, TipoAlerta, PainelOperacao, Relatorio  (*abstratas)
│   ├── dao/       CrudDAO (interface) + DAOs
│   ├── service/   regras de negócio e simulador IoT
│   ├── view/      MenuPrincipal
│   └── util/      SenhaUtil, GeoUtil, PdfSimples, Entrada
├── database/      schema.sql, seed.sql
├── docs/          PDF da 1ª entrega (UML e DER)
└── pom.xml
```

**Pilares de POO:** classes abstratas `Usuario` e `Equipamento`; herança (`Operador`, `Poste`, `Transformador`); polimorfismo com `@Override` (`getPerfil`, `getTipo`, `descrever`); interface `CrudDAO<T>`; composição 1:N (`Equipamento` → `Sensor` → `LeituraSensor`).

## Como executar

**Requisitos:** JDK 17 ou superior e Maven 3.8+ (Windows, Linux ou macOS).

1. **Banco (Supabase):** crie um projeto em supabase.com. No *SQL Editor*, execute `database/schema.sql` e, opcionalmente, `database/seed.sql` (dados de exemplo; rodar apenas uma vez).
2. **Credenciais:** copie `.env.example` para `.env` e preencha com os dados de *Project Settings → Database → Connection string → JDBC*. Prefira o **Session pooler** (funciona em redes IPv4). O `.env` está no `.gitignore`; nunca o envie ao GitHub.
3. **Compilar e rodar:**

```bash
mvn clean package
java -jar target/monitoramento.jar
```

> Execute o comando na raiz do projeto (onde está o `.env`). Alternativamente, defina `SUPABASE_DB_URL`, `SUPABASE_DB_USER` e `SUPABASE_DB_PASSWORD` como variáveis de ambiente.
> No Windows, se os acentos aparecerem trocados, use `chcp 65001` antes de executar.

No primeiro acesso o sistema pede para criar o primeiro operador; depois, basta entrar com matrícula e senha. Sugestão de teste: menu 4 → simulador IoT, depois menus 5, 6 e 7.

## Observações sobre o UML/DER

- Mantidos fielmente; no banco os nomes usam `snake_case` (padrão PostgreSQL).
- As operações `cadastrar/atualizar/excluir/buscar` de `Equipamento` ficam nos DAOs/serviços, para isolar a regra de negócio do acesso a dados (padrão em camadas).
- `PainelOperacao` ganhou a lista de sensores (status ativo/inativo no dashboard); `Sensor.enviarDados()` devolve a leitura enviada; `Transformador.verificarLimitesSeguranca` recebe temperatura e consumo.
