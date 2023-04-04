plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
}


architectury {
    common("forge", "fabric")
}

loom {
    silentMojangMappingsLicense()
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    // The following line declares the mojmap mappings, you may use other mappings as well
    mappings(loom.officialMojangMappings())
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    implementation("net.kyori:adventure-text-minimessage:${property("minimessage_version")}")
    implementation("net.kyori:adventure-text-serializer-gson:${property("minimessage_version")}")
    // Remove the next line if you don't want to depend on the API
    modApi("dev.architectury:architectury:${property("architectury_version")}") { isTransitive = false }
    modImplementation("com.cobblemon:mod:1.3.1+1.19.2-SNAPSHOT") { isTransitive = false }
}
