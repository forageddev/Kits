package dev.foraged.kits.kit

import com.cryptomorin.xseries.XMaterial
import com.google.common.base.Charsets
import com.google.common.io.Files
import dev.foraged.commons.annotations.Listeners
import dev.foraged.commons.persist.PluginService
import dev.foraged.kits.KitsExtendedPlugin
import dev.foraged.kits.configuration.KitsConfiguration
import dev.foraged.kits.kit.menu.KitsMenu
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.mkotb.configapi.comment.HeaderComment
import java.io.File
import dev.foraged.kits.KitsExtendedPlugin.Companion.instance;

@Service
@HeaderComment("Created kits go here")
@Listeners
object KitService : Listener, PluginService
{
    private val kitsMenuTemplate: File = File(instance.dataFolder, "kits-menu.json")
    val kits = mutableListOf<Kit>()
    lateinit var template: KitsMenu

    @Configure
    override fun configure()
    {
        if (instance.config<KitsConfiguration>().kits.isEmpty())
        {
            instance.config<KitsConfiguration>().kits.add(
                Serializers.gson.toJson(Kit(
                    id = "example",
                    name = "Example",
                    displayName = "${CC.B_GREEN}Example Kit",
                    contents = arrayOf(ItemBuilder.of(XMaterial.STICK).name("Example").build()),
                    armor = arrayOf(),
                    enabled = false
                )
                ))
            instance.configContainerized.container<KitsConfiguration>().save()
        }

        instance.reloadConfigurationKits()

        kitsMenuTemplate.parentFile.mkdirs()

        if (kitsMenuTemplate.exists()) {
            Files.newReader(kitsMenuTemplate, Charsets.UTF_8).use { reader ->
                template = Serializers.gson.fromJson(reader, KitsMenu::class.java) as KitsMenu
            }
        } else {
            template = KitsMenu()
        }
    }

    fun saveTemplate() {
        try {
            Files.write(Serializers.gson.toJson(template, KitsMenu::class.java), kitsMenuTemplate, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            instance.logger.severe("Failed to save kits-template.json!")
        }
    }

    fun registerKit(kit: Kit) {
        if (findKit(kit.id) != null) {
            kits.removeIf { it.id == kit.id }
            instance.logger.info("[Kit] Updated kit with id ${kit.id}.")
        } else {
            instance.logger.info("[Kit] Registered new kit with id ${kit.id}.")
        }
        kits.add(kit)
    }

    fun unregisterKit(kit: Kit) {
        if (findKit(kit.id) != null) {
            kits.removeIf { it.id == kit.id }
            instance.logger.info("[Kit] Unregistered kit with id ${kit.id}.")


            instance.config<KitsConfiguration>().kits.clear()
            instance.config<KitsConfiguration>().kits.addAll(kits.map { Serializers.gson.toJson(it) })
            instance.configContainerized.configContainers.forEach { (t, u) ->
                u.save()
            }
        }
    }

    fun findKit(name: String) : Kit? {
        return kits.find { it.id == name }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player

        if (event.clickedInventory == null) return
        if (event.cursor == null || event.cursor.type != Material.FIREWORK_CHARGE) return
        if (event.currentItem == null) return
        if (event.currentItem.type == Material.AIR) return
        if (event.cursor.amount > 1) return
        /*if (!isEnchantStar(event.cursor)) return

        val enchant = getEnchantFromStar(event.cursor) ?: return
        if (!enchant.enabled) {
            player.sendMessage("${CC.RED}This enchant is currently disabled.")
            return
        }
        if (!enchant.canEnchant(event.currentItem)) return

        val starLevel = enchant.getStarLevel(event.cursor)
        if (enchant.getEnchantLevel(event.currentItem) + starLevel > enchant.maxLevel) {
            player.sendMessage("${CC.RED}You cannot apply this enchant onto this item as it would become to powerful to behold.")
            return
        }

        enchant.apply(event.currentItem, starLevel)
        event.cursor = null
        event.isCancelled = true*/
    }
}