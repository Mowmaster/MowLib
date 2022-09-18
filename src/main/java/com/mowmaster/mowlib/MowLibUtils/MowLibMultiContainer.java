package com.mowmaster.mowlib.MowLibUtils;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Set;
import java.util.function.Predicate;

public interface MowLibMultiContainer extends Container
{
    int LARGE_MAX_STACK_SIZE = 64;

    int getContainerSize();

    boolean isEmpty();

    ItemStack getItem(int slot);

    FluidStack getFluidStack();

    int getEnergy();

    int getExperience();

    DustMagic getDustMagic();

    ItemStack removeItem(int p_18942_, int p_18943_);

    FluidStack removeFluid();

    int removeEnergy();

    int removeExperience();

    DustMagic removeDustMagic();

    ItemStack removeItemNoUpdate(int p_18951_);

    void setItem(int p_18944_, ItemStack p_18945_);

    void setFluidStack(FluidStack fluidIn);

    void setEnergy(int amount);

    void setExperience(int amount);

    void setDustMagic(DustMagic dustMagic);

    default int getMaxStackSize() {
        return 64;
    }

    void setChanged();

    boolean stillValid(Player p_18946_);

    default void startOpen(Player p_18955_) {
    }

    default void stopOpen(Player p_18954_) {
    }

    default boolean canPlaceItem(int p_18952_, ItemStack p_18953_) {
        return true;
    }

    default int countItem(Item p_18948_) {
        int i = 0;

        for(int j = 0; j < this.getContainerSize(); ++j) {
            ItemStack itemstack = this.getItem(j);
            if (itemstack.getItem().equals(p_18948_)) {
                i += itemstack.getCount();
            }
        }

        return i;
    }

    default boolean hasAnyOf(Set<Item> p_18950_) {
        return this.hasAnyMatching((p_216873_) -> {
            return !p_216873_.isEmpty() && p_18950_.contains(p_216873_.getItem());
        });
    }

    default boolean hasAnyMatching(Predicate<ItemStack> p_216875_) {
        for(int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemstack = this.getItem(i);
            if (p_216875_.test(itemstack)) {
                return true;
            }
        }

        return false;
    }
}
