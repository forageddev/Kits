package dev.foraged.kits.configuration

import xyz.mkotb.configapi.comment.Comment
import xyz.mkotb.configapi.comment.HeaderComment


@HeaderComment("Create kits for configuration")
class KitsConfiguration
{
    @Comment("List of kits to define")
    val kits = mutableListOf<String>()
}