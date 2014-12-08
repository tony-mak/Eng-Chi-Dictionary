package com.madeinhk.model;

/**
 * Created by tonymak on 10/16/14.
 */
public class LookupResult {

    private String mWord;
    private String mMeaning;
    private String mPhoneticString;
    private String mExample;

    public LookupResult(String word, String meaning, String phoneticString, String example) {
        this.mWord = word;
        this.mMeaning = meaning;
        this.mPhoneticString = phoneticString;
        this.mExample = example;
    }

    @Override
    public String toString() {
        return "LookupResult{" +
                "mWord='" + mWord + '\'' +
                ", mMeaning='" + mMeaning + '\'' +
                ", mPhoneticString='" + mPhoneticString + '\'' +
                ", mExample='" + mExample + '\'' +
                '}';
    }

    public String getWord() {
        return mWord;
    }

    public String getMeaning() {
        return mMeaning;
    }

    public String getPhoneticString() {
        return mPhoneticString;
    }

    public String getExample() {
        return mExample;
    }
}
