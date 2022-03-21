package edu.usc.nlcaceres.infectionprevention;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import edu.usc.nlcaceres.infectionprevention.databinding.ActivitySettingsBinding;
import edu.usc.nlcaceres.infectionprevention.util.ViewHelpersKt;

public class ActivitySettings extends AppCompatActivity {
    private ActivitySettingsBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        ViewHelpersKt.SetupToolbar(this, viewBinding.toolbarLayout.homeToolbar, 0);

        getSupportFragmentManager().beginTransaction().replace(R.id.settings_fragment, FragmentSettings.newInstance()).commit();
    }

}
