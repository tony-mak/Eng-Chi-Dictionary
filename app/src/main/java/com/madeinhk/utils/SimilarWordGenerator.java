package com.madeinhk.utils;

import androidx.collection.ArraySet;

import java.util.Set;

/**
 * Created by tonymak on 10/18/15.
 */
public class SimilarWordGenerator {
    private static final char[] ALPHABETS = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    public Set<String> generate(String source) {
        Set<String> generatedStrings = new ArraySet<>();
        generateByDeleteAt(source, generatedStrings);
        generateByInsertAt(source, generatedStrings);
        generateByTransposition(source, generatedStrings);
        genereateBySubstitution(source, generatedStrings);
        return generatedStrings;
    }

    private void generateByDeleteAt(String source, Set<String> generatedStrings) {
        final int length = source.length();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.setLength(0);
            stringBuilder.append(source);
            String generatedString = stringBuilder.deleteCharAt(i).toString();
            generatedStrings.add(generatedString);
        }
    }

    private void generateByInsertAt(String source, Set<String> generatedStrings) {
        final int length = source.length();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            for (char alphabet : ALPHABETS) {
                stringBuilder.setLength(0);
                stringBuilder.append(source);
                String generatedString = stringBuilder.insert(i, alphabet).toString();
                generatedStrings.add(generatedString);
            }
        }
    }

    private void genereateBySubstitution(String source, Set<String> generatedStrings) {
        final int length = source.length();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            for (char alphabet : ALPHABETS) {
                stringBuilder.setLength(0);
                stringBuilder.append(source);
                stringBuilder.setCharAt(i, alphabet);
                generatedStrings.add(stringBuilder.toString());
            }
        }
    }

    private void generateByTransposition(String source, Set<String> generatedStrings) {
        final int length = source.length();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                stringBuilder.setLength(0);
                stringBuilder.append(source);
                char a = stringBuilder.charAt(i);
                stringBuilder.setCharAt(i, stringBuilder.charAt(j));
                stringBuilder.setCharAt(j, a);
                generatedStrings.add(stringBuilder.toString());
            }
        }
    }
}
