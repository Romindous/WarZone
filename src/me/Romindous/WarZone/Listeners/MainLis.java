package me.Romindous.WarZone.Listeners;

import java.util.Iterator;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.Romindous.WarZone.Main;
import me.Romindous.WarZone.Game.Arena;
import me.Romindous.WarZone.Game.GameState;
import me.Romindous.WarZone.Utils.EntMeta;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.TextComponent;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Stat;
import ru.komiss77.events.BungeeDataRecieved;
import ru.komiss77.events.LocalDataLoadEvent;
import ru.komiss77.modules.player.PM;

public class MainLis implements Listener{
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBungee(final BungeeDataRecieved e) {
        final String wantArena = PM.getOplayer(e.getPlayer().getName()).getDataString(Data.WANT_ARENA_JOIN);
        if (!wantArena.isEmpty()) {
        	/*final Arena ar = Main.activearenas.get(wantArena);
        	if (ar == null) {
        		final Arena a = Main.createArena(wantArena);
        		if (a != null) {
            		a.addPl(e.getPlayer().getName());
        		}
        	} else if (ar.getState() == GameState.LOBBY_WAIT) {
            	ar.addPl(e.getPlayer().getName());
            }*/
        }
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(final LocalDataLoadEvent e) {
		final Player p = e.getPlayer();
		Main.lobbyPlayer(p);
		final String title;
		switch (new Random().nextInt(4)) {
		case 0:
			title = "Добро пожаловать!";
			break;
		case 1:
			title = "Приятной игры!";
			break;
		case 2:
			title = "Желаем удачи!";
			break;
		case 3:
			title = "Развлекайтесь!";
			break;
		default:
			title = "";
			break;
		}
		p.sendPlayerListHeaderAndFooter(Component.text("§7<§2Поле Брани§7>\n" + title), Component.text("§7Сейчас в игре: §2" + getPlaying() + "§7 человек!"));
	}

	@EventHandler
	public void onOpen(final InventoryOpenEvent e) {
		if (Arena.getPlArena(e.getPlayer().getName()) != null && Arena.getPlArena(e.getPlayer().getName()).getState() == GameState.RUNNING) {
			e.setCancelled(!e.getPlayer().hasPermission("ostrov.builder"));
			e.getPlayer().closeInventory();
		}
	}
	
	@EventHandler
	public void onLeave(final PlayerQuitEvent e) {
		e.quitMessage(null);
		if (e.getPlayer().hasMetadata("cns")) {
			Arena.getPlArena(e.getPlayer().getName()).removePl(e.getPlayer().getName());
		}
	}
	
	@EventHandler
	public void onFood(final FoodLevelChangeEvent e) {
		if (e.getEntity().hasMetadata("kls")) {
			return;
		}
		e.setFoodLevel(19);
	}
	
	@EventHandler
	public void onDeath(final PlayerDeathEvent e) {
		if (!e.getEntity().hasMetadata("kls")) {
			//можт добавим что то
			return;
		}
	}
	
	@EventHandler
	public void onResp(final PlayerRespawnEvent e) {
		if (Main.lobby != null) {
			if (!e.getPlayer().hasMetadata("kls")) {
				e.setRespawnLocation(Main.lobby);
			} else {
				e.setRespawnLocation(Arena.getPlArena(e.getPlayer().getName()).getPlTeam(e.getPlayer().getName()).getSpwn());
			}
			return;
		}
	}
	
	@EventHandler
	public void onSpawn(final EntitySpawnEvent e) {
		if (e.getEntityType() == EntityType.PHANTOM) {
			((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 1000000, 1));
			((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 1));
		}
	}
	
	@EventHandler(ignoreCancelled = false, priority = EventPriority.LOW)
	public void onDamage(final EntityDamageEvent e) {
		
		if (e.getEntity() instanceof LivingEntity && ((Damageable) e.getEntity()).getHealth() - e.getFinalDamage() <= 0) {
			e.setDamage(0);
			if (e.getEntity() instanceof Player) {
				final Player p = (Player) e.getEntity();
				//на арене ли игрок?
				final Arena ar = Arena.getPlArena(p.getName());
				if (ar != null && ar.getState() == GameState.RUNNING) {
					//+1 смерть игроку
					final Location loc = p.getLocation().add(0d, 0.4d, 0d);
					loc.getWorld().spawnParticle(Particle.SCULK_SOUL, loc, 40, 0.4d, 0.8d, 0.4d, 0, null, false);
					dropRecs(p.getInventory(), ar.recs, loc);
					if (e instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) e).getDamager() instanceof Player) {
						//если игрок убил игрока
						final Player dmgr = (Player) ((EntityDamageByEntityEvent) e).getDamager();
						dmgr.playSound(e.getEntity().getLocation(), Sound.BLOCK_ANVIL_LAND, 0.3f, 2);
						//50 монет и 1 килл дамагеру
						EntMeta.chngMeta(dmgr, "kls", (byte) 1);
						ApiOstrov.addStat(dmgr, Stat.WZ_klls);
						EntMeta.chngMoney(dmgr, (short) 50, true);
						if (ar.getPlTeam(p.getName()).rsps == 0) {
							//в спектатор
							ar.killToSpecPl(p.getName(), ar.getPlTeam(p.getName()).getName().substring(0, 2) + p.getName() + fromPlDie() + ar.getPlTeam(dmgr.getName()).getName().substring(0, 2) + dmgr.getName() + "§7, и больше его никто не видел...");
						} else {
							ar.getPlTeam(p.getName()).rsps--;
							p.teleport(ar.getPlTeam(p.getName()).getSpwn());
							Main.nrmlzPl(p);
							p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 1000000, 0, true, false));
							ApiOstrov.addStat(p, Stat.WZ_dths);
							//инфа
							for (final String s : ar.getPls()) {
								Bukkit.getPlayer(s).sendMessage(Main.prf() + ar.getPlTeam(p.getName()).getName().substring(0, 2) + p.getName() + fromPlDie() + ar.getPlTeam(dmgr.getName()).getName().substring(0, 2) + dmgr.getName() + "§7, теряя одно из возрождений!");
							}
						}
					} else if (e instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) e).getDamager() instanceof Projectile && ((Projectile) ((EntityDamageByEntityEvent) e).getDamager()).getShooter() instanceof Player) {
						final Player dmgr = (Player) ((Projectile) ((EntityDamageByEntityEvent) e).getDamager()).getShooter();
						//если игрока убил игрок с лука
						//50 монет и 1 килл дамагеру
						EntMeta.chngMeta(dmgr, "kls", (byte) 1);
						ApiOstrov.addStat(dmgr, Stat.WZ_klls);
						EntMeta.chngMoney(dmgr, (short) 50, true);
						if (ar.getPlTeam(p.getName()).rsps == 0) {
							//в спектатор
							ar.killToSpecPl(p.getName(), ar.getPlTeam(p.getName()).getName().substring(0, 2) + p.getName() + fromPlDie() + ar.getPlTeam(dmgr.getName()).getName().substring(0, 2) + dmgr.getName() + "§7, и больше его никто не видел...");
						} else {
							ar.getPlTeam(p.getName()).rsps--;
							p.teleport(ar.getPlTeam(p.getName()).getSpwn());
							Main.nrmlzPl(p);
							p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 1000000, 0, true, false));
							ApiOstrov.addStat(p, Stat.WZ_dths);
							//инфа
							for (final String s : ar.getPls()) {
								Bukkit.getPlayer(s).sendMessage(Main.prf() + ar.getPlTeam(p.getName()).getName().substring(0, 2) + p.getName() + fromPlDie() + ar.getPlTeam(dmgr.getName()).getName().substring(0, 2) + dmgr.getName() + "§7, теряя одно из возрождений!");
							}
						}
					} else {
						//если не игрок убил игрока
						if (ar.getPlTeam(p.getName()).rsps == 0) {
							//в спектатор
							ar.killToSpecPl(p.getName(), ar.getPlTeam(p.getName()).getName().substring(0, 2) + p.getName() + MainLis.snglDie() + ", и больше его никто не видел...");
						} else {
							ar.getPlTeam(p.getName()).rsps--;
							p.teleport(ar.getPlTeam(p.getName()).getSpwn());
							Main.nrmlzPl(p);
							p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 1000000, 0, true, false));
							//инфа
							for (final String s : ar.getPls()) {
								Bukkit.getPlayer(s).sendMessage(Main.prf() + ar.getPlTeam(p.getName()).getName().substring(0, 2) + p.getName() + snglDie() + ", потратив одно из возрождений!");
							}
						}
					}
					ar.updTmsSb();
				} else {
					e.setCancelled(true);
				}
			} else if (e instanceof EntityDamageByEntityEvent) {
				//если игрок убивает моба
				if (((EntityDamageByEntityEvent) e).getDamager() instanceof Player) {
					final Player dmgr = (Player) ((EntityDamageByEntityEvent) e).getDamager();
					dmgr.playSound(e.getEntity().getLocation(), Sound.BLOCK_ANVIL_LAND, 0.3f, 2);
					//монеты дамагеру
					if (e.getEntity().hasMetadata("lvl")) {
						final Arena ar = Arena.getPlArena(dmgr.getName());
						if (ar != null && ar.getState() == GameState.RUNNING) {
							ApiOstrov.addStat(dmgr, Stat.WZ_mbs);
							EntMeta.chngMoney(dmgr, (short) (e.getEntity().getMetadata("lvl").get(0).asByte() * 5 + 5), true);
						}
					}
					e.getEntity().remove();
				} else if (((EntityDamageByEntityEvent) e).getDamager() instanceof Projectile && ((Projectile) ((EntityDamageByEntityEvent) e).getDamager()).getShooter() instanceof Player) {
					final Player dmgr = (Player) ((Projectile) ((EntityDamageByEntityEvent) e).getDamager()).getShooter();
					dmgr.playSound(e.getEntity().getLocation(), Sound.BLOCK_ANVIL_LAND, 0.3f, 2);
					//монеты дамагеру
					if (e.getEntity().hasMetadata("lvl")) {
						final Arena ar = Arena.getPlArena(dmgr.getName());
						if (ar != null && ar.getState() == GameState.RUNNING) {
							ApiOstrov.addStat(dmgr, Stat.WZ_mbs);
							EntMeta.chngMoney(dmgr, (short) (e.getEntity().getMetadata("lvl").get(0).asByte() * 5 + 5), true);
						}
					}
					e.getEntity().remove();
				}
			} else {
				//если моб умер не от игрока
				e.getEntity().remove();
			}
		}

		if (e.getEntity() instanceof Player) {
			final Player p = (Player) e.getEntity();
			//на арене ли игрок?
			if (p.hasMetadata("kls") && Arena.getPlArena(p.getName()).getState() != GameState.END) {
				//если игрок бьет тиммейта
				final Arena ar = Arena.getPlArena(p.getName());
				if (e instanceof EntityDamageByEntityEvent) {
					e.setCancelled(((EntityDamageByEntityEvent) e).getDamager() instanceof Player && ar.getPlTeam(p.getName()).getName().equalsIgnoreCase(ar.getPlTeam(((EntityDamageByEntityEvent) e).getDamager().getName()).getName()));
				} else if (e.getCause() == DamageCause.FIRE_TICK) {
					e.setCancelled(true);
					p.setFireTicks(0);
				}
			} else {
				//если игрок в лобби и его бьет билдер
				e.setCancelled(!(e instanceof EntityDamageByEntityEvent) || !(((EntityDamageByEntityEvent) e).getDamager() instanceof Player) || !((EntityDamageByEntityEvent) e).getDamager().hasPermission("ostrov.builder"));
			}
		}
	}
	
	private void dropRecs(final PlayerInventory inv, final Material[] rcs, final Location loc) {
		int i = 0;
		for (final ItemStack it : inv) {
			if (it != null) {
				for (final Material rc : rcs) {
					if (it.getType() == rc) {
						loc.getWorld().dropItemNaturally(loc, it);
						inv.setItem(i, Main.air);
					}
				}
			}
			i++;
		}
	}

	@EventHandler
	public void onChat(final AsyncPlayerChatEvent e) {
		if (e.getMessage().startsWith("/")) {
			return;
		}
		final Player snd = e.getPlayer();
		//если на арене
		if (snd.hasMetadata("cns")) {
			final Arena ar = Arena.getPlArena(snd.getName());
			if (e.getMessage().startsWith("!") && ar.getState() == GameState.RUNNING) {
				for (final Player rec : e.getRecipients()) {
					if (ar.getSpcs().contains(rec.getName())) {
						sendSpigotMsg(Main.prf().replace('[', '<').replace(']', '>') + 
							"§7[" + ar.getPlTeam(snd.getName()).getName() + "§7] " + 
							snd.getName() + " ≫ " + e.getMessage().replaceFirst("!", ""), rec);
					} else if (Arena.getPlArena(rec.getName()) != null && Arena.getPlArena(rec.getName()).getName().equalsIgnoreCase(ar.getName())) {
						sendSpigotMsg(Main.prf().replace('[', '<').replace(']', '>') + 
							"§7[" + ar.getPlTeam(snd.getName()).getName() + "§7] " + 
							snd.getName() + " ≫ " + e.getMessage().replaceFirst("!", ""), rec);
					}
				}
			} else {
				for (final Player rec : e.getRecipients()) {
					if (rec.hasMetadata("cns") && !rec.hasMetadata("kls")) {
						if (ar.getState() == GameState.LOBBY_WAIT) {
							sendSpigotMsg(Main.prf().replace('[', '<').replace(']', '>') + "§7[§6" + ar.getName() + "§7] §2" + snd.getName() + " §7≫ " + e.getMessage(), rec);
						}
						continue;
					}
					final Arena rar;
					switch (ar.getState()) {
					case LOBBY_WAIT:
						sendSpigotMsg(Main.prf().replace('[', '<').replace(']', '>') + "§7[§6" + ar.getName() + "§7] §2" + snd.getName() + " §7≫ " + e.getMessage(), rec);
						break;
					case RUNNING:
						rar = Arena.getPlArena(rec.getName());
						if (rar != null && rar.getName().equalsIgnoreCase(ar.getName()) && ar.getPlTeam(rec.getName()).getName().equalsIgnoreCase(ar.getPlTeam(snd.getName()).getName())) {
							sendSpigotMsg(Main.prf().replace('[', '<').replace(']', '>') + ar.getPlTeam(snd.getName()).getName().substring(0, 2) + " " + snd.getName() + " §7≫ " + e.getMessage(), rec);
						}
						break;
					case END:
						rar = Arena.getPlArena(rec.getName());
						if ((rar != null && Arena.getPlArena(rec.getName()).getName().equalsIgnoreCase(ar.getName())) || ar.getSpcs().contains(rec.getName())) {
							sendSpigotMsg(Main.prf().replace('[', '<').replace(']', '>') + 
								"§7[" + ar.getPlTeam(snd.getName()).getName() + "§7] " + 
								snd.getName() + " ≫ " + e.getMessage().replaceFirst("!", ""), rec);
						}
						break;
					}
		        }
			}
		} else {
			final Iterator<Player> recs = e.getRecipients().iterator();
        	if (recs.next().hasMetadata("kls")) {
        		recs.remove();
        	}
			return;
		}
        e.getRecipients().clear();
    }

	/*@EventHandler
    public void Dchat(final DeluxeChatEvent e) {
        final Player p = e.getPlayer();
        final Arena ar = Arena.getPlArena(p.getName());
        if (ar != null && ar.getState() != GameState.LOBBY_WAIT) {
            e.setCancelled(true);
            return;
        }
        final Iterator<Player> recipients = e.getRecipients().iterator();
        while (recipients.hasNext()) {
            final Player recipient = recipients.next();
            if (!recipient.getWorld().getName().equalsIgnoreCase(p.getWorld().getName())) {
                recipients.remove();
            }
        }
        if (ar != null) {
            e.getDeluxeFormat().setPrefix(Main.prf() + "§7<§2" + ar.getName() + "§7> ");
        }
    }*/
	
	public static byte getPlaying() {
		byte in = 0;
		for (final Arena ar : Main.activearenas.values()) {
			in += ar.getPlAmt();
		}
		return in;
	}

	public static String fromPlDie() {
		switch (new Random().nextInt(4)) {
		case 0:
			return "§7 помер от рук ";
		case 1:
			return "§7 был раздроблен ";
		case 2:
			return "§7 не смог устоять перед красотой ";
		case 3:
			return "§7 самоликвидировался при виде ";
		default:
			return "§7";
		}
	}
	
	public static String snglDie() {
		switch (new Random().nextInt(4)) {
		case 0:
			return "§7 скончался";
		case 1:
			return "§7 откинул коньки";
		case 2:
			return "§7 сдохся";
		case 3:
			return "§7 умер";
		default:
			return "§7";
		}
	}
	
	public static void sendSpigotMsg(final String msg, final Player p) {
		p.spigot().sendMessage(new TextComponent(msg));
	}
}
