package com.madeinhk.utils;

public class StringUtils {

    public static boolean isEnglishWord(String text) {
        char[] chars = text.toCharArray();
        boolean isEnglish = true;
        for (int i = 0; i < chars.length; i++) {
            if (!Character.isLetter(chars[i])) {
                isEnglish = false;
                break;
            }
        }
        return isEnglish;
    }
}
