package me.Romindous.WarZone.Game;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.Romindous.WarZone.Main;
import me.Romindous.WarZone.Utils.EntMeta;

public class MnstrRun extends BukkitRunnable {

	final Location cntr;
	final Random rnd;
	final int mxdst;
	
	public MnstrRun(final Location cntr, final byte mxdst) {
		this.cntr = cntr;
		this.mxdst = mxdst;
		this.rnd = new Random();
	}
	
	@Override
	public void run() {
		//находим подходящую локацию
		Location spwn;
		byte trs = 5;
		do {
			if (trs < 0) {
				return;
			}
			trs--;
			spwn = new Location(cntr.getWorld(), cntr.getBlockX() - mxdst + rnd.nextInt(mxdst * 2) + 0.5, cntr.getBlockY() + 1.5, cntr.getBlockZ() - mxdst + rnd.nextInt(mxdst * 2) + 0.5);
		} while (!spwn.getBlock().isPassable() || !spwn.getBlock().getRelative(BlockFace.UP).isPassable());
		//смотрим если там уже поблизости есть мобы
		for (final Entity e : spwn.getWorld().getNearbyEntities(spwn, Main.tstRds, 3, Main.tstRds)) {
			if (e instanceof LivingEntity && e.getType() != EntityType.PLAYER) {
				return;
			}
		}
		//лвл моба
		final float lb = -Main.lvlbfr * (float) mxdst * (float) cntr.distance(spwn) + Main.maxlvl;
		final byte lvl = lb < 0f ? 0 : (byte) (lb);
		//спавн моба
		final LivingEntity ent = (LivingEntity) spwn.getWorld().spawnEntity(spwn, rndEntTp(lvl, rnd));
		spwn.getWorld().spawnParticle(Particle.SOUL, spwn, 20 * lvl, 0.6d, 0.8d, 0.6d, 0, null, false);
		ent.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 1000000, 0, true, false));
		final EntityEquipment eq = ent.getEquipment();
		//запоминаем лвл
		EntMeta.chngMeta(ent, "lvl", lvl);
		ent.setTicksLived(1);
		//даем мобу екипировку
		if (ent instanceof Zombie) {
			//оружие
			if ((float) lvl * Main.forWpnScr < 1) {
				eq.setItemInMainHand(new ItemStack(Material.AIR));
				return;
			} else if (rnd.nextBoolean()) {
				eq.setItemInMainHand(randEnch(new ItemStack(Material.getMaterial(getWpnPrfx(lvl) + "_AXE")), rnd, EnchantmentTarget.TOOL, lvl));
			} else {
				eq.setItemInMainHand(randEnch(new ItemStack(Material.getMaterial(getWpnPrfx(lvl) + "_SWORD")), rnd, EnchantmentTarget.WEAPON, lvl));
			}
			//бронь
			addArmr(eq, lvl, rnd);
			//баффы
			addStts(ent, lvl);
		} else {
			//оружие
			if ((float) lvl * Main.forWpnScr < 1) {
				eq.setItemInMainHand(new ItemStack(Material.AIR));
				return;
			} else if (rnd.nextBoolean()) {
				eq.setItemInMainHand(randEnch(new ItemStack(Material.BOW), rnd, EnchantmentTarget.BOW, lvl));
			} else {
				eq.setItemInMainHand(randEnch(new ItemStack(Material.getMaterial(getWpnPrfx(lvl) + "_SWORD")), rnd, EnchantmentTarget.WEAPON, lvl));
			}
			//бронь
			addArmr(eq, lvl, rnd);
			//баффы
			addStts(ent, lvl);
		}
	}

	//какой моб?
	private EntityType rndEntTp(final byte lvl, final Random rnd) {
		//легкие мобы
		if (lvl < Main.maxlvl / 3f) {
			return rnd.nextBoolean() ? EntityType.DROWNED : EntityType.SKELETON;
		//сложные мобы
		} else if (lvl > Main.maxlvl * 2f / 3f) {
			return rnd.nextBoolean() ? EntityType.HUSK : EntityType.WITHER_SKELETON;
		//нормальные мобы
		} else {
			return rnd.nextBoolean() ? EntityType.ZOMBIE : EntityType.STRAY;
		}
	}
	
	//накладываем зачары в зависимости от лвла
	private ItemStack randEnch(final ItemStack it, final Random rnd, final EnchantmentTarget et, final byte lvl) {
		byte j = (byte) ((float) lvl * Main.forEnchScr);
		if (j > 0 && rnd.nextInt(j + Main.enchThr) < j) {
			final ItemMeta mt = it.getItemMeta();
			//какой тип предмета?
			switch (et) {
			case TOOL:
				while (j != 0) {
					switch (rnd.nextInt(3)) {
					case 0:
						if (mt.hasEnchant(Enchantment.DIG_SPEED)) {
							mt.addEnchant(Enchantment.DIG_SPEED, mt.getEnchantLevel(Enchantment.DIG_SPEED) + 1, true);
						} else {
							mt.addEnchant(Enchantment.DIG_SPEED, 1, true);
						}
						j--;
						break;
					case 1:
						if (mt.hasEnchant(Enchantment.DURABILITY)) {
							mt.addEnchant(Enchantment.DURABILITY, mt.getEnchantLevel(Enchantment.DURABILITY) + 1, true);
						} else {
							mt.addEnchant(Enchantment.DURABILITY, 1, true);
						}
						j--;
						break;
					case 2:
						if (mt.hasEnchant(Enchantment.LOOT_BONUS_BLOCKS)) {
							mt.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, mt.getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS) + 1, true);
						} else {
							mt.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 1, true);
						}
						j--;
						break;
					}
				}
				break;
			case WEAPON:
				while (j != 0) {
					switch (rnd.nextInt(3)) {
					case 0:
						if (mt.hasEnchant(Enchantment.DAMAGE_ALL)) {
							mt.addEnchant(Enchantment.DAMAGE_ALL, mt.getEnchantLevel(Enchantment.DAMAGE_ALL) + 1, true);
						} else {
							mt.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
						}
						j--;
						break;
					case 1:
						if (mt.hasEnchant(Enchantment.DURABILITY)) {
							mt.addEnchant(Enchantment.DURABILITY, mt.getEnchantLevel(Enchantment.DURABILITY) + 1, true);
						} else {
							mt.addEnchant(Enchantment.DURABILITY, 1, true);
						}
						j--;
						break;
					case 2:
						if (mt.hasEnchant(Enchantment.FIRE_ASPECT)) {
							mt.addEnchant(Enchantment.FIRE_ASPECT, mt.getEnchantLevel(Enchantment.FIRE_ASPECT) + 1, true);
						} else {
							mt.addEnchant(Enchantment.FIRE_ASPECT, 1, true);
						}
						j--;
						break;
					}
				}
				break;
			case BOW:
				while (j != 0) {
					switch (rnd.nextInt(3)) {
					case 0:
						if (mt.hasEnchant(Enchantment.ARROW_DAMAGE)) {
							mt.addEnchant(Enchantment.ARROW_DAMAGE, mt.getEnchantLevel(Enchantment.ARROW_DAMAGE) + 1, true);
						} else {
							mt.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
						}
						j--;
						break;
					case 1:
						if (mt.hasEnchant(Enchantment.DURABILITY)) {
							mt.addEnchant(Enchantment.DURABILITY, mt.getEnchantLevel(Enchantment.DURABILITY) + 1, true);
						} else {
							mt.addEnchant(Enchantment.DURABILITY, 1, true);
						}
						j--;
						break;
					case 2:
						if (mt.hasEnchant(Enchantment.ARROW_FIRE)) {
							mt.addEnchant(Enchantment.ARROW_FIRE, mt.getEnchantLevel(Enchantment.ARROW_FIRE) + 1, true);
						} else {
							mt.addEnchant(Enchantment.ARROW_FIRE, 1, true);
						}
						j--;
						break;
					}
				}
				break;
			case ARMOR:
				while (j != 0) {
					switch (rnd.nextInt(3)) {
					case 0:
						if (mt.hasEnchant(Enchantment.PROTECTION_ENVIRONMENTAL)) {
							mt.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, mt.getEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL) + 1, true);
						} else {
							mt.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
						}
						j--;
						break;
					case 1:
						if (mt.hasEnchant(Enchantment.DURABILITY)) {
							mt.addEnchant(Enchantment.DURABILITY, mt.getEnchantLevel(Enchantment.DURABILITY) + 1, true);
						} else {
							mt.addEnchant(Enchantment.DURABILITY, 1, true);
						}
						j--;
						break;
					case 2:
						if (mt.hasEnchant(Enchantment.THORNS)) {
							mt.addEnchant(Enchantment.THORNS, mt.getEnchantLevel(Enchantment.THORNS) + 1, true);
						} else {
							mt.addEnchant(Enchantment.THORNS, 1, true);
						}
						j--;
						break;
					}
				}
				break;

			default:
				break;
			}
			it.setItemMeta(mt);
		}
		return it;
	}
	//--
	//префикс оружия
	private String getWpnPrfx(final byte lvl) {
		switch ((int) ((float) lvl * Main.forWpnScr + 1)) {
		case 1:
			return "WOODEN";
		case 2:
			return "STONE";
		case 3:
			return "GOLDEN";
		case 4:
			return "IRON";
		case 5:
			return "DIAMOND";
		default:
			return "NETHERITE";
		}
	}
	//--
	//префикс брони
	private String getArmrPrfx(final byte i) {
		switch (i) {
		case 1:
			return "LEATHER";
		case 2:
			return "GOLDEN";
		case 3:
			return "CHAINMAIL";
		case 4:
			return "IRON";
		case 5:
			return "DIAMOND";
		default:
			return "NETHERITE";
		}
	}
	//--
	//броня
	private void addArmr(final EntityEquipment eq, final byte lvl, final Random rnd) {
		final byte i = (byte) ((float) lvl * Main.forArmrScr);
		if (i > 0) {
			if (rnd.nextInt(i + Main.armrThr) < i) {
				eq.setHelmet(randEnch(new ItemStack(Material.getMaterial(getArmrPrfx(i) + "_HELMET")), rnd, EnchantmentTarget.ARMOR, i));
			}
			if (rnd.nextInt(i + Main.armrThr) < i) {
				eq.setChestplate(randEnch(new ItemStack(Material.getMaterial(getArmrPrfx(i) + "_CHESTPLATE")), rnd, EnchantmentTarget.ARMOR, i));
			}
			if (rnd.nextInt(i + Main.armrThr) < i) {
				eq.setLeggings(randEnch(new ItemStack(Material.getMaterial(getArmrPrfx(i) + "_LEGGINGS")), rnd, EnchantmentTarget.ARMOR, i));
			}
			if (rnd.nextInt(i + Main.armrThr) < i) {
				eq.setBoots(randEnch(new ItemStack(Material.getMaterial(getArmrPrfx(i) + "_BOOTS")), rnd, EnchantmentTarget.ARMOR, i));
			}
		}
	}
	//--
	//повышения хп и скорости
	private void addStts(final LivingEntity ent, final byte lvl) {
		ent.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(Main.flwRng);
		ent.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(ent.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() + lvl * Main.forSpdScr);
		ent.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(lvl * Main.forHlthScr + 20);
		ent.setHealth(lvl * Main.forHlthScr + 20);
	}
	//--
}