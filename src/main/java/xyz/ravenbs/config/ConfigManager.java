package xyz.ravenbs.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.module.setting.Setting;

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("RavenBS/ConfigManager");
    private static final String DEFAULT_CONFIG_FILE = "config.json";
    private static final String STATE_FILE = "state.json";

    private static final List<WeakReference<Runnable>> listeners = new ArrayList<>();
    private static final Map<String, Boolean> savedModuleStates = new HashMap<>();

    private static boolean loaded = false;
    private static String currentProfileName = "";

    public static boolean isLoaded() {
        return loaded;
    }

    public static String getCurrentProfileName() {
        return currentProfileName;
    }

    public static void addListener(Runnable listener) {
        if (listener == null) {
            return;
        }
        synchronized (listeners) {
            listeners.add(new WeakReference<>(listener));
        }
    }

    public static File getConfigDirectory() {
        File dir = new File(MinecraftClient.getInstance().runDirectory, "config/ravenbs");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getProfilesDirectory() {
        File dir = new File(getConfigDirectory(), "profiles");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static synchronized void bootstrap() {
        if (loaded) {
            return;
        }

        loaded = true;
        currentProfileName = readPersistedProfileName();

        if (!currentProfileName.isEmpty() && loadProfileInternal(currentProfileName, false)) {
            return;
        }

        currentProfileName = "";
        persistState();
        loadDefaultConfigInternal();
    }

    public static synchronized boolean saveProfile(String rawName) {
        String name = normalizeProfileName(rawName);
        if (name.isEmpty()) {
            return false;
        }

        boolean result = saveConfigInternal(getProfileFile(name));
        if (result) {
            notifyListeners();
        }
        return result;
    }

    public static synchronized boolean loadProfile(String rawName) {
        String name = normalizeProfileName(rawName);
        if (name.isEmpty()) {
            return false;
        }

        boolean result = loadProfileInternal(name, true);
        if (result) {
            notifyListeners();
        }
        return result;
    }

    public static synchronized boolean renameProfile(String oldRawName, String newRawName) {
        String oldName = normalizeProfileName(oldRawName);
        String newName = normalizeProfileName(newRawName);
        if (oldName.isEmpty() || newName.isEmpty() || oldName.equalsIgnoreCase(newName)) {
            return false;
        }

        File source = getProfileFile(oldName);
        File target = getProfileFile(newName);
        if (!source.exists() || target.exists()) {
            return false;
        }

        try {
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicMoveFailed) {
            try {
                Files.move(source.toPath(), target.toPath());
            } catch (IOException moveFailed) {
                LOGGER.error("Failed to rename profile {} to {}", oldName, newName, moveFailed);
                return false;
            }
        }

        File sourceBackup = getBackupFile(source);
        File targetBackup = getBackupFile(target);
        if (sourceBackup.exists()) {
            try {
                Files.move(sourceBackup.toPath(), targetBackup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOGGER.warn("Renamed profile {} but could not move its backup", oldName, e);
            }
        }

        if (oldName.equalsIgnoreCase(currentProfileName)) {
            currentProfileName = newName;
            persistState();
        }

        notifyListeners();
        return true;
    }

    public static synchronized boolean deleteProfile(String rawName) {
        String name = normalizeProfileName(rawName);
        if (name.isEmpty()) {
            return false;
        }

        File file = getProfileFile(name);
        if (!file.exists()) {
            return false;
        }

        try {
            Files.delete(file.toPath());
            Files.deleteIfExists(getBackupFile(file).toPath());
        } catch (IOException e) {
            LOGGER.error("Failed to delete profile {}", name, e);
            return false;
        }

        if (name.equalsIgnoreCase(currentProfileName)) {
            currentProfileName = "";
            persistState();
        }

        notifyListeners();
        return true;
    }

    public static synchronized List<String> listProfiles() {
        List<String> names = new ArrayList<>();
        for (File file : listProfileFiles()) {
            String fileName = file.getName();
            names.add(fileName.substring(0, fileName.length() - 5));
        }
        names.sort(String::compareToIgnoreCase);
        return names;
    }

    public static synchronized List<File> listProfileFiles() {
        File[] files = getProfilesDirectory().listFiles((dir, name) -> name.endsWith(".json"));
        List<File> result = new ArrayList<>();
        if (files == null) {
            return result;
        }
        for (File file : files) {
            result.add(file);
        }
        return result;
    }

    public static synchronized void saveActiveConfig() {
        File target = currentProfileName.isEmpty() ? getDefaultConfigFile() : getProfileFile(currentProfileName);
        saveConfigInternal(target);
        persistState();
    }

    public static synchronized void loadDefaultConfig() {
        loaded = true;
        currentProfileName = "";
        persistState();
        loadDefaultConfigInternal();
    }

    public static synchronized boolean isModuleEnabledInConfig(String moduleName) {
        return savedModuleStates.getOrDefault(moduleName, false);
    }

    public static boolean saveConfig(String name) {
        return saveProfile(name);
    }

    public static boolean deleteConfig(String name) {
        return deleteProfile(name);
    }

    public static void saveConfig() {
        saveActiveConfig();
    }

    public static boolean loadConfig(String name) {
        return loadProfile(name);
    }

    public static void loadConfig() {
        loadDefaultConfig();
    }

    private static boolean loadProfileInternal(String name, boolean persistProfileState) {
        File file = getProfileFile(name);
        if (!file.exists()) {
            return false;
        }

        if (!loadConfigInternal(file)) {
            return false;
        }

        currentProfileName = name;
        loaded = true;
        if (persistProfileState) {
            persistState();
        }
        return true;
    }

    private static boolean loadDefaultConfigInternal() {
        File file = getDefaultConfigFile();
        if (!file.exists()) {
            savedModuleStates.clear();
            for (Module module : ModuleManager.getModules()) {
                savedModuleStates.put(module.getId(), module.isEnabled());
            }
            return false;
        }

        boolean result = loadConfigInternal(file);
        if (result) {
            loaded = true;
        }
        return result;
    }

    private static boolean saveConfigInternal(File file) {
        if (MinecraftClient.getInstance().runDirectory == null) {
            return false;
        }

        JsonObject root = buildConfigJson();
        boolean saved = writeJsonAtomically(file, root);
        if (!saved) {
            LOGGER.error("Failed to save config to {}", file);
        }
        return saved;
    }

    private static JsonObject buildConfigJson() {
        JsonObject root = new JsonObject();
        for (Module module : ModuleManager.getModules()) {
            savedModuleStates.put(module.getId(), module.isEnabled());

            JsonObject moduleJson = new JsonObject();
            moduleJson.addProperty("enabled", module.isEnabled());
            moduleJson.addProperty("keycode", module.getKeycode());

            JsonObject settingsJson = new JsonObject();
            for (Setting setting : module.getSettings()) {
                JsonObject settingJson = setting.toJson();
                if (settingJson == null) {
                    continue;
                }
                for (String key : settingJson.keySet()) {
                    settingsJson.add(key, settingJson.get(key));
                }
            }

            moduleJson.add("settings", settingsJson);
            root.add(module.getId(), moduleJson);
        }
        return root;
    }

    private static boolean loadConfigInternal(File file) {
        JsonObject root = readJsonWithBackup(file);
        if (root == null) {
            return false;
        }

        JsonObject rollbackSnapshot = buildConfigJson();
        try {
            applyConfig(root);
            return true;
        } catch (Throwable loadError) {
            LOGGER.error("Failed to apply config {}; restoring previous module state", file, loadError);
            try {
                applyConfig(rollbackSnapshot);
            } catch (Throwable rollbackError) {
                LOGGER.error("Failed to restore module state after config load error", rollbackError);
            }
            return false;
        }
    }

    private static void applyConfig(JsonObject root) {
        savedModuleStates.clear();
        for (Module module : ModuleManager.getModules()) {
            JsonObject moduleJson = getModuleJson(root, module);
            if (moduleJson == null) {
                savedModuleStates.put(module.getId(), false);
                module.setEnabled(false);
                continue;
            }

            boolean enabled = moduleJson.has("enabled") && moduleJson.get("enabled").getAsBoolean();
            savedModuleStates.put(module.getId(), enabled);
            module.setEnabled(enabled);

            if (moduleJson.has("keycode")) {
                module.setBind(moduleJson.get("keycode").getAsInt());
            }

            if (moduleJson.has("settings") && moduleJson.get("settings").isJsonObject()) {
                JsonObject settingsJson = moduleJson.getAsJsonObject("settings");
                for (Setting setting : module.getSettings()) {
                    setting.loadProfile(settingsJson);
                }
            }
        }
    }

    private static JsonObject readJsonWithBackup(File file) {
        try {
            return readJsonObject(file);
        } catch (Exception primaryError) {
            File backup = getBackupFile(file);
            if (!backup.exists()) {
                LOGGER.error("Failed to read JSON file {} and no backup is available", file, primaryError);
                return null;
            }

            LOGGER.warn("Failed to read JSON file {}; recovering from {}", file, backup, primaryError);
            try {
                return readJsonObject(backup);
            } catch (Exception backupError) {
                LOGGER.error("Failed to read JSON backup {}", backup, backupError);
                return null;
            }
        }
    }

    private static JsonObject readJsonObject(File file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            if (!jsonElement.isJsonObject()) {
                throw new IOException("Expected a JSON object in " + file);
            }
            return jsonElement.getAsJsonObject();
        }
    }

    private static boolean writeJsonAtomically(File file, JsonObject data) {
        File parent = file.getParentFile();
        if (parent == null || (!parent.exists() && !parent.mkdirs())) {
            return false;
        }

        java.nio.file.Path temporary = null;
        try {
            temporary = Files.createTempFile(parent.toPath(), file.getName() + ".", ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }

            if (file.exists()) {
                Files.copy(file.toPath(), getBackupFile(file).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            try {
                Files.move(temporary, file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to atomically write {}", file, e);
            return false;
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static JsonObject getModuleJson(JsonObject root, Module module) {
        if (root.has(module.getId()) && root.get(module.getId()).isJsonObject()) {
            return root.getAsJsonObject(module.getId());
        }
        // Existing profiles used the module display name before stable module IDs were introduced.
        if (root.has(module.getName()) && root.get(module.getName()).isJsonObject()) {
            return root.getAsJsonObject(module.getName());
        }
        return null;
    }

    private static void persistState() {
        JsonObject stateJson = new JsonObject();
        stateJson.addProperty("currentProfile", currentProfileName);

        if (!writeJsonAtomically(getStateFile(), stateJson)) {
            LOGGER.error("Failed to save config state");
        }
    }

    private static String readPersistedProfileName() {
        File stateFile = getStateFile();
        if (!stateFile.exists()) {
            return "";
        }

        try {
            JsonObject stateJson = readJsonWithBackup(stateFile);
            if (stateJson == null) {
                return "";
            }
            if (!stateJson.has("currentProfile")) {
                return "";
            }

            return normalizeProfileName(stateJson.get("currentProfile").getAsString());
        } catch (Exception e) {
            LOGGER.error("Failed to read config state", e);
            return "";
        }
    }

    private static File getDefaultConfigFile() {
        return new File(getConfigDirectory(), DEFAULT_CONFIG_FILE);
    }

    private static File getStateFile() {
        return new File(getConfigDirectory(), STATE_FILE);
    }

    private static File getBackupFile(File file) {
        return new File(file.getParentFile(), file.getName() + ".bak");
    }

    private static File getProfileFile(String profileName) {
        return new File(getProfilesDirectory(), profileName + ".json");
    }

    private static String normalizeProfileName(String rawName) {
        if (rawName == null) {
            return "";
        }

        String name = rawName.trim();
        if (name.endsWith(".json")) {
            name = name.substring(0, name.length() - 5);
        }

        name = name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
        while (name.endsWith(".")) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    private static void notifyListeners() {
        List<Runnable> snapshot = new ArrayList<>();
        synchronized (listeners) {
            listeners.removeIf(ref -> ref.get() == null);
            for (WeakReference<Runnable> ref : listeners) {
                Runnable listener = ref.get();
                if (listener != null) {
                    snapshot.add(listener);
                }
            }
        }

        for (Runnable listener : snapshot) {
            try {
                listener.run();
            } catch (Throwable t) {
                LOGGER.warn("Config listener failed", t);
            }
        }
    }
}
