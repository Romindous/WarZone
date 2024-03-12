package ru.romindous.wz.Game;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.InventoryManager;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.romindous.wz.Main;
import ru.romindous.wz.Menus.TeamMenu;

import javax.annotation.Nullable;

public class PlWarrior extends Oplayer {

    public static final int KILL_RWD = 50, MOB_DEL = 2;
    public static final NamedTextColor dtc = NamedTextColor.GRAY;
    private Arena ar = null;
    private Team team = null;

    public PlWarrior(final HumanEntity p) {
        super(p);
    }

    public Arena arena() {return ar;}
    public void arena(final @Nullable Arena ar) {
        this.team = null;
        this.ar = ar;
    }

    public Team team() {return team;}
    public void team(final @Nullable Team tm, final Player p) {
        if (ar == null) {
            this.team = null;
            return;
        }
        if (team != null) {
            if (team.equals(tm)) {
                p.sendMessage(Main.PRFX + "§cТы уже в этой комманде!");
                return;
            }
            team.pls.remove(nik);
        }
        if (tm == null) {
            this.team = null;
            p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 2f, 0.6f);
        } else {
            this.team = tm;
            tm.pls.put(nik, this);
            p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 2f, 1f);
            p.sendMessage(Main.PRFX + "Ты теперь в " + tm.name("ой", true) + TCUtils.N + " комманде!");
            taq(Main.bfr('[', TCUtils.A + ar.getName(), ']'), team.color(),
                Main.afr('(', TCUtils.P + kills + TCUtils.N + "-" + TCUtils.P + mobs, ')'));
        }
        for (final PlWarrior sh : ar.getPls().values()) {
            final Player pl = sh.getPlayer(); if (pl == null) continue;
            final SmartInventory si = InventoryManager.getInventory(pl).orElse(null);
            if (si != null && si.getProvider() instanceof TeamMenu) {
                pl.closeInventory(); ar.teamInv.open(pl);
            }
        }
    }

    public void taq(final String pfx, final String afx, final String sfx) {
        color(team == null ? dtc : team.txc);
        final Player p = getPlayer();
        tabPrefix(pfx, p);
        tabSuffix(sfx, p);
        beforeName(afx, p);
        tag(pfx, sfx);
    }

    private int coins = 0;
    public int coins() {return coins;}
    public void coins(final int dc) {
        this.coins += dc;
        score.getSideBar().update(Arena.MONEY, TCUtils.N + "Монет: " + TCUtils.P + coins + " ⛃");
    }
    public void coins0() {this.coins = 0;}

    private int kills = 0;
    public void kills0() {this.kills = 0;}
    public void killsI() {this.kills += 1;}
    public int kills() {return kills;}

    private int mobs = 0;
    public void mobs0() {this.mobs = 0;}
    public void mobsI() {this.mobs += 1;}
    public int mobs() {return mobs;}
}
