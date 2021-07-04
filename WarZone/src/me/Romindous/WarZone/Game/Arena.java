package me.Romindous.WarZone.Game;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import me.Romindous.WarZone.Utils.TitleManager;
import me.Romindous.WarZone.Main;
import me.Romindous.WarZone.Listeners.MainLis;
import me.Romindous.WarZone.Utils.EntMeta;
import me.Romindous.WarZone.Utils.Inventories;
import me.Romindous.WarZone.Utils.Translates;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.player.PM;

public class Arena {

	private GameState state;
	private final String name;
	private final String tl;
	private final byte min;
	private final byte max;
	public final UUID[] shps;
	public final Material[] mnbls;
	public final Material[] recs;
	private short time;
	private final Team[] tms;
	private final Location cntr;
	private final HashSet<String> spcs;
	private final HashSet<String> pls;
	private BukkitTask task;
	private BukkitTask mbspwm;
	private final ScoreboardManager smg;
	
	public Arena(final String name, final byte min, final byte max, final Location cntr, final YamlConfiguration ars) {
		//подготвка карты
		this.time = 0;
		this.max = max;
		this.min = min;
		this.name = name;
		this.cntr = cntr;
		this.spcs = new HashSet<String>();
		this.pls = new HashSet<String>();
		this.state = GameState.LOBBY_WAIT;
		this.smg = Bukkit.getScoreboardManager();
		final ConfigurationSection cs = ars.getConfigurationSection("arenas." + name);
		//ископаемые блоки, ресурсы, и покупаемые блоки
		this.mnbls = new Material[] {Material.getMaterial(cs.getString("mnbl").split(":")[0]), 
			Material.getMaterial(cs.getString("mnbl").split(":")[1]), 
			Material.getMaterial(cs.getString("mnbl").split(":")[2])};
		this.recs = new Material[] {Material.getMaterial(cs.getString("recs").split(":")[0]), 
			Material.getMaterial(cs.getString("recs").split(":")[1]), 
			Material.getMaterial(cs.getString("recs").split(":")[2])};
		//предмет для копания
		this.tl = ars.getString("arenas." + name + ".tl");
		//заменяем прошлые цвета, если есть
		for (final String s : cs.getConfigurationSection("teams").getKeys(false)) {
			whtClrs(s.charAt(0));
		}
		//комманды на карте
		byte i = (byte) cs.getString("teams.x").split(":").length;
		tms = new Team[i];
		for (i--; i >= 0; i--) {
			//при создании проверяем если комманда с таким цветом уже есть
			ChatColor cc = ChatColor.values()[new Random().nextInt(16)];
			while (true) {
				boolean org = true;
				for (final Team tm : tms) {
					if (tm != null && tm.name.charAt(1) == cc.getChar()) {
						org = false;
					}
				}
				if (org) {
					break;
				} else {
					cc = ChatColor.values()[new Random().nextInt(16)];
				}
			}
			//--
			tms[i] = new Team(Translates.transClr(cc), 
					new Location(cntr.getWorld(), Integer.parseInt(cs.getString("teams.x").split(":")[i]), Integer.parseInt(cs.getString("teams.y").split(":")[i]), Integer.parseInt(cs.getString("teams.z").split(":")[i])));
			//асинхронная замена цвета блоков
			tmClrs(tms[i].spwn, ars, cc);
			//--
		}
		//--
		//магазины на карте
		i = (byte) ars.getString("arenas." + name + ".shops.x").split(":").length;
		this.shps = new UUID[i];
		for (i--; i >= 0; i--) {
			final ZombieVillager vll = (ZombieVillager) cntr.getWorld().spawnEntity(new Location(cntr.getWorld(), 
				Integer.parseInt(cs.getString("shops.x").split(":")[i]) + 0.5, 
				Integer.parseInt(cs.getString("shops.y").split(":")[i]) + 0.1, 
				Integer.parseInt(cs.getString("shops.z").split(":")[i]) + 0.5), 
				EntityType.ZOMBIE_VILLAGER);
			vll.setVillagerType(Translates.getBmVllTp(vll.getLocation().getBlock().getBiome()));
			vll.setVillagerProfession(Profession.CARTOGRAPHER);
			vll.setTicksLived(1);
			vll.setAdult();
			vll.setCustomName("§6§lМагазин");
			vll.setInvulnerable(true);
			vll.setPersistent(true);
			vll.setRemoveWhenFarAway(false);
			shps[i] = vll.getUniqueId();
		}
		//--
	}
	//асинхронная замена на цвета блоков
	public void tmClrs(final Location loc, final YamlConfiguration ars, final ChatColor cc) {
		final char ch = cc.getChar();
		final Set<Block> tbs = new HashSet<Block>();
		Bukkit.getScheduler().runTaskAsynchronously(Main.plug, new Runnable() {
			@Override
			public void run() {
				final World w = loc.getWorld();
				final short tx = (short) loc.getBlockX();
				final short ty = (short) loc.getBlockY();
				final short tz = (short) loc.getBlockZ();
				for (byte x = Main.clrRng; x >= -Main.clrRng; x--) {
					for (byte y = Main.clrRng; y >= -Main.clrRng; y--) {
						for (byte z = Main.clrRng; z >= -Main.clrRng; z--) {
							final String s = w.getBlockAt(tx + x, ty + y, tz + z).getType().toString();
							if (s.startsWith("WHITE")) {
								tbs.add(w.getBlockAt(tx + x, ty + y, tz + z));
								if (ars.contains("arenas." + name + ".teams." + ch)) {
									ars.set("arenas." + name + ".teams." + ch + ".x", ars.getString("arenas." + name + ".teams." + ch + ".x") + ':' + (tx + x));
									ars.set("arenas." + name + ".teams." + ch + ".y", ars.getString("arenas." + name + ".teams." + ch + ".y") + ':' + (ty + y));
									ars.set("arenas." + name + ".teams." + ch + ".z", ars.getString("arenas." + name + ".teams." + ch + ".z") + ':' + (tz + z));
								} else {
									ars.set("arenas." + name + ".teams." + ch + ".x", tx + x);
									ars.set("arenas." + name + ".teams." + ch + ".y", ty + y);
									ars.set("arenas." + name + ".teams." + ch + ".z", tz + z);
								}
							}
						}
					}
				}
			}
		});
		Bukkit.getScheduler().runTaskLater(Main.plug, new Runnable() {
			@Override
			public void run() {
				if (Main.activearenas.contains(getArena())) {
					for (final Block b : tbs) {
						b.setType(Material.getMaterial(Translates.mtFromCC(cc) + b.getType().toString().substring(5)));
					}
					try {
						ars.save(new File(Main.folder + File.separator + "arenas.yml"));
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		}, 600);
	}
	//--
	//асинхронная замена на белый цвет
	public void whtClrs(final char ch) {
		final YamlConfiguration ars = YamlConfiguration.loadConfiguration(new File(Main.folder + File.separator + "arenas.yml"));
		//если нету
		if (!ars.contains("arenas." + name + ".teams." + ch + ".x")) {
			return;
		}
		final String[] xs = ars.getString("arenas." + name + ".teams." + ch + ".x").split(":");
		final String[] ys = ars.getString("arenas." + name + ".teams." + ch + ".y").split(":");
		final String[] zs = ars.getString("arenas." + name + ".teams." + ch + ".z").split(":");
		short i = (short) xs.length;
		for (i--; i >= 0; i--) {
			final Block b = cntr.getWorld().getBlockAt(Integer.parseInt(xs[i]), Integer.parseInt(ys[i]), Integer.parseInt(zs[i]));
			b.setType(Material.getMaterial("WHITE" + b.getType().toString().substring(b.getType().toString().indexOf('_', b.getType().toString().startsWith("LIG") ? 8 : 0))));
		}
		
		ars.set("arenas." + name + ".teams." + ch, null);
		
		try {
			ars.save(new File(Main.folder + File.separator + "arenas.yml"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	//--
	//получение арены по игроку
	public static Arena getPlArena(final String name) {
		for (final Arena ar : Main.activearenas) {
			if (ar.getPls().contains(name)) {
				return ar;
			}
		}
		
		return null;
	}
	//получение арены по имени
	public static Arena getNameArena(final String name) {
		for (Arena ar : Main.activearenas) {
			if (ar.getName().equalsIgnoreCase(name)) {
				return ar;
			}
		}
		return null;
	}
	//получаем комманду от игрока
	public Team getPlTeam(final String name) {
		for (final Team tm : tms) {
			if (tm.pls.contains(name)) {
				return tm;
			}
		}
		
		return null;
	}
	//получаем комманду от имени
	public Team getNameTeam(final String name) {
		for (final Team tm : tms) {
			if (tm.name.equalsIgnoreCase(name)) {
				return tm;
			}
		}
		
		return null;
	}
	//получаем комманду от игрока
	public Team getMinTeam() {
		Team tm = null;
		for (final Team t : tms) {
			if (tm == null || t.pls.size() < tm.pls.size()) {
			tm = t;
				}
		}
		
		return tm;
	}
	//чек если осталась 1 комманда с игроками
	public Team oneTmLft() {
		boolean one = false;
		for (final Team tm : tms) {
			if (!tm.pls.isEmpty()) {
				if (one) {
					return null;
				}
				one = true;
			}
		}
		for (final Team tm : tms) {
			if (!tm.pls.isEmpty()) {
				return tm;
			}
		}
		return null;
	}
	//каждый игрок на арене
	public Set<String> getPls() {
		return pls;
	}
	//комманды
	public Team[] getTms() {
		return tms;
	}
	//вещь для добычи ресурсов
	public String getTlSfx() {
		return tl;
	}
	//центр
	public Location getCntr() {
		return cntr;
	}
	//время
	public short getTim() {
		return time;
	}
	
	public byte getMin() {
		return min;
	}
	
	public byte getMax() {
		return max;
	}
	//кол-во игроков на арене
	public byte getPlAmt() {
		return (byte) pls.size();
	}
	
	public GameState getState() {
		return state;
	}

	public Arena getArena() {
		return this;
	}
	
	public BukkitTask getTask() {
		return task;
	}

	public String getName() {
		return name;
	}
	//спектаторы
	public HashSet<String> getSpcs() {
		return spcs;
	}

	//сколько игроков из скольки
	public String amtToHB() {
		return pls.size() < min ? 
				"§7На карте §2" + pls.size() + "§7 игроков, нужно еще §2" + (min - pls.size()) + "§7 для начала" 
			: 
			"§7На карте §2" + pls.size() + "§7 игроков, максимум: §2" + max;
	}
	
	public void killToSpecPl(final String name, final String dmsg) {
		Main.lobbyPlayer(Bukkit.getPlayer(name));
		Bukkit.getPlayer(name).sendMessage(Main.prf() + "Вы были убиты, а возрождений у вашей команды небыло... ГГ!");
		//убираем из списка
		pls.remove(name);
		//кидаем в спектаторы
		for (final String s : spcs) {
			if (Bukkit.getPlayer(s) != null) {
				Bukkit.getPlayer(s).sendMessage(Main.prf() + dmsg);
			}
		}
		addSpct(Bukkit.getPlayer(name));
		//убираем игрока
		for (final Team tm : tms) {
			tm.pls.remove(name);
		}
		//записываем проигрыш
		Main.data.chngNum(name, "lss", 1);
		//--
		//инфа
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ИГРА, "§7[§2Поле Брани§7]", "§cИдет Игра", " ", "§7Игроков: " + pls.size(), "", pls.size());
		for (final String s : pls) {
			Bukkit.getPlayer(s).sendMessage(Main.prf() + dmsg);
		}
		//таб
		for (final Player pl : Bukkit.getOnlinePlayers()) {
			pl.setPlayerListFooter("§7Сейчас в игре: §2" + MainLis.getPlaying() + "§7 человек!");
		}
		//если осталась только 1 комманда
		if (oneTmLft() != null) {
			task.cancel();
			countEnd(new Random(), oneTmLft().name);
		}
	}
	
	public void addSpct(final Player p) {
		p.sendMessage(Main.prf() + "Помещаем вас в качестве зрителя");
		p.getInventory().clear();
		p.getInventory().setItem(8, Inventories.mkItm(Material.REDSTONE, "§4Обратно в лобби", true));
		p.teleport(cntr);
		spcs.add(p.getName());
		p.setGameMode(GameMode.SPECTATOR);
		for (final String s : pls) {
			if (Bukkit.getPlayer(s).hasMetadata("kls")) {
				p.showPlayer(Main.plug, Bukkit.getPlayer(s));
			}
		}
	}

	public void removePl(final String name) {
		Main.lobbyPlayer(Bukkit.getPlayer(name));
		switch (getState()) {
		case LOBBY_WAIT:
			//убираем игрока
			pls.remove(name);
			for (final Team tm : tms) {
				tm.pls.remove(name);
			}
			//таб
			for (final Player pl : Bukkit.getOnlinePlayers()) {
				pl.setPlayerListFooter("§7Сейчас в игре: §2" + MainLis.getPlaying() + "§7 человек!");
			}
			//инфа
			Bukkit.getPlayer(name).sendMessage(Main.prf() + "Вы покинули карту §2" + getName());
			for (final String s : pls) {
				TitleManager.sendActionBar(Bukkit.getPlayer(s), amtToHB());
				Bukkit.getPlayer(s).sendMessage(Main.prf() + (getPlTeam(name) == null ? "§2" : getPlTeam(name).name.substring(0, 2)) + name + "§7 вышел с карты!");
			}
			//если недостаточно игроков
			if (pls.size() < min) {
				if (task != null) {
					task.cancel();
				}
				if (pls.size() + 1 == min) {
					ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ОЖИДАНИЕ, "§7[§2Поле Брани§7]", "§2Ожидание", " ", "§7Игроков: §2" + pls.size() + "§7/§2" + min, "", pls.size());
					for (final String s : pls) {
						waitScore(s);
					}
				} else {
					for (final String s : pls) {
						Bukkit.getPlayer(s).sendMessage(Main.prf() + "На карте недостаточно игроков для начала!");
						Main.chgSbdTm(Bukkit.getPlayer(s).getScoreboard(), "pls", "", min - pls.size() > 1 ? 
							" §2" + (min - pls.size()) + "§7 игроков" 
							:
							" §2" + (min - pls.size()) + "§7 игрока");
					}
					if (pls.size() == 0) {
						Main.endArena(this);
					}
				}
			} else {
				ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.СТАРТ, "§7[§2Поле Брани§7]", "§6Скоро старт!", " ", "§7Игроков: §6" + pls.size() + "§7/§6" + max, "", pls.size());
				for (final String s : pls) {
					Main.chgSbdTm(Bukkit.getPlayer(s).getScoreboard(), "pls", "", " §2" + String.valueOf(getPlAmt()) + "§7/§2" + String.valueOf(max));
				}
			}
			break;
		case RUNNING:
			//инфа
			for (final String s : pls) {
				Bukkit.getPlayer(s).sendMessage(s.equalsIgnoreCase(name) ? Main.prf() + "Вы покинули игру §2" + getName() : Main.prf() + getPlTeam(name).name.substring(0, 2) + name + "§7 вышел из игры!");
			}
			for (final String s : spcs) {
				Bukkit.getPlayer(s).sendMessage(Main.prf() + getPlTeam(name).name.substring(0, 2) + name + "§7 вышел из игры!");
			}
			//убираем из списка
			pls.remove(name);
			//убираем игрока
			for (final Team tm : tms) {
				tm.pls.remove(name);
			}
			//записываем проигрыш
			Main.data.chngNum(name, "lss", 1);
			//--
			//таб
			for (final Player pl : Bukkit.getOnlinePlayers()) {
				pl.setPlayerListFooter("§7Сейчас в игре: §2" + MainLis.getPlaying() + "§7 человек!");
			}
			//если осталась только 1 комманда
			if (oneTmLft() != null) {
				task.cancel();
				countEnd(new Random(), oneTmLft().name);
			}
			break;
		case END:
			Bukkit.getPlayer(name).sendMessage(Main.prf() + "Вы покинули игру §2" + getName());
			//убираем из списка
			pls.remove(name);
			for (final Team tm : tms) {
				tm.pls.remove(name);
			}
			//таб
			for (final Player pl : Bukkit.getOnlinePlayers()) {
				pl.setPlayerListFooter("§7Сейчас в игре: §2" + MainLis.getPlaying() + "§7 человек!");
			}
			break;
		}
	}
	//доавление игрока
	public void addPl(final String name) {
		final Player p = Bukkit.getPlayer(name);
		if (pls.size() < max) {
			//добавляем игрока
			pls.add(name);
			//таб
			for (final Player pl : Bukkit.getOnlinePlayers()) {
				pl.setPlayerListFooter("§7Сейчас в игре: §2" + MainLis.getPlaying() + "§7 человек!");
			}
			p.sendMessage(Main.prf() + "Вы зашли на карту §2" + getName());
			final String prm = Main.data.getString(p.getName(), "prm");
			//неймтег
			PM.nameTagManager.setNametag(name, "§7[§6" + getName() + "§7] §2", (prm.length() > 1 ? " §7(§e" + prm + "§7)" : ""));
	        //таб
			p.setPlayerListName("§7[§6" + getName() + "§7] " + name + (prm.length() > 1 ? " §7(§e" + prm + "§7)" : ""));
			//инфа
			for (final String s : pls) {
				TitleManager.sendActionBar(Bukkit.getPlayer(s), amtToHB());
				if (!s.equalsIgnoreCase(name)) {
					Bukkit.getPlayer(s).sendMessage(Main.prf() + "§2" + name + "§7 зашел на карту!");
				}
			}
			//экипировка лобби
			Main.waitPlayer(p);
			//начинается ли игра?
			if (pls.size() == min) {
				ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.СТАРТ, "§7[§2Поле Брани§7]", "§6Скоро старт!", " ", "§7Игроков: §6" + pls.size() + "§7/§6" + max, "", pls.size());
				countLobby();
			} else if (pls.size() < min) {
				ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ОЖИДАНИЕ, "§7[§2Поле Брани§7]", "§2Ожидание", " ", "§7Игроков: §2" + pls.size() + "§7/§2" + min, "", pls.size());
				waitScore(name);
				for (final String s : pls) {
					Main.chgSbdTm(Bukkit.getPlayer(s).getScoreboard(), "pls", "", min - pls.size() > 1 ? 
							" §2" + (min - pls.size()) + "§7 игроков" 
							:
							" §2" + (min - pls.size()) + "§7 игрока");
				}
			} else {
				ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.СТАРТ, "§7[§2Поле Брани§7]", "§6Скоро старт!", " ", "§7Игроков: §6" + pls.size() + "§7/§6" + max, "", pls.size());
				lobbyScore(name);
				for (final String s : pls) {
					Main.chgSbdTm(Bukkit.getPlayer(s).getScoreboard(), "pls", "", " §2" + String.valueOf(getPlAmt()) + "§7/§2" + String.valueOf(max));
				}
			}
		} else {
			p.sendMessage(Main.prf() + "§cКарта §2" + getName() + "§c заполнена!");
		}
	}
	//привлечение игрока в комманду
	public void addToTm(final Player p, final Team tm) {
		tm.pls.add(p.getName());
		final String prm = Main.data.getString(p.getName(), "prm");
		//таб
		p.setPlayerListName("§7[§6" + getName() + "§7] " + tm.name.substring(0, 2) + p.getName() + (prm.length() > 1 ? " §7(§e" + prm + "§7)" : ""));
		//неймтег
		PM.nameTagManager.setNametag(p.getName(), "§7[§6" + getName() + "§7] " + tm.name.substring(0, 2), (prm.length() > 1 ? " §7(§e" + prm + "§7)" : ""));
		//звук
		p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 2, 1);
		p.sendMessage(Main.prf() + "Вы присоеденились к комманде " + tm.name + "§7!");
	}
	//убирание игрока из комманды
	public void remFromTm(final String name, final Team tm) {
		tm.pls.remove(name);
	}
	//отсчет в лобби
	public void countLobby() {
		time = 30;
		for (final String s : pls) {
			lobbyScore(s);
		}
		task = new BukkitRunnable() {
			
			@Override
			public void run() {
				for (final String s : pls) {
					Main.chgSbdTm(Bukkit.getPlayer(s).getScoreboard(), "time", "", " §6" + String.valueOf(time) + "§7 сек");
				}
				switch (time) {
				case 20:
				case 10:
				case 5:
					for (final String s : pls) {
						Bukkit.getPlayer(s).playSound(Bukkit.getPlayer(s).getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
						TitleManager.sendActionBar(Bukkit.getPlayer(s), "§7До начала осталось §2" + time + " §7секунд!");
					}
					break;
				case 4:
				case 3:
				case 2:
				case 1:
					for (final String s : pls) {
						TitleManager.sendTitle(Bukkit.getPlayer(s), "", "§6" + time, 20);
						Bukkit.getPlayer(s).playSound(Bukkit.getPlayer(s).getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
					}
					break;
				case 0:
					task.cancel();
					countGame();
					break;
				default:
					break;
				}
				time--;
			}
		}.runTaskTimer(Main.plug, 0, 20);
	}
	//отсчет в игре
	public void countGame() {
		time = (short) (Main.gmTmMin * 60);
		state = GameState.RUNNING;
		for (final String s : pls) {
			final Player p = Bukkit.getPlayer(s);
			//показ или скрывание игроков
			for (final Player pl : Bukkit.getOnlinePlayers()) {
				if (pls.contains(pl.getName())) {
					p.showPlayer(Main.plug, pl);
					pl.showPlayer(Main.plug, p);
				} else {
					p.hidePlayer(Main.plug, pl);
					pl.hidePlayer(Main.plug, p);
				}
			}
			//подставка в комманду
			if (getPlTeam(s) == null) {
				addToTm(p, getMinTeam());
			}
			//таб
			p.setPlayerListName("§7[" + getPlTeam(s).name + "§7] " + s);
			//неймтег
			PM.nameTagManager.setNametag(s, getPlTeam(s).name.substring(0, 2), "");
			//обовляем игрока
			Main.nrmlzPl(p);
			p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 1000000, 0, true, false));
			p.getInventory().clear();
			//даем инструмент
			p.getInventory().addItem(Inventories.mkItm(Material.getMaterial("WOODEN" + tl), "§6Первый Инструмент", true));
			//мета
			EntMeta.chngMeta(p, "kls", (byte) 0);
			//инфа
			TitleManager.sendTitle(p, "§2Начинаем!", "§7Собирайте §2ресурсы §7и громите чужие §2комманды§7!" + time, 20);
			p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 20, 1);
		}
		//тп игроков к комманду
		for (final Team tm : tms) {
			for (final String s : tm.pls) {
				Bukkit.getPlayer(s).teleport(tm.spwn);
				//скорбоард
				runnScore(s);
			}
		}
		//спавним мобов
		mbspwm = new MnstrRun(cntr, (byte) cntr.distance(tms[0].spwn)).runTaskTimer(Main.plug, 0, Main.mbPrd * 20);
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ИГРА, "§7[§2Поле Брани§7]", "§cИдет Игра", " ", "§7Игроков: " + pls.size(), "", pls.size());
		task = new BukkitRunnable() {
			
			@Override
			public void run() {
				//scoreboard stuff
				for (final String s : pls) {
					if (Bukkit.getPlayer(s) == null) {
						//убираем из списка
						pls.remove(name);
						//убираем игрока
						for (final Team tm : tms) {
							tm.pls.remove(name);
						}
						//записываем проигрыш
						Main.data.chngNum(name, "lss", 1);
						//таб
						for (final Player pl : Bukkit.getOnlinePlayers()) {
							pl.setPlayerListFooter("§7Сейчас в игре: §2" + MainLis.getPlaying() + "§7 человек!");
						}
						//если осталась только 1 комманда
						if (oneTmLft() != null) {
							task.cancel();
							countEnd(new Random(), oneTmLft().name);
						}
						return;
					} else {
						Main.chgSbdTm(Bukkit.getPlayer(s).getScoreboard(), "time", "", time % 60 < 10 ? 
								" §2" + String.valueOf((int) (((double) time) / 60.0) + "§7:§20" + (time % 60))
								:
								" §2" + String.valueOf((int) (((double) time) / 60.0) + "§7:§2" + (time % 60)));
					}
				}
				switch (time) {
				case 300:
					for (final String s : pls) {
						TitleManager.sendActionBar(Bukkit.getPlayer(s), "§7До конца осталось §25 §7минут!");
					}
				case 60:
					for (final String s : pls) {
						TitleManager.sendActionBar(Bukkit.getPlayer(s), "§7Конец игры через §21 §7минуту!");
					}
					break;
				case 30:
				case 10:
					for (final String s : pls) {
						TitleManager.sendActionBar(Bukkit.getPlayer(s), "§7Осталось §2" + time + "§7секунд!");
					}
					break;
				case 5:
				case 4:
				case 3:
				case 2:
				case 1:
					for (final String s : pls) {
						TitleManager.sendTitle(Bukkit.getPlayer(s), ChatColor.GOLD + "" + time, "", 20);
						Bukkit.getPlayer(s).playSound(Bukkit.getPlayer(s).getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
					}
					break;
				case 0:
					task.cancel();
					countEnd(new Random(), "");
					break;
				default:
					break;
				}
				time--;
			}
		}.runTaskTimer(Main.plug, 0, 20);
	}
	
	public void countEnd(final Random rand, final String wntm) {
		time = 6;
		mbspwm.cancel();
		state = GameState.END;
		//записываем победу
		if (wntm.isEmpty()) {
			for (final String s : pls) {
				Bukkit.getPlayer(s).sendMessage(Main.prf() + "Результат: Ничья!");
			}
			for (final String s : spcs) {
				Bukkit.getPlayer(s).sendMessage(Main.prf() + "Результат: Ничья!");
			}
		} else {
			for (final String s : pls) {
				Bukkit.getPlayer(s).sendMessage(Main.prf() + wntm + "§7 комманда победила в этой битве!");
				Bukkit.getPlayer(s).sendMessage("§7-=-=-=-=-=-=-=-=-=-=-=-=-=-");
				for (final String wn : pls) {
					Bukkit.getPlayer(s).sendMessage(wntm.substring(0, 2) + wn + "§7 - §2" + Bukkit.getPlayer(wn).getMetadata("kls").get(0).asByte() + "§7 убийств");
				}
				Bukkit.getPlayer(s).sendMessage("§7-=-=-=-=-=-=-=-=-=-=-=-=-=-");
				Main.data.chngNum(s, "wns", 1);
			}
			for (final String s : spcs) {
				Bukkit.getPlayer(s).sendMessage(Main.prf() + wntm + "§7 комманда победила в этой битве!");
				Bukkit.getPlayer(s).sendMessage("§7-=-=-=-=-=-=-=-=-=-=-=-=-=-");
				for (final String wn : pls) {
					Bukkit.getPlayer(s).sendMessage(wntm.substring(0, 2) + wn + "§7 - §2" + Bukkit.getPlayer(wn).getMetadata("kls").get(0).asByte() + "§7 убийств");
				}
				Bukkit.getPlayer(s).sendMessage("§7-=-=-=-=-=-=-=-=-=-=-=-=-=-");
			}
		}
		//--
		for (final String s : pls) {
			endScore(s, wntm);
		}
		task = new BukkitRunnable() {
		
			@Override
			public void run() {
				switch (time) {
				case 0:
					for (final String s : pls) {
						ApiOstrov.moneyChange(Bukkit.getPlayer(s), 100 + (Bukkit.getPlayer(s).getMetadata("kls").get(0).asByte() * 20), "§2Победа");
						Main.lobbyPlayer(Bukkit.getPlayer(s));
					}
					task.cancel();
					Main.endArena(getArena());
					break;
				default:
					for (final String s : pls) {
						Main.chgSbdTm(Bukkit.getPlayer(s).getScoreboard(), "time", "", " §2" + String.valueOf(time));
						final Firework fw = (Firework) Bukkit.getPlayer(s).getWorld().spawnEntity(Bukkit.getPlayer(s).getLocation(), EntityType.FIREWORK);
						fw.setTicksLived(1);
						final FireworkMeta fm = fw.getFireworkMeta();
						fm.addEffect(FireworkEffect.builder().withColor(Color.fromRGB(rand.nextInt(16777000) + 100)).build());
						fw.setFireworkMeta(fm);
						fm.setPower(2);
					}
					break;
				}
				time--;
			}
		}.runTaskTimer(Main.plug, 0, 20);
	}
	
	public void waitScore(final String name) {
		try {
			final ResultSet rs = Main.data.exctStrStmt("SELECT * FROM " + Main.tbl + " WHERE NAME=?", name).executeQuery(); rs.next();
			final Scoreboard sb = smg.getNewScoreboard();
			final Objective ob = sb.registerNewObjective("Поле Брани", "", "§7[§2Поле Брани§7]");
			ob.setDisplaySlot(DisplaySlot.SIDEBAR);
			ob.getScore("§7Карта: §6" + getName())
				.setScore(10);
			ob.getScore("    ").setScore(9);
			Main.crtSbdTm(sb, "pls", "", "§7Ждем еще", min - pls.size() > 1 ? 
					" §2" + (min - pls.size()) + "§7 игроков" 
					:
					" §2" + (min - pls.size()) + "§7 игрока");
			ob.getScore("§7Ждем еще")
				.setScore(8);
			ob.getScore("   ").setScore(7);
			ob.getScore("§7К/Д (убийства на смерти): ")
				.setScore(6);
			ob.getScore("§2" + String.valueOf(rs.getInt("KLS")) + " §7/ (§2" + String.valueOf(rs.getInt("DTHS")) + " §7-§2 " + String.valueOf(rs.getInt("RSPS")) + "§7) = §2" 
				+ Main.getStrFlt((float) rs.getInt("KLS") / (float) (rs.getInt("DTHS") - rs.getInt("RSPS") > 0 ? rs.getInt("DTHS") - rs.getInt("RSPS") : 1)))
				.setScore(5);
			ob.getScore("  ").setScore(4);
			ob.getScore("§7Победы / Проигрыши: ")
				.setScore(3);
			ob.getScore("§2" + String.valueOf(rs.getInt("WNS")) + " §7/§2 " + String.valueOf(rs.getInt("LSS")) + " §7=§2 " 
					+ Main.getStrFlt((float) rs.getInt("WNS") / (float) (rs.getInt("LSS") == 0 ? 1 : rs.getInt("LSS"))))
				.setScore(2);
			ob.getScore(" ").setScore(1);
			
			ob.getScore("§e      ostrov77.su")
				.setScore(0);
			Bukkit.getPlayer(name).setScoreboard(sb);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void lobbyScore(final String name) {
		try {
			final ResultSet rs = Main.data.exctStrStmt("SELECT * FROM " + Main.tbl + " WHERE NAME=?", name).executeQuery(); rs.next();
			final Scoreboard sb = smg.getNewScoreboard();
			final Objective ob = sb.registerNewObjective("Поле Брани", "", "§7[§2Поле Брани§7]");
			ob.setDisplaySlot(DisplaySlot.SIDEBAR);
			ob.getScore("§7Карта: §6" + getName())
				.setScore(12);
			ob.getScore("     ").setScore(11);
			Main.crtSbdTm(sb, "pls", "", "§7Игроков:", " §2" + String.valueOf(getPlAmt()) + "§7/§2" + String.valueOf(max));
			ob.getScore("§7Игроков:")
				.setScore(10);
			ob.getScore("    ").setScore(9);
			Main.crtSbdTm(sb, "time", "", "§7До начала:", " §6" + String.valueOf(time) + "§7 сек");
			ob.getScore("§7До начала:")
				.setScore(8);
			ob.getScore("   ").setScore(7);
			ob.getScore("§7К/Д (убийства на смерти): ")
				.setScore(6);
			ob.getScore("§2" + String.valueOf(rs.getInt("KLS")) + " §7/ (§2" + String.valueOf(rs.getInt("DTHS")) + " §7-§2 " + String.valueOf(rs.getInt("RSPS")) + "§7) = §2" 
				+ Main.getStrFlt((float) rs.getInt("KLS") / (float) (rs.getInt("DTHS") - rs.getInt("RSPS") > 0 ? rs.getInt("DTHS") - rs.getInt("RSPS") : 1)))
				.setScore(5);
			ob.getScore("  ").setScore(4);
			ob.getScore("§7Победы / Проигрыши: ")
				.setScore(3);
			ob.getScore("§2" + String.valueOf(rs.getInt("WNS")) + " §7/§2 " + String.valueOf(rs.getInt("LSS")) + " §7=§2 " 
					+ Main.getStrFlt((float) rs.getInt("WNS") / (float) (rs.getInt("LSS") == 0 ? 1 : rs.getInt("LSS"))))
				.setScore(2);
			ob.getScore(" ").setScore(1);
			
			ob.getScore("§e       ostrov77.su")
				.setScore(0);
			Bukkit.getPlayer(name).setScoreboard(sb);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void runnScore(final String name) {
		final Scoreboard sb = smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("Поле Брани", "", "§7[§2Поле Брани§7]");
		byte i = (byte) (tms.length + 8);
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		ob.getScore("§7Карта: §6" + getName())
			.setScore(i); i--;
		ob.getScore("    ").setScore(i); i--;
		ob.getScore("§7У комманд осталось:")
			.setScore(i); i--;
		for (final Team tm : tms) {
			if (tm.rsps == 0) {
				if (tm.pls.isEmpty()) {
					Main.crtSbdTm(sb, "tm" + String.valueOf(i-6), "", tm.name, "§7 : §4уничтожена");
					ob.getScore(tm.name)
					.setScore(i); i--;
				} else {
					Main.crtSbdTm(sb, "tm" + String.valueOf(i-6), "", tm.name, "§7 : " + "§a" + tm.pls.size() + "§7 чел.");
					ob.getScore(tm.name)
					.setScore(i); i--;
				}
			} else {
				if (tm.pls.isEmpty()) {
					Main.crtSbdTm(sb, "tm" + String.valueOf(i-6), "", tm.name, "§7 : §4уничтожена");
					ob.getScore(tm.name)
					.setScore(i); i--;
				} else {
					Main.crtSbdTm(sb, "tm" + String.valueOf(i-6), "", tm.name, "§7 : " + "§2" + tm.rsps + "§7 возр.");
					ob.getScore(tm.name)
					.setScore(i); i--;
				}
			}
		}
		ob.getScore("   ").setScore(i); i--;
		Main.crtSbdTm(sb, "cns", "", "§7Монет:", " §6" + String.valueOf(Bukkit.getPlayer(name).getMetadata("cns").get(0).asShort()));
		ob.getScore("§7Монет:")
		.setScore(i); i--;
		ob.getScore("  ").setScore(i); i--;
		Main.crtSbdTm(sb, "time", "", "§7Ничья через", time % 60 < 10 ? 
			" §2" + String.valueOf((int) (((double) time) / 60.0) + "§7:§20" + (time % 60))
			:
			" §2" + String.valueOf((int) (((double) time) / 60.0) + "§7:§2" + (time % 60)));
		ob.getScore("§7Ничья через")
			.setScore(i); i--;
		ob.getScore(" ").setScore(i); i--;
		
		ob.getScore("§e       ostrov77.su")
			.setScore(i);
		Bukkit.getPlayer(name).setScoreboard(sb);
	}
	
	public void endScore(final String name, final String wntm) {
		final Scoreboard sb = smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("Поле Брани", "", "§7[§2Поле Брани§7]");
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		ob.getScore("§7Карта: §6" + getName())
			.setScore(7);
		ob.getScore("   ")
			.setScore(6);
		ob.getScore("§7Игра окончена,")
			.setScore(5);
		ob.getScore(wntm.isEmpty() ? "§7Результат: Ничья!" : "§7Выйграла " + wntm + "§7 комманда!")
			.setScore(4);
		ob.getScore("  ")
			.setScore(3);
		Main.crtSbdTm(sb, "time", "", "§7Окончание через", " §2" + String.valueOf(time));
		ob.getScore("§7Окончание через")
			.setScore(2);
		ob.getScore(" ")
			.setScore(1);
		
		ob.getScore("§e       ostrov77.su")
			.setScore(0);
		Bukkit.getPlayer(name).setScoreboard(sb);
	}
	
	public void updTmsSb() {
		if (state == GameState.RUNNING) {
			byte i = (byte) (tms.length - 1);
			for (final Team tm : tms) {
				if (tm.rsps == 0) {
					if (tm.pls.isEmpty()) {
						for (String s : pls) {
							Main.chgSbdTm(Bukkit.getPlayer(s).getScoreboard(), "tm" + String.valueOf(i), "", "§7 : §4уничтожена");
						}
					} else {
						for (String s : pls) {
							Main.chgSbdTm(Bukkit.getPlayer(s).getScoreboard(), "tm" + String.valueOf(i), "", "§7 : " + "§a" + tm.pls.size() + "§7 чел.");
						}
					}
				} else { 
					if (tm.pls.isEmpty()) {
						for (String s : pls) {
							Main.chgSbdTm(Bukkit.getPlayer(s).getScoreboard(), "tm" + String.valueOf(i), "", "§7 : §4уничтожена");
						}
					} else {
						for (String s : pls) {
							Main.chgSbdTm(Bukkit.getPlayer(s).getScoreboard(), "tm" + String.valueOf(i), "", "§7 : " + "§2" + tm.rsps + "§7 возр.");
						}
					}
				}
				i--;
			}
		}
	}
}
