package io.github.maxoduke.wdimmnp.items;

import io.github.maxoduke.wdimmnp.Mod;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.jetbrains.annotations.Nullable;

public class LinkedCompassItem extends Item
{
    public static final String POI_DIMENSION = "NetherPortalBlockDimension";
    public static final String POI_POS_IN_OVERWORLD = "NetherPortalBlockPosInOverworld";
    public static final String POI_POS_IN_NETHER = "NetherPortalBlockPosInNether";
    public static final String OVERWORLD_ID = "minecraft:overworld";
    public static final String NETHER_ID = "minecraft:the_nether";

    public LinkedCompassItem(Settings settings)
    {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack)
    {
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
    {
        if (world.isClient)
            return;

        NbtCompound linkedCompassNbtData = stack.getNbt();
        if (linkedCompassNbtData == null || !linkedCompassNbtData.contains(POI_DIMENSION) || !linkedCompassNbtData.contains(POI_POS_IN_OVERWORLD) || !linkedCompassNbtData.contains(POI_POS_IN_NETHER))
            return;

        MinecraftServer server = ((ServerWorld) world).getServer();
        ServerWorld overworld = server.getWorld(World.OVERWORLD);
        ServerWorld nether = server.getWorld(World.NETHER);

        if (overworld == null || nether == null)
            return;

        PointOfInterestStorage overworldPoiStorage = overworld.getPointOfInterestStorage();
        PointOfInterestStorage netherPoiStorage = nether.getPointOfInterestStorage();

        String poiDimension = linkedCompassNbtData.getString(POI_DIMENSION);
        BlockPos poiPosInOverworld = NbtHelper.toBlockPos(linkedCompassNbtData.getCompound(POI_POS_IN_OVERWORLD));
        BlockPos poiPosInNether = NbtHelper.toBlockPos(linkedCompassNbtData.getCompound(POI_POS_IN_NETHER));

        if ((poiDimension.equals(OVERWORLD_ID) && !overworldPoiStorage.hasTypeAt(PointOfInterestType.NETHER_PORTAL, poiPosInOverworld)) ||
                (poiDimension.equals(NETHER_ID) && !netherPoiStorage.hasTypeAt(PointOfInterestType.NETHER_PORTAL, poiPosInNether)))
        {
            linkedCompassNbtData.remove(POI_DIMENSION);
            linkedCompassNbtData.remove(POI_POS_IN_OVERWORLD);
            linkedCompassNbtData.remove(POI_POS_IN_NETHER);
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context)
    {
        if (_useOnBlock(context))
            return ActionResult.success(true);

        return super.useOnBlock(context);
    }

    public static boolean _useOnBlock(ItemUsageContext context)
    {
        World world = context.getWorld();

        if (world.isClient)
            return false;

        BlockPos posOfBlockUsed = context.getBlockPos();
        if (world.getBlockState(posOfBlockUsed).isOf(Blocks.NETHER_PORTAL))
        {
            PlayerEntity player = context.getPlayer();
            if (player == null)
                return false;

            ItemStack linkedCompassItemStack, itemStack = context.getStack();
            String playerDimension = player.world.getRegistryKey().getValue().toString();
            BlockPos poiPosInOverworld, poiPosInNether;

            double x = posOfBlockUsed.getX();
            double y = posOfBlockUsed.getY();
            double z = posOfBlockUsed.getZ();

            if (playerDimension.equals(OVERWORLD_ID))
            {
                poiPosInOverworld = new BlockPos(x, y, z);
                poiPosInNether = new BlockPos(Math.floor(x / 8), y, Math.floor(z / 8));
            }
            else if (playerDimension.equals(NETHER_ID))
            {
                poiPosInNether = new BlockPos(x, y, z);
                poiPosInOverworld = new BlockPos(Math.floor(x * 8), y, Math.floor(z * 8));
            }
            else
                return false;

            int slot = -1;
            boolean modifyingExistingCompass = false;

            if (itemStack.getItem() instanceof LinkedCompassItem && itemStack.getCount() == 1)
            {
                linkedCompassItemStack = itemStack;
                modifyingExistingCompass = true;
            }
            else
            {
                linkedCompassItemStack = new ItemStack(Mod.LINKED_COMPASS_ITEM, 1);
                if (itemStack.getItem() instanceof CompassItem && itemStack.getCount() == 1)
                {
                    EnvType currentEnv = FabricLauncherBase.getLauncher().getEnvironmentType();
                    slot = !player.isCreative() && currentEnv == EnvType.CLIENT ? player.getInventory().getSlotWithStack(itemStack) : -1;
                }
            }

            NbtCompound linkedCompassNbtData = linkedCompassItemStack.getOrCreateNbt();
            linkedCompassNbtData.putString(POI_DIMENSION, playerDimension);
            linkedCompassNbtData.put(POI_POS_IN_OVERWORLD, NbtHelper.fromBlockPos(poiPosInOverworld));
            linkedCompassNbtData.put(POI_POS_IN_NETHER, NbtHelper.fromBlockPos(poiPosInNether));

            if (!modifyingExistingCompass)
            {
                if (!player.isCreative())
                    itemStack.decrement(1);

                if (!player.getInventory().insertStack(slot, linkedCompassItemStack))
                    player.dropItem(linkedCompassItemStack, false);
            }

            PointOfInterestStorage poiStorage = ((ServerWorld) world).getPointOfInterestStorage();
            poiStorage.add(posOfBlockUsed, PointOfInterestType.NETHER_PORTAL);

            world.playSound(null, posOfBlockUsed, SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 1.0f, 1.0f);
            return true;
        }

        return false;
    }

    @Nullable
    public static BlockPos getLinkedBlockPos(ItemStack itemStack, ClientWorld clientWorld)
    {
        String playerDimension = clientWorld.getRegistryKey().getValue().toString();
        NbtCompound linkedCompassNbtData = itemStack.getNbt();
        BlockPos poiPosInOverWorld, poiPosInNether, posToPoint = null;

        if (linkedCompassNbtData == null)
            return null;

        if (!linkedCompassNbtData.contains(POI_DIMENSION) || !linkedCompassNbtData.contains(POI_POS_IN_OVERWORLD) || !linkedCompassNbtData.contains(POI_POS_IN_NETHER))
            return null;

        poiPosInOverWorld = NbtHelper.toBlockPos(linkedCompassNbtData.getCompound(POI_POS_IN_OVERWORLD));
        poiPosInNether = NbtHelper.toBlockPos(linkedCompassNbtData.getCompound(POI_POS_IN_NETHER));

        if (playerDimension.equals(OVERWORLD_ID))
            posToPoint = poiPosInOverWorld;
        else if (playerDimension.equals(NETHER_ID))
            posToPoint = poiPosInNether;

        return posToPoint;
    }
}