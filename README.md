# Infection Prevention - Android

## For Better Infection Prevention and Hospital Documentation
  After talking with my father about his job in Infection Control, it seemed incredibly clear that many hospitals, often being fairly
  slow to adopt new tech, were missing out on the potential benefits of increased data collection and tracking, not only for halting 
  the spread of disease but for its potential to bring deeper insight for medical researchers. With the rise of Big Data and with 
  consideration to HIPAA, retaining and analyzing clinical information has been shown to improve patient outcome, prevent hospital 
  acquired infections, and reduce treatment costs. With those benefits in mind, creating an app that could universally and
  effortlessly meet the demands of a hospital or a local clinic the second it's adopted would be endlessly invaluable.  

## Features
- Home View that allows users to quickly choose from a set of Health Violations and begin filing a Report
- Create Report View to collect date, look up and select employee as well as note place and type of violation
- Navigation Drawer to view Health Practice violation Reports in a list, initially sorted by recency 
  - Buttons in Nav Drawer to immediately filter out Reports based on type of health violation
- Report List View with a Floating Sort Button so the user can better narrow down the reports via a list of sorting and filtering options
- Settings to personalize app, individually and as a team

### Recent Updates
- Jetpack Composables for each ViewHolder in the HealthPracticeAdapter and FilterAdapter
  - SelectedFilterAdapter completely replaced by FlowRow-based ListView Composables, consolidating UI into 2 simple Stateful Composables
  as well as a Screen/View-level Composable containing the Stateful Composables, 1 each for FragmentReportList and FragmentSortFilter
- Consolidated Hilt Modules into main AppModule, leaving improved RepositoryModule for UI Testing to easily stub in data
- Updated RecyclerView Diff'ing for all Adapters
- Sort/Filter Options + Search Bar working with Report List
- Dropped Toasts for Snackbars
  - Update leftover references to Toasts
- Reduced magic string usage
  - Relevant to i18n and future l10n
- Added Kaspersky Kaspresso to not only reduce flakiness of Android UI Tests BUT ALSO to further improve their readability and speed
  - All UI tests have been updated to Kaspresso TestCases, all of which consistently pass
- Take advantage of AppManifest merging to set usesCleartext to false in all build variants EXCEPT for debug
  - Ensures only the Debug build connects over HTTP, allowing the Debug variant to connect to a local server

### Technical Upgrades
- Integrated Android Navigation Component to simplify navigation logic
- Dropped MVC for MVVM approach, splitting Views & ViewModels
  - Using mix of LiveData, StateFlow, and MutableState depending on level of data-manipulation needed, making UI-updates as simple as possible through reactivity
- Additions
    - Hilt
      - Merged DataSourceModule into main AppModule by converting it into abstract class that includes a Kotlin Companion Object
      - Simplified RepositoryModule by swapping @Provides for @Binds so @Inject works as intended in the Repository Implementations
    - Retrofit
    - Animations
    - LeakCanary
- Improved coverage w/ updated UI + Unit tests
    - Included factories for data fakes
    - Espresso
    - Mockito
    - Robolectric
    - Kaspersky Kaspresso
      - Adding Kaspresso has reduced (and potentially completely eliminated) the need for Espresso Idling Resources
        - As more features are built out, time will tell if Espresso Idling can be completely dropped
- Leverage Compose Compiler Stability Reports as needed to maximize UI responsiveness and rendering efficiency
- Improved accessibility
  - Sufficient contrast, text size, touch target size, and use of content descriptions
  - Improved use of modern native elements for more intuitive User Experience
  - Leverage Jetpack Compose Semantics while developing improved UI
- Complete Kotlin Conversion

### Future Changes
- Jetpack Compose for future views
  - Add data charts
  - Employee Profile
  - Progressively update current views
    - Homepage (List of potential health precautions to create new report on)
      - Create Report Screen
        - Update spinner UX where each acts as a button navigating to a page listing selectable options
          - I.e. Employee spinner takes user to list view that populates with Employee data BUT tapping
          the Location spinner will populate list with location data
    - Report List Screen
    - Settings
- Incorporate MaterialUI 3
  - Leverage MaterialTheme to improve personalization for individuals and teams
- Cache data via Room Database (SQLCipher to maintain an encrypted version?)
- Split ReportList and Sort/Filter Options into their own reusable Composables similar to SelectedFilter Composables
- Full Localization

#### Note on Jetpack Compose
- As of 2023, Jetpack Compose is getting very close to being full featured! With updates in availability for
  Flow Layout, Material 3 Components, and much more coming, any spot in the app that can be converted to Compose
  probably should be! To track these new features, [check here](https://developer.android.com/jetpack/androidx/compose-roadmap)
  - HOWEVER, it does still lack in a few major ways namely:
    - Jetpack Composables render significantly slower on startup of the app BUT likely not a big concern
      since the release build runs very well, AND all subsequent Composables load plenty fast
    - Shared Element Transitions is still unavailable
    - Flow Layout while technically available is still experimental
    - Material 3 is missing Swipe to Refresh still
    - While super easy to use, ComposeTestRule is completely separate from Espresso UI Testing which makes
    hybrid views a bit more difficult to test since neither is aware of the other's View Hierarchy and where they intersect
- Jetpack Compose offers a convenient Gradle Task that allows you to inspect its Stability Inferences. Ensuring your Composables
  are Stable helps to optimize recomposition, skipping it whenever possible.
  - The command to run in the root of the project is `./gradlew assembleRelease -PcomposeCompilerReports=true`
    - Android Studio allows you to enter the command into its terminal and run it with the IDE using `Command + Return`
  - When using the Compose Compiler Reports, it's worth noting that even Google insists not getting too caught up by its results.
    If you or your users are noticing performance issues, and you think it's related to stability, THEN you can
    guarantee it'll be important to check the Compose Compiler Reports. Otherwise, you'll just be optimizing prematurely

## Related Apps
- Front-end website: https://github.com/NLCaceres/Angular-Infection-Prevention
    - Now, running on Angular 15 with all tests passing. 
    - Major redesign beginning. Given Tailwind's success, TailwindUI/CSS might provide the best professional styling while not overly conventional
    - Deploy to Vercel since CORS settings wouldn't need changing anyway?
- Back-end server: https://github.com/NLCaceres/Spring-Boot-Infection-Prevention
    - Uses Spring Boot 3 to serve both a REST & GraphQL API for the web and mobile clients, storing their data with MongoDB
    - Will need to setup OAuth2 Single Sign On Authorization
    - Will need to Dockerize App to compile it into a GraalVM Image, compatible with Fly.io or Railway  
- iOS App: https://github.com/NLCaceres/iOS-records
    - Currently, improving code separation for better readability and reusability by extracting business logic out
      of ViewModels and into Repositories/Services + Domain-layer reusable functions
      - Employee and Report Repositories created, helping abstract out the underlying Networking API
    - Will need to add search bar to ReportList view for filtering
    - Will need to feed data into SwiftUI Charts