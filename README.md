# MawiBluetoothDemo

This library powered by Kotlin language and supports AndroidX and RxJava 2.x.

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

    implementation 'band.mawi.android:bluetooth:2.1.1'
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
Failed to resolve: band.mawi.android:bluetooth:2.1.1
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

##### Kotlin

```kotlin
val client = MawiBluetooth.client()
```

##### Java

```java
MawiBluetoothClient client = MawiBluetooth.INSTANCE.client(RetainMode.RELEASE_DISCONNECT);
```

## Usage
### 1. Turning bluetooth on

The library does _NOT_ handle managing the state of the BluetoothAdapter.
<br>Direct managing of the state is not recommended as it violates the application user's right to manage the state of their phone. See `Javadoc` of [BluetoothAdapter.enable()](https://developer.android.com/reference/android/bluetooth/BluetoothAdapter.html#enable()) method.
<br>It is the user's responsibility to inform why the application needs Bluetooth to be turned on and for ask the application's user consent.
<br>It is possible to show a native activity for turning the Bluetooth on by calling:

```Kotlin
val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
startActivityForResult(intent, ENABLE_BLUETOOTH_REQUEST);
```

### 2. Search devices

##### Kotlin
```Kotlin

val disposable: Disposable = client.searchDevices()
    .subscribe { searchResult ->
        when (searchResult) {
            is SearchResult.Batch -> {
                // handle result
            }

            is SearchResult.Error -> {
                // handle error
            }
        }
    }

    ...

disposable.dispose()
```

##### Java
```java
Disposable disposable = client.searchDevices()
    .subscribe(
        searchResult -> {
            if (searchResult instanceof SearchResult.Batch) {
                // handle result
            } else if (searchResult instanceof SearchResult.Error) {
                // handle error
            }
        }
    );

// When done, just dispose
disposable.dispose();
```

### 3. Connection
For further interactions with Mawi Band the connection is required.

```Kotlin
client.connect(macAddress)
```


### 4. Observing connection state
Before connection establishment you need to subscribe for connection _state_ changes with device.
```Kotlin
val disposable = client.connectionState()
    .subscribe { state ->
        when (state) {
            DEVICE_READY_FOR_USE -> {}
            CONNECTED -> {}
            DISCONNECTED -> {}
            CONNECTING -> {}
            DISCONNECTING -> {}

        }
    }

// When done, just dispose
disposable.dispose()
```

### 5. Disconnect
When you need to disconnect from device use following snippet:
```Kotlin
client.disconnect()
```

### 6. Unpair
When you need to unpair the device use following snippet. _NOTE:_ after calling the `unpair()` function, the disconnect is queued automatically.
```kotlin
client.unpair()
```

### 7. Handling `OperationResult<T>`
For ease of use, the special `OperationResult<T>` class has been defined, which has child classes such as `Success` and `Failure`.

There are some examples of handling results.

##### On Kotlin

```Kotlin
fun handleResult(result: OperationResult<T>) {
    when (result) {
        is OperationResult.Success -> {
            val data = result.data ?: return
            // do some stuff
        }
        is OperationResult.Failure -> {
            val message = result.message
            val status = result.status
            // handle error
        }
    }
}
```
##### On Java
```Java
public void handleResult(OperationResult<T> result) {
    if (result instanceof OperationResult.Success) {
        T data = ((OperationResult.Success<T>) result).getData();
        // do some stuff
    } else if (result instanceof OperationResult.Failure) {
        String message = ((OperationResult.Failure) result).getMessage();
        int status = ((OperationResult.Failure) result).getStatus();
        // handle error
    }
}
```

## Interaction
This section describes available services on band and code for interaction with them.

### Device information
For getting device information like device name, current firmware and hardware revision, model number and manufacturer you can use following snippet:
```Kotlin
val diposable = client.deviceInformation()
    .subscribe { result ->
        handleResult(result)
    }

...

disposable.dispose()
```

### Battery level
This function returns an `Observable` that emits `BatteryLevel` which contains two parameters: `level` and `mode`.
```Kotlin
val disposable = client.batteryLevel()
    .subscribe { result ->
        handleResult(result)
    }

...

disposable.dispose()
```

### Band location
You can read and write band location.

>_NOTE: This is an important feature because it determines your ECG whether inverted or not._

`BandLocation` states:

*   ***`Location.LEFT` - set if you wearing band on left hand;***
*   ***`Location.RIGHT` - set if you wearing band on right hand***

```Kotlin

client.setBandLocation(Location.LEFT)
```


### Notifications
The device supports two types of notifications: Alarms and Reminders.

You can set no more than 5 notifications for each type.

If you set an empty array for alarms or reminders – it will be disabled.

#### Alarms

```kotlin
val alarms = arrayOf<Alarm>(
    ...
)

...

client.setAlarms(alarms)
```

#### Reminders

```kotlin
val reminders = arrayOf<Reminder>(
    ...
)

...

client.setReminders(reminders)
```

### Clear data on the device
If you want to clear all data on the device use following snippet. Note that this function deletes all data from the device.

```kotlin
client.clearData()
    .subscribe {
        // do stuff
    }
```

## Controllers

The `Controller` is a unified interface that provides data flow from different data sources, which are combined in a specific controller for ease of use.

Main parts of the Controller is a `data()` and `events()`.

*   ***`data()` - this function returns infinite Observable that emitting results according to of data source type. Emitting automatically started and stopped based on the `Observable` lifecycle.***
*   ***`events()` - this function returns an infinite Observable, which notifies of events that occur inside the `Controller`. Events will come as long as there is a subscription on the `data()` function.***

Controllers may have some additional functions depending on the implementation. Here are some of them:

### Screening Controller

#### Raw ECG stream
```Kotlin
val disposable = client.screeningController().data()
    .subscribe { result ->
        handleResult(result)
    }

// When done, just dispose
disposable.dispose()
```

#### ECG stream events
```Kotlin
val disposable = client.screeningProvider().events()
    .subscribe { event ->
        when (event) {
            is ScreeningEvent.PackageLoss -> {
                // handle package loss
            }
            is ScreeningEvent.Session -> {
                // handle session change
            }
        }

    }

// When done, just dispose
disposable.dispose()
```

#### Heart rate
This is additional function in `ScreeningController`. It works only when function `data()` has subscription (but in some versions of firmware it may be working without it).
```Kotlin
val disposable = client.screeningController().heartRate()
    .subscribe { result ->
        handleResult(result)
    }

// When done, just dispose
disposable.dispose()
```

### Fitness Controller

#### Fitness history stream
This function provides endless `Observable` that emits fitness history. The main feature of this function is the history subtraction from the device’s flash memory. When a certain amount of fitness data is typed on the device, after subscribing to this function, they all get subtracted. After that, the data will arrive at intervals of one minute.
Emitting automatically stopped based on the `Observable` lifecycle.

```Kotlin
val disposable = client.fitnessController().data()
    .subscribe { result ->
        handleResult(result)
    }

disposable.dispose()
```

#### Fitness history stream events
```Kotlin
val disposable = client.fitnessController().events()
    .subscribe { event ->
        when (event) {
            is FitnessEvent.Started -> {
               // handle this event
            }

            FitnessEvent.Stoped -> {
                // handle this event
            }
        }
    }

disposable.dispose()
```

#### Daily fitness data
This part of the `FitnessController` returns the daytime activity, such as how many steps you have gone, the calories spent, what distance you have passed, and how much active time was for the day.

```Kotlin
val disposable = client.fitnessController().dailyFitnessData()
    .subscribe { result ->
        handleResult(result)
    }

// When done, just dispose
disposable.dispose()
```

#### Body Metrics
An important part of the `FitnessController`, as the calculations of the remaining indicators are based on data that are presented by `BodyMetrics`.

The following are the parameters and their types:
*   ***`sex` - are presented in Sex.OTHER(defAvgStep = 72 cm), Sex.MALE(defAvgStep = 76 cm), Sex.FEMALE(defAvgStep = 64 cm);***
*   ***`age` - are presented in years;***
*   ***`height` - are presented in cm;***
*   ***`weight` - are presented in kg;***
*   ***`avgStep` - are presented in cm;***

For write use following snippet:
```Kotlin

val bodyMetrics = BodyMetrics(
    sex = Sex.MALE,
    age = 30,
    height = 170,
    weight = 75,
    avgStep = Sex.MALE.avgStep
)

client.setBodyMetrics(bodyMetrics)

```

### For more examples see the sample project.

### Version 1.x

The BLE library v 1.x is no longer supported. Please migrate to 2.x for bug fixing releases.