package com.mrpowergamerbr.loritta.nashorn.wrappers

import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.userdata.LorittaServerUserData
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.core.entities.Guild

/**
 * Wrapper para a Guild, usado para imagens de comandos Nashorn
 */
class NashornGuild(private val guild: Guild) {
	private val serverConfig: ServerConfig

	init {
		serverConfig = loritta.getServerConfigForGuild(guild.id)
	}

	@NashornCommand.NashornDocs()
	fun getName(): String {
		return guild.name
	}

	@NashornCommand.NashornDocs()
	fun getIconUrl(): String {
		return guild.iconUrl
	}

	@NashornCommand.NashornDocs()
	fun getIcon(): NashornImage? {
		val image = LorittaUtils.downloadImage(guild.iconUrl) ?: return null;
		return NashornImage(image)
	}

	@NashornCommand.NashornDocs()
	fun getMembers(): MutableList<NashornLorittaUser> {
		val members = mutableListOf<NashornLorittaUser>()

		guild.members.forEach {
			members.add(NashornLorittaUser(it, serverConfig.userData.getOrDefault(it.user.id, LorittaServerUserData())))
		}

		return members
	}

	@NashornCommand.NashornDocs()
	fun getRoles(): MutableList<NashornRole> {
		val roles = mutableListOf<NashornRole>()

		guild.roles.forEach {
			roles.add(NashornRole(it))
		}

		return roles
	}

	@NashornCommand.NashornDocs()
	fun getRoleById(id: String): NashornRole {
		return NashornRole(guild.getRoleById(id))
	}

	@NashornCommand.NashornDocs()
	fun getMemberById(id: String): NashornLorittaUser {
		return NashornLorittaUser(guild.getMemberById(id), serverConfig.userData.getOrDefault(id, LorittaServerUserData()));
	}

	@NashornCommand.NashornDocs()
	fun play(url: String) {
		if (serverConfig.musicConfig.isEnabled) {
			loritta.loadAndPlayNoFeedback(guild, serverConfig, url)
		}
	}

	@NashornCommand.NashornDocs()
	fun ban(user: NashornUser, delDays: Int, reason: String) {
		guild.controller.ban(user.user, delDays, reason).complete()
	}

	@NashornCommand.NashornDocs()
	fun kick(member: NashornMember, reason: String) {
		guild.controller.kick(member.member, reason).complete()
	}

	@NashornCommand.NashornDocs()
	fun unban(id: String) {
		guild.controller.unban(id).complete()
	}
}