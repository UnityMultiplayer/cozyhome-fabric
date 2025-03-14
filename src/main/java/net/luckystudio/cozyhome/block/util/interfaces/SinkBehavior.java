package net.luckystudio.cozyhome.block.util.interfaces;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.luckystudio.cozyhome.block.custom.counters.SinkCounterBlock;
import net.luckystudio.cozyhome.block.util.ModProperties;
import net.minecraft.block.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.Map;
import java.util.function.Predicate;

public interface SinkBehavior {
    Map<String, SinkBehavior.SinkBehaviorMap> BEHAVIOR_MAPS = new Object2ObjectArrayMap<>();
    Codec<SinkBehavior.SinkBehaviorMap> CODEC = Codec.stringResolver(SinkBehavior.SinkBehaviorMap::name, BEHAVIOR_MAPS::get);
    /**
     * The sink behaviors for water sinks.
     *
     * @see #createMap
     */
    SinkBehavior.SinkBehaviorMap BASE = createMap("base");
    /**
     * A behavior that fills sinks with water.
     *
     * @see #fillSink
     */
    SinkBehavior FILL_WITH_WATER = (state, world, pos, player, hand, stack) -> 
            fillSink(world, pos, player, hand, stack, state.with(ModProperties.FILLED_LEVEL_0_3, 3), SoundEvents.ITEM_BUCKET_EMPTY);
    /**
     * A behavior that cleans dyed shulker boxes.
     */
    SinkBehavior CLEAN_SHULKER_BOX = (state, world, pos, player, hand, stack) -> {
        Block block = Block.getBlockFromItem(stack.getItem());
        if (!(block instanceof ShulkerBoxBlock)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        } else {
            if (!world.isClient) {
                ItemStack itemStack = stack.copyComponentsToNewStack(Blocks.SHULKER_BOX, 1);
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, itemStack, false));
                player.incrementStat(Stats.CLEAN_SHULKER_BOX);
                SinkCounterBlock.decrementFluidLevel(state, world, pos);
            }

            return ItemActionResult.success(world.isClient);
        }
    };
    /**
     * A behavior that cleans banners with patterns.
     */
    SinkBehavior CLEAN_BANNER = (state, world, pos, player, hand, stack) -> {
        BannerPatternsComponent bannerPatternsComponent = stack.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT);
        if (bannerPatternsComponent.layers().isEmpty()) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        } else {
            if (!world.isClient) {
                ItemStack itemStack = stack.copyWithCount(1);
                itemStack.set(DataComponentTypes.BANNER_PATTERNS, bannerPatternsComponent.withoutTopLayer());
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, itemStack, false));
                player.incrementStat(Stats.CLEAN_BANNER);
                SinkCounterBlock.decrementFluidLevel(state, world, pos);
            }

            return ItemActionResult.success(world.isClient);
        }
    };

    /**
     * A behavior that cleans dyeable items.
     */
    SinkBehavior CLEAN_DYEABLE_ITEM = (state, world, pos, player, hand, stack) -> {
        if (!stack.isIn(ItemTags.DYEABLE)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        } else if (!stack.contains(DataComponentTypes.DYED_COLOR)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        } else {
            if (!world.isClient) {
                stack.remove(DataComponentTypes.DYED_COLOR);
                player.incrementStat(Stats.CLEAN_ARMOR);
                SinkCounterBlock.decrementFluidLevel(state, world, pos);
            }

            return ItemActionResult.success(world.isClient);
        }
    };

    /**
     * Creates a mutable map from {@linkplain Item items} to their
     * corresponding cauldron behaviors.
     *
     * <p>The default return value in the map is a cauldron behavior
     * that returns {@link ItemActionResult#PASS_TO_DEFAULT_BLOCK_INTERACTION} for all items.
     *
     * @return the created map
     */
    static SinkBehavior.SinkBehaviorMap createMap(String name) {
        Object2ObjectOpenHashMap<Item, SinkBehavior> object2ObjectOpenHashMap = new Object2ObjectOpenHashMap<>();
        object2ObjectOpenHashMap.defaultReturnValue((state, world, pos, player, hand, stack) -> ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
        SinkBehavior.SinkBehaviorMap sinkBehaviorMap = new SinkBehavior.SinkBehaviorMap(name, object2ObjectOpenHashMap);
        BEHAVIOR_MAPS.put(name, sinkBehaviorMap);
        return sinkBehaviorMap;
    }

    /**
     * Called when a player interacts with a cauldron.
     *
     * @return a {@linkplain ItemActionResult#isAccepted successful} action result if this behavior succeeds,
     * {@link ItemActionResult#PASS_TO_DEFAULT_BLOCK_INTERACTION} otherwise
     *
     * @param pos the cauldron's position
     * @param player the interacting player
     * @param hand the hand interacting with the cauldron
     * @param stack the stack in the player's hand
     * @param state the current cauldron block state
     * @param world the world where the cauldron is located
     */
    ItemActionResult interact(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack);

    /**
     * Registers the vanilla cauldron behaviors.
     */
    static void registerBehavior() {
        Map<Item, SinkBehavior> map2 = BASE.map();
        registerBucketBehavior(map2);
        map2.put(
                Items.BUCKET,
                (state, world, pos, player, hand, stack) -> emptyCauldron(
                        state,
                        world,
                        pos,
                        player,
                        hand,
                        stack,
                        new ItemStack(Items.WATER_BUCKET),
                        statex -> statex.get(ModProperties.FILLED_LEVEL_0_3) == 3,
                        SoundEvents.ITEM_BUCKET_FILL
                )
        );
        map2.put(Items.GLASS_BOTTLE, (state, world, pos, player, hand, stack) -> {
            if (!world.isClient) {
                Item item = stack.getItem();
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, PotionContentsComponent.createStack(Items.POTION, Potions.WATER)));
                player.incrementStat(Stats.USE_CAULDRON);
                player.incrementStat(Stats.USED.getOrCreateStat(item));
                SinkCounterBlock.decrementFluidLevel(state, world, pos);
                world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }

            return ItemActionResult.success(world.isClient);
        });
        map2.put(Items.POTION, (state, world, pos, player, hand, stack) -> {
            if (state.get(ModProperties.FILLED_LEVEL_0_3) == 3) {
                return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            } else {
                PotionContentsComponent potionContentsComponent = stack.get(DataComponentTypes.POTION_CONTENTS);
                if (potionContentsComponent != null && potionContentsComponent.matches(Potions.WATER)) {
                    if (!world.isClient) {
                        player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                        player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
                        SinkCounterBlock.incrementFluidLevel(state, world, pos);
                        world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
                    }
                    return ItemActionResult.success(world.isClient);
                } else {
                    return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }
            }
        });
        map2.put(Items.LEATHER_BOOTS, CLEAN_DYEABLE_ITEM);
        map2.put(Items.LEATHER_LEGGINGS, CLEAN_DYEABLE_ITEM);
        map2.put(Items.LEATHER_CHESTPLATE, CLEAN_DYEABLE_ITEM);
        map2.put(Items.LEATHER_HELMET, CLEAN_DYEABLE_ITEM);
        map2.put(Items.LEATHER_HORSE_ARMOR, CLEAN_DYEABLE_ITEM);
        map2.put(Items.WOLF_ARMOR, CLEAN_DYEABLE_ITEM);
        map2.put(Items.WHITE_BANNER, CLEAN_BANNER);
        map2.put(Items.GRAY_BANNER, CLEAN_BANNER);
        map2.put(Items.BLACK_BANNER, CLEAN_BANNER);
        map2.put(Items.BLUE_BANNER, CLEAN_BANNER);
        map2.put(Items.BROWN_BANNER, CLEAN_BANNER);
        map2.put(Items.CYAN_BANNER, CLEAN_BANNER);
        map2.put(Items.GREEN_BANNER, CLEAN_BANNER);
        map2.put(Items.LIGHT_BLUE_BANNER, CLEAN_BANNER);
        map2.put(Items.LIGHT_GRAY_BANNER, CLEAN_BANNER);
        map2.put(Items.LIME_BANNER, CLEAN_BANNER);
        map2.put(Items.MAGENTA_BANNER, CLEAN_BANNER);
        map2.put(Items.ORANGE_BANNER, CLEAN_BANNER);
        map2.put(Items.PINK_BANNER, CLEAN_BANNER);
        map2.put(Items.PURPLE_BANNER, CLEAN_BANNER);
        map2.put(Items.RED_BANNER, CLEAN_BANNER);
        map2.put(Items.YELLOW_BANNER, CLEAN_BANNER);
        map2.put(Items.WHITE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.GRAY_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.BLACK_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.BLUE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.BROWN_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.CYAN_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.GREEN_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.LIGHT_BLUE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.LIGHT_GRAY_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.LIME_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.MAGENTA_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.ORANGE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.PINK_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.PURPLE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.RED_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.YELLOW_SHULKER_BOX, CLEAN_SHULKER_BOX);
    }

    /**
     * Registers the behavior for filled buckets in the specified behavior map.
     */
    static void registerBucketBehavior(Map<Item, SinkBehavior> behavior) {
        behavior.put(Items.WATER_BUCKET, FILL_WITH_WATER);
    }

    /**
     * Empties a cauldron if it's full.
     *
     * @return a {@linkplain ItemActionResult#isAccepted successful} action result if emptied, {@link ItemActionResult#PASS_TO_DEFAULT_BLOCK_INTERACTION} otherwise
     *
     * @param output the item stack that replaces the interaction stack when the cauldron is emptied
     * @param fullPredicate a predicate used to check if the cauldron can be emptied into the output stack
     * @param hand the hand interacting with the cauldron
     * @param stack the stack in the player's hand
     * @param soundEvent the sound produced by emptying
     * @param pos the cauldron's position
     * @param player the interacting player
     * @param state the cauldron block state
     * @param world the world where the cauldron is located
     */
    static ItemActionResult emptyCauldron(
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            Hand hand,
            ItemStack stack,
            ItemStack output,
            Predicate<BlockState> fullPredicate,
            SoundEvent soundEvent
    ) {
        if (!fullPredicate.test(state)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        } else {
            if (!world.isClient) {
                Item item = stack.getItem();
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, output));
                player.incrementStat(Stats.USE_CAULDRON);
                player.incrementStat(Stats.USED.getOrCreateStat(item));
                world.setBlockState(pos, state.with(ModProperties.FILLED_LEVEL_0_3, 0));
                world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }

            return ItemActionResult.success(world.isClient);
        }
    }

    /**
     * Fills a cauldron from a bucket stack.
     *
     * <p>The filled bucket stack will be replaced by an empty bucket in the player's
     * inventory.
     *
     * @return a {@linkplain ItemActionResult#isAccepted successful} action result
     *
     * @param soundEvent the sound produced by filling
     * @param state the filled cauldron state
     * @param stack the filled bucket stack in the player's hand
     * @param hand the hand interacting with the cauldron
     * @param player the interacting player
     * @param pos the cauldron's position
     * @param world the world where the cauldron is located
     */
    static ItemActionResult fillSink(World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack, BlockState state, SoundEvent soundEvent) {
        if (!world.isClient) {
            Item item = stack.getItem();
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.BUCKET)));
            player.incrementStat(Stats.FILL_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(item));
            world.setBlockState(pos, state);
            world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
        }

        return ItemActionResult.success(world.isClient);
    }

    record SinkBehaviorMap(String name, Map<Item, SinkBehavior> map) {
    }
}
