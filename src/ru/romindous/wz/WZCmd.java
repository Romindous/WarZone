package ru.romindous.wz;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ru.komiss77.modules.world.WXYZ;
import ru.romindous.wz.Game.Arena;
import ru.romindous.wz.Game.GameState;
import ru.romindous.wz.Game.Setup;

public class WZCmd implements CommandExecutor, TabCompleter{

	private final Main plug;
	
	public WZCmd(final Main plug) {
		this.plug = plug;
	}
	
	@Override
	public List<String> onTabComplete(final CommandSender send, final Command cmd, final String al, final String[] args) {
		final LinkedList<String> sugg = new LinkedList<>();
		if (send instanceof final Player p) {
            if (p.hasPermission("ostrov.builder")) {
				if (args.length == 1) {
					sugg.add("join");
					sugg.add("leave");
					sugg.add("help");
					sugg.add("create");
					sugg.add("addteam");
					sugg.add("addshop");
					sugg.add("finish");
					sugg.add("delete");
					sugg.add("setcenter");
					sugg.add("setlobby");
					sugg.add("reload");
				} else if (args.length == 2 && (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("delete"))) {
					sugg.addAll(Main.nonactivearenas.keySet());
				} else if (args.length == 2 && (args[0].equalsIgnoreCase("addteam") || args[0].equalsIgnoreCase("addshop") || args[0].equalsIgnoreCase("setcenter") || args[0].equalsIgnoreCase("finish"))) {
					for (final Entry<String, Setup> e : Main.nonactivearenas.entrySet()) {
						if (e.getValue().fin) {
							sugg.add(e.getKey());
						}
					}
				}
			} else {
				if (args.length == 1) {
					sugg.add("join");
					sugg.add("leave");
					sugg.add("help");
				} else if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
					sugg.addAll(Main.nonactivearenas.keySet());
				}
			}
		}
		return sugg;
	}

	@Override
	public boolean onCommand(final CommandSender send, final Command cmd, final String label, final String[] args) {
		if (label.equalsIgnoreCase("wz") && send instanceof final Player p) {
            final YamlConfiguration ars = YamlConfiguration.loadConfiguration(new File(Main.folder + File.separator + "arenas.yml"));
			//админ комманды
			if (p.hasPermission("ostrov.builder")) {
				//создание карты
				if (args.length == 4 && args[0].equalsIgnoreCase("create") && !ars.contains("arenas." + args[1])) {
					p.sendMessage(Main.PRFX + "Начинаем cоздание арены §2" + args[1] + "§7:");
					final byte min;
					final byte max;
					//проверка на число
					try {
						min = Byte.parseByte(args[2]);
						max = Byte.parseByte(args[3]);
					} catch (NumberFormatException e) {
						p.sendMessage(Main.PRFX + "§cПосле названия надо вписать 2 числа!");
						return false;
					}
					if (min >= 2 && min <= max) {
						//добавляем арену
						ars.set("arenas." + args[1] + ".min", min);
						ars.set("arenas." + args[1] + ".max", max);
						ars.set("arenas." + args[1] + ".world", p.getWorld().getName());
						try {
							ars.save(new File(Main.folder + File.separator + "arenas.yml"));
						} catch (IOException e) {
							e.printStackTrace();
						}
						p.sendMessage(Main.PRFX + "Минимальное кол-во игроков: §2" + min);
						p.sendMessage(Main.PRFX + "Максимальное кол-во игроков: §2" + max);
						return true;
					} else {
						p.sendMessage(Main.PRFX + "§cПервое число должно быть меньше или равно второму и быть более 2!");
						return false;
					}
				} else if (args.length == 2) {
					//добавление комманд (teams)
					if (args[0].equalsIgnoreCase("addteam") && ars.contains("arenas." + args[1]) && !ars.contains("arenas." + args[1] + ".fin")) {
						if (ars.contains("arenas." + args[1] + ".teams")) {
							if (ars.getString("arenas." + args[1] + ".teams.x").split(":").length < 8) {
								ars.set("arenas." + args[1] + ".teams.x", ars.getString("arenas." + args[1] + ".teams.x") + ':' + p.getLocation().getBlockX());
								ars.set("arenas." + args[1] + ".teams.y", ars.getString("arenas." + args[1] + ".teams.y") + ':' + p.getLocation().getBlockY());
								ars.set("arenas." + args[1] + ".teams.z", ars.getString("arenas." + args[1] + ".teams.z") + ':' + p.getLocation().getBlockZ());
								
								p.sendMessage(Main.PRFX + "Новая комманда была создана, с центром на координатах (§2" + p.getLocation().getBlockX() + "§7, §2" + p.getLocation().getBlockY() + "§7, §2" + p.getLocation().getBlockZ() + "§7)!");
								
								try {
									ars.save(new File(Main.folder + File.separator + "arenas.yml"));
								} catch (IOException e) {
									e.printStackTrace();
								}
								
								return true;
							} else {
								p.sendMessage(Main.PRFX + "§cВы уже создали достаточно комманд для этой карты!");
								return false;
							}
						} else {
							ars.set("arenas." + args[1] + ".teams.x", p.getLocation().getBlockX());
							ars.set("arenas." + args[1] + ".teams.y", p.getLocation().getBlockY());
							ars.set("arenas." + args[1] + ".teams.z", p.getLocation().getBlockZ());
							
							p.sendMessage(Main.PRFX + "Новая комманда была создана, с центром на координатах (§2" + p.getLocation().getBlockX() + "§7, §2" + p.getLocation().getBlockY() + "§7, §2" + p.getLocation().getBlockZ() + "§7)!");
							
							try {
								ars.save(new File(Main.folder + File.separator + "arenas.yml"));
							} catch (IOException e) {
 
								e.printStackTrace();
							}
							
							return true;
						}
					} else if (args[0].equalsIgnoreCase("addshop") && ars.contains("arenas." + args[1]) && !ars.contains("arenas." + args[1] + ".fin")) {
						if (ars.contains("arenas." + args[1] + ".shops")) {
							if (ars.getString("arenas." + args[1] + ".shops.x").split(":").length < 8) {
								ars.set("arenas." + args[1] + ".shops.x", ars.getString("arenas." + args[1] + ".shops.x") + ':' + p.getLocation().getBlockX());
								ars.set("arenas." + args[1] + ".shops.y", ars.getString("arenas." + args[1] + ".shops.y") + ':' + p.getLocation().getBlockY());
								ars.set("arenas." + args[1] + ".shops.z", ars.getString("arenas." + args[1] + ".shops.z") + ':' + p.getLocation().getBlockZ());
								
								p.sendMessage(Main.PRFX + "Новый магазин был создан, на координатах (§2" + p.getLocation().getBlockX() + "§7, §2" + p.getLocation().getBlockY() + "§7, §2" + p.getLocation().getBlockZ() + "§7)!");
								
								try {
									ars.save(new File(Main.folder + File.separator + "arenas.yml"));
								} catch (IOException e) {
									e.printStackTrace();
								}
								
								return true;
							} else {
								p.sendMessage(Main.PRFX + "§cВы уже создали достаточно магазинов на этой карте");
								return false;
							}
						} else {
							ars.set("arenas." + args[1] + ".shops.x", p.getLocation().getBlockX());
							ars.set("arenas." + args[1] + ".shops.y", p.getLocation().getBlockY());
							ars.set("arenas." + args[1] + ".shops.z", p.getLocation().getBlockZ());
							
							p.sendMessage(Main.PRFX + "Новый магазин был создан, на координатах (§2" + p.getLocation().getBlockX() + "§7, §2" + p.getLocation().getBlockY() + "§7, §2" + p.getLocation().getBlockZ() + "§7)!");
							
							try {
								ars.save(new File(Main.folder + File.separator + "arenas.yml"));
							} catch (IOException e) {
 
								e.printStackTrace();
							}
							
							return true;
						}
						//окончание разработки карты	
					} else if (args[0].equalsIgnoreCase("setcenter") && ars.contains("arenas." + args[1]) && !ars.contains("arenas." + args[1] + ".fin") && !ars.contains("arenas." + args[1] + ".cntr")) {

						ars.set("arenas." + args[1] + ".pbs.x", p.getLocation().getBlockX());
						ars.set("arenas." + args[1] + ".pbs.y", p.getLocation().getBlockY());
						ars.set("arenas." + args[1] + ".pbs.z", p.getLocation().getBlockZ());
						
						ars.set("arenas." + args[1] + ".cntr.x", p.getLocation().getBlockX());
						ars.set("arenas." + args[1] + ".cntr.y", p.getLocation().getBlockY());
						ars.set("arenas." + args[1] + ".cntr.z", p.getLocation().getBlockZ());
						
						p.sendMessage(Main.PRFX + "Центр на карте §2" + args[1] + " §7поставлен!");
						
						try {
							ars.save(new File(Main.folder + File.separator + "arenas.yml"));
						} catch (IOException e) {
							e.printStackTrace();
						}
						//окончание разработки карты	
					} else if (args[0].equalsIgnoreCase("finish") && ars.contains("arenas." + args[1]) && !ars.contains("arenas." + args[1] + ".fin")) {
						if (ars.contains("arenas." + args[1] + ".teams") && ars.getString("arenas." + args[1] + ".teams.x").split(":").length > 1 && ars.contains("arenas." + args[1] + ".shops") && ars.getString("arenas." + args[1] + ".shops.x").split(":").length > 1 && ars.contains("arenas." + args[1] + ".cntr")) {
							final ItemStack[] its = p.getInventory().getContents();
							for (byte i = 1; i < 5; i++) {
								if (its[i] == null || its[i].getType() == Material.AIR) {
									p.sendMessage(Main.PRFX + "§cВы должны держать *определенные* предметы в хотбаре");
									return true;
								}
								switch (i) {
								//инструмент
								case 1:
									ars.set("arenas." + args[1] + ".tl", its[i].getType().toString().substring(6));
									break;
								//ресурсы
								case 2:
									ars.set("arenas." + args[1] + ".mnbl", its[i].getType().toString());
									p.getLocation().getBlock().setType(its[i].getType());
									ars.set("arenas." + args[1] + ".recs", p.getLocation().getBlock().getDrops().iterator().next().getType().toString());
									break;
								case 3:
									ars.set("arenas." + args[1] + ".mnbl", ars.getString("arenas." + args[1] + ".mnbl") + ':' + its[i].getType().toString());
									p.getLocation().getBlock().setType(its[i].getType());
									ars.set("arenas." + args[1] + ".recs", ars.getString("arenas." + args[1] + ".recs") + ':' + p.getLocation().getBlock().getDrops().iterator().next().getType().toString());
									break;
								case 4:
									ars.set("arenas." + args[1] + ".mnbl", ars.getString("arenas." + args[1] + ".mnbl") + ':' + its[i].getType().toString());
									p.getLocation().getBlock().setType(its[i].getType());
									ars.set("arenas." + args[1] + ".recs", ars.getString("arenas." + args[1] + ".recs") + ':' + p.getLocation().getBlock().getDrops().iterator().next().getType().toString());
									p.getLocation().getBlock().setType(Material.AIR);
									break;
								}
							}
							ars.set("arenas." + args[1] + ".fin", 1);
							Main.nonactivearenas.put(args[1], new Setup(ars.getConfigurationSection("arenas." + args[1])));
							p.sendMessage(Main.PRFX + "Карта §2" + args[1] + " §7успешно создана!");
							try {
								ars.save(new File(Main.folder + File.separator + "arenas.yml"));
							} catch (IOException e) {
								e.printStackTrace();
							}
							return true;
						} else {
							p.sendMessage(Main.PRFX + "§cСоздайте хотя бы две комманды (с магазинами) и центр для этой карты!");
							return false;
						}
						//удаление карты
					} else if (args[0].equalsIgnoreCase("delete") && ars.contains("arenas." + args[1])) {
						ars.set("arenas." + args[1], null);
						Main.nonactivearenas.remove(args[1]);
						p.sendMessage(Main.PRFX + "Карта §2" + args[1] + "§7 успешно удалена!");
						try {
							ars.save(new File(Main.folder + File.separator + "arenas.yml"));
						} catch (IOException e) {
							e.printStackTrace();
						}
						return true;
					} else if (!args[0].equalsIgnoreCase("join")){
						p.sendMessage(Main.PRFX + "§cНеправельный синтакс комманды, все комманды - §2/wz help");
						return false;
					}
					//установка лобби
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("setlobby")) {
						Main.lobby = new WXYZ(p.getLocation());
						ars.set("lobby.world", Main.lobby.w.getName());
						ars.set("lobby.x", Main.lobby.x);
						ars.set("lobby.y", Main.lobby.y);
						ars.set("lobby.z", Main.lobby.z);
						p.sendMessage(Main.PRFX + "Точка лобби сохранена на " + 
							"(§2" + p.getLocation().getBlockX() + "§7, §2" + p.getLocation().getBlockY() + "§7, §2" + p.getLocation().getBlockZ() + "§7)!");
						try {
							ars.save(new File(Main.folder + File.separator + "arenas.yml"));
						} catch (IOException e) {
							e.printStackTrace();
						}
						return true;
						//перезапуск конфига
					} else if (args[0].equalsIgnoreCase("reload")) {
						plug.loadConfigs();
						p.sendMessage(Main.PRFX + "Конфиги плагина успешно перезагружены!");
						return true;
					} else if (!args[0].equalsIgnoreCase("join") && !args[0].equalsIgnoreCase("leave") && !args[0].equalsIgnoreCase("help")) {
						p.sendMessage(Main.PRFX + "§cНеправельный синтакс комманды, все комманды - §2/wz help");
						return false;
					}
				} else {
					p.sendMessage(Main.PRFX + "§cНеправельный синтакс комманды, все комманды - §2/wz help");
					return false;
				}
			}
			
			//общие комманды
			if (ars.contains("lobby")) {
				//добавление на карту
				if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
					if (Arena.getPlArena(p) == null) {
						final Arena ar = Main.activearenas.get(args[1]);
						if (ar == null) {
							if (Main.nonactivearenas.containsKey(args[1])) {
								final Arena a = Main.createArena(args[1]);
								Main.activearenas.put(args[1], a);
								a.addPl(p);
								return true;
							}
							p.sendMessage(Main.PRFX + "§cТакой карты не существует!");
							return false;
						}
						ar.addPl(p);
						return true;
					}
					p.sendMessage(Main.PRFX + "§cТы уже на карте, используйте §9/wz leave§c для выхода!");
					return false;
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("join")) {
						if (Arena.getPlArena(p) == null) {
							final Arena ba = biggestArena(Main.activearenas.values());
							if (ba == null) {
								if (Main.nonactivearenas.size() > 0) {
									final Arena ar = Main.createArena(Main.nonactivearenas.keySet().iterator().next());
									Main.activearenas.put(ar.getName(), ar);
									ar.addPl(p);
									return true;
								}
								p.sendMessage(Main.PRFX + "§cНи одной карты еще не создано!");
								return false;
							}
							ba.addPl(p);
							return true;
						}
						p.sendMessage(Main.PRFX + "§cТы уже на карте, используйте §9/wz leave§c для выхода!");
						return false;
						//выход с карты
					} else if (args[0].equalsIgnoreCase("leave")) {
						final Arena ar = Arena.getPlArena(p);
						if (ar == null) {
							p.sendMessage(Main.PRFX + "§cТы не находишься в игре!");
							return false;
						}

						ar.remPl(p);
						return true;
						//помощь
					} else if (args[0].equalsIgnoreCase("help")) {
						if (p.hasPermission("ostrov.builder")) {
							p.sendMessage("§2-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
							p.sendMessage("§2Помощь по коммандам:");
							p.sendMessage("§2/wz join (название) §7- присоединится к игре");
							p.sendMessage("§2/wz leave §7- выход из игры");
							p.sendMessage("§2/wz help §7- этот текст");
							p.sendMessage("§2/wz create название [мин. кол-во игроков] [макс. кол-во игроков] §7- создание карты");
							p.sendMessage("§2/wz addteam название §7- добавить комманду на карту");
							p.sendMessage("§2/wz addcenter название §7- добавить центр на карту");
							p.sendMessage("§2/wz finish название §7- окончание разработки карты");
							p.sendMessage("§2/wz delete название §7- удвление карты");
							p.sendMessage("§2/wz setlobby §7- установка лобби");
							p.sendMessage("§2/wz reload §7- перезагрузка конфигов");
							p.sendMessage("§2-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
							return true;
						}
						p.sendMessage("§2-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
						p.sendMessage("§2Помощь по коммандам:");
						p.sendMessage("§2/wz join (название) §7- присоединится к игре");
						p.sendMessage("§2/wz leave §7- выход из игры");
						p.sendMessage("§2/wz help §7- этот текст");
						p.sendMessage("§2-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
						return true;
					}
				}
			} else {
				p.sendMessage(Main.PRFX + "§cСначала поставте точку лобби с помощью §9/wz setlobby");
				return false;
			}
		}
		return false;
	}

	//арена на которой больше всего игроков
	public Arena biggestArena(final Collection<Arena> ars) {
		Arena ret = null;

		boolean one = true;
		for (final Arena ar : ars) {
			if (ar.getState() == GameState.WAITING) {
				if (one) {
					ret = ar;
					one = false;
				} else {
					ret = ar.getPls().size() > ret.getPls().size() ? ar : ret;
				}
			}
		}
		
		return ret;
	}

}
