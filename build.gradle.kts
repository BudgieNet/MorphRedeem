plugins {
    id("com.budgienet.paper-plugin")
}

dependencies {
    implementation("redis.clients:jedis:8.0.2")
    // json-simple 1.1.1 wrongly lists junit as a runtime dependency
    implementation("com.googlecode.json-simple:json-simple:1.1.1") { exclude(group = "junit") }
    compileOnly(libs.mcmmo)
    compileOnly(libs.placeholderapi)
}
