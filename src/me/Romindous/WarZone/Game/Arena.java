package me.Romindous.WarZone.Game;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import me.Romindous.WarZone.Main;
import me.Romindous.WarZone.Listeners.MainLis;
import me.Romindous.WarZone.Utils.EntMeta;
import me.Romindous.WarZone.Utils.Inventories;
import me.Romindous.WarZone.Utils.TitleManager;
import me.Romindous.WarZone.Utils.Translates;
import net.kyori.adventure.text.Component;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.BaseBlockPosition;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Stat;
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
	private final BaseBlockPosition[] shSpwns;
	private final HashMap<Character, BaseBlockPosition[]> tmBlks;
	private BukkitTask task;
	private BukkitTask mbspwm;
	private final ScoreboardManager smg;
	
	public Arena(final Setup stp) {
		//подготвка карты
		this.time = 0;
		this.max = stp.max;
		this.min = stp.min;
		this.name = stp.nm;
		final World w = Bukkit.getWorld(stp.wnm);
		this.cntr = new Location(w, stp.cntr.u() + 0.5d, stp.cntr.v() + 0.1d, stp.cntr.w() + 0.5d);
		this.tmBlks = new HashMap<Character, BaseBlockPosition[]>();
		this.spcs = new HashSet<String>();
		this.pls = new HashSet<String>();
		this.state = GameState.LOBBY_WAIT;
		this.smg = Bukkit.getScoreboardManager();
		//ископаемые блоки, ресурсы, и покупаемые блоки
		this.mnbls = stp.mnbls;
		Bukkit.getConsoleSender().sendMessage("mn-" + Arrays.toString(mnbls));
		this.recs = stp.recs;
		Bukkit.getConsoleSender().sendMessage("rs-" + Arrays.toString(recs));
		this.tl = stp.tl;
		//комманды на карте
		int i = stp.tmSpawns.length;
		this.tms = new Team[i];
		//при создании проверяем если комманда с таким цветом уже есть
		final LinkedList<Character> ccs = new LinkedList<>();
		final ChatColor[] cs = ChatColor.values();
		//Bukkit.getConsoleSender().sendMessage("cs-" + Arrays.toString(cs));
		for (int j = 0; j < 15; j++) {
			ccs.add(cs[j].getChar());
		}
		
		Collections.shuffle(ccs, Main.srnd);
		for (i--; i >= 0; i--) {
			final Character cc = ccs.pop();
			//Bukkit.getConsoleSender().sendMessage("cc-" + cc);
			final BaseBlockPosition tp = stp.tmSpawns[i];
			tms[i] = new Team(Translates.transClr(cc), 
					new Location(cntr.getWorld(), tp.u(), tp.v(), tp.w()));
			//асинхронная замена цвета блоков
			tmClrs(tms[i].spwn, cc);
			//--
		}
		//--
		//магазины на карте
		this.shps = new UUID[stp.tmShops.length];
		this.shSpwns = stp.tmShops;
		//--
	}
	//асинхронная замена на цвета блоков
	public void tmClrs(final Location loc, final char cc) {
		//final IServer is = VM.getNmsServer();
		final World w = loc.getWorld();
		final int tx = loc.getBlockX();
		final int ty = loc.getBlockY();
		final int tz = loc.getBlockZ();
		Bukkit.getConsoleSender().sendMessage("org-" + tx + ", " + ty + ", " + tz);
		Ostrov.async(() -> {
			final LinkedList<BaseBlockPosition> tbs = new LinkedList<>();
			for (int x = -Main.clrRng; x < Main.clrRng; x++) {
				for (int y = -Main.clrRng; y < Main.clrRng; y++) {
					for (int z = -Main.clrRng; z < Main.clrRng; z++) {
						switch (w.getType(tx + x, ty + y, tz + z)) {
						case BLACK_CONCRETE, BLUE_CONCRETE, BROWN_CONCRETE, CYAN_CONCRETE, 
						GRAY_CONCRETE, GREEN_CONCRETE, LIGHT_BLUE_CONCRETE, LIGHT_GRAY_CONCRETE, 
						LIME_CONCRETE, MAGENTA_CONCRETE, YELLOW_CONCRETE, WHITE_CONCRETE, 
						RED_CONCRETE, PURPLE_CONCRETE, PINK_CONCRETE, ORANGE_CONCRETE, 
						
						BLACK_CONCRETE_POWDER, BLUE_CONCRETE_POWDER, BROWN_CONCRETE_POWDER, CYAN_CONCRETE_POWDER, 
						GRAY_CONCRETE_POWDER, GREEN_CONCRETE_POWDER, LIGHT_BLUE_CONCRETE_POWDER, LIGHT_GRAY_CONCRETE_POWDER, 
						LIME_CONCRETE_POWDER, MAGENTA_CONCRETE_POWDER, YELLOW_CONCRETE_POWDER, WHITE_CONCRETE_POWDER, 
						RED_CONCRETE_POWDER, PURPLE_CONCRETE_POWDER, PINK_CONCRETE_POWDER, ORANGE_CONCRETE_POWDER, 
						
						BLACK_STAINED_GLASS_PANE, BLUE_STAINED_GLASS_PANE, BROWN_STAINED_GLASS_PANE, CYAN_STAINED_GLASS_PANE, 
						GRAY_STAINED_GLASS_PANE, GREEN_STAINED_GLASS_PANE, LIGHT_BLUE_STAINED_GLASS_PANE, LIGHT_GRAY_STAINED_GLASS_PANE, 
						LIME_STAINED_GLASS_PANE, MAGENTA_STAINED_GLASS_PANE, YELLOW_STAINED_GLASS_PANE, WHITE_STAINED_GLASS_PANE, 
						RED_STAINED_GLASS_PANE, PURPLE_STAINED_GLASS_PANE, PINK_STAINED_GLASS_PANE, ORANGE_STAINED_GLASS_PANE, 
						
						BLACK_STAINED_GLASS, BLUE_STAINED_GLASS, BROWN_STAINED_GLASS, CYAN_STAINED_GLASS, 
						GRAY_STAINED_GLASS, GREEN_STAINED_GLASS, LIGHT_BLUE_STAINED_GLASS, LIGHT_GRAY_STAINED_GLASS, 
						LIME_STAINED_GLASS, MAGENTA_STAINED_GLASS, YELLOW_STAINED_GLASS, WHITE_STAINED_GLASS, 
						RED_STAINED_GLASS, PURPLE_STAINED_GLASS, PINK_STAINED_GLASS, ORANGE_STAINED_GLASS, 
						
						BLACK_WOOL, BLUE_WOOL, BROWN_WOOL, CYAN_WOOL, 
						GRAY_WOOL, GREEN_WOOL, LIGHT_BLUE_WOOL, LIGHT_GRAY_WOOL, 
						LIME_WOOL, MAGENTA_WOOL, YELLOW_WOOL, WHITE_WOOL, 
						RED_WOOL, PURPLE_WOOL, PINK_WOOL, ORANGE_WOOL, 
						
						BLACK_TERRACOTTA, BLUE_TERRACOTTA, BROWN_TERRACOTTA, CYAN_TERRACOTTA, 
						GRAY_TERRACOTTA, GREEN_TERRACOTTA, LIGHT_BLUE_TERRACOTTA, LIGHT_GRAY_TERRACOTTA, 
						LIME_TERRACOTTA, MAGENTA_TERRACOTTA, YELLOW_TERRACOTTA, WHITE_TERRACOTTA, 
						RED_TERRACOTTA, PURPLE_TERRACOTTA, PINK_TERRACOTTA, ORANGE_TERRACOTTA, 
						
						BLACK_CANDLE, BLUE_CANDLE, BROWN_CANDLE, CYAN_CANDLE, 
						GRAY_CANDLE, GREEN_CANDLE, LIGHT_BLUE_CANDLE, LIGHT_GRAY_CANDLE, 
						LIME_CANDLE, MAGENTA_CANDLE, YELLOW_CANDLE, WHITE_CANDLE, 
						RED_CANDLE, PURPLE_CANDLE, PINK_CANDLE, ORANGE_CANDLE:
							tbs.add(new BaseBlockPosition(tx + x, ty + y, tz + z));
							break;
						default:
							break;
						}
					}
				}
			}

			Ostrov.sync(() -> {
				if (Main.activearenas.containsKey(name)) {
					tmBlks.put(cc, tbs.toArray(new BaseBlockPosition[0]));
					final String bcl = Translates.mtFromCC(cc);
					for (final BaseBlockPosition b : tbs) {
						final Block bl = w.getBlockAt(b.u(), b.v(), b.w());
						final String add;
						switch (bl.getType()) {
						case BLACK_CONCRETE, BLUE_CONCRETE, BROWN_CONCRETE, CYAN_CONCRETE, 
						GRAY_CONCRETE, GREEN_CONCRETE, LIGHT_BLUE_CONCRETE, LIGHT_GRAY_CONCRETE, 
						LIME_CONCRETE, MAGENTA_CONCRETE, YELLOW_CONCRETE, WHITE_CONCRETE, 
						RED_CONCRETE, PURPLE_CONCRETE, PINK_CONCRETE, ORANGE_CONCRETE:
							add = "_CONCRETE";
							break;
						case BLACK_CONCRETE_POWDER, BLUE_CONCRETE_POWDER, BROWN_CONCRETE_POWDER, CYAN_CONCRETE_POWDER, 
						GRAY_CONCRETE_POWDER, GREEN_CONCRETE_POWDER, LIGHT_BLUE_CONCRETE_POWDER, LIGHT_GRAY_CONCRETE_POWDER, 
						LIME_CONCRETE_POWDER, MAGENTA_CONCRETE_POWDER, YELLOW_CONCRETE_POWDER, WHITE_CONCRETE_POWDER, 
						RED_CONCRETE_POWDER, PURPLE_CONCRETE_POWDER, PINK_CONCRETE_POWDER, ORANGE_CONCRETE_POWDER:
							add = "_CONCRETE_POWDER";
							break;
						case BLACK_STAINED_GLASS_PANE, BLUE_STAINED_GLASS_PANE, BROWN_STAINED_GLASS_PANE, CYAN_STAINED_GLASS_PANE, 
						GRAY_STAINED_GLASS_PANE, GREEN_STAINED_GLASS_PANE, LIGHT_BLUE_STAINED_GLASS_PANE, LIGHT_GRAY_STAINED_GLASS_PANE, 
						LIME_STAINED_GLASS_PANE, MAGENTA_STAINED_GLASS_PANE, YELLOW_STAINED_GLASS_PANE, WHITE_STAINED_GLASS_PANE, 
						RED_STAINED_GLASS_PANE, PURPLE_STAINED_GLASS_PANE, PINK_STAINED_GLASS_PANE, ORANGE_STAINED_GLASS_PANE:
							add = "_STAINED_GLASS_PANE";
							break;
						case BLACK_STAINED_GLASS, BLUE_STAINED_GLASS, BROWN_STAINED_GLASS, CYAN_STAINED_GLASS, 
						GRAY_STAINED_GLASS, GREEN_STAINED_GLASS, LIGHT_BLUE_STAINED_GLASS, LIGHT_GRAY_STAINED_GLASS, 
						LIME_STAINED_GLASS, MAGENTA_STAINED_GLASS, YELLOW_STAINED_GLASS, WHITE_STAINED_GLASS, 
						RED_STAINED_GLASS, PURPLE_STAINED_GLASS, PINK_STAINED_GLASS, ORANGE_STAINED_GLASS:
							add = "_STAINED_GLASS";
							break;
						case BLACK_WOOL, BLUE_WOOL, BROWN_WOOL, CYAN_WOOL, 
						GRAY_WOOL, GREEN_WOOL, LIGHT_BLUE_WOOL, LIGHT_GRAY_WOOL, 
						LIME_WOOL, MAGENTA_WOOL, YELLOW_WOOL, WHITE_WOOL, 
						RED_WOOL, PURPLE_WOOL, PINK_WOOL, ORANGE_WOOL:
							add = "_WOOL";
							break;
						case BLACK_TERRACOTTA, BLUE_TERRACOTTA, BROWN_TERRACOTTA, CYAN_TERRACOTTA, 
						GRAY_TERRACOTTA, GREEN_TERRACOTTA, LIGHT_BLUE_TERRACOTTA, LIGHT_GRAY_TERRACOTTA, 
						LIME_TERRACOTTA, MAGENTA_TERRACOTTA, YELLOW_TERRACOTTA, WHITE_TERRACOTTA, 
						RED_TERRACOTTA, PURPLE_TERRACOTTA, PINK_TERRACOTTA, ORANGE_TERRACOTTA:
							add = "_TERRACOTTA";
							break;
						case BLACK_CANDLE, BLUE_CANDLE, BROWN_CANDLE, CYAN_CANDLE, 
						GRAY_CANDLE, GREEN_CANDLE, LIGHT_BLUE_CANDLE, LIGHT_GRAY_CANDLE, 
						LIME_CANDLE, MAGENTA_CANDLE, YELLOW_CANDLE, WHITE_CANDLE, 
						RED_CANDLE, PURPLE_CANDLE, PINK_CANDLE, ORANGE_CANDLE:
							add = "_CANDLE";
							break;
						default:
							continue;
						}
						final Material mat = Material.getMaterial(bcl + add);
						if (mat == null) {
							Bukkit.getConsoleSender().sendMessage("type-" + bcl + bl.getType().toString().substring(5));
						} else {
							bl.setType(mat, false);
						}
					}
				}
			});
		});
	}
	//--
	//асинхронная замена на белый цвет
	public void whtClrs(final char ch) {
		final BaseBlockPosition[] cbps = tmBlks.remove(ch);
		if (cbps == null) {
			return;
		}
		
		for (final BaseBlockPosition bp : cbps) {
			final Block b = cntr.getWorld().getBlockAt(bp.u(), bp.v(), bp.w());
			final String tp = b.getType().toString();
			b.setType(Material.getMaterial("WHITE" + tp.substring(tp.indexOf('_', tp.startsWith("LIG") ? 8 : 0))));
		}
	}
	//--
	//получение арены по игроку
	public static Arena getPlArena(final String name) {
		for (final Arena ar : Main.activearenas.values()) {
			if (ar.getPls().contains(name)) {
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
		ApiOstrov.addStat(Bukkit.getPlayer(name), Stat.WZ_dths);
		ApiOstrov.addStat(Bukkit.getPlayer(name), Stat.WZ_game);
		ApiOstrov.addStat(Bukkit.getPlayer(name), Stat.WZ_loose);
		//--
		//инфа
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ИГРА, "§7[§2Поле Брани§7]", "§cИдет Игра", " ", "§7Игроков: " + pls.size(), "", pls.size());
		for (final String s : pls) {
			Bukkit.getPlayer(s).sendMessage(Main.prf() + dmsg);
		}
		//таб
		for (final Player pl : Bukkit.getOnlinePlayers()) {
			pl.sendPlayerListFooter(Component.text("§7Сейчас в игре: §2" + MainLis.getPlaying() + "§7 человек!"));
		}
		//если осталась только 1 комманда
		if (oneTmLft() != null) {
			task.cancel();
			countEnd(Main.srnd, oneTmLft().name);
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
		final Player p = Bukkit.getPlayer(name);
		Main.lobbyPlayer(p);
		switch (getState()) {
		case LOBBY_WAIT:
			//убираем игрока
			pls.remove(name);
			for (final Team tm : tms) {
				tm.pls.remove(name);
			}
			//таб
			for (final Player pl : Bukkit.getOnlinePlayers()) {
				pl.sendPlayerListFooter(Component.text("§7Сейчас в игре: §2" + MainLis.getPlaying() + "§7 человек!"));
			}
			//инфа
			p.sendMessage(Main.prf() + "Вы покинули карту §2" + getName());
			for (final String s : pls) {
				TitleManager.sendAcBr(Bukkit.getPlayer(s), amtToHB(), 30);
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
			ApiOstrov.addStat(p, Stat.WZ_loose);
			//--
			//таб
			for (final Player pl : Bukkit.getOnlinePlayers()) {
				pl.sendPlayerListFooter(Component.text("§7Сейчас в игре: §2" + MainLis.getPlaying() + "§7 человек!"));
			}
			//если осталась только 1 комманда
			if (oneTmLft() != null) {
				task.cancel();
				countEnd(Main.srnd, oneTmLft().name);
			} else {
				updTmsSb();
			}
			break;
		case END:
			p.sendMessage(Main.prf() + "Вы покинули игру §2" + getName());
			//убираем из списка
			pls.remove(name);
			for (final Team tm : tms) {
				tm.pls.remove(name);
			}
			//таб
			for (final Player pl : Bukkit.getOnlinePlayers()) {
				pl.sendPlayerListFooter(Component.text("§7Сейчас в игре: §2" + MainLis.getPlaying() + "§7 человек!"));
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
				pl.sendPlayerListFooter(Component.text("§7Сейчас в игре: §2" + MainLis.getPlaying() + "§7 человек!"));
			}
			p.sendMessage(Main.prf() + "Вы зашли на карту §2" + getName());
			final String prm = Main.getTopPerm(PM.getOplayer(p));
			//неймтег
			TitleManager.sendNmTg(p.getName(), "§7[§6" + getName() + "§7] ", (prm.isEmpty() ? "" : " §7(§e" + prm + "§7)"), EnumChatFormat.c);
	        //таб
			p.playerListName(Component.text("§7[§6" + getName() + "§7] " + name + (prm.length() > 1 ? " §7(§e" + prm + "§7)" : "")));
			//инфа
			for (final String s : pls) {
				TitleManager.sendAcBr(Bukkit.getPlayer(s), amtToHB(), 30);
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
		final String prm = Main.getTopPerm(PM.getOplayer(p));
		//неймтег
		TitleManager.sendNmTg(p.getName(), "§7[§6" + getName() + "§7] ", (prm.isEmpty() ? "" : " §7(§e" + prm + "§7)"), EnumChatFormat.a(tm.name.charAt(1)));
		//таб
		p.playerListName(Component.text("§7[§6" + getName() + "§7] " + tm.name.substring(0, 2) + p.getName() + (prm.length() > 1 ? " §7(§e" + prm + "§7)" : "")));
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
						TitleManager.sendAcBr(Bukkit.getPlayer(s), "§7До начала осталось §2" + time + " §7секунд!", 30);
					}
					break;
				case 4:
				case 3:
				case 2:
				case 1:
					for (final String s : pls) {
						TitleManager.sendSbTtl(Bukkit.getPlayer(s), "§6" + time, 10);
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
			p.playerListName(Component.text("§7[" + getPlTeam(s).name + "§7] " + s));
			//неймтег
			TitleManager.sendNmTg(s, "", "", EnumChatFormat.a(getPlTeam(s).name.charAt(1)));
			//обовляем игрока
			Main.nrmlzPl(p);
			p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 1000000, 0, true, false));
			p.getInventory().clear();
			//даем инструмент
			p.getInventory().addItem(Inventories.mkItm(Material.getMaterial("STONE" + tl), "§6Первый Инструмент", true));
			//мета
			EntMeta.chngMeta(p, "kls", (byte) 0);
			//инфа
			TitleManager.sendTtlSbTtl(p, "§2Начинаем!", "§7Собирайте §2ресурсы §7и громите чужие §2комманды§7!" + time, 50);
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
		//магазины
		for (byte i = (byte) (shps.length - 1); i >= 0; i--) {
			final BaseBlockPosition bp = shSpwns[i];
			final ZombieVillager vll = (ZombieVillager) cntr.getWorld().spawnEntity(new Location(cntr.getWorld(), 
				bp.u() + 0.5d, bp.v() + 0.1d, bp.w() + 0.5d), EntityType.ZOMBIE_VILLAGER);
			vll.setVillagerType(Translates.getBmVllTp(vll.getLocation().getBlock().getBiome()));
			vll.setVillagerProfession(Profession.CARTOGRAPHER);
			vll.setTicksLived(1);
			vll.setAdult();
			vll.customName(Component.text("§6§lМагазин"));
			vll.setInvulnerable(true);
			vll.setPersistent(true);
			vll.setRemoveWhenFarAway(false);
			Bukkit.getMobGoals().removeAllGoals(vll);
			Bukkit.getMobGoals().addGoal(vll, 0, new GoalLookAtPl(vll));
			shps[i] = vll.getUniqueId();
		}
		//спавним мобов
		mbspwm = new MnstrRun(cntr, (byte) cntr.distance(tms[0].spwn)).runTaskTimer(Main.plug, 0, Main.mbPrd << 4);
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ИГРА, "§7[§2Поле Брани§7]", "§cИдет Игра", " ", "§7Игроков: " + pls.size(), "", pls.size());
		task = new BukkitRunnable() {
			@Override
			public void run() {
				//scoreboard stuff
				for (final String s : pls) {
					if (Bukkit.getPlayer(s) == null) {
						//убираем из списка
						pls.remove(s);
						//убираем игрока
						for (final Team tm : tms) {
							tm.pls.remove(s);
						}
						//записываем проигрыш
						ApiOstrov.addStat(Bukkit.getPlayer(s), Stat.WZ_game);
						ApiOstrov.addStat(Bukkit.getPlayer(s), Stat.WZ_loose);
						//таб
						for (final Player pl : Bukkit.getOnlinePlayers()) {
							pl.sendPlayerListFooter(Component.text("§7Сейчас в игре: §2" + MainLis.getPlaying() + "§7 человек!"));
						}
						//если осталась только 1 комманда
						if (oneTmLft() != null) {
							task.cancel();
							countEnd(Main.srnd, oneTmLft().name);
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
						TitleManager.sendAcBr(Bukkit.getPlayer(s), "§7До конца осталось §25 §7минут!", 30);
					}
				case 60:
					for (final String s : pls) {
						TitleManager.sendAcBr(Bukkit.getPlayer(s), "§7Конец игры через §21 §7минуту!", 30);
					}
					break;
				case 30:
				case 10:
					for (final String s : pls) {
						TitleManager.sendAcBr(Bukkit.getPlayer(s), "§7Осталось §2" + time + "§7секунд!", 30);
					}
					break;
				case 5:
				case 4:
				case 3:
				case 2:
				case 1:
					for (final String s : pls) {
						TitleManager.sendTtl(Bukkit.getPlayer(s), "§6" + time, 10);
						Bukkit.getPlayer(s).playSound(Bukkit.getPlayer(s).getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
					}
					break;
				case 0:
					task.cancel();
					countEnd(Main.srnd, "");
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
		//заменяем цвета на белый
		for (final Team tm : tms) {
			whtClrs(tm.getName().charAt(1));
		}
		//записываем победу
		if (wntm.isEmpty()) {
			for (final String s : pls) {
				Bukkit.getPlayer(s).sendMessage(Main.prf() + "Результат: Ничья!");
				ApiOstrov.addStat(Bukkit.getPlayer(s), Stat.WZ_game);
				endScore(s, wntm);
			}
			for (final String s : spcs) {
				final Player p = Bukkit.getPlayer(s);
				if (p != null) {
					p.sendMessage(Main.prf() + "Результат: Ничья!");
				}
			}
		} else {
			for (final String s : pls) {
				final Player p = Bukkit.getPlayer(s);
				p.sendMessage(Main.prf() + wntm + "§7 комманда победила в этой битве!");
				p.sendMessage("§7-=-=-=-=-=-=-=-=-=-=-=-=-=-");
				for (final String wn : pls) {
					p.sendMessage(wntm.substring(0, 2) + wn + "§7 - §2" + Bukkit.getPlayer(wn).getMetadata("kls").get(0).asByte() + "§7 убийств");
				}
				p.sendMessage("§7-=-=-=-=-=-=-=-=-=-=-=-=-=-");
				ApiOstrov.addStat(p, Stat.WZ_game);
				ApiOstrov.addStat(p, Stat.WZ_win);
				endScore(s, wntm);
			}
			for (final String s : spcs) {
				final Player p = Bukkit.getPlayer(s);
				if (p != null) {
					p.sendMessage(Main.prf() + wntm + "§7 комманда победила в этой битве!");
					p.sendMessage("§7-=-=-=-=-=-=-=-=-=-=-=-=-=-");
					for (final String wn : pls) {
						p.sendMessage(wntm.substring(0, 2) + wn + "§7 - §2" + Bukkit.getPlayer(wn).getMetadata("kls").get(0).asByte() + "§7 убийств");
					}
					p.sendMessage("§7-=-=-=-=-=-=-=-=-=-=-=-=-=-");
				}
			}
		}
		//--
		task = new BukkitRunnable() {
		
			@Override
			public void run() {
				switch (time) {
				case 0:
					for (final String s : pls) {
						//ApiOstrov.moneyChange(Bukkit.getPlayer(s), 10 + (Bukkit.getPlayer(s).getMetadata("kls").get(0).asByte() * 5), "§2Победа");
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
		final Player p = Bukkit.getPlayer(name);
		final Scoreboard sb = smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("Поле Брани", Criteria.DUMMY, Component.text("§7[§2Поле Брани§7]"));
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
		ob.getScore("§2" + String.valueOf(ApiOstrov.getStat(p, Stat.WZ_klls)) + " §7/ (§2" + String.valueOf(ApiOstrov.getStat(p, Stat.WZ_dths)) + "§7) = §2" 
			+ Main.getStrFlt((float) ApiOstrov.getStat(p, Stat.WZ_klls) / (float) (ApiOstrov.getStat(p, Stat.WZ_dths) == 0 ? 1 : ApiOstrov.getStat(p, Stat.WZ_dths))))
			.setScore(5);
		ob.getScore("  ").setScore(4);
		ob.getScore("§7Победы / Проигрыши: ")
			.setScore(3);
		ob.getScore("§2" + String.valueOf(ApiOstrov.getStat(p, Stat.WZ_win)) + " §7/§2 " + String.valueOf(ApiOstrov.getStat(p, Stat.WZ_loose)) + " §7=§2 " 
				+ Main.getStrFlt((float) ApiOstrov.getStat(p, Stat.WZ_win) / (float) (ApiOstrov.getStat(p, Stat.WZ_loose) == 0 ? 1 : ApiOstrov.getStat(p, Stat.WZ_loose))))
			.setScore(2);
		ob.getScore(" ").setScore(1);
		
		ob.getScore("§e      ostrov77.su")
			.setScore(0);
		p.setScoreboard(sb);
	}
	
	public void lobbyScore(final String name) {
		final Player p = Bukkit.getPlayer(name);
		final Scoreboard sb = smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("Поле Брани", Criteria.DUMMY, Component.text("§7[§2Поле Брани§7]"));
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
		ob.getScore("§2" + String.valueOf(ApiOstrov.getStat(p, Stat.WZ_klls)) + " §7/ (§2" + String.valueOf(ApiOstrov.getStat(p, Stat.WZ_dths)) + "§7) = §2" 
			+ Main.getStrFlt((float) ApiOstrov.getStat(p, Stat.WZ_klls) / (float) (ApiOstrov.getStat(p, Stat.WZ_dths) == 0 ? 1 : ApiOstrov.getStat(p, Stat.WZ_dths))))
			.setScore(5);
		ob.getScore("  ").setScore(4);
		ob.getScore("§7Победы / Проигрыши: ")
			.setScore(3);
		ob.getScore("§2" + String.valueOf(ApiOstrov.getStat(p, Stat.WZ_win)) + " §7/§2 " + String.valueOf(ApiOstrov.getStat(p, Stat.WZ_loose)) + " §7=§2 " 
				+ Main.getStrFlt((float) ApiOstrov.getStat(p, Stat.WZ_win) / (float) (ApiOstrov.getStat(p, Stat.WZ_loose) == 0 ? 1 : ApiOstrov.getStat(p, Stat.WZ_loose))))
			.setScore(2);
		ob.getScore(" ").setScore(1);
		
		ob.getScore("§e       ostrov77.su")
			.setScore(0);
		p.setScoreboard(sb);
	}
	
	public void runnScore(final String name) {
		final Scoreboard sb = smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("Поле Брани", Criteria.DUMMY, Component.text("§7[§2Поле Брани§7]"));
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
		final Objective ob = sb.registerNewObjective("Поле Брани", Criteria.DUMMY, Component.text("§7[§2Поле Брани§7]"));
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

	public static String getByNum(int used) {
		for (final String a : Main.nonactivearenas.keySet()) {
			if ((used--) == 0) {
				return a;
			}
		}
		
		return null;
	}
	
	public void stopMobSpawn() {
		mbspwm.cancel();
	}
}
