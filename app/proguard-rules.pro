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
-dontpreverify
-overloadaggressively

-obfuscationdictionary obf-dict.txt
-classobfuscationdictionary obf-dict.txt
-repackageclasses "re.limus.timas.random"

-keep class re.limus.timas.hook.HookEntry { *; }
-keep,allowobfuscation class re.limus.timas.ui.SettingActivity { *; }

# ProtoBuf 相关
-keepclassmembers public class * extends com.google.protobuf.MessageLite {*;}

# 动态字节库 不排除可能会 java.lang.ExceptionInInitializerError
-keep class net.bytebuddy.** {*;}

# java.lang.IllegalStateException: Could not resolve dispatcher: j1.b.translate [class h1.a, class [B, class j1.a, class i1.a, class com.android.dx.dex.file.c]
-keep class com.android.dx.** {*;}

# Base
-dontwarn javax.**
-dontwarn java.**

# ByteBuddy
-dontwarn com.sun.**
-dontwarn edu.umd.**