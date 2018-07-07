# PrefX

![CircleCI branch](https://img.shields.io/circleci/project/github/Glassdoor/PrefX/master.svg)

Declarative extensions on SharedPreferences and Editor with the help of annotation processing.

## Download


![Maven Central](https://img.shields.io/maven-central/v/com.shaishavgandhi/prefextensions-compiler.svg)


```groovy
dependencies {
  compileOnly 'com.shaishavgandhi:prefextensions-annotations:x.y.z'
  kapt 'com.shaishavgandhi:prefextensions-compiler:x.y.z'
}
```

Snapshots of the development version are available in [Sonatype's snapshots repository.](https://oss.sonatype.org/content/repositories/snapshots/)


## Use Case

Often times, SharedPreference getters and setters are non declarative and not the most readable. Typically, to get a value from SharedPreference, you do a variation of:

```kotlin
val sharedPreferences = context.getSharedPreferences("file_name", Context.MODE_PRIVATE)
val appStartCount = sharedPreferences.getLong("appStartCount", 0)
```

To insert a value, you do:
```kotlin
val editor = sharedPreferences.edit()
editor.putLong("appStartCount", 1)
```
This isn't the most readable API. Without reading the key, it's hard to know what we're going for. 

## Usage

PrefX creates a readable API by generating extensions on `SharedPreferences` and `SharedPreferences.Editor`. The same example would be something like:

#### Declare your preference variable:
```kotlin
class MyPreferences {
  @Preference private val appStartCount: Long? = null
}
```

#### Get that value
```kotlin
// Or get from your favorite DI
val sharedPreferences = context.getSharedPreferences("file_name", Context.MODE_PRIVATE) 
val appStartCount = sharedPreferences.getAppStartCount() // Generated extension
```

#### Set the value
```kotlin
val editor = sharedPreferences.edit() // Or get from your favorite DI
editor.putAppStartCount(2) // Generated extension
```

## Default Values

SharedPreference reads require a default value to be given. You can easily define the default value for your given preference with an optional parameter:

```kotlin
@Preference(defaultLong = 1L) 
private val appStartCount: Long? = null

@Preference(defaultString = "hello world")
private val greetingText: String? = null
```

PrefX also takes advantage of Kotlin's default values in parameters and the generated code creates an optional parameter in the getter. This is useful for one off-ing a different defaultValue than the one you provided in the annotation.

**Generated Code**
```kotlin
fun SharedPreferences.getAppStartCount(defaultValue: Long = 1): Long = getLong("appStartCount", defaultValue)
```

**Usage**
```kotlin
preferences.getAppStartCount(defaultValue = 20) 
```


## Custom Keys

PrefX can be added incrementally into your project. If you already have a SharedPreference entry defined with a particular key that is not very reader friendly (like "app_launch_count"), you can easily define a custom key to your Preference declaration. 

Using this, you can continue to use the newer API in some cases, while still supporting access through the usual SharedPreference API.

```kotlin
@Preference(key = "app_launch_count") 
private val appStartCount: Long? = null
```


## License
    
    Copyright 2018 Glasdoor, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
