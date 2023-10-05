# ReminderLocation

Project: Location Reminder - Fourth Project from Udacity Advanced Android Development Kotlin Nanodegree
In this project, I am create a TODO list app with location reminders that remind the user to do something
when the user is at a specific location.
The app will require the user to create an account and login to set and access reminders.

# Project Instructions

User Authentication
Login: Create a Login screen to ask users to login using an email address or a Google account. Upon successful login, navigate the user to the Reminders screen. If there is no account, the app should navigate to a Registration (Signup) screen.
Signup: Create a Registration screen to allow a user to register using an email address or a Google account.
Authentication: Enable the authentication using Firebase console and include Firebase UI dependency in the project. You must use this library, FirebaseUI-Android, to implement the authentication.
Tip: You can use either FirebaseUserLiveData or just FirebaseAuth.getInstance().currentUser or shared preferences.
Logout: Make sure that the users can log out of the app and when the app starts again they are required to login first.
![image](https://user-images.githubusercontent.com/87489620/214917836-7475bc10-5d42-4568-9285-22e62d60c5f7.png)

Map View
Map view: Create a Map view that shows the user's current location.
It should first ask the user's location access permission to show his/her current location on the map.
Ensure that the location access is handled in cases of user denial that the user is shown the right error messages.
POI: The app should ask the user to select a point of interest (POI) on the map to create a reminder.
The app asks the user to select a location or POI on the map and add a new marker at that location.
The selected POI can be just a simple POI or an area or specific latitude and longitude. The POI must have a location name.
Upon saving, the selected location is returned to the Save Reminder page and the user is asked to input the title and description for the reminder.
When the reminder is saved, a geofencing request is created. Allowing the user to take Circular Radius for the geofence is a bonus.
Compatibility: The app should work on all the different Android versions including Android Q.
Map Styling: Update the Map Styling using the map styling wizard to generate a nice looking map.
Users should have the option to change map type from the toolbar items.
Geofencing: When the user enters a geofence, a reminder is retrieved from the local storage and a notification showing the reminder title will appear, even if the app is not open.
Tip: Use the JobIntentService to make the geofencing work in the background.

![image](https://user-images.githubusercontent.com/87489620/214917937-f04fd269-b3a6-4eb8-b468-ba0bd4515a0f.png)

![image](https://user-images.githubusercontent.com/87489620/214918029-30cce56a-6b0f-49e5-a95e-c1e1d3345864.png)

Reminders
Create Reminders: Create a screen to add a new reminder when a user reaches the selected location.
Each new reminder should include
title
description
selected location
Important: Capture the user-entered data using live data and data binding.
Use RemindersLocalRepository to save the reminder to the local DB, and create the geofencing request after confirmation.
Tip: Retrieve the ViewModel and Repository files using a ServiceLocator library called Koin
Reminder data should be saved to local storage.
Reminders list view: Create a screen that displays the reminders retrieved from local storage. If there are no reminders, display a "No Data" indicator. If there are any errors, display an error message.
User should be able to navigate from the Reminders list view screen to another screen to create a new reminder.
Each reminder should include
title
description
selected location
Reminder notification: For each reminder, create a geofencing request in the background that fires up a notification when the user enters the geofencing area.
Display details about a reminder when a selected POI is reached and the user clicked on the notification.
When the user clicks a notification, a new screen appears to display the reminder details.
Tip: Notification click opens a new activity using the pending intent, and users should be able to get the selected reminder from local storage and display the details.

![image](https://user-images.githubusercontent.com/87489620/214918106-00f03a84-73c4-46ca-b7dc-52d84d89195d.png)

Testing
Use MVVM and Dependency Injection
Use MVVM and Dependency Injection to architect your app.
The app should follow the MVVM design pattern and uses ViewModels to hold the live data objects, do the validation and interact with the data sources.
Make sure that the validation and data interactions aren’t made through the Fragments or Activities but through the ViewModel.
Retrieve the ViewModels and DataSources using Koin.
Test the ViewModels, Coroutines, and LiveData
Provide testing for the ViewModels, Coroutines and LiveData objects.

RemindersListViewModelTest or SaveReminderViewModelTest should be present in the test package that tests the functions inside the view model.

Live data objects should be tested using shouldReturnError and check_loading testing functions.

Testing methods names should follow the naming convention for testing.

Tip: Run the test coverage to check how much code is tested in the app.

FakeDataSource
Create a FakeDataSource to replace the Data Layer and test the app in isolation.
Project repo should contain a FakeDataSource class that acts as a test double for the LocalDataSource.
Ensure that error handling is done at this class to simulate a failure scenario.
Tip: Use Before and After correctly to initialize the testing and clean up after finishing the test
Test Framework
Use Espresso and Mockito to test the app UI (each screen of the app) and Fragments Navigation. Testing should follow the “Given, When, Then” pattern. Test method names should include the action and expected result of the test. Use Before and After correctly to initialize the testing and clean up after finishing the test. Tests should include:
Automation Testing using ViewMatchers and ViewInteractions to simulate user interactions with the app.
Testing for Snackbar and Toast messages.
Testing the fragments’ navigation.
The testing classes present at androidTest package.
Test DAO (Data Access Object) and Repository classes.
Testing should use Room.inMemoryDatabaseBuilder to create a Room DB instance.
Testing should cover:
inserting and retrieving data using DAO.
predictable errors messages like data not found.
Make sure that wrapEspressoIdlingResource is used to wrap the heavy and background tasks.
Don't forget to close the DB at the @after method.
