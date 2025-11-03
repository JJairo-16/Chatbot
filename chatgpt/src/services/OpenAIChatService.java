package services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import chatgpt.org.json.JSONArray;
import chatgpt.org.json.JSONObject;

/**
 * Servicio de chat OpenAI
 * - Mantiene historial (opcional)
 * - Permite configurar modelo, rol del sistema y temperatura
 * - Llama a /v1/chat/completions con HttpClient
 *
 * Inspirado en tu implementación de consola, pero aislado como servicio reutilizable.  // Fuente original: :contentReference[oaicite:2]{index=2}
 */
public class OpenAIChatService {
    private static OpenAIChatService svc;

    // #region Declaración de variables
    // Default config
    private static final String DEF_MODEL = "gpt-3.5-turbo";
    private static final String DEF_SYSTEM_ROLE = "Ets un assistent amable i útil a consola.";
    private static final double DEF_TEMPERATURE = 0.7;

    // Config
    private String model = DEF_MODEL;
    private String systemRole = DEF_SYSTEM_ROLE;
    private double temperature = DEF_TEMPERATURE;

    // Endpoint (Chat Completions)
    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";

    // Historial (opcional). Si no quieres historial, no uses addUserMessage y llama a chatOnce(...)
    private final List<Message> history = new ArrayList<>();

    // HTTP
    private final HttpClient http;
    private final String apiKey;

    // #endregion

    // #region Defaults
    public static String getDefModel() {
        return DEF_MODEL;
    }

    public static String getDefSystemRole() {
        return DEF_SYSTEM_ROLE;
    }

    public static double getDefTemperature() {
        return DEF_TEMPERATURE;
    }

    // #endregion

    // #region Reglas
    public static final double MIN_TEMPERATURE = 0;
    public static final double MAX_TEMPERATURE = 1;

    // #endregion

    /** Estructura de mensaje simple (role/content). */
    public static class Message {
        public final String role;
        public final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    // #region Servicio
    /** Crear servicio. */
    public static OpenAIChatService newSvc(String apiKey) {
        if (svc == null) {
            svc = new OpenAIChatService(apiKey);
        }
        return svc;
    }

    /** Crear servicio. */
    public static OpenAIChatService newSvc() {
        if (svc == null) {
            return null;
        }

        return svc;
    }

    // * Constructor
    private OpenAIChatService(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Falta OPENAI_API_KEY.");
        }

        if (!apiKey.startsWith("sdk-proj-")) {
            throw new IllegalStateException("OPENAI_API_KEY no válida.");
        }

        this.apiKey = apiKey;

        // Crear conexión
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        // Mensaje del sistema por defecto
        history.add(new Message("system", systemRole));
    }

    // #endregion

    // ! ===== API pública =====
    // #region Parámetros
    public void setModel(String model) {
        if (model != null && !model.isBlank()) this.model = model;
    }

    public String getModel() {
        return this.model;
    }

    public void setSystemRole(String systemRole) {
        if (systemRole == null || systemRole.isBlank()) {
            return;
        }
        
        this.systemRole = systemRole;
        // Actualiza el primer system del historial
        OptionalInt idx = findFirstSystem();
        if (idx.isPresent()) {
            history.set(idx.getAsInt(), new Message("system", systemRole));
        } else {
            history.add(0, new Message("system", systemRole));
        }
    }

    public String getRole() {
        return this.systemRole;
    }

    public void setTemperature(double temperature) {
        if (temperature < MIN_TEMPERATURE || temperature > MAX_TEMPERATURE) {
            String msg = String.format("La temperatura debe estar entre %.1f y %.1f", MIN_TEMPERATURE, MAX_TEMPERATURE);
            throw new IllegalArgumentException(msg);
        }

        this.temperature = temperature;
    }

    public double getTemperature() {
        return this.temperature;
    }

    // #endregion

    /** Limpia historial y vuelve a insertar el rol del sistema actual. */
    public void resetHistory() {
        history.clear();
        history.add(new Message("system", systemRole));
    }

    /**
     * Añade un mensaje de usuario al historial y devuelve la respuesta del assistant, manteniendo el contexto.
     * @param userInput mensaje del usuario.
    */
    public String chat(String userInput) throws IOException, InterruptedException {
        // Añadir mensaje del usuario al historial
        history.add(new Message("user", userInput));
        
        // Añadir respuesta al historial
        String reply = callApi(buildBody(history));
        history.add(new Message("assistant", reply));
        
        return reply;
    }

    /**
     * Llama una sola vez sin tocar el historial: útil si no quieres contexto.
     * @param userInput mensaje del usuario.
     */
    public String chatOnce(String userInput) throws IOException, InterruptedException {
        List<Message> tmp = new ArrayList<>(); // ? Historial temporal
        
        // * Preparar mensaje
        tmp.add(new Message("system", systemRole));
        tmp.add(new Message("user", userInput));
        
        // * Mandar mensaje
        return callApi(buildBody(tmp));
    }

    /** Devuelve el historial actual (copia). */
    public List<Message> getHistory() {
        return new ArrayList<>(history);
    }

    // ! ===== Internos =====
    private OptionalInt findFirstSystem() {
        for (int i = 0; i < history.size(); i++) {
            if ("system".equals(history.get(i).role)) {
                return OptionalInt.of(i);
            }
        }
        return OptionalInt.empty();
    }
    
    // #region Mensaje
    /**
     * Construir mensaje (json).
     * @param msgs Lista de mensajes del usuario.
     * @return
     */
    private String buildBody(List<Message> msgs) {
        // Construir mensaje (json)
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("temperature", temperature);

        // Construir contenido (json)
        JSONArray messagesArray = new JSONArray();
        for (Message m : msgs) {
            JSONObject msgObj = new JSONObject();

            msgObj.put("role", m.role);
            msgObj.put("content", m.content);

            messagesArray.put(msgObj);
        }

        body.put("messages", messagesArray);

        return body.toString(); // convierte el JSONObject a String JSON
    }

    /**
     * Mandar mensaje a la API.
     * 
     * @param jsonBody Mensaje en json.
     * 
     * @return Respuesta del agente.
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    private String callApi(String jsonBody) throws IOException, InterruptedException {
        // Mandar mensaje
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        // Obtener respuesta
        HttpResponse<String> response =
                http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        // timeout
        int sc = response.statusCode();
        if (sc < 200 || sc >= 300) {
            throw new IOException("HTTP " + sc + " → " + response.body());
        }

        // Obtener respuesta
        String bodyString = response.body();

        // Extraer respuesta.
        JSONObject body = new JSONObject(bodyString);
        return body.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
    }

    // #endregion
}
