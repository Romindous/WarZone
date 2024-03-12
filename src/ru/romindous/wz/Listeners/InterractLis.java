package ru.romindous.wz.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.TCUtils;
import ru.romindous.wz.Game.Arena;
import ru.romindous.wz.Game.GameState;
import ru.romindous.wz.Main;
import ru.romindous.wz.Utils.Inventories;

public class InterractLis implements Listener{
	
	@EventHandler
	public void onSwap(final PlayerSwapHandItemsEvent e) {
		e.setCancelled(Arena.getPlArena(e.getPlayer()) == null);
	}
	
	@EventHandler
	public void onDrop(final PlayerDropItemEvent e) {
		//если играет
		final Arena ar = Arena.getPlArena(e.getPlayer());
		if (ar == null) {
			e.setCancelled(!ApiOstrov.isLocalBuilder(e.getPlayer()));
		} else {
			final Material m = e.getItemDrop().getItemStack().getType();
			for (final Material mt : ar.recs) {
				if (m == mt) return;
			}
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onOpen(final InventoryOpenEvent e) {
		//если играет
		final Arena ar = Arena.getPlArena(e.getPlayer());
		if (ar != null) return;
		final String inm = TCUtils.toString(e.getView().title());
		if (inm.contains("Карты") || inm.contains("Комманды")) return;
		e.setCancelled(!ApiOstrov.isLocalBuilder(e.getPlayer()));
	}
	
	@EventHandler
	public void onPickup(final EntityPickupItemEvent e) {
		if (e.getEntityType() != EntityType.PLAYER) return;
		final Arena ar = Arena.getPlArena((HumanEntity) e.getEntity());
		//если играет
		if (ar == null) e.setCancelled(!ApiOstrov.isLocalBuilder(e.getEntity()));
	}
	
	@EventHandler
	public void onInter(final PlayerInteractEvent e) {
		final Player p = e.getPlayer();
		switch (e.getAction()) {
		case PHYSICAL:
			e.setCancelled(e.getClickedBlock().getType() == Material.FARMLAND);
			break;
		case RIGHT_CLICK_AIR:
		case RIGHT_CLICK_BLOCK:
			e.setUseInteractedBlock(ApiOstrov.isLocalBuilder(p)
				? Event.Result.ALLOW : Event.Result.DENY);
		case LEFT_CLICK_AIR:
		case LEFT_CLICK_BLOCK:
			final Arena ar = Arena.getPlArena(p);
			final ItemStack it = e.getItem();
			if (ItemUtils.isBlank(it, true)) return;
			final String in = it.getItemMeta().hasDisplayName()
				? TCUtils.toString(it.getItemMeta().displayName()) : "";
			if (ar == null) {
				//игрок в лобби
				if (in.contains("Карты")) {
					e.setCancelled(true);
					p.playSound(p.getLocation(), Sound.BLOCK_BEEHIVE_EXIT, 80, 1);
					final Inventory inv = Bukkit.createInventory(p,
						9 * (Main.nonactivearenas.size() / 9 + 3),
						TCUtils.format(TCUtils.P + "Меню выбора Карты"));
					inv.setContents(Inventories.fillArInv(9 * (Main.nonactivearenas.size() / 9 + 3)));
					p.openInventory(inv);
				} else if (in.contains("Выход")) {
					e.setCancelled(true);
					ApiOstrov.sendToServer(p, "lobby1", "");
				}
			} else {
				if (ar.getState() != GameState.RUNNING) {
					//игрок выбрал карту
					if (in.contains("Комманды")) {
						e.setCancelled(true);
						ar.teamInv.open(p);
					} else if (in.contains("Выход")) {
						e.setCancelled(true);
						p.performCommand("wz leave");
					}
				}
			}
			break;
		}
		//Bukkit.getConsoleSender().sendMessage("c- " + e.isCancelled());
	}

	@EventHandler
	public static void onBreak(final BlockBreakEvent e) {
        //Bukkit.getConsoleSender().sendMessage("b- " + e.getBlock().getType().toString());
		final Player p = e.getPlayer();
		final Arena ar = Arena.getPlArena(p);
		e.setCancelled(!ApiOstrov.isLocalBuilder(p));
		if (ar == null) return;
		//игрок в игре
		final Material[] mts = ar.mnbls;
		for (int i = mts.length -1; i >= 0; i--) {
			final Material mt = mts[i];
			if (e.getBlock().getType() == mt) {
				e.setExpToDrop(0);
				e.setDropItems(false);
				final ItemStack it = e.getBlock().getDrops().iterator().next();
				final ItemMeta im = it.getItemMeta();
				im.displayName(switch (i) {
					case 1 -> TCUtils.format("§3Нормальный Материал");
					case 2 -> TCUtils.format("§2Прочный Материал");
					case 0 -> TCUtils.format("§aХрупкий Материал");
                    default -> TCUtils.format("§e??? Материал");
                });
				it.setItemMeta(im);
				p.getInventory().addItem(it);
				e.getBlock().setType(Material.AIR, false);
				p.playSound(e.getBlock().getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 0.8f);
				Ostrov.sync(() -> e.getBlock().setType(mt, false), Main.blkkd * 20);
				return;
			}
		}

	}
	
	@EventHandler
	public void onTrade(final PlayerInteractAtEntityEvent e) {
		final Player p = e.getPlayer();
        if (e.getRightClicked().getType() == Arena.SHOP_TYPE) {
			e.setCancelled(true);
			final Arena ar = Arena.getPlArena(p);
			if (ar == null) {
				p.sendMessage(Main.PRFX + "§cНужно быть в игре чтобы изпользовать магазин!");
				return;
			}

			final Inventory inv = Bukkit.createInventory(p, 27, TCUtils.format(TCUtils.P + "Выбор Магазина"));
			inv.setContents(Inventories.fillShpInv(Arena.getPlArena(p)));
			p.openInventory(inv);
		}
	}
}
