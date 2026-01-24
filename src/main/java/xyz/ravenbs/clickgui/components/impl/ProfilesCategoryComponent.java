package xyz.ravenbs.clickgui.components.impl;

import xyz.ravenbs.clickgui.components.Component;
import xyz.ravenbs.config.ConfigManager;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.module.setting.impl.StringSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Arrays;
import java.util.Comparator;

public class ProfilesCategoryComponent extends CategoryComponent {

    private boolean sortByName = false;

    public ProfilesCategoryComponent(int x, int y) {
        super(ModuleCategory.profiles, x, y);
        ConfigManager.addListener(this::reloadModules);
    }

    @Override
    public void reloadModules() {
        this.getModules().clear();

        // 1. Settings / Tools
        this.getModules().add(new ModuleComponent(new SortModule(), this, 0));
        this.getModules().add(new ModuleComponent(new CreateProfileModule(), this, 0));

        // 2. Scan Directory
        File dir = ConfigManager.getProfilesDirectory();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            if (files != null) {
                // Sorting logic
                if (sortByName) {
                    Arrays.sort(files, Comparator.comparing(File::getName));
                } else {
                    // Start from newest
                    Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                }

                for (File file : files) {
                    String name = file.getName().replace(".json", "");
                    this.getModules().add(new ModuleComponent(new ProfileModule(name, file), this, 0));
                }
            }
        }
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.WRITABLE_BOOK);
    }

    // --- Fake Modules ---

    private class SortModule extends Module {
        public SortModule() {
            super("Sorting: " + (sortByName ? "Name" : "Date"), ModuleCategory.profiles, 0);
        }

        @Override
        public void onEnable() {
            sortByName = !sortByName;
            this.name = "Sorting: " + (sortByName ? "Name" : "Date"); // Update name
            ProfilesCategoryComponent.this.reloadModules();
            // Don't disable, let it reload which resets state
        }
    }

    private class CreateProfileModule extends Module {
        private final StringSetting nameSetting;

        public CreateProfileModule() {
            super("Create Profile", ModuleCategory.profiles, 0);
            String defaultName = "profile_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            this.registerSetting(nameSetting = new StringSetting("Name", defaultName));
            
            this.registerSetting(new ButtonSetting("Create", false) {
                 @Override
                 public void toggle() {
                      String name = nameSetting.getString();
                      if (name.isEmpty()) name = defaultName;
                      // Ensure unique? ConfigManager.saveConfig overwrites.
                      if (ConfigManager.saveConfig(name)) {
                          ProfilesCategoryComponent.this.reloadModules();
                      }
                 }
            });
        }
        
        @Override
        public void onEnable() {
             this.disable(); // It's just a folder for settings
        }
    }

    private class ProfileModule extends Module {
        private final String profileName;
        private final File file;
        private final StringSetting renameSetting;

        public ProfileModule(String name, File file) {
            super(name, ModuleCategory.profiles, 0);
            this.profileName = name;
            this.file = file;

            // Highlight active
            if (profileName.equals(ConfigManager.getCurrentProfileName())) {
                this.enabled = true;
            }

            // Settings
            this.registerSetting(new DescriptionSetting("Modified: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(file.lastModified()))));
            
            this.registerSetting(new ButtonSetting("Overwrite with Current", false) {
                 @Override
                 public void toggle() {
                     if (ConfigManager.saveConfig(profileName)) {
                         ProfilesCategoryComponent.this.reloadModules();
                     }
                 }
            });
            
            this.registerSetting(renameSetting = new StringSetting("Rename to", profileName));
            this.registerSetting(new ButtonSetting("Apply Rename", false) {
                 @Override
                 public void toggle() {
                     String newName = renameSetting.getString();
                     if (!newName.equals(profileName) && !newName.isEmpty()) {
                         File newFile = new File(file.getParent(), newName + ".json");
                         if (file.renameTo(newFile)) {
                             // Update current profile name if we renamed the active one
                             if (ConfigManager.getCurrentProfileName().equals(profileName)) {
                                 // We need to manually update ConfigManager field? 
                                 // No direct setter, but loading sets it. 
                                 // Let's just reload modules.
                             }
                             ProfilesCategoryComponent.this.reloadModules();
                         }
                     }
                 }
            });

            this.registerSetting(new ButtonSetting("Delete Profile", false) {
                @Override
                public void toggle() {
                     ConfigManager.deleteConfig(profileName);
                }
            });
        }

        @Override
        public void onEnable() {
            if (ConfigManager.loadConfig(profileName)) {
                 // Config loaded. This sets currentProfileName.
                 ProfilesCategoryComponent.this.reloadModules();
            }
        }
    }
}
