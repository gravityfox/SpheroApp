package net.gravityfox.apcsa.sphero.apcsasphero;

import com.flowpowered.math.TrigMath;
import com.flowpowered.math.imaginary.Quaternionf;

/**
 * Created by Fox on 6/1/2016.
 * Project: APCSASphero
 */
public class Util {

    public static Quaternionf quatfFromArray(float[] array) {
        return Quaternionf.from(array[0], array[1], array[2], array[3]);
    }

    public static float wrapAngle(float radians) {
        while (radians < 0) radians += TrigMath.TWO_PI;
        return (float) (radians % TrigMath.TWO_PI);
    }
}
