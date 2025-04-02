package fr.loudo.narrativecraft.utils;

import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import net.minecraft.world.phys.Vec3;

public class MovementUtils {

    public static Vec3 getDeltaMovement(MovementData loc1, MovementData loc2) {
        double dX, dY, dZ;
        dX = loc2.getX() - loc1.getX();
        dY = loc2.getY() - loc1.getY();
        dZ = loc2.getZ() - loc1.getZ();

        return new Vec3(dX, dY, dZ);

    }

}
