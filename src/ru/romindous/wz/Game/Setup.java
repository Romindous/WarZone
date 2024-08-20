package ru.romindous.wz.Game;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import ru.komiss77.modules.world.XYZ;

public class Setup {
	
	public final String nm;
	public final String tl;
	public final byte min;
	public final byte max;
	public final boolean fin;
	public final XYZ[] tmSpawns;
	public final XYZ[] tmShops;
	public final XYZ cntr;
	public final Material[] mnbls;
	public final Material[] recs;
	
	public Setup(final ConfigurationSection ar) {
		nm = ar.getName();
		fin = ar.contains("fin");
		tl = ar.getString("tl");
		min = (byte) ar.getInt("min");
		max = (byte) ar.getInt("max");
		final String[] tx = ar.getString("teams.x").split(":");
		final String[] ty = ar.getString("teams.y").split(":");
		final String[] tz = ar.getString("teams.z").split(":");
		tmSpawns = new XYZ[tx.length];
		for (byte i = 0; i < tx.length; i++) {
			tmSpawns[i] = new XYZ("",
				Integer.parseInt(tx[i]),
				Integer.parseInt(ty[i]),
				Integer.parseInt(tz[i]));
		}
		final String[] sx = ar.getString("shops.x").split(":");
		final String[] sy = ar.getString("shops.y").split(":");
		final String[] sz = ar.getString("shops.z").split(":");
		tmShops = new XYZ[sx.length];
		for (byte i = 0; i < sx.length; i++) {
			tmShops[i] = new XYZ("",
				Integer.parseInt(sx[i]),
				Integer.parseInt(sy[i]),
				Integer.parseInt(sz[i]));
		}
		cntr = new XYZ(ar.getString("world"), ar.getInt("cntr.x"), ar.getInt("cntr.y"), ar.getInt("cntr.z"));
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
		Bukkit.getConsoleSender().sendMessage("regging-" + nm + " in w-" + cntr.worldName);
	}
}
