-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 5

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgent
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.app.Fragment
-keep public class com.android.vending.licensing.ILicensingService


# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}


# Keep View constructors
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}


# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class **.R$* {
   public static <fields>;
}


# Other
-dontwarn com.google.android.gms.**
-dontwarn java.lang.invoke**


# Square
-dontwarn com.squareup.okhttp.**
-dontwarn retrofit2.**
-dontwarn okio.**
-keep class retrofit2.** { *; }


# GSON
-keepclassmembers class it.sasabz.android.sasabus.network.rest.model.** { <fields>; }
-keepclassmembers class it.sasabz.android.sasabus.network.rest.response.** { <fields>; }

-keep class it.sasabz.android.sasabus.data.model.JsonSerializable { *; }
-keep class * implements it.sasabz.android.sasabus.data.model.JsonSerializable { <fields>; }


# RxJava
-dontwarn rx.internal.util.**

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}


# Javascript Bridge
-keep public class it.sasabz.android.sasabus.util.map.JSInterface
-keepclassmembers class it.sasabz.android.sasabus.util.map.JSInterface {
    <fields>;
    <methods>;
}

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}