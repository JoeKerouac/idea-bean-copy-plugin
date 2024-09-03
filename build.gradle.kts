import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

group = "com.github.JoeKerouac"
version = "0.0.1"

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
    sourceCompatibility = JavaVersion.VERSION_1_8
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
        sourceCompatibility = "8"
        targetCompatibility = "8"
        options.encoding = "utf-8"
    }

    build {
        charset("utf-8")
    }

    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("242.*")
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
        privateKey.set("""
            -----BEGIN ENCRYPTED PRIVATE KEY-----
            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
            -----END ENCRYPTED PRIVATE KEY-----
        """.trimIndent())
        password.set(System.getenv("IDEA_PLUGIN_PRIVATE_KEY"))
    }

    publishPlugin {
        token.set(System.getenv("IDEA_PLUGIN_PUBLISH_TOKEN"))
    }
}
