package io.github.maxoduke.wdimmnp.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class LinkedCompassModelPredicateProvider implements ModelPredicateProvider
{
    private final AngleInterpolator value = new AngleInterpolator();
    private final AngleInterpolator speed = new AngleInterpolator();

    @Override
    public float call(ItemStack itemStack, @Nullable ClientWorld clientWorld, @Nullable LivingEntity livingEntity)
    {
        Entity entity = livingEntity != null ? livingEntity : itemStack.getHolder();
        if (entity == null)
            return 0.0f;

        clientWorld = (ClientWorld) entity.world;
        long worldTime = clientWorld.getTime();

        BlockPos posToPoint = LinkedCompassItem.getLinkedBlockPos(itemStack, clientWorld);
        if (posToPoint == null)
        {
            if (this.speed.shouldUpdate(worldTime))
                this.speed.update(worldTime, Math.random());

            double angle = this.speed.value + (double) ((float) itemStack.hashCode() / 2.14748365E9f);
            return MathHelper.floorMod((float) angle, 1.0f);
        }

        boolean isPlayer = livingEntity instanceof PlayerEntity && ((PlayerEntity) livingEntity).isMainPlayer();
        double value = 0.0;

        if (isPlayer)
            value = livingEntity.yaw;
        else if (entity instanceof ItemFrameEntity)
            value = this.getItemFrameAngleOffset((ItemFrameEntity) entity);
        else if (entity instanceof ItemEntity)
            value = 180.0f - ((ItemEntity) entity).method_27314(0.5f) / ((float) Math.PI * 2) * 360.0f;
        else if (livingEntity != null)
            value = livingEntity.bodyYaw;

        value = MathHelper.floorMod(value / 360.0, 1.0);

        double angle;
        double angleToPos = this.getAngleToPos(Vec3d.ofCenter(posToPoint), entity) / 6.2831854820251465;

        if (isPlayer)
        {
            if (this.value.shouldUpdate(worldTime))
                this.value.update(worldTime, 0.5 - (value - 0.25));

            angle = angleToPos + this.value.value;
        }
        else
            angle = 0.5 - (value - 0.25 - angleToPos);

        return MathHelper.floorMod((float) angle, 1.0f);
    }

    private double getItemFrameAngleOffset(ItemFrameEntity itemFrame)
    {
        Direction direction = itemFrame.getHorizontalFacing();
        int i = direction.getAxis().isVertical() ? 90 * direction.getDirection().offset() : 0;
        return MathHelper.wrapDegrees(180 + direction.getHorizontal() * 90 + itemFrame.getRotation() * 45 + i);
    }

    private double getAngleToPos(Vec3d pos, Entity entity)
    {
        return Math.atan2(pos.getZ() - entity.getZ(), pos.getX() - entity.getX());
    }

    // Had to re-create this class because it's not accessible outside the "net.minecraft.client.item" package.
    @Environment(value = EnvType.CLIENT)
    private static class AngleInterpolator
    {
        private double value;
        private double speed;
        private long lastUpdateTime;

        private AngleInterpolator()
        {

        }

        private boolean shouldUpdate(long time)
        {
            return this.lastUpdateTime != time;
        }

        private void update(long time, double d)
        {
            this.lastUpdateTime = time;
            double e = d - this.value;
            e = MathHelper.floorMod(e + 0.5, 1.0) - 0.5;
            this.speed += e * 0.1;
            this.speed *= 0.8;
            this.value = MathHelper.floorMod(this.value + this.speed, 1.0);
        }
    }
}
