-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class com.android.vending.licensing.ILicensingService
-keep public class com.google.android.gms.maps.GoogleMap

-keep public class custom.components.**

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

-keepclassmembers class * {
 public void onClickButton1(android.view.View);
 public void onClickButton2(android.view.View);
 public void onClickButton3(android.view.View);
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}



# Other
-dontwarn com.google.android.gms.**
-dontwarn java.lang.invoke**


# Square
-dontwarn com.squareup.okhttp.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-dontwarn okio.**

# GSON
-keepattributes Signature
-keepclassmembers class com.davale.sasabus.network.rest.model.** { <fields>; }
-keepclassmembers class com.davale.sasabus.network.rest.response.** { <fields>; }
-keepattributes *Annotation*

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