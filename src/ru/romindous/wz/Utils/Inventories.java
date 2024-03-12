package ru.romindous.wz.Utils;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.TCUtils;
import ru.romindous.wz.Game.Arena;
import ru.romindous.wz.Game.GameState;
import ru.romindous.wz.Game.Team;
import ru.romindous.wz.Main;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Inventories {

	public static ItemStack[] fillArInv(final int slots) {
        final ItemStack[] its = new ItemStack[slots];
		final Iterator<String> mit = Main.nonactivearenas.keySet().iterator();
		for (byte i = 0; i < slots; i++) {
			switch (i) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 5:
			case 6:
			case 7:
			case 8:
				its[i] = mkItm(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§0.", true);
				break;
				
			case 4:
				its[i] = mkItm(Material.LEATHER, "§6Выбор Карты", true);
				break;
				
			default:
				if (i > (slots - 10)) {
					its[i] = mkItm(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§0.", true);
				} else if (mit.hasNext()) {
					final String anm = mit.next();
					final Arena ar = Main.activearenas.get(anm);
					if (ar == null) {
						its[i] = mkItm(Material.GREEN_CONCRETE_POWDER, "§a" + anm,
							new LinkedList<>(Arrays.asList("", "§2Ожидание (§7" + Main.nonactivearenas.get(anm).min + "§2)")), (byte) 1, true);
					} else {
						if (ar.getState() == GameState.WAITING) {
							final int amt = ar.getPls().size();
							its[i] = mkItm(Material.YELLOW_CONCRETE_POWDER, "§e" + anm,
								new LinkedList<>(Arrays.asList("", "§6Игроки: " + (amt < ar.getMin()
									? amt + " из " + ar.getMin() : amt + " из " + ar.getMax()))), (byte) 1, true);
						} else {
							its[i] = mkItm(Material.RED_CONCRETE_POWDER, "§c" + anm,
								new LinkedList<>(Arrays.asList("", "§4Идет Игра", "", TCUtils.N + "Нажмите для наблюдения!")), (byte) 1, true);
						}
					}
				}
				break;
			}
		}
		return its;
	}

	public static ItemStack[] fillShpInv(final Arena ar) {
		final ItemStack[] its = new ItemStack[27];
		for (byte i = 0; i < 27; i++) {
			switch (i) {
			case 2:
				its[i] = mkItm(ar.recs[0], "§aХрупкий Материал", true);
				break;
			case 4:
				its[i] = mkItm(ar.recs[1], "§3Нормальный Материал", true);
				break;
			case 6:
				its[i] = mkItm(ar.recs[2], "§2Прочный Материал", true);
				break;
			case 10:
				its[i] = mkItm(Material.CAKE, "§6Еда",
					Arrays.asList(" ", TCUtils.N + "Покупка за " + TCUtils.P + "Ресурсы"), (byte) 1, true);
				break;
			case 12:
				its[i] = mkItm(Material.WOODEN_SWORD, "§6Оружие",
					Arrays.asList(" ", TCUtils.N + "Покупка за " + TCUtils.P + "Ресурсы"), (byte) 1, true);
				break;
			case 13:
				its[i] = mkItm(Material.getMaterial("WOODEN" + ar.getTlSfx()), "§6Инструменты",
					Arrays.asList(" ", TCUtils.N + "Покупка за " + TCUtils.P + "Ресурсы"), (byte) 1, true);
				break;
			case 14:
				its[i] = mkItm(Material.TURTLE_HELMET, "§6Броня",
					Arrays.asList(" ", TCUtils.N + "Покупка за " + TCUtils.P + "Ресурсы"), (byte) 1, true);
				break;
			case 16:
				its[i] = mkItm(Material.ENDER_EYE, "§6Разное",
					Arrays.asList(" ", TCUtils.N + "Покупка за §eМонеты"), (byte) 1, true);
				break;
			case 22:
				its[i] = mkItm(Material.BARRIER, "§cВыход", true);
				break;
			default:
				its[i] = mkItm(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§0.", true);
				break;
			}
		}
		return its;
	}

	private static final Color fwc = TCUtils.getBukkitColor(TCUtils.getTextColor(TCUtils.A));
	
	public static ItemStack[] fillShpWpnInv(final PlayerInventory inv, final Team tm) {
		final ItemStack[] its = new ItemStack[27];
		if (tm == null) return its;
        final float bfr = 1f -
			((inv.contains(Material.NETHERITE_SWORD) ? 1f : 
			(inv.contains(Material.IRON_SWORD) ? 0.7f : 
			(inv.contains(Material.GOLDEN_SWORD) ? 0.4f : 0f))));
		for (byte i = 0; i < 27; i++) {
			switch (i) {
			case 4:
				its[i] = mkItm(Material.WOODEN_SWORD, "§6Оружие",
					Arrays.asList(" ", TCUtils.N + "Покупка за " + TCUtils.P + "Ресурсы"), (byte) 1, true);
				break;
			case 10:
				its[i] = mkItm(Material.GOLDEN_SWORD, tm.color() + "Затупелый Меч", getTlWpnPrs("sword1", bfr), (byte) 1, true);
				break;
			case 11:
				its[i] = mkItm(Material.IRON_SWORD, tm.color() + "Нормальный Меч", getTlWpnPrs("sword2", bfr), (byte) 1, true);
				break;
			case 12:
				its[i] = mkItm(Material.NETHERITE_SWORD, tm.color() + "Остренный Меч", getTlWpnPrs("sword3", bfr), (byte) 1, true);
				break;
			case 14:
				its[i] = mkItm(Material.CROSSBOW, tm.color() + "Арбалет", getRecPrs("cross"), (byte) 1, true);
				break;
			case 15:
				its[i] = mkItm(Material.ARROW, tm.color() + "Стрела", getRecPrs("arrow"), (byte) 8, true);
				break;
			case 16:
				final ItemStack it = new ItemStack(Material.FIREWORK_ROCKET, 8);
				final FireworkMeta fm = (FireworkMeta) it.getItemMeta();
				fm.displayName(TCUtils.format(tm.color() + "Фейерверк"));
				fm.lore(getRecPrs("frwork").stream().map(TCUtils::format).toList());
				final FireworkEffect fe = FireworkEffect.builder().with(Type.BURST).withColor(Color.fromRGB(tm.txc.value())).build();
				fm.addEffects(fe, fe, fe, fe);
				it.setItemMeta(fm);
				its[i] = it;
				break;
			case 22:
				its[i] = mkItm(Material.REDSTONE_TORCH, "§cНазад", true);
				break;
			default:
				its[i] = mkItm(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§0.", true);
				break;
			}
		}
		return its;
	}
	
	public static ItemStack[] fillShpTlInv(final PlayerInventory inv, final String sfx, final Team tm) {
		final ItemStack[] its = new ItemStack[27];
		if (tm == null) return its;
		final float bfr = 1 - 
			((inv.contains(Material.getMaterial("NETHERITE" + sfx)) ? 1 : 
			(inv.contains(Material.getMaterial("DIAMOND" + sfx)) ? 0.7f : 
			(inv.contains(Material.getMaterial("IRON" + sfx)) ? 0.4f : 0))));
		for (byte i = 0; i < 27; i++) {
			switch (i) {
			case 4:
				its[i] = mkItm(Material.getMaterial("STONE" + sfx), "§6Инструменты",
					Arrays.asList(" ", TCUtils.N + "Покупка за " + TCUtils.P + "Ресурсы"), (byte) 1, true);
				break;
			case 10:
				its[i] = mkItm(Material.getMaterial("IRON" + sfx), tm.color() + "Затупелый Инструмент",
					getTlWpnPrs("tool1", bfr), (byte) 1, true);
				break;
			case 12:
				its[i] = mkItm(Material.getMaterial("DIAMOND" + sfx), tm.color() + "Нормальный Инструмент",
					getTlWpnPrs("tool2", bfr), (byte) 1, true);
				break;
			case 14:
				its[i] = mkItm(Material.getMaterial("NETHERITE" + sfx), tm.color() + "Остренный Инструмент",
					getTlWpnPrs("tool3", bfr), (byte) 1, true);
				break;
			case 16:
				its[i] = mkItm(Material.SHIELD, tm.color() + "Щит", getRecPrs("shield"), (byte) 1, true);
				break;
			case 22:
				its[i] = mkItm(Material.REDSTONE_TORCH, "§cНазад", true);
				break;
			default:
				its[i] = mkItm(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§0.", true);
				break;
			}
		}
		return its;
	}
	
	public static ItemStack[] fillShpFdInv() {
		final ItemStack[] its = new ItemStack[27];
		for (byte i = 0; i < 27; i++) {
			switch (i) {
			case 4:
				its[i] = mkItm(Material.CAKE, "§6Еда",
					Arrays.asList(" ", TCUtils.N + "Покупка за " + TCUtils.P + "Ресурсы"), (byte) 1, true);
				break;
			case 10:
				its[i] = mkItm(Material.APPLE, null, getRecPrs("food1"), (byte) 4, true);
				break;
			case 12:
				its[i] = mkItm(Material.PUMPKIN_PIE, null, getRecPrs("food2"), (byte) 2, true);
				break;
			case 13:
				its[i] = mkItm(Material.COOKED_PORKCHOP, null, getRecPrs("food3"), (byte) 2, true);
				break;
			case 14:
				its[i] = mkItm(Material.GOLDEN_APPLE, null, getRecPrs("food4"), (byte) 2, true);
				break;
			case 16:
				its[i] = mkItm(Material.ENCHANTED_GOLDEN_APPLE, null, getRecPrs("food5"), (byte) 1, true);
				break;
			case 22:
				its[i] = mkItm(Material.REDSTONE_TORCH, "§cНазад", true);
				break;
			default:
				its[i] = mkItm(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§0.", true);
				break;
			}
		}
		return its;
	}
	
	public static ItemStack[] fillShpArmrInv(final PlayerInventory inv, final Team tm) {
		final ItemStack[] its = new ItemStack[54];
		if (tm == null) return its;
		final ConfigurationSection cs = Main.plug.getConfig().getConfigurationSection("trade");
		final float bfr = 1 - getArmBfr(inv.getArmorContents());
		for (byte i = 0; i < 54; i++) {
			switch (i) {
			case 4:
				its[i] = mkItm(Material.TURTLE_HELMET, "§6Броня",
					Arrays.asList(" ", TCUtils.N + "Покупка за " + TCUtils.P + "Ресурсы"), (byte) 1, true);
				break;
				
			case 10:
				its[i] = mkItm(Material.LEATHER_HELMET, tm.color() + "Растрепаный Шлем", getArmPrs("armor1", bfr, 0.2f), (byte) 1, true);
				break;
			case 11:
				its[i] = mkItm(Material.CHAINMAIL_HELMET, tm.color() + "Паршивый Шлем", getArmPrs("armor2", bfr, 0.2f), (byte) 1, true);
				break;
			case 13:
				its[i] = mkItm(Material.IRON_HELMET, tm.color() + "Нормальный Шлем", getArmPrs("armor3", bfr, 0.2f), (byte) 1, true);
				break;
			case 15:
				its[i] = mkItm(Material.DIAMOND_HELMET, tm.color() + "Качественный Шлем", getArmPrs("armor4", bfr, 0.2f), (byte) 1, true);
				break;
			case 16:
				its[i] = mkItm(Material.NETHERITE_HELMET, tm.color() + "Безупречный Шлем", getArmPrs("armor5", bfr, 0.2f), (byte) 1, true);
				break;
				
			case 19:
				its[i] = mkItm(Material.LEATHER_CHESTPLATE, tm.color() + "Растрепаный Нагрудник", getArmPrs("armor1", bfr, 0.4f), (byte) 1, true);
				break;
			case 20:
				its[i] = mkItm(Material.CHAINMAIL_CHESTPLATE, tm.color() + "Паршивый Нагрудник", getArmPrs("armor2", bfr, 0.4f), (byte) 1, true);
				break;
			case 22:
				its[i] = mkItm(Material.IRON_CHESTPLATE, tm.color() + "Нормальный Нагрудник", getArmPrs("armor3", bfr, 0.4f), (byte) 1, true);
				break;
			case 24:
				its[i] = mkItm(Material.DIAMOND_CHESTPLATE, tm.color() + "Качественный Нагрудник", getArmPrs("armor4", bfr, 0.4f), (byte) 1, true);
				break;
			case 25:
				its[i] = mkItm(Material.NETHERITE_CHESTPLATE, tm.color() + "Безупречный Нагрудник", getArmPrs("armor5", bfr, 0.4f), (byte) 1, true);
				break;
				
			case 28:
				its[i] = mkItm(Material.LEATHER_LEGGINGS, tm.color() + "Растрепаные Штаны", getArmPrs("armor1", bfr, 0.3f), (byte) 1, true);
				break;
			case 29:
				its[i] = mkItm(Material.CHAINMAIL_LEGGINGS, tm.color() + "Паршивые Штаны", getArmPrs("armor2", bfr, 0.3f), (byte) 1, true);
				break;
			case 31:
				its[i] = mkItm(Material.IRON_LEGGINGS, tm.color() + "Нормальные Штаны", getArmPrs("armor3", bfr, 0.3f), (byte) 1, true);
				break;
			case 33:
				its[i] = mkItm(Material.DIAMOND_LEGGINGS, tm.color() + "Качественные Штаны", getArmPrs("armor4", bfr, 0.3f), (byte) 1, true);
				break;
			case 34:
				its[i] = mkItm(Material.NETHERITE_LEGGINGS, tm.color() + "Безупречные Штаны", getArmPrs("armor5", bfr, 0.3f), (byte) 1, true);
				break;
				
			case 37:
				its[i] = mkItm(Material.LEATHER_BOOTS, tm.color() + "Растрепаные Ботинки", getArmPrs("armor1", bfr, 0.1f), (byte) 1, true);
				break;
			case 38:
				its[i] = mkItm(Material.CHAINMAIL_BOOTS, tm.color() + "Паршивые Ботинки", getArmPrs("armor2", bfr, 0.1f), (byte) 1, true);
				break;
			case 40:
				its[i] = mkItm(Material.IRON_BOOTS, tm.color() + "Нормальные Ботинки", getArmPrs("armor3", bfr, 0.1f), (byte) 1, true);
				break;
			case 42:
				its[i] = mkItm(Material.DIAMOND_BOOTS, tm.color() + "Качественные Ботинки", getArmPrs("armor4", bfr, 0.1f), (byte) 1, true);
				break;
			case 43:
				its[i] = mkItm(Material.NETHERITE_BOOTS, tm.color() + "Безупречные Ботинки", getArmPrs("armor5", bfr, 0.1f), (byte) 1, true);
				break;
				
			case 49:
				its[i] = mkItm(Material.REDSTONE_TORCH, "§cНазад", true);
				break;
			default:
				its[i] = mkItm(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§0.", true);
				break;
			}
		}
		return its;
	}

	public static float getArmBfr(final ItemStack[] arm) {
		int i = 0;
		if (arm[0] != null) {
			switch (arm[0].getType()) {
			case LEATHER_BOOTS:
				i += 2;
				break;
			case CHAINMAIL_BOOTS:
				i += 4;
				break;
			case IRON_BOOTS:
				i += 6;
				break;
			case DIAMOND_BOOTS:
				i += 8;
				break;
			case NETHERITE_BOOTS:
				i += 10;
				break;
			default:
				break;
			}
		}
		if (arm[1] != null) {
			switch (arm[1].getType()) {
			case LEATHER_LEGGINGS:
				i += 6;
				break;
			case CHAINMAIL_LEGGINGS:
				i += 12;
				break;
			case IRON_LEGGINGS:
				i += 18;
				break;
			case DIAMOND_LEGGINGS:
				i += 24;
				break;
			case NETHERITE_LEGGINGS:
				i += 30;
				break;
			default:
				break;
			}
		}
		if (arm[2] != null) {
			switch (arm[2].getType()) {
			case LEATHER_CHESTPLATE:
				i += 8;
				break;
			case CHAINMAIL_CHESTPLATE:
				i += 16;
				break;
			case IRON_CHESTPLATE:
				i += 24;
				break;
			case DIAMOND_CHESTPLATE:
				i += 32;
				break;
			case NETHERITE_CHESTPLATE:
				i += 40;
				break;
			default:
				break;
			}
		}
		if (arm[3] != null) {
			switch (arm[3].getType()) {
			case LEATHER_HELMET:
				i += 4;
				break;
			case CHAINMAIL_HELMET:
				i += 8;
				break;
			case IRON_HELMET:
				i += 12;
				break;
			case DIAMOND_HELMET:
				i += 16;
				break;
			case NETHERITE_HELMET:
				i += 20;
				break;
			default:
				break;
			}
		}
		return (float) i / 100f;
	}

	public static ItemStack[] fillShpXtrInv() {
		final ItemStack[] its = new ItemStack[27];
		for (byte i = 0; i < 27; i++) {
			switch (i) {
			case 4:
				its[i] = mkItm(Material.ENDER_EYE, "§6Разное", Arrays.asList(" ", TCUtils.N + "Покупка за §eМонеты"), (byte) 1, true);
				break;
			case 10:
				its[i] = mkItm(Material.ENCHANTED_BOOK, "§eЗачарование", Arrays.asList(" ", 
					TCUtils.P + Main.extras.getOrDefault("ench", 0) + " + (" + Main.extras.getOrDefault("dlvl", 0) 
						+ " * lvl)" + TCUtils.N + " Монет", " ", TCUtils.N + "Кликните сюда предметом для его §eзачарования" + TCUtils.N + "!", " "), (byte) 1, true);
				break;
			case 12:
				its[i] = mkItm(Material.TOTEM_OF_UNDYING, "§e+1 Возрождение", Arrays.asList(" ", TCUtils.P + Main.extras.getOrDefault("resp", 0) 
					+ TCUtils.N + " Монет", " ", TCUtils.N + "Добавляет одно §eвозрождение " + TCUtils.N + "ващей комманде!", " "), (byte) 1, true);
				break;
			case 14:
				its[i] = new ItemBuilder(Material.SPLASH_POTION).setBasePotionType(PotionType.INSTANT_HEAL)
					.addCustomPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1))
					.name("§eЛечащее Зелье").addLore(getCnsPrs("health", "Имеет лечащие свойства!")).build();
				break;
			case 15:
				its[i] = new ItemBuilder(Material.SPLASH_POTION).setBasePotionType(PotionType.SPEED)
					.addCustomPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 500, 1))
					.name("§eЗелье Ускорения").addLore(getCnsPrs("speed", "Ускоряет работу!")).build();
				break;
			case 16:
				its[i] = new ItemBuilder(Material.SPLASH_POTION).setBasePotionType(PotionType.STRENGTH)
					.name("§eЗелье Силы").addLore(getCnsPrs("strong", "Дарует силу!")).build();
				break;
			case 22:
				its[i] = mkItm(Material.REDSTONE_TORCH, "§cНазад", true);
				break;
			default:
				its[i] = mkItm(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§0.", true);
				break;
			}
		}
		return its;
	}

	public static LinkedList<String> getRecPrs(final String name) {
		final LinkedList<String> prs = new LinkedList<>();
		prs.add(" ");
		final Priced pr = Main.trades.get(name);
		if (pr.fst() > 0) prs.add(Arena.FIRST + pr.fst() + TCUtils.N + " Хрупкого Материала");
		if (pr.scd() > 0) prs.add(Arena.SECOND + pr.scd() + TCUtils.N + " Нормального Материала");
		if (pr.thd() > 0) prs.add(Arena.THIRD + pr.thd() + TCUtils.N + " Прочного Материала");
		prs.add(" ");
		return prs;
	}
	
	public static LinkedList<String> getArmPrs(final String name, final float bfr, final float f) {
		final LinkedList<String> prs = new LinkedList<>();
		prs.add(" ");
		final Priced pr = Main.trades.get(name);
		if (pr.fst() > 0) prs.add(Arena.FIRST + (int) (pr.fst() * bfr * f) + TCUtils.N + " Хрупкого Материала");
		if (pr.scd() > 0) prs.add(Arena.SECOND + (int) (pr.scd() * bfr * f) + TCUtils.N + " Нормального Материала");
		if (pr.thd() > 0) prs.add(Arena.THIRD + (int) (pr.thd() * bfr * f) + TCUtils.N + " Прочного Материала");
		prs.add(" ");
		return prs;
	}
	
	public static LinkedList<String> getTlWpnPrs(final String name, final float bfr) {
		final LinkedList<String> prs = new LinkedList<>();
		prs.add(" ");
		final Priced pr = Main.trades.get(name);
		if (pr.fst() > 0) prs.add(Arena.FIRST + (int) (pr.fst() * bfr) + TCUtils.N + " Хрупкого Материала");
		if (pr.scd() > 0) prs.add(Arena.SECOND + (int) (pr.scd() * bfr) + TCUtils.N + " Нормального Материала");
		if (pr.thd() > 0) prs.add(Arena.THIRD + (int) (pr.thd() * bfr) + TCUtils.N + " Прочного Материала");
		prs.add(" ");
		return prs;
	}

	public static List<String> getCnsPrs(final String name, final String dscr) {
		return Arrays.asList(" ", TCUtils.A + Main.extras.getOrDefault(name, 0) + TCUtils.N + " Монет", " ", TCUtils.N + dscr, " ");
	}
	
	public static ItemStack mkItm(final Material m, final String dn, final boolean unbrk) {
		final ItemStack it = new ItemStack(m);
		final ItemMeta mt = it.getItemMeta();
		mt.setUnbreakable(unbrk);
		mt.displayName(TCUtils.format(dn));
		it.setItemMeta(mt);
		return it;
	}

	public static ItemStack mkItm(final Material m, final String dn, final List<String> lr, final byte amt, final boolean unbrk) {
		return new ItemBuilder(m).setAmount(amt).name(dn).addLore(lr)
			.setUnbreakable(unbrk).setItemFlag(ItemFlag.HIDE_UNBREAKABLE).build();
	}
}
