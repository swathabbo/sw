package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import java.util.*

class RoleIdCommand : CommandBase("roleid") {
	override fun getDescription(locale: BaseLocale): String {
		return locale["ROLEID_DESCRIPTION"]
	}

	override fun getUsage(): String {
		return "CargoMencionado"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("Moderadores")
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_ROLES);
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.ADMIN
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.rawArgs.isNotEmpty()) {
			val argument = context.rawArgs.joinToString(" ")

			val roles = context.guild.roles.filter { it.name.contains(argument, true) }

			val list = mutableListOf<LoriReply>()
			list.add(LoriReply(
					message = "Cargos que contém `$argument`...",
					prefix = "\uD83D\uDCBC"
			))

			if (roles.isEmpty()) {
				list.add(
						LoriReply(
								message = "*Nenhum cargo...*",
								mentionUser = false,
								prefix = "\uD83D\uDE22"
						)
				)
			} else {
				roles.mapTo(list) {
					LoriReply(
							message = "*${it.name}* - `${it.id}`",
							mentionUser = false
					)
				}
			}

			context.reply(*list.toTypedArray())
		} else {
			context.explain()
		}
	}
}