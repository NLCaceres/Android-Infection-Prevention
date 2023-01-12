# Infection Prevention on Android ([Also on iOS](https://github.com/NLCaceres/iOS-Records))

## For Better Infection Prevention and Hospital Documentation
  After talking with my father about his job in Infection Control, it seemed incredibly clear that many hospitals, often being fairly
  slow to adopt new tech, were missing out on the potential benefits of increased data collection and tracking, not only for halting 
  the spread of disease but for its potential to bring deeper insight for medical researchers. With the rise of Big Data and with 
  consideration to HIPAA, retaining and analyzing clinical information has been shown to improve patient outcome, prevent hospital 
  acquired infections, and reduce treatment costs. With those benefits in mind, creating an app that could universally and
  effortlessly meet the demands of a hospital or a local clinic the second it's adopted would be endlessly invaluable.  
  
## Future Changes
  - Jetpack Compose for future views
      - Add data charts
      - Employee Profile
      - As well as progressively update current views
          - Homepage (List of potential health precautions to create new report on)
          - Create Report Screen
            - Update spinner UX where each acts as a button navigating to a page listing selectable options
                - I.e. Employee spinner takes user to list view that populates with Employee data BUT tapping
                the Location spinner will populate list with location data
          - Report List Screen
              - Sort and Filter Options List
          - Settings
  - Update Hilt AppModule
      - Integrate DataSource Module into it
      - Convert @Provides in RepositoryModule to @Binds where possible
  - Take advantage of AppManifest merging to set usesCleartext to false in release
  - Cache data via Room Database (SQLCipher to maintain an encrypted version?)
  - Split ReportList, Sort/Filter Options, and Selected Filters into their own reusable child fragments
      - Possibly good candidates for converting into Jetpack Composables
  - Full Localization

## Recent Changes
  - Integrated Android Navigation Component to simplify navigation logic
  - Additions
      - Hilt
      - Retrofit
      - Animations
      - LeakCanary
  - Improved coverage w/ updated UI + Unit tests
      - Included factories for data fakes
      - Espresso
      - Mockito
      - Robolectric
  - Dropped MVC for MVVM approach, splitting Views & ViewModels
  - Updated RecyclerView Diff'ing for all Adapters
  - Sort/Filter Options + Search Bar working with Report List 
  - Drop Toasts for Snackbars
  - Improved accessibility
  - Complete Kotlin Conversion
  - Reduced magic string usage
      - Relevant to i18n and future l10n
