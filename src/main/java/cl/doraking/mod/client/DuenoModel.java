package cl.doraking.mod.client;

import cl.doraking.mod.Doraking;
import cl.doraking.mod.entity.DuenoEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Modelo propio del Dueno de Doraking: complexion robusta, gorra con visera, lentes y barba. Textura 128x64 (textures/entity/dueno.png).
 */
public class DuenoModel extends EntityModel<DuenoEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(Doraking.MODID, "dueno"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public DuenoModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();

        PartDefinition head = r.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4f, -8f, -4f, 8f, 8f, 8f),
                PartPose.offset(0f, 0f, 0f));
        head.addOrReplaceChild("cap",
                CubeListBuilder.create().texOffs(32, 0).addBox(-4.5f, -9f, -4.5f, 9f, 4f, 9f), PartPose.ZERO);
        head.addOrReplaceChild("brim",
                CubeListBuilder.create().texOffs(68, 0).addBox(-4.5f, -6f, -8.5f, 9f, 1f, 4f), PartPose.ZERO);
        head.addOrReplaceChild("glasses",
                CubeListBuilder.create().texOffs(94, 0).addBox(-4.5f, -5f, -4.5f, 9f, 2f, 1f), PartPose.ZERO);

        // torso algo mas ancho que el de un jugador, con pancita
        PartDefinition body = r.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 16).addBox(-5f, 0f, -3f, 10f, 12f, 6f),
                PartPose.offset(0f, 0f, 0f));
        body.addOrReplaceChild("belly",
                CubeListBuilder.create().texOffs(32, 16).addBox(-4f, 5f, -5f, 8f, 6f, 2f), PartPose.ZERO);

        r.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(52, 16).addBox(-2f, -2f, -2f, 4f, 12f, 4f),
                PartPose.offset(-7f, 2f, 0f));
        r.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(52, 16).mirror().addBox(-2f, -2f, -2f, 4f, 12f, 4f),
                PartPose.offset(7f, 2f, 0f));

        r.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 36).addBox(-2.5f, 0f, -2.5f, 5f, 12f, 5f),
                PartPose.offset(-2.6f, 12f, 0f));
        r.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 36).mirror().addBox(-2.5f, 0f, -2.5f, 5f, 12f, 5f),
                PartPose.offset(2.6f, 12f, 0f));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(DuenoEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * ((float) Math.PI / 180f);
        head.xRot = headPitch * ((float) Math.PI / 180f);
        rightLeg.xRot = (float) Math.cos(limbSwing * 0.6662f) * 1.2f * limbSwingAmount;
        leftLeg.xRot = (float) Math.cos(limbSwing * 0.6662f + Math.PI) * 1.2f * limbSwingAmount;
        rightArm.xRot = (float) Math.cos(limbSwing * 0.6662f + Math.PI) * 0.8f * limbSwingAmount + 0.1f;
        leftArm.xRot = (float) Math.cos(limbSwing * 0.6662f) * 0.8f * limbSwingAmount + 0.1f;
        rightArm.zRot = 0.05f;
        leftArm.zRot = -0.05f;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay,
                               float r, float g, float b, float a) {
        root.render(pose, buffer, light, overlay, r, g, b, a);
    }
}
