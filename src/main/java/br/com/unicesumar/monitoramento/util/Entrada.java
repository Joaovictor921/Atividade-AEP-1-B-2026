package br.com.unicesumar.monitoramento.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;

/** Leitura validada de dados no console. */
public final class Entrada {

    private static final Scanner SC = new Scanner(System.in);
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private Entrada() {
    }

    public static String texto(String pergunta) {
        while (true) {
            System.out.print(pergunta);
            String s = SC.nextLine().trim();
            if (!s.isEmpty()) {
                return s;
            }
            System.out.println("Valor obrigatorio.");
        }
    }

    /** Retorna null se o usuario apenas apertar ENTER. */
    public static String textoOpcional(String pergunta) {
        System.out.print(pergunta);
        String s = SC.nextLine().trim();
        return s.isEmpty() ? null : s;
    }

    public static int inteiro(String pergunta) {
        while (true) {
            try {
                return Integer.parseInt(texto(pergunta));
            } catch (NumberFormatException e) {
                System.out.println("Digite um numero inteiro valido.");
            }
        }
    }

    public static double decimal(String pergunta) {
        while (true) {
            Double v = converterDecimal(texto(pergunta));
            if (v != null) {
                return v;
            }
            System.out.println("Digite um numero valido (ex.: 12.5).");
        }
    }

    public static Double decimalOpcional(String pergunta) {
        while (true) {
            String s = textoOpcional(pergunta);
            if (s == null) {
                return null;
            }
            Double v = converterDecimal(s);
            if (v != null) {
                return v;
            }
            System.out.println("Digite um numero valido (ex.: 12.5).");
        }
    }

    public static LocalDate data(String pergunta) {
        while (true) {
            LocalDate d = converterData(texto(pergunta));
            if (d != null) {
                return d;
            }
            System.out.println("Data invalida. Use o formato dd/MM/aaaa.");
        }
    }

    public static LocalDate dataOpcional(String pergunta) {
        while (true) {
            String s = textoOpcional(pergunta);
            if (s == null) {
                return null;
            }
            LocalDate d = converterData(s);
            if (d != null) {
                return d;
            }
            System.out.println("Data invalida. Use o formato dd/MM/aaaa.");
        }
    }

    public static boolean confirmar(String pergunta) {
        return texto(pergunta + " (s/n): ").equalsIgnoreCase("s");
    }

    public static String formatarData(LocalDate data) {
        return data == null ? "-" : data.format(FORMATO_DATA);
    }

    private static Double converterDecimal(String s) {
        try {
            return Double.parseDouble(s.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDate converterData(String s) {
        try {
            return LocalDate.parse(s, FORMATO_DATA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
