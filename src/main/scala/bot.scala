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

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractEvent;
import discord4j.core.`object`.component.ActionRow;
import discord4j.core.`object`.component.SelectMenu;
import discord4j.core.`object`.entity.Message;
import discord4j.core.`object`.entity.channel.TextChannel;
import reactor.core.publisher.Mono;

import scala.collection.mutable.Map
import java.time.ZonedDateTime

import collection.JavaConverters._

object Bot extends App {
  implicit class J2SOpt[T](val x: java.util.Optional[T]) {
    def toScala = if(x.isPresent) { Some(x.get) } else { None }
  }

  val token = sys.env("DISCORD_TEST_BOT")
  val gid = sys.env("GUILD_ID")
  val client: GatewayDiscordClient = (DiscordClient.create(token).login().block())
  val pingCmd = (ApplicationCommandRequest.builder()
                   .name ("pong")
                   .description ("Test pong command")
                   .build)

  val restClient = client.getRestClient
  val appId = restClient.getApplicationId.block

  (restClient
     .getApplicationService
     .createGuildApplicationCommand(appId, Snowflake.asLong(gid), pingCmd)
     // .createGuildApplicationCommand (appId, Snowflake.asLong(gid), pingCmd)
     .doOnError (err ⇒ println(s"Can't create command: ${err}"))
     .onErrorResume (e ⇒ Mono.empty())
     .block)

  client.on(classOf[SlashCommandEvent])
    .filter(evt ⇒ evt.getCommandName == "pong")
    .flatMap(evt ⇒
      evt.reply(msg ⇒ {
                  msg.setContent("Select some options!")
                    .setComponents(
                      ActionRow.of(
                        SelectMenu.of("mySelectMenu",
                                      SelectMenu.Option.of("option 1", "foo"),
                                      SelectMenu.Option.of("option 2", "bar"),
                                      SelectMenu.Option.of("option 3", "baz"))
                          .withMaxValues(3)
                      )
                    )
                })
        .`then`(client.on(classOf[SelectMenuInteractEvent])
                  // .filter(sme ⇒  sme.getCustomId == "foo" || sme.getCustomId == "bar" || sme.getCustomId == "baz")
                  // .filter(sme ⇒  sme.getCustomId == "mySelectMenu")
                  .flatMap(evt ⇒ evt.reply(evt.getCustomId + " values: " + evt.getValues.asScala.mkString(", ")))
                  .`then`
        )
    )
    .blockLast()

  // (client
  //    on new ReactiveEventAdapter() {

  //      override def onSlashCommand(evt: SlashCommandEvent) = {
  //        if(evt.getCommandName() == "ping") {
  //          evt.on(classOf[SlashCommandEvent])
  //            .flatMap(evt ⇒ evt.reply(msg ⇒ {
  //                                       msg.setContent("Select some options!")
  //                                         .setComponents(
  //                                           ActionRow.of(
  //                                             SelectMenu.of("mySelectMenu",
  //                                                           SelectMenu.Option.of("option 1", "foo"),
  //                                                           SelectMenu.Option.of("option 2", "bar"),
  //                                                           SelectMenu.Option.of("option 3", "baz"))
  //                                               .withMaxValues(2)
  //                                           )
  //                                         )
  //                                     }))
  //            .`then`(evt.on(classOf[ SelectMenuInteractEvent]))
  //            .filter(sme ⇒  sme.getCustomId == "foo" || sme.getCustomId == "bar" || sme.getCustomId == "baz")
  //            .flatMap(evt ⇒ evt.reply("Foo"))
  //            .take(3)
  //            .`then`
  //        } else {
  //          Mono.empty()
  //        }
  //      }
  //    }
  //    blockLast)
}
