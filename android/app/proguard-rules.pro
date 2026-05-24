# Jama ProGuard Rules

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-dontwarn dagger.hilt.internal.aggregatedroot.codegen.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.jama.expense.data.** { *; }

# Vico Charts
-keep class com.patrykandpatrick.vico.** { *; }
