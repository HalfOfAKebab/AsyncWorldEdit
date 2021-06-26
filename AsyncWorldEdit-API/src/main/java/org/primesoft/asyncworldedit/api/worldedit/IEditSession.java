/*
 * AsyncWorldEdit API
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit API contributors
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution,
 * 3. Redistributions of source code, with or without modification, in any form 
 *    other then free of charge is not allowed,
 * 4. Redistributions in binary form in any form other then free of charge is 
 *    not allowed.
 * 5. Any derived work based on or containing parts of this software must reproduce 
 *    the above copyright notice, this list of conditions and the following 
 *    disclaimer in the documentation and/or other materials provided with the 
 *    derived work.
 * 6. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 7. The original author of the software is allowed to sublicense the software 
 *    or its parts using any license terms he sees fit.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.primesoft.asyncworldedit.api.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSession.Stage;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.extent.world.SurvivalModeExtent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 *
 * @author SBPrime
 */
public interface IEditSession extends Extent {
    
    /**
     * Turns on specific features for a normal WorldEdit session, such as
     * {@link #enableQueue() queuing} and {@link #setBatchingChunks(boolean)
     * chunk batching}.
     */
    void enableStandardMode();
    
    /**
     * Get the world.
     *
     * @return the world
     */
    World getWorld();

    /**
     * Get the underlying {@link ChangeSet}.
     *
     * @return the change set
     */
    ChangeSet getChangeSet();

    /**
     * Get the maximum number of blocks that can be changed. -1 will be returned
     * if it the limit disabled.
     *
     * @return the limit (&gt;= 0) or -1 for no limit
     */
    int getBlockChangeLimit();

    /**
     * Set the maximum number of blocks that can be changed.
     *
     * @param limit the limit (&gt;= 0) or -1 for no limit
     */
    public void setBlockChangeLimit(int limit);

    /**
     * Returns queue status.
     *
     * @return whether the queue is enabled
     */
    public boolean isQueueEnabled();

    /**
     * Queue certain types of block for better reproduction of those blocks.
     */
    public void enableQueue();

    /**
     * Disable the queue. This will flush the queue.
     */
    public void disableQueue();

    /**
     * Get the mask.
     *
     * @return mask, may be null
     */
    public Mask getMask();

    /**
     * Set a mask.
     *
     * @param mask mask or null
     */
    public void setMask(Mask mask);

    /**
     * Get the {@link SurvivalModeExtent}.
     *
     * @return the survival simulation extent
     */
    public SurvivalModeExtent getSurvivalExtent();

    /**
     * Set whether fast mode is enabled.
     *
     * <p>Fast mode may skip lighting checks or adjacent block
     * notification.</p>
     *
     * @param enabled true to enable
     */
    public void setFastMode(boolean enabled);

    /**
     * Return fast mode status.
     *
     * <p>Fast mode may skip lighting checks or adjacent block
     * notification.</p>
     *
     * @return true if enabled
     */
    public boolean hasFastMode();

    /**
     * Get the {@link BlockBag} is used.
     *
     * @return a block bag or null
     */
    public BlockBag getBlockBag();

    /**
     * Set a {@link BlockBag} to use.
     *
     * @param blockBag the block bag to set, or null to use none
     */
    public void setBlockBag(BlockBag blockBag);

    /**
     * Gets the list of missing blocks and clears the list for the next
     * operation.
     *
     * @return a map of missing blocks
     */
    public Map<BlockType, Integer> popMissingBlocks();

      /**
     * Returns chunk batching status.
     *
     * @return whether chunk batching is enabled
     */
    public boolean isBatchingChunks();

    /**
     * Enable or disable chunk batching. Disabling will
     * {@linkplain #flushSession() flush the session}.
     *
     * @param batchingChunks {@code true} to enable, {@code false} to disable
     */
    public void setBatchingChunks(boolean batchingChunks);

    /**
     * Disable all buffering extents.
     *
     * @see #disableQueue()
     * @see #setBatchingChunks(boolean)
     */
    public void disableBuffering();
    
    /**
     * Get the number of blocks changed, including repeated block changes.
     *
     * <p>This number may not be accurate.</p>
     *
     * @return the number of block changes
     */
    public int getBlockChangeCount();

    @Override
    public BiomeType getBiome(BlockVector3 position);

    @Override
    public boolean setBiome(BlockVector3 position, BiomeType biome);

    @Override    
    public BlockState getBlock(BlockVector3 vector);

    @Override
    public BaseBlock getFullBlock(BlockVector3 vector);

    /**
     * Returns the highest solid 'terrain' block which can occur naturally.
     *
     * @param x the X coordinate
     * @param z the Z cooridnate
     * @param minY minimal height
     * @param maxY maximal height
     * @return height of highest block found or 'minY'
     */
    public int getHighestTerrainBlock(int x, int z, int minY, int maxY);

    
    /**
     * Set a block, bypassing both history and block re-ordering.
     *
     * @param <B>
     * @param position the position to set the block at
     * @param block the block
     * @param stage the level
     * @return whether the block changed
     * @throws WorldEditException thrown on a set error
     */
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, Stage stage) throws WorldEditException;

    /**
     * Set a block, bypassing both history and block re-ordering.
     *
     * @param <B>
     * @param position the position to set the block at
     * @param block the block
     * @return whether the block changed
     */
    public <B extends BlockStateHolder<B>> boolean rawSetBlock(BlockVector3 position, B block);

    /**
     * Set a block, bypassing history but still utilizing block re-ordering.
     *
     * @param <B>
     * @param position the position to set the block at
     * @param block the block
     * @return whether the block changed
     */
    public <B extends BlockStateHolder<B>> boolean smartSetBlock(BlockVector3 position, B block);

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block) throws MaxChangedBlocksException;

    /**
     * Sets the block at a position, subject to both history and block re-ordering.
     *
     * @param position the position
     * @param pattern a pattern to use
     * @return Whether the block changed -- not entirely dependable
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    @SuppressWarnings("deprecation")
    public boolean setBlock(BlockVector3 position, Pattern pattern) throws MaxChangedBlocksException;

    @Override
    @Nullable
    public Entity createEntity(com.sk89q.worldedit.util.Location location, BaseEntity entity);

    /**
     * Restores all blocks to their initial state.
     *
     * @param editSession a new {@link EditSession} to perform the undo in
     */
    public void undo(EditSession editSession);

    /**
     * Sets to new state.
     *
     * @param editSession a new {@link EditSession} to perform the redo in
     */
    public void redo(EditSession editSession);

    /**
     * Get the number of changed blocks.
     *
     * @return the number of changes
     */
    public int size();

    @Override
    public BlockVector3 getMinimumPoint();

    @Override
    public BlockVector3 getMaximumPoint();

    @Override
    public List<? extends Entity> getEntities(Region region);

    @Override
    public List<? extends Entity> getEntities();

    /**
     * Closing an EditSession {@linkplain #flushSession() flushes its buffers}.
     */
    public void close();
    
    /**
     * Communicate to the EditSession that all block changes are complete,
     * and that it should apply them to the world.
     */
    public void flushSession();

    @Override
    public @Nullable Operation commit();

    /**
     * Count the number of blocks of a list of types in a region.
     *
     * @param region the region
     * @param searchBlocks the list of blocks to search
     * @return the number of blocks that matched the pattern
     */
    public int countBlocks(Region region, Set<BaseBlock> searchBlocks);

    /**
     * Fills an area recursively in the X/Z directions.
     *
     * @param <B>
     * @param origin the location to start from
     * @param block the block to fill with
     * @param radius the radius of the spherical area to fill
     * @param depth the maximum depth, starting from the origin
     * @param recursive whether a breadth-first search should be performed
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public <B extends BlockStateHolder<B>> int fillXZ(BlockVector3 origin, B block, double radius, int depth, boolean recursive) throws MaxChangedBlocksException;

    /**
     * Fills an area recursively in the X/Z directions.
     *
     * @param origin the origin to start the fill from
     * @param pattern the pattern to fill with
     * @param radius the radius of the spherical area to fill, with 0 as the smallest radius
     * @param depth the maximum depth, starting from the origin, with 1 as the smallest depth
     * @param recursive whether a breadth-first search should be performed
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int fillXZ(BlockVector3 origin, Pattern pattern, double radius, int depth, boolean recursive) throws MaxChangedBlocksException;
    
    /**
     * Remove a cuboid above the given position with a given apothem and a given height.
     *
     * @param position base position
     * @param apothem an apothem of the cuboid (on the XZ plane), where the minimum is 1
     * @param height the height of the cuboid, where the minimum is 1
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int removeAbove(BlockVector3 position, int apothem, int height) throws MaxChangedBlocksException;

    /**
     * Remove a cuboid below the given position with a given apothem and a given height.
     *
     * @param position base position
     * @param apothem an apothem of the cuboid (on the XZ plane), where the minimum is 1
     * @param height the height of the cuboid, where the minimum is 1
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int removeBelow(BlockVector3 position, int apothem, int height) throws MaxChangedBlocksException;

    /**
     * Remove blocks of a certain type nearby a given position.
     *
     * @param position center position of cuboid
     * @param mask
     * @param apothem an apothem of the cuboid, where the minimum is 1
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int removeNear(BlockVector3 position, Mask mask, int apothem) throws MaxChangedBlocksException;

    /**
     * Sets all the blocks inside a region to a given block type.
     *
     * @param <B>
     * @param region the region
     * @param block the block
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public <B extends BlockStateHolder<B>> int setBlocks(Region region, B block) throws MaxChangedBlocksException;

    /**
     * Sets all the blocks inside a region to a given pattern.
     *
     * @param region the region
     * @param pattern the pattern that provides the replacement block
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int setBlocks(Region region, Pattern pattern) throws MaxChangedBlocksException;

    /**
     * Replaces all the blocks matching a given filter, within a given region, to a block
     * returned by a given pattern.
     *
     * @param <B>
     * @param region the region to replace the blocks within
     * @param filter a list of block types to match, or null to use {@link com.sk89q.worldedit.function.mask.ExistingBlockMask}
     * @param replacement the replacement block
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public <B extends BlockStateHolder<B>> int replaceBlocks(Region region, Set<BaseBlock> filter, B replacement) throws MaxChangedBlocksException;

    /**
     * Replaces all the blocks matching a given filter, within a given region, to a block
     * returned by a given pattern.
     *
     * @param region the region to replace the blocks within
     * @param filter a list of block types to match, or null to use {@link com.sk89q.worldedit.function.mask.ExistingBlockMask}
     * @param pattern the pattern that provides the new blocks
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int replaceBlocks(Region region, Set<BaseBlock> filter, Pattern pattern) throws MaxChangedBlocksException;

    /**
     * Replaces all the blocks matching a given mask, within a given region, to a block
     * returned by a given pattern.
     *
     * @param region the region to replace the blocks within
     * @param mask the mask that blocks must match
     * @param pattern the pattern that provides the new blocks
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int replaceBlocks(Region region, Mask mask, Pattern pattern) throws MaxChangedBlocksException;

    /**
     * Sets the blocks at the center of the given region to the given pattern.
     * If the center sits between two blocks on a certain axis, then two blocks
     * will be placed to mark the center.
     *
     * @param region the region to find the center of
     * @param pattern the replacement pattern
     * @return the number of blocks placed
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int center(Region region, Pattern pattern) throws MaxChangedBlocksException;

    /**
     * Make the faces of the given region as if it was a {@link CuboidRegion}.
     *
     * @param region the region
     * @param block the block to place
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public <B extends BlockStateHolder<B>> int makeCuboidFaces(Region region, B block) throws MaxChangedBlocksException;

    /**
     * Make the faces of the given region as if it was a {@link CuboidRegion}.
     *
     * @param region the region
     * @param pattern the pattern to place
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeCuboidFaces(Region region, Pattern pattern) throws MaxChangedBlocksException;

    /**
     * Make the faces of the given region. The method by which the faces are found
     * may be inefficient, because there may not be an efficient implementation supported
     * for that specific shape.
     *
     * @param region the region
     * @param pattern the pattern to place
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeFaces(final Region region, Pattern pattern) throws MaxChangedBlocksException;

    /**
     * Make the walls (all faces but those parallel to the X-Z plane) of the given region
     * as if it was a {@link CuboidRegion}.
     *
     * @param <B>
     * @param region the region
     * @param block the block to place
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public <B extends BlockStateHolder<B>> int makeCuboidWalls(Region region, B block) throws MaxChangedBlocksException;

    /**
     * Make the walls (all faces but those parallel to the X-Z plane) of the given region
     * as if it was a {@link CuboidRegion}.
     *
     * @param region the region
     * @param pattern the pattern to place
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeCuboidWalls(Region region, Pattern pattern) throws MaxChangedBlocksException;

    /**
     * Make the walls of the given region. The method by which the walls are found
     * may be inefficient, because there may not be an efficient implementation supported
     * for that specific shape.
     *
     * @param region the region
     * @param pattern the pattern to place
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeWalls(final Region region, Pattern pattern) throws MaxChangedBlocksException;

    /**
     * Places a layer of blocks on top of ground blocks in the given region
     * (as if it were a cuboid).
     *
     * @param <B>
     * @param region the region
     * @param block the placed block
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public <B extends BlockStateHolder<B>> int overlayCuboidBlocks(Region region, B block) throws MaxChangedBlocksException;

    /**
     * Places a layer of blocks on top of ground blocks in the given region
     * (as if it were a cuboid).
     *
     * @param region the region
     * @param pattern the placed block pattern
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int overlayCuboidBlocks(Region region, Pattern pattern) throws MaxChangedBlocksException;

    /**
     * Turns the first 3 layers into dirt/grass and the bottom layers
     * into rock, like a natural Minecraft mountain.
     *
     * @param region the region to affect
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int naturalizeCuboidBlocks(Region region) throws MaxChangedBlocksException;

    /**
     * Stack a cuboid region.
     *
     * @param region the region to stack
     * @param dir the direction to stack
     * @param count the number of times to stack
     * @param copyAir true to also copy air blocks
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int stackCuboidRegion(Region region, BlockVector3 dir, int count, boolean copyAir) throws MaxChangedBlocksException;

    /**
     * Move the blocks in a region a certain direction.
     *
     * @param region the region to move
     * @param dir the direction
     * @param distance the distance to move
     * @param copyAir true to copy air blocks
     * @param replacement the replacement block to fill in after moving, or null to use air
     * @return number of blocks moved
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int moveRegion(Region region, BlockVector3 dir, int distance, boolean copyAir, Pattern replacement) throws MaxChangedBlocksException;

    /**
     * Move the blocks in a region a certain direction.
     *
     * @param region the region to move
     * @param dir the direction
     * @param distance the distance to move
     * @param copyAir true to copy air blocks
     * @param replacement the replacement block to fill in after moving, or null to use air
     * @return number of blocks moved
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int moveCuboidRegion(Region region, BlockVector3 dir, int distance, boolean copyAir, Pattern replacement) throws MaxChangedBlocksException;

    /**
     * Drain nearby pools of water or lava.
     *
     * @param origin the origin to drain from, which will search a 3x3 area
     * @param radius the radius of the removal, where a value should be 0 or greater
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int drainArea(BlockVector3 origin, double radius) throws MaxChangedBlocksException;

    /**
     * Fix liquids so that they turn into stationary blocks and extend outward.
     *
     * @param origin the original position
     * @param radius the radius to fix
     * @param fluid the type of the fluid
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int fixLiquid(BlockVector3 origin, double radius, BlockType fluid) throws MaxChangedBlocksException;

    /**
     * Makes a cylinder.
     *
     * @param pos Center of the cylinder
     * @param block The block pattern to use
     * @param radius The cylinder's radius
     * @param height The cylinder's up/down extent. If negative, extend downward.
     * @param filled If false, only a shell will be generated.
     * @return number of blocks changed
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeCylinder(BlockVector3 pos, Pattern block, double radius, int height, boolean filled) throws MaxChangedBlocksException;

    /**
     * Makes a cylinder.
     *
     * @param pos Center of the cylinder
     * @param block The block pattern to use
     * @param radiusX The cylinder's largest north/south extent
     * @param radiusZ The cylinder's largest east/west extent
     * @param height The cylinder's up/down extent. If negative, extend downward.
     * @param filled If false, only a shell will be generated.
     * @return number of blocks changed
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeCylinder(BlockVector3 pos, Pattern block, double radiusX, double radiusZ, int height, boolean filled) throws MaxChangedBlocksException;

    /**
    * Makes a sphere.
    *
    * @param pos Center of the sphere or ellipsoid
    * @param block The block pattern to use
    * @param radius The sphere's radius
    * @param filled If false, only a shell will be generated.
    * @return number of blocks changed
    * @throws MaxChangedBlocksException thrown if too many blocks are changed
    */
    public int makeSphere(BlockVector3 pos, Pattern block, double radius, boolean filled) throws MaxChangedBlocksException;

    /**
     * Makes a sphere or ellipsoid.
     *
     * @param pos Center of the sphere or ellipsoid
     * @param block The block pattern to use
     * @param radiusX The sphere/ellipsoid's largest north/south extent
     * @param radiusY The sphere/ellipsoid's largest up/down extent
     * @param radiusZ The sphere/ellipsoid's largest east/west extent
     * @param filled If false, only a shell will be generated.
     * @return number of blocks changed
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeSphere(BlockVector3 pos, Pattern block, double radiusX, double radiusY, double radiusZ, boolean filled) throws MaxChangedBlocksException;

    /**
     * Makes a pyramid.
     *
     * @param position a position
     * @param block a block
     * @param size size of pyramid
     * @param filled true if filled
     * @return number of blocks changed
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makePyramid(BlockVector3 position, Pattern block, int size, boolean filled) throws MaxChangedBlocksException;

    /**
     * Thaw blocks in a radius.
     *
     * @param position the position
     * @param radius the radius
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int thaw(BlockVector3 position, double radius)
            throws MaxChangedBlocksException;

    /**
     * Make snow in a radius.
     *
     * @param position a position
     * @param radius a radius
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int simulateSnow(BlockVector3 position, double radius) throws MaxChangedBlocksException;

    /**
     * Make dirt green.
     *
     * @param position a position
     * @param radius a radius
     * @param onlyNormalDirt only affect normal dirt (data value 0)
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int green(BlockVector3 position, double radius, boolean onlyNormalDirt)
            throws MaxChangedBlocksException;

    /**
     * Makes pumpkin patches randomly in an area around the given position.
     *
     * @param position the base position
     * @param apothem the apothem of the (square) area
     * @return number of patches created
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makePumpkinPatches(BlockVector3 position, int apothem) throws MaxChangedBlocksException;

    /**
     * Makes a forest.
     *
     * @param basePosition a position
     * @param size a size
     * @param density between 0 and 1, inclusive
     * @param treeType the tree type
     * @return number of trees created
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeForest(BlockVector3 basePosition, int size, double density, TreeGenerator.TreeType treeType) throws MaxChangedBlocksException;

    /**
     * Get the block distribution inside a region.
     *
     * @param region a region
     * @param separateStates
     * @return the results
     */
    public List<Countable<BlockState>> getBlockDistribution(Region region, boolean separateStates);

    public int makeShape(final Region region, final Vector3 zero, final Vector3 unit, final Pattern pattern, final String expressionString, final boolean hollow) throws ExpressionException, MaxChangedBlocksException;

    public int deformRegion(final Region region, final Vector3 zero, final Vector3 unit, final String expressionString) throws ExpressionException, MaxChangedBlocksException;

    /**
     * Hollows out the region (Semi-well-defined for non-cuboid selections).
     *
     * @param region the region to hollow out.
     * @param thickness the thickness of the shell to leave (manhattan distance)
     * @param pattern The block pattern to use
     *
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int hollowOutRegion(Region region, int thickness, Pattern pattern) throws MaxChangedBlocksException;

    /**
     * Draws a line (out of blocks) between two vectors.
     *
     * @param pattern The block pattern used to draw the line.
     * @param pos1 One of the points that define the line.
     * @param pos2 The other point that defines the line.
     * @param radius The radius (thickness) of the line.
     * @param filled If false, only a shell will be generated.
     *
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int drawLine(Pattern pattern, BlockVector3 pos1, BlockVector3 pos2, double radius, boolean filled)
            throws MaxChangedBlocksException;

    /**
     * Draws a spline (out of blocks) between specified vectors.
     *
     * @param pattern The block pattern used to draw the spline.
     * @param nodevectors The list of vectors to draw through.
     * @param tension The tension of every node.
     * @param bias The bias of every node.
     * @param continuity The continuity of every node.
     * @param quality The quality of the spline. Must be greater than 0.
     * @param radius The radius (thickness) of the spline.
     * @param filled If false, only a shell will be generated.
     *
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int drawSpline(Pattern pattern, List<BlockVector3> nodevectors, double tension, double bias, double continuity, double quality, double radius, boolean filled)
            throws MaxChangedBlocksException;

    public int makeBiomeShape(final Region region, final Vector3 zero, final Vector3 unit, final BiomeType biomeType, final String expressionString, final boolean hollow) throws ExpressionException, MaxChangedBlocksException;

    SideEffectSet getSideEffectApplier();

    void setSideEffectApplier(SideEffectSet sideEffectSet);

}