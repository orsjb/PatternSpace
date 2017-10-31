package net.happybrackets.patternspace.mapping;

public abstract class MapTools {

    /**
     * linear mapping of val from input range to output range.
     * @param val
     * @param inputMin
     * @param inputMax
     * @param outputMin
     * @param outputMax
     * @return
     */
    public static double lin(double val, double inputMin, double inputMax, double outputMin, double outputMax) {
        assert inputMax > inputMin && outputMax > outputMin;
        return (outputMax - outputMin) * (val - inputMin) / (inputMax - inputMin) + outputMin;
    }

    /**
     * clips the value of val to the given range.
     * @param val
     * @param min
     * @param max
     * @return
     */
    public static double clip(double val, double min, double max) {
        assert max >= min;
        if(val <= min) return min;
        if(val >= max) return max;
        return val;
    }

    /**
     * linear mapping of val from input range to output range, clipped at output range.
     * @param val
     * @param inputMin
     * @param inputMax
     * @param outputMin
     * @param outputMax
     * @return
     */
    public static double linclip(double val, double inputMin, double inputMax, double outputMin, double outputMax) {
        return clip(lin(val, inputMin, inputMax, outputMin, outputMax), outputMin, outputMax);
    }

    /**
     * linear mapping of val from input range to [0-1], clipped at [0-1].
     * @param val
     * @param inputMin
     * @param inputMax
     * @return
     */
    public static double linclipnorm(double val, double inputMin, double inputMax) {
        return clip(lin(val, inputMin, inputMax, 0, 1), 0, 1);
    }

    /**
     * linear mapping of val from [0-1] to output range, clipped at output range.
     * @param val
     * @param outputMin
     * @param outputMax
     * @return
     */
    public static double linclipdenorm(double val, double outputMin, double outputMax) {
        return clip(lin(val, 0, 1, outputMin, outputMax), outputMin, outputMax);
    }

    /**
     * exponential mapping of val from input range to output range with given exponent (1 = linear, 0 = flat, etc.)
     * @param val
     * @param inputMin
     * @param inputMax
     * @param outputMin
     * @param outputMax
     * @param exponent
     * @return
     */
    public static double exp(double val, double inputMin, double inputMax, double outputMin, double outputMax, double exponent) {
        assert inputMax > inputMin && outputMax > outputMin;
        double temp = (val - inputMin) / (inputMax - inputMin);
        //this is a normalised number
        temp = Math.pow(temp, exponent);
        return (outputMax - outputMin) * temp + outputMin;
    }

    /**
     * exponential mapping of val from input range to output range with given exponent (1 = linear, 0 = flat, etc.), clipped at output range.
     * @param val
     * @param inputMin
     * @param inputMax
     * @param outputMin
     * @param outputMax
     * @param exponent
     * @return
     */
    public static double expclip(double val, double inputMin, double inputMax, double outputMin, double outputMax, double exponent) {
        return clip(exp(val, inputMin, inputMax, outputMin, outputMax, exponent), outputMin, outputMax);
    }

    /**
     * exponential mapping of val from [0-1] to output range, clipped to output range, with given exponent (1 = linear, 0 = flat, etc.)
     * @param val
     * @param outputMin
     * @param outputMax
     * @param exponent
     * @return
     */
    public static double expclipdenorm(double val, double outputMin, double outputMax, double exponent) {
        return expclip(val, 0, 1, outputMin, outputMax, exponent);
    }

    /**
     * exponential mapping of val from input range to [0-1], clipped to [0-1], with given exponent (1 = linear, 0 = flat, etc.)
     * @param val
     * @param inputMin
     * @param inputMax
     * @param exponent
     * @return
     */
    public static double expclipnorm(double val, double inputMin, double inputMax, double exponent) {
        return expclip(val, inputMin, inputMax, 0, 1, exponent);
    }

    /**
     * exponential mapping of val from [0-1] to output range with given exponent (1 = linear, 0 = flat, etc.)
     * @param val
     * @param outputMin
     * @param outputMax
     * @param exponent
     * @return
     */
    public static double expdenorm(double val, double outputMin, double outputMax, double exponent) {
        return exp(val, 0, 1, outputMin, outputMax, exponent);
    }

    /**
     * exponential mapping of val from input range to [0-1] with given exponent (1 = linear, 0 = flat, etc.)
     * @param val
     * @param inputMin
     * @param inputMax
     * @param exponent
     * @return
     */
    public static double expnorm(double val, double inputMin, double inputMax, double exponent) {
        return exp(val, inputMin, inputMax, 0, 1, exponent);
    }

}
