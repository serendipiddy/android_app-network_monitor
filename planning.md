# Planning the App

## App Objectives

Read the network traffic and events from the game from logcat, matching network events with those from within the game. This should be possible using the game's UID -- on Android this is the same thing as Linux, as uniquely identifies each app/package on the phone [Source](http://stackoverflow.com/questions/5708906/what-is-uid-on-android).

## Getting the UIDs from the phone

Android API methods that might be useful:

* android.pm.PackageManager:
	* [getPackageInfo()](https://developer.android.com/reference/android/content/pm/PackageManager.html#getPackageInfo(java.lang.String, int))
	* [getApplicationInfo()](https://developer.android.com/reference/android/content/pm/PackageManager.html#getApplicationInfo(java.lang.String, int))
	* [getInstalledApplications()](https://developer.android.com/reference/android/content/pm/PackageManager.html#getInstalledApplications(int))

## Reading the Game Events

Will be using the [system log](https://developer.android.com/reference/android/util/Log.html) to track this as the game prints in-game events to this openly. It appears pretty easy, if the exec() code works as it appears to, and does so without needing to root.

```
try {
  Process process = Runtime.getRuntime().exec("logcat");
  BufferedReader bufferedReader = new BufferedReader(
  new InputStreamReader(process.getInputStream()));

  StringBuilder log=new StringBuilder();
  String line = "";
  while ((line = bufferedReader.readLine()) != null) {
    log.append(line);
  }
  TextView tv = (TextView)findViewById(R.id.textView1);
  tv.setText(log.toString());
  } 
catch (IOException e) {}
```

## Monitoring Network Traffic
Track the network stats of the UID for the game. This includes packet counts and data counts, done with the [TrafficStats class](https://developer.android.com/reference/android/net/TrafficStats.html). [Example of this being used](http://stackoverflow.com/questions/17674790/how-do-i-programmatically-show-data-usage-of-all-applications).

