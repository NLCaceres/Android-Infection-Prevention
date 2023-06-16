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
- Jetpack Composable within the Sort & Filter's RecyclerView ViewHolders
- Consolidate Hilt Modules into main AppModule, leaving improved RepositoryModule for UI Testing to easily stub in data
- Updated RecyclerView Diff'ing for all Adapters
- Sort/Filter Options + Search Bar working with Report List
- Drop Toasts for Snackbars
  - Update leftover references to Toasts
- Reduced magic string usage
  - Relevant to i18n and future l10n

### Technical Upgrades
- Integrated Android Navigation Component to simplify navigation logic
- Dropped MVC for MVVM approach, splitting Views & ViewModels
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
- Improved accessibility
  - Sufficient contrast, text size, touch target size, and use of content descriptions
  - Improved use of modern native elements for more intuitive User Experience
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
- Take advantage of AppManifest merging to set usesCleartext to false in release
- Cache data via Room Database (SQLCipher to maintain an encrypted version?)
- Split ReportList, Sort/Filter Options, and Selected Filters into their own reusable child fragments
    - Possibly good candidates for converting into Jetpack Composables
- Full Localization

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