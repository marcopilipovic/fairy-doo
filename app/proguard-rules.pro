# kotlinx.serialization: Serializer-Companions der @Serializable-Klassen erhalten
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class ug.humb.fairydoku.** {
    *** Companion;
}
-keepclasseswithmembers class ug.humb.fairydoku.** {
    kotlinx.serialization.KSerializer serializer(...);
}
