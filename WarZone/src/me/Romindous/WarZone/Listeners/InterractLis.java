package me.Romindous.WarZone.Listeners;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.Romindous.WarZone.Main;
import me.Romindous.WarZone.Game.Arena;
import me.Romindous.WarZone.Game.GameState;
import me.Romindous.WarZone.Utils.Inventories;
import net.kyori.adventure.text.Component;
import ru.komiss77.ApiOstrov;

public class InterractLis implements Listener{
	
	@EventHandler
	public void onSwap(final PlayerSwapHandItemsEvent e) {
		e.setCancelled(!e.getPlayer().hasMetadata("kls"));
	}
	
	@EventHandler
	public void onDrop(final PlayerDropItemEvent e) {
		//если играет
		if (e.getPlayer().hasMetadata("kls")) {
			e.setCancelled(!Arrays.asList(Arena.getPlArena(e.getPlayer().getName()).recs).contains(e.getItemDrop().getItemStack().getType()));
		} else {
			e.setCancelled(!e.getPlayer().hasPermission("ostrov.builder"));
		}
	}
	
	@EventHandler
	public void onOpen(final InventoryOpenEvent e) {
		//если играет
		if (e.getPlayer().hasMetadata("kls")) {
			return;
		} else {
			e.setCancelled(!(e.getView().getTitle().contains("Карты") || e.getView().getTitle().contains("Комманды")) && !e.getPlayer().hasPermission("ostrov.builder"));
		}
	}
	
	@EventHandler
	public void onPickup(final EntityPickupItemEvent e) {
		if (e.getEntityType() != EntityType.PLAYER) {
			return;
		}
		//если играет
		if (e.getEntity().hasMetadata("kls")) {
			return;
		} else {
			e.setCancelled(!e.getEntity().hasPermission("ostrov.builder"));
		}
	}
	
	@EventHandler
	public void onInter(final PlayerInteractEvent e) {
		switch (e.getAction()) {
		case PHYSICAL:
			e.setCancelled(e.getClickedBlock().getType() == Material.FARMLAND);
			break;
		case RIGHT_CLICK_AIR:
		case RIGHT_CLICK_BLOCK:
		case LEFT_CLICK_AIR:
		case LEFT_CLICK_BLOCK:
			final Player p = e.getPlayer();
			final ItemStack it = e.getItem();
			if (p.hasMetadata("cns")) {
				if (p.hasMetadata("kls") && Arena.getPlArena(p.getName()).getState() == GameState.RUNNING) {
					//игрок в игре
					if (it == null) {
						return;
					}
					e.setCancelled(e.getAction() == Action.RIGHT_CLICK_BLOCK && (it.getType().isBlock() || it.getType().toString().contains(Arena.getPlArena(p.getName()).getTlSfx())) && p.getGameMode() != GameMode.CREATIVE);
				} else {
					//игрок выбрал карту
					if (Main.notItmNull(it) && it.getItemMeta().getDisplayName().contains("Комманды")) {
						e.setCancelled(true);
						p.playSound(p.getLocation(), Sound.BLOCK_BEEHIVE_EXIT, 80, 1);
						final Inventory inv = Bukkit.createInventory(p, 27, "§2Выбор Комманды");
						inv.setContents(Inventories.fillTmInv(p.getName()));
						p.openInventory(inv);
					} else if (Main.notItmNull(it) && it.getItemMeta().getDisplayName().contains("Выход")) {
						e.setCancelled(true);
						p.performCommand("wz leave");
					}
				}
			} else {
				//игрок в лобби
				if (Main.notItmNull(it) && it.getItemMeta().getDisplayName().contains("Карты")) {
					e.setCancelled(true);
					p.playSound(p.getLocation(), Sound.BLOCK_BEEHIVE_EXIT, 80, 1);
					final Inventory inv = Bukkit.createInventory(p, 27 + (9 * (int) (((float) Main.nonactivearenas.size()) / 9.0f)), "§2Меню выбора Карты");
					inv.setContents(Inventories.fillArInv(27 + (9 * (int) (((float) Main.nonactivearenas.size()) / 9.0f))));
					p.openInventory(inv);
				} else if (Main.notItmNull(it) && it.getItemMeta().getDisplayName().contains("Выход")) {
					e.setCancelled(true);
					ApiOstrov.sendToServer(p, "lobby1", "");
				}
			}
			break;
		}
	}

	@EventHandler
	public static void onBreak(final BlockBreakEvent e) {
		//если это не был игрок
		if (e.getPlayer() == null) {
			return;
		}
		
		final Player p = e.getPlayer();
		if (p.hasMetadata("kls")) {
			//игрок в игре
			final Material[] mts = Arena.getPlArena(p.getName()).mnbls;
			for (int i = mts.length -1; i >= 0; i--) {
				final Material mt = mts[i];
				if (e.getBlock().getType() == mt) {
					e.setExpToDrop(0);
					e.setDropItems(false);
					final ItemStack it = e.getBlock().getDrops().iterator().next();
					final ItemMeta im = it.getItemMeta();
					switch (i) {
						case 1:
							im.displayName(Component.text("§3Нормальный Материал"));
							break;
						case 2:
							im.displayName(Component.text("§2Прочный Материал"));
							break;
						default:
						case 0:
							im.displayName(Component.text("§aХрупкий Материал"));
							break;
					}
					it.setItemMeta(im);
					p.getInventory().addItem(it);
					p.playSound(e.getBlock().getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1);
					Bukkit.getScheduler().runTaskLater(Main.plug, new Runnable() {
						@Override
						public void run() {
							e.getBlock().setType(mt);
						}
					}, Main.blkkd * 20);
					return;
				}
			}
			e.setCancelled(true);
		} else {
			//игрок выбрал карту
			e.setCancelled(!p.hasPermission("ostrov.builder") || p.getGameMode() != GameMode.CREATIVE);
		}
	}
	
	@EventHandler
	public void onTrade(final PlayerInteractAtEntityEvent e) {
		final Player p = e.getPlayer();
		if (e.getRightClicked() != null && e.getRightClicked().getName().equalsIgnoreCase("§6§lМагазин")) {
			e.setCancelled(true);
			if (p.hasMetadata("kls")) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plug, new Runnable() {
					@Override
					public void run() {
						final Inventory inv = Bukkit.createInventory(p, 27, "§6Выбор Магазина");
						inv.setContents(Inventories.fillShpInv(Arena.getPlArena(p.getName())));
						p.openInventory(inv);
					}
				}, 1);
			} else {
				p.sendMessage(Main.prf() + "§cВы должны быть в игре чтобы изпользовать магазин!");
			}
		}
	}
}
