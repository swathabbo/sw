package com.mrpowergamerbr.loritta.threads

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.listeners.DiscordListener
import com.mrpowergamerbr.loritta.listeners.EventLogListener
import com.mrpowergamerbr.loritta.listeners.MusicMessageListener
import com.mrpowergamerbr.loritta.listeners.UpdateTimeListener
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.LORITTA_SHARDS
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class ShardReviverThread : Thread("Shard Reviver") {
	override fun run() {
		super.run()

		while (true) {
			try {
				checkAndReviveDeadShards()
			} catch (e: Exception) {
				e.printStackTrace()
			}
			Thread.sleep(1000)
		}
	}

	fun checkAndReviveDeadShards() {
		try {
			for (shard in LORITTA_SHARDS.shards) {
				val lastUpdate = LORITTA_SHARDS.lastJdaEventTime.getOrDefault(shard, System.currentTimeMillis())

				val seconds = (System.currentTimeMillis() - lastUpdate) / 1000

				if (seconds >= 10) {
					println("[!] Shard ${shard.shardInfo.shardId} não recebeu update a mais de 10s! ~  ${seconds}s")
				}
			}

			val deadShards = LORITTA_SHARDS.shards.filter {
				val lastUpdate = LORITTA_SHARDS.lastJdaEventTime.getOrDefault(it, System.currentTimeMillis())

				System.currentTimeMillis() - lastUpdate > 12500
			}

			if (deadShards.isNotEmpty()) {
				val okHttpBuilder = OkHttpClient.Builder()
						.connectTimeout(60, TimeUnit.SECONDS)
						.readTimeout(60, TimeUnit.SECONDS)
						.writeTimeout(60, TimeUnit.SECONDS)

				val discordListener = DiscordListener(loritta) // Vamos usar a mesma instância para todas as shards
				val eventLogListener = EventLogListener(loritta) // Vamos usar a mesma instância para todas as shards
				val updateTimeListener = UpdateTimeListener(loritta)
				val messageListener = MusicMessageListener(loritta)

				for (deadShard in deadShards) {
					println("Reiniciando shard ${deadShard.shardInfo.shardId}...")
					var guild = loritta.lorittaShards.getGuildById("297732013006389252")
					if (guild != null) {
						val textChannel = guild.getTextChannelById("297732013006389252")
						textChannel.sendMessage("Shard ${deadShard.shardInfo.shardId}${if (false) " (\uD83C\uDFB6)" else ""} demorou mais de 30 segundos para responder... \uD83D\uDE22 ~ Irei reiniciar esta shard (e torcer para que não dê problema novamente! \uD83D\uDE47)").complete()
					}
					val shardId = deadShard.shardInfo.shardId

					LORITTA_SHARDS.shards.remove(deadShard)
					LORITTA_SHARDS.lastJdaEventTime.remove(deadShard)

					thread(block = deadShard::shutdownNow)

					val shard = JDABuilder(AccountType.BOT)
							.useSharding(shardId, Loritta.config.shards)
							.setToken(Loritta.config.clientToken)
							.setHttpClientBuilder(okHttpBuilder)
							.setCorePoolSize(24)
							.buildBlocking()

					shard.addEventListener(updateTimeListener)
					shard.addEventListener(discordListener)
					shard.addEventListener(eventLogListener)

					LORITTA_SHARDS.shards.add(shard)

					guild = loritta.lorittaShards.getGuildById("297732013006389252")
					if (guild != null) {
						val textChannel = guild.getTextChannelById("297732013006389252")
						textChannel.sendMessage("Shard ${shard.shardInfo.shardId}${if (false) " (\uD83C\uDFB6)" else ""} foi reiniciada com sucesso! \uD83D\uDC4F").complete()
					}
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}