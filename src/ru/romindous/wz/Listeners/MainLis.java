package ru.romindous.wz.Listeners;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Stat;
import ru.komiss77.events.ChatPrepareEvent;
import ru.komiss77.events.LocalDataLoadEvent;
import ru.komiss77.listener.ChatLst;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Perm;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.wz.Game.Arena;
import ru.romindous.wz.Game.GameState;
import ru.romindous.wz.Game.PlWarrior;
import ru.romindous.wz.Main;

import java.util.Objects;
import java.util.Random;

public class MainLis implements Listener {
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(final LocalDataLoadEvent e) {
		final Player p = e.getPlayer();
		Main.lobbyPlayer(p, (PlWarrior) e.getOplayer());
        p.sendPlayerListHeader(TCUtil.form(Main.FULL
			+ "\n" + switch (Main.srnd.nextInt(4)) {
			case 0 -> "Добро пожаловать!";
			case 1 -> "Приятной игры!";
			case 2 -> "Желаем удачи!";
			case 3 -> "Развлекайтесь!";
			default -> "";
		}));

		final String wantArena = e.getOplayer().getDataString(Data.WANT_ARENA_JOIN);
		if (!wantArena.isEmpty()) {
			final Arena ta = Main.activearenas.get(wantArena);
			final Arena ar = ta == null ? Main.createArena(wantArena) : ta;
			if (ar == null) return;
			switch (ar.getState()) {
				case WAITING -> ar.addPl(p);
				case RUNNING -> ar.addSpct(p, PM.getOplayer(p, PlWarrior.class));
				case END -> {}
			}
		}
	}

	@EventHandler
	public void onOpen(final InventoryOpenEvent e) {
		final Arena ar = Arena.getPlArena(e.getPlayer());
		if (ar != null && ar.getState() == GameState.RUNNING) {
			e.setCancelled(!e.getPlayer().hasPermission("ostrov.builder"));
			e.getPlayer().closeInventory();
		}
	}
	
	@EventHandler
	public void onLeave(final PlayerQuitEvent e) {
		e.quitMessage(null);
		final Player p = e.getPlayer();
		final Arena ar = Arena.getPlArena(p);
		if (ar != null) {
			ar.remPl(p);
			ar.remSpct(p);
		}
	}
	
	@EventHandler
	public void onFood(final FoodLevelChangeEvent e) {
		if (Arena.getPlArena(e.getEntity()) == null) {
			e.setFoodLevel(19);
		}
	}
	
	@EventHandler
	public void onDeath(final EntityDeathEvent e) {
		final LivingEntity ent = e.getEntity();
		final LivingEntity dmgr = EntityUtil.lastDamager(ent, true);
		//если игрок убивает моба
		if (dmgr instanceof final Player dp) {
			final PlWarrior dpw = PM.getOplayer(dp, PlWarrior.class);
//			dmgr.playSound(e.getEntity().getLocation(), Sound.BLOCK_ANVIL_LAND, 0.3f, 2);
			//монеты дамагеру
			if (ent.hasMetadata(Arena.LVL)) {
				final Arena ar = dpw.arena();
				if (ar != null && ar.getState() == GameState.RUNNING) {
					ent.getWorld().playSound(ent.getLocation(), Sound.BLOCK_CHAIN_PLACE, 1f, 0.8f);
					ent.getWorld().spawnParticle(Particle.LANDING_HONEY, ent.getLocation(), 40, 0.2d, 0.8d, 0.2d);
					dpw.coins((ent.getMetadata(Arena.LVL).get(0).asByte() + 1) << PlWarrior.MOB_DEL);
					dpw.addStat(Stat.WZ_mbs, 1);
					dpw.mobsI();
				}
			}
		}
	}
	
	@EventHandler
	public void onResp(final PlayerRespawnEvent e) {
		if (Main.lobby != null) {
			final PlWarrior pw = PM.getOplayer(e.getPlayer(), PlWarrior.class);
			if (pw.team() == null) {
				e.setRespawnLocation(Main.lobby.getCenterLoc());
			} else {
				e.setRespawnLocation(pw.team().spwn.getCenterLoc());
			}
        }
	}
	
	@EventHandler
	public void onSpawn(final EntitySpawnEvent e) {
		if (e.getEntityType() == EntityType.PHANTOM) {
			((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,
				1000000, 1, true, false, false));
			((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
				1000000, 1, true, false, false));
		}
	}
	
	@EventHandler(ignoreCancelled = false, priority = EventPriority.LOW)
	public void onDamage(final EntityDamageEvent e) {
		final LivingEntity dmgr = EntityUtil.getDamager(e, true);
		if (dmgr == null) {
			if (e.getEntity() instanceof final Player p) {
				final PlWarrior pw = PM.getOplayer(p, PlWarrior.class);
				final Arena ar = pw.arena();
				if (ar == null || ar.getState() != GameState.RUNNING) {
					e.setCancelled(true);
					e.setDamage(0d);
					return;
				}

				if (p.getHealth() - e.getFinalDamage() > 0) return;
				e.setCancelled(true);
				e.setDamage(0d);
				ar.killWar(p, pw, null);
			}
		} else {
			final PlWarrior dpw;
			if (dmgr instanceof HumanEntity) {
				dpw = PM.getOplayer((HumanEntity) dmgr, PlWarrior.class);
				final Arena ar = dpw.arena();
				if ((ar == null || ar.getState() != GameState.RUNNING)
					&& !ApiOstrov.isLocalBuilder(dmgr, false)) {
					e.setCancelled(true);
					e.setDamage(0d);
					return;
				}
			} else {
				dpw = null;
			}

			if (e.getEntity() instanceof final Player p) {
				final PlWarrior pw = PM.getOplayer(p, PlWarrior.class);
				final Arena ar = pw.arena();
				if (ar == null || ar.getState() != GameState.RUNNING) {
					e.setCancelled(true);
					e.setDamage(0d);
					return;
				}

				if (dpw != null && Objects.equals(dpw.team(), pw.team())) {
					e.setCancelled(true);
					e.setDamage(0d);
					return;
				}

				if (p.getHealth() - e.getFinalDamage() > 0) return;
				e.setCancelled(true);
				e.setDamage(0d);
				if (dpw == null) {
					final String enm = TCUtil.deform(dmgr.customName());
					ar.killWar(p, pw, enm.isEmpty() ? TCUtil.P + StringUtil.nrmlzStr(dmgr.getType().toString()) : enm);
				} else {
					ar.killWar(p, pw, dpw.team() == null ? TCUtil.P + dpw.nik : dpw.team().color() + dpw.nik);
					p.getWorld().playSound(p.getLocation(), Sound.BLOCK_CHAIN_PLACE, 1f, 0.8f);
					p.getWorld().spawnParticle(Particle.LANDING_HONEY, p.getLocation(), 40, 0.2d, 0.8d, 0.2d);
					dpw.addStat(Stat.WZ_klls, 1);
					dpw.coins(PlWarrior.KILL_RWD);
					dpw.killsI();
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChat(final ChatPrepareEvent e) {
		final Player p = e.getPlayer();
		final PlWarrior pw = PM.getOplayer(p, PlWarrior.class);
		final Arena ar = pw.arena();
		final String msg = Perm.canColorChat(e.getOplayer())
			? e.getMessage().replace('&', '§') : e.getMessage();
		e.showLocal(true);
		e.showSelf(false);
		if (ar == null) {
			final Component modMsg = TCUtil.form(Main.bfr('{', TCUtil.P + StringUtil.toSigFigs(
				(double) pw.getStat(Stat.WZ_klls) / (double) pw.getStat(Stat.WZ_dths), (byte) 2), '}')
				+ ChatLst.NIK_COLOR + p.getName() + Main.afr('[', TCUtil.A + "ЛОББИ", ']') + " <gray><i>≫</i> " + TCUtil.N + msg);
			for (final Audience au : e.viewers()) {
				au.sendMessage(modMsg);
			}
			p.sendMessage(modMsg);
		} else {
			e.sendProxy(false);
			final Component modMsg;
			switch (ar.getState()) {
			case WAITING:
				modMsg = TCUtil.form(ChatLst.NIK_COLOR + p.getName()
					+ Main.afr('[', TCUtil.P + ar.getName(), ']') + " <gray><i>≫</i> " + TCUtil.N + msg);
				for (final Audience au : e.viewers()) {
					au.sendMessage(modMsg);
				}
				p.sendMessage(modMsg);
				break;
			case RUNNING:
			case END:
				if (msg.length() > 1 && msg.charAt(0) == '!') {
					modMsg = TCUtil.form(TCUtil.N + "[Всем] "
						+ (pw.team() == null ? TCUtil.N : pw.team().color()) +
						p.getName() + " <gray><i>≫</i> " + TCUtil.N + msg.substring(1));
					for (final PlWarrior ors : ar.getPls().values()) {
						final Player pl = ors.getPlayer();
						pl.sendMessage(modMsg);
						pl.playSound(pl.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1f, 1.4f);
					}
					for (final PlWarrior ors : ar.getSpcs().values()) {
						final Player pl = ors.getPlayer();
						pl.sendMessage(modMsg);
						pl.playSound(pl.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1f, 1.4f);
					}
				} else {
					modMsg = TCUtil.form((pw.team() == null ? TCUtil.N : pw.team().color()) +
						p.getName() + " <gray><i>≫</i> " + TCUtil.N + msg);
					for (final PlWarrior ors : ar.getPls().values()) {
						if (Objects.equals(pw.team(), ors.team())) {
							final Player pl = ors.getPlayer();
							pl.sendMessage(modMsg);
							pl.playSound(pl.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1f, 1.2f);
						}
					}
				}
				break;
			}
		}
		e.viewers().clear();
	}

	public static String entDie() {
        return switch (new Random().nextInt(4)) {
            case 0 -> TCUtil.N + " помер от рук ";
            case 1 -> TCUtil.N + " был раздроблен ";
            case 2 -> TCUtil.N + " не смог устоять перед красотой ";
            case 3 -> TCUtil.N + " самоликвидировался при виде ";
            default -> TCUtil.N;
        };
	}
	
	public static String snglDie() {
        return switch (new Random().nextInt(4)) {
            case 0 -> TCUtil.N + " скончался";
            case 1 -> TCUtil.N + " откинул коньки";
            case 2 -> TCUtil.N + " сдохся";
            case 3 -> TCUtil.N + " умер";
            default -> TCUtil.N;
        };
	}
}
