package haage.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            try {
                return AutoConfig.getConfigScreen(ClothConfigWrapper.class, parent).get();
            } catch (Exception e) {
                // Log the error and return parent as fallback
                System.err.println("Failed to create config screen: " + e.getMessage());
                e.printStackTrace();
                return parent;
            }
        };
    }
}
