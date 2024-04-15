package io.github.seggan.notwalshbot

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.ALL
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import io.github.seggan.notwalshbot.commands.CommandEvent
import io.github.seggan.notwalshbot.commands.CommandExecutor
import io.github.seggan.notwalshbot.filters.ScamFilter
import io.github.seggan.notwalshbot.server.Channels
import io.github.seggan.notwalshbot.server.isAtLeast
import io.github.seggan.notwalshbot.util.DiscordTimestamp
import io.github.seggan.notwalshbot.util.respondEphemeral
import io.ktor.client.*
import io.ktor.client.engine.java.*
import kotlinx.coroutines.runBlocking
import kotlin.io.path.Path
import kotlin.io.path.readText

fun main() = runBlocking {
    println("Starting")
    bot = Kord(Path("token.txt").readText())

    bot.on<ReadyEvent> {
        log("Hello, World!")
        log("Started with session ID `$sessionId` at ${DiscordTimestamp.now()}")
        ScamFilter.updateScamCache(this@runBlocking)
        println("Ready")
    }
    bot.on(consumer = MessageCreateEvent::onMessageSend)

    println("Registering commands")
    val commandMap = CommandExecutor.all.associateBy {
        bot.createGuildChatInputCommand(
            SERVER_ID,
            it.name,
            it.description,
            it.args
        ).id
    }
    bot.on<CommandEvent> {
        val command = commandMap[interaction.command.rootId] ?: return@on
        val permission = command.permission
        if (permission == null || interaction.user.isAtLeast(permission)) {
            with(command) { execute() }
        } else {
            respondEphemeral("You do not have permission to use this command.")
        }
    }

    println("Logging in")
    bot.login {
        @OptIn(PrivilegedIntent::class)
        intents = Intents.ALL
    }
}

lateinit var bot: Kord
val httpClient = HttpClient(Java) {
    engine {
        pipelining = true
        protocolVersion = java.net.http.HttpClient.Version.HTTP_2
    }
}

val SERVER_ID = Snowflake(809178621424041997)

suspend fun log(message: String) {
    Channels.BOT_TESTING.get().createMessage(message)
}