package ru.romindous.wz.Menus;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.*;
import ru.romindous.wz.Game.Arena;
import ru.romindous.wz.Game.PlWarrior;
import ru.romindous.wz.Game.Team;

public class TeamMenu implements InventoryProvider {

    private static final ItemStack[] emt = getEmpty();
    private static final int midSlt = 13;

    @Override
    public void init(final Player p, final InventoryContent its) {
        p.playSound(p.getLocation(), Sound.BLOCK_BEEHIVE_ENTER, 1f, 1.2f);
        final PlWarrior pw = PM.getOplayer(p, PlWarrior.class);
        final Arena ar = pw.arena();
        if (ar == null) {
            p.closeInventory();
            return;
        }

        its.getInventory().setContents(emt);
        its.set(22, ClickableItem.of(new ItemBuilder(Material.REDSTONE_TORCH).name("§cВыход").build(), e -> {
            p.closeInventory();
        }));

        final Team[] tms = ar.getTms();
        int l = 0;
        for (final Team t : tms) l += t.pls.size();
        l = l / tms.length;
        final int lmt = l;
        final Team pt = pw.team();
        its.set(4, ClickableItem.of(new ItemBuilder(Material.TURTLE_HELMET).name(TCUtil.P + "Быстрый Выбор").build(), e -> {
            for (final Team tm : tms) {
                if (tm.pls.size() > lmt || tm.equals(pt)) continue;
                pw.team(tm, p);
                break;
            }
        }));

        final int hlf = tms.length >> 1;
        for (int i = 0; i != tms.length; i++) {
            final Team tm = tms[i];
            final int df = i - hlf;
            final int slt = midSlt + df + (1 + (df >> 31));
            if (tm.equals(pt)) {
                its.set(slt, ClickableItem.empty(new ItemBuilder(Material.LEATHER_HELMET)
                    .color(Color.fromRGB(tm.txc.value())).enchant(Enchantment.MENDING)
                    .name(tm.name("ая", true)).lore(tm.lore("§cТы уже в этой комманде!")).build()));
            } else if (tm.pls.size() > lmt) {
                its.set(slt, ClickableItem.empty(new ItemBuilder(Material.LEATHER_HELMET)
                    .color(Color.fromRGB(tm.txc.value())).name(tm.name("ая", true))
                    .lore(tm.lore("§cCлишком много игроков!")).build()));
            } else {
                its.set(slt, ClickableItem.of(new ItemBuilder(Material.LEATHER_HELMET)
                    .color(Color.fromRGB(tm.txc.value())).name(tm.name("ая", true))
                    .lore(tm.lore("")).build(), e -> {
                    pw.team(tm, p);
                }));
            }
        }
    }

    private static ItemStack[] getEmpty() {
        final ItemStack[] its = new ItemStack[27];
        for (int i = 0; i < 27; i++) {
            its[i] = new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE).name("§0.").build();
        }
        return its;
    }
}
