package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.Image
import java.io.File
import javax.imageio.ImageIO

class SAMCommand : CommandBase("sam") {
	override fun getAliases(): List<String> {
		return listOf("southamericamemes")
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["SAM_DESCRIPTION"]
	}

	override fun getExample(): List<String> {
		return listOf("https://cdn.discordapp.com/attachments/265632341530116097/297440837871206420/meme.png")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.IMAGES
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var div: Double? = 1.5

		if (context.args.size >= 2) {
			div = context.args[1].toDoubleOrNull()
		}

		if (div == null) {
			div = 1.5
		}

		val image = LorittaUtils.getImageFromContext(context, 0)

		if (!LorittaUtils.isValidImage(context, image)) {
			return
		}

		var seloSouthAmericaMemes: Image = ImageIO.read(File(Loritta.ASSETS + "selo_sam.png"))

		val height = (image.height / div).toInt() // Baseando na altura
		seloSouthAmericaMemes = seloSouthAmericaMemes.getScaledInstance(height, height, Image.SCALE_SMOOTH)

		val x = Loritta.RANDOM.nextInt(0, Math.max(1, image.width - seloSouthAmericaMemes.getWidth(null)))
		val y = Loritta.RANDOM.nextInt(0, Math.max(1, image.height - seloSouthAmericaMemes.getHeight(null)))

		image.graphics.drawImage(seloSouthAmericaMemes, x, y, null)

		context.sendFile(image, "south_america_memes.png", context.getAsMention(true))
	}
}