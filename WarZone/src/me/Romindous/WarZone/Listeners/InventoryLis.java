package me.Romindous.WarZone.Listeners;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import me.Romindous.WarZone.Utils.TitleManager;
import me.Romindous.WarZone.Main;
import me.Romindous.WarZone.Game.Arena;
import me.Romindous.WarZone.Game.Team;
import me.Romindous.WarZone.Utils.EntMeta;
import me.Romindous.WarZone.Utils.Inventories;

public class InventoryLis implements Listener{	
	
	@EventHandler
	public void onOpen(final InventoryOpenEvent e) {
		e.setCancelled(e.getInventory().getType() == InventoryType.MERCHANT);
	}
	
	@EventHandler
	public void onClick(final InventoryClickEvent e) {
		final Player p = (Player) e.getWhoClicked();
		//правая - лева рука и числа
		if (!p.hasMetadata("kls") && (e.getClick() == ClickType.NUMBER_KEY || e.getClick() == ClickType.SWAP_OFFHAND)) {
			e.setCancelled(true);
			return;
		}
		//клик на ничего?
		if (!Main.notItmNull(e.getCurrentItem())) {
			return;
		}
		//спектаторы
		if (p.getGameMode() == GameMode.SPECTATOR && e.getCurrentItem().getType() == Material.REDSTONE) {
			e.setCancelled(true);
			for (final Arena ar : Main.activearenas) {
				ar.getSpcs().remove(p.getName());
			}
			Main.lobbyPlayer((Player) p);
			p.sendMessage(Main.prf() + "Перемещаем вас обратно в лобби!");
			return;
		}
		
		if (e.getClickedInventory() instanceof PlayerInventory) {
			//передвигает вещи которые не надо
			if (e.getCurrentItem().getItemMeta().getDisplayName().contains("Выбор") || e.getCurrentItem().getItemMeta().getDisplayName().contains("Выход")) {
				e.setCancelled(true);
				e.getCursor().setType(Material.AIR);
				return;
			}
		} else if (e.getView().getTitle().contains("Карты")) {
			e.setCancelled(true);
			switch (e.getCurrentItem().getType()) {
			case LIGHT_GRAY_STAINED_GLASS_PANE:
				break;
			case LEATHER:
				TitleManager.sendBack(p);
				break;
			case GREEN_CONCRETE_POWDER:
				if (e.getCurrentItem().getItemMeta().getDisplayName().contains("§a")) {
					p.performCommand("wz join " + e.getCurrentItem().getItemMeta().getDisplayName().substring(2));
					p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 80, 1);
					p.closeInventory();
				}
				break;
			case YELLOW_CONCRETE_POWDER:
				if (e.getCurrentItem().getItemMeta().getDisplayName().contains("§e")) {
					p.performCommand("wz join " + e.getCurrentItem().getItemMeta().getDisplayName().substring(2));
					p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 80, 1);
					p.closeInventory();
				}
				break;
			case RED_CONCRETE_POWDER:
				if (e.getCurrentItem().getItemMeta().getDisplayName().contains("§c")) {
					p.sendMessage(Main.prf() + "§cНа этой карте уже идет игра!");
					Arena.getNameArena(e.getCurrentItem().getItemMeta().getDisplayName().substring(2)).addSpct(p);
				}
				break;	
			default:
				break;
			}
		} else if (e.getView().getTitle().contains("Комманды")) {
			e.setCancelled(true);
			final Arena ar = Arena.getPlArena(p.getName());
			switch (e.getCurrentItem().getType()) {
			case LIGHT_GRAY_STAINED_GLASS_PANE:
				break;
			case REDSTONE_TORCH:
				p.closeInventory();
				return;
			case LEATHER_HELMET:
				if (ar.getPlTeam(p.getName()) == null) {
					if (Team.canJoin(ar.getTms(), ar.getNameTeam(e.getCurrentItem().getItemMeta().getDisplayName()))) {
						ar.addToTm(p, ar.getNameTeam(e.getCurrentItem().getItemMeta().getDisplayName()));
						p.closeInventory();
					} else {
						p.sendMessage(Main.prf() + "§cВ етой комманде слишком много игроков!");
					}
				} else if (e.getCurrentItem().getItemMeta().hasEnchants()) {
					p.sendMessage(Main.prf() + "§cВы уже находитесь в этой комманде!");
				} else if (Team.canJoin(ar.getTms(), ar.getNameTeam(e.getCurrentItem().getItemMeta().getDisplayName()))) {
					ar.remFromTm(p.getName(), ar.getPlTeam(p.getName()));
					ar.addToTm(p, ar.getNameTeam(e.getCurrentItem().getItemMeta().getDisplayName()));
					p.closeInventory();
				}
				break;
			default:
				break;
			}
		} else if (e.getView().getTitle().contains("Магазина")) {
			e.setCancelled(true);
			final Arena ar = Arena.getPlArena(p.getName());
			final Inventory inv;
			switch (e.getCurrentItem().getType()) {
			case BARRIER:
				p.closeInventory();
				return;
			case CAKE:
				inv = Bukkit.createInventory(p, 27, "§6Магазин Еды");
				inv.setContents(Inventories.fillShpFdInv(ChatColor.getByChar(ar.getPlTeam(p.getName()).getName().charAt(1))));
				p.closeInventory();
				p.openInventory(inv);
				break;
			case WOODEN_SWORD:
				inv = Bukkit.createInventory(p, 27, "§6Магазин Оружия");
				inv.setContents(Inventories.fillShpWpnInv(p.getInventory(), ChatColor.getByChar(ar.getPlTeam(p.getName()).getName().charAt(1))));
				p.closeInventory();
				p.openInventory(inv);
				break;
			case TURTLE_HELMET:
				inv = Bukkit.createInventory(p, 54, "§6Магазин Брони");
				inv.setContents(Inventories.fillShpArmrInv(p.getInventory(), ChatColor.getByChar(ar.getPlTeam(p.getName()).getName().charAt(1))));
				p.closeInventory();
				p.openInventory(inv);
				break;
			case ENDER_EYE:
				inv = Bukkit.createInventory(p, 27, "§6Магазин Разного");
				inv.setContents(Inventories.fillShpXtrInv());
				p.closeInventory();
				p.openInventory(inv);
				break;
			default:
				if (e.getCurrentItem().getType() == Material.getMaterial("WOODEN" + ar.getTlSfx())) {
					inv = Bukkit.createInventory(p, 27, "§6Магазин Инструментов");
					inv.setContents(Inventories.fillShpTlInv(p.getInventory(), ar.getTlSfx(), ChatColor.getByChar(ar.getPlTeam(p.getName()).getName().charAt(1))));
					p.closeInventory();
					p.openInventory(inv);
				}
				break;
			}
		} else if (e.getView().getTitle().contains("Еды")) {
			e.setCancelled(true);
			final Arena ar = Arena.getPlArena(p.getName());
			switch (e.getCurrentItem().getType()) {
			case REDSTONE_TORCH:
				p.closeInventory();
				final Inventory inv = Bukkit.createInventory(p, 27, "§6Выбор Магазина");
				inv.setContents(Inventories.fillShpInv(Arena.getPlArena(p.getName())));
				p.openInventory(inv);
				return;
			case APPLE:
			case PUMPKIN_PIE:
			case COOKED_PORKCHOP:
			case GOLDEN_APPLE:
			case ENCHANTED_GOLDEN_APPLE:
				if (canResBuy(p.getInventory(), e.getCurrentItem(), ar.recs)) {
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_TRADE, 1, 1);
					remIts(p.getInventory(), e.getCurrentItem(), ar.recs);
					p.getInventory().addItem(strpLr(e.getCurrentItem().clone()));
				} else {
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
					p.sendMessage(Main.prf() + "§cУ вас не хватает ресурсов для покупки этого!");
				}
				break;
			default:
				break;
			}
		} else if (e.getView().getTitle().contains("Оружия")) {
			e.setCancelled(true);
			final PlayerInventory pi = p.getInventory();
			final Arena ar = Arena.getPlArena(p.getName());
			switch (e.getCurrentItem().getType()) {
			case LIGHT_GRAY_STAINED_GLASS_PANE:
			case WOODEN_SWORD:
				break;
			case ARROW:
			case FIREWORK_ROCKET:
				if (canResBuy(pi, e.getCurrentItem(), ar.recs)) {
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_TRADE, 1, 1);
					remIts(pi, e.getCurrentItem(), ar.recs);
					pi.addItem(strpLr(e.getCurrentItem().clone()));
				} else {
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
					p.sendMessage(Main.prf() + "§cУ вас не хватает ресурсов для покупки этого!");
				}
				break;
			case CROSSBOW:
				if (canResBuy(pi, e.getCurrentItem(), ar.recs)) {
					if (pi.contains(Material.CROSSBOW)) {
						p.sendMessage(Main.prf() + "§cУ вас уже есть это оружие!");
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
					} else {
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
						remIts(pi, e.getCurrentItem(), ar.recs);
						pi.addItem(strpLr(e.getCurrentItem().clone()));
					}
				} else {
					p.sendMessage(Main.prf() + "§cУ вас не хватает ресурсов для покупки этого!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
				}
				break;
			case REDSTONE_TORCH:
				p.closeInventory();
				final Inventory inv = Bukkit.createInventory(p, 27, "§6Выбор Магазина");
				inv.setContents(Inventories.fillShpInv(Arena.getPlArena(p.getName())));
				p.openInventory(inv);
				return;
			default:
				if (canResBuy(pi, e.getCurrentItem(), ar.recs)) {
					switch (e.getCurrentItem().getType().toString().charAt(0)) {
					case 'G':
						if (chkIfHas(pi, Material.GOLDEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD)) {
							p.sendMessage(Main.prf() + "§cУ вас уже есть это оружие, или что то получше!");
							p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
							return;
						}
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
						remIts(pi, e.getCurrentItem(), ar.recs);
						pi.addItem(strpLr(e.getCurrentItem().clone()));
						break;
					case 'S':
						if (chkIfHas(pi, Material.STONE_SWORD, Material.IRON_SWORD)) {
							p.sendMessage(Main.prf() + "§cУ вас уже есть это оружие, или что то получше!");
							p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
							return;
						}
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
						remIt(pi, false, Material.GOLDEN_SWORD);
						remIts(pi, e.getCurrentItem(), ar.recs);
						pi.addItem(strpLr(e.getCurrentItem().clone()));
						break;
					case 'I':
						if (chkIfHas(pi, Material.IRON_SWORD)) {
							p.sendMessage(Main.prf() + "§cУ вас уже есть это оружие, или что то получше!");
							p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
							return;
						}
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
						remIt(pi, false, Material.GOLDEN_SWORD, Material.STONE_SWORD);
						remIts(pi, e.getCurrentItem(), ar.recs);
						pi.addItem(strpLr(e.getCurrentItem().clone()));
						break;
					}
				} else {
					p.sendMessage(Main.prf() + "§cУ вас не хватает ресурсов для покупки этого!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
				}
				break;
			}
		} else if (e.getView().getTitle().contains("Инстр")) {
			e.setCancelled(true);
			
			final Arena ar = Arena.getPlArena(p.getName());
			switch (e.getCurrentItem().getType()) {
			case LIGHT_GRAY_STAINED_GLASS_PANE:
				break;
			case PHANTOM_SPAWN_EGG:
				if (canResBuy(p.getInventory(), e.getCurrentItem(), ar.recs)) {
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
					remIts(p.getInventory(), e.getCurrentItem(), ar.recs);
					p.getInventory().addItem(strpLr(e.getCurrentItem().clone()));
				} else {
					p.sendMessage(Main.prf() + "§cУ вас не хватает ресурсов для покупки этого!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
				}
				break;
			case REDSTONE_TORCH:
				p.closeInventory();
				final Inventory inv = Bukkit.createInventory(p, 27, "§6Выбор Магазина");
				inv.setContents(Inventories.fillShpInv(Arena.getPlArena(p.getName())));
				p.openInventory(inv);
				return;
			default:
				final PlayerInventory pi = p.getInventory();
				if (canResBuy(pi, e.getCurrentItem(), ar.recs)) {
					switch (e.getCurrentItem().getType().toString().charAt(0)) {
					case 'I':
						if (chkIfHas(pi, Material.getMaterial("IRON" + ar.getTlSfx()), Material.getMaterial("DIAMOND" + ar.getTlSfx()), Material.getMaterial("NETHERITE" + ar.getTlSfx()))) {
							p.sendMessage(Main.prf() + "§cУ вас уже есть этот инструмент, или что-то получше!");
							p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
							return;
						}
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
						remIt(pi, false, Material.getMaterial("WOODEN" + ar.getTlSfx()));
						remIts(pi, e.getCurrentItem(), ar.recs);
						pi.addItem(strpLr(e.getCurrentItem().clone()));
						break;
					case 'D':
						if (chkIfHas(pi, Material.getMaterial("DIAMOND" + ar.getTlSfx()), Material.getMaterial("NETHERITE" + ar.getTlSfx()))) {
							p.sendMessage(Main.prf() + "§cУ вас уже есть этот инструмент, или что-то получше!");
							p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
							return;
						}
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
						remIt(pi, false, Material.getMaterial("WOODEN" + ar.getTlSfx()), Material.getMaterial("IRON" + ar.getTlSfx()));
						remIts(pi, e.getCurrentItem(), ar.recs);
						pi.addItem(strpLr(e.getCurrentItem().clone()));
						break;
					case 'N':
						if (chkIfHas(pi, Material.getMaterial("NETHERITE" + ar.getTlSfx()))) {
							p.sendMessage(Main.prf() + "§cУ вас уже есть этот инструмент, или что-то получше!");
							p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
							return;
						}
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
						remIt(pi, false, Material.getMaterial("WOODEN" + ar.getTlSfx()), Material.getMaterial("IRON" + ar.getTlSfx()), Material.getMaterial("DIAMOND" + ar.getTlSfx()));
						remIts(pi, e.getCurrentItem(), ar.recs);
						pi.addItem(strpLr(e.getCurrentItem().clone()));
						break;
					}
				} else {
					p.sendMessage(Main.prf() + "§cУ вас не хватает ресурсов для покупки этого!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
				}
				break;
			}
		} else if (e.getView().getTitle().contains("Брони")) {
			e.setCancelled(true);
			final Arena ar = Arena.getPlArena(p.getName());
			switch (e.getCurrentItem().getType()) {
			case TURTLE_HELMET:
			case LIGHT_GRAY_STAINED_GLASS_PANE:
				break;
			case REDSTONE_TORCH:
				p.closeInventory();
				final Inventory inv = Bukkit.createInventory(p, 27, "§6Выбор Магазина");
				inv.setContents(Inventories.fillShpInv(Arena.getPlArena(p.getName())));
				p.openInventory(inv);
				return;
			default:
				final PlayerInventory pi = p.getInventory();
				if (canResBuy(pi, e.getCurrentItem(), ar.recs)) {
					final String sfx = e.getCurrentItem().getType().toString().substring(e.getCurrentItem().getType().toString().indexOf('_'));
					switch (e.getCurrentItem().getType().toString().charAt(0)) {
					case 'L':
						if (chkIfHas(pi, Material.getMaterial("LEATHER" + sfx), Material.getMaterial("CHAINMAIL" + sfx), Material.getMaterial("IRON" + sfx), Material.getMaterial("DIAMOND" + sfx), Material.getMaterial("NETHERITE" + sfx))) {
							p.sendMessage(Main.prf() + "§cУ вас уже есть эта бронь, или что-то получше!");
							p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
							return;
						}
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
						remIts(pi, e.getCurrentItem(), ar.recs);
						pi.addItem(strpLr(e.getCurrentItem().clone()));
						break;
					case 'C':
						if (chkIfHas(pi, Material.getMaterial("CHAINMAIL" + sfx), Material.getMaterial("IRON" + sfx), Material.getMaterial("DIAMOND" + sfx), Material.getMaterial("NETHERITE" + sfx))) {
							p.sendMessage(Main.prf() + "§cУ вас уже есть эта бронь, или что-то получше!");
							p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
							return;
						}
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
						remIt(pi, true, Material.getMaterial("LEATHER" + sfx));
						remIts(pi, e.getCurrentItem(), ar.recs);
						pi.addItem(strpLr(e.getCurrentItem().clone()));
						break;
					case 'I':
						if (chkIfHas(pi, Material.getMaterial("IRON" + sfx), Material.getMaterial("DIAMOND" + sfx), Material.getMaterial("NETHERITE" + sfx))) {
							p.sendMessage(Main.prf() + "§cУ вас уже есть эта бронь, или что-то получше!");
							p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
							return;
						}
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
						remIt(pi, true, Material.getMaterial("LEATHER" + sfx), Material.getMaterial("CHAINMAIL" + sfx));
						remIts(pi, e.getCurrentItem(), ar.recs);
						pi.addItem(strpLr(e.getCurrentItem().clone()));
						break;
					case 'D':
						if (chkIfHas(pi, Material.getMaterial("DIAMOND" + sfx), Material.getMaterial("NETHERITE" + sfx))) {
							p.sendMessage(Main.prf() + "§cУ вас уже есть эта бронь, или что-то получше!");
							p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
							return;
						}
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
						remIt(pi, true, Material.getMaterial("LEATHER" + sfx), Material.getMaterial("CHAINMAIL" + sfx), Material.getMaterial("IRON" + sfx));
						remIts(pi, e.getCurrentItem(), ar.recs);
						pi.addItem(strpLr(e.getCurrentItem().clone()));
						break;
					case 'N':
						if (chkIfHas(pi, Material.getMaterial("NETHERITE" + sfx))) {
							p.sendMessage(Main.prf() + "§cУ вас уже есть эта бронь, или что-то получше!");
							p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
							return;
						}
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
						remIt(pi, true, Material.getMaterial("LEATHER" + sfx), Material.getMaterial("CHAINMAIL" + sfx), Material.getMaterial("IRON" + sfx), Material.getMaterial("DIAMOND" + sfx));
						remIts(pi, e.getCurrentItem(), ar.recs);
						pi.addItem(strpLr(e.getCurrentItem().clone()));
						break;
					}
				} else {
					p.sendMessage(Main.prf() + "§cУ вас не хватает ресурсов для покупки этого!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
				}
				break;
			}
		} else if (e.getView().getTitle().contains("Разного")) {
			e.setCancelled(true);
			final Arena ar = Arena.getPlArena(p.getName());
			switch (e.getCurrentItem().getType()) {
			case LIGHT_GRAY_STAINED_GLASS_PANE:
			case ENDER_EYE:
				break;
			case TOTEM_OF_UNDYING:
				if (p.getMetadata("cns").get(0).asShort() >= Short.parseShort(e.getCurrentItem().getItemMeta().getLore().get(1).substring(2, e.getCurrentItem().getItemMeta().getLore().get(1).indexOf(' ') - 2))) {
					p.playSound(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
					EntMeta.chngMoney(p, (short) -Short.parseShort(e.getCurrentItem().getItemMeta().getLore().get(1).substring(2, e.getCurrentItem().getItemMeta().getLore().get(1).indexOf(' ') - 2)), true);
					ar.getPlTeam(p.getName()).rsps++;
					ar.updTmsSb();
					Main.data.chngNum(p.getName(), "rsps", 1);
					for (final String s : Arena.getPlArena(p.getName()).getPls()) {
						Bukkit.getPlayer(s).sendMessage(Main.prf() + ar.getPlTeam(p.getName()).getName() + "§7 комманда приобрела себе §2+1 §7возрождение!");
					}
				} else {
					p.sendMessage(Main.prf() + "§cУ вас не хватает монет для покупки этого!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
				}
				break;
			case SPLASH_POTION:
				if (p.getMetadata("cns").get(0).asShort() >= Short.parseShort(e.getCurrentItem().getItemMeta().getLore().get(1).substring(2, e.getCurrentItem().getItemMeta().getLore().get(1).indexOf(' ') - 2))) {
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1, 1);
					EntMeta.chngMoney(p, (short) -Short.parseShort(e.getCurrentItem().getItemMeta().getLore().get(1).substring(2, e.getCurrentItem().getItemMeta().getLore().get(1).indexOf(' ') - 2)), true);
					p.getInventory().addItem(strpLr(e.getCurrentItem().clone(), 2));
				} else {
					p.sendMessage(Main.prf() + "§cУ вас не хватает монет для покупки этого!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
				}
				break;
			case ENCHANTED_BOOK:
				//есть что то в курсоре?
				final ItemStack cr = e.getCursor();
				if (Main.notItmNull(cr)) {
					final String m = cr.getType().toString();
					final Enchantment en;
					if (m.contains("_SWORD")) {
						en = Enchantment.DAMAGE_ALL;
					} else if (m.contains(ar.getTlSfx())) {
						en = Enchantment.DIG_SPEED;
					} else if (m.contains("HELM") || m.contains("CHEST") || m.contains("LEGG") || m.contains("BOOT")) {
						en = Enchantment.PROTECTION_ENVIRONMENTAL;
					} else if (m.contains("BOW")) {
						en = Enchantment.QUICK_CHARGE;
					} else {
						p.sendMessage(Main.prf() + "§cВы не можете зачаровать этот предмет!");
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
						return;
					}
					if (!cr.getItemMeta().hasEnchant(en) || cr.getItemMeta().getEnchantLevel(en) < Main.mxEnchLvl) {
						final String lr = e.getCurrentItem().getItemMeta().getLore().get(1);
						final int i = lr.indexOf(' ');
						if (p.getMetadata("cns").get(0).asShort() >= Integer.parseInt(lr.substring(2, i)) + (Integer.parseInt(lr.substring(i + 4, lr.indexOf(' ', i + 4))) * cr.getItemMeta().getEnchantLevel(en))) {
							p.setItemOnCursor(addEnch(cr, en));
							p.playSound(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
							EntMeta.chngMoney(p, (short) -(Integer.parseInt(lr.substring(2, i)) + (Integer.parseInt(lr.substring(i + 4, lr.indexOf(' ', i + 4))) * cr.getItemMeta().getEnchantLevel(en))), true);
						} else {
							p.sendMessage(Main.prf() + "§cУ вас не хватает монет для покупки этого!");
							p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
							return;
						}
					} else {
						p.sendMessage(Main.prf() + "§cВы уже максимально зачаровали этот предмет!");
						p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
						return;
					}
				} else {
					p.sendMessage(Main.prf() + "§cНажмите на книгу предметом, который хотите зачаровать!");
					p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
					return;
				}
				
				break;
			case REDSTONE_TORCH:
				p.closeInventory();
				final Inventory inv = Bukkit.createInventory(p, 27, "§6Выбор Магазина");
				inv.setContents(Inventories.fillShpInv(Arena.getPlArena(p.getName())));
				p.openInventory(inv);
				return;
			default:
				break;
			}
		}
	}
	public static void remIt(final PlayerInventory inv, final boolean armr, final Material... mts) {
		for (final Material m : mts) {
			inv.remove(m);
			if (inv.getItemInOffHand() == null || inv.getItemInOffHand().getType() == m) {
				inv.setItemInOffHand(null);
			}
			if (armr) {
				inv.setArmorContents(remFromArmr(inv.getArmorContents(), m));
			}
		}
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
	public static boolean chkIfHas(final PlayerInventory inv, Material... mts) {
		boolean b = false;
		for (final Material m : mts) {
			b = inv.contains(m) ? true : b;
			b = inv.getItemInOffHand() != null && inv.getItemInOffHand().getType() == m ? true : b;
			for (final ItemStack i : inv.getArmorContents()) {
				b = i != null && i.getType() == m ? true : b;
			}
		}
		return b;
	}
	//убираем лор
	public static ItemStack strpLr(final ItemStack it) {
		final ItemMeta mt = it.getItemMeta();
		mt.setLore(null);
		it.setItemMeta(mt);
		return it;
	}
	//убираем лор
	public static ItemStack strpLr(final ItemStack it, int rws) {
		final ItemMeta mt = it.getItemMeta();
		final List<String> lr =  mt.getLore();
		for (rws-- ; rws >= 0; rws--) {
			lr.remove(rws);
		}
		mt.setLore(lr);
		it.setItemMeta(mt);
		return it;
	}
	//может ли игрок купить это?
	public static boolean canResBuy(final PlayerInventory inv, final ItemStack it, final Material[] mts) {
		for (final String s : it.getItemMeta().getLore()) {
			if (s.contains("Х") && !inv.containsAtLeast(new ItemStack(mts[0]), Integer.parseInt(s.substring(2, s.indexOf('Х') - 3)))) {
				return false;
			} else if (s.contains("Н") && !inv.containsAtLeast(new ItemStack(mts[1]), Integer.parseInt(s.substring(2, s.indexOf('Н') - 3)))) {
				return false;
			} else if (s.contains("П") && !inv.containsAtLeast(new ItemStack(mts[2]), Integer.parseInt(s.substring(2, s.indexOf('П') - 3)))) {
				return false;
			}
		}
		return true;
	}
	//убираем ресурсы из инвентаря
	public static void remIts(final PlayerInventory inv, final ItemStack it, final Material[] mts) {
		for (final String s : it.getItemMeta().getLore()) {
			if (s.contains("Х")) {
				byte amt = Byte.parseByte(s.substring(2, s.indexOf('Х') - 3));
				while (amt != 0) {
					final ItemStack i = inv.getItem(inv.first(mts[0]));
					if (i.getAmount() - amt < 1) {
						inv.remove(i);
						amt -= i.getAmount();
					} else {
						i.setAmount(i.getAmount() - amt);
						inv.setItem(inv.first(mts[0]), i);
						break;
					}
				}
			} else if (s.contains("Н")) {
				byte amt = Byte.parseByte(s.substring(2, s.indexOf('Н') - 3));
				while (amt != 0) {
					final ItemStack i = inv.getItem(inv.first(mts[1]));
					if (i.getAmount() - amt < 1) {
						inv.remove(i);
						amt -= i.getAmount();
					} else {
						i.setAmount(i.getAmount() - amt);
						inv.setItem(inv.first(mts[1]), i);
						break;
					}
				}
			} else if (s.contains("П")) {
				byte amt = Byte.parseByte(s.substring(2, s.indexOf('П') - 3));
				while (amt != 0) {
					final ItemStack i = inv.getItem(inv.first(mts[2]));
					if (i.getAmount() - amt < 1) {
						inv.remove(i);
						amt -= i.getAmount();
					} else {
						i.setAmount(i.getAmount() - amt);
						inv.setItem(inv.first(mts[2]), i);
						break;
					}
				}
			}
		}
	}
}
