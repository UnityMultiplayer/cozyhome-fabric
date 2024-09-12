package net.luckystudio.cozyhome.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.luckystudio.cozyhome.CozyHome;
import net.luckystudio.cozyhome.block.ModBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;

public class ModItemGroups {

    public static final ItemGroup COZY_HOME = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(CozyHome.MOD_ID, "cozyhome"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModBlocks.OAK_PLANKED_WALL))
                    .displayName(Text.translatable("itemgroup.cozyhome.cozyhome"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModBlocks.OAK_PLANKED_WALL);
                        entries.add(ModBlocks.SPRUCE_PLANKED_WALL);

                        entries.add(ModBlocks.WHITE_LAMP);
                        entries.add(ModBlocks.ORANGE_LAMP);
                        entries.add(ModBlocks.MAGENTA_LAMP);
                        entries.add(ModBlocks.LIGHT_BLUE_LAMP);
                        entries.add(ModBlocks.YELLOW_LAMP);
                        entries.add(ModBlocks.LIME_LAMP);
                        entries.add(ModBlocks.PINK_LAMP);
                        entries.add(ModBlocks.GRAY_LAMP);
                        entries.add(ModBlocks.LIGHT_GRAY_LAMP);
                        entries.add(ModBlocks.CYAN_LAMP);
                        entries.add(ModBlocks.PURPLE_LAMP);
                        entries.add(ModBlocks.BLUE_LAMP);
                        entries.add(ModBlocks.BROWN_LAMP);
                        entries.add(ModBlocks.GREEN_LAMP);
                        entries.add(ModBlocks.RED_LAMP);
                        entries.add(ModBlocks.BLACK_LAMP);
                    }).build());

    public static void registerModItemGroups() {
        CozyHome.LOGGER.info("Registering Item Groups for " + CozyHome.MOD_ID);
    }
}
