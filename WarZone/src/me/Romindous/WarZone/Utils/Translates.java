package me.Romindous.WarZone.Utils;

import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.block.Biome;
import org.bukkit.entity.Villager.Type;

public class Translates {

	//перевод цвета
	public static String transClr(final ChatColor cc) {
		switch (cc) {
		case BLACK:
			return "§0Черная";
		case DARK_BLUE:
			return "§1Темно-Синяя";
		case DARK_GREEN:
			return "§2Зеленая";
		case DARK_AQUA:
			return "§3Бирюзовая";
		case DARK_RED:
			return "§4Бардовая";
		case DARK_PURPLE:
			return "§5Пурпурная";
		case GOLD:
			return "§6Золотая";
		case GRAY:
			return "§7Серая";
		case DARK_GRAY:
			return "§8Темно-Серая";
		case BLUE:
			return "§9Синяя";
		case GREEN:
			return "§aЛаймовая";
		case AQUA:
			return "§bГолубая";
		case RED:
			return "§cКрасная";
		case LIGHT_PURPLE:
			return "§dРозовая";
		case YELLOW:
			return "§eЖелтая";
		case WHITE:
			return "§fБелая";
		default:
			break;
		}
		return "";
	}

	public static String mtFromCC(final ChatColor cc) {
		switch (cc) {
		case AQUA:
			return "LIGHT_BLUE";
		case BLACK:
			return "BLACK";
		case BLUE:
			return "BLUE";
		case DARK_AQUA:
			return "CYAN";
		case DARK_BLUE:
			return "BLUE";
		case DARK_GRAY:
			return "GRAY";
		case DARK_GREEN:
			return "GREEN";
		case DARK_PURPLE:
			return "PURPLE";
		case DARK_RED:
			return "BROWN";
		case GOLD:
			return "ORANGE";
		case GRAY:
			return "LIGHT_GRAY";
		case GREEN:
			return "LIME";
		case LIGHT_PURPLE:
			return "PINK";
		case RED:
			return "RED";
		case WHITE:
			return "WHITE";
		case YELLOW:
			return "YELLOW";
		default:
			return "";
		}
	}

	public static Color ccToClr(final ChatColor cc) {
		switch (cc) {
		case AQUA:
			return Color.AQUA;
		case BLACK:
			return Color.BLACK;
		case BLUE:
			return Color.BLUE;
		case DARK_AQUA:
			return Color.TEAL;
		case DARK_BLUE:
			return Color.NAVY;
		case DARK_GRAY:
			return Color.GRAY;
		case DARK_GREEN:
			return Color.GREEN;
		case DARK_PURPLE:
			return Color.PURPLE;
		case DARK_RED:
			return Color.MAROON;
		case GOLD:
			return Color.ORANGE;
		case GRAY:
			return Color.SILVER;
		case GREEN:
			return Color.LIME;
		case LIGHT_PURPLE:
			return Color.FUCHSIA;
		case RED:
			return Color.RED;
		case WHITE:
			return Color.WHITE;
		case YELLOW:
			return Color.YELLOW;
		default:
			break;
		}
		return Color.WHITE;
	}

	public static String getTopGroup(final Collection<String> grps) {
		if (grps.contains("xpanitely")) {
			return "Хранитель";
		} else if (grps.contains("builder")) {
			return "Строитель";
		} else if (grps.contains("supermoder")) {
			return "Архангел";
		} else if (grps.contains("moder-spy")) {
			return "Ангел";
		} else if (grps.contains("moder")) {
			return "Модератор";
		} else if (grps.contains("mchat")) {
			return "Чат-Модер";
		} else {
			return "N";
		}
	}

	public static byte getItmLvl(final char m) {
		switch (m) {
		case 'W':
		case 'L':
			return 0;
		case 'G':
		case 'S':
			return 1;
		case 'C':
			return 2;
		case 'I':
			return 3;
		case 'D':
			return 4;
		case 'N':
			return 5;
		default:
			return 0;
		}
	}

	public static Type getBmVllTp(final Biome b) {
		switch (b) {
		case BADLANDS:
		case SAVANNA:
		case SAVANNA_PLATEAU:
		case CRIMSON_FOREST:
		case ERODED_BADLANDS:
			return Type.SAVANNA;
		case BEACH:
		case DESERT:
		case NETHER_WASTES:
		case SOUL_SAND_VALLEY:
			return Type.DESERT;
		case BAMBOO_JUNGLE:
		case JUNGLE:
		case MUSHROOM_FIELDS:
			return Type.JUNGLE;
		case SWAMP:
		case DARK_FOREST:
		case WARPED_FOREST:
		case RIVER:
			return Type.SWAMP;
		case TAIGA:
			return Type.TAIGA;
		case SNOWY_BEACH:
		case SNOWY_TAIGA:
		case ICE_SPIKES:
			return Type.SNOW;
		default:
			return Type.PLAINS;
		}
	}
}
