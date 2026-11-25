package cl.doraking.mod;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Doraking.MODID);

    public static final RegistryObject<CreativeModeTab> DORAKING = TABS.register("doraking",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.doraking"))
                    .icon(() -> new ItemStack(ModItems.DORAKIN_ANKO.get()))
                    .displayItems((params, out) -> {
                        out.accept(ModItems.DORAKIN_ANKO.get());
                        out.accept(ModItems.DORAKIN_MANJAR.get());
                        out.accept(ModItems.PUESTO.get());
                        out.accept(ModItems.DUENO_SPAWN_EGG.get());
                    })
                    .build());
}
