package me.travis.wurstplusthree.hack.player;

import me.travis.wurstplusthree.hack.Hack;
import me.travis.wurstplusthree.setting.type.BooleanSetting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.item.ItemExpBottle;

public class FastUtil extends Hack {

    public FastUtil() {
        super("FastUtil", "util but fast", Category.PLAYER, false, false);
    }

    BooleanSetting xp = new BooleanSetting("XP", true, this);
    BooleanSetting crystal = new BooleanSetting("Crystal", true, this);

    @Override
    public void onUpdate() {
        Item main = mc.player.getHeldItemMainhand().getItem();
        Item off  = mc.player.getHeldItemOffhand().getItem();

        boolean mainXP = main instanceof ItemExpBottle;
        boolean offXP  = off instanceof ItemExpBottle;
        boolean mainC = main instanceof ItemEndCrystal;
        boolean offC  = off instanceof ItemEndCrystal;

        if (mainXP | offXP && xp.getValue()) {
            mc.rightClickDelayTimer = 0;
        }

        if (mainC | offC && crystal.getValue()) {
            mc.rightClickDelayTimer = 0;
        }
    }
}
