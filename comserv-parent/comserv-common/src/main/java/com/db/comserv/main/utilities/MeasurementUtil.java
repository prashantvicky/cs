/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.utilities;

/**
 * This class contains utility methods for measurements.
 */
public final class MeasurementUtil {
    private static final double BYTES_IN_MEGABIT = 131072;
    private static final double MILLISECONDS_IN_SECONDS = 1000d;
    private static final double NANOSECONDS_IN_MILLISECONDS = 1000000d;
    private static final double MIN_PER_SECOND_MS = 0.001d;
    private static final double MIN_MBPS_MS = 1d;
    
    /**
     * Returns the time taken, in milliseconds from the starting time 
     * in nano seconds.
     * @param startNano the start time in nano seconds, obtained by 
     * System.nanoTime()
     * @return the time taken, in milliseconds, from the given starting time
     */
    public static long takenMs(final long startNano) {
        return Math.round((System.nanoTime() - startNano)/NANOSECONDS_IN_MILLISECONDS);
    }
    
    /**
     * Returns the time, in milliseconds from the time in nanoseconds
     * @param startNano the time in nano seconds 
     * @return the time, in milliseconds, 
     */
    public static long toMs(final long startNano) {
        return Math.round(startNano/NANOSECONDS_IN_MILLISECONDS);
    }
    
    /**
     * Returns the number per second from the given amount and taken time
     * in milliseconds.
     * @param amount the base amount
     * @param takenMs the time taken in milliseconds
     * @return the number per second
     */
    public static double perSecond(final long amount, final long takenMs) {
        return amount/Math.max(MIN_PER_SECOND_MS, takenMs/MILLISECONDS_IN_SECONDS);
    }
    
    /**
     * Returns the megabits per second from the given number of bytes over the
     * given time in milliseconds
     * @param bytes the number of bytes
     * @param takenMs the time taken in milliseconds
     * @return the megabits per second
     */
    public static double toMbps(final long bytes, final long takenMs) {
        return bytes/BYTES_IN_MEGABIT/Math.max(MIN_MBPS_MS, takenMs/MILLISECONDS_IN_SECONDS);
    }
    
    /**
     * Returns a string as the number per second for the given amount and
     * the taken time in milliseconds. Formated to 2 decimal places and ends in
     * /sec. 
     * @param amount the base amount
     * @param takenMs the time taken in milliseconds
     * @return the number per second string
     */
    public static String perSecondString(final long amount, final long takenMs) {
        return  String.format("%.2f/sec", perSecond(amount, takenMs));
    }
    
    /**
     * Default private constructor.
     */
    private MeasurementUtil() {
        // no-op
    }
}