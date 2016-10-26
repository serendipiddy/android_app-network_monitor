Android App Network Usage Monitor
=================================

This app reports the past usage time and network traffic of a specified app. The user selects an app they with to examine, and are presented with a summary of the usage of the app and an option to export this information to the storage.

This is useful when investigating the network traffic of a particular application, for research or for curiosity, without diving. Results are given at the Network Layer (Layer 3), including both TCP and UDP traffic.

## Development Details

This application showcases several Android API features:
* Using Android 6.0 (Marshmallow) permissions request model
* Requesting Usage Access permission from the OS
* Accessing OS records of app usage:
  - Reading app usage details (android.app.usage.UsageStats)
  - Reading network usage details from (android.app.usage.NetworkStats), both Wifi and Mobile
* Listing the apps which are currently installed
* Reading/writing to an app's 'external' folder, accessible to other apps and the user

## How it works

In comparison to the solutions explored below, this application requires not active monitoring. Instead it simply queries the OS's own record of usage. A list of installed applications is searched by the user, and a selected application's UID is used to extract the OS's records of network usage and active duration.

This method comes with limited control over what information can be collected and the granularity of measurements.

## Alternative Solutions Explored

### VPN Service
Another solution to this problem is to pass traffic through a device-hosted VPN. This is available through android.net.VpnService, and allows packets to be intercepted as they enter/leave the phone, and telemetry performed.

This is suitable if greater granularity or which access to Link Layer (Layer 2) is required. The issue is the work involved in making a VPN server capable of recording traffic, which is a very big task. The existing, open-source work (Eg. [hexene/LocalVPN](https://github.com/hexene/LocalVPN)) is not fully functioning as was found to produce many bugs and even failing to allow traffic at times. Given time constraints to investigage/fix these issues, and the requirements not requiring this level of granularity, another solution was sought.

### TrafficStats
This solution was initially explored, recording real-time usage with a background service. This reads an integer value for packets and bytes, separate Rx and Tx directions, which can be queried periodically to see usage over time.

Permissions were a problem, as not all phone models allowed open access to the android.net.TrafficStats information. However, which accessible, it did provide control over the granularity of the samples (down to the millisecond..!).
