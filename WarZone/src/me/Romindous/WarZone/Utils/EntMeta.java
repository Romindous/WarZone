package me.Romindous.WarZone.Utils;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;

import me.Romindous.WarZone.Main;

public class EntMeta {

	public static void chngMeta(final Metadatable ent, final String meta, final byte val) {
		ent.setMetadata(meta, new FixedMetadataValue(Main.plug, val + (ent.hasMetadata(meta) ? ent.getMetadata(meta).get(0).asByte() : 0)));
	}
	
	public static void chngMoney(final Player ent, final short val, final boolean show) {
		ent.setMetadata("cns", new FixedMetadataValue(Main.plug, val + (ent.hasMetadata("cns") ? ent.getMetadata("cns").get(0).asShort() : 0)));
		if (show) {
			Main.chgSbdTm(ent.getScoreboard(), "cns", "", " §6" + String.valueOf(ent.getMetadata("cns").get(0).asShort()));
			TitleManager.sendActionBar(ent, val > 0 ? "§a+" + val + " монет" : "§c" + val + " монет");
		}
	}

	/*public static boolean checkBol(final Metadatable ent, final String meta, final Object value, final Main plug) {
		if (!ent.hasMetadata(meta) || ent.getMetadata(meta).size() < 1) {
			ent.setMetadata(meta, new FixedMetadataValue(plug, value));
			return true;
		}
		return ent.getMetadata(meta).get(0).value().equals(value);
	}
	
	public static void setOnKD(final Metadatable ent, final String meta, final Object rpl, String msg, final int kd, final Main plug) {
		final Object o = ent.getMetadata(meta).get(0).value();
		ent.setMetadata(meta, new FixedMetadataValue(plug, rpl));
		Bukkit.getScheduler().scheduleSyncDelayedTask(plug, new Runnable() {
			@Override
			public void run() {
				ent.setMetadata(meta, new FixedMetadataValue(plug, o));
				if (ent instanceof Player) {
					TitleManager.sendActionBar(((Player) ent), msg);
				}
			}
		}, kd);
	}*/
}
