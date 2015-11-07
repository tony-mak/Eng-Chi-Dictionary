package com.madeinhk.utils;

public class EditDistanceCalculator {

    public int getEditDistance(String wordA, String wordB) {
        if (wordA.charAt(0) != wordB.charAt(0)) {
            return Integer.MAX_VALUE;
        }
        int lengthA = wordA.length();
        int lengthB = wordB.length();
        int[][] table = new int[lengthA][lengthB];
        // initialization
        for (int i = 0; i < lengthA; i++) {
            table[i][0] = i;
        }
        for (int j = 0; j < lengthB; j++) {
            table[0][j] = j;
        }
        for (int i = 1; i < lengthA; i++) {
            for (int j = 1; j < lengthB; j++) {
                int deleteEditDistance = table[i - 1][j] + 1;
                int insertEditDistance = table[i][j - 1] + 1;
                int substituteEditDistance =
                        table[i - 1][j - 1] + (wordA.charAt(i) == wordB.charAt(j) ? 0 : 2);
                table[i][j] = Math.min(Math.min(deleteEditDistance, insertEditDistance),
                        substituteEditDistance);
            }
        }
        return table[lengthA - 1][lengthB - 1];
    }
}
