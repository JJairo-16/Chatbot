import java.io.IOException;

// * Consola
import java.util.Scanner;
import utils.Formatter;
import utils.fttComplements.Pretty;

// * Chat
import utils.ConfigsUtils.CommandExecuter;
import services.OpenAIChatService;

// * Configuración (json)
import chatgpt.org.json.JSONObject;
import services.ConfigLoader;
import services.SvcLoader.*;

public class App {
    private static final String USER_CONFIG_PATH = "chatgpt/data/userConfig.json";
    
    // * Objetos de chat
    OpenAIChatService svc;
    CommandExecuter comex;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    public void run() {
        // * Declaración de variables
        String prompt;
        String answer;
        boolean running = true;

        // * Ftt
        Formatter ftt = new Formatter();
        ftt.cleanConsole();

        // * Cargar configuración del usuario
        JSONObject userConfig;
        try {
            userConfig = ConfigLoader.initUser(USER_CONFIG_PATH);
        } catch (IOException e) {
            Pretty.error("Ha habido un error al cargar la configuración. Por favor, inténtelo más tarde.");
            return;
        }

        // * Cargar api
        if (!UserLoader.apiKeyExists(userConfig)) {
            return;
        }
        String apiKey = userConfig.getString("apiKey");

        // * Montar servicio del chat
        try {
            svc  = OpenAIChatService.newSvc(apiKey);
        } catch (IllegalStateException e) {
            Pretty.error("La API proporcionada no es válida.");
            return;
        }
        
        comex = new CommandExecuter();
        
        // Configurar chat
        UserLoader userLoader = new UserLoader(userConfig);
        userLoader.loadConfig();

        // * Crear scanner
        Scanner scanner = new Scanner(System.in);

        while (running) {
            // * Obtener prompt
            System.out.print("user: ");
            prompt = scanner.nextLine();

            if (prompt.isBlank()) {
                Pretty.warn("El pompt no puede estar vacío.");
                continue;
            }

            // * Gestionar prompt
            prompt = prompt.trim();
            if (prompt.equalsIgnoreCase("/exit")) {
                running = false;
            
            } else if (prompt.startsWith("/")) {
                comex.processCommand(prompt);
            
            } else {
                try {
                    answer = svc.chat(prompt);
                    System.out.printf("%nChatGPT:%n%s%n%n", answer);

                } catch (java.net.ConnectException e) {
                    Pretty.warn("Ha ocurrido un error en la conexión. Por favor, inténtelo más tarde.");

                } catch (java.io.IOException e) {
                    Pretty.warn("Error de entrada/salida. Verifique su conexión o inténtelo nuevamente.");

                } catch (InterruptedException e) {
                    Pretty.warn("La operación fue interrumpida. Por favor, inténtelo de nuevo.");
                    Thread.currentThread().interrupt(); // ? buena práctica: volver a marcar el hilo como interrumpido

                } catch (Exception e) {
                    Pretty.warn("Ha ocurrido un error desconocido.");
                }
            }
        }

        // * Cerrar scanner
        scanner.close();
    }
}