package dev.foraged.kits.kit.menu

import dev.foraged.kits.kit.Kit
import dev.foraged.kits.kit.KitService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.template.MenuTemplate
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.ItemUtils
import net.evilblock.cubed.util.text.TextSplitter
import net.evilblock.cubed.util.text.TextUtil
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Type

class KitsMenu : MenuTemplate<Kit>(id = "kits") {

    override fun createEntryButton(entry: Kit): Button {
        return KitButton(entry)
    }

    override fun getListEntries(): List<Kit> {
        return KitService.kits.filter { it.enabled }.sortedBy { it.order }
    }

    inner class KitButton(val kit: Kit) : Button() {
        override fun getName(player: Player): String {
            return kit.displayName
        }

        override fun getDescription(player: Player): List<String> {
            return mutableListOf<String>().also { desc ->
                if (kit.hasCooldown) {
                    val formattedCooldown = TimeUtil.formatIntoAbbreviatedString((kit.cooldown).toInt())
                    desc.add(" ${ChatColor.GRAY}(${ChatColor.RED}${ChatColor.BOLD}$formattedCooldown ${ChatColor.GRAY}Cooldown)")
                } else {
                    desc.add(" ${ChatColor.GRAY}(No Cooldown)")
                }

                desc.add("")
                desc.add("${ChatColor.YELLOW}This kit contains...")

                if (kit.contents.isEmpty()) {
                    desc.add("${ChatColor.GRAY}No items")
                } else {
                    for ((index, item) in kit.contents.filterNotNull().withIndex()) {
                        desc.add("  ${formatItemName(item)}")

                        if (index >= 4) {
                            break
                        }
                    }

                    if (kit.contents.size > 5) {
                        val overlap = kit.contents.size - 5
                        desc.add("  ${ChatColor.GRAY}and $overlap more ${TextUtil.pluralize(overlap, "item", "items")}...")
                    }
                }

                desc.add("")

                if (kit.hasCooldown && kit.isOnCooldown(player.uniqueId)) {
                    val formattedCooldown = TimeUtil.formatIntoAbbreviatedString(((kit.getCooldown(player.uniqueId) - System.currentTimeMillis()) / 1000).toInt())
                        .split(" ")
                        .joinToString { ChatColor.BOLD.toString() + it }

                    desc.addAll(TextSplitter.split(text = "You can't redeem this kit again for another $formattedCooldown${ChatColor.RED}!", linePrefix = ChatColor.RED.toString()))
                } else {
                    desc.add("${ChatColor.YELLOW}Click to redeem kit")
                }
            }
        }


        override fun getButtonItem(player: Player): ItemStack {
            return ItemBuilder.copyOf(kit.icon).name(getName(player)).setLore(getDescription(player)).build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView)
        {
            if (clickType.isLeftClick) {
                if (kit.permission.isNotEmpty()) {
                    if (!player.hasPermission(kit.permission)) {
                        player.sendMessage("${CC.RED}You do not have permission to redeem this kit!")
                        return
                    }
                }

                if (kit.hasCooldown && kit.isOnCooldown(player.uniqueId)) {
                    val cooldown = TimeUtil.formatIntoDetailedString(((kit.getCooldown(player.uniqueId) - System.currentTimeMillis()) / 1000).toInt())
                    player.sendMessage("${CC.RED}You cannot use this kit for another ${CC.BOLD}${cooldown}${CC.RED}.")
                    return
                }

                kit.apply(player)
                if (kit.hasCooldown) kit.startCooldown(player.uniqueId, kit.cooldown)
            }
        }
    }

    override fun getAbstractType(): Type
    {
        return KitsMenu::class.java
    }

    private fun formatItemName(itemStack: ItemStack): String {
        val name = if (itemStack.hasItemMeta() && itemStack.itemMeta.hasDisplayName()) {
            itemStack.itemMeta.displayName
        } else {
            "${ChatColor.GRAY}${ItemUtils.getName(itemStack)}"
        }

        return "${ChatColor.GRAY}${itemStack.amount}x $name"
    }
}