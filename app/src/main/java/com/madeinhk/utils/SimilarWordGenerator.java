package com.madeinhk.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by tonymak on 10/18/15.
 */
public class SimilarWordGenerator {
    private static final char[] ALPHABETS = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    public List<String> generate(String source) {
        List<String> similarStrings = new ArrayList<>();
        similarStrings.addAll(generateByDeleteAt(source));
        similarStrings.addAll(generateByInsertAt(source));
        similarStrings.addAll(generateByTransposition(source));
        similarStrings.addAll(genereateBySubstitution(source));
        return similarStrings;
    }

    private Set<String> generateByDeleteAt(String source) {
        Set<String> genereatedStrings = new HashSet<>();
        final int length = source.length();
        for (int i = 0; i < length; i++) {
            String generatedString = new StringBuilder(source).deleteCharAt(i).toString();
            genereatedStrings.add(generatedString);
        }
        return genereatedStrings;
    }

    private Set<String> generateByInsertAt(String source) {
        Set<String> genereatedStrings = new HashSet<>();
        final int length = source.length();
        for (int i = 0; i < length; i++) {
            for (char alphabet : ALPHABETS) {
                String generatedString = new StringBuilder(source).insert(i, alphabet).toString();
                genereatedStrings.add(generatedString);
            }
        }
        return genereatedStrings;
    }

    private Set<String> genereateBySubstitution(String source) {
        Set<String> genereatedStrings = new HashSet<>();
        final int length = source.length();
        for (int i = 0; i < length; i++) {
            for (char alphabet : ALPHABETS) {
                StringBuilder sb = new StringBuilder(source);
                sb.setCharAt(i, alphabet);
                genereatedStrings.add(sb.toString());
            }
        }
        return genereatedStrings;
    }

    private Set<String> generateByTransposition(String source) {
        Set<String> genereatedStrings = new HashSet<>();
        final int length = source.length();
        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                StringBuilder sb = new StringBuilder(source);
                char a = sb.charAt(i);
                sb.setCharAt(i, sb.charAt(j));
                sb.setCharAt(j, a);
                genereatedStrings.add(sb.toString());
            }
        }
        return genereatedStrings;
    }

    public static void main(String[] args) {
        SimilarWordGenerator generator = new SimilarWordGenerator();
        List<String> list = generator.generate("morroco");
        for (String str : list) {
            System.out.println(str);
        }
    }

}
