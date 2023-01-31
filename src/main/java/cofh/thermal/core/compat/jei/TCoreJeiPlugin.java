package cofh.thermal.core.compat.jei;

import cofh.core.util.helpers.FluidHelper;
import cofh.thermal.core.client.gui.device.DeviceComposterScreen;
import cofh.thermal.core.client.gui.device.DeviceRockGenScreen;
import cofh.thermal.core.client.gui.device.DeviceTreeExtractorScreen;
import cofh.thermal.core.compat.jei.device.ComposterCategory;
import cofh.thermal.core.compat.jei.device.RockGenCategory;
import cofh.thermal.core.compat.jei.device.TreeExtractorCategory;
import cofh.thermal.core.util.recipes.device.RockGenMapping;
import cofh.thermal.core.util.recipes.device.TreeExtractorMapping;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import static cofh.lib.util.Constants.BASE_CHANCE;
import static cofh.lib.util.Constants.BUCKET_VOLUME;
import static cofh.lib.util.constants.ModIds.ID_THERMAL;
import static cofh.lib.util.helpers.StringHelper.getTextComponent;
import static cofh.thermal.core.ThermalCore.BLOCKS;
import static cofh.thermal.core.compat.jei.device.ComposterCategory.ID_MAPPING_COMPOSTER;
import static cofh.thermal.core.config.ThermalClientConfig.jeiBucketTanks;
import static cofh.thermal.core.init.TCoreRecipeTypes.*;
import static cofh.thermal.lib.common.ThermalFlags.getFlag;
import static cofh.thermal.lib.common.ThermalIDs.*;

@JeiPlugin
public class TCoreJeiPlugin implements IModPlugin {

    @Override
    public void registerRecipes(IRecipeRegistration registration) {

        RecipeManager recipeManager = getRecipeManager();
        if (recipeManager == null) {
            // TODO: Log an error.
            return;
        }
        if (getFlag(ID_DEVICE_TREE_EXTRACTOR).get()) {
            registration.addRecipes(TREE_EXTRACTOR, recipeManager.getAllRecipesFor(MAPPING_TREE_EXTRACTOR));
        }
        if (getFlag(ID_DEVICE_COMPOSTER).get()) {
            registration.addRecipes(COMPOSTER, ComposterCategory.getMappings());
        }
        if (getFlag(ID_DEVICE_ROCK_GEN).get()) {
            registration.addRecipes(ROCK_GEN, recipeManager.getAllRecipesFor(MAPPING_ROCK_GEN));
        }
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {

        registration.addRecipeCategories(new TreeExtractorCategory(registration.getJeiHelpers().getGuiHelper(), new ItemStack(BLOCKS.get(ID_DEVICE_TREE_EXTRACTOR)), ID_MAPPING_TREE_EXTRACTOR));
        registration.addRecipeCategories(new ComposterCategory(registration.getJeiHelpers().getGuiHelper(), new ItemStack(BLOCKS.get(ID_DEVICE_COMPOSTER)), ID_MAPPING_COMPOSTER));
        registration.addRecipeCategories(new RockGenCategory(registration.getJeiHelpers().getGuiHelper(), new ItemStack(BLOCKS.get(ID_DEVICE_ROCK_GEN)), ID_MAPPING_ROCK_GEN));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {

        registration.addRecipeCatalyst(new ItemStack(BLOCKS.get(ID_DEVICE_TREE_EXTRACTOR)), TREE_EXTRACTOR);
        registration.addRecipeCatalyst(new ItemStack(BLOCKS.get(ID_DEVICE_COMPOSTER)), COMPOSTER);
        registration.addRecipeCatalyst(new ItemStack(BLOCKS.get(ID_DEVICE_ROCK_GEN)), ROCK_GEN);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {

        int progressY = 35;
        int progressW = 24;
        int progressH = 16;

        registration.addRecipeClickArea(DeviceTreeExtractorScreen.class, 80, progressY, 16, progressH, TREE_EXTRACTOR);
        registration.addRecipeClickArea(DeviceComposterScreen.class, 87, progressY, progressW, progressH, COMPOSTER);
        registration.addRecipeClickArea(DeviceRockGenScreen.class, 84, progressY, progressW, progressH, ROCK_GEN);
    }

    @Override
    public ResourceLocation getPluginUid() {

        return new ResourceLocation(ID_THERMAL, "core");
    }

    // region HELPERS
    private RecipeManager getRecipeManager() {

        RecipeManager recipeManager = null;
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            recipeManager = level.getRecipeManager();
        }
        return recipeManager;
    }

    public static IRecipeSlotTooltipCallback catalystTooltip() {

        return (recipeSlotView, tooltip) -> tooltip.add(getTextComponent("info.cofh.optional_catalyst"));
    }

    public static IRecipeSlotTooltipCallback defaultOutputTooltip(float baseChance) {

        return (recipeSlotView, tooltip) -> {

            float chance = Math.abs(baseChance);
            if (chance < BASE_CHANCE) {
                tooltip.add(getTextComponent("info.cofh.chance").append(": " + (int) (100 * chance) + "%"));
            } else {
                chance -= (int) chance;
                if (chance > 0) {
                    tooltip.add(getTextComponent("info.cofh.chance_additional").append(": " + (int) (100 * chance) + "%"));
                }
            }
            if (baseChance >= 0) {
                tooltip.add(getTextComponent("info.cofh.boostable").withStyle(ChatFormatting.GOLD));
            }
        };
    }

    public static IRecipeSlotTooltipCallback catalyzedOutputTooltip(float baseChance, boolean catalyzable) {

        return (recipeSlotView, tooltip) -> {

            float chance = Math.abs(baseChance);
            if (chance < BASE_CHANCE) {
                tooltip.add(getTextComponent("info.cofh.chance").append(": " + (int) (100 * chance) + "%"));
            } else {
                chance -= (int) chance;
                if (chance > 0) {
                    tooltip.add(getTextComponent("info.cofh.chance_additional").append(": " + (int) (100 * chance) + "%"));
                }
            }
            if (catalyzable && baseChance >= 0) {
                tooltip.add(getTextComponent("info.cofh.boostable").withStyle(ChatFormatting.GOLD));
            }
        };
    }

    public static IRecipeSlotTooltipCallback defaultFluidTooltip() {

        return (recipeSlotView, tooltip) -> recipeSlotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent((ingredient) -> {
            if (FluidHelper.hasPotionTag(ingredient)) {
                FluidHelper.addPotionTooltipStrings(ingredient, tooltip);
            }
        });
    }

    public static int tankSize(int size) {

        return jeiBucketTanks.get() ? BUCKET_VOLUME : size;
    }

    public static IDrawable tankOverlay(IDrawable overlay) {

        return jeiBucketTanks.get() ? null : overlay;
    }

    public static int getCenteredOffset(Font font, String string, int xPos) {

        return ((xPos * 2) - font.width(string)) / 2;
    }

    public static double roundToPlaces(double value, int places) {

        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }
    // endregion

    // region RECIPE TYPES
    public static final RecipeType<TreeExtractorMapping> TREE_EXTRACTOR = new RecipeType<>(ID_MAPPING_TREE_EXTRACTOR, TreeExtractorMapping.class);
    public static final RecipeType<ComposterCategory.ComposterMapping> COMPOSTER = new RecipeType<>(ID_MAPPING_COMPOSTER, ComposterCategory.ComposterMapping.class);
    public static final RecipeType<RockGenMapping> ROCK_GEN = new RecipeType<>(ID_MAPPING_ROCK_GEN, RockGenMapping.class);
    // endregion
}