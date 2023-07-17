package me.Romindous.WarZone.Game;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import me.Romindous.WarZone.Utils.TitleManager;
import net.minecraft.core.BaseBlockPosition;

public class Setup {
	
	public final String nm;
	public final String wnm;
	public final String tl;
	public final byte min;
	public final byte max;
	public final boolean rndM;
	public final boolean fin;
	public final BaseBlockPosition[] tmSpawns;
	public final BaseBlockPosition[] tmShops;
	public final BaseBlockPosition cntr;
	public final Material[] mnbls;
	public final Material[] recs;
	
	public Setup(final ConfigurationSection ar) {
		rndM = false;
		nm = TitleManager.getFullName(ar.getName());
		fin = ar.contains("fin");
		wnm = ar.getString("world");
		tl = ar.getString("tl");
		min = (byte) ar.getInt("min");
		max = (byte) ar.getInt("max");
		if (rndM) {
			mnbls = null;
			recs = null;
			tmSpawns = null;
			tmShops = null;
			cntr = new BaseBlockPosition(0, 0, 0);
		} else {
			final String[] tx = ar.getString("teams.x").split(":");
			final String[] ty = ar.getString("teams.y").split(":");
			final String[] tz = ar.getString("teams.z").split(":");
			tmSpawns = new BaseBlockPosition[tx.length];
			for (byte i = 0; i < tx.length; i++) {
				tmSpawns[i] = new BaseBlockPosition(
					Integer.parseInt(tx[i]), 
					Integer.parseInt(ty[i]), 
					Integer.parseInt(tz[i]));
			}
			final String[] sx = ar.getString("shops.x").split(":");
			final String[] sy = ar.getString("shops.y").split(":");
			final String[] sz = ar.getString("shops.z").split(":");
			tmShops = new BaseBlockPosition[sx.length];
			for (byte i = 0; i < sx.length; i++) {
				tmShops[i] = new BaseBlockPosition(
					Integer.parseInt(sx[i]), 
					Integer.parseInt(sy[i]), 
					Integer.parseInt(sz[i]));
			}
			cntr = new BaseBlockPosition(ar.getInt("cntr.x"), ar.getInt("cntr.y"), ar.getInt("cntr.z"));
			final String[] ms = ar.getString("mnbl").split(":");
			mnbls = new Material[ms.length];
			for (int i = 0; i < ms.length; i++) {
				mnbls[i] = Material.getMaterial(ms[i]);
			}
			final String[] rs = ar.getString("recs").split(":");
			recs = new Material[rs.length];
			for (int i = 0; i < rs.length; i++) {
				recs[i] = Material.getMaterial(rs[i]);
			}
		}
		Bukkit.getConsoleSender().sendMessage("regging-" + nm + " in w-" + wnm);
		
		/*wnm = ar.getString("world");
		if (rndM = ar.getBoolean("rnd")) {
			ctSpawns = null;
			tSpawns = null;
			A = null;
			B = null;
		} else {
			final String[] tx = ar.getString("tspawns.x").split(":");
			final String[] ty = ar.getString("tspawns.y").split(":");
			final String[] tz = ar.getString("tspawns.z").split(":");
			tSpawns = new BaseBlockPosition[tx.length];
			for (byte i = (byte) (tx.length - 1); i >= 0; i--) {
				tSpawns[i] = new BaseBlockPosition(Integer.parseInt(tx[i]), Integer.parseInt(ty[i]), Integer.parseInt(tz[i]));
			}
			final String[] ctx = ar.getString("ctspawns.x").split(":");
			final String[] cty = ar.getString("ctspawns.y").split(":");
			final String[] ctz = ar.getString("ctspawns.z").split(":");
			ctSpawns = new BaseBlockPosition[ctx.length];
			for (byte i = (byte) (ctx.length - 1); i >= 0; i--) {
				ctSpawns[i] = new BaseBlockPosition(Integer.parseInt(ctx[i]), Integer.parseInt(cty[i]), Integer.parseInt(ctz[i]));
			}
			A = new BaseBlockPosition(ar.getInt("asite.x"), ar.getInt("asite.y"), ar.getInt("asite.z"));
			B = new BaseBlockPosition(ar.getInt("bsite.x"), ar.getInt("bsite.y"), ar.getInt("bsite.z"));
		}
		fin = ar.contains("fin");
		switch (ar.getString("type")) {
		case "gungame":
			gt = GameType.GUNGAME;
			break;
		case "invasion":
			gt = GameType.INVASION;
			break;
		case "defusal":
		default:
			gt = GameType.DEFUSAL;
			break;
		}*/
	}
}
