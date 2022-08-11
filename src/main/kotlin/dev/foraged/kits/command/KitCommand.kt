package dev.foraged.kits.command

import dev.foraged.commons.acf.CommandHelp
import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.*
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.annotations.commands.customizer.CommandManagerCustomizer
import dev.foraged.commons.command.CommandManager
import dev.foraged.commons.command.GoodCommand
import dev.foraged.kits.KitsExtendedPlugin
import dev.foraged.kits.configuration.KitsConfiguration
import dev.foraged.kits.kit.Kit
import dev.foraged.kits.kit.KitService
import dev.foraged.kits.kit.menu.KitsMenu
import dev.foraged.kits.kit.result.KitPaginatedResult
import net.evilblock.cubed.menu.template.menu.EditTemplateLayoutMenu
import net.evilblock.cubed.menu.template.menu.TemplateEditorMenu
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.text.TextUtil
import net.evilblock.cubed.util.time.Duration
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID

@CommandAlias("kit")
@CommandPermission("kits.kit.management")
@AutoRegister
object KitCommand : GoodCommand()
{
    @CommandManagerCustomizer
    fun customizer(manager: CommandManager) {
        manager.commandContexts.registerContext(Kit::class.java) {
            val kit = it.popFirstArg().lowercase()
            return@registerContext KitService.findKit(kit) ?: throw ConditionFailedException("There is no kit registered with the id \"${kit}\".")
        }
    }

    @HelpCommand
    fun help(commandHelp: CommandHelp) {
        commandHelp.showHelp()
    }

    @Subcommand("create")
    @Description("Create a new kit")
    fun create(player: Player, name: String, display: String) {
        val kit = Kit(name.lowercase(), name, CC.translate(display),
            contents = player.inventory.contents,
            armor = player.inventory.armorContents,
            enabled = false
        )

        KitService.registerKit(kit)
        KitsExtendedPlugin.instance.containerDisable()
        player.sendMessage("${CC.B_GREEN}Registered new kit with name $name.")
    }

    @Subcommand("delete")
    @Description("Delete an existing kit")
    fun delete(player: Player, kit: Kit) {
        KitService.unregisterKit(kit)
        player.sendMessage("${CC.B_GOLD}Unregistered kit with name ${kit.name}")
    }

    @Subcommand("reset-template")
    @Description("Reset kits menu template")
    fun reset(player: Player) {
        KitService.template = KitsMenu()
        KitService.saveTemplate()
        player.sendMessage("${CC.B_GREEN}Successfully reset the kit template menu.")
    }

    @Subcommand("edit-template")
    @Description("Edit kits menu template")
    fun edit(player: Player) {
        EditTemplateLayoutMenu(KitService.template).openMenu(player)
    }

    @Subcommand("toggle")
    @Description("Toggle if a kit is enabled")
    fun toggle(sender: CommandSender, kit: Kit) {
        kit.enabled = !kit.enabled
        sender.sendMessage("${CC.SEC}You have ${TextUtil.stringifyBoolean(kit.enabled, TextUtil.FormatType.ENABLED_DISABLED).lowercase()} the kit ${kit.displayName}${CC.SEC}.")
    }

    @Subcommand("order")
    @Description("Update order for a kit")
    fun order(sender: CommandSender, kit: Kit, order: Int) {
        kit.order = order
        sender.sendMessage("${CC.SEC}You have updated the order of kit ${kit.displayName}${CC.SEC} to ${CC.PRI}${kit.order}${CC.SEC}.")
    }

    @Subcommand("display")
    @Description("Update display name for a kit")
    fun display(sender: CommandSender, kit: Kit, displayName: String) {
        sender.sendMessage("${CC.SEC}You have updated the display of kit ${kit.displayName}${CC.SEC} to ${CC.PRI}${CC.translate(displayName)}${CC.SEC}.")
        kit.displayName = CC.translate(displayName)
    }

    @Subcommand("permission|perm")
    @Description("Update permission for a kit")
    fun permission(sender: CommandSender, kit: Kit, permission: String) {
        kit.permission = permission
        sender.sendMessage("${CC.SEC}You have updated the permission of kit ${kit.displayName}${CC.SEC} to ${CC.PRI}${kit.permission}${CC.SEC}.")
    }

    @Subcommand("update")
    @Description("Update contents for a kit")
    fun update(player: Player, kit: Kit) {
        kit.armor = player.inventory.armorContents
        kit.contents = player.inventory.contents
        player.sendMessage("${CC.SEC}You have updated the contents of kit ${kit.displayName}${CC.SEC} to ${CC.PRI}your inventory${CC.SEC}.")
    }

    @Subcommand("cooldown")
    @Description("Update cooldown for a kit")
    fun token(sender: CommandSender, kit: Kit, @Optional duration: Duration?) {
        kit.cooldown = duration?.get()?.div(1000) ?: 0L
        sender.sendMessage("${CC.SEC}You have updated the cooldown of the kit ${kit.displayName}${CC.SEC} to ${CC.PRI}${TimeUtil.formatIntoAbbreviatedString(kit.cooldown.toInt())}${CC.SEC}.")
    }

    @Subcommand("token")
    @Description("Give a kit token to a target")
    fun token(sender: CommandSender, kit: Kit, amount: Int, target: Player) {
        target.inventory.addItem(kit.buildKitToken(amount))
        sender.sendMessage("${CC.SEC}You have given ${target.displayName} ${CC.PRI}$amount${CC.SEC} ${kit.displayName}${CC.SEC} kit token.")
    }

    @Subcommand("apply")
    @Description("Apply a kit to a player")
    fun apply(sender: Player, targetId: UUID, kit: Kit) {
        val target = Bukkit.getPlayer(targetId) ?: throw ConditionFailedException("Player not found.")

        kit.apply(target)
        sender.sendMessage("${CC.SEC}You have applied the ${kit.displayName}${CC.SEC} kit to ${CC.PRI}${target.displayName}${CC.SEC}.")
    }

    @Subcommand("list")
    @Description("List all created kits")
    fun list(sender: CommandSender, @Default("1") page: Int) {
        KitPaginatedResult.display(sender, KitService.kits, page)
    }
}