package dev.foraged.kits.command

import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.acf.annotation.Description
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import dev.foraged.kits.kit.KitService
import net.evilblock.cubed.menu.template.menu.TemplateMenu
import org.bukkit.entity.Player

@AutoRegister
object KitsCommand : GoodCommand()
{
    @CommandAlias("kits|kitmenu|gkit|gkits|gkitz|kitgui")
    @Description("Open the kits display menu")
    fun kits(player: Player) {
        TemplateMenu(KitService.template).openMenu(player)
    }
}