package ru.romindous.wz.Game;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.scoreboard.SideBar;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.ScreenUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.TimeUtil;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.komiss77.version.Nms;
import ru.romindous.wz.Listeners.MainLis;
import ru.romindous.wz.Main;
import ru.romindous.wz.Menus.TeamMenu;
import ru.romindous.wz.Utils.Inventories;
import ru.romindous.wz.Utils.Priced;

import javax.annotation.Nullable;
import java.util.*;

public class Arena {
	private GameState state;
	private final String name;
	private final String tool;
	private final byte min;
	private final byte max;
	private final int tmDst;
	public final Mob[] shps;
	public final Material[] mnbls;
	public final Material[] recs;
	private int time;
	private final Team[] tms;
	private final WXYZ cntr;
	private final HashMap<String, PlWarrior> spcs;
	private final HashMap<String, PlWarrior> pls;
	private final XYZ[] shSpwns;
	private final HashSet<Priced> tmBlks;
	private BukkitTask task;
	private BukkitTask mbspwm;
	public final SmartInventory teamInv;

	public static final EntityType SHOP_TYPE = EntityType.ZOMBIE_VILLAGER;
	public static final String MONEY = "cns", TEAM = "tm", AMT = "amt", LIMIT = "rem", LVL = "lvl",
		FIRST = "§a", SECOND = "§3", THIRD = "§2";
	public static final NamedTextColor[] colors = {NamedTextColor.DARK_GREEN, NamedTextColor.AQUA, NamedTextColor.BLUE,
		NamedTextColor.DARK_AQUA, NamedTextColor.DARK_GRAY, NamedTextColor.DARK_PURPLE, NamedTextColor.DARK_RED, NamedTextColor.GOLD,
		NamedTextColor.GREEN, NamedTextColor.LIGHT_PURPLE, NamedTextColor.RED, NamedTextColor.YELLOW};
//	private static final PlWarrior[] eps = new PlWarrior[0];
	
	public Arena(final Setup stp) {
		//подготвка карты
		this.time = 0;
		this.max = stp.max;
		this.min = stp.min;
		this.name = stp.nm;
		this.cntr = new WXYZ(stp.cntr);
		this.tmBlks = new HashSet<>();
		this.spcs = new HashMap<>();
		this.pls = new HashMap<>();
		this.state = GameState.WAITING;
		this.teamInv = SmartInventory.builder().size(3).id("Team " + name)
			.title(TCUtil.P + "Выбор Комманды").provider(new TeamMenu()).build();
		//ископаемые блоки, ресурсы, и покупаемые блоки
		this.mnbls = stp.mnbls;
		Bukkit.getConsoleSender().sendMessage("mn-" + Arrays.toString(mnbls));
		this.recs = stp.recs;
		Bukkit.getConsoleSender().sendMessage("rs-" + Arrays.toString(recs));
		this.tool = stp.tl;
		//комманды на карте
		int i = stp.tmSpawns.length;
		this.tms = new Team[i];
		//при создании проверяем если комманда с таким цветом уже есть
		ClassUtil.shuffle(colors);

		int td = 0;
		for (i = 0; i != tms.length; i++) {
			//Bukkit.getConsoleSender().sendMessage("cc-" + cc);
			final XYZ tp = stp.tmSpawns[i];
			final WXYZ sp = new WXYZ(cntr.w, tp);
			tms[i] = new Team(colors[i], sp);
			td += cntr.distAbs(sp);
			//асинхронная замена цвета блоков
			tmClrs(tms[i].spwn, TCUtil.getDyeColor((TextColor) colors[i]));
			//--
		}

		tmDst = td / Math.max(tms.length, 1);
		//--
		//магазины на карте
		final Mob[] shs = new Mob[stp.tmShops.length];
		this.shSpwns = stp.tmShops;
		this.shps = shs;
		//--
	}

	//асинхронная замена на цвета блоков
	public void tmClrs(final WXYZ loc, final DyeColor dc) {
		final LinkedList<Priced> tbs = new LinkedList<>();
//		Ostrov.async(() -> {
			for (int x = -Main.clrRng; x != Main.clrRng; x++) {
				for (int y = -Main.clrRng; y != Main.clrRng; y++) {
					for (int z = -Main.clrRng; z != Main.clrRng; z++) {
						final WXYZ nlc = new WXYZ(loc.w, loc.x + x, loc.y + y, loc.z + z);
						switch (Nms.getFastMat(nlc)) {
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
							tbs.add(new Priced(nlc.x, nlc.y, nlc.z));
//							Bukkit.getConsoleSender().sendMessage("find-" + nlc.toString());
							break;
						default:
							break;
						}
					}
				}
			}

//			Ostrov.sync(() -> {
				final String bcl = dc.name();
				for (final Priced b : tbs) {
					final Block bl = loc.w.getBlockAt(b.fst(), b.scd(), b.thd());
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
						Bukkit.getConsoleSender().sendMessage("type-" + bcl + bl.getType().name());
					} else {
//						Bukkit.getConsoleSender().sendMessage("repl-" + mat.name() + " at " + b.toString());
						bl.setType(mat, false);
						tmBlks.add(b);
					}
				}
//			});
//		});
	}

	//асинхронная замена на белый цвет
	public void whtClrs() {
		for (final Priced bp : tmBlks) {
			final Block b = cntr.w.getBlockAt(bp.fst(), bp.scd(), bp.thd());
			final String tp = b.getType().toString();
			b.setType(Material.getMaterial("WHITE" + tp.substring(tp.indexOf('_', tp.startsWith("LIGHT") ? 8 : 0))), false);
		}
	}
	//--
	//получение арены по игроку
	public static Arena getPlArena(final HumanEntity p) {
		return PM.getOplayer(p, PlWarrior.class).arena();
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
	public @Nullable Team oneTmLft() {
		Team one = null;
		for (final Team tm : tms) {
			if (!tm.pls.isEmpty()) {
				if (one != null) return null;
				one = tm;
			}
		}
		return one;
	}
	//каждый игрок на арене
	public HashMap<String, PlWarrior> getPls() {
		return pls;
	}
	//спектаторы
	public HashMap<String, PlWarrior> getSpcs() {
		return spcs;
	}
	//комманды
	public Team[] getTms() {
		return tms;
	}
	//вещь для добычи ресурсов
	public String getTlSfx() {
		return tool;
	}
	//центр
	public WXYZ getCntr() {
		return cntr;
	}
	//время
	public int getTime() {
		return time;
	}
	
	public byte getMin() {
		return min;
	}
	
	public byte getMax() {
		return max;
	}
	
	public GameState getState() {
		return state;
	}

	public Arena getArena() {
		return this;
	}

	public String getName() {
		return name;
	}

	//сколько игроков из скольки
	public String amtToHB() {
		return pls.size() < min ?
			TCUtil.P + "На карте " + TCUtil.A + pls.size() + TCUtil.P + " игроков, нужно еще "
				+ TCUtil.A + (min - pls.size()) + TCUtil.P + " для начала" :
			TCUtil.P + "На карте " + TCUtil.A + pls.size() + TCUtil.P + " игроков, максимум: " + TCUtil.A + max;
	}

	public void killWar(final Player p, final PlWarrior pw, final @Nullable String klr) {
		final Team tm = pw.team();
		if (tm == null) return;
		pw.addStat(Stat.WZ_dths, 1);
		dropRecs(p.getInventory(), p.getLocation());
		final String dmsg;
		if (tm.rsps == 0) {
			dmsg = klr == null ? Main.PRFX + pw.team().color() + p.getName() + MainLis.snglDie() + TCUtil.N + ", и ушел в мир иной..."
				: Main.PRFX + pw.team().color() + p.getName() + MainLis.entDie() + klr + TCUtil.N + ", и ушел в мир иной...";
			//в спектатор
			Main.lobbyPlayer(p, pw);
			//убираем из списка
			pls.remove(p.getName());
			//инфа
			p.sendMessage(Main.PRFX + "Ты умер, а возрождений у вашей команды небыло... ГГ!");
			GM.sendArenaData(Game.WZ, this.name, ru.komiss77.enums.GameState.ИГРА, pls.size(), Main.PRFX,
				"§cИдет Игра", " ", TCUtil.N + "Игроков: " + pls.size());
			//кидаем в спектаторы
			addSpct(p, pw);
			//записываем проигрыш
			pw.addStat(Stat.WZ_game, 1);
			pw.addStat(Stat.WZ_loose, 1);
			//если осталась только 1 комманда
			final Team ltm = oneTmLft();
			if (ltm != null) {
				//инфа
				for (final PlWarrior plw : pls.values()) {
					plw.getPlayer().sendMessage(dmsg);
					plw.score.getSideBar().update(tm.color(), tm.desc(plw));
				}
				for (final PlWarrior plw : spcs.values()) {
					plw.getPlayer().sendMessage(dmsg);
				}
				//end
				if (task != null) task.cancel();
				countEnd(Main.srnd, ltm);
				return;
			}
		} else {
			tm.rsps--;
			dmsg = klr == null ? Main.PRFX + pw.team().color() + p.getName() + MainLis.snglDie() + TCUtil.N + ", теряя одно из возрождений!"
				: Main.PRFX + pw.team().color() + p.getName() + MainLis.entDie() + klr + TCUtil.N + ", теряя одно из возрождений!";
			p.teleport(tm.spwn.getCenterLoc());
			Main.nrmlzPl(p);
			p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 1000000, 0, true, false, false));
		}
		//инфа
		for (final PlWarrior plw : pls.values()) {
			plw.getPlayer().sendMessage(dmsg);
			plw.score.getSideBar().update(tm.color(), tm.desc(plw));
		}
		for (final PlWarrior plw : spcs.values()) {
			plw.getPlayer().sendMessage(dmsg);
		}
	}

	private void dropRecs(final PlayerInventory inv, final Location loc) {
		for (final ItemStack it : inv) {
			if (it != null) {
				for (final Material rc : recs) {
					if (it.getType() == rc) {
						loc.getWorld().dropItemNaturally(loc, it);
						it.setAmount(0);
					}
				}
			}
		}
	}
	
	public void addSpct(final Player p, final PlWarrior pw) {
		pw.arena(this);
		p.sendMessage(Main.PRFX + "Помещаем тебя в качестве зрителя!");
		p.getInventory().clear();
		p.getInventory().setItem(8, Inventories.mkItm(Material.REDSTONE, "§4Обратно в лобби", true));
		p.teleport(cntr.getCenterLoc());
		spcs.put(p.getName(), pw);
		p.setGameMode(GameMode.SPECTATOR);
		for (Player pl : Bukkit.getOnlinePlayers()) {
			final Arena oar = getPlArena(pl);
			if (oar == null || oar.getState() == GameState.WAITING) {
				p.hidePlayer(Main.plug, pl);
			}
		}
	}

	public void remSpct(final Player p) {
		final PlWarrior pw = spcs.remove(p.getName());
		if (pw == null) return;
		Main.lobbyPlayer(p, pw);
	}

	public void remPl(final Player p) {
		final PlWarrior pw = pls.remove(p.getName());
		if (pw == null) return;

		p.sendMessage(Main.PRFX + TCUtil.N + "Ты больше не на карте " + TCUtil.A + getName());
		switch (getState()) {
		case WAITING:
			if (pls.size() < min) {//если недостаточно игроков
				if (pls.size() == 0) Main.endArena(this);
				if (task != null) task.cancel();
				GM.sendArenaData(Game.WZ, this.name, ru.komiss77.enums.GameState.ОЖИДАНИЕ, pls.size(), Main.PRFX,
					"§2Ожидание", " ", TCUtil.N + "Игроков: §2" + pls.size() + TCUtil.N + "/§2" + min);
				for (final PlWarrior plw : pls.values()) {
					final Player pl = plw.getPlayer();
					ScreenUtil.sendActionBarDirect(pl, amtToHB());
					pl.sendMessage(Main.PRFX + TCUtil.P + p.getName() + TCUtil.N + " вышел с карты!");
					plw.score.getSideBar().update(AMT, TCUtil.N + "Игроков: " + TCUtil.P + pls.size() + " чел.")
							.update(LIMIT, TCUtil.N + "Нужно еще " + TCUtil.A + (min - pls.size()) + " чел.");
				}
			} else {
				GM.sendArenaData(Game.WZ, this.name, ru.komiss77.enums.GameState.СТАРТ, pls.size(), Main.PRFX,
					"§6Скоро старт!", " ", TCUtil.N + "Игроков: §6" + pls.size() + TCUtil.N + "/§6" + max);
				for (final PlWarrior plw : pls.values()) {
					final Player pl = plw.getPlayer();
					ScreenUtil.sendActionBarDirect(pl, amtToHB());
					pl.sendMessage(Main.PRFX + TCUtil.P + p.getName() + TCUtil.N + " вышел с карты!");
					plw.score.getSideBar().update(AMT, TCUtil.N + "Игроков: " + TCUtil.P + pls.size() + " чел.");
				}
			}
			break;
		case RUNNING:
			final Team tm = pw.team();
			final String CLR = tm == null ? "§r" : tm.color();
			for (final PlWarrior plw : pls.values()) {
				final Player pl = plw.getPlayer();
				pl.sendMessage(Main.PRFX + TCUtil.P + p.getName() + TCUtil.N + " вышел из игры!");
				plw.score.getSideBar().update(CLR, tm == null ? TCUtil.N + " null" : tm.desc(pw));
			}
			for (final PlWarrior plh : spcs.values()) {
				plh.getPlayer().sendMessage(Main.PRFX + TCUtil.P + p.getName() + TCUtil.N + " вышел из игры");
			}
			//записываем проигрыш
			pw.team(null, p);
			pw.addStat(Stat.WZ_loose, 1);
			//если осталась только 1 комманда
			final Team ltm = oneTmLft();
			if (ltm != null) {
				if (task != null) task.cancel();
				countEnd(Main.srnd, ltm);
			}
			break;
		case END:
			if (pls.size() == 0) {
				if (task != null) task.cancel();
				Main.endArena(this);
			}
			break;
		}
		Main.lobbyPlayer(p, pw);
	}

	//доавление игрока
	public void addPl(final Player p) {
		if (pls.size() < max) {
			final PlWarrior pw = PM.getOplayer(p, PlWarrior.class);
			switch (getState()) {
			case WAITING:
				p.setGameMode(GameMode.SURVIVAL);
				p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
				waitScore(pw);
				break;
			case RUNNING:
				addSpct(p, pw);
			case END:
				p.sendMessage(Main.PRFX + "§cНа карте " + TCUtil.P + getName() + " §cуже идет игра!");
				return;
			}
			pw.arena(this);
			pls.put(p.getName(), pw);
			final String prm = pw.getTopPerm();
			pw.taq(Main.bfr('[', TCUtil.A + getName(), ']'), TCUtil.P,
				(prm.isEmpty() ? "" : Main.afr('(', "§e" + prm, ')')));
			if (pls.size() < min) {
				for (final PlWarrior plh : pls.values()) {
					final Player pl = plh.getPlayer();
					ScreenUtil.sendActionBarDirect(pl, amtToHB());
					pl.sendMessage(Main.PRFX + TCUtil.P + p.getName() + TCUtil.N + " зашел на карту!");
					plh.score.getSideBar().update(AMT, TCUtil.N + "Игроков: " + TCUtil.P + pls.size() + " чел.")
						.update(LIMIT, TCUtil.N + "Нужно еще " + TCUtil.A + (min - pls.size()) + " чел.");
				}
			} else {
				for (final PlWarrior plh : pls.values()) {
					final Player pl = plh.getPlayer();
					ScreenUtil.sendActionBarDirect(pl, amtToHB());
					pl.sendMessage(Main.PRFX + TCUtil.P + p.getName() + TCUtil.N + " зашел на карту!");
					plh.score.getSideBar().update(AMT, TCUtil.N + "Игроков: " + TCUtil.P + pls.size() + " чел.");
				}
			}
			Main.nrmlzPl(p);
			Main.inGameCnt();
			p.getInventory().clear();
			p.getInventory().setItem(2, Inventories.mkItm(Material.END_CRYSTAL, "§eВыбор Комманды", true));
			p.getInventory().setItem(6, Inventories.mkItm(Material.SLIME_BALL, "§cВыход", true));
			if (pls.size() < min) {
				GM.sendArenaData(Game.WZ, this.name, ru.komiss77.enums.GameState.СТАРТ, pls.size(), Main.PRFX, "§6Скоро старт!", " ", TCUtil.N + "Игроков: §6" + pls.size() + TCUtil.N + "/§6" + max);
			} else {
				if (pls.size() == min) countLobby();
				GM.sendArenaData(Game.WZ, this.name, ru.komiss77.enums.GameState.ОЖИДАНИЕ, pls.size(), Main.PRFX, "§2Ожидание", " ", TCUtil.N + "Игроков: §2" + pls.size() + TCUtil.N + "/§2" + min);
			}
		} else {
			p.sendMessage(Main.PRFX + "§cКарта " + TCUtil.P + getName() + "§c заполнена!");
		}
	}

	//отсчет в лобби
	public void countLobby() {
		time = 30;
		for (final PlWarrior plw : pls.values()) {
			waitScore(plw);
		}
		task = new BukkitRunnable() {
			
			@Override
			public void run() {
				final String rmn = TCUtil.N + "До начала: " + TCUtil.P + time + " сек.";
				for (final PlWarrior plw : pls.values()) {
					plw.score.getSideBar().update(LIMIT, rmn);
				}
				switch (time) {
				case 20:
				case 10:
				case 5:
					for (final PlWarrior plw : pls.values()) {
						final Player pl = plw.getPlayer();
						pl.playSound(pl.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
						ScreenUtil.sendActionBarDirect(pl, TCUtil.N + "До начала осталось " + TCUtil.P + time + " §7секунд!");
					}
					break;
				case 4:
				case 3:
				case 2:
				case 1:
					for (final PlWarrior plw : pls.values()) {
						final Player pl = plw.getPlayer();
						ScreenUtil.sendTitleDirect(pl, TCUtil.A + time, "", 8, 20, 8);
						pl.playSound(pl.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
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
		//магазины
		for (int i = shps.length; i != 0; i--) {
			final XYZ bp = shSpwns[i - 1];
			final ZombieVillager vll = (ZombieVillager) cntr.w.spawnEntity(new Location(
				cntr.w, bp.x + 0.5d, bp.y + 0.1d, bp.z + 0.5d), SHOP_TYPE, CreatureSpawnEvent.SpawnReason.CUSTOM);
			vll.setVillagerType(getBmVllTp(vll.getLocation().getBlock().getBiome()));
			vll.setVillagerProfession(Profession.CARTOGRAPHER);
			vll.setAdult();
			vll.customName(TCUtil.form("§6§lМагазин"));
			vll.setInvulnerable(true);
			vll.setPersistent(true);
			vll.setRemoveWhenFarAway(false);
			Bukkit.getMobGoals().removeAllGoals(vll);
			Bukkit.getMobGoals().addGoal(vll, 0, new GoalLookAtPl(vll));
			shps[i - 1] = vll;
		}
		//игроки
		for (final PlWarrior plw : pls.values()) {
			final Player p = plw.getPlayer();
			//показ или скрывание игроков
			for (Player pl : Bukkit.getOnlinePlayers()) {
				final Arena oar = getPlArena(pl);
				if (oar == null || oar.getState() == GameState.WAITING) {
					p.hidePlayer(Main.plug, pl);
				}
			}
			//подставка в комманду
			if (plw.team() == null) plw.team(getMinTeam(), p);
			//тп игроков к комманду
			p.teleport(plw.team().spwn.getCenterLoc());
			//обовляем игрока
			Main.nrmlzPl(p);
			p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 1000000, 0, true, false));
			p.getInventory().clear();
			//даем инструмент
			p.getInventory().addItem(Inventories.mkItm(Material.getMaterial("STONE" + tool), "§6Первый Инструмент", true));
			//инфа
			ScreenUtil.sendTitleDirect(p, TCUtil.P + "Начинаем!", TCUtil.N + "Собирайте " + TCUtil.P + "ресурсы "
				+ TCUtil.N + "и громите чужие " + TCUtil.P + "комманды" + TCUtil.N + "!" + time, 20, 80, 8);
			p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 20, 1);
		}

		for (final PlWarrior plw : pls.values()) {
			//обовляем score
			runnScore(plw);
		}
		//спавним мобов
		mbspwm = new MonsterRun(this, tmDst).runTaskTimer(Main.plug, 0, Main.mbPrd * 20);
		GM.sendArenaData(Game.WZ, this.name, ru.komiss77.enums.GameState.ИГРА, pls.size(), Main.PRFX, "§cИдет Игра", " ", TCUtil.N + "Игроков: " + pls.size());
		task = new BukkitRunnable() {
			@Override
			public void run() {
				//scoreboard stuff
				final String rmn = TCUtil.P + TimeUtil.secondToTime(time);
				for (final PlWarrior plw : pls.values()) {
					plw.score.getSideBar().update(LIMIT, rmn);
				}
				switch (time) {
				case 300:
					for (final PlWarrior plw : pls.values()) {
						final Player pl = plw.getPlayer();
						ScreenUtil.sendActionBarDirect(pl, TCUtil.N + "До конца осталось " + TCUtil.P + "5 §7минут!");
					}
				case 60:
					for (final PlWarrior plw : pls.values()) {
						final Player pl = plw.getPlayer();
						ScreenUtil.sendActionBarDirect(pl, TCUtil.N + "Конец игры через " + TCUtil.P + "1 §7минуту!");
					}
					break;
				case 30:
				case 10:
					for (final PlWarrior plw : pls.values()) {
						final Player pl = plw.getPlayer();
						ScreenUtil.sendActionBarDirect(pl, TCUtil.N + "Осталось " + TCUtil.P + time + TCUtil.N + "секунд!");
					}
					break;
				case 5:
				case 4:
				case 3:
				case 2:
				case 1:
					for (final PlWarrior plw : pls.values()) {
						final Player pl = plw.getPlayer();
						ScreenUtil.sendTitleDirect(pl, TCUtil.A + time, "", 8, 20, 8);
						pl.playSound(pl.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
					}
					break;
				case 0:
					task.cancel();
					countEnd(Main.srnd, null);
					break;
				default:
					break;
				}
				time--;
			}
		}.runTaskTimer(Main.plug, 0, 20);
	}

	private Villager.Type getBmVllTp(final Biome b) {
		return Villager.Type.PLAINS;/*switch (b) {
			case BADLANDS, SAVANNA, SAVANNA_PLATEAU, CRIMSON_FOREST, ERODED_BADLANDS -> Villager.Type.SAVANNA;
			case BEACH, DESERT, NETHER_WASTES, SOUL_SAND_VALLEY -> Villager.Type.DESERT;
			case BAMBOO_JUNGLE, JUNGLE, MUSHROOM_FIELDS, SPARSE_JUNGLE -> Villager.Type.JUNGLE;
			case SWAMP, DARK_FOREST, WARPED_FOREST, RIVER -> Villager.Type.SWAMP;
			case SNOWY_BEACH, SNOWY_TAIGA, ICE_SPIKES, SNOWY_PLAINS -> Villager.Type.SNOW;
			case TAIGA -> Villager.Type.TAIGA;
			default -> Villager.Type.PLAINS;
		};*/
	}

	public void countEnd(final Random rand, final @Nullable Team wntm) {
		time = 6;
		mbspwm.cancel();
		state = GameState.END;
		//заменяем цвета на белый
		whtClrs();
		//убираем магазы
		for (final Mob sh : shps) {
			if (sh == null) continue;
			sh.remove();
		}
		//записываем победу
		if (wntm == null) {
			final String msg = Main.PRFX + "Результат: " + TCUtil.P + "Ничья" + TCUtil.N + "!";
			for (final PlWarrior plw : pls.values()) {
				plw.getPlayer().sendMessage(msg);
				plw.addStat(Stat.WZ_game, 1);
				endScore(plw, null);
			}
			for (final PlWarrior plw : spcs.values()) {
				plw.getPlayer().sendMessage(msg);
			}
		} else {
			final StringBuilder sb = new StringBuilder();
			sb.append(Main.PRFX).append(wntm.name("ая", true)).append(TCUtil.N).append(" комманда победила в этой битве!\n");
			sb.append(TCUtil.A).append("-=-=-=-=-=-=-=-=-=-=-=-=-=-\n");
			for (final PlWarrior p : wntm.pls.values()) {
				sb.append(wntm.color()).append(p.nik).append(TCUtil.N).append(" - ")
				.append(TCUtil.P).append(p.kills()).append(TCUtil.N).append(" убийств\n");
			}
			sb.append(TCUtil.A).append("-=-=-=-=-=-=-=-=-=-=-=-=-=-");
			final String msg = sb.toString();
			for (final PlWarrior plw : pls.values()) {
				plw.getPlayer().sendMessage(msg);
				plw.addStat(Stat.WZ_game, 1);
				plw.addStat(Stat.WZ_win, 1);
				endScore(plw, wntm);
			}
			for (final PlWarrior plw : spcs.values()) {
				plw.getPlayer().sendMessage(msg);
			}
		}
		//--
		task = new BukkitRunnable() {
			@Override
			public void run() {
                if (time == 0) {
                    for (final PlWarrior plw : pls.values()) {
                        Main.lobbyPlayer(plw.getPlayer(), plw);
                    }
                    task.cancel();
                    Main.endArena(getArena());
                } else {
					final String rmn = TCUtil.N + "До конца: " + TCUtil.P + time + " сек.";
                    for (final PlWarrior plw : pls.values()) {
						final Player pl = plw.getPlayer();
						plw.score.getSideBar().update(LIMIT, rmn);
                        final Firework fw = (Firework) pl.getWorld().spawnEntity(pl.getLocation(), EntityType.FIREWORK_ROCKET);
                        final FireworkMeta fm = fw.getFireworkMeta();
                        fm.addEffect(FireworkEffect.builder().withColor(Color.fromRGB(rand.nextInt(16777000) + 100)).build());
                        fw.setFireworkMeta(fm);
                        fm.setPower(2);
                    }
                }
				time--;
			}
		}.runTaskTimer(Main.plug, 0, 20);
	}
	
	public void waitScore(final PlWarrior pw) {
		pw.score.getSideBar().reset().title(Main.FULL)
			.add(" ")
			.add(TCUtil.N + "Карта: " + TCUtil.P + getName())
			.add(TCUtil.A + "=-=-=-=-=-=-=-")
			.add(TEAM, TCUtil.N + "Тима: " + (pw.team() == null
				? "§8Не Выбрана" : pw.team().name("ая", true)))
			.add(" ")
			.add(AMT, TCUtil.N + "Игроков: " + TCUtil.P + pls.size() + " чел.")
			.add(TCUtil.A + "=-=-=-=-=-=-=-")
//			.add(LIMIT, TCUtil.N + "Нужно еще " + TCUtil.A + (min - pls.size()) + " чел.")
			.add(LIMIT, TCUtil.N + "До начала: " + TCUtil.P + time + " сек.")
			.add(" ")
			.add("§e     ostrov77.ru").build();
	}
	
	public void runnScore(final PlWarrior pw) {
        final SideBar sb = pw.score.getSideBar().reset().title(Main.FULL)
            .add(" ")
            .add(TCUtil.N + "Карта: " + TCUtil.P + getName())
            .add(TCUtil.A + "=-=-=-=-=-=-=-")
            .add(" ");
        for (final Team tm : tms) {
            sb.add(tm.color(), tm.desc(pw));
        }
        sb.add(" ")
            .add(MONEY, TCUtil.N + "Монет: " + TCUtil.P + pw.coins() + " ⛃")
            .add(TCUtil.A + "=-=-=-=-=-=-=-")
			.add(LIMIT, TCUtil.P + TimeUtil.secondToTime(time))
            .add(" ")
            .add("§e     ostrov77.ru").build();
	}
	
	public void endScore(final PlWarrior pw, final @Nullable Team wntm) {
		pw.score.getSideBar().reset().title(Main.FULL)
			.add(" ")
			.add(TCUtil.N + "Карта: " + TCUtil.P + getName())
			.add(TCUtil.A + "=-=-=-=-=-=-=-")
			.add(TCUtil.N + "Поздравляем!")
			.add(" ")
			.add(TCUtil.N + "Победила: " + (wntm == null ? "§8Ничья" : wntm.name("ая", true)))
			.add(TCUtil.A + "=-=-=-=-=-=-=-")
			.add(LIMIT, TCUtil.N + "До конца: " + TCUtil.P + time + " сек.")
			.add(" ")
			.add("§e     ostrov77.ru").build();
	}
	
	public void stopMobSpawn() {
		if (mbspwm != null) mbspwm.cancel();
	}
}
