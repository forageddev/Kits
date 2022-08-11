package dev.foraged.kits.kit.result

import dev.foraged.kits.kit.Kit
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.PaginatedResult

object KitPaginatedResult : PaginatedResult<Kit>()
{
    override fun getHeader(page: Int, maxPages: Int) = "${CC.PRI}=== ${CC.SEC}Kits ${CC.WHITE}($page/$maxPages) ${CC.PRI}==="

    override fun format(result: Kit, resultIndex: Int): String
    {
        return " ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${result.displayName} ${CC.GRAY}(Enabled: ${result.enabled})"
    }
}