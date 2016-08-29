# Planning the App

## App Objectives

Read the network traffic and events from the game from logcat, matching network events with those from within the game.

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
Track the network stats of the UID for the game. This includes packet counts and data counts, done with the [TrafficStats class](https://developer.android.com/reference/android/net/TrafficStats.html). [Example of this being used](http://stackoverflow.com/questions/17674790/how-do-i-programmatically-show-data-usage-of-all-applications)

