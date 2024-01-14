package net.antireach.r1ver;

public class Utils {

    /**
     * Arredonda um valor double para o número especificado de casas decimais.
     *
     * @param value     O valor a ser arredondado.
     * @param precision O número de casas decimais a serem mantidas.
     * @return O valor arredondado.
     * @throws IllegalArgumentException Se a precisão for negativa.
     */
    public static double cut(double value, int precision) {
        if (precision < 0) {
            throw new IllegalArgumentException("A precisão não pode ser negativa.");
        }

        double scale = Math.pow(10, precision);
        return Math.round(value * scale) / scale;
    }
}
