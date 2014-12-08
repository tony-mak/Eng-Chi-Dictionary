package com.madeinhk.model;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import java.util.List;


public class ECDictionaryTest extends AndroidTestCase {

    public void setUp() throws Exception {
        super.setUp();

    }

    public void tearDown() throws Exception {

    }


    public void testLookup() {
        ECDictionary ecDictionary = new ECDictionary(getContext());
        Word word = ecDictionary.lookup("sun");
        List<Word.TypeEntry> mTypeEntry = word.mTypeEntry;
        Assert.assertEquals(5, mTypeEntry.size());
    }


}