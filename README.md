Android App Network Usage Monitor
=================================

This app reports the past usage time and network traffic of a specified app. The user selects an app they with to examine, and are presented with a summary of the usage of the app and an option to export this information to the storage.

### Development Details

This application showcases several Android API features:
* Using Android 6.0 (Marshmallow) permissions request model
* Requesting Usage Access permission from the OS
* Accessing OS records of app usage:
  - Reading app usage details (android.app.usage.UsageStats)
  - Reading network usage details from (android.app.usage.NetworkStats), both Wifi and Mobile
* Listing the apps which are currently installed
* Reading/writing to an app's 'external' folder, accessible to other apps and the user
