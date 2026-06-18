import net.neoforged.moddevgradle.dsl.NeoForgeExtension

plugins {
    id("net.neoforged.moddev")
    // Modrinth publishing. Guarded below so it never requires a token at build time.
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}

// Resolve moddev's extension explicitly: Gradle does not generate the `neoForge` accessor for
// Stonecutter's non-`build.gradle.kts` buildscripts.
val neoForge = extensions.getByType(NeoForgeExtension::class.java)
val mcVersion = property("deps.minecraft") as String

base.archivesName = "${property("mod.id")}-neoforge-$mcVersion"
version = "${property("mod.version")}+$mcVersion"
group = property("mod.group") as String

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
}

neoForge.apply {
    version = property("deps.neoforge") as String

    runs {
        register("client") {
            client()
        }
    }

    mods {
        register(property("mod.id") as String) {
            sourceSet(sourceSets["main"])
        }
    }
}

dependencies {
    // MixinExtras (@ModifyExpressionValue / @Local). Bundled & active at runtime by NeoForge,
    // so we only need it on the compile classpath. Generic add(...) avoids relying on DSL accessors
    // that Gradle does not generate for Stonecutter's non-`build.gradle.kts` buildscripts.
    add("compileOnly", "io.github.llamalad7:mixinextras-common:0.5.4")
}

// Compile the Stonecutter-processed sources (where the inactive loader is commented out),
// not the raw shared `src/main`. generatedSourcesDir = versions/<node>/build/generated/stonecutter/.
sourceSets.named("main") {
    val generated = layout.buildDirectory.dir("generated/stonecutter/main")
    java.setSrcDirs(listOf(generated.get().dir("java")))
    resources.setSrcDirs(listOf(generated.get().dir("resources")))
}

// Inject the Mixin compatibility level (farplayers.mixins.json) per generation. The mixin targets
// vanilla Entity bytecode: Java 21 on 1.21.x, Java 25 on 26.x — so compatibilityLevel must follow.
val is26 = stonecutter.eval(mcVersion, ">=26.1")
val expandProps = mapOf(
    "version" to "${property("mod.version")}+$mcVersion",
    "minecraft" to mcVersion,
    "mixin_compat" to if (is26) "JAVA_25" else "JAVA_21",
)

tasks.named<ProcessResources>("processResources") {
    // Only the NeoForge metadata is relevant for the NeoForge jar.
    exclude("fabric.mod.json")
    inputs.properties(expandProps)
    filesMatching("META-INF/neoforge.mods.toml") { expand(expandProps) }
    filesMatching("farplayers.mixins.json") { expand(expandProps) }
}

// Every task that reads the generated sources must run after they are produced.
listOf("processResources", "compileJava", "sourcesJar").forEach { name ->
    tasks.matching { it.name == name }.configureEach { dependsOn("stonecutterGenerate") }
}

// moddev generates the MC artifacts from the (Stonecutter-processed) sources.
tasks.matching { it.name == "createMinecraftArtifacts" }.configureEach {
    dependsOn("stonecutterGenerate")
}

// 26.x requires JDK 25, 1.21.11 requires JDK 21. A Java toolchain resolves the correct
// compile JDK from org.gradle.java.installations.paths regardless of the launching JVM.
val javaTarget = if (stonecutter.eval(mcVersion, ">=26.1")) 25 else 21

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaTarget))
    }
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(javaTarget)
}

// ---- Modrinth publishing (guarded) ----------------------------------------------------------
// Only configures the Modrinth target when MODRINTH_TOKEN is present, so plain `build` never
// fails for lack of a token. Publish with: MODRINTH_TOKEN=xxx ./gradlew :<node>:publishMods
val modrinthToken = providers.environmentVariable("MODRINTH_TOKEN")
if (modrinthToken.isPresent) {
    publishMods {
        file.set(tasks.named<Jar>("jar").flatMap { it.archiveFile })
        displayName.set("${property("mod.name")} ${property("mod.version")} (NeoForge $mcVersion)")
        version.set("${property("mod.version")}+neoforge-$mcVersion")
        changelog.set("Initial release. See other players up to 32 chunks away, independent of your terrain render distance (bounded by the server's view distance).")
        type = me.modmuss50.mpp.ReleaseType.STABLE
        modLoaders.add("neoforge")
        modrinth {
            projectId.set("UvvG2PsB")
            accessToken.set(modrinthToken)
            minecraftVersions.add(mcVersion)
        }
    }
}
