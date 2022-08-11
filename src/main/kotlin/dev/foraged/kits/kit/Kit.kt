package dev.foraged.kits.kit

import com.cryptomorin.xseries.XMaterial
import dev.foraged.commons.persist.PersistMap
import dev.foraged.commons.persist.impl.CooldownPersistMap
import dev.foraged.commons.persist.impl.LongPersistMap
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.ItemUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

class Kit(
    var id: String,
    var name: String,
    var displayName: String,
    var permission: String = "kits.use.${id}.${Bukkit.getServerName()}",
    var order: Int = 1,
    var cooldown: Long = 0,
    var contents: Array<ItemStack>,
    var armor: Array<ItemStack>,
    var icon: ItemStack = ItemBuilder.of(contents.firstOrNull()?.type ?: Material.CHEST).build(),
    var enabled: Boolean = false
) : CooldownPersistMap("${Bukkit.getServerName()}${id}KitCooldown", "", false), Listener
{
    companion object {
        val CHAT_PREFIX = "${CC.B_PRI}[Kits] "
    }

    val hasCooldown: Boolean get() = cooldown != 0L

    fun buildKitToken(amount: Int = 1): ItemStack
    {
        return ItemBuilder.of(XMaterial.NETHER_STAR)
            .name("$displayName Token")
            .setLore(
                listOf(
                    "${CC.SEC}You can use this token to",
                    "${CC.SEC}receive a $displayName${CC.SEC}.",
                    "",
                    "${CC.B_GREEN}LEFT-CLICK ${CC.GREEN}to use this kit."
                )
            ).amount(amount)
            .glow()
            .build()
    }

    fun apply(target: Player) {
        for (item in (contents + armor).toList().filterNotNull().filter { it.type != null }) {
            var notInserted = mutableListOf(item)

            if (ItemUtils.isArmorEquipment(item.type)) {
                if (item.type.name.contains("HELMET")) {
                    if (target.inventory.helmet == null) {
                        target.inventory.helmet = item
                        notInserted = mutableListOf()
                    }
                }
                if (item.type.name.contains("CHESTPLATE")) {
                    if (target.inventory.chestplate == null) {
                        target.inventory.chestplate = item
                        notInserted = mutableListOf()
                    }
                }
                if (item.type.name.contains("LEGGINGS")) {
                    if (target.inventory.leggings == null) {
                        target.inventory.leggings = item
                        notInserted = mutableListOf()
                    }
                }
                if (item.type.name.contains("BOOTS")) {
                    if (target.inventory.boots == null) {
                        target.inventory.boots = item
                        notInserted = mutableListOf()
                    }
                }
            } else notInserted = target.inventory.addItem(item).values.toMutableList()
            if (notInserted.isNotEmpty()) {
                notInserted = target.inventory.addItem(*notInserted.toTypedArray()).values.toMutableList()

                for (drop in notInserted) {
                    target.world.dropItemNaturally(target.location, drop)
                }
            }
        }

        target.updateInventory()
    }


}