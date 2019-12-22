package edu.usc.nlcaceres.infectionprevention;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class ActivitySettings extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        androidx.appcompat.widget.Toolbar settingsToolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(settingsToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction().replace(R.id.settings_fragment, FragmentSettings.newInstance()).commit();
    }

}
