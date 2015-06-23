package com.madeinhk.model;

/**
 * Created by tonymak on 10/16/14.
 */
public class LookupResult {

    private String mWord;
    private String mMeaning;
    private String mPhoneticString;
    private String mExample;
    private  int mDifficulty;

    public LookupResult(String word, String meaning, String phoneticString, String example, int
            difficulty) {
        this.mWord = word;
        this.mMeaning = meaning;
        this.mPhoneticString = phoneticString;
        this.mExample = example;
        this.mDifficulty = difficulty;
    }

    @Override
    public String toString() {
        return "LookupResult{" +
                "mWord='" + mWord + '\'' +
                ", mMeaning='" + mMeaning + '\'' +
                ", mPhoneticString='" + mPhoneticString + '\'' +
                ", mExample='" + mExample + '\'' +
                ", mDifficulty=" + mDifficulty +
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

    public int getmDifficulty() {
        return mDifficulty;
    }
}
