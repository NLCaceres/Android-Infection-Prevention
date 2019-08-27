package edu.usc.nlcaceres.infectionprevention;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Loads preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}
