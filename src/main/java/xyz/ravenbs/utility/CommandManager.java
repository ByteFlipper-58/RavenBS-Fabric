package xyz.ravenbs.utility;

import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.module.Module;
import java.util.Arrays;

public class CommandManager {
    
    public static boolean onChat(String message) {
        if (!message.startsWith(".")) return false;
        
        String[] args = message.substring(1).split(" ");
        if (args.length == 0) return false;
        
        String cmd = args[0].toLowerCase();
        
        switch (cmd) {
            case "help":
                Utils.sendMessage("§bRavenBS Fabric Commands:");
                Utils.sendMessage("§7.bind <module> <key> - Bind module");
                Utils.sendMessage("§7.friend add/remove <name> - Manage friends");
                Utils.sendMessage("§7.config save/load <name> - Manage configs");
                return true;
                
            case "bind":
                if (args.length < 3) {
                    Utils.sendMessage("§cUsage: .bind <module> <key>");
                    return true;
                }
                Module m = ModuleManager.getModule(args[1]);
                if (m == null) {
                    Utils.sendMessage("§cModule not found: " + args[1]);
                    return true;
                }
                // Fabric/GLFW Key mapping
                int key = -1;
                try {
                    // Start of simple lookup
                    String k = args[2].toUpperCase();
                    if (k.length() == 1) {
                         key = net.minecraft.client.util.InputUtil.fromTranslationKey("key.keyboard." + k.toLowerCase()).getCode();
                    } else {
                         // Handle special keys
                         // Simplified: just assume valid integer or use InputUtil
                         // For now, let's use a naive lookup or error out
                         java.lang.reflect.Field f = org.lwjgl.glfw.GLFW.class.getField("GLFW_KEY_" + k);
                         key = f.getInt(null);
                    }
                } catch (Exception e) {
                     Utils.sendMessage("§cInvalid key: " + args[2]);
                     return true;
                }
                m.setBind(key);
                Utils.sendMessage("§aBound " + m.getName() + " to " + args[2].toUpperCase());
                return true;
                
            case "friend":
            case "f":
                if (args.length < 3) {
                    Utils.sendMessage("§cUsage: .friend add/remove <name>");
                    return true;
                }
                if (args[1].equalsIgnoreCase("add")) {
                    FriendManager.addFriend(args[2]);
                } else if (args[1].equalsIgnoreCase("remove")) {
                    FriendManager.removeFriend(args[2]);
                } else if (args[1].equalsIgnoreCase("clear")) {
                    FriendManager.clear();
                }
                return true;
            
            case "config":
            case "c":
                if (args.length < 3) {
                    Utils.sendMessage("§cUsage: .config save/load <name>");
                    return true;
                }
                String profileName = args[2];
                if (args[1].equalsIgnoreCase("save")) {
                    if (xyz.ravenbs.config.ConfigManager.saveConfig(profileName)) {
                        Utils.sendMessage("§aSaved config: " + profileName);
                    } else {
                        Utils.sendMessage("§cFailed to save.");
                    }
                } else if (args[1].equalsIgnoreCase("load")) {
                    if (xyz.ravenbs.config.ConfigManager.loadConfig(profileName)) {
                        Utils.sendMessage("§aLoaded config: " + profileName);
                    } else {
                        Utils.sendMessage("§cConfig not found.");
                    }
                }
                return true;
                
            case "toggle":
            case "t":
                if (args.length < 2) {
                    Utils.sendMessage("§cUsage: .toggle <module>");
                    return true;
                }
                Module mToggle = ModuleManager.getModule(args[1]);
                if (mToggle == null) {
                    Utils.sendMessage("§cModule not found: " + args[1]);
                    return true;
                }
                mToggle.toggle();
                Utils.sendMessage(mToggle.isEnabled() ? "§aEnabled " + mToggle.getName() : "§cDisabled " + mToggle.getName());
                return true;
                
            default:
                Utils.sendMessage("§cUnknown command. Type .help");
                return true;
        }
    }
    public static class Suggestion {
        public final String text;
        public final String tooltip;
        public Suggestion(String text, String tooltip) {
            this.text = text;
            this.tooltip = tooltip;
        }
    }

    public static class SuggestionContext {
        public final int offset;
        public final java.util.List<Suggestion> suggestions;
        public SuggestionContext(int offset, java.util.List<Suggestion> suggestions) {
            this.offset = offset;
            this.suggestions = suggestions;
        }
    }

    public static SuggestionContext getSuggestions(String input) {
        if (input.endsWith(" ")) {
            // Treat "cmd " as "cmd " (ready for next arg)
            // But split ignores trailing empty strings usually unless -1 used.
        }
        
        String[] args = input.split(" ", -1);
        
        // Case 1: Typing command
        // Input: "." or ".t" -> args[0] is "." or ".t"
        if (args.length == 1) {
            java.util.List<Suggestion> list = new java.util.ArrayList<>();
            String current = args[0];
            
            addIfMatch(list, current, ".help", "Show help");
            addIfMatch(list, current, ".bind", "Bind a module to a key");
            addIfMatch(list, current, ".friend", "Manage friends (add/remove)");
            addIfMatch(list, current, ".config", "Manage config profiles");
            addIfMatch(list, current, ".toggle", "Toggle a module");
            // Aliases
            addIfMatch(list, current, ".t", "Alias for .toggle");
            addIfMatch(list, current, ".f", "Alias for .friend");
            addIfMatch(list, current, ".c", "Alias for .config");
            
            return new SuggestionContext(0, list);
        }
        
        // Case 2: Typing first argument
        if (args.length == 2) {
            String cmd = args[0].toLowerCase();
            String arg = args[1].toLowerCase();
            int offset = input.lastIndexOf(" ") + 1;
            java.util.List<Suggestion> list = new java.util.ArrayList<>();
            
            if (cmd.equals(".toggle") || cmd.equals(".t") || cmd.equals(".bind")) {
                for (Module m : ModuleManager.modules) {
                    if (m.getName().toLowerCase().startsWith(arg)) {
                        list.add(new Suggestion(m.getName(), m.getDescription())); // Assuming getDescription exists or use name
                    }
                }
            } else if (cmd.equals(".friend") || cmd.equals(".f")) {
                addIfMatch(list, arg, "add", "Add a friend");
                addIfMatch(list, arg, "remove", "Remove a friend");
                addIfMatch(list, arg, "clear", "Clear all friends");
            } else if (cmd.equals(".config") || cmd.equals(".c")) {
                addIfMatch(list, arg, "save", "Save current profile");
                addIfMatch(list, arg, "load", "Load a profile");
            }
            
            // Sort list?
            list.sort((s1, s2) -> s1.text.compareToIgnoreCase(s2.text));
            
            return new SuggestionContext(offset, list);
        }
        
        // Case 3: Typing second argument? (e.g. .bind Module Key)
        // Not implemented heavily yet, but possible.
        
        return new SuggestionContext(0, new java.util.ArrayList<>());
    }
    
    private static void addIfMatch(java.util.List<Suggestion> list, String input, String target, String tooltip) {
        if (target.toLowerCase().startsWith(input.toLowerCase())) {
            list.add(new Suggestion(target, tooltip));
        }
    }
}
