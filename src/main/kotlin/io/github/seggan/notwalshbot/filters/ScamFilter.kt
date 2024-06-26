package io.github.seggan.notwalshbot.filters

import dev.kord.core.behavior.ban
import dev.kord.core.entity.Message
import io.github.seggan.notwalshbot.httpClient
import io.github.seggan.notwalshbot.log
import io.github.seggan.notwalshbot.server.Channels
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.time.Duration.Companion.seconds

object ScamFilter : MessageFilter {

    private val urlRegex =
        """(http(s)?://.)?(www\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_+.~#?&/=]*)""".toRegex()

    private val scamCache = mutableSetOf<Regex>()

    override suspend fun test(message: Message): Boolean {
        if (message.author?.isBot == true) return false
        return scamCache.any { it in message.content }
    }

    override suspend fun act(message: Message) {
        message.delete("Scam link detected")
        message.getAuthorAsMember().ban {
            reason = "Scam link"
            deleteMessageDuration = 0.seconds
        }
        val content = message.content
            .replace(urlRegex) { "<${it.groups[1]?.value}>" }
            .replace("@", "\\@")
        Channels.BOT_LOGS.get()
            .createMessage("Banned ${message.author?.mention} for sending scam link: $content")
    }

    suspend fun updateScamCache() {
        scamCache.clear()
        addToScamCache("https://bad-domains.walshy.dev/domains.txt")
        addToScamCache("https://raw.githubusercontent.com/DevSpen/scam-links/master/src/links.txt")
        addToScamCache("https://raw.githubusercontent.com/BuildBot42/discord-scam-links/main/list.txt")
        addToScamCache("https://raw.githubusercontent.com/Discord-AntiScam/scam-links/main/list.txt")
    }

    private suspend fun addToScamCache(url: String) {
        val response = httpClient.get(url)
        val body = response.bodyAsText()
        if (response.status == HttpStatusCode.OK) {
            scamCache.addAll(
                body.split('\n')
                    .filter { it.isNotBlank() }
                    .map { it.toRegex() }
            )
        } else {
            log("Failed to add scam links: ${response.status} $body")
        }
    }
}

