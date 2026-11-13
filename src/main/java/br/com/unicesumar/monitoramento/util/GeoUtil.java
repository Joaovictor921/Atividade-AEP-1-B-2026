package br.com.unicesumar.monitoramento.util;

/** Calculos geograficos (RF04: transformador mais proximo da queda). */
public final class GeoUtil {

    private static final double RAIO_TERRA_KM = 6371.0088;

    private GeoUtil() {
    }

    /** Distancia entre dois pontos (formula de Haversine), em km. */
    public static double distanciaKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return RAIO_TERRA_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
