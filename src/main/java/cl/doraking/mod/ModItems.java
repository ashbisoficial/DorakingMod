package cl.doraking.mod;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Doraking.MODID);

    /** Dorakin de anko (porotos rojos): regeneracion. Solo se consigue comerciando. */
    public static final RegistryObject<Item> DORAKIN_ANKO = ITEMS.register("dorakin_anko",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(8).saturationMod(0.8f)
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 1.0f)
                    .build())));

    /** Dorakin de manjar: velocidad. Solo se consigue comerciando. */
    public static final RegistryObject<Item> DORAKIN_MANJAR = ITEMS.register("dorakin_manjar",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(8).saturationMod(0.8f)
                    .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 0), 1.0f)
                    .build())));

    /** Dorakin de pastelera: absorcion. Solo se consigue comerciando. */
    public static final RegistryObject<Item> DORAKIN_PASTELERA = ITEMS.register("dorakin_pastelera",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(8).saturationMod(0.8f)
                    .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 1200, 0), 1.0f)
                    .build())));

    /** Dorakin de nutella: salto. Solo se consigue comerciando. */
    public static final RegistryObject<Item> DORAKIN_NUTELLA = ITEMS.register("dorakin_nutella",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(8).saturationMod(0.8f)
                    .effect(() -> new MobEffectInstance(MobEffects.JUMP, 600, 1), 1.0f)
                    .build())));

    public static final RegistryObject<Item> PUESTO = ITEMS.register("puesto_doraking",
            () -> new BlockItem(ModBlocks.PUESTO.get(), new Item.Properties()));

    public static final RegistryObject<Item> DUENO_SPAWN_EGG = ITEMS.register("dueno_doraking_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.DUENO, 0xF4D70A, 0xE42020, new Item.Properties()));
}
