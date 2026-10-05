package dev.cd.shpricefix;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ShPriceFix implements ClientModInitializer {
    public static final String MOD_ID = "shpricefix";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        String warning = PriceFix.load();
        ShPriceFixCommand.register();

        // warning for stuff like no skyhanni found/incompatible version
        if (warning != null) {
            LOGGER.warn(warning);
            boolean[] pending = {true};
            ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> {
                if (!pending[0] || client.player == null) {
                    return;
                }
                pending[0] = false;
                client.player.sendSystemMessage(Component.literal(warning).withStyle(ChatFormatting.RED));
            });
        }
        LOGGER.info("shpricefix initialized");
    }
}
