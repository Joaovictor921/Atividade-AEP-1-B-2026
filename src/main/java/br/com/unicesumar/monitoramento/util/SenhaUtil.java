package br.com.unicesumar.monitoramento.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Hash de senhas com PBKDF2 (formato salvo: iteracoes:salt:hash, em Base64). */
public final class SenhaUtil {

    private static final int ITERACOES = 120_000;
    private static final int TAMANHO_HASH = 256;

    private SenhaUtil() {
    }

    public static String gerarHash(String senha) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = pbkdf2(senha, salt, ITERACOES);
        return ITERACOES + ":" + Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verificar(String senhaInformada, String senhaArmazenada) {
        if (senhaInformada == null || senhaArmazenada == null) {
            return false;
        }
        String[] partes = senhaArmazenada.split(":");
        if (partes.length != 3) {
            return false;
        }
        int iteracoes = Integer.parseInt(partes[0]);
        byte[] salt = Base64.getDecoder().decode(partes[1]);
        byte[] esperado = Base64.getDecoder().decode(partes[2]);
        byte[] calculado = pbkdf2(senhaInformada, salt, iteracoes);
        return MessageDigest.isEqual(esperado, calculado);
    }

    private static byte[] pbkdf2(String senha, byte[] salt, int iteracoes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), salt, iteracoes, TAMANHO_HASH);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Algoritmo de hash indisponivel", e);
        }
    }
}
