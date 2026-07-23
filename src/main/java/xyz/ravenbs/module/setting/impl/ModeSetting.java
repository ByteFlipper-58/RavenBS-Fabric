package xyz.ravenbs.module.setting.impl;

import com.google.gson.JsonObject;
import xyz.ravenbs.module.setting.Setting;

public class ModeSetting extends Setting {
    private String[] options;
    private int input;

    public ModeSetting(String name, String[] options, int defaultInput) {
        super(name);
        this.options = options;
        this.input = defaultInput;
    }

    public String[] getOptions() {
        return options;
    }

    public int getInput() {
        return input;
    }

    public void setInput(int input) {
        this.input = options.length == 0 ? 0 : Math.max(0, Math.min(input, options.length - 1));
    }

    public void cycle() {
        input++;
        if (input >= options.length) {
            input = 0;
        }
    }

    @Override
    public void loadProfile(JsonObject data) {
        com.google.gson.JsonElement value = getProfileValue(data);
        if (value != null && value.isJsonPrimitive()) {
            setInput(value.getAsInt());
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject data = new JsonObject();
        data.addProperty(getStorageId(), input);
        return data;
    }
}
