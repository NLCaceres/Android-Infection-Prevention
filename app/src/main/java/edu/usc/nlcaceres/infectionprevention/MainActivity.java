package edu.usc.nlcaceres.infectionprevention;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;

    private PrecautionRVAdapter precautionAdapter;
    private RecyclerView precautionRecycleView;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<Precaution> precautions;
    private List<String> buttons;

    static final String settingsIntent = "edu.usc.nlcaceres.infectionprevention.settings";
    static final String newReportIntent = "edu.usc.nlcaceres.infectionprevention.new_report";

    public static final String ReqCancelTag = "MainActivityCancelTag";
    private RequestQueue requestQueue;
    private String url ="https://safe-retreat-87739.herokuapp.com/api/";
    private String devURL = "http://10.0.2.2:81/api/";
    private StringRequest stringRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Actually not necessary these days to cast the view as a specific subclass
        // Best practice: No longer cast these views to their subclasses it can infer it
        androidx.appcompat.widget.Toolbar myToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        Drawable hamburgerNav = getResources().getDrawable(R.drawable.ic_menu);
        hamburgerNav.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        ActionBar actionBar = getSupportActionBar();
        // This would enable an up button to take over hamburger
        // Not sure it actually is necessary or works
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(hamburgerNav);

        mDrawerLayout = findViewById(R.id.myNavDrawer);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        // Highlight item persists
                        menuItem.setChecked(true);

                        // Closes navDrawer on tap
                        mDrawerLayout.closeDrawers();

                        // All addition UI updates go here
                        // so scroll down in my case

                        String itemID = menuItem.getTitle().toString();

                        //LinearLayoutManager llm = new LinearLayoutManager(this);
                        //llm.scrollToPositionWithOffset(0,20);

                        return true;
                    }
                }
        );

        // Setting RecyclerView

        // Init recyclerview and give it a layout manager to set orientation, activity etc.

        precautionRecycleView = findViewById(R.id.precautionRV);
        precautionRecycleView.setHasFixedSize(true);
        precautionRecycleView.setLayoutManager(new LinearLayoutManager(this));

        // Set up the vertical adapter and init data

        // Instantiate the RequestQueue.
        requestQueue = Volley.newRequestQueue(this);
        // Note there is a tag that belongs to this Req in case a cancel is needed

        // Request a string response from the provided URL.
        String professionsURL = devURL + "precautions";
        Log.d("Endpoint", professionsURL);
        // This works
        // Express returns Array so we need to convert array string into JSONArray
        // From there, iterate over the array to get each object and keys
        stringRequest = new StringRequest(Request.Method.GET,
                professionsURL,
                response -> {
//                    try {
//                        JSONArray arrRes = new JSONArray(response);
//                        for (int i = 0; i < arrRes.length(); i++)
//                        {
//                            JSONObject jsonObj = arrRes.getJSONObject(i);
//                            Iterator<String> keys = jsonObj.keys();
//                            while (keys.hasNext()) {
//                                String key = keys.next();
//                                String tag = "Prof Key " + key;
//                                Log.d(tag, jsonObj.getString(key));
//                            }
//
//                        }
//                    } catch (Exception e) {
//                        Log.d("Professions GET Err", "JSONArray conversion error");
//                    }
                   Log.d("Professions GET Res", response);
                },
                err -> Log.d("Professions GET Err", "Response invalid or not sent")
        );
        // Cancel TAG set
        stringRequest.setTag(ReqCancelTag);

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);

        precautions = new ArrayList<>();

        // These will be loaded from a file stored in the server
        // or simply a tree of backend data (JSON, PDF, Excel, etc).

        // Each array will represent one set of buttons

        ArrayList<HealthPractice> standard = new ArrayList<>();
        //standard.add(new HealthPractice("Hand Hygiene"));
        //standard.add(new HealthPractice("PPO"));

        //precautions.add(new Precaution("Standard Precaution", standard));

        ArrayList<HealthPractice> isolation = new ArrayList<>();
        //isolation.add(new HealthPractice("Masks"));
        //isolation.add(new HealthPractice("Quarantine"));

        //precautions.add(new Precaution("Isolation Practices", isolation));

        // Data will now be set inside adapter

        precautionAdapter = new PrecautionRVAdapter(precautions);
        precautionRecycleView.setAdapter(precautionAdapter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (requestQueue != null) {
            requestQueue.cancelAll(ReqCancelTag);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_buttons, menu);

        // Goal here is to use the preferences to change colors of vector icon

        Drawable settingsIcon = menu.getItem(0).getIcon();
        settingsIcon.setColorFilter(Color.parseColor("#FFCC00"), PorterDuff.Mode.SRC_IN);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // Press hamburger icon to open the nav drawer
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

            // Press the settings icon to begin preferences settings activity
            case R.id.action_settings:
                Snackbar.make(findViewById(R.id.myCoordinatorLayout), R.string.my_message, Snackbar.LENGTH_SHORT).show();
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

            // There really isn't a point to this since it would be default for buttons
            // that don't really exist. Just common practice to define a default for switches
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}




