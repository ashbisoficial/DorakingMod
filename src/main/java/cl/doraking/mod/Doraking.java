package cl.doraking.mod;

import cl.doraking.mod.entity.DuenoEntity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

@Mod(Doraking.MODID)
public class Doraking {
    public static final String MODID = "doraking";

    public Doraking() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModTabs.TABS.register(bus);
        bus.addListener(this::onAttributes);
    }

    private void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.DUENO.get(), DuenoEntity.createAttributes().build());
    }
}
