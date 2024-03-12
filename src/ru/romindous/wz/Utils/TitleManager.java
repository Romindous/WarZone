package ru.romindous.wz.Utils;

import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;

public class TitleManager {

	public static void sendTtlSbTtl(final Player p, final String ttl, final String sbttl, final int tm) {
		ApiOstrov.sendTitleDirect(p, ttl, sbttl, 4, tm, 20);
	}

	public static void sendTtl(final Player p, final String ttl, final int tm) {
		ApiOstrov.sendTitleDirect(p, ttl, "", 4, tm, 20);
	}

	public static void sendSbTtl(final Player p, final String sbttl, final int tm) {
		ApiOstrov.sendTitleDirect(p, "", sbttl, 4, tm, 20);
	}

	public static void sendAcBr(final Player p, final String msg) {
		ApiOstrov.sendActionBarDirect(p, msg);
	}
}
