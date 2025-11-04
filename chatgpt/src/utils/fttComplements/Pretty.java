package utils.fttComplements;

import static utils.fttComplements.Ansi.*;

public class Pretty {
    private Pretty() {}

    private static final String ERR_ICO = BOLD + BRIGHT_RED + "[ERR]" + RESET;
    private static final String WARN_ICO = BOLD + ORANGE + "[WARN]" + RESET;
    private static final String INFO_ICO = BOLD + CYAN + "[i]" + RESET;

    /**
     * Muestra un mensaje con icono y margen.
     */
    private static void log(String ico, String message, boolean padding) {
        String pad = padding ? "\n" : "";
        System.out.println(pad + ico + " " + message + pad);
    }

    // #region Tipos de logs
    // * error
    /**
     * Muestra mensaje de error.
     */
    public static void error(String message, boolean padding) {
        log(ERR_ICO, message, padding);
    }

    /**
     * Muestra mensaje de error.
     */
    public static void error(String message) {
        error(message, true);
    }

    // * warn
    /**
     * Muestra mensaje de advertencia.
     */
    public static void warn(String message, boolean padding) {
        log(WARN_ICO, message, padding);
    }

    /**
     * Muestra mensaje de advertencia.
     */
    public static void warn(String message) {
        warn(message, true);
    }

    // * info
    /**
     * Muestra mensaje informativo.
     */
    public static void info(String message, boolean padding) {
        log(INFO_ICO, message, padding);
    }

    /**
     * Muestra mensaje informativo.
     */
    public static void info(String message) {
        info(message, true);
    }

    // #endregion
}
