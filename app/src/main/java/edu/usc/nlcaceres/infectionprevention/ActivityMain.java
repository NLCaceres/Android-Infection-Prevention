package edu.usc.nlcaceres.infectionprevention;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import edu.usc.nlcaceres.infectionprevention.helpers.ConstantsKt;
import edu.usc.nlcaceres.infectionprevention.helpers.FilterItem;
import edu.usc.nlcaceres.infectionprevention.helpers.HealthPractice;
import edu.usc.nlcaceres.infectionprevention.helpers.Precaution;
import edu.usc.nlcaceres.infectionprevention.helpers.PrecautionDeserializer;
import edu.usc.nlcaceres.infectionprevention.helpers.RequestQueueSingleton;

public class ActivityMain extends AppCompatActivity {

  private DrawerLayout mDrawerLayout;

  private ProgressBar mProgIndicator;
  private TextView sorryMessage;

  private AdapterPrecautionsRV precautionAdapter;
  private RecyclerView precautionRecycleView;

  private ArrayList<Precaution> precautionList;

  @Override
  protected void onCreate(Bundle savedInstanceState) { // ActivityMain may not be make newReportActivity but instead list recent reports
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    setUpToolbarNavView();

    precautionRecycleView = findViewById(R.id.precautionRV); // Set LinearLayoutManager in XML
    precautionRecycleView.setHasFixedSize(true); // If each horizontal view is always same height

    precautionList = new ArrayList<>();
    precautionAdapter = new AdapterPrecautionsRV(precautionList, new HPracticeClickListener());
    precautionRecycleView.setAdapter(precautionAdapter);

    if (precautionList.size() == 0) { fetchPrecautions(); }
  }

  @Override
  protected void onStop() {
    super.onStop();
    RequestQueueSingleton.Companion.getInstance(getApplicationContext()).getRequestQueue().cancelAll(ConstantsKt.MainActivityRequestCancelTag); // Cancel requests created here
  }

  private void setUpToolbarNavView() {
    Toolbar myToolbar = (Toolbar) findViewById(R.id.home_toolbar); // Best practice: Infer the view! No cast necessary anymore
    setSupportActionBar(myToolbar);

    //Drawable hamburgerNav = getResources().getDrawable(R.drawable.ic_menu);
    //hamburgerNav.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.ic_menu));

    mDrawerLayout = findViewById(R.id.myNavDrawer);

    NavigationView navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(new NavDrawerHandler());
    navigationView.getHeaderView(0).findViewById(R.id.navCloseButton).setOnClickListener(v -> mDrawerLayout.closeDrawers());

    mProgIndicator = findViewById(R.id.app_progressbar);
    mProgIndicator.setVisibility(View.VISIBLE);

    sorryMessage = findViewById(R.id.sorryTextView);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.action_buttons, menu);

//    Drawable settingsIcon = menu.getItem(0).getIcon();
//    settingsIcon.setColorFilter(Color.parseColor("#FFCC00"), PorterDuff.Mode.SRC_IN); // Goal: Change color along with App prefs
    //Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.usc_seal_gold, null);
    //icon.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_OUT));

    return true;
  }
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home: // Hamburger icon to open NavView
        mDrawerLayout.openDrawer(GravityCompat.START);
        return true;

      case R.id.action_settings: // Settings icon to SettingsPrefs Activity
        Snackbar.make(findViewById(R.id.myCoordinatorLayout), R.string.my_message, Snackbar.LENGTH_SHORT).show();
        Intent settingsIntent = new Intent(this, ActivitySettings.class);
        startActivity(settingsIntent);
        return true;

      default: // Always good practice but not likely to run
        return super.onOptionsItemSelected(item);
    }
  }

  private void fetchPrecautions() { // All descendants of Request cache by default (assuming GET request)
    // requestQueue = Volley.newRequestQueue(this); // Volley uses factory method to init
    // Two constructors - one always fetches, the other can be customized to whatever Request Method (and includes param for array/obj to POST)
    JsonArrayRequest precautionsListRequest = new JsonArrayRequest(ConstantsKt.precautionsURL, response -> {
      try {
        precautionList.addAll(new GsonBuilder().registerTypeAdapter(Precaution.class, new PrecautionDeserializer()).create()
            .fromJson(response.toString(), TypeToken.getParameterized(ArrayList.class, Precaution.class).getType()));
      } catch (Exception e) { Log.w("Precaution Serial Err", "Issue while deserializing precautions into array"); }
      precautionAdapter.notifyDataSetChanged(); // Will only work if add or addAll is called (arr = new Arr() does not fire it!)
      mProgIndicator.setVisibility(View.INVISIBLE);

    }, error -> {
      Log.d("JsonArrayReq Err", error.toString());
      // TODO: Main only show once! (At least with the back button thru shown boolean var)
      // TODO: Don't forget to modify so you can get rid of cancel (may need to provide guideline for that reason so it doesn't expand way too big when gone [maybe use invis?])
      AppFragmentAlertDialog alertDialog = AppFragmentAlertDialog.Companion.newInstance(getResources().getString(R.string.main_alert_dialog_title),
          getResources().getString(R.string.main_alert_dialog_message), false);
      alertDialog.show(getSupportFragmentManager(), "main_alert_dialog");
      sorryMessage.setVisibility(View.VISIBLE);
      mProgIndicator.setVisibility(View.INVISIBLE);
    });

    precautionsListRequest.setTag(ConstantsKt.MainActivityRequestCancelTag); // For cancelling later
    precautionsListRequest.setRetryPolicy(new DefaultRetryPolicy(ConstantsKt.TIMEOUT_MS,
        ConstantsKt.MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    RequestQueueSingleton.Companion.getInstance(this.getApplicationContext()).addToRequestQueue(precautionsListRequest);
  }

  private class NavDrawerHandler implements NavigationView.OnNavigationItemSelectedListener {
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
      Intent reportListIntent = new Intent(getApplicationContext(), ActivityReportList.class);

      switch (menuItem.getItemId()) {
        case R.id.nav_reports:
          Log.d("Nav Item Selected", "Clicked Reg Reports item");
          break;
        case R.id.nav_standard_precautions:
          reportListIntent.putExtra(ConstantsKt.preSelectedFilterExtra, new FilterItem("Standard",true,"Precaution Type"));
          Log.d("Nav Item Selected", "Clicked standard reports item");
          break;
        case R.id.nav_isolation_precautions:
          reportListIntent.putExtra(ConstantsKt.preSelectedFilterExtra, new FilterItem("Isolation", true, "Precaution Type"));
          Log.d("Nav Item Selected", "Clicked isolation reports item");
          break;
        default: return false;
      }

      mDrawerLayout.closeDrawers();

      ArrayList<String> precautionNames = new ArrayList<>();
      ArrayList<String> practiceNames = new ArrayList<>();
      for (int i = 0; i < precautionList.size(); i++) {
        Precaution checkedPrecaution = precautionList.get(i);
        precautionNames.add(checkedPrecaution.getName());
        ArrayList<HealthPractice> checkedPractices = checkedPrecaution.getPractices();
        for (int j = 0; j < checkedPractices.size(); j++) {
          practiceNames.add(checkedPractices.get(j).getName());
        }
      }
      reportListIntent.putStringArrayListExtra("PrecautionList", precautionNames);
      reportListIntent.putStringArrayListExtra("PracticeList", practiceNames);
      startActivity(reportListIntent);
      return true;
    }
  }

  private class HPracticeClickListener implements AdapterPracticesRV.HealthPracticeClickListener {
    @Override
    public void onHealthPracticeItemClick(@NotNull View view, @NotNull HealthPractice healthPractice) {
      Intent createReportIntent = new Intent(getApplicationContext(), ActivityCreateReport.class);
      createReportIntent.putExtra(ConstantsKt.createReportPracticeExtra, healthPractice.getName());
      startActivityForResult(createReportIntent, ConstantsKt.CreateReportRequestCode);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == ConstantsKt.CreateReportRequestCode) {
      if (resultCode == Activity.RESULT_OK) { startActivity(new Intent(getApplicationContext(), ActivityReportList.class)); }
    }
  }
}




