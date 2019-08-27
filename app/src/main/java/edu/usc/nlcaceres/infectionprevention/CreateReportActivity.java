package edu.usc.nlcaceres.infectionprevention;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class CreateReportActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_report_activity);

        androidx.appcompat.widget.Toolbar createReportToolbar = findViewById(R.id.create_report_toolbar);
        setSupportActionBar(createReportToolbar);

        // In a nut shell always how you will change or swap fragments
        getFragmentManager().beginTransaction().replace(R.id.create_report_fragment, new SettingsFragment()).commit();

        // Important for adding to back stack while keeping it one activity
        // transaction.replace(R.id.fragment_container, newFragment);
        // transaction.addToBackStack(null);


    }


}
