# Table of contents 

- [What is SASAbus?](#what-is-sasabus)
- [Download](#download)
- [Build](#build)
- [Issues](#issues)
- [Contributions](#contributions)
- [License](#license)

## What is SASAbus?

SASAbus is an [Android](https://android.com) application that provides information about the bus service of [SASA SpA-AG](http://www.sasabz.it). The company's buses operate in Bolzano, Merano and Laives, cities in South Tyrol (Italy). It is developed and maintained by our [community](http://sasabus.org/community).

## Download

You may download the application for free in the [Google Play Store](https://play.google.com/store/apps/details?id=it.sasabz.android.sasabus).

## Build

This is a Gradle-based project that works best with Android Studio.

To build the app:

1. Install the following software:

	- [Android Studio](http://developer.android.com/sdk/installing/studio.html)
	- [Android SDK](http://developer.android.com/sdk/index.html)
	- [Gradle](http://www.gradle.org/downloads)

1. Run the Android SDK Manager by pressing the SDK Manager toolbar button in Android Studio or by running the 'android' command in a terminal window.

1. In the Android SDK Manager, ensure that the following are installed, and are updated to the latest available version:

	- Tools > Android SDK Platform-tools
	- Tools > Android SDK Tools
	- Tools > Android SDK Build-tools
	- Android 6.0 > SDK Platform (API 23)
	- Extras > Android Support Repository
	- Extras > Android Support Library
	- Extras > Google Play services
	- Extras > Google Repository

1. Create a file in your working directory called local.properties, containing the path to your Android SDK.

1. Import the project in Android Studio:

	1. Press File > Import Project
	1. Navigate to and choose the settings.gradle file in this project
	1. Press OK

1. Choose Build > Make Project in Android Studio or run the following command in the project root directory:

	```
	./gradlew clean assembleDebug
	```

1. To install on your test device:

	```
	./gradlew installDebug
	```

## Issues

If you find any bugs or notice incorrect app behavior please let us know by adding an [issue](https://github.com/SASAbus/SASAbus/issues) to this project. We will discuss the problem with you and fix any errors.

## Contributions

Feel free to contribute to this project by creating a [pull request](https://github.com/SASAbus/SASAbus/pulls). We appreciate any help.

## License

```
SASAbus is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SASAbus is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```
