package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.translate.GoogleTranslateUtils

class TranslateCommand : CommandBase("traduzir") {
	override fun getDescription(locale: BaseLocale): String {
		return locale["TRANSLATE_DESCRIPTION"]
	}

	override fun getAliases(): List<String> {
		return listOf("translate")
	}

	override fun getUsage(): String {
		return "língua texto"
	}

	override fun getExample(): List<String> {
		return listOf("pt Hello World!")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.size >= 2) {
			val strLang = context.args[0]
			context.args[0] = "" // Super workaround
			val text = context.args.joinToString(" ")

			try {
				val translatedText = GoogleTranslateUtils.translate(text, "auto", strLang)
				context.sendMessage(context.getAsMention(true) + translatedText!!.escapeMentions())
			} catch (e: Exception) {
				e.printStackTrace()
			}

		} else {
			context.explain()
		}
	}
}