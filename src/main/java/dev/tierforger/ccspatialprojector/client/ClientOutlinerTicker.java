package dev.tierforger.ccspatialprojector.client;

import com.mojang.datafixers.util.Pair;
import dev.tierforger.ccspatialprojector.CcSpatialProjector;
import dev.tierforger.ccspatialprojector.registry.ModBlocks;
import dev.tierforger.ccspatialprojector.util.GoggleAccess;
import dev.tierforger.ccspatialprojector.util.GoggleLink;
import dev.tierforger.ccspatialprojector.util.ProjectorKey;
import dev.tierforger.ccspatialprojector.util.ProjectorLocation;
import dev.tierforger.ccspatialprojector.visual.VisualObject;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Optional;

@EventBusSubscriber(modid = CcSpatialProjector.MOD_ID, value = Dist.CLIENT)
public final class ClientOutlinerTicker {
    private ClientOutlinerTicker() {}

    private static final int BOUND_PROJECTOR_COLOR = 0x33ff88;
    private static final float BOUND_PROJECTOR_WIDTH = 0.075f;
    private static final float MIN_VISUAL_WIDTH = 0.005f;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        drawHeldGogglesBinding(mc.player);
        GoggleAccess.activeSource(mc.player).ifPresent(source -> {
            for (VisualObject object : ClientVisualStore.objectsFor(source)) draw(object);
        });
    }

    private static void drawHeldGogglesBinding(Player player) {
        drawHeldGogglesBinding(player.getMainHandItem(), "main");
        drawHeldGogglesBinding(player.getOffhandItem(), "off");
    }

    private static void drawHeldGogglesBinding(ItemStack stack, String hand) {
        Optional<ProjectorKey> maybeKey = GoggleLink.boundProjector(stack);
        if (maybeKey.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Optional<ProjectorLocation> maybeLocation = ClientVisualStore.activeLocationFor(maybeKey.get());
        if (maybeLocation.isEmpty() || !maybeLocation.get().isSameDimension(mc.level)) return;

        BlockPos pos = maybeLocation.get().blockPos();
        boolean projectorLoaded = mc.level.isLoaded(pos) && mc.level.getBlockState(pos).is(ModBlocks.SPATIAL_PROJECTOR.get());
        if (!projectorLoaded) return;

        Outliner.getInstance()
            .showAABB(Pair.of("cc_spatial_projector_bound_projector", hand), new AABB(pos).inflate(0.06))
            .colored(BOUND_PROJECTOR_COLOR)
            .lineWidth(BOUND_PROJECTOR_WIDTH);
    }

    private static void draw(VisualObject object) {
        int color = object.options().color();
        float width = Math.max(MIN_VISUAL_WIDTH, object.options().width());

        switch (object.kind()) {
            case LINE -> {
                if (object.points().size() >= 2) drawLine(object, 0, object.points().get(0), object.points().get(1), color, width);
            }
            case POLYLINE -> {
                for (int i = 0; i < object.points().size() - 1; i++) {
                    drawLine(object, i, object.points().get(i), object.points().get(i + 1), color, width);
                }
            }
            case BOX, MARKER -> {
                if (object.box() != null) drawAabb(object, object.box(), color, width);
            }
        }
    }

    private static void drawLine(VisualObject object, int index, Vec3 from, Vec3 to, int color, float width) {
        Outliner.getInstance()
            .showLine(Pair.of("cc_spatial_projector_line", object.source().id() + ":" + object.id() + ":" + index), from, to)
            .colored(color)
            .lineWidth(width);
    }

    private static void drawAabb(VisualObject object, AABB box, int color, float width) {
        Outliner.getInstance()
            .showAABB(Pair.of("cc_spatial_projector_box", object.source().id() + ":" + object.id()), box)
            .colored(color)
            .lineWidth(width);
    }
}
