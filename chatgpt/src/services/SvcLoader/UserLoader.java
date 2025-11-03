package services.SvcLoader;

import java.util.List;

import chatgpt.org.json.JSONObject;
import services.OpenAIChatService;

import utils.ConfigsUtils.CommandExecuter;

public class UserLoader {
    private OpenAIChatService svc;
    private JSONObject userConfig;

    // #region Constructor
    public UserLoader(JSONObject userConfig) {
        this.svc = OpenAIChatService.newSvc();
        this.userConfig = userConfig;
    }

    public static boolean apiKeyExists(JSONObject userConfig) {
        return stringKeyExists(userConfig, "apiKey");
    }

    // #endregion

    private static boolean keyExists(JSONObject obj, String key) {
        if (!obj.has(key)) {
            return false;
        }

        return !obj.isNull(key);
    }

    /**
     * Comprobar si una llave de tipo String existe y si no es vacía.
     */
    private static boolean stringKeyExists(JSONObject obj, String key) {
        if (!keyExists(obj, key)) {
            return false;
        }

        String value = obj.optString(key, "").trim();
        return !value.isEmpty();
    }

    /**
     * Cargar parámetros al chat.
     */
    public void loadConfig() {
        if (!keyExists(userConfig, "param")) {
            return;
        }

        JSONObject param = userConfig.getJSONObject("param");

        loadModel(param);
        loadRole(param);
        loadTemperature(param);
    }

    // #region Parámetros individuales
    public void loadModel(JSONObject param) {
        if (!stringKeyExists(param, "model")) {
            return;
        }

        List<String> models = CommandExecuter.getModels();

        String model = param.getString("model").trim().toLowerCase();

        if (!models.contains(model)) {
            return;
        }

        svc.setModel(model);
    }

    public void loadRole(JSONObject param) {
        if (!stringKeyExists(param, "role")) {
            return;
        }

        int maxLen = CommandExecuter.getRoleMaxLen();
        String role = param.getString("role").trim().toLowerCase();

        if (role.length() > maxLen) {
            return;
        }

        svc.setSystemRole(role);
    }

    public void loadTemperature(JSONObject param) {
        if (!stringKeyExists(param, "temperature")) {
            return;
        }

        double temp = param.getDouble("temperature");
        if (!CommandExecuter.validateTemp(temp)) {
            return;
        }

        temp = Math.round(temp * 10.0) / 10.0;
        svc.setTemperature(temp);
    }

    // #endregion
}
