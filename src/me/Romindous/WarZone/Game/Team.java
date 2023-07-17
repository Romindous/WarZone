package me.Romindous.WarZone.Game;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;

public class Team {
	
	public byte rsps;
	final String name;
	final Location spwn;
	final Set<String> pls;
	
	public Team(final String name, final Location spwn) {
		//Bukkit.getConsoleSender().sendMessage("nm-" + name);
		this.rsps = 3;
		this.spwn = spwn;
		this.name = name;
		this.pls = new HashSet<String>();
	}
	
	public Location getSpwn() {
		return spwn;
	}

	public String getName() {
		return name;
	}

	public static List<String> lrFromPls(final Team tm) {
		final LinkedList<String> lr = new LinkedList<String>();
		lr.add("");
		for (final String s : tm.pls) {
			lr.add(tm.name.substring(0, 2) + "✦ §7" + s);
		}
		
		return lr;
	}

	public static boolean canJoin(final Team[] tms, final Team tm) {
		float i = 0;
		for (final Team t : tms) {
			i += t.pls.size();
		}
		i = i / (float) tms.length;
		return tm.pls.size() <= i ;
	}
}
