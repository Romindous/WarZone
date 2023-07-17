package me.Romindous.WarZone.Utils;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import me.Romindous.WarZone.Main;
import me.Romindous.WarZone.Game.Arena;
import me.Romindous.WarZone.Game.GameState;
import me.Romindous.WarZone.Game.Team;

public class Inventories {
	public static ItemStack[] fillTmInv(final String name) {
		final ItemStack[] its = new ItemStack[27];
		final Arena ar = Arena.getPlArena(name);
		byte i = 0;
		for ( ; i < 27; i++) {
			its[i] = mkItm(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§8-=-=-=-=-", true);;
		}
		its[13] = mkItm(Material.REDSTONE_TORCH, "§cВыход", true);
		i = 1;
		for (final Team tm : ar.getTms()) {
			final ItemStack it = new ItemStack(Material.LEATHER_HELMET);
			final LeatherArmorMeta lmt = (LeatherArmorMeta) it.getItemMeta();
			lmt.setDisplayName(tm.getName());
			lmt.setColor(Translates.ccToClr(ChatColor.getByChar(tm.getName().charAt(1))));
			lmt.setLore(Team.lrFromPls(tm));
			if (ar.getPlTeam(name) != null && ar.getPlTeam(name).getName().equalsIgnoreCase(tm.getName())) {
				lmt.addEnchant(Enchantment.LOYALTY, 1, true);
			}
			it.setItemMeta(lmt);
			its[i % 2 == 0 ? 13 - (int) (i * 0.5f) : 14 + (int) (i * 0.5f)] = it;
			i++;
		}
		return its;
	}

	public static ItemStack[] fillArInv(final int slots) {
		final YamlConfiguration ars = YamlConfiguration.loadConfiguration(new File(Main.folder + File.separator + "arenas.yml"));
		final ItemStack[] its = new ItemStack[slots];
		byte used = 0;
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
				its[i] = mkItm(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§8-=-=-=-=-", true);
				break;
				
			case 4:
				its[i] = mkItm(Material.LEATHER, "§6Выбор Карты", true);
				break;
				
			default:
				if (i > (slots - 10)) {
					its[i] = mkItm(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§8-=-=-=-=-", true);
				} else if (used < Main.nonactivearenas.size()) {
					final String anm = Arena.getByNum(used);
					final Arena ar = Main.activearenas.get(anm);
					if (ar == null) {
						its[i] = mkItm(Material.GREEN_CONCRETE_POWDER, "§a" + anm, 
								new LinkedList<String>(Arrays.asList("", "§2Ожидание (§7" + Main.nonactivearenas.get(anm).min + "§2)")), (byte) 1, true);
					} else {
						if (ar.getState() == GameState.LOBBY_WAIT) {
							its[i] = mkItm(Material.YELLOW_CONCRETE_POWDER, "§e" + anm, 
								new LinkedList<String>(Arrays.asList("", "§6Игроки: " + (ar.getPlAmt() < ar.getMin() ? ar.getPlAmt() + " из " + ar.getMin() : ar.getPlAmt() + " из " + ar.getMax()))), (byte) 1, true);
						} else {
							its[i] = mkItm(Material.RED_CONCRETE_POWDER, "§c" + anm, 
								new LinkedList<String>(Arrays.asList("", "§4Идет Игра", "", "§7Нажмите для наблюдения!")), (byte) 1, true);
						}
					}
					used++;
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
				its[i] = mkItm(Material.CAKE, "§6Еда", Arrays.asList(" ", "§7Покупка за §2Ресурсы"), (byte) 1, true);
				break;
			case 12:
				its[i] = mkItm(Material.WOODEN_SWORD, "§6Оружие", new LinkedList<String>(Arrays.asList(" ", "§7Покупка за §2Ресурсы")), (byte) 1, true);
				break;
			case 13:
				its[i] = mkItm(Material.getMaterial("WOODEN" + ar.getTlSfx()), "§6Инструменты", new LinkedList<String>(Arrays.asList(" ", "§7Покупка за §2Ресурсы")), (byte) 1, true);
				break;
			case 14:
				its[i] = mkItm(Material.TURTLE_HELMET, "§6Броня", new LinkedList<String>(Arrays.asList(" ", "§7Покупка за §2Ресурсы")), (byte) 1, true);
				break;
			case 16:
				its[i] = mkItm(Material.ENDER_EYE, "§6Разное", new LinkedList<String>(Arrays.asList(" ", "§7Покупка за §eМонеты")), (byte) 1, true);
				break;
			case 22:
				its[i] = mkItm(Material.BARRIER, "§cВыход", true);
				break;
			default:
				its[i] = mkItm(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§8-=-=-=-=-", true);
				break;
			}
		}
		return its;
	}
	
	public static ItemStack[] fillShpWpnInv(final PlayerInventory inv, final ChatColor cc) {
		final ItemStack[] its = new ItemStack[27];
		final ConfigurationSection cs = Main.plug.getConfig().getConfigurationSection("trade");
		final float bfr = 1f - 
			((inv.contains(Material.NETHERITE_SWORD) ? 1f : 
			(inv.contains(Material.IRON_SWORD) ? 0.7f : 
			(inv.contains(Material.GOLDEN_SWORD) ? 0.4f : 0f))));
		for (byte i = 0; i < 27; i++) {
			switch (i) {
			case 4:
				its[i] = mkItm(Material.WOODEN_SWORD, "§6Оружие", Arrays.asList(" ", "§7Покупка за §2Ресурсы"), (byte) 1, true);
				break;
			case 10:
				its[i] = mkItm(Material.GOLDEN_SWORD, cc + "Затупелый Меч", getTlWpnPrs(cs, "wpn.s1", bfr), (byte) 1, true);
				break;
			case 11:
				its[i] = mkItm(Material.IRON_SWORD, cc + "Нормальный Меч", getTlWpnPrs(cs, "wpn.s2", bfr), (byte) 1, true);
				break;
			case 12:
				its[i] = mkItm(Material.NETHERITE_SWORD, cc + "Остренный Меч", getTlWpnPrs(cs, "wpn.s3", bfr), (byte) 1, true);
				break;
			case 14:
				its[i] = mkItm(Material.CROSSBOW, cc + "Арбалет", getRecPrs(cs, "wpn.cb"), (byte) 1, true);
				break;
			case 15:
				its[i] = mkItm(Material.ARROW, cc + "Стрела", getRecPrs(cs, "wpn.ar"), (byte) 8, true);
				break;
			case 16:
				final ItemStack it = new ItemStack(Material.FIREWORK_ROCKET, 8);
				final FireworkMeta fm = (FireworkMeta) it.getItemMeta();
				fm.setDisplayName(cc + "Фейерверк");
				fm.setLore(getRecPrs(cs, "wpn.fw"));
				fm.addEffect(FireworkEffect.builder().with(Type.BURST).withColor(Translates.ccToClr(cc)).build());
				it.setItemMeta(fm);
				its[i] = it;
				break;
			case 22:
				its[i] = mkItm(Material.REDSTONE_TORCH, "§cНазад", true);
				break;
			default:
				its[i] = mkItm(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§8-=-=-=-=-", true);
				break;
			}
		}
		return its;
	}
	
	public static ItemStack[] fillShpTlInv(final PlayerInventory inv, final String sfx, final ChatColor cc) {
		final ItemStack[] its = new ItemStack[27];
		final ConfigurationSection cs = Main.plug.getConfig().getConfigurationSection("trade");
		final float bfr = 1 - 
			((inv.contains(Material.getMaterial("NETHERITE" + sfx)) ? 1 : 
			(inv.contains(Material.getMaterial("DIAMOND" + sfx)) ? 0.7f : 
			(inv.contains(Material.getMaterial("IRON" + sfx)) ? 0.4f : 0))));
		for (byte i = 0; i < 27; i++) {
			switch (i) {
			case 4:
				its[i] = mkItm(Material.getMaterial("STONE" + sfx), "§6Инструменты", Arrays.asList(" ", "§7Покупка за §2Ресурсы"), (byte) 1, true);
				break;
			case 10:
				its[i] = mkItm(Material.getMaterial("IRON" + sfx), cc + "Затупелый Инструмент", getTlWpnPrs(cs, "tl.t1", bfr), (byte) 1, true);
				break;
			case 12:
				its[i] = mkItm(Material.getMaterial("DIAMOND" + sfx), cc + "Нормальный Инструмент", getTlWpnPrs(cs, "tl.t2", bfr), (byte) 1, true);
				break;
			case 14:
				its[i] = mkItm(Material.getMaterial("NETHERITE" + sfx), cc + "Остренный Инструмент", getTlWpnPrs(cs, "tl.t3", bfr), (byte) 1, true);
				break;
			case 16:
				its[i] = mkItm(Material.SHIELD, cc + "Щит", getRecPrs(cs, "tl.sh"), (byte) 1, true);
				break;
			case 22:
				its[i] = mkItm(Material.REDSTONE_TORCH, "§cНазад", true);
				break;
			default:
				its[i] = mkItm(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§8-=-=-=-=-", true);
				break;
			}
		}
		return its;
	}
	
	public static ItemStack[] fillShpFdInv(final ChatColor cc) {
		final ItemStack[] its = new ItemStack[27];
		final ConfigurationSection cs = Main.plug.getConfig().getConfigurationSection("trade");
		for (byte i = 0; i < 27; i++) {
			switch (i) {
			case 4:
				its[i] = mkItm(Material.CAKE, "§6Еда", Arrays.asList(" ", "§7Покупка за §2Ресурсы"), (byte) 1, true);
				break;
			case 10:
				its[i] = mkItm(Material.APPLE, null, getRecPrs(cs, "fd.f1"), (byte) 4, true);
				break;
			case 12:
				its[i] = mkItm(Material.PUMPKIN_PIE, null, getRecPrs(cs, "fd.f2"), (byte) 2, true);
				break;
			case 13:
				its[i] = mkItm(Material.COOKED_PORKCHOP, null, getRecPrs(cs, "fd.f3"), (byte) 2, true);
				break;
			case 14:
				its[i] = mkItm(Material.GOLDEN_APPLE, null, getRecPrs(cs, "fd.f4"), (byte) 2, true);
				break;
			case 16:
				its[i] = mkItm(Material.ENCHANTED_GOLDEN_APPLE, null, getRecPrs(cs, "fd.f5"), (byte) 1, true);
				break;
			case 22:
				its[i] = mkItm(Material.REDSTONE_TORCH, "§cНазад", true);
				break;
			default:
				its[i] = mkItm(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§8-=-=-=-=-", true);
				break;
			}
		}
		return its;
	}
	
	public static ItemStack[] fillShpArmrInv(final PlayerInventory inv, final ChatColor cc) {
		final ItemStack[] its = new ItemStack[54];
		final ConfigurationSection cs = Main.plug.getConfig().getConfigurationSection("trade");
		final float bfr = 1 - getArmBfr(inv.getArmorContents());
		for (byte i = 0; i < 54; i++) {
			switch (i) {
			case 4:
				its[i] = mkItm(Material.TURTLE_HELMET, "§6Броня", Arrays.asList(" ", "§7Покупка за §2Ресурсы"), (byte) 1, true);
				break;
				
			case 10:
				its[i] = mkItm(Material.LEATHER_HELMET, cc + "Растрепаный Шлем", getArmPrs(cs, "armr.a1", bfr, 0.2f), (byte) 1, true);
				break;
			case 11:
				its[i] = mkItm(Material.CHAINMAIL_HELMET, cc + "Паршивый Шлем", getArmPrs(cs, "armr.a2", bfr, 0.2f), (byte) 1, true);
				break;
			case 13:
				its[i] = mkItm(Material.IRON_HELMET, cc + "Нормальный Шлем", getArmPrs(cs, "armr.a3", bfr, 0.2f), (byte) 1, true);
				break;
			case 15:
				its[i] = mkItm(Material.DIAMOND_HELMET, cc + "Качественный Шлем", getArmPrs(cs, "armr.a4", bfr, 0.2f), (byte) 1, true);
				break;
			case 16:
				its[i] = mkItm(Material.NETHERITE_HELMET, cc + "Безупречный Шлем", getArmPrs(cs, "armr.a5", bfr, 0.2f), (byte) 1, true);
				break;
				
			case 19:
				its[i] = mkItm(Material.LEATHER_CHESTPLATE, cc + "Растрепаный Нагрудник", getArmPrs(cs, "armr.a1", bfr, 0.4f), (byte) 1, true);
				break;
			case 20:
				its[i] = mkItm(Material.CHAINMAIL_CHESTPLATE, cc + "Паршивый Нагрудник", getArmPrs(cs, "armr.a2", bfr, 0.4f), (byte) 1, true);
				break;
			case 22:
				its[i] = mkItm(Material.IRON_CHESTPLATE, cc + "Нормальный Нагрудник", getArmPrs(cs, "armr.a3", bfr, 0.4f), (byte) 1, true);
				break;
			case 24:
				its[i] = mkItm(Material.DIAMOND_CHESTPLATE, cc + "Качественный Нагрудник", getArmPrs(cs, "armr.a4", bfr, 0.4f), (byte) 1, true);
				break;
			case 25:
				its[i] = mkItm(Material.NETHERITE_CHESTPLATE, cc + "Безупречный Нагрудник", getArmPrs(cs, "armr.a5", bfr, 0.4f), (byte) 1, true);
				break;
				
			case 28:
				its[i] = mkItm(Material.LEATHER_LEGGINGS, cc + "Растрепаные Штаны", getArmPrs(cs, "armr.a1", bfr, 0.3f), (byte) 1, true);
				break;
			case 29:
				its[i] = mkItm(Material.CHAINMAIL_LEGGINGS, cc + "Паршивые Штаны", getArmPrs(cs, "armr.a2", bfr, 0.3f), (byte) 1, true);
				break;
			case 31:
				its[i] = mkItm(Material.IRON_LEGGINGS, cc + "Нормальные Штаны", getArmPrs(cs, "armr.a3", bfr, 0.3f), (byte) 1, true);
				break;
			case 33:
				its[i] = mkItm(Material.DIAMOND_LEGGINGS, cc + "Качественные Штаны", getArmPrs(cs, "armr.a4", bfr, 0.3f), (byte) 1, true);
				break;
			case 34:
				its[i] = mkItm(Material.NETHERITE_LEGGINGS, cc + "Безупречные Штаны", getArmPrs(cs, "armr.a5", bfr, 0.3f), (byte) 1, true);
				break;
				
			case 37:
				its[i] = mkItm(Material.LEATHER_BOOTS, cc + "Растрепаные Ботинки", getArmPrs(cs, "armr.a1", bfr, 0.1f), (byte) 1, true);
				break;
			case 38:
				its[i] = mkItm(Material.CHAINMAIL_BOOTS, cc + "Паршивые Ботинки", getArmPrs(cs, "armr.a2", bfr, 0.1f), (byte) 1, true);
				break;
			case 40:
				its[i] = mkItm(Material.IRON_BOOTS, cc + "Нормальные Ботинки", getArmPrs(cs, "armr.a3", bfr, 0.1f), (byte) 1, true);
				break;
			case 42:
				its[i] = mkItm(Material.DIAMOND_BOOTS, cc + "Качественные Ботинки", getArmPrs(cs, "armr.a4", bfr, 0.1f), (byte) 1, true);
				break;
			case 43:
				its[i] = mkItm(Material.NETHERITE_BOOTS, cc + "Безупречные Ботинки", getArmPrs(cs, "armr.a5", bfr, 0.1f), (byte) 1, true);
				break;
				
			case 49:
				its[i] = mkItm(Material.REDSTONE_TORCH, "§cНазад", true);
				break;
			default:
				its[i] = mkItm(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§8-=-=-=-=-", true);
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
		final ConfigurationSection cs = Main.plug.getConfig().getConfigurationSection("trade");
		for (byte i = 0; i < 27; i++) {
			switch (i) {
			case 4:
				its[i] = mkItm(Material.ENDER_EYE, "§6Разное", Arrays.asList(" ", "§7Покупка за §eМонеты"), (byte) 1, true);
				break;
			case 10:
				its[i] = mkItm(Material.ENCHANTED_BOOK, "§eЗачарование", Arrays.asList(" ", "§6" + cs.getString("xtr.ench") + " + (" + cs.getString("xtr.dlvl") + " * lvl)" + "§7 Монет", " ", "§7Кликните сюда предметом для его §eзачарования§7!", " "), (byte) 1, true);
				break;
			case 12:
				its[i] = mkItm(Material.TOTEM_OF_UNDYING, "§e+1 Возрождение", Arrays.asList(" ", "§6" + cs.getString("xtr.resp") + "§7 Монет", " ", "§7Добавляет одно §eвозрождение §7ващей комманде!", " "), (byte) 1, true);
				break;
			case 14:
				ItemStack it = new ItemStack(Material.SPLASH_POTION);
				PotionMeta pm = (PotionMeta) it.getItemMeta();
				pm.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL));
				pm.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1), true);
				pm.setDisplayName("§eЛечащее Зелье");
				pm.setLore(getCnsPrs(cs, "xtr.hlt", "§7Имеет §eлечащие §7свойства!"));
				it.setItemMeta(pm);
				its[i] = it;
				break;
			case 15:
				it = new ItemStack(Material.SPLASH_POTION);
				pm = (PotionMeta) it.getItemMeta();
				pm.setBasePotionData(new PotionData(PotionType.SPEED));
				pm.addCustomEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 500, 1), true);
				pm.setDisplayName("§eЗелье Ускорения");
				pm.setLore(getCnsPrs(cs, "xtr.spd", "§eУскоряет §7вас!"));
				it.setItemMeta(pm);
				its[i] = it;
				break;
			case 16:
				it = new ItemStack(Material.SPLASH_POTION);
				pm = (PotionMeta) it.getItemMeta();
				pm.setBasePotionData(new PotionData(PotionType.STRENGTH));
				pm.setDisplayName("§eЗелье Силы");
				pm.setLore(getCnsPrs(cs, "xtr.str", "§7Дарует §eсилу§7!"));
				it.setItemMeta(pm);
				its[i] = it;
				break;
			case 22:
				its[i] = mkItm(Material.REDSTONE_TORCH, "§cНазад", true);
				break;
			default:
				its[i] = mkItm(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§8-=-=-=-=-", true);
				break;
			}
		}
		return its;
	}

	public static LinkedList<String> getRecPrs(final ConfigurationSection cs, final String pth) {
		final LinkedList<String> prs = new LinkedList<String>();
		prs.add(" ");
		for (final String s : cs.getString(pth).split(" ")) {
			switch (s.charAt(0)) {
			case 'f':
				prs.add("§a" + s.substring(1) + "§7 Хрупкого Материала");
				break;
			case 's':
				prs.add("§3" + s.substring(1) + "§7 Нормального Материала");
				break;
			case 't':
				prs.add("§2" + s.substring(1) + "§7 Прочного Материала");
				break;
			default:
				prs.add("§a100" + "§7 Хрупкого Материала");
				break;
			}
		}
		prs.add(" ");
		return prs;
	}
	
	public static LinkedList<String> getArmPrs(final ConfigurationSection cs, final String pth, final float bfr, final float f) {
		final LinkedList<String> prs = new LinkedList<String>();
		prs.add(" ");
		for (final String s : cs.getString(pth).split(" ")) {
			switch (s.charAt(0)) {
			case 'f':
				prs.add("§a" + (int) (Integer.parseInt(s.substring(1)) * bfr * f) + "§7 Хрупкого Материала");
				break;
			case 's':
				prs.add("§3" + (int) (Integer.parseInt(s.substring(1)) * bfr * f) + "§7 Нормального Материала");
				break;
			case 't':
				prs.add("§2" + (int) (Integer.parseInt(s.substring(1)) * bfr * f) + "§7 Прочного Материала");
				break;
			default:
				prs.add("§a100§7 Хрупкого Материала");
				break;
			}
		}
		prs.add(" ");
		return prs;
	}
	
	public static LinkedList<String> getTlWpnPrs(final ConfigurationSection cs, final String pth, final float bfr) {
		final LinkedList<String> prs = new LinkedList<String>();
		prs.add(" ");
		for (final String s : cs.getString(pth).split(" ")) {
			switch (s.charAt(0)) {
			case 'f':
				prs.add("§a" + (int) (Integer.parseInt(s.substring(1)) * bfr) + "§7 Хрупкого Материала");
				break;
			case 's':
				prs.add("§3" + (int) (Integer.parseInt(s.substring(1)) * bfr) + "§7 Нормального Материала");
				break;
			case 't':
				prs.add("§2" + (int) (Integer.parseInt(s.substring(1)) * bfr) + "§7 Прочного Материала");
				break;
			default:
				prs.add("§a100§7 Хрупкого Материала");
				break;
			}
		}
		prs.add(" ");
		return prs;
	}

	public static List<String> getCnsPrs(final ConfigurationSection cs, final String pth, final String dscr) {
		return Arrays.asList(" ", "§6" + cs.getString(pth) + "§7 Монет", " ", dscr, " ");
	}
	
	public static ItemStack mkItm(final Material m, final String dn, final boolean unbrk) {
		final ItemStack it = new ItemStack(m);
		final ItemMeta mt = it.getItemMeta();
		mt.setUnbreakable(unbrk);
		mt.setDisplayName(dn);
		it.setItemMeta(mt);
		return it;
	}

	public static ItemStack mkItm(final Material m, final String dn, final List<String> lr, final byte amt, final boolean unbrk) {
		final ItemStack it = new ItemStack(m, amt);
		final ItemMeta mt = it.getItemMeta();
		mt.setLore(lr);
		mt.setUnbreakable(unbrk);
		mt.setDisplayName(dn);
		it.setItemMeta(mt);
		return it;
	}
}
