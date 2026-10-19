package net.lvnatic.satchels.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.lvnatic.satchels.ModItems;
import net.lvnatic.satchels.Satchels;
import net.lvnatic.satchels.api.SatchelAccess;

public final class SatchelLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation MODEL_ID = Satchels.at("item/satchel_worn");
    private final ItemRenderer itemRenderer;

    public SatchelLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, ItemRenderer itemRenderer) {
        super(parent);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (player.isInvisible() || !SatchelAccess.satchelIsVisible(player)) return;

        AccessoriesCapability capability = AccessoriesCapability.get(player);
        if (capability == null) return;

        var equipped = capability.getFirstEquipped(ModItems.SATCHEL);
        if (equipped == null) return;

        var reference = equipped.reference();
        AccessoriesContainer container = capability.getContainers().get(reference.slotName());

        if (container == null || !container.shouldRender(reference.slot())) return;

        ItemStack stack = equipped.stack();
        if (!stack.is(ModItems.SATCHEL)) return;

        BakedModel model = ((FabricBakedModelManager) Minecraft.getInstance().getModelManager()).getModel(MODEL_ID);

        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);
        poseStack.translate(0.0f, 0.25f, 0.0f);
        poseStack.scale(-1.0f, -1.0f, 1.0f);

        itemRenderer.render(
                stack,
                ItemDisplayContext.HEAD,
                false,
                poseStack,
                buffer,
                light,
                OverlayTexture.NO_OVERLAY,
                model
        );

        poseStack.popPose();
    }
}