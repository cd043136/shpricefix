package dev.cd.shpricefix;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.Minecraft;

public final class ShPriceFixCommand {
    private ShPriceFixCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> dispatcher.register(
            ClientCommands.literal("pricefix").executes(ctx -> {
                Minecraft client = ctx.getSource().getClient();
                client.execute(() -> client.setScreen(new PriceFixScreen()));
                return Command.SINGLE_SUCCESS;
            })
        ));
    }
}
