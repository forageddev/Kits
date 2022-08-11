package dev.foraged.kits

import com.cryptomorin.xseries.XMaterial
import com.google.gson.Gson
import dev.foraged.commons.ExtendedPaperPlugin
import dev.foraged.commons.annotations.container.ContainerDisable
import dev.foraged.commons.annotations.container.ContainerEnable
import dev.foraged.commons.config.ConfigContainer
import dev.foraged.commons.config.annotations.ContainerConfig
import dev.foraged.kits.configuration.KitsConfiguration
import dev.foraged.kits.kit.Kit
import dev.foraged.kits.kit.KitService
import me.lucko.helper.plugin.ap.Plugin
import me.lucko.helper.plugin.ap.PluginDependency
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder

@Plugin(
    name = "Kits",
    version = "\${git.commit.id.abbrev}",
    depends = [
        PluginDependency("Commons")
    ]
)
@ContainerConfig(
    value = "kits",
    model = KitsConfiguration::class,
    crossSync = false
)
class KitsExtendedPlugin : ExtendedPaperPlugin()
{
    companion object {
        lateinit var instance: KitsExtendedPlugin
    }

    @ContainerEnable
    fun containerEnable()
    {
        instance = this
    }

    @ContainerDisable
    fun containerDisable() {
        config<KitsConfiguration>().kits.clear()
        config<KitsConfiguration>().kits.addAll(KitService.kits.map { Serializers.gson.toJson(it) })
        configContainerized.configContainers.forEach { (t, u) ->
            u.save()
        }
        KitService.saveTemplate()
    }

    override fun configContainerUpdate(config: ConfigContainer) {
        reloadConfigurationKits()
    }

    fun reloadConfigurationKits() {
        config<KitsConfiguration>().kits.forEach {
            KitService.registerKit(Serializers.gson.fromJson(it, Kit::class.java))
        }
    }
}
