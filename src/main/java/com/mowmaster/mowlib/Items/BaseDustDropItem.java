package com.mowmaster.mowlib.Items;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import com.mowmaster.mowlib.Capabilities.Dust.IDustTank;
import org.jetbrains.annotations.NotNull;

public class BaseDustDropItem extends BaseDustStorageItem implements IDustTank {
    public BaseDustDropItem(Properties p_41383_) {
        super(p_41383_);
    }

    @NotNull
    @Override
    public DustMagic getDustMagic() {
        return null;
    }

    @Override
    public int getDustMagicAmount() {
        return 0;
    }

    @Override
    public int getTankCapacity() {
        return 0;
    }

    @Override
    public boolean isDustValid(DustMagic dust) {
        return false;
    }

    @Override
    public int fill(DustMagic dust, IDustHandler.DustAction action) {
        return 0;
    }

    @NotNull
    @Override
    public DustMagic drain(int maxDrain, IDustHandler.DustAction action) {
        return null;
    }

    @NotNull
    @Override
    public DustMagic drain(DustMagic dust, IDustHandler.DustAction action) {
        return null;
    }
}
