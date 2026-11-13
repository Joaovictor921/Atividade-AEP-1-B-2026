package br.com.unicesumar.monitoramento.view;

import br.com.unicesumar.monitoramento.model.Alerta;
import br.com.unicesumar.monitoramento.model.Equipamento;
import br.com.unicesumar.monitoramento.model.Incidente;
import br.com.unicesumar.monitoramento.model.Operador;
import br.com.unicesumar.monitoramento.model.PainelOperacao;
import br.com.unicesumar.monitoramento.model.Poste;
import br.com.unicesumar.monitoramento.model.Relatorio;
import br.com.unicesumar.monitoramento.model.Sensor;
import br.com.unicesumar.monitoramento.model.Transformador;
import br.com.unicesumar.monitoramento.service.AlertaService;
import br.com.unicesumar.monitoramento.service.IncidenteService;
import br.com.unicesumar.monitoramento.service.MonitoramentoService;
import br.com.unicesumar.monitoramento.service.PainelService;
import br.com.unicesumar.monitoramento.service.PosteService;
import br.com.unicesumar.monitoramento.service.RelatorioService;
import br.com.unicesumar.monitoramento.service.SensorService;
import br.com.unicesumar.monitoramento.service.SimuladorSensor;
import br.com.unicesumar.monitoramento.service.TransformadorService;
import br.com.unicesumar.monitoramento.util.Entrada;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Camada de apresentacao (console): menus que chamam apenas a camada de servico. */
public class MenuPrincipal {

    @FunctionalInterface
    private interface Acao {
        void executar() throws SQLException, IOException;
    }

    private final Operador operador;
    private final TransformadorService transformadorService = new TransformadorService();
    private final PosteService posteService = new PosteService();
    private final SensorService sensorService = new SensorService();
    private final MonitoramentoService monitoramentoService = new MonitoramentoService();
    private final SimuladorSensor simulador = new SimuladorSensor(monitoramentoService);
    private final IncidenteService incidenteService = new IncidenteService();
    private final AlertaService alertaService = new AlertaService();
    private final PainelService painelService = new PainelService();
    private final RelatorioService relatorioService = new RelatorioService();

    public MenuPrincipal(Operador operador) {
        this.operador = operador;
    }

    public void executar() {
        int opcao;
        do {
            System.out.println("\n=========== MONITORAMENTO DA INFRAESTRUTURA ELETRICA ===========");
            System.out.println("Operador: " + operador.getNome() + " | Turno: " + operador.getTurno());
            System.out.println("1 - Transformadores (CRUD)");
            System.out.println("2 - Postes (CRUD)");
            System.out.println("3 - Sensores");
            System.out.println("4 - Monitoramento (leituras / simulador IoT)");
            System.out.println("5 - Painel de Operacao (dashboard e alertas)");
            System.out.println("6 - Incidentes (locais sem energia)");
            System.out.println("7 - Relatorios de consumo excessivo");
            System.out.println("0 - Sair");
            opcao = Entrada.inteiro("Opcao: ");
            switch (opcao) {
                case 1 -> menuTransformadores();
                case 2 -> menuPostes();
                case 3 -> menuSensores();
                case 4 -> menuMonitoramento();
                case 5 -> menuPainel();
                case 6 -> menuIncidentes();
                case 7 -> menuRelatorios();
                case 0 -> System.out.println("Encerrando. Ate logo!");
                default -> System.out.println("Opcao invalida.");
            }
        } while (opcao != 0);
    }

    // ------------------------------------------------------------------ Transformadores
    private void menuTransformadores() {
        int op;
        do {
            System.out.println("\n--- Transformadores ---");
            System.out.println("1 - Cadastrar | 2 - Listar | 3 - Buscar por ID | 4 - Atualizar | 5 - Excluir | 0 - Voltar");
            op = Entrada.inteiro("Opcao: ");
            switch (op) {
                case 1 -> seguro(this::cadastrarTransformador);
                case 2 -> seguro(this::listarTransformadores);
                case 3 -> seguro(() -> System.out.println(
                        detalhe(transformadorService.buscar(Entrada.inteiro("ID: ")))));
                case 4 -> seguro(this::atualizarTransformador);
                case 5 -> seguro(this::excluirTransformador);
                case 0 -> { }
                default -> System.out.println("Opcao invalida.");
            }
        } while (op != 0);
    }

    private void cadastrarTransformador() throws SQLException {
        String identificador = Entrada.texto("Identificador unico (ex.: TR-0004): ");
        double lat = Entrada.decimal("Latitude: ");
        double lon = Entrada.decimal("Longitude: ");
        String bairro = Entrada.texto("Bairro: ");
        double capacidade = Entrada.decimal("Capacidade nominal (kVA): ");
        LocalDate manutencao = Entrada.dataOpcional("Data da ultima manutencao (dd/MM/aaaa, ENTER = nenhuma): ");
        Transformador t = transformadorService.cadastrar(lat, lon, bairro, identificador, capacidade, manutencao);
        System.out.println("Transformador cadastrado com ID " + t.getId() + ".");
    }

    private void listarTransformadores() throws SQLException {
        List<Transformador> lista = transformadorService.listar();
        if (lista.isEmpty()) {
            System.out.println("Nenhum transformador cadastrado.");
        }
        lista.forEach(t -> System.out.println(detalhe(t)));
    }

    private void atualizarTransformador() throws SQLException {
        Transformador t = transformadorService.buscar(Entrada.inteiro("ID do transformador: "));
        System.out.println("Atual: " + detalhe(t));
        System.out.println("(ENTER mantem o valor atual)");
        String ident = Entrada.textoOpcional("Identificador [" + t.getIdentificadorUnico() + "]: ");
        String bairro = Entrada.textoOpcional("Bairro [" + t.getBairro() + "]: ");
        Double lat = Entrada.decimalOpcional("Latitude [" + t.getLatitude() + "]: ");
        Double lon = Entrada.decimalOpcional("Longitude [" + t.getLongitude() + "]: ");
        Double cap = Entrada.decimalOpcional("Capacidade kVA [" + t.getCapacidadeNominal() + "]: ");
        LocalDate manut = Entrada.dataOpcional("Ultima manutencao [" + Entrada.formatarData(t.getDataUltimaManutencao()) + "]: ");
        if (ident != null) t.setIdentificadorUnico(ident);
        if (bairro != null) t.setBairro(bairro);
        if (lat != null) t.setLatitude(lat);
        if (lon != null) t.setLongitude(lon);
        if (cap != null) t.setCapacidadeNominal(cap);
        if (manut != null) t.setDataUltimaManutencao(manut);
        t.setAtivo(Entrada.confirmar("Transformador ativo?"));
        transformadorService.atualizar(t);
        System.out.println("Transformador atualizado.");
    }

    private void excluirTransformador() throws SQLException {
        int id = Entrada.inteiro("ID do transformador: ");
        if (Entrada.confirmar("Confirma a exclusao do transformador " + id + "?")) {
            transformadorService.excluir(id);
            System.out.println("Transformador excluido.");
        }
    }

    // ------------------------------------------------------------------ Postes
    private void menuPostes() {
        int op;
        do {
            System.out.println("\n--- Postes ---");
            System.out.println("1 - Cadastrar | 2 - Listar | 3 - Atualizar | 4 - Excluir | 0 - Voltar");
            op = Entrada.inteiro("Opcao: ");
            switch (op) {
                case 1 -> seguro(() -> {
                    String tipo = Entrada.texto("Tipo do poste (ex.: Concreto Duplo T): ");
                    double altura = Entrada.decimal("Altura (m): ");
                    double lat = Entrada.decimal("Latitude: ");
                    double lon = Entrada.decimal("Longitude: ");
                    String bairro = Entrada.texto("Bairro: ");
                    Poste p = posteService.cadastrar(lat, lon, bairro, tipo, altura);
                    System.out.println("Poste cadastrado com ID " + p.getId() + ".");
                });
                case 2 -> seguro(() -> {
                    List<Poste> lista = posteService.listar();
                    if (lista.isEmpty()) {
                        System.out.println("Nenhum poste cadastrado.");
                    }
                    lista.forEach(p -> System.out.println(detalhe(p)));
                });
                case 3 -> seguro(() -> {
                    Poste p = posteService.buscar(Entrada.inteiro("ID do poste: "));
                    System.out.println("Atual: " + detalhe(p));
                    String tipo = Entrada.textoOpcional("Tipo [" + p.getTipoPoste() + "]: ");
                    Double altura = Entrada.decimalOpcional("Altura [" + p.getAltura() + "]: ");
                    String bairro = Entrada.textoOpcional("Bairro [" + p.getBairro() + "]: ");
                    if (tipo != null) p.setTipoPoste(tipo);
                    if (altura != null) p.setAltura(altura);
                    if (bairro != null) p.setBairro(bairro);
                    p.setAtivo(Entrada.confirmar("Poste ativo?"));
                    posteService.atualizar(p);
                    System.out.println("Poste atualizado.");
                });
                case 4 -> seguro(() -> {
                    int id = Entrada.inteiro("ID do poste: ");
                    if (Entrada.confirmar("Confirma a exclusao do poste " + id + "?")) {
                        posteService.excluir(id);
                        System.out.println("Poste excluido.");
                    }
                });
                case 0 -> { }
                default -> System.out.println("Opcao invalida.");
            }
        } while (op != 0);
    }

    // ------------------------------------------------------------------ Sensores
    private void menuSensores() {
        int op;
        do {
            System.out.println("\n--- Sensores ---");
            System.out.println("1 - Cadastrar | 2 - Listar | 3 - Ativar/Desativar | 4 - Excluir | 0 - Voltar");
            op = Entrada.inteiro("Opcao: ");
            switch (op) {
                case 1 -> seguro(() -> {
                    String ident = Entrada.texto("Identificador do dispositivo (ex.: SN-TR-0004): ");
                    int equipamentoId = Entrada.inteiro("ID do equipamento (poste/transformador): ");
                    LocalDate data = Entrada.dataOpcional("Data de instalacao (dd/MM/aaaa, ENTER = nenhuma): ");
                    Sensor s = sensorService.cadastrar(ident, equipamentoId, data);
                    System.out.println("Sensor cadastrado com ID " + s.getId() + ".");
                });
                case 2 -> seguro(() -> sensorService.listar().forEach(s -> System.out.printf(
                        "#%d | %s | %s | %s%n", s.getId(), s.getIdentificadorDispositivo(),
                        s.getEquipamento().descrever(), s.isAtivo() ? "ativo" : "inativo")));
                case 3 -> seguro(() -> {
                    int id = Entrada.inteiro("ID do sensor: ");
                    sensorService.alterarAtivo(id, Entrada.confirmar("Sensor ativo?"));
                    System.out.println("Sensor atualizado.");
                });
                case 4 -> seguro(() -> {
                    int id = Entrada.inteiro("ID do sensor: ");
                    if (Entrada.confirmar("Confirma a exclusao do sensor " + id + " e suas leituras?")) {
                        sensorService.excluir(id);
                        System.out.println("Sensor excluido.");
                    }
                });
                case 0 -> { }
                default -> System.out.println("Opcao invalida.");
            }
        } while (op != 0);
    }

    // ------------------------------------------------------------------ Monitoramento
    private void menuMonitoramento() {
        int op;
        do {
            System.out.println("\n--- Monitoramento ---");
            System.out.println("1 - Enviar leitura manual | 2 - Simulador IoT (1 rodada) | 3 - Simulador IoT (varias rodadas) | 0 - Voltar");
            op = Entrada.inteiro("Opcao: ");
            switch (op) {
                case 1 -> seguro(() -> {
                    Sensor s = sensorService.buscar(Entrada.inteiro("ID do sensor: "));
                    double temp = Entrada.decimal("Temperatura (C): ");
                    double consumo = Entrada.decimal("Consumo instantaneo (kVA): ");
                    boolean energia = Entrada.confirmar("Ha energia no ponto monitorado?");
                    List<String> eventos = monitoramentoService.processarLeitura(s, temp, consumo, energia);
                    System.out.println("Leitura armazenada. Eventos gerados: " + eventos.size());
                    eventos.forEach(e -> System.out.println(" - " + e));
                });
                case 2 -> seguro(() -> System.out.println("Eventos gerados: " + simulador.executarRodada()));
                case 3 -> seguro(() -> {
                    int rodadas = Entrada.inteiro("Quantas rodadas? ");
                    int total = 0;
                    for (int i = 1; i <= rodadas; i++) {
                        System.out.println("-- Rodada " + i + " --");
                        total += simulador.executarRodada();
                    }
                    System.out.println("Total de eventos gerados: " + total);
                });
                case 0 -> { }
                default -> System.out.println("Opcao invalida.");
            }
        } while (op != 0);
    }

    // ------------------------------------------------------------------ Painel
    private void menuPainel() {
        int op;
        do {
            seguro(() -> {
                PainelOperacao painel = painelService.montar(operador);
                painel.atualizarDashboard();
                painel.exibirAlertaCritico();
            });
            System.out.println("\n1 - Analisar alerta | 2 - Atualizar painel | 0 - Voltar");
            op = Entrada.inteiro("Opcao: ");
            if (op == 1) {
                seguro(() -> {
                    Alerta a = alertaService.analisar(operador, Entrada.inteiro("ID do alerta: "));
                    System.out.println("Alerta em analise: " + a.formatar());
                });
            }
        } while (op != 0);
    }

    // ------------------------------------------------------------------ Incidentes
    private void menuIncidentes() {
        int op;
        do {
            System.out.println("\n--- Incidentes ---");
            System.out.println("1 - Listar abertos | 2 - Listar todos | 3 - Registrar queda manual | "
                    + "4 - Despachar equipe | 5 - Marcar como resolvido | 0 - Voltar");
            op = Entrada.inteiro("Opcao: ");
            switch (op) {
                case 1 -> seguro(() -> listarIncidentes(incidenteService.listarAbertos()));
                case 2 -> seguro(() -> listarIncidentes(incidenteService.listarTodos()));
                case 3 -> seguro(() -> {
                    double lat = Entrada.decimal("Latitude do local sem energia: ");
                    double lon = Entrada.decimal("Longitude do local sem energia: ");
                    String desc = Entrada.texto("Descricao: ");
                    incidenteService.registrarQuedaEnergia(lat, lon, desc).ifPresentOrElse(
                            i -> System.out.println("Incidente registrado: " + i.formatar()),
                            () -> System.out.println("Ja existe incidente aberto para o transformador mais proximo."));
                });
                case 4 -> seguro(() -> System.out.println("Equipe despachada: "
                        + incidenteService.despacharEquipe(operador, Entrada.inteiro("ID do incidente: ")).formatar()));
                case 5 -> seguro(() -> System.out.println("Incidente resolvido: "
                        + incidenteService.resolver(Entrada.inteiro("ID do incidente: ")).formatar()));
                case 0 -> { }
                default -> System.out.println("Opcao invalida.");
            }
        } while (op != 0);
    }

    private void listarIncidentes(List<Incidente> lista) {
        if (lista.isEmpty()) {
            System.out.println("Nenhum incidente encontrado.");
        }
        lista.forEach(i -> System.out.println(i.formatar()));
    }

    // ------------------------------------------------------------------ Relatorios
    private void menuRelatorios() {
        seguro(() -> {
            System.out.println("\n--- Relatorio de consumo excessivo ---");
            System.out.println("Bairros cadastrados: " + String.join(", ", relatorioService.listarBairros()));
            String bairro = Entrada.textoOpcional("Bairro (ENTER = todos): ");
            LocalDate inicio = Entrada.data("Data inicial (dd/MM/aaaa): ");
            LocalDate fim = Entrada.data("Data final (dd/MM/aaaa): ");
            Relatorio relatorio = relatorioService.gerarConsumoExcessivo(bairro, inicio, fim);
            System.out.println();
            System.out.println(relatorio.getConteudoTexto());
            if (Entrada.confirmar("\nExportar para PDF?")) {
                Path destino = Path.of("relatorios", "consumo-excessivo-" + relatorio.getId() + ".pdf");
                relatorioService.exportarPDF(relatorio, destino);
                System.out.println("PDF gerado em: " + destino.toAbsolutePath());
            }
        });
    }

    // ------------------------------------------------------------------ Utilitarios
    private String detalhe(Equipamento e) {
        String base = "#" + e.getId() + " | " + e.descrever() + " | lat/lon: " + e.getLatitude() + ", " + e.getLongitude()
                + " | " + (e.isAtivo() ? "ativo" : "INATIVO");
        if (e instanceof Transformador t) {
            base += " | ultima manutencao: " + Entrada.formatarData(t.getDataUltimaManutencao());
        }
        return base;
    }

    /** Executa a acao exibindo mensagens amigaveis em vez de derrubar o programa. */
    private void seguro(Acao acao) {
        try {
            acao.executar();
        } catch (SQLException e) {
            System.out.println("Erro de banco de dados: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Erro de arquivo: " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Aviso: " + e.getMessage());
        }
    }
}
