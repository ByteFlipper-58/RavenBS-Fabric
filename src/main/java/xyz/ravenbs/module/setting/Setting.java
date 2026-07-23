package xyz.ravenbs.module.setting;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

public abstract class Setting {
    public String name;
    public boolean visible = true;
    private String storageId = "";

    public Setting(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public final String getStorageId() {
        return storageId.isEmpty() ? name : storageId;
    }

    public final void assignStorageId(String storageId) {
        if (this.storageId.isEmpty() && storageId != null && !storageId.isBlank()) {
            this.storageId = storageId;
        }
    }

    protected final JsonElement getProfileValue(JsonObject data) {
        if (data.has(getStorageId())) {
            return data.get(getStorageId());
        }
        // Profiles created before stable setting IDs used the display name as the key.
        return data.get(name);
    }

    public boolean isPersistent() {
        return true;
    }
    
    public String getLocalizedName() {
         // raven.setting.name -> Translation
         String key = "raven.setting." + name.toLowerCase().replace(" ", "_");
         if (net.minecraft.client.resource.language.I18n.hasTranslation(key)) {
             return net.minecraft.client.resource.language.I18n.translate(key);
         }
         return net.minecraft.client.resource.language.I18n.hasTranslation(name) ? net.minecraft.client.resource.language.I18n.translate(name) : name;
    }

    // Abstract methods for saving/loading
    public abstract void loadProfile(JsonObject data);
    public abstract JsonObject toJson();
}
