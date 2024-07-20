/*
Business Entity Library (BEL) - A foundational library for JSF web applications 
Copyright (C) 2024  D P Bennett & Associates Limited

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

Email: info@dpbennett.com.jm
 */
package jm.com.dpbennett.sm.util;

public class TimeUtils {

    /**
     * Convert to millis time.
     *
     * @param time the time
     * @return the long
     */
    public static long convertToMillisTime(String time) {
        long timeMillis;
        long hours = Integer.parseInt(time.substring(0, 2));
        long minutes = Integer.parseInt(time.substring(3, 5));
        long seconds = Integer.parseInt(time.substring(6, 8));
        long millis = 0;
        
        if (time.length() == 12) {
            millis = Integer.parseInt(time.substring(9, 12));
        }
        
        timeMillis = (hours * 60 + minutes) * 60 * 1000 + seconds * 1000 + millis;
        
        return timeMillis;
    }

    /**
     * Convert to readable time.
     *
     * @param millis the millis
     * @return the string
     */
    public static String convertToReadableTime(long millis) {
        long second = (millis / 1000) % 60;
        long minute = (millis / (1000 * 60)) % 60;
        long hour = (millis / (1000 * 60 * 60)) % 24;

        String time = String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);
        return time;
    }

}
