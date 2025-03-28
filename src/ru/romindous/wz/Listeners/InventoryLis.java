package ru.romindous.wz.Listeners;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.wz.Game.Arena;
import ru.romindous.wz.Game.PlWarrior;
import ru.romindous.wz.Main;
import ru.romindous.wz.Utils.Inventories;
import ru.romindous.wz.Utils.Priced;

public class InventoryLis implements Listener{	
	
	@EventHandler
	public void onOpen(final InventoryOpenEvent e) {
		e.setCancelled(e.getInventory().getType() == InventoryType.MERCHANT);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onClick(final InventoryClickEvent e) {
		final Player p = (Player) e.getWhoClicked();
		final PlWarrior pw = PM.getOplayer(p, PlWarrior.class);
		final Arena ar = pw.arena();
		//правая - лева рука и числа
		if (ar == null && (e.getClick() == ClickType.NUMBER_KEY || e.getClick() == ClickType.SWAP_OFFHAND)) {
			e.setCancelled(true);
			return;
		}
		final ItemStack it = e.getCurrentItem();
		//клик на ничего?
		if (ItemUtil.isBlank(it, false)) return;
		final String nm = it.hasItemMeta() && it.getItemMeta().hasDisplayName() 
			? TCUtil.strip(it.getItemMeta().displayName()) : "";
		//спектаторы
		if (p.getGameMode() == GameMode.SPECTATOR
			&& it.getType() == Material.REDSTONE) {
			e.setCancelled(true);
			ar.remSpct(p);
			return;
		}
		
		final String inm = TCUtil.strip(e.getView().title());
		if (e.getClickedInventory() instanceof PlayerInventory) {
			//передвигает вещи которые не надо
			if (nm.contains("Выбор") || nm.contains("Выход")) {
				e.setCancelled(true);
				p.setItemOnCursor(null);
            }
		} else if (inm.contains("Карты")) {
			e.setCancelled(true);
			switch (it.getType()) {
			case GREEN_CONCRETE_POWDER, YELLOW_CONCRETE_POWDER:
				p.performCommand("wz join " + nm);
				p.closeInventory();
				break;
			case RED_CONCRETE_POWDER:
				p.sendMessage(Main.PRFX + "§cНа этой карте уже идет игра!");
				Main.activearenas.get(nm).addSpct(p, pw);
				break;	
			default:
				break;
			}
		} else if (inm.contains("Магазина")) {
			e.setCancelled(true);
			final Inventory inv;
			switch (it.getType()) {
			case BARRIER:
				p.closeInventory();
				return;
			case CAKE:
				inv = Bukkit.createInventory(p, 27, TCUtil.form(TCUtil.P + "Магазин Еды"));
				inv.setContents(Inventories.fillShpFdInv());
				p.closeInventory();
				p.openInventory(inv);
				break;
			case WOODEN_SWORD:
				inv = Bukkit.createInventory(p, 27, TCUtil.form(TCUtil.P + "Магазин Оружия"));
				inv.setContents(Inventories.fillShpWpnInv(p.getInventory(), pw.team()));
				p.closeInventory();
				p.openInventory(inv);
				break;
			case TURTLE_HELMET:
				inv = Bukkit.createInventory(p, 54, TCUtil.form(TCUtil.P + "Магазин Брони"));
				inv.setContents(Inventories.fillShpArmrInv(p.getInventory(), pw.team()));
				p.closeInventory();
				p.openInventory(inv);
				break;
			case ENDER_EYE:
				inv = Bukkit.createInventory(p, 27, TCUtil.form(TCUtil.P + "Магазин Разного"));
				inv.setContents(Inventories.fillShpXtrInv());
				p.closeInventory();
				p.openInventory(inv);
				break;
			default:
				if (it.getType() == Material.getMaterial("WOODEN" + ar.getTlSfx())) {
					inv = Bukkit.createInventory(p, 27, TCUtil.form(TCUtil.P + "Магазин Инструментов"));
					inv.setContents(Inventories.fillShpTlInv(p.getInventory(), ar.getTlSfx(), pw.team()));
					p.closeInventory();
					p.openInventory(inv);
				}
				break;
			}
		} else if (inm.contains("Еды")) {
			e.setCancelled(true);
			switch (it.getType()) {
			case REDSTONE_TORCH:
				p.closeInventory();
				final Inventory inv = Bukkit.createInventory(p, 27, TCUtil.form(TCUtil.P + "Выбор Магазина"));
				inv.setContents(Inventories.fillShpInv(ar));
				p.openInventory(inv);
				return;
			case APPLE:
			case PUMPKIN_PIE:
			case COOKED_PORKCHOP:
			case GOLDEN_APPLE:
			case ENCHANTED_GOLDEN_APPLE:
				if (canResBuy(p.getInventory(), it, ar.recs)) {
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_TRADE, 1, 1);
					remIts(p.getInventory(), it, ar.recs);
					p.getInventory().addItem(strpLr(it.clone()));
				} else {
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
					p.sendMessage(Main.PRFX + "§cУ вас не хватает ресурсов для покупки этого!");
				}
				break;
			default:
				break;
			}
		} else if (inm.contains("Оружия")) {
			e.setCancelled(true);
			final PlayerInventory pi = p.getInventory();
			switch (it.getType()) {
			case LIGHT_GRAY_STAINED_GLASS_PANE:
			case WOODEN_SWORD:
				break;
			case ARROW:
			case FIREWORK_ROCKET:
				if (canResBuy(pi, it, ar.recs)) {
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_TRADE, 1, 1);
					remIts(pi, it, ar.recs);
					pi.addItem(strpLr(it.clone()));
				} else {
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
					p.sendMessage(Main.PRFX + "§cУ вас не хватает ресурсов для покупки этого!");
				}
				break;
			case CROSSBOW:
				if (canResBuy(pi, it, ar.recs)) {
					if (pi.contains(Material.CROSSBOW)) {
						p.sendMessage(Main.PRFX + "§cУ вас уже есть это оружие!");
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
					} else {
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
						remIts(pi, it, ar.recs);
						pi.addItem(strpLr(it.clone()));
					}
				} else {
					p.sendMessage(Main.PRFX + "§cУ вас не хватает ресурсов для покупки этого!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
				}
				break;
			case REDSTONE_TORCH:
				p.closeInventory();
				final Inventory inv = Bukkit.createInventory(p, 27, TCUtil.form(TCUtil.P + "Выбор Магазина"));
				inv.setContents(Inventories.fillShpInv(ar));
				p.openInventory(inv);
				return;
			default:
				if (canResBuy(pi, it, ar.recs)) {
					final int stage;
					if (it.getType() == Priced.FST_WEAPON) {
						stage = 0;
					} else if (it.getType() == Priced.SCD_WEAPON) {
						stage = 1;
					} else if (it.getType() == Priced.THD_WEAPON) {
						stage = 2;
					} else stage = 0;

					if (chkIfHas(pi, Arrays.copyOfRange(Priced.WEAPONS, stage, Priced.WEAPONS.length))) {
						p.sendMessage(Main.PRFX + "§cУ тебя уже есть это оружие, или что то получше!");
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
						return;
					}

					remIts(pi, it, ar.recs);
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
					final ItemStack nit = strpLr(it.clone());
					nit.addEnchantments(remIt(pi, false, Arrays.copyOf(Priced.WEAPONS, stage)));
					pi.addItem(nit);
					p.closeInventory();
					final Inventory ninv = Bukkit.createInventory(p, 27, TCUtil.form(TCUtil.P + "Магазин Оружия"));
					ninv.setContents(Inventories.fillShpWpnInv(pi, pw.team()));
					p.openInventory(ninv);
				} else {
					p.sendMessage(Main.PRFX + "§cУ вас не хватает ресурсов для покупки этого!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
					return;
				}
				break;
			}
		} else if (inm.contains("Инстр")) {
			e.setCancelled(true);
			switch (it.getType()) {
			case LIGHT_GRAY_STAINED_GLASS_PANE:
				break;
			case SHIELD:
				if (p.getInventory().contains(it.getType())) {
					p.sendMessage(Main.PRFX + "§cУ тебя уже есть щит!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
					return;
				}

				if (canResBuy(p.getInventory(), it, ar.recs)) {
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
					remIts(p.getInventory(), it, ar.recs);
					p.getInventory().addItem(strpLr(it.clone()));
				} else {
					p.sendMessage(Main.PRFX + "§cУ вас не хватает ресурсов для покупки этого!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
				}
				break;
			case REDSTONE_TORCH:
				p.closeInventory();
				final Inventory inv = Bukkit.createInventory(p, 27, TCUtil.form(TCUtil.P + "Выбор Магазина"));
				inv.setContents(Inventories.fillShpInv(ar));
				p.openInventory(inv);
				return;
			default:
				final PlayerInventory pi = p.getInventory();
				if (canResBuy(pi, it, ar.recs)) {
					final Material[] mts = Priced.collect(ar.getTlSfx(),
						Priced.ZRT_TOOL, Priced.FST_TOOL, Priced.SCD_TOOL, Priced.THD_TOOL);
					final int stage;
					final String tnm = it.getType().name();
					if (tnm.startsWith(Priced.FST_TOOL)) {
						stage = 1;
					} else if (tnm.startsWith(Priced.SCD_TOOL)) {
						stage = 2;
					} else if (tnm.startsWith(Priced.THD_TOOL)) {
						stage = 3;
					} else stage = 0;

					if (chkIfHas(pi, Arrays.copyOfRange(mts, stage, mts.length))) {
						p.sendMessage(Main.PRFX + "§cУ тебя уже есть этот инструмент, или что-то получше!");
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
						return;
					}

					remIts(pi, it, ar.recs);
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
					final ItemStack nit = strpLr(it.clone());
					nit.addEnchantments(remIt(pi, false, Arrays.copyOf(mts, stage)));
					pi.addItem(nit);
					final Inventory ninv = Bukkit.createInventory(p, 27, TCUtil.form(TCUtil.P + "Магазин Инструментов"));
					ninv.setContents(Inventories.fillShpTlInv(p.getInventory(), ar.getTlSfx(), pw.team()));
					p.closeInventory();
					p.openInventory(ninv);
				} else {
					p.sendMessage(Main.PRFX + "§cУ вас не хватает ресурсов для покупки этого!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
				}
				break;
			}
		} else if (inm.contains("Брони")) {
			e.setCancelled(true);
			switch (it.getType()) {
			case TURTLE_HELMET:
			case LIGHT_GRAY_STAINED_GLASS_PANE:
				break;
			case REDSTONE_TORCH:
				p.closeInventory();
				final Inventory inv = Bukkit.createInventory(p, 27, TCUtil.form(TCUtil.P + "Выбор Магазина"));
				inv.setContents(Inventories.fillShpInv(ar));
				p.openInventory(inv);
				return;
			default:
				final PlayerInventory pi = p.getInventory();
				if (canResBuy(pi, it, ar.recs)) {
					final Material[] mts = switch (it.getType().getEquipmentSlot()) {
						case HAND, OFF_HAND, SADDLE -> new Material[] {};
                        case FEET -> Priced.FEET;
                        case LEGS -> Priced.LEGS;
                        case CHEST, BODY -> Priced.CHEST;
                        case HEAD -> Priced.HEAD;
                    };

					final int stage;
					final String tnm = it.getType().name();
					if (tnm.startsWith(Priced.FST_ARMOR)) {
						stage = 0;
					} else if (tnm.startsWith(Priced.SCD_ARMOR)) {
						stage = 1;
					} else if (tnm.startsWith(Priced.THD_ARMOR)) {
						stage = 2;
					} else if (tnm.startsWith(Priced.FRT_ARMOR)) {
						stage = 3;
					} else if (tnm.startsWith(Priced.FFT_ARMOR)) {
						stage = 4;
					} else stage = 0;
					if (chkIfHas(pi, Arrays.copyOfRange(mts, stage, mts.length))) {
						p.sendMessage(Main.PRFX + "§cУ вас уже есть эта бронь, или что-то получше!");
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
						return;
					}

					remIts(pi, it, ar.recs);
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
					final ItemStack nit = strpLr(it.clone());
					nit.addEnchantments(remIt(pi, false, Arrays.copyOf(mts, stage)));
					pi.setItem(it.getType().getEquipmentSlot(), nit);
					final Inventory ninv = Bukkit.createInventory(p, 54, TCUtil.form(TCUtil.P + "Магазин Брони"));
					ninv.setContents(Inventories.fillShpArmrInv(pi, pw.team()));
					p.closeInventory();
					p.openInventory(ninv);
				} else {
					p.sendMessage(Main.PRFX + "§cУ вас не хватает ресурсов для покупки этого!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
					return;
				}
				break;
			}
		} else if (inm.contains("Разного")) {
			e.setCancelled(true);
			final int prc;
			switch (it.getType()) {
			case TOTEM_OF_UNDYING:
				prc = Main.extras.getOrDefault("resp", 0);
				if (pw.coins() < prc) {
					p.sendMessage(Main.PRFX + "§cУ тебя не хватает монет!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
					return;
				}

				p.playSound(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
				if (pw.team() != null) {
					pw.team().rsps++;
					final String msg = Main.PRFX + pw.team().name("ая", true) + TCUtil.N
						+ " комманда приобрела себе " + TCUtil.A + "+1 " + TCUtil.N + "возрождение!";
					for (final PlWarrior plw : ar.getPls().values()) {
						plw.getPlayer().sendMessage(msg);
						plw.score.getSideBar().update(pw.team().color(), pw.team().desc(plw));
					}
					pw.coins(-prc);
				}
				break;
			case SPLASH_POTION:
				prc = switch (((PotionMeta) it.getItemMeta()).getBasePotionType()) {
					case HEALING -> Main.extras.getOrDefault("health", 0);
					case SWIFTNESS -> Main.extras.getOrDefault("speed", 0);
					case STRENGTH -> Main.extras.getOrDefault("strong", 0);
					default -> 0;
				};
				if (pw.coins() < prc) {
					p.sendMessage(Main.PRFX + "§cУ тебя не хватает монет!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
					return;
				}

				p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
				p.getInventory().addItem(strpLr(it.clone(), 2));
				pw.coins(-prc);
				break;
			case ENCHANTED_BOOK:
				//есть что то в курсоре?
				final ItemStack cr = e.getCursor();
				if (ItemUtil.isBlank(cr, false)) {
					p.sendMessage(Main.PRFX + "§cНажми на книгу предметом, который хочешь зачаровать!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
					return;
				}
				
				final String m = cr.getType().name();
				final Enchantment en;
				if (m.endsWith("_SWORD")) {
					en = Enchantment.SHARPNESS;
				} else if (m.endsWith(ar.getTlSfx())) {
					en = Enchantment.EFFICIENCY;
				} else if (m.endsWith("HELMET") || m.contains("CHESTPLATE") || m.contains("LEGGINGS") || m.contains("BOOTS")) {
					en = Enchantment.PROTECTION;
				} else if (m.endsWith("BOW")) {
					en = Enchantment.QUICK_CHARGE;
				} else {
					p.sendMessage(Main.PRFX + "§cЭтот предмет нельзя зачаровать!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
					return;
				}
				
				if (cr.getEnchantmentLevel(en) == Main.mxEnchLvl) {
					p.sendMessage(Main.PRFX + "§cЭтот предмет уже максимально зачарован!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
					return;
				}

				prc = Main.extras.getOrDefault("dlvl", 0) *
					cr.getEnchantmentLevel(en) + Main.extras.getOrDefault("ench", 0);
				if (pw.coins() < prc) {
					p.sendMessage(Main.PRFX + "§cУ тебя не хватает монет!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
					return;
				}

				p.playSound(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
				p.setItemOnCursor(addEnch(cr, en));
				pw.coins(-prc);
				
				break;
			case REDSTONE_TORCH:
				p.closeInventory();
				final Inventory inv = Bukkit.createInventory(p, 27, TCUtil.form(TCUtil.P + "Выбор Магазина"));
				inv.setContents(Inventories.fillShpInv(ar));
				p.openInventory(inv);
				return;
			default:
				break;
			}
		}
	}
	public static HashMap<Enchantment, Integer> remIt(final PlayerInventory inv, final boolean armr, final Material[] mts) {
		final HashMap<Enchantment, Integer> ens = new HashMap<>();
		for (final ItemStack it : inv) {
			if (it == null) continue;
			final Material mt = it.getType();
			for (final Material m : mts) {
				if (mt != m) continue;
				if (it.hasItemMeta()) {
					for (final Map.Entry<Enchantment, Integer> en : it.getItemMeta().getEnchants().entrySet()) {
						final Integer pl = ens.get(en.getKey());
						if (pl == null || pl < en.getValue()) {
							ens.put(en.getKey(), en.getValue());
						}
					}
				}
				it.setAmount(0);
//				inv.remove(m);
//				if (inv.getItemInOffHand().getType() == m) inv.setItemInOffHand(null);
//				if (armr) inv.setArmorContents(remFromArmr(inv.getArmorContents(), m));
			}
		}
		return ens;
	}

	public static ItemStack[] remFromArmr(final ItemStack[] cnts, final Material m) {
		final ItemStack[] nevv = cnts.clone();
		for (byte i = 0; i < nevv.length; i++) {
			if (nevv[i] == null || nevv[i].getType() == m) {
				nevv[i] = null;
			}
		}
		return nevv;
	}
	
	public static ItemStack addEnch(final ItemStack it, final Enchantment en) {
		final ItemMeta mt = it.getItemMeta();
		mt.addEnchant(en, mt.hasEnchant(en) ? mt.getEnchantLevel(en) + 1 : 1, true);
		it.setItemMeta(mt);
		return it;
	}

	//проверяем если у человека есть предмет
	public static boolean chkIfHas(final PlayerInventory inv, final Material[] mts) {
		for (final Material m : mts) {
			if (inv.contains(m) || inv.getItemInOffHand().getType() == m) return true;
			for (final ItemStack i : inv.getArmorContents()) {
				if (i != null && i.getType() == m) return true;
			}
		}
		return false;
	}
	//убираем лор
	public static ItemStack strpLr(final ItemStack it) {
		final ItemMeta mt = it.getItemMeta();
		mt.lore(null);
		it.setItemMeta(mt);
		return it;
	}
	//убираем лор
	public static ItemStack strpLr(final ItemStack it, int rws) {
		final ItemMeta mt = it.getItemMeta();
		final List<Component> lr = mt.lore();
		if (lr == null) return it;
		for (rws--; rws >= 0; rws--) {
			lr.remove(rws);
		}
		mt.lore(lr);
		it.setItemMeta(mt);
		return it;
	}
	//может ли игрок купить это?
	public static boolean canResBuy(final PlayerInventory inv, final ItemStack it, final Material[] mts) {
		if (!it.hasItemMeta() || !it.getItemMeta().hasLore()) return false;
		for (final Component c : it.getItemMeta().lore()) {
			final String s = TCUtil.strip(c);

			final int mt;
			if (s.indexOf(Priced.FST_MAT) > 0) mt = 0;
			else if (s.indexOf(Priced.SCD_MAT) > 0) mt = 1;
			else if (s.indexOf(Priced.THD_MAT) > 0) mt = 2;
			else continue;

			if (!hasAtLeast(inv, mts[mt], NumUtil.intOf(s.substring(0, s.indexOf(' ')), 0)))
				return false;
		}
		return true;
	}
	
	public static boolean hasAtLeast(final PlayerInventory inv, final Material mt, int amt) {
		for (final ItemStack it : inv) {
			if (it != null && it.getType() == mt) {
				amt -= it.getAmount();
			}
			if (amt <= 0) return true;
		}
		return false;
	}
	
	//убираем ресурсы из инвентаря
	public static void remIts(final PlayerInventory inv, final ItemStack it, final Material[] mts) {
		if (!it.hasItemMeta() || !it.getItemMeta().hasLore()) return;
		for (final Component c : it.getItemMeta().lore()) {
			final String s = TCUtil.strip(c);
//			inv.getHolder().sendMessage(s);
			final int mt;
			if (s.indexOf(Priced.FST_MAT) > 0) mt = 0;
			else if (s.indexOf(Priced.SCD_MAT) > 0) mt = 1;
			else if (s.indexOf(Priced.THD_MAT) > 0) mt = 2;
			else continue;

			int amt = NumUtil.intOf(s.substring(0, s.indexOf(' ')), 0);
//			inv.getHolder().sendMessage(String.valueOf(amt));
			if (amt > 0) {
				final ItemStack ofh = inv.getItemInOffHand();
				if (ofh.getType() == mts[mt]) {
					if (amt - ofh.getAmount() < 1) {
						ofh.setAmount(ofh.getAmount() - amt);
						inv.setItemInOffHand(ofh);
						continue;
					}
					amt -= ofh.getAmount();
					inv.setItemInOffHand(ItemUtil.air);
				}

				while (true) {
					final int slt = inv.first(mts[mt]);
					if (slt == -1) break;
					final ItemStack i = inv.getItem(slt);
					if (i == null) break;
					if (amt - i.getAmount() < 1) {
						i.setAmount(i.getAmount() - amt);
						inv.setItem(slt, i);
						break;
					}
					amt -= i.getAmount();
					inv.setItem(slt, null);
				}
			}
		}
	}
}
