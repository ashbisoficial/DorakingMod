package cl.doraking.mod;

import cl.doraking.mod.trade.TradeConfig;
import cl.doraking.mod.trade.TradeEditorMenu;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Doraking.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        TradeConfig.load();   // lee config/doraking_trades.json al iniciar el mundo/servidor
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("doraking")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("tradeos").executes(ctx -> {
                    TradeEditorMenu.open(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("recargar").executes(ctx -> {
                    TradeConfig.load();
                    ctx.getSource().sendSuccess(() -> Component.literal("Tradeos de Doraking recargados."), true);
                    return 1;
                })));
    }
}
