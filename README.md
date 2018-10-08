# MawiBluetoothDemo

This library based on RxJava2.X.

## Description

This project uses Mawi Solutions Bluetooth SDK. Before using project contact us via [web-site](https://research.mawi.band/) to get credentials for SDK access


## Installation

### 1. Download

First of all, you need to add following lines in your _app_ `build.gradle`.

```groovy
apply plugin: 'com.android.application'

Properties properties = new Properties()
properties.load(project.rootProject.file("local.properties").newDataInputStream())

repositories {
    maven {
        url  "<URL>"
        credentials {
           username properties.getProperty("mawi.username")
           password properties.getProperty("mawi.apikey")
        }
    }
}

android {
    ...
}

...

dependencies {
    ...

    // rx java
    implementation "io.reactivex.rxjava2:rxjava:2.2.2"
    implementation "io.reactivex.rxjava2:rxandroid:2.1.0"

    implementation 'band.mawi.android:bluetooth:1.2.0'
    ...
}
```

In `local.properties` file:
```grovy
...

mawi.username=<YOUR_USER_NAME>
mawi.apikey=<YOUR_API_KEY>

...
```

### *Important!*

If you got this message:

```
Failed to resolve: band.mawi.android:bluetooth:1.2.0
```

You need to download `mawi-bluetooth.aar` and place this file in _libs_ folder of your _app_ module.
Then add following lines to your application level `build.gradle`

```groovy
apply plugin: 'com.android.application'

...

repositories {
     flatDir {
        dirs 'libs'
     }

     ...
}

android {
    ...
}

...

dependencies {
    ...

    // rx java
    implementation "io.reactivex.rxjava2:rxjava:2.2.2"
    implementation "io.reactivex.rxjava2:rxandroid:2.1.0"

    implementation "no.nordicsemi.android:dfu:1.7.0"
    implementation "com.polidea.rxandroidble2:rxandroidble:1.7.0"
    implementation 'com.jakewharton.rx2:replaying-share:2.0.1'

    implementation(name: 'mawi-bluetooth', ext: 'aar')
    ...
}
```

### 2. Permissions
```xml
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

If you are using SDK 23+ you need to enable this permissions manually (you can use [RxPermissions](https://github.com/tbruyelle/RxPermissions) or whatever else you want).

For more information about why you should use this permissions check official [documentation](https://developer.android.com/about/versions/marshmallow/android-6.0-changes#behavior-hardware-id).

### 3. Obtaining the client

It's your job to maintain single instance of the client. You can use singleton, scoped [Dagger](http://google.github.io/dagger/) component or whatever else you want.

```java
MawiBluetoothClient client = MawiBluetoothClient.initialize(context);
```

### 4. Start Current Time Service
After client obtaining you need to start Current Time Service (CTS) for sync date-time with your peripheral. When the peripheral are connected it must automatically synchronize the time.

To start the server use following snippet
```java
client.timeService().startService();
```

For receiving notifications about CTS status you need to subscribe on their events
```java
...
private TimeServiceActionListener listener = new TimeServiceActionListener() {
    @Override
    public void onGattServerStart() {

    }

    @Override
    public void onGattServerStop() {
    }

   ...
};

...
client.timeService().setOnTimeServiceActionListener(listener);
```

To stop this service
```java
client.timeService().stopService();
```

## Usage
### 1. Turning bluetooth on

The library does _NOT_ handle managing the state of the BluetoothAdapter.
<br>Direct managing of the state is not recommended as it violates the application user's right to manage the state of their phone. See `Javadoc` of [BluetoothAdapter.enable()](https://developer.android.com/reference/android/bluetooth/BluetoothAdapter.html#enable()) method.
<br>It is the user's responsibility to inform why the application needs Bluetooth to be turned on and for ask the application's user consent.
<br>It is possible to show a native activity for turning the Bluetooth on by calling:
```java
Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
startActivityForResult(intent, ENABLE_BLUETOOTH_REQUEST);
```

### 2. Search devices
```java
Disposable scanSubscription = client.scanDevices()
    .subscribe(
        scanResult -> {
           // do something
        },
        throwable -> {
           // Handle error there
        }
    );

// When done, just dispose
scanSubscription.dispose();
```

### 3. Observing client state
On Android it is not always trivial to determine if a particular BLE operation has a potential to succeed. i.e. to scan on Android 6.0 the device needs to have a `BluetoothAdapter`, the application needs to have a granted permission to use either `ACCESS_COARSE_LOCATION` or `ACCESS_FINE_LOCATION` and `Location Services` needs to be turned on.
To be sure that the scan will work only when everything is ready you could use:
```java
Disposable stateChangesSubscription = client.observeStateChanges()
    .switchMap(state -> { // switchMap makes sure that if the state will change the  will dispose and thus end the scan
        switch (state) {
            case READY:
                // everything should work
                return client.scanDevices();
            case BLUETOOTH_NOT_AVAILABLE:
                // basically no functionality will work here
            case LOCATION_PERMISSION_NOT_GRANTED:
                // scanning and connecting will not work
            case BLUETOOTH_NOT_ENABLED:
                // scanning and connecting will not work
            case LOCATION_SERVICES_NOT_ENABLED:
                // scanning will not work
            default:
                return Observable.empty();
         }
    }).subscribe(
        scanResult -> {
            // do something
        }, throwable -> {
            // Handle error there
        }
    );

// When done, just dispose
stateChangesSubscription.dispose();
```

### 4. Connection
For further interactions with Mawi Band the connection is required.
```java
client.connect(macAddress);
```

### 5. Observing connection state
Before connection establishment you need to subscribe for connection _state_ changes with device
```java
Disposable connectionStatesSuscription = client.observeConnectionChanges()
    .subscribe(
        connectionState -> {
            switch(connectionState) {
                case ConnectionState.CONNECTING:
                    // do something
                    break;
                case ConnectionState.CONNECTED:
                    // do something
                    break;
                case ConnectionState.DISCONNECTED:
                    // do something
                    break;
                 case ConnectionState.DISCONNECTING:
                    // do something
                    break;
            }
        }
    );

// When done, just dispose
connectionStatesSuscription.dispose();
```

### 6. Disconnect
When you need to disconnect from device use following snippet:
```java
client.disconnect();
```

## Interaction
This section describes available services on band and code for interaction with them.

### Device Manager

#### Device information
For getting device information like device name, MAC-address, current firmware and hardware revision, model number and manufacturer you can use following snippet:
```java
client.deviceManager().readDeviceInformation()
    .subscribe(
        deviceInfo -> {
            //
        },
        throwable -> {
            // Handle error there
        }
    );
```
#### Battery level
You can both read and observe battery level. To get the initial battery level and then observe changes you can use:
```java
Disposable batterySubscription = Observable.merge(client.deviceManager().readBatteryLevel(), client.deviceManager.observeBatteryLevel())
    .subscribe(
        battery -> {
            // returning battery level
        },
        throwable -> {
            // Handle error there
        }
    );

//
batterySubscription.dispose();
```

#### Band location
You can read and write band location.

>_NOTE: This is an important feature because it determines your ECG whether inverted or not._

BandLocation states:

*   ***`BandLocation.LEFT_HAND` - set if you wearing band on left hand;***
*   ***`BandLocation.RIGHT_HAND` - set if you wearing band on right hand***

```java
// Read
client.deviceManager().readBandLocation()
    .subscribe(
        bandLocation -> {
            // do something
        },
        throwable -> {
            // Handle error there
        }
    );

// Write
client.deviceManager().setBandLocation(BandLocation.LEFT_HAND) // or BandLocation.RIGHT_HAND
    .subscribe(
       () -> {
            // band-location successfully set
       },
       throwable -> {
            // Handle error there
       }
    );
```

### ECG service
#### Raw ECG stream
```java
Disposable ecgStreamSubscription = client.ecgService().observeECGStream()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(
        ecgPacket -> {
            // handle ECG data
        },
        throwable -> {
            // Handle error there
        }
    );

// When done, just dispose
ecgStreamSubscription.dispose();
```

#### ECG stream session
```java
Disposable ecgStreamSessionSubscription = client.ecgService().observeSession()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(
        ecgSession -> {
            // handle ECG stream sesion data
        },
        throwable -> {
            // Handle error there
        }
    );

// When done, just dispose
ecgStreamSessionSubscription.dispose();
```

#### Heart rate
```java
Disposable heartRateSubscription = client.ecgService().observeHeartRate()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(
        heartRate -> {
            // handle heart rate data
        },
        throwable -> {
            // Handle error there
        }
    );

// When done, just dispose
heartRateSubscription.dispose();
```

### Fit service
#### Daily goals
```java
// Read
client.fitService().readGoals()
    .subscribe(
        fitGoals -> {
            // handle fit goals data
        },
        throwable -> {
            // Handle error there
        }
    );

// Write

int steps = DefaultValues.DEF_DAILY_STEPS_GOAL; // steps
int distance = 4000; // in meters ~ 4 km
int calories = 4000; // in calories ~ 4 kcal
int activeTime = 120; // in minutes ~ 2 hours

FitProgress fitProgress = new FitProgress(steps, distance, colories, activeTime);
FitGoals fitGoals = new FitGoals(fitProgress);

client.fitService().writeGoals(fitGoals)
    .subscribe(
        () -> {
            // daily goals successfully set
        },
        throwable -> {
            // Handle error there
        }
    );
```

#### Fit Information
`FitInfo` give information about available activity records history. In other words: fit records count.
```java
client.fitService().readFitInfo()
    .subscribe(
        fitInfo -> {
            // handle  fit info
        },
        throwable -> {
            // handle error there
        }
    );
```
You can observe changes of `FitInfo` by using snippet bellow (temporary, for some reasons it's not working)
```java
Disposable fitInfoSubscription = client.fitService().observeFitInfo()
    .subscribe(
        fitInfo -> {
            // handle received fit info
        },
        throwable -> {
            // handle error there
        }
    );

...
fitInfoSubscription.dispose();
```

To get the initial `FitInfo` and then observe changes you can use:
```java
Disposable fitInfoSubscription = Observable.merge(client.fitService().readFitInfo(), client.fitService().observeFitInfo())
    .subscribe(
        fitInfo -> {
            // handle received fit info
        },
        throwable -> {
            // handle error there
        }
    );

...

fitInfoSubscription.dispose();
```

#### Activity history request/response
According above snippet, after calling `client.fitService().readFitInfo()` you receive `FitInfo` that provides fit records count.
For example, you have received:
```json
{"recordsCount":1500}
```

Based on this data you can split it on `100` parts, in other words `1500 / DefaultConfig.MAX_FIT_RECORDS_BATCH_SIZE = 100` (_for more information about `DefaultConfig.MAX_FIT_RECORDS_BATCH_SIZE` see documentation_).
```java
...

Deque<Integer> deque = new ArrayDeque<>();

...

int count = fitInfo.recordsCount / DefaultConfig.MAX_FIT_RECORDS_BATCH_SIZE;
for (int i = 0; i < count; i++) {
    deque.add(DefaultConfig.MAX_FIT_RECORDS_BATCH_SIZE);
}

// this is necessary when the count is not an integer
int diff = fitInfo.recordsCount - count * DefaultConfig.MAX_FIT_RECORDS_BATCH_SIZE;
deque.add(diff);

...
```

Before sending request, you need to subscribe for `FitResponse` updates.
```java
Disposable fitRequestSubscription = client.fitService().observeActivityHistory()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(
        fitResponse -> {
            // do whatever you want like save to db etc.
            // you can check result code
            // here you can make a request for the next piece of data
        },
        throwable -> {
            // handle error there
        }
    );

...

// When done, just dispose
fitRequestSubscription.dispose();
```

After above operations you need to create _header_ and just send to the device.
```java
...

// initial values
int recordsCount = deque[0]; // allowed value in range [1..15]
int startId = 0; // id of the first record in the sequence, last id available is equal fitInfo.recordsCount - 1

...

FitRecordHeader header = new FitRecordHeader(recordsCount, startId);

client.fitService().requestActivityHistory(header)
    .subscribe(
        () -> {
            // header successfully set
        },
        throwable -> {
            // handle error there
        }
    );

...
```

#### Fit state
This part of the Fit Service returns the daytime activity, such as how many steps you have gone, the calories spent, what distance you have passed, and how much active time was for the day.
You can both read and observe activity.
```java
// For read
client.fitService().readFitState()
    .subscribe(
        fitState -> {
            // do whatever you want
        },
        throwable -> {
            // handle error there
        }
    );

// For observe
Disposable fitStateSubscription = client.fitService().observeFitState()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(
        fitState -> {
            // whatewer you want
        },
        throwable -> {
            // handle error there
        }
    );

// When done, just dispose
fitStateSubscription.dispose();
```

But you can also get the initial `FitState` and then observe changes you can use:
```java
Disposable fitStateSubscription = Observable.merge(client.fitService().readFitState(), client.fitService().observeFitState())
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(
        fitState -> {
            // whatewer you want
        },
        throwable -> {
            // handle error there
        }
    );

// When done, just dispose
fitStateSubscription.dispose();
```

#### Body Metrics
An important part of the Fit Service, as the calculations of the remaining indicators are based on data that are presented by `FitBodyMetrics`.

The following are the parameters and their types:
*   ***`sex` - are presented in Sex.OTHER, Sex.MALE, Sex.FEMALE;***
*   ***`age` - are presented in years;***
*   ***`height` - are presented in cm;***
*   ***`weight` - are presented in kg;***
*   ***`avgStep` - are presented in cm (NB new experimental optional feature);***

You may use default values for each parameter:
*   ***`DefaultConfig.DEFAULT_AGE` - described as 30 years old;***
*   ***`DefaultConfig.DEFAULT_HEIGHT` - described as 170 cm;***
*   ***`DefaultConfig.DEFAULT_WEIGHT` - described as 70 kg;***
*   ***`DefaultConfig.DEFAULT_MALE_AVERAGE_STEP_LENGTH` - described as 76 cm for male gender;***
*   ***`DefaultConfig.DEFAULT_FEMALE_AVERAGE_STEP_LENGTH` - described as 67 cm for female gender;***
*   ***`DefaultConfig.DEFAULT_AVERAGE_STEP_LENGTH` - described as 72 cm for others;***

For read/write use following snippets:
```java
// For write
FitBodyMetrics bodyMetrics = new FitBodyMetrics(Sex.MALE, DefaultConfig.DEFAULT_AGE, DefaultConfig.DEFAULT_HEIGHT, DefaultConfig.DEFAULT_WEIGHT, DefaultConfig.DEFAULT_MALE_AVERAGE_STEP_LENGTH);

client.fitService().writeBodyMetrics(bodyMetrics)
    .subscribe(
        () -> {
           // do whatever you want
        },
        throwable -> {
            // handle error there
        }
    );

// For read
client.fitService().readBodyMetrics()
    .subscribe(
        bodyMetrics -> {
            // do whatever you want
        },
        throwable -> {
            // handle error there
        }
    );
```

### Update firmware service (Over-the-air update)

#### Describe the Service
Extend the `BaseUpdateFirmwareService` in your project and implement the protected Class<? extends Activity> getNotificationTarget() method. This method should return an activity class that will be open when you press the DFU notification while transferring the firmware. This activity will be started with the 'Intent.FLAG_ACTIVITY_NEW_TASK' flag.
```java
package your.package.name;

import band.mawi.android.bluetooth.service.BaseUpdateFirmwareService;
import android.app.Activity;

public class UpdateService extends BaseUpdateFirmwareService {

    @Override
    protected Class<? extends Activity> getNotificationTarget() {
       /**
         * As a target activity the NotificationActivity is returned, not the MainActivity. This is because
         * the notification must create a new task:
         *
         * intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         *
         * when you press it. You can use NotificationActivity to check whether the new activity
         * is a root activity (that means no other activity was open earlier) or that some
         * other activity is already open. In the latter case the NotificationActivity will just be
         * closed. The system will restore the previous activity. However, if the application has been
         * closed during upload and you click the notification, a NotificationActivity will
         * be launched as a root activity. It will create and start the main activity and
         * terminate itself.
         *
         * This method may be used to restore the target activity in case the application
         * was closed or is open. It may also be used to recreate an activity history using
         * startActivities(...).
         */
         return NotificationActivity.class;
    }
}
```

Remember to add your service to _AndroidManifest.xml_.

#### Add target Activity

You may use the following class in order to prevent starting another instance of your application:
```java
package your.package.name;

import your.package.name.MainActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class NotificationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If this activity is the root activity of the task, the app is not running
        if (isTaskRoot()) {
            // Start the app before finishing
            final Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtras(getIntent().getExtras()); // copy all extras
            startActivity(intent);
        }

        // Now finish, which will drop you to the activity at which you were at the top of the task stack
        finish();
    }
}
```

#### Start update

To start the update firmware service use following code:
```java
// fileStreamUri - the URI of the zip-file
// filePath - the path of the zip-file
client.updateFirmwareService(UpdateService.class, macAddress, fileStreamUri, filePath);
```


#### Receive notifications about update progress

If you want to receive notifications about progress of update you can implement the `UpdateFirmwareProgressListener`:
```java
...

private final UpdateFirmwareProgressListener listener = new UpdateFirmwareProgressListener() {

    @Override
    public void onProgressChanged(@Nullable String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int totalParts) {
        mProgressBar.setProgress(percent);
        mTextPercentage.setText(percent + "%");
    }

    @Override
    public void onDeviceConnecting(@Nullable String deviceAddress) {
        mProgressBar.setIndeterminate(true);
        mTextPercentage.setText(R.string.status_connecting);
    }

    @Override
    public void onProcessStarted(@Nullable String deviceAddress) {
        mProgressBar.setIndeterminate(false);
        mProgressBar.setProgress(0);
        mTextPercentage.setText(0 + "%");
    }

    ...
};

...

@Override
protected void onResume() {
    super.onResume();
    client.updateFirmwareService().registerProgressListener(listener);
}

...

@Override
protected void onPause() {
    super.onPause();
    client.updateFirmwareService().unregisterProgressListener();
}
```
