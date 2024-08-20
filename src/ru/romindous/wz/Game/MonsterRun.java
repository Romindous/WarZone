package ru.romindous.wz.Game;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.modules.world.LocFinder;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.FastMath;
import ru.komiss77.utils.LocUtil;
import ru.romindous.wz.Main;

public class MonsterRun extends BukkitRunnable {

	final Arena ar;
	final int mxdst;
	
	public MonsterRun(final Arena ar, final int mxdst) {
		this.ar = ar;
		this.mxdst = mxdst;
	}
	
	@Override
	public void run() {
		//находим подходящую локацию
		final double rfx = (Main.srnd.nextDouble() - 0.5d) * FastMath.square(Main.tstRds);
		final double rfz = (Main.srnd.nextDouble() - 0.5d) * FastMath.square(Main.tstRds);
		final double rdl = Main.srnd.nextDouble() * 0.6d + 0.2d;
		final Location cntr = ar.getCntr().getCenterLoc();
		for (final PlWarrior pw : ar.getPls().values()) {
			final Location loc = pw.getPlayer().getEyeLocation();
			LocFinder.onAsyncFind(new WXYZ(loc.getWorld(), (int) ((loc.getX() - cntr.getX()) * rdl + cntr.getX() + rfx),
				cntr.getBlockY(), (int) ((loc.getZ() - cntr.getZ()) * rdl + cntr.getZ() + rfz)),
				LocFinder.DEFAULT_CHECKS, true, 3, 1, spwn -> {
				//смотрим если там уже поблизости есть мобы
				if (!LocUtil.getChEnts(spwn, Main.tstRds, Mob.class, m -> true).isEmpty()) return;
				//лвл моба
				final byte lvl = (byte) (Math.max(Main.maxlvl - (Main.maxlvl * ar.getCntr().distAbs(spwn) / mxdst), 0));
				//спавн моба
				final LivingEntity ent = (LivingEntity) spwn.w.spawnEntity(spwn.getCenterLoc(), rndEntTp(lvl));
				spwn.w.spawnParticle(Particle.SOUL, spwn.getCenterLoc(), 20 * lvl, 0.6d, 0.8d, 0.6d, 0d);
				ent.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,
					1000000, 0, true, false, false));
				final EntityEquipment eq = ent.getEquipment();
				//запоминаем лвл
				ent.setMetadata(Arena.LVL, new FixedMetadataValue(Main.plug, lvl));
				//даем мобу екипировку
				if (eq == null) return;
				if (ent instanceof Zombie) {
					//оружие
					if ((float) lvl * Main.forWpnScr < 1) {
						eq.setItemInMainHand(new ItemStack(Material.AIR));
						return;
					} else {
						eq.setItemInMainHand(randEnch(new ItemStack(Material.getMaterial(getWpnPrfx(lvl) + "_AXE")), EnchantmentTarget.TOOL, lvl));
					}
				} else {
					//оружие
					if ((float) lvl * Main.forWpnScr < 1) {
						eq.setItemInMainHand(new ItemStack(Material.AIR));
						return;
					} else if (Main.srnd.nextBoolean()) {
						eq.setItemInMainHand(randEnch(new ItemStack(Material.BOW), EnchantmentTarget.BOW, lvl));
					} else {
						eq.setItemInMainHand(randEnch(new ItemStack(Material.getMaterial(getWpnPrfx(lvl) + "_SWORD")), EnchantmentTarget.WEAPON, lvl));
					}
				}
				//бронь
				addArmr(eq, lvl);
				//баффы
				addStts(ent, lvl);

			});
		}
    }

	//какой моб?
	private EntityType rndEntTp(final byte lvl) {
		//легкие мобы
		if (lvl < Main.maxlvl * 0.3f) {
			return Main.srnd.nextBoolean() ? EntityType.DROWNED : EntityType.SKELETON;
		//сложные мобы
		} else if (lvl > Main.maxlvl * 0.6f) {
			return Main.srnd.nextBoolean() ? EntityType.HUSK : EntityType.WITHER_SKELETON;
		//нормальные мобы
		} else {
			return Main.srnd.nextBoolean() ? EntityType.ZOMBIE : EntityType.STRAY;
		}
	}

	//накладываем зачары в зависимости от лвла
	private ItemStack randEnch(final ItemStack it, final EnchantmentTarget et, final int lvl) {
		int j = (int) (Main.forEnchScr * lvl);
		if (j > 0 && Main.srnd.nextInt(j + Main.enchThr) < j) {
			final ItemMeta mt = it.getItemMeta();
			//какой тип предмета?
			switch (et) {
			case TOOL:
				while (j != 0) {
                    switch (Main.srnd.nextInt(3)) {
                        case 0 -> incrEnch(mt, Enchantment.EFFICIENCY);
                        case 1 -> incrEnch(mt, Enchantment.UNBREAKING);
                        case 2 -> incrEnch(mt, Enchantment.FORTUNE);
                    }
					j--;
				}
				break;
			case WEAPON:
				while (j != 0) {
                    switch (Main.srnd.nextInt(3)) {
                        case 0 -> incrEnch(mt, Enchantment.SHARPNESS);
                        case 1 -> incrEnch(mt, Enchantment.KNOCKBACK);
                        case 2 -> incrEnch(mt, Enchantment.FIRE_ASPECT);
                    }
					j--;
				}
				break;
			case BOW:
				while (j != 0) {
                    switch (Main.srnd.nextInt(3)) {
                        case 0 -> incrEnch(mt, Enchantment.POWER);
                        case 1 -> incrEnch(mt, Enchantment.PUNCH);
                        case 2 -> incrEnch(mt, Enchantment.FLAME);
                    }
					j--;
				}
				break;
			case ARMOR:
				while (j != 0) {
                    switch (Main.srnd.nextInt(3)) {
                        case 0 -> incrEnch(mt, Enchantment.PROTECTION);
                        case 1 -> incrEnch(mt, Enchantment.PROJECTILE_PROTECTION);
                        case 2 -> incrEnch(mt, Enchantment.THORNS);
                    }
					j--;
				}
				break;

			default:
				break;
			}
			it.setItemMeta(mt);
		}
		return it;
	}

	private void incrEnch(final ItemMeta mt, final Enchantment ench) {
		if (mt.hasEnchant(ench)) {
			mt.addEnchant(ench, mt.getEnchantLevel(ench) + 1, true);
		} else {
			mt.addEnchant(ench, 1, true);
		}
	}

	//--
	//префикс оружия
	private String getWpnPrfx(final int lvl) {
        return switch ((int) (lvl * Main.forWpnScr + 1f)) {
            case 1 -> "WOODEN";
            case 2 -> "STONE";
            case 3 -> "GOLDEN";
            case 4 -> "IRON";
            case 5 -> "DIAMOND";
            default -> "NETHERITE";
        };
	}
	//--
	//префикс брони
	private String getArmrPrfx(final int i) {
        return switch (i) {
            case 1 -> "LEATHER";
            case 2 -> "GOLDEN";
            case 3 -> "CHAINMAIL";
            case 4 -> "IRON";
            case 5 -> "DIAMOND";
            default -> "NETHERITE";
        };
	}
	//--
	//броня
	private void addArmr(final EntityEquipment eq, final byte lvl) {
		final int i = (int) (Main.forArmrScr * lvl);
		if (i > 0) {
			if (Main.srnd.nextInt(i + Main.armrThr) < i) {
				eq.setHelmet(randEnch(new ItemStack(Material.getMaterial(getArmrPrfx(i) + "_HELMET")), EnchantmentTarget.ARMOR, i));
			}
			if (Main.srnd.nextInt(i + Main.armrThr) < i) {
				eq.setChestplate(randEnch(new ItemStack(Material.getMaterial(getArmrPrfx(i) + "_CHESTPLATE")), EnchantmentTarget.ARMOR, i));
			}
			if (Main.srnd.nextInt(i + Main.armrThr) < i) {
				eq.setLeggings(randEnch(new ItemStack(Material.getMaterial(getArmrPrfx(i) + "_LEGGINGS")), EnchantmentTarget.ARMOR, i));
			}
			if (Main.srnd.nextInt(i + Main.armrThr) < i) {
				eq.setBoots(randEnch(new ItemStack(Material.getMaterial(getArmrPrfx(i) + "_BOOTS")), EnchantmentTarget.ARMOR, i));
			}
		}
	}
	//--
	//повышения хп и скорости
	private void addStts(final LivingEntity ent, final byte lvl) {
		ent.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(Main.flwRng);
		ent.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(ent
			.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() + lvl * Main.forSpdScr);
		ent.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(lvl * Main.forHlthScr + 20);
		ent.setHealth(lvl * Main.forHlthScr + 20f);
	}
	//--
}