package xyz.ravenbs.module.setting.impl;

import com.google.gson.JsonObject;
import xyz.ravenbs.module.setting.Setting;

public class StringSetting extends Setting {
    private String defaultValue;
    private String string;

    public StringSetting(String name, String defaultValue) {
        super(name);
        this.defaultValue = defaultValue;
        this.string = defaultValue;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    @Override
    public void loadProfile(JsonObject data) {
        com.google.gson.JsonElement value = getProfileValue(data);
        if (value != null && value.isJsonPrimitive()) {
            this.setString(value.getAsString());
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(getStorageId(), getString());
        return jsonObject;
    }
}
