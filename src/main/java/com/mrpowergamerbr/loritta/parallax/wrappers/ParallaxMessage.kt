package com.mrpowergamerbr.loritta.parallax.wrappers

import com.mrpowergamerbr.loritta.Loritta
import jdk.nashorn.api.scripting.ScriptObjectMirror
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Message



class ParallaxMessage(private val message: Message) {
	// TODO: attachments
	val author = ParallaxUser(message.author)
	val channel = ParallaxTextChannel(message.textChannel)
	val cleanContent get() = message.strippedContent
	val client = ParallaxClient()
	val content get() = message.content
	val createdAt get() = message.creationTime
	// TODO: createdTimestamp
	val deletable get() = message.author.id == Loritta.config.clientId || message.guild.selfMember.hasPermission(Permission.MESSAGE_MANAGE)
	val editable get() = message.author.id == Loritta.config.clientId
	val editedAt get() = message.editedTime
	// TODO: editedTimestamp
	// TODO: edits
	// TODO: embeds
	val guild = ParallaxGuild(message.guild)
	// TODO: hit
	val id get() = message.id
	val member get() = ParallaxMember(message.member)
	// TODO: mentions
	// TODO: nonce
	val pinnable get() = message.author.id == Loritta.config.clientId
	val pinned get() = message.isPinned
	// TODO: reactions
	// TODO: system
	val tts get() = message.isTTS
	// TODO: type
	// TODO: webhookID

	// TODO: awaitReactions

	fun clearReactions() {
		message.clearReactions().complete()
	}

	// TODO: createReactionCollector

	fun delete() {
		message.delete().complete()
	}

	fun edit(content: String) {
		message.editMessage(content).complete()
	}

	fun edit(embed: ParallaxEmbed) {
		message.editMessage(embed.toDiscordEmbed()).complete()
	}

	fun edit(mirror: ScriptObjectMirror) {
		edit(ParallaxUtils.toParallaxEmbed(mirror))
	}

	// TODO: isMemberMentioned
	// TODO: isMentioned

	fun pin() {
		message.pin().complete()
	}

	fun unpin() {
		message.unpin().complete()
	}

	fun react(reaction: String) {
		message.addReaction(reaction).complete()
	}

	fun reply(content: String) {
		channel.send(content)
	}

	fun reply(embed: ParallaxEmbed) {
		channel.send(embed)
	}

	fun reply(mirror: ScriptObjectMirror) {
		reply(ParallaxUtils.toParallaxEmbed(mirror))
	}

	override fun toString(): String = content
}