package services;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import chatgpt.org.json.JSONObject;

public class ConfigLoader {
    private ConfigLoader() {}

    // #region init
    /**
     * Lee la configuración del usuario i crea el archivo de ser necesario.
     */
    public static JSONObject initUser(String pathString) throws IOException {
        Path path = Paths.get(pathString);

        if (!Files.exists(path)) {
            initConfigFile(pathString);
        }

        return readJson(pathString);
    }

    /**
     * Crea la configuración predeterminada.
     */
    private static void initConfigFile(String pathString) throws IOException {
        // Parámetros
        String model = OpenAIChatService.getDefModel();
        String role = OpenAIChatService.getDefSystemRole();
        double temperature = OpenAIChatService.getDefTemperature();

        // Montar config (json)
        JSONObject config = new JSONObject();
        config.put("apiKey", "TU_API_KEY");

        // Montar parámetros (json) 
        JSONObject parameters = new JSONObject();
        parameters.put("model", model);
        parameters.put("role", role);
        parameters.put("temperature", temperature);

        // Meter parámetros en la config
        config.put("param", parameters);

        // Crear dir (si no existe)
        Path path = Paths.get(pathString);
        Files.createDirectories(path.getParent());

        // Crear archivo de config
        Files.writeString(path, config.toString(4));
    }

    // #endregion

    public static JSONObject readJson(String path) throws IOException {
        Path fullPath = Paths.get(path);
        String content = new String(Files.readAllBytes(fullPath));

        return new JSONObject(content);
    }

    /** Comprobar si una llave existe y si no es nula. */
    public static boolean keyExists(JSONObject obj, String key) {
        if (!obj.has(key)) {
            return false;
        }

        return !obj.isNull(key);
    }
    
}
