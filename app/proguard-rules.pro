# kotlinx.serialization: Serializer-Companions der @Serializable-Klassen erhalten
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class com.fairydoo.game.** {
    *** Companion;
}
-keepclasseswithmembers class com.fairydoo.game.** {
    kotlinx.serialization.KSerializer serializer(...);
}
