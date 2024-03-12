package ru.romindous.wz.Game;

import net.kyori.adventure.text.format.NamedTextColor;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.TCUtils;

import java.util.HashMap;
import java.util.LinkedList;

public class Team {
	
	public byte rsps;
	public final WXYZ spwn;
	public final char cc;
	public final NamedTextColor txc;
	public final HashMap<String, PlWarrior> pls;

	private static final int RESPS = 3;

	public Team(final NamedTextColor txc, final WXYZ spwn) {
		this.rsps = RESPS;
		this.spwn = spwn;
		this.txc = txc;
		this.cc = TCUtils.toChar(txc);
		this.pls = new HashMap<>();
	}

	public String color() {
		return "§" + cc;
	}

	public String name(final String end, final boolean clr) {
		return TCUtils.nameOf(cc, end, clr);
	}

	public String desc(final PlWarrior pw) {
		if (pls.isEmpty()) {
			return " " + name("ая", true) + TCUtils.N + ": <X>";
		} else if (this.equals(pw.team())) {
			return rsps == 0 ? "§8◆ " + name("ая", true) + TCUtils.N + ": " + TCUtils.A + pls.size() + TCUtils.N + " чел."
				: "§8◆ " + name("ая", true) + TCUtils.N + ": " + TCUtils.P + rsps + TCUtils.N + " возр.";
		} else {
			return rsps == 0 ? " " + name("ая", true) + TCUtils.N + ": " + TCUtils.A + pls.size() + TCUtils.N + " чел."
				: " " + name("ая", true) + TCUtils.N + ": " + TCUtils.P + rsps + TCUtils.N + " возр.";
		}
	}

	public LinkedList<String> lore(final String first) {
		final LinkedList<String> lrs = new LinkedList<>();
		lrs.add(first);
		for (final String s : pls.keySet()) {
			lrs.add(color() + "✦ §7" + s);
		}
		return lrs;
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof Team && ((Team) o).cc == cc;
	}

	@Override
	public int hashCode() {
		return cc;
	}
}
