package net.glowstone.block.blocktype;

import net.glowstone.EventFactory;
import net.glowstone.GlowChunk;
import net.glowstone.GlowServer;
import net.glowstone.block.GlowBlock;
import net.glowstone.block.GlowBlockState;
import net.glowstone.block.ItemTable;
import net.glowstone.block.entity.TileEntity;
import net.glowstone.block.itemtype.ItemType;
import net.glowstone.entity.GlowPlayer;
import net.glowstone.util.SoundInfo;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Base class for specific types of blocks.
 */
public class BlockType extends ItemType {

    protected static final Random random = new Random();
    protected List<ItemStack> drops;

    protected SoundInfo placeSound = new SoundInfo(Sound.BLOCK_WOOD_BREAK, 1F, 0.75F);

    ////////////////////////////////////////////////////////////////////////////
    // Setters for subclass use

    /**
     * Gets the BlockFace opposite of the direction the location is facing.
     * Usually used to set the way container blocks face when being placed.
     *
     * @param location Location to get opposite of
     * @param inverted If up/down should be used
     * @return Opposite BlockFace or EAST if yaw is invalid
     */
    protected static BlockFace getOppositeBlockFace(Location location, boolean inverted) {
        double rot = location.getYaw() % 360;
        if (inverted) {
            // todo: Check the 67.5 pitch in source. This is based off of WorldEdit's number for this.
            double pitch = location.getPitch();
            if (pitch < -67.5D) {
                return BlockFace.DOWN;
            } else if (pitch > 67.5D) {
                return BlockFace.UP;
            }
        }
        if (rot < 0) {
            rot += 360.0;
        }
        if (0 <= rot && rot < 45) {
            return BlockFace.NORTH;
        } else if (45 <= rot && rot < 135) {
            return BlockFace.EAST;
        } else if (135 <= rot && rot < 225) {
            return BlockFace.SOUTH;
        } else if (225 <= rot && rot < 315) {
            return BlockFace.WEST;
        } else if (315 <= rot && rot < 360.0) {
            return BlockFace.NORTH;
        } else {
            return BlockFace.EAST;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Public accessors

    protected final void setDrops(ItemStack... drops) {
        this.drops = Arrays.asList(drops);
    }

    /**
     * Get the items that will be dropped by digging the block.
     *
     * @param block The block being dug.
     * @param tool  The tool used or {@code null} if fists or no tool was used.
     * @return The drops that should be returned.
     */
    public Collection<ItemStack> getDrops(GlowBlock block, ItemStack tool) {
        if (drops == null) {
            // default calculation
            return Arrays.asList(new ItemStack(block.getType(), 1, block.getData()));
        } else {
            return Collections.unmodifiableList(drops);
        }
    }

    /**
     * Gets the sound that will be played when a player places the block.
     *
     * @return The sound to be played
     */
    public SoundInfo getPlaceSound() {
        return placeSound;
    }

    /**
     * Sets the sound that will be played when a player places the block.
     *
     * @param sound The sound.
     */
    public void setPlaceSound(Sound sound) {
        placeSound = new SoundInfo(sound, 1F, 0.75F);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Actions

    /**
     * Get the items that will be dropped as if the block would be successfully mined.
     * This is used f.e. to calculate TNT drops.
     *
     * @param block The block.
     * @return The drops from that block.
     */
    public Collection<ItemStack> getMinedDrops(GlowBlock block) {
        return getDrops(block, null);
    }

    /**
     * Create a new tile entity at the given location.
     *
     * @param chunk The chunk to create the tile entity at.
     * @param cx    The x coordinate in the chunk.
     * @param cy    The y coordinate in the chunk.
     * @param cz    The z coordinate in the chunk.
     * @return The new TileEntity, or null if no tile entity is used.
     */
    public TileEntity createTileEntity(GlowChunk chunk, int cx, int cy, int cz) {
        return null;
    }

    /**
     * Check whether the block can be placed at the given location.
     *
     * @param block   The location the block is being placed at.
     * @param against The face the block is being placed against.
     * @return Whether the placement is valid.
     */
    public boolean canPlaceAt(GlowBlock block, BlockFace against) {
        return true;
    }

    /**
     * Called when a block is placed to calculate what the block will become.
     *
     * @param player     the player who placed the block
     * @param state      the BlockState to edit
     * @param holding    the ItemStack that was being held
     * @param face       the face off which the block is being placed
     * @param clickedLoc where in the block the click occurred
     */
    public void placeBlock(GlowPlayer player, GlowBlockState state, BlockFace face, ItemStack holding, Vector clickedLoc) {
        state.setType(getMaterial());
        state.setRawData((byte) holding.getDurability());
    }

    /**
     * Called after a block has been placed by a player.
     *
     * @param player  the player who placed the block
     * @param block   the block that was placed
     * @param holding the the ItemStack that was being held
     * @param oldState The old block state before the block was placed.
     */
    public void afterPlace(GlowPlayer player, GlowBlock block, ItemStack holding, GlowBlockState oldState) {
        block.applyPhysics(oldState.getType(), block.getTypeId(), oldState.getRawData(), block.getData());
    }

    /**
     * Called when a player attempts to interact with (right-click) a block of
     * this type already in the world.
     *
     * @param player     the player interacting
     * @param block      the block interacted with
     * @param face       the clicked face
     * @param clickedLoc where in the block the click occurred
     * @return Whether the interaction occurred.
     */
    public boolean blockInteract(GlowPlayer player, GlowBlock block, BlockFace face, Vector clickedLoc) {
        return false;
    }

    /**
     * Called when a player attempts to destroy a block.
     *
     * @param player The player interacting
     * @param block  The block the player destroyed
     * @param face   The block face
     */
    public void blockDestroy(GlowPlayer player, GlowBlock block, BlockFace face) {
        // do nothing
    }

    /**
     * Called after a player successfully destroys a block.
     *
     * @param player The player interacting
     * @param block  The block the player destroyed
     * @param face   The block face
     * @param oldState The block state of the block the player destroyed.
     */
    public void afterDestroy(GlowPlayer player, GlowBlock block, BlockFace face, GlowBlockState oldState) {
        block.applyPhysics(oldState.getType(), block.getTypeId(), oldState.getRawData(), block.getData());
    }

    /**
     * Called when the BlockType gets pulsed as requested.
     *
     * @param block The block that was pulsed pulsed
     */
    public void receivePulse(GlowBlock block) {

    }

    /**
     * Called when a player attempts to place a block on an existing block of
     * this type. Used to determine if the placement should occur into the air
     * adjacent to the block (normal behavior), or absorbed into the block
     * clicked on.
     *
     * @param block   The block the player right-clicked
     * @param face    The face on which the click occurred
     * @param holding The ItemStack the player was holding
     * @return Whether the place should occur into the block given.
     */
    public boolean canAbsorb(GlowBlock block, BlockFace face, ItemStack holding) {
        return false;
    }

    /**
     * Called to check if this block can be overridden by a block place
     * which would occur inside it.
     *
     * @param block   The block being targeted by the placement
     * @param face    The face on which the click occurred
     * @param holding The ItemStack the player was holding
     * @return Whether this block can be overridden.
     */
    public boolean canOverride(GlowBlock block, BlockFace face, ItemStack holding) {
        return block.isLiquid();
    }

    /**
     * Called when a neighboring block (within a 3x3x3 cube) has changed its
     * type or data and physics checks should occur.
     *
     * @param block        The block to perform physics checks for
     * @param face         The BlockFace to the changed block, or null if unavailable
     * @param changedBlock The neighboring block that has changed
     * @param oldType      The old type of the changed block
     * @param oldData      The old data of the changed block
     * @param newType      The new type of the changed block
     * @param newData      The new data of the changed block
     */
    public void onNearBlockChanged(GlowBlock block, BlockFace face, GlowBlock changedBlock, Material oldType, byte oldData, Material newType, byte newData) {

    }

    /**
     * Called when this block has just changed to some other type. This is
     * called whenever {@link GlowBlock#setTypeIdAndData}, {@link GlowBlock#setType}
     * or {@link GlowBlock#setData} is called with physics enabled, and might
     * be called from plugins or other means of changing the block.
     *
     * @param block   The block that was changed
     * @param oldType The old Material
     * @param oldData The old data
     * @param newType The new Material
     * @param data    The new data
     */
    public void onBlockChanged(GlowBlock block, Material oldType, byte oldData, Material newType, byte data) {
        // do nothing
    }

    /**
     * Called when the BlockType should calculate the current physics.
     *
     * @param block The block
     */
    public void updatePhysics(GlowBlock block) {
        // do nothing
    }

    @Override
    public final void rightClickBlock(GlowPlayer player, GlowBlock against, BlockFace face, ItemStack holding, Vector clickedLoc) {
        GlowBlock target = against.getRelative(face);

        // prevent building above the height limit
        if (target.getLocation().getY() >= target.getWorld().getMaxHeight()) {
            player.sendMessage(ChatColor.RED + "The height limit for this world is " + target.getWorld().getMaxHeight() + " blocks");
            return;
        }

        // check whether the block clicked against should absorb the placement
        BlockType againstType = ItemTable.instance().getBlock(against.getTypeId());
        if (againstType.canAbsorb(against, face, holding)) {
            target = against;
        } else if (!target.isEmpty()) {
            // air can always be overridden
            BlockType targetType = ItemTable.instance().getBlock(target.getTypeId());
            if (!targetType.canOverride(target, face, holding)) {
                return;
            }
        }

        // call canBuild event
        boolean canBuild = canPlaceAt(target, face);
        BlockCanBuildEvent canBuildEvent = new BlockCanBuildEvent(target, getId(), canBuild);
        if (!EventFactory.callEvent(canBuildEvent).isBuildable()) {
            //revert(player, target);
            return;
        }

        // grab states and update block
        GlowBlockState oldState = target.getState(), newState = target.getState();
        placeBlock(player, newState, face, holding, clickedLoc);
        newState.update(true);

        // call blockPlace event
        BlockPlaceEvent event = new BlockPlaceEvent(target, oldState, against, holding, player, canBuild);
        EventFactory.callEvent(event);
        if (event.isCancelled() || !event.canBuild()) {
            oldState.update(true);
            return;
        }

        // play a sound effect
        getPlaceSound().play(target.getLocation());

        // do any after-place actions
        afterPlace(player, target, holding, oldState);

        // deduct from stack if not in creative mode
        if (player.getGameMode() != GameMode.CREATIVE) {
            holding.setAmount(holding.getAmount() - 1);
        }
    }

    /**
     * Called to check if this block can perform random tick updates.
     *
     * @return Whether this block updates on tick.
     */
    public boolean canTickRandomly() {
        return false;
    }

    /**
     * Called when this block needs to be updated.
     *
     * @param block The block that needs an update
     */
    public void updateBlock(GlowBlock block) {
        // do nothing
    }

    ////////////////////////////////////////////////////////////////////////////
    // Helper methods

    /**
     * Called when a player left clicks a block
     *
     * @param player  the player who clicked the block
     * @param block   the block that was clicked
     * @param holding the ItemStack that was being held
     */
    public void leftClickBlock(GlowPlayer player, GlowBlock block, ItemStack holding) {
        // do nothing
    }

    /**
     * Display the warning for finding the wrong MaterialData subclass.
     *
     * @param clazz The expected subclass of MaterialData.
     * @param data  The actual MaterialData found.
     */
    protected void warnMaterialData(Class<?> clazz, MaterialData data) {
        GlowServer.logger.warning("Wrong MaterialData for " + getMaterial() + " (" + getClass().getSimpleName() + "): expected " + clazz.getSimpleName() + ", got " + data);
    }

    public void onRedstoneUpdate(GlowBlock block) {
        // do nothing
    }
}
