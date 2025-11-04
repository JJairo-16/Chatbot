package utils.ConfigsUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import services.OpenAIChatService;

import java.util.Scanner;
import utils.Formatter;

import utils.fttComplements.Pretty;
import utils.fttComplements.Ansi;

public class CommandExecuter {
    // #region comex
    private OpenAIChatService svc;
    private Formatter ftt;

    private Scanner scanner;

    public CommandExecuter() {
        svc = OpenAIChatService.newSvc();
        currentModel = svc.getModel();

        ftt = new Formatter();
        scanner = new Scanner(System.in);
    }

    // #endregion

    // #region Reglas
    // * Configs
    private static final Map<String, String> ALIAS_MAP = Map.of(
            "temp", "temperature",
            "his", "history");

    // * Modelos
    private String currentModel;
    private static final List<String> MODELS = List.of("gpt-3.5-turbo");

    public static List<String> getModels() {
        return MODELS;
    }

    // * Rol
    private static final int MAX_ROLE_LEN = 250;

    public static int getRoleMaxLen() {
        return MAX_ROLE_LEN;
    }

    // * Temperatura
    private static final double MIN_TEMP = OpenAIChatService.MIN_TEMPERATURE;
    private static final double MAX_TEMP = OpenAIChatService.MAX_TEMPERATURE;

    public static boolean validateTemp(double temp) {
        return (temp >= MIN_TEMP && temp <= MAX_TEMP);
    }

    // #endregion

    /**
     * Traducir alias.
     */
    private String getAlias(String alias) {
        if (ALIAS_MAP.containsKey(alias)) { // ? Devolver valor real
            return ALIAS_MAP.get(alias);
        }

        return alias;
    }

    /**
     * Procesar comando (redirigir responsabilidad).
     */
    public void processCommand(String command) {
        // Parsear comando
        String commandSqrt = command.split("\\s+")[0];
        commandSqrt = commandSqrt.substring(1);
        commandSqrt = getAlias(commandSqrt);

        String input;
        String confirmString;

        switch (commandSqrt) {
            case "new": // ? Reiniciar chat
                confirmString = Pretty.getSpecialValueFormat("NEW");
                System.out.printf("%nIntroduzca \"%s\" para confirmar: ", confirmString);
                input = scanner.nextLine().trim();

                if (input.equals("NEW")) {
                    resetChat();
                    break;
                }

                System.out.println();
                break;

            case "config": // ? Configurar parámetro
                String cleanCommand = command.trim();
                if (cleanCommand.length() <= 7) {
                    String helpString = Pretty.getSpecialValueFormat("/help");
                    String msg = String.format("Debe introducir un parámetro. Utilice \"%s\" para visualizarlos.",
                            helpString);
                    Pretty.warn(msg);
                    break;
                }

                String param = command.substring(commandSqrt.length() + 2).trim(); // ? Parámetro a configurar
                config(param);
                break;

            case "help": // ? Obtener texto de ayuda
                System.out.println();
                showHelp();
                System.out.println();
                break;

            case "clean": // ? Limpiar chat (con confirmación)
                confirmString = Pretty.getSpecialValueFormat("CLEAN");
                System.out.printf("%nIntroduzca \"%s\" para confirmar: ", confirmString);
                input = scanner.nextLine().trim();

                if (input.equals("CLEAN")) {
                    ftt.cleanConsole();
                    break;
                }

                System.out.println();
                break;

            case "history": // ? Guardar historial
                System.out.println();

                String dir = getDir();

                if (dir.equalsIgnoreCase("cancel")) {
                    Pretty.info("Se ha cancelado la operación.");
                    break;
                }

                try {
                    saveHistoryToFile(dir, "history.txt");
                } catch (Exception e) {
                    Pretty.warn("Ha ocurrido un error desconocido.");
                }
                break;

            default: // ? Comando no detectado
                Pretty.warn("El comando introducido no se encuentra disponible o no existe.");
                break;
        }
    }

    // #region new
    private void resetChat() {
        svc.resetHistory();
        ftt.cleanConsole();
    }

    // #endregion

    // #region config
    /**
     * Configurar parámetro.
     */
    public void config(String param) {
        param = getAlias(param); // ? Sacar alias

        switch (param) {
            case "model":
                String msg;

                if (MODELS.size() < 2) { // ? No hay más modelos disponibles
                    String modelString = MODELS.get(0);
                    msg = String.format("Solo hay un modelo disponible: %s.", Pretty.getGreenBold(modelString));
                    Pretty.info(msg);
                    break;
                }

                String currentModelString = Pretty.getGreenBold(currentModel);
                msg = String.format("Modelo actual: %s", currentModelString);
                Pretty.info(msg);

                // Obtener modelo del usuario
                String model = getModel();
                svc.setModel(model);
                currentModel = model;

                String modelString = Pretty.getGreenBold(model);
                msg = String.format("Modelo seleccionado: %s", modelString);
                Pretty.info(msg);

                break;

            case "role":
                // Obtener rol del usuario
                String role = getRole();

                if (role.equals("CANCEL")) {
                    Pretty.info("El rol no ha sido actualizado.");
                } else {
                    svc.setSystemRole(role);
                    Pretty.info("El rol ha sido actualizado correctamente.");
                }

                break;

            case "temperature":
                System.out.println();

                // Obtener temperatura del usuario
                double temp = getTemperature();

                if (temp == -1) { // ? Cancelar
                    Pretty.info("La temperatura no ha sido actualizada.");
                } else {
                    svc.setTemperature(temp);
                    Pretty.info("La temperatura ha sido actualizada correctamente.");
                }

                break;
        }
    }

    // #region Modelo
    /**
     * Obtener modelo mediante un menú.
     */
    private String getModel() {
        int max = MODELS.size();
        int option = 0;

        // No hay más modelos disponibles
        if (max < 2) {
            return MODELS.get(0);
        }

        // Mostrar menú
        showModels();
        System.out.println();

        // Obtener opción
        option = getOption(0, max);

        if (option == 0) { // ? Cancelar
            return currentModel;
        }

        return MODELS.get(option - 1);
    }

    /**
     * Mostrar menú de modelos.
     */
    private void showModels() {
        System.out.println("Modelos disponibles:");
        System.out.println("0. Cancelar");

        for (int i = 0; i < MODELS.size(); i++) {
            String model = MODELS.get(i);
            int number = i + 1;
            System.out.printf("%d. %s%n", number, model);
        }
    }

    private int getOption(int min, int max) {
        String input;
        int option = 0;

        while (true) {
            String cancelString = Pretty.getSpecialValueFormat(min);
            System.out.printf("Introduzca el modelo deseado (%s para cancelar): ", cancelString);
            input = scanner.nextLine();

            if (input.isBlank()) {
                Pretty.warn("Debe seleccionar un modelo. Por favor, vuelva a intentarlo:");
                continue;
            }

            try {
                option = Integer.parseInt(input);

                if (option >= min && option <= max) {
                    return option;
                }

                String msg = String.format(
                        "La opción elegida está fuera del rango permitido (%d-%d). Por favor, vuelva a intentarlo:",
                        min, max);
                Pretty.warn(msg);

            } catch (NumberFormatException e) {
                Pretty.warn("La opción a elegir debe ser un número válido. Por favor, vuelva a intentarlo:");
            } catch (Exception e) {
                Pretty.warn("Algo ha salido mal. Por favor, vuelva a intentarlo:");
            }
        }
    }

    // #endregion

    // #region Rol
    private String getRole() {
        String role = "";
        String currentRole = svc.getRole();

        String msg;
        String currentRoleString = Ansi.CYAN + currentRole + Ansi.RESET;
        msg = String.format("Rol actual: '%s'", currentRoleString);
        Pretty.info(msg);

        while (true) {
            String cancelString = Pretty.getSpecialValueFormat("CANCEL");
            System.out.printf("Introduzca el nuevo rol (introduzca \"%s\" para cancelar): ", cancelString);
            role = scanner.nextLine();

            if (role.isBlank()) {
                Pretty.warn("El nuevo rol no puede estar vacío. Por favor, vuelva a intentarlo:");
                continue;
            }

            role = role.trim();
            if (role.length() <= MAX_ROLE_LEN) {
                return role;
            }

            msg = String.format(
                    "La descripción del rol es demasiado larga (longitud máxima permitida: %d caracteres). Por favor, vuelva a intentarlo:",
                    MAX_ROLE_LEN);
            Pretty.warn(msg);
        }
    }

    // #endregion

    // #region Temperatura
    private double getTemperature() {
        // * Declaración de variables
        // Input
        String input;
        double temp;

        // Información
        double currentTemp = svc.getTemperature();

        // * Mostrar información
        System.out.printf("La temperatura controla la precisión (%.1f) y variedad (%.1f).%n", MIN_TEMP, MAX_TEMP);

        double tempTemperature = Math.round(currentTemp * 10.0) / 10.0;
        String tempString = Pretty.getGreenBold(tempTemperature);
        System.out.printf("Temperatura actual: %s.%n%n", tempString);

        // * Bucle
        while (true) {
            String cancelString = Pretty.getSpecialValueFormat(-1);
            System.out.printf("Introduzca la nueva temperatura (Introduzca %s para cancelar): ", cancelString);
            input = scanner.nextLine();

            if (input.isBlank()) {
                Pretty.warn("La temperatura no puede estar en blanco. Por favor, vuelva a intentarlo:");
                continue;
            }

            input = input.trim().replace(",", ".");
            try {
                temp = Double.parseDouble(input);

                if (temp == -1) {
                    return -1;

                } else if (temp >= MIN_TEMP && temp <= MAX_TEMP) {
                    temp = Math.round(temp * 10.0) / 10.0;
                    return temp;
                }

                String msg = String.format(
                        "La temperatura introducida está fuera del rango permitido (%.1f - %.1f). Por favor, vuelva a intentarlo:",
                        MIN_TEMP, MAX_TEMP);
                Pretty.warn(msg);

            } catch (NumberFormatException e) {
                Pretty.warn("El formato de la temperatura introducida no es válido. Por favor, vuelva a intentarlo:");

            } catch (Exception e) {
                Pretty.warn("Algo ha salido mal. Por favor, vuelva a intentarlo:");
            }
        }
    }

    // #endregion

    // #endregion

    // #region History
    private void saveHistoryToFile(String dirString, String fileName) throws IOException {
        Path dir = Paths.get(dirString);
        Path file = dir.resolve(fileName);

        List<OpenAIChatService.Message> history = svc.getHistory();

        try (BufferedWriter w = Files.newBufferedWriter(
                file,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW)) {

            for (OpenAIChatService.Message m : history) {
                // Cabecera del mensaje
                w.write("[" + m.role + "]"); // roles: system/user/assistant
                w.newLine();

                // Contenido, con sangría para líneas subsiguientes
                String content = m.content == null ? "" : m.content;
                content = content.replace("\r", "");
                String[] lines = content.split("\n", -1);
                for (int i = 0; i < lines.length; i++) {
                    if (i == 0) {
                        w.write(lines[i]);
                    } else {
                        w.write("    " + lines[i]);
                    }
                    w.newLine();
                }

                // Línea en blanco entre mensajes
                w.newLine();
            }

            Pretty.info("Historial guardado en: " + file.toAbsolutePath());

        } catch (IOException e) {
            Pretty.warn("No se pudo guardar el historial: " + e.getMessage());
        }
    }

    private String getDir() {
        // * Declaración de variables
        String input;

        String cancelString = Pretty.getSpecialValueFormat("cancel");

        do {
            System.out.printf("Introduzca la ruta del directorio donde guardar el historial (\"%s\" para cancelar): ",
                    cancelString);
            input = scanner.nextLine();

            if (input.isBlank()) {
                Pretty.warn("La ruta no puede estar vacía. Por favor, vuelva a intentarlo:");
                continue;
            }

            input = input.trim();
            if (input.equalsIgnoreCase("cancel")) {
                return input;
            }

            int code = validatePath(input, "history.txt");
            if (code > 0) {
                String msg = VALIDATION_PATH_MSG_MAP.get(code);
                Pretty.warn(msg);
            } else {
                return input;
            }

        } while (true);
    }

    private static final Map<Integer, String> VALIDATION_PATH_MSG_MAP = Map.of(
            1, "El directorio introducido no existe.",
            2, "El archivo 'history.txt' ya existe en este directorio.");

    private int validatePath(String dirString, String name) {
        Path dir = Paths.get(dirString);

        // Verificar que dir existe y es un directorio
        if (!Files.isDirectory(dir)) {
            return 1;
        }

        // Construir la ruta completa del archivo
        Path filePath = dir.resolve(name);

        // Validar que el archivo NO exista
        if (Files.exists(filePath)) {
            return 2;
        }

        return 0;
    }

    // #endregion

    private void showHelp() {
        String tab = "  ";

        System.out.println("===============================================");
        System.out.println("              GUÍA DE COMANDOS DEL CHAT");
        System.out.println("===============================================");
        System.out.println();

        System.out.println("/new");
        System.out.println(tab + "Reinicia el chat actual y comienza una nueva conversación.");
        System.out.println();

        System.out.println("/config");
        System.out.println(tab + "Permite configurar los parámetros del chat:");
        System.out.println(tab + "- model:");
        System.out.println(tab + tab + "Define el modelo de IA que se utilizará.");
        System.out.println();
        System.out.println(tab + "- role:");
        System.out.println(tab + tab + "Establece el rol o personalidad que tomará la IA.");
        System.out.println();
        System.out.println(tab + "- temperature (temp):");
        System.out.println(tab + tab + "Controla el equilibrio entre precisión y creatividad.");
        System.out.println();

        System.out.println("/help");
        System.out.println(tab + "Muestra esta guía de ayuda.");
        System.out.println();

        System.out.println("/clean");
        System.out.println(tab + "Limpia la conversación del chat (sin reiniciar el contexto de la IA).");
        System.out.println();

        System.out.println("/history (his)");
        System.out.println(tab + "Guarda el historial completo del chat actual.");
        System.out.println();

        System.out.println("/exit");
        System.out.println(tab + "Cierra el programa de forma segura.");
        System.out.println();

        System.out.println("===============================================");
        System.out.println("Consejo: usa /help en cualquier momento para volver a ver esta lista.");
        System.out.println("===============================================");
    }

}
