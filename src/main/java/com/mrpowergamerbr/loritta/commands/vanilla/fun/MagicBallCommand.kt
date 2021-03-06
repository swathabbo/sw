package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.getOrCreateWebhook
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import java.util.*

class MagicBallCommand : CommandBase("vieirinha") {
	override fun getAliases(): List<String> {
		return listOf("8ball", "magicball", "eightball")
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["VIEIRINHA_DESCRIPTION"]
	}

	override fun getExample(): List<String> {
		return Arrays.asList("você me ama?")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun hasCommandFeedback(): Boolean {
		return false
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val temmie = getOrCreateWebhook(context.event.textChannel, "Vieirinha")

			context.sendMessage(temmie, DiscordMessage.builder()
					.username("Vieirinha")
					.content(context.getAsMention(true) + context.locale.VIEIRINHA_responses[Loritta.RANDOM.nextInt(context.locale.VIEIRINHA_responses.size)])
					.avatarUrl("http://i.imgur.com/rRtHdti.png")
					.build())
		} else {
			context.explain()
		}
	}
}
