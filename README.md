# JourneyTracker
Android sensors course project



## Requirements
- Minimum Android version: Android 5.0 API 21
- Targeted for: Android 7.1.1 API 25

## Testing report
- [https://docs.google.com/document/d/1bj5q7X9Fx7YF_oet4s_mtoXkWpb7gxQ-kJyQnlPZR48/edit?usp=sharing](https://docs.google.com/document/d/1bj5q7X9Fx7YF_oet4s_mtoXkWpb7gxQ-kJyQnlPZR48/edit?usp=sharing)

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
