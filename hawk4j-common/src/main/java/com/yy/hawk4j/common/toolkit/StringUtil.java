package com.yy.hawk4j.common.toolkit;

public class StringUtil {

    /**
     * Is blank.
     *
     * @param str
     * @return
     */
    public static boolean isBlank(CharSequence str) {
        if ((str == null)) {
            return true;
        }
        int length = str.length();
        if (length == 0) {
            return true;
        }
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            boolean charBlank = Character.isWhitespace(c) || Character.isSpaceChar(c) || c == '\ufeff' || c == '\u202a';
            if (!charBlank) {
                return false;
            }
        }
        return true;
    }

}
