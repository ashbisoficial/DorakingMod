package cl.doraking.mod;

import cl.doraking.mod.entity.DuenoEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Doraking.MODID);

    // MobCategory.MISC: no aparece solo. Solo se invoca con el huevo de aparicion.
    public static final RegistryObject<EntityType<DuenoEntity>> DUENO = ENTITIES.register("dueno_doraking",
            () -> EntityType.Builder.of(DuenoEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.95f).clientTrackingRange(10)
                    .build("dueno_doraking"));
}
