import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExternalKotlinTargetApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.net.URI

/** https://github.com/evant/kotlin-inject/blob/main/docs/multiplatform.md#ksp-common-source-set-configuration */
@OptIn(ExternalKotlinTargetApi::class)
fun KotlinMultiplatformExtension.configureCommonMainKsp(project: Project) {
	sourceSets.named("commonMain").configure {
		kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
	}

	project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
		if(name != "kspCommonMainKotlinMetadata") {
			dependsOn("kspCommonMainKotlinMetadata")
		}
	}

	project.afterEvaluate {
		val sourceTasks = listOf(
			"sourcesJar", "jvmSourcesJar", "androidReleaseSourcesJar", "iosArm64SourcesJar", "iosSimulatorArm64SourcesJar",
			"watchosArm64SourcesJar", "watchosDeviceArm64SourcesJar", "watchosSimulatorArm64SourcesJar"
		)
		sourceTasks.forEach {
			tasks.findByName(it)?.dependsOn("kspCommonMainKotlinMetadata")
		}
	}
}