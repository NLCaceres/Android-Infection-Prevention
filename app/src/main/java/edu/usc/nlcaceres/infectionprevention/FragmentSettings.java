package edu.usc.nlcaceres.infectionprevention;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public class FragmentSettings extends PreferenceFragmentCompat {
    
    public static FragmentSettings newInstance() {
        Bundle args = new Bundle();
        
        FragmentSettings fragment = new FragmentSettings();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Loads preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }
}
