package cl.doraking.mod.client;

import cl.doraking.mod.Doraking;
import cl.doraking.mod.ModEntities;
import cl.doraking.mod.entity.DuenoEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Doraking.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.DUENO.get(), DuenoRenderer::new);
    }

    public static class DuenoRenderer extends HumanoidMobRenderer<DuenoEntity, PlayerModel<DuenoEntity>> {
        private static final ResourceLocation TEXTURE =
                new ResourceLocation(Doraking.MODID, "textures/entity/dueno.png");

        public DuenoRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
        }

        @Override
        public ResourceLocation getTextureLocation(DuenoEntity entity) {
            return TEXTURE;
        }
    }
}
