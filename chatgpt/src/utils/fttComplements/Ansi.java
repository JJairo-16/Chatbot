package utils.fttComplements;

public class Ansi {
    private Ansi() {}

    // * BASE
    private static final String ESC = "\u001b[";

    // * Formato
    public static final String RESET = ESC + "0m";
    public static final String BOLD = ESC + "1m";

    // * Colores
    public static final String BLACK = ESC + "30m";
    public static final String RED = ESC + "31m";
    public static final String GREEN = ESC + "32m";
    public static final String YELLOW = ESC + "33m";
    public static final String BLUE = ESC + "34m";
    public static final String MAGENTA = ESC + "35m";
    public static final String CYAN = ESC + "36m";
    public static final String WHITE = ESC + "37m";

    public static final String ORANGE = ESC + "38;5;208m";

    // * Colores brillantes
    public static final String BRIGHT_BLACK = ESC + "90m";
    public static final String BRIGHT_RED = ESC + "91m";
    public static final String BRIGHT_GREEN = ESC + "92m";
    public static final String BRIGHT_YELLOW = ESC + "93m";
    public static final String BRIGHT_BLUE = ESC + "94m";
    public static final String BRIGHT_MAGENTA = ESC + "95m";
    public static final String BRIGHT_CYAN = ESC + "96m";
    public static final String BRIGHT_WHITE = ESC + "97m";
}
