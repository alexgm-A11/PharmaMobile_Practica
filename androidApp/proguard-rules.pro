# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Modelos DTO serializados por kotlinx.serialization.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep class pe.edu.upeu.pharmamobile.data.remote.dto.** { *; }
-keepclassmembers class **$$serializer { *; }
-keepclassmembers class **$Companion { kotlinx.serialization.KSerializer serializer(...); }
-keep class kotlinx.serialization.** { *; }

# Los módulos Koin se construyen explícitamente, pero se conservan los puntos de entrada.
-keep class pe.edu.upeu.pharmamobile.di.** { *; }
