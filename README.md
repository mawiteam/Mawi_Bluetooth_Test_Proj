# MawiBluetoothDemo

This library based on RxJava2.X.

## Usage
### Download

First of all, you need to add following lines in your _app_ `build.gradle`.

```grovy
apply plugin: 'com.android.application'

Properties properties = new Properties()
properties.load(project.rootProject.file("local.properties").newDataInputStream())

repositories {
    maven {
        url  "https://mawisolutions.bintray.com/MawiBluetooth"
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

    implementation 'band.mawi.android:bluetooth:1.0.0'
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

### Obtaining the client

It's your job to maintain single instance of the client. You can use singleton, scoped [Dagger](http://google.github.io/dagger/) component or whatever else you want.

```java
MawiBluetoothClient client = MawiBluetoothClient.initialize(context);
```

### Permissions
```xml
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

If you are using SDK 23+ you need to enable this permissions manually (you can use [RxPermissions](https://github.com/tbruyelle/RxPermissions) or whatever else you want).

### Turning bluetooth on

The library does _NOT_ handle managing the state of the BluetoothAdapter.
<br>Direct managing of the state is not recommended as it violates the application user's right to manage the state of their phone. See `Javadoc` of [BluetoothAdapter.enable()](https://developer.android.com/reference/android/bluetooth/BluetoothAdapter.html#enable()) method.
<br>It is the user's responsibility to inform why the application needs Bluetooth to be turned on and for ask the application's user consent.
<br>It is possible to show a native activity for turning the Bluetooth on by calling:
```java
Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
startActivityForResult(intent, ENABLE_BLUETOOTH_REQUEST);
```

### Search devices
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

### Observing client state
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

### Connect
For further interactions with Mawi Band the connection is required.
```java
client.connect(macAddress);
```

### Observing connection state
Before connection establishment you need to subscribe for connection _state_ changes with device
```java
Disposable connectionStatesSuscription = client.observeConnectionChanges()
    .subscribe(
        connectionState -> {
            switch(connectionState) {
                case RxBleConnectionState.CONNECTING:
                    // do something
                    break;
                case RxBleConnectionState.CONNECTED:
                    // do something
                    break;
                case RxBleConnectionState.DISCONNECTED:
                    // do something
                    break;
                 case RxBleConnectionState.DISCONNECTING:
                    // do something
                    break;
            }
        }
    );

// When done, just dispose
connectionStatesSuscription.dispose();
```

### Disconnect
When you need to disconnect from device use following snippet:
```java
client.disconnect();
```

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
```java
// read
client.deviceManager().readBandLocation()
    .subscribe(
        bandLocation -> {
            // do something
        },
        throwable -> {
            // Handle error there
        }
    );

// write
client.deviceManager().setBandLocation(BandLocation.LEFT_HAND)
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

int steps = 8000; // steps
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
            // handle delivered fit info
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
            // handle delivered fit info
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
            // handle delivered fit info
        },
        throwable -> {
            // handle error there
        }
    );

...
fitInfoSubscription.dispose();
```

#### Activity history request
According above snippet, after calling `client.fitService().readFitInfo()` you deliver `FitInfo` that provides fit records count.
For example, you are delivered
```json
{"recordsCount":1500}
```

Based on this data you can split it on `100` parts, in other words `1500 / 15 = 100` (_reasons of it will be explained bellow_).
```java
...

Deque<Integer> deque = new ArrayDeque<>();

...

int count = fitInfo.recordsCount / 15;
for (int i = 0; i < count; i++) {
    deque.add(15);
}

int diff = fitInfo.recordsCount - count * 15;
deque.add(diff);

...

```

*Why do we divide by `15`:*
_According device API max count of delivered activity history records are within range [1..15]_


After above operations you need to create _header_.
```java
int recordsCount = 15;
int startId = 0;
FitRecordHeader header = new FitRecordHeader(recordsCount, startId);
```

Having it you can make request for deliver activity history.
```java
client.fitService().requestActivityHistory(header)
    .subscribe(
        () -> {
            // header successfully set
        },
        throwable -> {
            // handle error there
        }
    );
```
