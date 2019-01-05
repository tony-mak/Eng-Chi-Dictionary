package com.madeinhk.app;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.madeinhk.english_chinesedictionary.R;


public class AboutFragment extends Fragment {


    public AboutFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        TextView versionTextView = rootView.findViewById(R.id.version);
        versionTextView.setText("Version " + getAppVersionString());
        Button contactButton = rootView.findViewById(R.id.contact_button);
        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "ming030890@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback about Eng-Chi Dictionary");
                startActivity(Intent.createChooser(emailIntent, null));
            }
        });
        return rootView;
    }

    private String getAppVersionString() {
        try {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "N/A";
    }
}
