rootProject.name = "APlugin"

include(":lib_plugin")
include(":lib_gradle_plugin")

private val hasPlugin = File(rootDir, "repos").run {
    isDirectory && !list().isNullOrEmpty()
}

if (hasPlugin) {
    include(":app")
    include(":demo_plugin1", ":demo_plugin1:api")
    include(":demo_plugin2")
}
