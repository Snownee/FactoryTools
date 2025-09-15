package eu.pb4.factorytools.api.virtualentity;

import eu.pb4.factorytools.api.util.SharedMatrix4f;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * You should expect this element to move with pistons and falling blocks!
 */
public class BlockModel extends ElementHolder {
    private static int startTick = 0;
    private int updateTick = (startTick++) % 20;
    protected BlockState blockState = Blocks.AIR.getDefaultState();

    private static final SharedMatrix4f mat = new SharedMatrix4f();

    public static Matrix4f mat() {
        return mat.main();
    }

    public final int getTick() {
        return this.updateTick;
    }

    public boolean isTimeForMediumUpdate() {
        return updateTick % 2 == 0;
    }

    @Override
    public void tick() {
        super.tick();
        this.updateTick++;
    }

    protected double squaredDistance(ServerPlayNetworkHandler player) {
        return this.getPos().squaredDistanceTo(player.player.getPos());
    }

    @Nullable
    protected BlockAwareAttachment blockAware() {
        return this.getAttachment() instanceof BlockAwareAttachment blockAwareAttachment ? blockAwareAttachment : null;
    }

    protected BlockState blockState() {
        var x = blockAware();
        return x != null && !x.getBlockState().isAir() ? x.getBlockState() : blockState;
    }

    protected BlockPos blockPos() {
        var x = blockAware();
        return x != null ? x.getBlockPos() : BlockPos.ORIGIN;
    }

    protected boolean inWorld() {
        var x = blockAware();
        return x != null && x.isPartOfTheWorld();
    }

    @Override
    public boolean addElementWithoutUpdates(VirtualElement element) {
        refreshBlockContext(element);
        return super.addElementWithoutUpdates(element);
    }

    protected void refreshBlockContext(VirtualElement element) {
        BlockState blockState = blockState();
        if (blockState.isAir() || !(element instanceof ItemDisplayElement display) || display.getItem().isEmpty()) {
            return;
        }
        ItemStack item = display.getItem().copy();
        item.set(DataComponentTypes.ITEM_NAME, blockState.getBlock().getName());
        item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT.with(NbtOps.INSTANCE, PolymerItemUtils.POLYMER_STACK_ID_CODEC, Registries.BLOCK.getId(blockState.getBlock())).getOrThrow());
        display.setItem(item);
    }
}
