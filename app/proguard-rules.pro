# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
# For more details, see http://developer.android.com/guide/developing/tools/proguard.html

# Keep application class
-keep class com.sanin.tv.App { *; }

# Keep all Activity classes
-keep public class * extends android.app.Activity
-keep public class * extends androidx.appcompat.app.AppCompatActivity
-keep public class * extends androidx.fragment.app.Fragment

# Keep Hilt-generated classes
-keep class dagger.hilt.** { *; }
-keep class **_HiltModules* { *; }
-keep @dagger.hilt.android.HiltAndroidApp class *
-keepclasseswithmembers class * {
    @dagger.hilt.android.AndroidEntryPoint *;
}

# Keep Room entities and DAOs
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Keep Serializable classes
-keepnames class * implements java.io.Serializable

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp & Retrofit
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# ExoPlayer / Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Keep WebView JS interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Suppress warnings for optional dependencies
-dontwarn org.slf4j.**
-dontwarn javax.annotation.**
