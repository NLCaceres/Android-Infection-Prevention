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

        // Calling Kotlin code from Java means default param values are REQUIRED, no omitting like in kotlin!
        ViewHelpersKt.SetupToolbar(this, viewBinding.toolbarLayout.homeToolbar,
            R.drawable.ic_back_arrow, "Settings");

        getSupportFragmentManager().beginTransaction().replace(R.id.settings_fragment, FragmentSettings.newInstance()).commit();
    }

}
