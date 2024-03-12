package ru.romindous.wz.Utils;

import org.bukkit.Material;

import java.util.Objects;

public record Priced(int fst, int scd, int thd) {

    public static final char FST_MAT = 'Х';
    public static final char SCD_MAT = 'Н';
    public static final char THD_MAT = 'П';

    public static final String ZRT_TOOL = "STONE";
    public static final String FST_TOOL = "IRON";
    public static final String SCD_TOOL = "DIAMOND";
    public static final String THD_TOOL = "NETHERITE";

    public static final Material FST_WEAPON = Material.GOLDEN_SWORD;
    public static final Material SCD_WEAPON = Material.IRON_SWORD;
    public static final Material THD_WEAPON = Material.NETHERITE_SWORD;
    public static final Material[] WEAPONS = {FST_WEAPON, SCD_WEAPON, THD_WEAPON};

    public static final String FST_ARMOR = "LEATHER";
    public static final String SCD_ARMOR = "CHAINMAIL";
    public static final String THD_ARMOR = "IRON";
    public static final String FRT_ARMOR = "DIAMOND";
    public static final String FFT_ARMOR = "NETHERITE";

    public static final Material[] HEAD = collect("_HELMET", FST_ARMOR, SCD_ARMOR, THD_ARMOR, FRT_ARMOR, FFT_ARMOR);
    public static final Material[] CHEST = collect("_CHESTPLATE", FST_ARMOR, SCD_ARMOR, THD_ARMOR, FRT_ARMOR, FFT_ARMOR);
    public static final Material[] LEGS = collect("_LEGGINGS", FST_ARMOR, SCD_ARMOR, THD_ARMOR, FRT_ARMOR, FFT_ARMOR);
    public static final Material[] FEET = collect("_BOOTS", FST_ARMOR, SCD_ARMOR, THD_ARMOR, FRT_ARMOR, FFT_ARMOR);

    public static Material[] collect(final String sfx, final String... types) {
        final Material[] mts = new Material[types.length];
        for (int i = 0; i != types.length; i++) {
            mts[i] = Objects.requireNonNullElse(Material.getMaterial(types[i] + sfx), Material.GRAY_DYE);
        }
        return mts;
    }
}
