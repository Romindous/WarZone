package me.Romindous.WarZone.Utils;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import ru.komiss77.Objects.Oplayer;

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

	public static String getTopGrp(Oplayer op) {
		if (op.groups.contains("xpanitely")) {
			return "Хранитель";
		} else if (op.groups.contains("builder")) {
			return "Строитель";
		} else if (op.groups.contains("supermoder")) {
			return "Архангел";
		} else if (op.groups.contains("moder-spy")) {
			return "Ангел";
		} else if (op.groups.contains("moder")) {
			return "Модератор";
		} else if (op.groups.contains("mchat")) {
			return "Чат-Модер";
		} else {
			return null;
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
}