Network Usage App - Planning The Development
============================================

## Links that started this line of development

* [android.app.usage](https://developer.android.com/reference/android/app/usage/package-summary.html)
* [Android 5.0 APIs](https://developer.android.com/about/versions/android-5.0.html#Power)
  - Checking the times a certain app was in use for
  - Checkout Project Volta (JobScheduler) API introduced in 5.0
* [NetworkStatsManager](https://developer.android.com/reference/android/app/usage/NetworkStatsManager.html)

## Steps

1) main activity to display and remember the selected apps
    - to get a new one start InstalledAppList
    - http://stackoverflow.com/a/10407371 -- how to return a value from an Intent/activity.
2) getting network usage stats
    - https://github.com/googlesamples/android-AppUsageStatistics
3) checking that permissions are enabled before trying to use them
    - question, with examples of forwarding to settings and answer 
    - http://stackoverflow.com/questions/27215013/check-if-my-application-has-usage-access-enabled