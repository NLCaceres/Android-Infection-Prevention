package edu.usc.nlcaceres.infectionprevention

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected

/** Provide a simple basic Menu Provider as a default for screens,
 * The only icon is a Settings Gear icon, used to navigate to the Settings Screen
 **/
class MenuProviderDefault(private val navController: NavController): MenuProvider {
  // It should be memory-safe to hold a ref to the NavController, since there's only one NavHost (the default FragmentContainerView)
  // WHICH has only one NavController that lives as long as the graph does AND outlives ANY MenuProvider
  // WHICH are usually created and destroyed based on the fragment's lifecycle (usually Lifecycle.State.STARTED)
  override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) = menuInflater.inflate(R.menu.action_buttons, menu)
  override fun onMenuItemSelected(item: MenuItem) = onNavDestinationSelected(item, navController)
}
