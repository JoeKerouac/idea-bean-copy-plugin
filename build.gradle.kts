import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

group = "com.github.JoeKerouac"
version = "0.0.3"

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.0.1"
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
        releases()
        marketplace()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

dependencies {
    intellijPlatform {
        val type = providers.gradleProperty("platformType")
        val version = providers.gradleProperty("platformVersion")
        create(type, version)

        zipSigner()

        // 在2019.2版本以后，默认Java支持提取为单独的plugin了，参考：https://blog.jetbrains.com/platform/2019/06/java-functionality-extracted-as-a-plugin/
        bundledPlugins("com.intellij.java")
        instrumentationTools()
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "utf-8"
    }

    build {
        charset("utf-8")
    }

    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("251")
    }

    signPlugin {
        certificateChain.set("""
            -----BEGIN CERTIFICATE-----
            MIIEATCCAumgAwIBAgIUKkTV5kraxf1froJvpyTOpwilizYwDQYJKoZIhvcNAQEL
            BQAwgY4xCzAJBgNVBAYTAkNOMQswCQYDVQQIDAJITjELMAkGA1UEBwwCSE4xEzAR
            BgNVBAoMCkpvZUtlcm91YWMxEzARBgNVBAsMCkpvZUtlcm91YWMxEzARBgNVBAMM
            CkpvZUtlcm91YWMxJjAkBgkqhkiG9w0BCQEWF0pvZS5LZXJvdWFjQG91dGxvb2su
            Y29tMCAXDTI0MDkwMTE0MTkyMloYDzIxMjQwODA4MTQxOTIyWjCBjjELMAkGA1UE
            BhMCQ04xCzAJBgNVBAgMAkhOMQswCQYDVQQHDAJITjETMBEGA1UECgwKSm9lS2Vy
            b3VhYzETMBEGA1UECwwKSm9lS2Vyb3VhYzETMBEGA1UEAwwKSm9lS2Vyb3VhYzEm
            MCQGCSqGSIb3DQEJARYXSm9lLktlcm91YWNAb3V0bG9vay5jb20wggEiMA0GCSqG
            SIb3DQEBAQUAA4IBDwAwggEKAoIBAQCs8I6TKbw9ZGrU4QJK8rdW5HIgeKP84J+4
            vV54qrdDXwgpKtx/Q290FK/nX2GEnzoeDhO7iBUUBPQi0xyytQ4U8kwqXaN3YGKm
            +q0jLYLSzsKTxFMPZ7mu+HHAk1LvPlxCI47OkyoPPsilNiWnq39048ooxhwvSeaW
            QoyCuBnZutRZUj9deiKMsZ4GD9Nmo+rqE5+17dGeTIhAiTeqySohrcMEiYhbq2TK
            7MlKx8OsVnjWfOH4bxq5eJdBJb2IQU8SVuhuFitxkbK1yGAmyU+wb+YwHFspLwu9
            i8yE3fmAWCMKY6ziw9/4N7bT1Kaz6xn6Uqm/wHau+zulUGftriMDAgMBAAGjUzBR
            MB0GA1UdDgQWBBTLoMQQNuHkGPyHBZmxJzdHltLX6TAfBgNVHSMEGDAWgBTLoMQQ
            NuHkGPyHBZmxJzdHltLX6TAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUA
            A4IBAQCgCyQe6ZXQDa4WYHeqBBnp9ok5hRP/3FbinKtDpKM6C72i+cHsrJUN8hoD
            EVLMX9Du3+iEKdNJhOd8uFIgcBxJXNUwOUydS3rtr2giFekr2i7IDSF6owaLt2IO
            iVvBjDQgNVYwCgQTfRRu1yu+AaIXkTFYB430X3hQ3TRa037otFAtuFPqdipwWoz/
            BLbnr2vVH5uv1nR5yYElYrVEA8QxfeBDJ0BPVtbyq8G+rZsSaVS1D/KnX4tXwK2Y
            yls2ZpYJfbd/KZRbpqE3X2pAPGlRcaXoM4pcb4Sb8BvRn4VQCADthCCY6x69qut+
            gpw7OpVapu/R/SPUTouAdgTuhPWW
            -----END CERTIFICATE-----
        """.trimIndent())
        privateKey.set(System.getenv("IDEA_PLUGIN_PRIVATE_KEY"))
        password.set(System.getenv("IDEA_PLUGIN_PRIVATE_KEY_PWD"))
    }

    publishPlugin {
        token.set(System.getenv("IDEA_PLUGIN_PUBLISH_TOKEN"))
    }
}
