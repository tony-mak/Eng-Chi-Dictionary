//package com.madeinhk.adapter;
//
//import android.content.Context;
//import android.database.Cursor;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.CursorAdapter;
//import android.widget.TextView;
//
//import com.madeinhk.model.ECDictionary;
//
///**
// * Created by tonymak on 10/19/14.
// */
//public class SuggestionAdapter extends CursorAdapter {
//    public SuggestionAdapter(Context context) {
//        ECDictionary dictionary = new ECDictionary(context);
//        dictionary.autoComplete()
//        super(context, cursor, true);
//    }
//
//    @Override
//    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//        return LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null);
//
//    }
//
//    @Override
//    public void bindView(View view, Context context, Cursor cursor) {
//        TextView tv = (TextView) view.findViewById(android.R.id.text1);
//        tv.setText(cursor.getString(0));
//    }
//
//}
