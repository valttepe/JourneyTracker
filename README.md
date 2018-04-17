# JourneyTracker
Android sensors course project

JourneyTracker is your buddy when you go to hike, to walk or to do anything outside! First you can add targets (markers) in a map to ensure you remember to visit places you have planned to see during your exercise. Targets can also be dragged and removed. Then your route is being tracked with a real-time meter calculator and stopwatch. When your exercise is over you can inspect your route as a drawn path in a map and see the meters and minutes you used and also the targets you set earlier. The app also includes two compasses, one uses an internal sensor and another uses an external sensor (Metawear).

Hidden features: You can drag a marker by holding and then moving it. Remove a marker by tapping it and pressing the title "Remove a marker". Tap the map so you can explore the view freely. Center button appears which allows you to center camera back to your current location. 

ATTENTION! When an exercise is started, it takes one minute to start tracking. This is because we create a location request in the same view, so first locations are not accurate. If this app will be developed further, location request will be done when the app starts to make location accurate in that point when exercise should begin.



## Requirements
- Minimum Android version: Android 5.0 API 21
- Targeted for: Android 7.1.1 API 25

## Testing report
- [https://docs.google.com/document/d/1bj5q7X9Fx7YF_oet4s_mtoXkWpb7gxQ-kJyQnlPZR48/edit?usp=sharing](https://docs.google.com/document/d/1bj5q7X9Fx7YF_oet4s_mtoXkWpb7gxQ-kJyQnlPZR48/edit?usp=sharing)

## UI Documentation
- [https://drive.google.com/open?id=1CrizKgJuNukQJN_pgZfcedjr64Gchd0K](https://drive.google.com/open?id=1CrizKgJuNukQJN_pgZfcedjr64Gchd0K)

## Optional
- MbientLab MetaMotionC external sensor.

## Installation
1. Clone the repository:
```git clone https://github.com/valttepe/JourneyTracker```

2. Open project in Android Studio

   Download Android Studio: [https://developer.android.com/studio/index.html](https://developer.android.com/studio/index.html)

3. Configure your MetaMotionC external sensor (optional)

   Download and install MetaWear app from Google Play:
[https://play.google.com/store/apps/details?id=com.mbientlab.metawear.app](https://play.google.com/store/apps/details?id=com.mbientlab.metawear.app)

   Connect to your MetaMotionC external sensor and check devices MAC address.

   In Android Studio open class `MetaWearFragment` and find line: 

   `private final String MW_MAC_ADDRESS = ...;` 

   Insert your sensors MAC address as value for `MW_MAC_ADDRESS`.

5. Run Application

   Attach your Android device to your computer. In Android Studio, click `Run (app)` in the toolbar. Select your device and click OK.

   Now you're all set. Thank you for downloading our app!
