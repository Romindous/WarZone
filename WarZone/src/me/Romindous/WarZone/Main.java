package me.Romindous.WarZone;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import me.Romindous.WarZone.Commands.WZCmd;
import me.Romindous.WarZone.Game.Arena;
import me.Romindous.WarZone.Game.Team;
import me.Romindous.WarZone.Listeners.InterractLis;
import me.Romindous.WarZone.Listeners.InventoryLis;
import me.Romindous.WarZone.Listeners.MainLis;
import me.Romindous.WarZone.SQL.MySQL;
import me.Romindous.WarZone.Utils.EntMeta;
import me.Romindous.WarZone.Utils.Inventories;
import me.Romindous.WarZone.Utils.TitleManager;
import net.kyori.adventure.text.Component;
import net.minecraft.EnumChatFormat;
import net.minecraft.server.dedicated.DedicatedServer;
import me.Romindous.WarZone.SQL.SQLGet;
import ru.komiss77.ApiOstrov;

public class Main extends JavaPlugin{
	
	public static Main plug;
	public static String tbl;
	public static SQLGet data;
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
	public static final HashSet<Arena> activearenas = new HashSet<Arena>();
	public static final LinkedList<String> nonactivearenas = new LinkedList<String>();
	
	
    public void onEnable() {
		
		getServer().getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "WarZone is ready!");
		getCommand("wz").setExecutor(new WZCmd(this));
		plug = this;
		try {
	    	ds = (DedicatedServer) getServer().getClass().getMethod("getServer").invoke(getServer());
	    } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
	    	e.printStackTrace();
	    }
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
		
		//конфиг
		loadConfigs();
		//mysql
		dataConn();
	}
	
	public static void dataConn() {
		Bukkit.getLogger().info("Reconnected to a database! :D");
		(data = new SQLGet()).mkTbl("wzpls", "name", "kls", "dths", "rsps", "wns", "lss", "prm");
	}
	
	public void onDisable() {

		getServer().getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "WarZone is disabled...");
		
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
					if (ars.contains("arenas." + s + ".fin")) {
						ApiOstrov.sendArenaData(s, ru.komiss77.enums.GameState.ОЖИДАНИЕ, "§7[§2Поле Брани§7]", "§2Ожидание", " ", "§7Игроков: §20§7/§2" + ars.get("arenas." + s + ".min"), "", 0);
						nonactivearenas.add(s);
					}
				}
			}
	        if (ars.contains("lobby")) {
	        	lobby = new Location(getServer().getWorld(ars.getString("lobby.world")), ars.getInt("lobby.x"), ars.getInt("lobby.y"), ars.getInt("lobby.z"));
	        }
	        folder = getDataFolder();
	        
        }
        catch (IOException | NullPointerException e) {
        	e.printStackTrace();
            return;
        }
	}
	
	public Arena createArena(final String name) {
		final YamlConfiguration ars = YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "arenas.yml"));
		return new Arena(name, 
				(byte) ars.getInt("arenas." + name + ".min"), 
				(byte) ars.getInt("arenas." + name + ".max"), 
				new Location(Bukkit.getWorld(ars.getString("arenas." + name + ".world")), ars.getInt("arenas." + name + ".cntr.x"), ars.getInt("arenas." + name + ".cntr.y"), ars.getInt("arenas." + name + ".cntr.z")), 
				ars);
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
		final String prm = data.getString(p.getName(), "prm");
		p.playerListName(Component.text("§7[§5ЛОББИ§7] " + p.getName() + (prm.length() > 1 ? " §7(§e" + prm + "§7)" : "")));

        Bukkit.getScheduler().runTaskLater(plug, new Runnable() {
			@Override
			public void run() {
				for (final Player other : Bukkit.getOnlinePlayers()) {
					if (other.hasMetadata("kls")) {
						p.hidePlayer(plug, other);
						other.hidePlayer(plug, p);
					} else {
						final Arena ar = Arena.getPlArena(other.getName());
						final String prm = data.getString(other.getName(), "prm");
						TitleManager.sendNmTg(other.getName(), "§7[" + (ar == null ? "§5ЛОББИ" : "§6" + ar.getName()) + "§7] ", (prm.length() > 1 ? " §7(§e" + prm + "§7)" : ""), EnumChatFormat.c);
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
		try {
			final ResultSet rs = data.exctStrStmt("SELECT * FROM " + Main.tbl + " WHERE NAME=?", name).executeQuery(); rs.next();
			final Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
			final Objective ob = sb.registerNewObjective("Поле Брани", "", Component.text("§7[§2Поле Брани§7]"));
			ob.setDisplaySlot(DisplaySlot.SIDEBAR);
			ob.getScore("§7Карта: §5Лобби")
				.setScore(8);
			ob.getScore("   ").setScore(7);
			ob.getScore("§7К/Д (убийства на смерти): ")
				.setScore(6);
			ob.getScore("§2" + String.valueOf(rs.getInt("KLS")) + " §7/ (§2" + String.valueOf(rs.getInt("DTHS")) + " §7-§2 " + String.valueOf(rs.getInt("RSPS")) + "§7) = §2" 
				+ getStrFlt((float) rs.getInt("KLS") / (float) (rs.getInt("DTHS") - rs.getInt("RSPS") > 0 ? rs.getInt("DTHS") - rs.getInt("RSPS") : 1)))
				.setScore(5);
			ob.getScore("  ").setScore(4);
			ob.getScore("§7Победы / Проигрыши: ")
				.setScore(3);
			ob.getScore("§2" + String.valueOf(rs.getInt("WNS")) + " §7/§2 " + String.valueOf(rs.getInt("LSS")) + " §7=§2 " 
				+ getStrFlt((float) rs.getInt("WNS") / (float) (rs.getInt("LSS") == 0 ? 1 : rs.getInt("LSS"))))
				.setScore(2);
			ob.getScore(" ").setScore(1);
			
			ob.getScore("§e     ostrov77.su")
				.setScore(0);
			Bukkit.getPlayer(name).setScoreboard(sb);
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
		activearenas.remove(ar);
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
		//чистим коллекции
		Arrays.fill(ar.getTms(), null);
		Arrays.fill(ar.shps, null);
		Arrays.fill(ar.mnbls, null);
		Arrays.fill(ar.recs, null);
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
		tm.prefix(Component.text(prf));
		tm.suffix(Component.text(sfx));
	}
}