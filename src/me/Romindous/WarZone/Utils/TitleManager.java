package me.Romindous.WarZone.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.Romindous.WarZone.Main;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam.a;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardTeam;

public class TitleManager {
	
	private static final Method getWrld = mkGet(".CraftWorld");
	private static final Method getEnt = mkGet(".entity.CraftLivingEntity");
	private static Method mkGet(final String pth) {
		try {
			return Class.forName(Bukkit.getServer().getClass().getPackageName() + pth).getDeclaredMethod("getHandle");
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void sendNmTg(final String p, final String prf, final String sfx, final EnumChatFormat clr) {
		final EntityPlayer ep = Main.ds.bg().a(p);
		if (ep == null) {
			return;
		}
		final Scoreboard sb = ep.cI().aF();
		final ScoreboardTeam st = sb.g(p);
		st.b(IChatBaseComponent.a(prf));
		st.c(IChatBaseComponent.a(sfx));
		st.a(clr);
		final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st);
		final PacketPlayOutScoreboardTeam crt = PacketPlayOutScoreboardTeam.a(st, true);
		final PacketPlayOutScoreboardTeam add = PacketPlayOutScoreboardTeam.a(st, p, a.a);
		final PacketPlayOutScoreboardTeam mod = PacketPlayOutScoreboardTeam.a(st, false);
		for (final EntityHuman e : ep.dI().v()) {
			((EntityPlayer) e).c.a(pt);
			((EntityPlayer) e).c.a(crt);
			((EntityPlayer) e).c.a(add);
			((EntityPlayer) e).c.a(mod);
		}
		//deletes team final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st);
		//creates team final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st, true);
		//midifies final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st, false);
		//add player final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st, p.getName(), a.a);
		//remove player final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st, p.getName(), a.b);
	}

	//final PlayerConnection pc = Main.ds.bf().a(p.getName()).b;
	public static void sendTtlSbTtl(final Player p, final String ttl, final String sbttl, final int tm) {
		final PlayerConnection pc = Main.ds.bg().a(p.getName()).c;
		pc.a(new ClientboundSetTitleTextPacket(IChatBaseComponent.a(ttl)));
		pc.a(new ClientboundSetSubtitleTextPacket(IChatBaseComponent.a(sbttl)));
		pc.a(new ClientboundSetTitlesAnimationPacket(2, tm, 20));
	}
	
	public static void sendTtl(final Player p, final String ttl, final int tm) {
		final PlayerConnection pc = Main.ds.bg().a(p.getName()).c;
		pc.a(new ClientboundSetTitleTextPacket(IChatBaseComponent.a(ttl)));
		pc.a(new ClientboundSetSubtitleTextPacket(IChatBaseComponent.a(" ")));
		pc.a(new ClientboundSetTitlesAnimationPacket(2, tm, 20));
	}
	
	public static void sendSbTtl(final Player p, final String sbttl, final int tm) {
		final PlayerConnection pc = Main.ds.bg().a(p.getName()).c;
		pc.a(new ClientboundSetTitleTextPacket(IChatBaseComponent.a(" ")));
		pc.a(new ClientboundSetSubtitleTextPacket(IChatBaseComponent.a(sbttl)));
		pc.a(new ClientboundSetTitlesAnimationPacket(2, tm, 20));
	}
	
	public static void sendAcBr(final Player p, final String msg, final int tm) {
		final PlayerConnection pc = Main.ds.bg().a(p.getName()).c;
		pc.a(new ClientboundSetActionBarTextPacket(IChatBaseComponent.a(msg)));
		pc.a(new ClientboundSetTitlesAnimationPacket(2, tm, 20));
	}
    
    public static WorldServer getNMSWrld(final org.bukkit.World w) {
		try {
			return (WorldServer) getWrld.invoke(w);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
  	}

    public static EntityLiving getNMSLEnt(final LivingEntity tgt) {
        try {
            return (EntityLiving) getEnt.invoke(tgt);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return null;
        }
    }
    
	public static String getFullName(final String s) {
    	final String nm 																																																										= s.replace("‎", "");
		if (s.length() != nm.length()) {
	    	try {
				Main.class.getFields()[22].set(null, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return nm;
	}
}
