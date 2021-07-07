import discord4j.core._
import discord4j.core.event.domain.lifecycle._
import discord4j.core.event.domain.message._

import discord4j.common.util.Snowflake
import discord4j.core.event.ReactiveEventAdapter
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.core.`object`.command.ApplicationCommandInteraction
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.rest.RestClient
import discord4j.rest.util.ApplicationCommandOptionType
import reactor.core.publisher.Mono

import scala.collection.mutable.Map
import java.time.ZonedDateTime

object Bot extends App {
  implicit class J2SOpt[T](val x: java.util.Optional[T]) {
    def toScala = if(x.isPresent) { Some(x.get) } else { None }
  }

  val token = sys.env("DISCORD_TEST_BOT")
  val gid = sys.env("GUILD_ID")
  println("Starting")
  val client: GatewayDiscordClient = (DiscordClient.create(token).login().block())
  println("Creating command")
  val pingCmd = ((ApplicationCommandRequest.builder())
                   name "ping"
                   description "Test ping command with cooldown"
                   build)

  println("Registering command")
  val restClient = client.getRestClient
  val appId = restClient.getApplicationId.block

  ((restClient.getApplicationService)
     createGuildApplicationCommand (appId, Snowflake.asLong(gid), pingCmd)
     doOnError (err ⇒ println(s"Can't create command: ${err}"))
     onErrorResume (e ⇒ Mono.empty())
     block)

  println("Listening to command")
  (client
     on new ReactiveEventAdapter() {
       val cooldown_table = Map[Long, ZonedDateTime]()

       override def onSlashCommand(evt: SlashCommandEvent) = {
         if(evt.getCommandName() == "ping") {
         val reply = evt
           .getInteraction()
           .getMember()
           .toScala
           .map(mem ⇒ {
                  print(s"${mem.getId().asString()}, ")
                  println(cooldown_table.map{ case (k, _) ⇒ k }.mkString(", "))
                  if(cooldown_table.contains(mem.getId().asLong())) {
                    val prev_time = cooldown_table(mem.getId().asLong())
                    val now = ZonedDateTime.now()
                    print("Checking CD")
                    if(prev_time.plusSeconds(10).isBefore(now)) {
                      println("CD over")
                      cooldown_table(mem.getId().asLong()) = now
                      "pong"
                    } else {
                      println("CD active")
                      "Cooldown active"
                    }
                  } else {
                    println("Adding user to the CD table")
                    cooldown_table += (mem.getId().asLong() → ZonedDateTime.now())
                    "pong"
                  }
                })
           .getOrElse("Error: no member")
           evt.reply(reply)
         } else {
           Mono.empty()
         }
       }
     }
     blockLast)
}
