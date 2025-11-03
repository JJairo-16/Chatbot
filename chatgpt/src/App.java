import java.io.IOException;

// * Consola
import java.util.Scanner;
import utils.Formatter;

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

        // * Cargar configuración del usuario
        JSONObject userConfig;
        try {
            userConfig = ConfigLoader.initUser(USER_CONFIG_PATH);
        } catch (IOException e) {
            System.out.println("Ha habido un error al cargar la configuración. Por favor, inténtelo más tarde.");
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
            System.out.println("La API proporcionada no es válida.");
            return;
        }
        
        comex = new CommandExecuter();
        
        // Configurar chat
        UserLoader userLoader = new UserLoader(userConfig);
        userLoader.loadConfig();

        // * Herramientas de consola
        Scanner scanner = new Scanner(System.in);
        Formatter ftt = new Formatter();
        ftt.cleanConsole();

        while (running) {
            // * Obtener prompt
            System.out.print("user: ");
            prompt = scanner.nextLine();

            if (prompt.isBlank()) {
                System.out.println("El pompt no puede estar vacío.");
                continue;
            }

            System.out.println();

            // * Gestionar prompt
            prompt = prompt.trim();
            if (prompt.equalsIgnoreCase("/exit")) {
                running = false;
            
            } else if (prompt.startsWith("/")) {
                comex.processCommand(prompt);
            
            } else {
                try {
                    answer = svc.chat(prompt);
                    System.out.printf("ChatGPT:%n%s%n%n", answer);

                } catch (java.net.ConnectException e) {
                    System.out.println("Ha ocurrido un error en la conexión. Por favor, inténtelo más tarde.\n");

                } catch (java.io.IOException e) {
                    System.out.println("Error de entrada/salida. Verifique su conexión o inténtelo nuevamente.\n");

                } catch (InterruptedException e) {
                    System.out.println("La operación fue interrumpida. Por favor, inténtelo de nuevo.\n");
                    Thread.currentThread().interrupt(); // ? buena práctica: volver a marcar el hilo como interrumpido

                } catch (Exception e) {
                    System.out.println("Ha ocurrido un error desconocido.");
                }
            }
        }

        // * Cerrar scanner
        scanner.close();
    }
}