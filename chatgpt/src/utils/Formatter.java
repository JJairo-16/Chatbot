package utils;

public class Formatter {
    private ProcessBuilder cls;
    private static final int AUX_CLS = 50;

    // #region Limpiar consola
    private void mountCleaner() {
        // ? Utilizar cmd?
        boolean windows = System.getProperty("os.name").toLowerCase().contains("windows");

        // Elegir método
        if (windows) {
            this.cls = new ProcessBuilder("cmd", "/c", "cls").inheritIO();
        } else {
            this.cls = new ProcessBuilder("clear").inheritIO();
        }
    }

    public void cleanConsole() {
        // Montar cls
        if (this.cls == null) {
            mountCleaner();
        }

        try {
            cls.start().waitFor();

        } catch (Exception e) {
            // Limpiar por auxiliar
            System.out.println("\n".repeat(AUX_CLS - 1));
        }
    }

    // #endregion
}

