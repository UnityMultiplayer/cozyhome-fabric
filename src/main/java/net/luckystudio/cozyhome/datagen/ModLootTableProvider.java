package net.luckystudio.cozyhome.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.luckystudio.cozyhome.block.ModBlocks;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends FabricBlockLootTableProvider {
    public ModLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        // Call its self to drop its self
        addDrop(ModBlocks.OAK_PLANKED_WALL);
        addDrop(ModBlocks.SPRUCE_PLANKED_WALL);
        addDrop(ModBlocks.BIRCH_PLANKED_WALL);
        addDrop(ModBlocks.JUNGLE_PLANKED_WALL);
        addDrop(ModBlocks.ACACIA_PLANKED_WALL);
        addDrop(ModBlocks.DARK_OAK_PLANKED_WALL);
        addDrop(ModBlocks.MANGROVE_PLANKED_WALL);
        addDrop(ModBlocks.CHERRY_PLANKED_WALL);
        addDrop(ModBlocks.BAMBOO_PLANKED_WALL);
        addDrop(ModBlocks.CRIMSON_PLANKED_WALL);
        addDrop(ModBlocks.WARPED_PLANKED_WALL);

        addDrop(ModBlocks.OAK_COUNTER);
        addDrop(ModBlocks.SPRUCE_COUNTER);
        addDrop(ModBlocks.BIRCH_COUNTER);
        addDrop(ModBlocks.JUNGLE_COUNTER);
        addDrop(ModBlocks.ACACIA_COUNTER);
        addDrop(ModBlocks.DARK_OAK_COUNTER);
        addDrop(ModBlocks.MANGROVE_COUNTER);
        addDrop(ModBlocks.CHERRY_COUNTER);
        addDrop(ModBlocks.BAMBOO_COUNTER);
        addDrop(ModBlocks.CRIMSON_COUNTER);
        addDrop(ModBlocks.WARPED_COUNTER);

        addDrop(ModBlocks.WHITE_LAMP);
        addDrop(ModBlocks.ORANGE_LAMP);
        addDrop(ModBlocks.MAGENTA_LAMP);
        addDrop(ModBlocks.LIGHT_BLUE_LAMP);
        addDrop(ModBlocks.YELLOW_LAMP);
        addDrop(ModBlocks.LIME_LAMP);
        addDrop(ModBlocks.PINK_LAMP);
        addDrop(ModBlocks.GRAY_LAMP);
        addDrop(ModBlocks.PURPLE_LAMP);
        addDrop(ModBlocks.BLUE_LAMP);
        addDrop(ModBlocks.BROWN_LAMP);
        addDrop(ModBlocks.GREEN_LAMP);
        addDrop(ModBlocks.RED_LAMP);
        addDrop(ModBlocks.BLACK_LAMP);
    }
}
