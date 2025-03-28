package ru.romindous.wz;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.wz.Game.Arena;
import ru.romindous.wz.Game.GameState;
import ru.romindous.wz.Game.PlWarrior;
import ru.romindous.wz.Game.Setup;
import ru.romindous.wz.Listeners.InterractLis;
import ru.romindous.wz.Listeners.InventoryLis;
import ru.romindous.wz.Listeners.MainLis;
import ru.romindous.wz.Utils.Inventories;
import ru.romindous.wz.Utils.Priced;

public class Main extends JavaPlugin {

    public static Main plug;
    public static File folder;
    public static BVec lobby;
    public static String PRFX;
    public static String FULL;
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
    public static final SecureRandom srnd = new SecureRandom();
    public static final HashMap<String, Integer> extras = new HashMap<>();
    public static final HashMap<String, Priced> trades = new HashMap<>();
    public static final HashMap<String, Arena> activearenas = new HashMap<String, Arena>();
    public static final HashMap<String, Setup> nonactivearenas = new HashMap<String, Setup>();
    private static final String SPLIT = " ";


    public void onEnable() {
        //Ostrov things
        PM.setOplayerFun(p -> new PlWarrior(p), true);
        TCUtil.N = "§7";
        TCUtil.P = "§d";
        TCUtil.A = "§3";

        FULL = bfr('[', TCUtil.P + "Поле" + TCUtil.A + "Брани", ']');
        PRFX = bfr('[', TCUtil.P + "П" + TCUtil.A + "Б", ']');

        getServer().getConsoleSender().sendMessage("§2WarZone is ready!");
        plug = this;

        //конфиг
        loadConfigs();

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
        }
    }

    public void onDisable() {
        final ArrayList<Arena> ars = new ArrayList<>(activearenas.values());
        for (final Arena ar : ars) {
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
            for (final String s : getConfig().getConfigurationSection("trade").getKeys(false)) {
                regTrade(s, getConfig().getString("trade." + s).split(SPLIT));
            }
            //арены
            file = new File(getDataFolder() + File.separator + "arenas.yml");
            file.createNewFile();
            final YamlConfiguration ars = YamlConfiguration.loadConfiguration(file);
            nonactivearenas.clear();
            if (ars.contains("lobby")) {
                final BVec lb = BVec.of(ars.getInt("lobby.x"),
                    ars.getInt("lobby.y"), ars.getInt("lobby.z"));
                final World tw = getServer().getWorld(ars.getString("lobby.world"));
                lobby = lb.w(tw == null ? Bukkit.getWorlds().getFirst() : tw);
            }
            if (!ars.contains("arenas")) {
                ars.createSection("arenas");
                ars.save(file);
            } else {
                for (final String s : ars.getConfigurationSection("arenas").getKeys(false)) {
                    final Setup stp = new Setup(ars.getConfigurationSection("arenas." + s));
                    nonactivearenas.put(stp.nm, stp);
                    if (stp.fin) {
                        GM.sendArenaData(Game.WZ, stp.nm, ru.komiss77.enums.GameState.ОЖИДАНИЕ, 0, "§7[§2Поле Брани§7]", "§2Ожидание", " ", "§7Игроков: §20§7/§2" + stp.min);
                    }
                }
            }
            folder = getDataFolder();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void regTrade(final String s, final String[] prs) {
        final int[] amts = new int[3];
        for (final String pr : prs) {
            if (pr.isEmpty()) continue;
            if (pr.length() < 2) {
                extras.put(s, NumUtil.intOf(pr, 0));
                return;
            }
            switch (pr.charAt(0)) {
                case 'f' -> amts[0] = NumUtil.intOf(pr.substring(1), 0);
                case 's' -> amts[1] = NumUtil.intOf(pr.substring(1), 0);
                case 't' -> amts[2] = NumUtil.intOf(pr.substring(1), 0);
                default -> {
                    extras.put(s, NumUtil.intOf(pr, 0));
                    return;
                }
            }
        }

        trades.put(s, new Priced(amts[0], amts[1], amts[2]));
    }

    public static Arena createArena(final String name) {
        final Setup stp = Main.nonactivearenas.get(name);
        return stp == null ? null : new Arena(stp);
    }

    public static void lobbyPlayer(final Player p, final PlWarrior pw) {
        nrmlzPl(p);
        pw.mobs0();
        pw.kills0();
        pw.coins0();
        pw.team(null, p);
        pw.arena(null);
        p.getInventory().clear();
        p.getInventory().setItem(0, Inventories.mkItm(Material.TRIDENT, TCUtil.P + "Выбор Карты", true));
        p.getInventory().setItem(8, Inventories.mkItm(Material.MAGMA_CREAM, "§4Выход в Лобби", true));
        updateScore(pw);
        inGameCnt();
        if (lobby != null) p.teleport(lobby());
        final String prm = pw.getTopPerm();
        pw.taq(bfr('[', TCUtil.A + "ЛОББИ", ']'),
            TCUtil.P, (prm.isEmpty() ? "" : afr('(', "§e" + prm, ')')));
        for (final Player op : Bukkit.getOnlinePlayers()) {
            if (p.getEntityId() == op.getEntityId()) continue;
            p.showPlayer(plug, op);
            final Arena ar = Arena.getPlArena(op);
            if (ar == null || ar.getState() == GameState.WAITING) {
                op.showPlayer(plug, p);
            } else {
                op.hidePlayer(plug, p);
            }
        }
    }

    public static Location lobby() {
        final World w = lobby.w();
        return lobby.center(w == null ? Bukkit.getWorlds().getFirst() : w);
    }

    public static void inGameCnt() {
        int i = 0;
        for (final Arena ar : Main.activearenas.values()) i+=ar.getPls().size();
        final Component c = TCUtil.form(TCUtil.N + "Сейчас в игре: " + TCUtil.P + i + TCUtil.N + " человек!");
        for (final Player pl : Bukkit.getOnlinePlayers()) pl.sendPlayerListFooter(c);
    }

    public static void updateScore(final PlWarrior pw) {
        pw.score.getSideBar().reset().title(Main.FULL)
            .add(" ")
            .add(TCUtil.N + "Карта: " + TCUtil.A + "ЛОББИ")
            .add(TCUtil.A + "=-=-=-=-=-=-=-")
            .add(TCUtil.N + "Мобов убито: " + TCUtil.P + pw.getStat(Stat.WZ_mbs))
            .add(" ")
            .add(TCUtil.A + "Игр " + TCUtil.N + "всего:")
            .add(TCUtil.N + "Выйграно: " + TCUtil.P + pw.getStat(Stat.WZ_win))
            .add(TCUtil.N + "Проиграно: " + TCUtil.P + pw.getStat(Stat.WZ_loose))
            .add(" ")
            .add(TCUtil.A + "=-=-=-=-=-=-=-")
            .add(TCUtil.N + "(" + TCUtil.P + "К" + TCUtil.N + "/" + TCUtil.A + "Д" + TCUtil.N + "): " + TCUtil.P +
                StringUtil.toSigFigs((float) pw.getStat(Stat.WZ_klls) / (float) pw.getStat(Stat.WZ_dths), (byte) 2))
            .add(" ")
            .add("§e    ostrov77.ru").build();
    }

    public static void endArena(Arena ar) {
        GM.sendArenaData(Game.WZ, ar.getName(), ru.komiss77.enums.GameState.ОЖИДАНИЕ, 0, "§7[§2Поле Брани§7]", "§2Ожидание", " ", "§7Игроков: §20§7/§2" + ar.getMin());
        for (final PlWarrior pw : ar.getSpcs().values()) {
            lobbyPlayer(pw.getPlayer(), pw);
        }
        activearenas.remove(ar.getName());
        ar.getPls().clear();
        ar.getSpcs().clear();
        ar.stopMobSpawn();
        //заменяем цвета на белый
        ar.whtClrs();
        //убираем магазы
        for (final Mob sh : ar.shps) {
            if (sh == null) continue;
            sh.remove();
        }
    }

    public static void nrmlzPl(final Player p) {
        p.setGameMode(GameMode.SURVIVAL);
        p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20d);
        p.setHealth(20d);
        p.setExp(0);
        p.setLevel(0);
        p.setFoodLevel(20);
        p.setSaturation(20f);
        p.setFireTicks(-1);
        p.closeInventory();
        for (final PotionEffect ef : p.getActivePotionEffects()) {
            p.removePotionEffect(ef.getType());
        }
    }

    public static String bfr(final char b, final String txt, final char d) {
        return TCUtil.N + b + txt + TCUtil.N + d + " ";
    }

    public static String afr(final char b, final String txt, final char d) {
        return " " + TCUtil.N + b + txt + TCUtil.N + d;
    }
}