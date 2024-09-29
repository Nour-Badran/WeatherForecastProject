Weather Forecast Application
Project Brief Description
The Weather Forecast Application is an Android mobile application that provides users with
real-time weather status and temperature based on their current location or any specified location.
Users can select a location on the map or search for it using an autocomplete text field. Additionally, 
users can save their favorite locations and receive alerts for weather conditions such as rain, wind, extreme temperatures, fog, and snow.
Weather Alarm
Favorite Screen
Project Screens
Settings Screen
In this screen, users can:
•	Choose their preferred location using GPS or by selecting a specific location on the map.
•	Select temperature units (Kelvin, Celsius, Fahrenheit).
•	Choose wind speed units (m/s, mph).
•	Select language (Arabic, English).
Home Screen
The Home screen displays:
•	Current temperature
•	Current date and time
•	Humidity
•	Wind speed
•	Pressure
•	Cloud cover
•	City name
•	Weather icon (representing the current weather status)
•	Weather description (e.g., clear sky, light rain)
•	Past hourly weather data for the current date
•	Forecast data for the next five days
Weather Alerts Screen
This screen allows users to set weather alerts with the following options:
•	Duration for which the alert is active.
•	Type of alarm (notification or default alarm sound).
•	Option to stop notifications or turn off the alarm.
Favorite Screen
•	This screen lists all favorite locations.
•	Tapping an item opens another screen displaying the forecast information for that location.
•	A Floating Action Button (FAB) allows users to add a new favorite location.
•	Users can either place a marker on the map or type the city name into the autocomplete text field.
•	Users can also remove a saved location.
Technologies Used
•	Android SDK
•	Kotlin
•	MVVM Architecture
•	Retrofit (for API calls)
•	Room (for local database)
•	OpenStreetMap Maps API (for location selection)
•	Coroutines (for asynchronous operations)
How to Run the Application
1.	Clone the repository.
2.	Open the project in Android Studio.
3.	Make sure to add your OpenWeatherMap API key in the project.
4.	Build and run the application on an Android device or emulator.
