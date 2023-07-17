package me.Romindous.WarZone;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import me.Romindous.WarZone.Commands.WZCmd;
import me.Romindous.WarZone.Game.Arena;
import me.Romindous.WarZone.Game.Setup;
import me.Romindous.WarZone.Game.Team;
import me.Romindous.WarZone.Listeners.InterractLis;
import me.Romindous.WarZone.Listeners.InventoryLis;
import me.Romindous.WarZone.Listeners.MainLis;
import me.Romindous.WarZone.Utils.EntMeta;
import me.Romindous.WarZone.Utils.Inventories;
import me.Romindous.WarZone.Utils.TitleManager;
import net.kyori.adventure.text.Component;
import net.minecraft.EnumChatFormat;
import net.minecraft.server.dedicated.DedicatedServer;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;

public class Main extends JavaPlugin{
	
	public static Main plug;
	public static String tbl;
	public static File folder;
	public static Location lobby;
	public static DedicatedServer ds;
	public static byte GPTmMin;
	public static byte gmTmMin;
	public static byte blkkd;
	public static byte mbPrd;
	public static byte clrRng;
	public static byte tstRds;
	public static byte enchThr;
	public static byte armrThr;
	public static byte flwRng;
	public static byte mxEnchLvl;
	public static float forArmrScr;
	public static float forWpnScr;
	public static float forHlthScr;
	public static float forSpdScr;
	public static float forEnchScr;
	public static float lvlbfr;
	public static float maxlvl;
	public static boolean maxSQL = true;
	public static final SecureRandom srnd = new SecureRandom();
	public static final ItemStack air = new ItemStack(Material.AIR);
	public static final HashMap<String, Arena> activearenas = new HashMap<String, Arena>();
	public static final HashMap<String, Setup> nonactivearenas = new HashMap<String, Setup>();
	
	
    public void onEnable() {
		
		getServer().getConsoleSender().sendMessage("§2WarZone is ready!");
		plug = this;
		try {
	    	ds = (DedicatedServer) getServer().getClass().getMethod("getServer").invoke(getServer());
	    } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
	    	e.printStackTrace();
	    }
		
		//конфиг
		loadConfigs();
		
		if (maxSQL) {return;}
		getCommand("wz").setExecutor(new WZCmd(this));
		getServer().getPluginManager().registerEvents(new MainLis(), this);
		getServer().getPluginManager().registerEvents(new InterractLis(), this);
		getServer().getPluginManager().registerEvents(new InventoryLis(), this);
		for (final World w : getServer().getWorlds()) {
			w.setGameRule(GameRule.KEEP_INVENTORY, true);
			w.setGameRule(GameRule.NATURAL_REGENERATION, true);
			w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
			w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
			w.setTime(6000);
			w.setStorm(true);
			w.setWeatherDuration(1728000);
			w.setThundering(true);
			w.setThunderDuration(1728000);
			for (final Entity e : w.getEntitiesByClass(WanderingTrader.class)) {
				e.remove();
			}
		}
	}
	
	public void onDisable() {
		final ArrayList<Arena> ars = new ArrayList<>(activearenas.values());
		for (final Arena ar : ars) {
			ar.stopMobSpawn();
			endArena(ar);
		}
		getServer().getConsoleSender().sendMessage("§4WarZone is disabled...");
		
	}

	public void loadConfigs() {
		try {
			File file = new File(getDataFolder() + File.separator + "config.yml");
	        if (!file.exists() || !getConfig().contains("spawns")) {
	        	getServer().getConsoleSender().sendMessage("Config for WarZone not found, creating a new one...");
	    		getConfig().options().copyDefaults(true);
	    		getConfig().save(file);
	        }
	        //значения из конфига
	        tbl = getConfig().getString("setup.table");
	        GPTmMin = (byte) getConfig().getInt("setup.GPTmMin");
	        gmTmMin = (byte) getConfig().getInt("setup.gmTmMin");
	        blkkd = (byte) getConfig().getInt("setup.blkkd");
	        mbPrd = (byte) getConfig().getInt("spawns.mbPrd");
	        clrRng = (byte) getConfig().getInt("setup.clrRng");
	        tstRds = (byte) getConfig().getInt("spawns.tstRds");
	        enchThr = (byte) getConfig().getInt("spawns.enchThr");
	        armrThr = (byte) getConfig().getInt("spawns.armrThr");
	        flwRng = (byte) getConfig().getInt("spawns.flwRng");
	        mxEnchLvl = (byte) getConfig().getInt("setup.mxEnchLvl");
	        forArmrScr = (float) getConfig().getDouble("spawns.forArmrScr");
	        forWpnScr = (float) getConfig().getDouble("spawns.forWpnScr");
	        forHlthScr = (float) getConfig().getDouble("spawns.forHlthScr");
	        forSpdScr = (float) getConfig().getDouble("spawns.forSpdScr");
	        forEnchScr = (float) getConfig().getDouble("spawns.forEnchScr");
	        lvlbfr = (float) getConfig().getDouble("spawns.lvlbfr");
	        maxlvl = (float) getConfig().getDouble("spawns.maxlvl");
	        //арены
	        file = new File(getDataFolder() + File.separator + "arenas.yml");
	        file.createNewFile();
	        final YamlConfiguration ars = YamlConfiguration.loadConfiguration(file);
	        nonactivearenas.clear();
	        if (!ars.contains("arenas")) {
	        	ars.createSection("arenas");
		        ars.save(file);
	        } else {
				for(final String s : ars.getConfigurationSection("arenas").getKeys(false)) {
					final Setup stp = new Setup(ars.getConfigurationSection("arenas." + s));
					nonactivearenas.put(stp.nm, stp);
					if (stp.fin) {
						ApiOstrov.sendArenaData(stp.nm, ru.komiss77.enums.GameState.ОЖИДАНИЕ, "§7[§2Поле Брани§7]", "§2Ожидание", " ", "§7Игроков: §20§7/§2" + stp.min, "", 0);
					}
				}
			}
	        if (ars.contains("lobby")) {
	        	lobby = new Location(getServer().getWorld(ars.getString("lobby.world")), ars.getInt("lobby.x"), ars.getInt("lobby.y"), ars.getInt("lobby.z"));
	        }
	        folder = getDataFolder();
	        
        }
        catch (IOException e) {
        	e.printStackTrace();
            return;
        }
	}
	
	public static Arena createArena(final String name) {
		final Setup stp = Main.nonactivearenas.get(name);
		return stp == null ? null : new Arena(stp);
	}
	
	public static void lobbyPlayer(final Player p) {
		if (p == null) {
			return;
		}
		nrmlzPl(p);
		p.getInventory().clear();
		p.removeMetadata("kls", plug);
		p.removeMetadata("cns", plug);
		p.getInventory().setItem(0, Inventories.mkItm(Material.TRIDENT, "§6Выбор Карты", true));
		p.getInventory().setItem(8, Inventories.mkItm(Material.MAGMA_CREAM, "§4Выход в Лобби", true));
		final String prm = Main.getTopPerm(PM.getOplayer(p));
		p.playerListName(Component.text("§7[§5ЛОББИ§7] " + p.getName() + (prm.isEmpty() ? "" : " §7(§e" + prm + "§7)")));

        Bukkit.getScheduler().runTaskLater(plug, new Runnable() {
			@Override
			public void run() {
				for (final Player other : Bukkit.getOnlinePlayers()) {
					if (other.hasMetadata("kls")) {
						p.hidePlayer(plug, other);
						other.hidePlayer(plug, p);
					} else {
						final Arena ar = Arena.getPlArena(other.getName());
						final String prm = Main.getTopPerm(PM.getOplayer(other));
						//неймтег
						TitleManager.sendNmTg(other.getName(), "§7[" + (ar == null ? "§5ЛОББИ" : "§6" + ar.getName()) + "§7] ", (prm.isEmpty() ? "" : " §7(§e" + prm + "§7)"), EnumChatFormat.c);
						p.showPlayer(plug, other);
						other.showPlayer(plug, p);
					}
				}
			}
		}, 4);
		if (lobby != null) {
			p.teleport(lobby);
		}
		updateScore(p.getName());
	}
	
	public static void updateScore(final String name) {
		final Player p = Bukkit.getPlayer(name);
		final Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
		final Objective ob = sb.registerNewObjective("Поле Брани", Criteria.DUMMY, Component.text("§7[§2Поле Брани§7]"));
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		ob.getScore("§7Карта: §5Лобби")
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
		
		ob.getScore("§e     ostrov77.su")
			.setScore(0);
		p.setScoreboard(sb);
	}

	public static String getStrFlt(final float flt) {
		return String.valueOf(flt).length() > 4 ? String.valueOf(flt).substring(0, 4) : String.valueOf(flt);
	}

	public static void waitPlayer(final Player p) {
		nrmlzPl(p);
		p.getInventory().clear();
		EntMeta.chngMoney(p, (short) 0, false);
		p.getInventory().setItem(2, Inventories.mkItm(Material.END_CRYSTAL, "§eВыбор Комманды", true));
		p.getInventory().setItem(6, Inventories.mkItm(Material.SLIME_BALL, "§cВыход", true));
	}
	
	public static String prf() {
		return "§7[§2WZ§7] ";
	}
	
	public static void endArena(Arena ar) {
		ApiOstrov.sendArenaData(ar.getName(), ru.komiss77.enums.GameState.ОЖИДАНИЕ, "§7[§2Поле Брани§7]", "§2Ожидание", " ", "§7Игроков: §20§7/§2" + ar.getMin(), "", 0);
		for (String s : ar.getSpcs()) {
			lobbyPlayer(Bukkit.getPlayer(s));
		}
		activearenas.remove(ar.getName());
		ar.getPls().clear();
		ar.getSpcs().clear();
		//заменяем цвета на белый
		for (final Team tm : ar.getTms()) {
			ar.whtClrs(tm.getName().charAt(1));
		}
		//убираем магазины
		for (final UUID id : ar.shps) {
			if (id != null) {
				Bukkit.getEntity(id).remove();
			}
		}
		//убираем мобов на карте
		for (final Entity e : ar.getCntr().getWorld().getNearbyEntities(ar.getCntr(), ar.getCntr().distance(ar.getTms()[0].getSpwn()) + 20, 10, ar.getCntr().distance(ar.getTms()[0].getSpwn()) + 20)) {
			if (e instanceof LivingEntity && e.getType() != EntityType.PLAYER) {
				e.remove();
			}
		}
		ar = null;
		for (Player pl : Bukkit.getOnlinePlayers()) {
			pl.sendPlayerListFooter(Component.text("§7Сейчас в игре: §2" + MainLis.getPlaying() + "§7 человек!"));
		}
	}
	
	public static void nrmlzPl(final Player p) {
		p.setGameMode(GameMode.SURVIVAL);
		p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
		p.setExp(0);
		p.setLevel(0);
		p.setFoodLevel(20);
		p.setSaturation(20);
		p.setHealth(20);
		Bukkit.getPlayer(p.getName()).setFireTicks(0);
		p.closeInventory();
		for (final PotionEffect ef : p.getActivePotionEffects()) {
	        p.removePotionEffect(ef.getType());
		}
	}
	
	public static boolean notItmNull(final ItemStack it) {
		return it != null && it.getType() != Material.AIR && it.hasItemMeta();
	}
	
	public static void crtSbdTm(final Scoreboard sb, final String nm, final String prf, final String val, final String sfx) {
		final org.bukkit.scoreboard.Team tm = sb.registerNewTeam(nm);
		tm.addEntry(val);
		tm.prefix(Component.text(prf));
		tm.suffix(Component.text(sfx));
	}
	
	public static void chgSbdTm(final Scoreboard sb, final String nm, final String prf, final String sfx) {
		final org.bukkit.scoreboard.Team tm = sb.getTeam(nm);
		if (tm == null) {
			plug.getLogger().info("Team " + nm + " is null");
		} else {
			tm.prefix(Component.text(prf));
			tm.suffix(Component.text(sfx));
		}
	}
	
	public static String getTopPerm(final Oplayer op) {
		if (op.hasGroup("xpanitely")) {
			return "Хранитель";
		} else if (op.hasGroup("supermoder")) {
			return "Архангел";
		} else if (op.hasGroup("legend")) {
			return "Легенда";
		} else if (op.hasGroup("hero")) {
			return "Герой";
		} else if (op.hasGroup("warior")) {
			return "Воин";
		}
		return "";
	}
	
	public static String transPerm(final String s) {
		switch (s) {
		case "xpanitely":
			return "Хранитель";
		case "supermoder":
			return "Архангел";
		case "legend":
			return "Легенда";
		case "hero":
			return "Герой";
		case "warior":
			return "Воин";
		default:
			return "";
		}
	}
}