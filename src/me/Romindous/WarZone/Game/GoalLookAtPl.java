package me.Romindous.WarZone.Game;

import java.util.EnumSet;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;

import me.Romindous.WarZone.Main;
import ru.komiss77.modules.world.XYZ;

public class GoalLookAtPl implements Goal<Mob> {
	
    private final Mob mob;
    private final GoalKey<Mob> key;
    private final XYZ pos;
    
    private int tick;
    
    public GoalLookAtPl(final Mob mob) {
        this.key = GoalKey.of(Mob.class, new NamespacedKey(Main.plug, "site"));
        this.pos = new XYZ(mob.getLocation());
        this.mob = mob;
        this.tick = 0;
    }

	@Override
	public GoalKey<Mob> getKey() {
		return key;
	}

	@Override
	public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE);
	}

	@Override
	public boolean shouldActivate() {
		return true;
	}
	
	@Override
	public void tick() {
		if (((tick++) & 7) == 0) {
			int dst = Integer.MAX_VALUE;
			Player pl = null;
			for (final Player p : mob.getWorld().getPlayers()) {
				final Location l = p.getLocation();
				final int d = Math.abs(pos.x - l.getBlockX()) + Math.abs(pos.y - l.getBlockY()) + Math.abs(pos.z - l.getBlockZ());
				if (d < dst) {
					dst = d;
					pl = p;
				}
			}
			
			if (pl != null) {
				mob.lookAt(pl);
			}
		}
	}  

}
