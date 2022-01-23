package edu.usc.nlcaceres.infectionprevention;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import edu.usc.nlcaceres.infectionprevention.databinding.ActivitySettingsBinding;

public class ActivitySettings extends AppCompatActivity {
    private ActivitySettingsBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        androidx.appcompat.widget.Toolbar settingsToolbar = viewBinding.toolbarLayout.homeToolbar;
        setSupportActionBar(settingsToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) supportActionBar.setDisplayHomeAsUpEnabled(true); // Possible Null ActionBar (but not likely)

        getSupportFragmentManager().beginTransaction().replace(R.id.settings_fragment, FragmentSettings.newInstance()).commit();
    }

}
