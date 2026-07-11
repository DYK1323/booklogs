// Intentionally empty: each module declares its own plugins so that `:domain` (pure Kotlin/JVM, no
// Android dependency) can be configured and tested independently of `:app`'s Android Gradle Plugin,
// which requires Google's Maven (maven.google.com / dl.google.com) — unreachable in some sandboxes.
