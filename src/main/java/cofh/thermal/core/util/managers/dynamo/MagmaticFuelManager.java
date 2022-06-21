package cofh.thermal.core.util.managers.dynamo;

import cofh.thermal.core.init.TCoreRecipeTypes;
import cofh.thermal.lib.util.managers.SingleFluidFuelManager;
import cofh.thermal.lib.util.recipes.internal.IDynamoFuel;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fluids.FluidStack;

public class MagmaticFuelManager extends SingleFluidFuelManager {

    private static final MagmaticFuelManager INSTANCE = new MagmaticFuelManager();
    protected static final int DEFAULT_ENERGY = 100000;

    public static MagmaticFuelManager instance() {

        return INSTANCE;
    }

    private MagmaticFuelManager() {

        super(DEFAULT_ENERGY);
    }

    public int getEnergy(FluidStack stack) {

        IDynamoFuel fuel = getFuel(stack);
        return fuel != null ? fuel.getEnergy() : 0;
    }

    // region IManager
    @Override
    public void refresh(RecipeManager recipeManager) {

        clear();
        var recipes = recipeManager.byType(TCoreRecipeTypes.FUEL_MAGMATIC);
        for (var entry : recipes.entrySet()) {
            addFuel(entry.getValue());
        }
    }
    // endregion
}
