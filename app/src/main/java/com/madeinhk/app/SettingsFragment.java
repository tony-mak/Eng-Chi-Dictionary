package com.madeinhk.app;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.madeinhk.english_chinesedictionary.R;

public class SettingsFragment extends Fragment {

    RadioButton greyRb;
    RadioButton redRb;
    RadioButton pinkRb;


    Context mContext;
    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        //get RadioGroup
        RadioGroup colorsRg = (RadioGroup) rootView.findViewById(R.id.colors);

        //get RadioButton
        greyRb = (RadioButton) rootView.findViewById(R.id.grey);
        redRb = (RadioButton) rootView.findViewById(R.id.red);
        pinkRb = (RadioButton) rootView.findViewById(R.id.pink);

        mContext = rootView.getContext();

        //set RadioGroup OnCheckedChangeListener

        colorsRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            @Override
            public void onCheckedChanged(RadioGroup rg, int checkedId) {

                String color = "notSet";
                if (checkedId == greyRb.getId()) {
                    color = "grey";
                }
                if (checkedId == pinkRb.getId()) {
                    color = "pink";
                }
                if (checkedId == redRb.getId()) {
                    color = "red";
                }

                SharedPreferences.Editor editor =  preferences.edit();
                editor.remove("color");
                editor.putString("color",color);
                editor.commit();
            }
        });

        return rootView;
    }

}
