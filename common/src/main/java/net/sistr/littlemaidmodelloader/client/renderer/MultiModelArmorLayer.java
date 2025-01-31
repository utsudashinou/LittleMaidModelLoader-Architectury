package net.sistr.littlemaidmodelloader.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.profiler.Profiler;
import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel;
import net.sistr.littlemaidmodelloader.maidmodel.IModelCaps;
import net.sistr.littlemaidmodelloader.multimodel.layer.MMRenderContext;

//todo 重すぎる
@Environment(EnvType.CLIENT)
public class MultiModelArmorLayer<T extends LivingEntity & IHasMultiModel, M extends MultiModel<T>> extends FeatureRenderer<T, M> {

    public MultiModelArmorLayer(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity,
                       float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                       float headYaw, float headPitch) {
        Profiler profiler = MinecraftClient.getInstance().getProfiler();
        profiler.push("littlemaidmodelloader:mm_armor_layer");
        this.renderArmorPart(matrices, vertexConsumers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress,
                headYaw, headPitch, IHasMultiModel.Part.HEAD);
        this.renderArmorPart(matrices, vertexConsumers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress,
                headYaw, headPitch, IHasMultiModel.Part.BODY);
        this.renderArmorPart(matrices, vertexConsumers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress,
                headYaw, headPitch, IHasMultiModel.Part.LEGS);
        this.renderArmorPart(matrices, vertexConsumers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress,
                headYaw, headPitch, IHasMultiModel.Part.FEET);
        profiler.pop();
    }

    private void renderArmorPart(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity,
                                 float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                                 float headYaw, float headPitch, IHasMultiModel.Part part) {
        if (!entity.isArmorVisible(part)) {
            return;
        }

        boolean glint = entity.isArmorGlint(part);

        IModelCaps caps = entity.getCaps();

        renderArmorLayer(matrices, vertexConsumers, light, entity,
                limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch,
                part, IHasMultiModel.Layer.INNER, false, caps, glint);
        renderArmorLayer(matrices, vertexConsumers, light, entity,
                limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch,
                part, IHasMultiModel.Layer.INNER, true, caps, glint);
        renderArmorLayer(matrices, vertexConsumers, light, entity,
                limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch,
                part, IHasMultiModel.Layer.OUTER, false, caps, glint);
        renderArmorLayer(matrices, vertexConsumers, light, entity,
                limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch,
                part, IHasMultiModel.Layer.OUTER, true, caps, glint);

    }

    private void renderArmorLayer(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity,
                                  float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                                  float headYaw, float headPitch,
                                  IHasMultiModel.Part part, IHasMultiModel.Layer layer, boolean isLight, IModelCaps caps,
                                  boolean glint) {
        entity.getTexture(layer, part, isLight).ifPresent(resourceLocation ->
                entity.getModel(layer, part).ifPresent(model -> {
                    model.showArmorParts(part.getIndex(), layer.getPartIndex());
                    RenderLayer type = MultiModelRenderLayer.getDefault(resourceLocation);
                    VertexConsumer builder = ItemRenderer.getArmorGlintConsumer(vertexConsumers, type, false, glint);
                    int light0 = isLight ? 0xF00000 : light;
                    model.animateModel(caps, limbAngle, limbDistance, tickDelta);
                    model.setAngles(caps, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
                    model.render(new MMRenderContext(matrices, builder, light0, OverlayTexture.DEFAULT_UV,
                            1F, 1F, 1F, 1F));
                })
        );
    }

}
