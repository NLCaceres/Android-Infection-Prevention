package edu.usc.nlcaceres.infectionprevention

import edu.usc.nlcaceres.infectionprevention.util.SetupToolbar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.usc.nlcaceres.infectionprevention.databinding.ActivitySettingsBinding

class ActivitySettings : AppCompatActivity() {
  private lateinit var viewBinding: ActivitySettingsBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewBinding = ActivitySettingsBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)

    SetupToolbar(this, viewBinding.toolbarLayout.homeToolbar, R.drawable.ic_back_arrow, "Settings")

    supportFragmentManager.beginTransaction()
      .replace(R.id.settings_fragment, FragmentSettings.newInstance()).commit()
  }
}