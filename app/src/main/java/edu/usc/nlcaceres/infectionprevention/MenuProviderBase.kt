package edu.usc.nlcaceres.infectionprevention

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected

/* Provide a simple basic Menu Provider as a default for screens, making 1 settings icon */
// BUT memory-wise I think this might be an interesting study!
class MenuProviderBase(private val navController: NavController): MenuProvider {
  // It should be memory-safe to hold a ref to the NavController, since there's only one NavHost (the default FragmentContainerView)
  // WHICH has only one controller that will live as long as the graph does, outliving the MenuProvider
  // WHICH is created and destroyed based on the fragment's lifecycle
  override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) = menuInflater.inflate(R.menu.action_buttons, menu)
  override fun onMenuItemSelected(item: MenuItem) = onNavDestinationSelected(item, navController)
  // We COULD make the navController prop into a lambda block that returns a NavController BUT
  // CURRENTLY by providing controller directly, no implicit ref to FragmentMain gets created in the bytecode
  // BUT passing in a lambda, we'd have an implicit ref to FragmentMain instead! but WHY?
  // findNavController is a somewhat special case because the Fragment extension func is
  // Actually calling the static NavController.findNavController(fragment) under the hood AND
  // According to LeakCanary, no leak! BUT according to the bytecode it MIGHT due to its fragment param!
  // The static findNavController() gets a FragmentMain ref via FragmentMain.this i.e it creates an anonymous class
  // Which acts like a Java inner class, saving a ref to the outer FragmentMain class to use as FragmentMain.this
  // BUT if nothing needed from FragmentMain, the lambda turns into a FragmentMain static method so no instance ref needed or can occur
  // Therefore whether it leaks depends on the block. Static does not. A quick instance inner class version, not really
  // A long running async task that needs a ref to FragmentMain? Potentially! See below for more!
}
/* A note on Android memory leaks:
They mainly arise in two somewhat similar places, long running tasks that hold a reference to their activity or fragment
and when long or indefinitely living objects reference short-living objects
The above MenuProvider sits somewhere in that 2nd scenario HOWEVER it represents a reverse relationship

The menuProvider is USUALLY short-living compared to the NavController, lasting one of 3 lengths
1. Until manually removed (which seems likely to leak the fragment's view if it has refs to it in onMenuItemSelected)
2. Until the fragment's view's lifecycle emits ON_DESTROY (View fires it onNav up or away, fragment itself fires its own later)
3. Until the view lifecycle state you specify ends (RESUMED seems to work well and is the most short-lived)
This app uses #3, simply creating the MenuProvider in the Fragment's onViewCreated, adding it in RESUMED,
and finally removing it if the user begins to navigate away, i.e. the ON_PAUSE signal emitted
In essence, this means the NavController continues living, remaining as unaware as ever that the menuProvider is gone
Meanwhile the MenuProvider loses its reference in the ActionBar Menu Host, and likely the last remaining ref keeping it alive

The question remains, though, "Can you leak the NavController?"
My guess is a truly indefinitely-living object like a Singleton could leak it in a multi-NavHost/NavController app
While N/A here, a large app w/ multiple activities, each w/ a NavHost & NavGraph, that all share a Singleton
could pass one of the NavHost's NavController into the Singleton, leaking the NavController upon starting
a new activity and entering the new NavGraph, effectively allowing two NavHosts to be alive at once
at least until the Singleton's NavController ref is set to null or changed to the new NavHost's controller
This highlights 1 reason Google suggests a single activity w/ 1 default NavHost that uses multiple, likely nested, nav graphs
This holds true even in multi-module apps where you should define the NavHost (& UI) in the main app module
*/