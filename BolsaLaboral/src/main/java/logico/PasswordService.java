package logico;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PBKDF2 con salt independiente por usuario. No agrega dependencias y funciona
 * en Java 8.
 */
public final class PasswordService {

    public static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    public static final int ITERACIONES = 120000;
    private static final int BITS_HASH = 256;
    private static final int BYTES_SALT = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordService() {
    }

    public static Credencial crear(char[] password) {
        validarPassword(password);
        byte[] salt = new byte[BYTES_SALT];
        RANDOM.nextBytes(salt);
        return new Credencial(salt, derivar(password, salt, ITERACIONES), ITERACIONES, ALGORITMO);
    }

    public static boolean verificar(char[] password, byte[] salt, byte[] hash, int iteraciones,
            String algoritmo) {
        if (password == null || salt == null || hash == null || salt.length == 0 || hash.length == 0) {
            return false;
        }
        int iteracionesEfectivas = iteraciones > 0 ? iteraciones : ITERACIONES;
        String algoritmoEfectivo = algoritmo == null || algoritmo.trim().isEmpty()
                ? ALGORITMO : algoritmo;
        byte[] calculado = derivar(password, salt, iteracionesEfectivas, algoritmoEfectivo,
                hash.length * 8);
        try {
            return MessageDigest.isEqual(hash, calculado);
        } finally {
            Arrays.fill(calculado, (byte) 0);
        }
    }

    private static byte[] derivar(char[] password, byte[] salt, int iteraciones) {
        return derivar(password, salt, iteraciones, ALGORITMO, BITS_HASH);
    }

    private static byte[] derivar(char[] password, byte[] salt, int iteraciones,
            String algoritmo, int bits) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iteraciones, bits);
        try {
            return SecretKeyFactory.getInstance(algoritmo).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("No fue posible proteger la contraseña con " + algoritmo + ".",
                    exception);
        } finally {
            spec.clearPassword();
        }
    }

    private static void validarPassword(char[] password) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
    }

    public static final class Credencial {
        private final byte[] salt;
        private final byte[] hash;
        private final int iteraciones;
        private final String algoritmo;

        private Credencial(byte[] salt, byte[] hash, int iteraciones, String algoritmo) {
            this.salt = salt.clone();
            this.hash = hash.clone();
            this.iteraciones = iteraciones;
            this.algoritmo = algoritmo;
        }

        public byte[] getSalt() {
            return salt.clone();
        }

        public byte[] getHash() {
            return hash.clone();
        }

        public int getIteraciones() {
            return iteraciones;
        }

        public String getAlgoritmo() {
            return algoritmo;
        }
    }
}
