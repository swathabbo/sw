package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.artist
import com.mrpowergamerbr.loritta.utils.donator
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaSupervisor
import com.mrpowergamerbr.loritta.utils.makeRoundedCorners
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import com.mrpowergamerbr.loritta.utils.onResponseByAuthor
import com.mrpowergamerbr.loritta.utils.patreon
import com.mrpowergamerbr.loritta.utils.save
import com.mrpowergamerbr.loritta.utils.tamagotchi.TamagotchiPet
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.lang.Thread.sleep
import javax.imageio.ImageIO

class TamagotchiCommand : CommandBase("tamagotchi") {
	override fun getCategory(): CommandCategory {
		return CommandCategory.MAGIC
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if ((context.args.isNotEmpty() && context.args[0] == "reset") || (context.userHandle.id != Loritta.config.ownerId && !context.userHandle.patreon && !context.userHandle.lorittaSupervisor && !context.userHandle.artist && !context.userHandle.donator && context.guild.id != "297732013006389252")) {
			context.lorittaUser.profile.tamagotchi = null
			loritta save context.lorittaUser.profile
			context.reply(
					"Atualmente eu estou sem pets para te dar... volte mais tarde quando tudo estiver pronto!"
			)
			return
		}

		if (context.lorittaUser.profile.tamagotchi == null) {
			handlePetCreation(context)
		} else {
			handleGameplay(context)
		}
	}

	fun getPet(context: CommandContext): TamagotchiPet {
		val profile = loritta.getLorittaProfileForUser(context.userHandle.id)

		val pet = profile.tamagotchi!!

		if (pet.lastUpdate == 0L)
			pet.lastUpdate = System.currentTimeMillis()

		val diff = System.currentTimeMillis() - pet.lastUpdate

		// calcular a perda de necessidades
		// + ou - 2 horas para o pet morrer sem interação do usuário
		var hungerDown = 9f // 0.00013f
		var happinessDown = 9f
		var hygieneDown = 9f
		var socialDown = 9f
		var bladderDown = 9f

		if (pet.upgrades.contains(TamagotchiPet.PetUpgrades.FAN_ART)) {
			happinessDown -= 1f
		}

		if (pet.upgrades.contains(TamagotchiPet.PetUpgrades.TELEVISION)) {
			happinessDown -= 2f
		}

		if (pet.upgrades.contains(TamagotchiPet.PetUpgrades.COMPUTER)) {
			happinessDown -= 3f
		}

		if (pet.upgrades.contains(TamagotchiPet.PetUpgrades.MINIBAR)) {
			hungerDown -= 1f
		}

		pet.hunger -= (diff / 1000) * (hungerDown * 0.0001f)
		pet.happiness -= (diff / 1000) * (happinessDown * 0.0001f)
		pet.hygiene -= (diff / 1000) * (hygieneDown * 0.0001f)
		pet.social -= (diff / 1000) * (socialDown * 0.0001f)
		pet.bladder -= (diff / 1000) * (bladderDown * 0.0001f)

		// santity checks
		if (pet.hunger > 1f)
			pet.hunger = 1f

		if (pet.happiness > 1f)
			pet.happiness = 1f

		if (pet.hygiene > 1f)
			pet.hygiene = 1f

		if (pet.social > 1f)
			pet.social = 1f

		if (pet.bladder > 1f)
			pet.bladder = 1f
		return pet
	}

	fun applyPetChanges(context: CommandContext, pet: TamagotchiPet): Boolean {
		val profile = loritta.getLorittaProfileForUser(context.userHandle.id)

		if (0 >= pet.hunger || 0 >= pet.happiness || 0 >= pet.hygiene) { // Pet morreu... :(
			profile.tamagotchi = null

			val artigo = when (pet.gender) {
				TamagotchiPet.PetGender.MALE -> "o"
				TamagotchiPet.PetGender.FEMALE -> "a"
			}

			context.reply(
					LoriReply(
							message = "Sinto muito, mas $artigo `${pet.petName}` fugiu para um lugar melhor devido a sua negligência...",
							prefix = "\uD83D\uDC7C"
					)
			)
			loritta save profile
			return true
		} else {
			pet.lastUpdate = System.currentTimeMillis()

			profile.tamagotchi = pet
		}

		loritta save profile
		return false
	}

	fun handleGameplay(context: CommandContext) {
		val pet = getPet(context)

		if (applyPetChanges(context, pet)) {
			return
		}

		val message = context.sendFile(drawGame(pet).makeRoundedCorners(14), "tamagotchi.png", "Tamagotchi Test\nHunger: ${pet.hunger}\nFun: ${pet.happiness}\nUpgrades: ${pet.upgrades.joinToString(", ")}")

		message.onReactionAddByAuthor(context, {
			message.delete().complete()

			if (it.reactionEmote.name == "\uD83C\uDF57") {
				// Comida
				handleFood(context)
			}

			if (it.reactionEmote.name == "⏫") {
				// Upgrades
				handleUpgrades(context)
			}

			if (it.reactionEmote.name == "\uD83D\uDDE3") {
				// Talk
				handleTalking(context)
			}

			if (it.reactionEmote.name == "\uD83D\uDEBF") {
				// Hygiene
				handleHygiene(context)
			}

			if (it.reactionEmote.name == "\uD83D\uDEBD") {
				// Bladder
				handleBladder(context)
			}

			if (it.reactionEmote.name == "\uD83C\uDF1F") {
				pet.hunger = 1f
				pet.happiness = 1f
				pet.hygiene = 1f
				if (applyPetChanges(context, pet))
					return@onReactionAddByAuthor
				handleGameplay(context)
			}
		})

		message.addReaction("\uD83C\uDF57").complete() // comida
		message.addReaction("\uD83D\uDEBF").complete() // higiene
		message.addReaction("\uD83D\uDEBD").complete() // banheiro
		message.addReaction("\uD83D\uDDE3").complete() // conversar
		message.addReaction("⏫").complete()
		message.addReaction("\uD83C\uDF1F").complete()
	}

	fun drawGame(pet: TamagotchiPet): BufferedImage {
		val base = BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB)
		val graphics = base.graphics
		val wrapper = ImageIO.read(File(Loritta.ASSETS, "tamagotchi.png"))

		val playfield = drawPlayfield(pet)
		val playfieldGraphics = playfield.graphics

		playfieldGraphics.drawImage(getPetImage(pet), 163, 109, null)
		graphics.drawImage(playfield, 5, 5, null)
		graphics.drawImage(wrapper, 0, 0, null)

		drawNeeds(pet, graphics)
		return base
	}

	fun drawPlayfield(pet: TamagotchiPet): BufferedImage {
		val playfield = ImageIO.read(File(Loritta.ASSETS, "playfield.png"))
		val playfieldGraphics = playfield.graphics

		if (pet.upgrades.contains(TamagotchiPet.PetUpgrades.FAN_ART)) {
			val upgrade = ImageIO.read(File(Loritta.ASSETS, "tamagotchi/upgrade_fanart.png"))
			playfieldGraphics.drawImage(upgrade, 0, 0, null)
		}

		if (pet.upgrades.contains(TamagotchiPet.PetUpgrades.TELEVISION)) {
			val upgrade = ImageIO.read(File(Loritta.ASSETS, "tamagotchi/upgrade_tv${RANDOM.nextInt(0, 5)}.png"))
			playfieldGraphics.drawImage(upgrade, 0, 0, null)
		}

		if (pet.upgrades.contains(TamagotchiPet.PetUpgrades.COUCH)) {
			val upgrade = ImageIO.read(File(Loritta.ASSETS, "tamagotchi/upgrade_couch.png"))
			playfieldGraphics.drawImage(upgrade, 0, 0, null)
		}

		if (pet.upgrades.contains(TamagotchiPet.PetUpgrades.LOURO)) {
			val upgrade = ImageIO.read(File(Loritta.ASSETS, "tamagotchi/upgrade_louro.png"))
			playfieldGraphics.drawImage(upgrade, 0, 0, null)
		}

		if (pet.upgrades.contains(TamagotchiPet.PetUpgrades.MINIBAR)) {
			val upgrade = ImageIO.read(File(Loritta.ASSETS, "tamagotchi/upgrade_minibar.png"))
			playfieldGraphics.drawImage(upgrade, 0, 0, null)
		}

		if (pet.upgrades.contains(TamagotchiPet.PetUpgrades.COMPUTER)) {
			val upgrade = ImageIO.read(File(Loritta.ASSETS, "tamagotchi/upgrade_computer.png"))
			playfieldGraphics.drawImage(upgrade, 0, 0, null)
		}

		return playfield
	}

	fun drawNeeds(pet: TamagotchiPet, graphics: Graphics) {
		fun drawNeed(y: Int, name: String, value: Float): Int {
			var _y = y
			graphics.color = Color.WHITE
			graphics.drawString(name, 160, _y)
			_y += 2
			graphics.color = Color.BLACK
			graphics.fillRect(160, _y, 64, 6)
			graphics.color = Color.GREEN
			graphics.fillRect(160, _y, (64 * value).toInt(), 6)
			_y += 15
			return _y
		}

		val minecraftia = Font.createFont(Font.TRUETYPE_FONT,
				FileInputStream(File(Loritta.ASSETS + "Volter__28Goldfish_29.ttf"))) // A fonte para colocar os discriminators

		val minecraftia8 = minecraftia.deriveFont(9f)
		val minecraftia16 = minecraftia.deriveFont(18f)

		graphics.font = minecraftia16
		graphics.drawString("Sobre", 5, 206)
		graphics.drawString("Necessidades", 160, 206)

		graphics.font = minecraftia8
		graphics.drawString("Nome: ${pet.petName}", 5, 216)
		graphics.drawString("Gênero: ${pet.gender}", 5, 226)

		var y = 216
		y = drawNeed(y, "Fome", pet.hunger)
		y = drawNeed(y, "Diversão", pet.happiness)
		y = drawNeed(y, "Higiene", pet.hygiene)
		y = drawNeed(y, "Banheiro", pet.bladder)
		y = drawNeed(y, "Social", pet.social)
	}

	fun getPetImage(pet: TamagotchiPet): Image {
		val petImage = ImageIO.read(File(Loritta.ASSETS, "pets/${pet.petType}"))
		val petImage32 = petImage.getScaledInstance(64, 64, BufferedImage.SCALE_SMOOTH)

		return petImage32
	}

	fun handleFood(context: CommandContext) {
		val pet = getPet(context)

		val foodMap = mutableMapOf(
				"\uD83C\uDF5E" to Pair(0.025f, 5),
				"\uD83C\uDF6A" to Pair(0.05f, 7),
				"\uD83C\uDF5C" to Pair(0.075f, 14)
		)

		val message = context.sendMessage(context.getAsMention(true), EmbedBuilder().apply {
			setTitle("Comidas...")
			appendDescription("\uD83C\uDF5E - Um simples pão ~ 5 sonhos\n")
			appendDescription("\uD83C\uDF6A - Cookie, não é biscoito e nem bolacha ~ 7 sonhos\n")
			appendDescription("\uD83C\uDF5C - Miojo, para quando você quer uma comida rápida ~ 14 sonhos\n")
		}.build())

		message.onReactionAddByAuthor(context, {
			message.delete().complete()

			if (foodMap.containsKey(it.reactionEmote.name)) {
				val pair = foodMap[it.reactionEmote.name]!!

				pet.hunger += pair.first
				pet.bladder -= 0.075f

				if (applyPetChanges(context, pet))
					return@onReactionAddByAuthor

				handleGameplay(context)
			}
		})

		foodMap.keys.forEach {
			message.addReaction(it).complete()
		}
	}

	fun handleUpgrades(context: CommandContext) {
		val pet = getPet(context)

		val upgradeMap = mutableMapOf(
				"\uD83D\uDDBC" to Pair(TamagotchiPet.PetUpgrades.FAN_ART, 5),
				"\uD83D\uDC26" to Pair(TamagotchiPet.PetUpgrades.LOURO, 5),
				"\uD83D\uDECB" to Pair(TamagotchiPet.PetUpgrades.COUCH, 15),
				"\uD83D\uDCFA" to Pair(TamagotchiPet.PetUpgrades.TELEVISION, 25),
				"\uD83D\uDCE1" to Pair(TamagotchiPet.PetUpgrades.ANTENNA, 25),
				"\uD83C\uDF79" to Pair(TamagotchiPet.PetUpgrades.MINIBAR, 50),
				"\uD83D\uDDA5" to Pair(TamagotchiPet.PetUpgrades.COMPUTER, 250)
		)

		val message = context.sendMessage(context.getAsMention(true), EmbedBuilder().apply {
			setTitle("Upgrades...")
			appendDescription("\uD83D\uDDBC - Um quadro da Loritta ~ 5 sonhos\n")
			appendDescription("\uD83D\uDC26 - Louro José\n")
			appendDescription("\uD83D\uDCFA - Uma televisão para a sua casa\n")
			appendDescription("\uD83D\uDCE1 - TV aberta é do passado, o negócio é TV fechada!\n")
			appendDescription("\uD83D\uDECB - Sofá\n")
			appendDescription("\uD83C\uDF79 - Minibar\n")
			appendDescription("\uD83D\uDCBB - Computador\n")
		}.build())

		message.onReactionAddByAuthor(context, {
			message.delete().complete()

			if (upgradeMap.containsKey(it.reactionEmote.name)) {
				val pair = upgradeMap[it.reactionEmote.name]!!

				pet.upgrades.add(pair.first)

				if (applyPetChanges(context, pet))
					return@onReactionAddByAuthor

				handleGameplay(context)
			}
		})

		upgradeMap.keys.forEach {
			message.addReaction(it).complete()
		}
	}

	fun handleTalking(context: CommandContext) {
		val pet = getPet(context)

		val userList = context.guild.members.map { it.user.id }

		val profiles = loritta.ds.find(LorittaProfile::class.java)
				.field("tamagotchi")
				.exists()
				.field("_id")
				.`in`(userList)
				.field("_id")
				.notEqual(context.userHandle.id)
				.shuffled()

		val message = context.sendMessage(context.getAsMention(true), EmbedBuilder().apply {
			setTitle("Pets pelas suas redondezas...")
			for ((index, profile) in profiles.withIndex()) {
				if (index > 9)
					break

				val member = context.guild.getMemberById(profile.userId) ?: continue

				appendDescription("**${profile.tamagotchi!!.petName}** de ${member.user.asMention}\n")
			}
		}.build())

		message.onReactionAddByAuthor(context, {
			var pos = -1
			for ((idx, emote) in Constants.INDEXES.withIndex()) {
				if (it.reactionEmote.name == emote)
					pos = idx
			}

			if (pos == -1)
				return@onReactionAddByAuthor

			message.delete().complete()

			val selectedPet = profiles[pos].tamagotchi!!

			val pet = getPet(context)

			val base = BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB)
			val graphics = base.graphics
			val wrapper = ImageIO.read(File(Loritta.ASSETS, "tamagotchi.png"))

			val playfield = ImageIO.read(File(Loritta.ASSETS, "tamagotchi/shopping.png"))
			val playfieldGraphics = playfield.graphics
			playfieldGraphics.drawImage(getPetImage(pet), 127, 94, null)
			playfieldGraphics.drawImage(getPetImage(selectedPet), 200, 94, null)
			graphics.drawImage(playfield, 5, 5, null)
			graphics.drawImage(wrapper, 0, 0, null)

			drawNeeds(pet, graphics)

			val message = context.sendFile(base.makeRoundedCorners(14), "tamagotchi.png", context.getAsMention(true))

			sleep(6000)

			message.delete().complete()

			pet.social += 0.35f

			if (applyPetChanges(context, pet))
				return@onReactionAddByAuthor

			handleGameplay(context)
		})

		for (idx in 0 until Math.min(9, profiles.size)) {
			message.addReaction(Constants.INDEXES[idx]).complete()
		}
	}

	fun handleHygiene(context: CommandContext) {
		val pet = getPet(context)

		val base = BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB)
		val graphics = base.graphics
		val wrapper = ImageIO.read(File(Loritta.ASSETS, "tamagotchi.png"))

		val playfield = drawPlayfield(pet)
		val playfieldGraphics = playfield.graphics

		val baciaBack = ImageIO.read(File(Loritta.ASSETS, "tamagotchi/bacia_back.png"))
		val baciaFront = ImageIO.read(File(Loritta.ASSETS, "tamagotchi/bacia_front.png"))
		playfieldGraphics.drawImage(baciaBack, 0, 0, null)
		playfieldGraphics.drawImage(getPetImage(pet), 163, 109, null)
		playfieldGraphics.drawImage(baciaFront, 0, 0, null)
		graphics.drawImage(playfield, 5, 5, null)
		graphics.drawImage(wrapper, 0, 0, null)

		drawNeeds(pet, graphics)

		val message = context.sendFile(base.makeRoundedCorners(14), "tamagotchi.png", context.getAsMention(true))

		sleep(4000)

		message.delete().complete()

		pet.hygiene += 0.15f

		if (applyPetChanges(context, pet))
			return

		handleGameplay(context)
	}

	fun handleBladder(context: CommandContext) {
		val pet = getPet(context)

		val base = BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB)
		val graphics = base.graphics
		val wrapper = ImageIO.read(File(Loritta.ASSETS, "tamagotchi.png"))

		val playfield = drawPlayfield(pet)
		val playfieldGraphics = playfield.graphics

		val toilet = ImageIO.read(File(Loritta.ASSETS, "tamagotchi/privada.png"))
		playfieldGraphics.drawImage(toilet, 0, 0, null)
		playfieldGraphics.drawImage(getPetImage(pet), 174, 68, null)
		graphics.drawImage(playfield, 5, 5, null)
		graphics.drawImage(wrapper, 0, 0, null)

		drawNeeds(pet, graphics)

		val message = context.sendFile(base.makeRoundedCorners(14), "tamagotchi.png", context.getAsMention(true))

		sleep(4000)

		message.delete().complete()

		pet.bladder += 0.15f

		if (applyPetChanges(context, pet))
			return

		handleGameplay(context)
	}

	fun handlePetCreation(context: CommandContext) {
		// Usuário quer um Tamagotchi
		val message = context.reply(
				LoriReply(
						message = "Olá! Você quer um bichinho virtual?",
						prefix = "<:fluffy:372454445721845761>"
				)
		)

		message.addReaction("vieirinha:339905091425271820").complete()

		message.onReactionAddByAuthor(context, {
			if (it.reactionEmote.name == "vieirinha") {
				message.delete().complete()

				val newName = context.reply(
						LoriReply(
								message = "Qual será o nome do seu novo bichinho virtual!",
								prefix = "\uD83E\uDD5A"
						)
				)

				newName.onResponseByAuthor(context, {
					val name = it.message.content

					val petGender = context.reply(
							LoriReply(
									message = "Qual será o gênero de `${name}`?"
							)
					)

					petGender.onReactionAddByAuthor(context, {
						val gender = when (it.reactionEmote.name) {
							"male" -> TamagotchiPet.PetGender.MALE
							"female" -> TamagotchiPet.PetGender.FEMALE
							else -> return@onReactionAddByAuthor
						}

						petGender.delete().complete()

						val petType = context.reply(
								LoriReply(
										message = "Como será o `$name`?"
								)
						)

						petType.onResponseByAuthor(context, {
							if (it.message.emotes.isEmpty()) {
								context.reply(
										LoriReply(
												message = "Por favor, envie um emoji!",
												prefix = Constants.ERROR
										)
								)
								return@onResponseByAuthor
							}

							val emoji = it.message.emotes[0]
							val emoteId = emoji.imageUrl.split("/").last()

							val image = LorittaUtils.downloadImage(emoji.imageUrl)

							ImageIO.write(image, "png", File(Loritta.ASSETS, "pets/$emoteId"))

							petType.delete().complete()

							val pet = TamagotchiPet(name, gender, emoteId)
							val profile = loritta.getLorittaProfileForUser(context.userHandle.id)
							profile.tamagotchi = pet
							loritta save profile
							handleGameplay(context)
						})
					})

					newName.delete().complete()

					petGender.addReaction("male:384048518853296128").complete()
					petGender.addReaction("female:384048518337265665").complete()
				})
			}
		})
	}
}

fun main(args: Array<String>) {
	while (true) {
		println("Quanto?")
		val fall = readLine()!!.toFloat()

		var wow = 1f

		var seconds = 0
		while (wow > 0f) {
			seconds += 1
			wow -= fall
		}

		println("Irá demorar $seconds segundos (${seconds / 60} minutos) até acabar...")
	}
}