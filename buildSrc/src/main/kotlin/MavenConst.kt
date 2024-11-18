import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import java.io.File
import java.net.URI

/*
 * Maven 发布用的常量
 *
 * @author Ysj
 * Create time: 2023/6/30
 */

val Project.MAVEN_LOCAL: URI get() = File(rootDir, "repos").toURI()

const val POM_DEVELOPER_ID = "Yangshujian"
const val POM_DEVELOPER_NAME = "Yangshujian"

fun Project.applyMavenLocal(handler: RepositoryHandler) = handler.maven {
    url = MAVEN_LOCAL
}