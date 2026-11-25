package cl.doraking.mod.client;

import cl.doraking.mod.Doraking;
import cl.doraking.mod.ModEntities;
import cl.doraking.mod.entity.DuenoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Doraking.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DuenoModel.LAYER, DuenoModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.DUENO.get(), DuenoRenderer::new);
    }

    public static class DuenoRenderer extends MobRenderer<DuenoEntity, DuenoModel> {
        private static final ResourceLocation TEXTURE =
                new ResourceLocation(Doraking.MODID, "textures/entity/dueno.png");

        public DuenoRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new DuenoModel(ctx.bakeLayer(DuenoModel.LAYER)), 0.6f);
        }

        @Override
        public ResourceLocation getTextureLocation(DuenoEntity entity) {
            return TEXTURE;
        }
    }
}
