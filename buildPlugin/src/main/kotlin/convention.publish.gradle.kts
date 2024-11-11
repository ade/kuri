import com.vanniktech.maven.publish.SonatypeHost

plugins {
	id("com.vanniktech.maven.publish")
}

mavenPublishing {
	group = "se.ade.kuri"
	publishToMavenCentral(SonatypeHost.S01)
	signAllPublications()


	pom {
		name.set("Kuri")
		description.set("Kuri")
		url.set("https://github.com/ade/kuri")

		licenses {
			license {
				name.set("MIT")
				url.set("https://opensource.org/licenses/MIT")
			}
		}
		developers {
			developer {
				id.set("ade")
				name.set("ade")
				email.set("oss@ade.se")
			}
		}
		scm {
			url.set("https://github.com/ade/kuri")
		}
	}
}