
/*
 * Dice heroes is a turn based rpg-strategy game where characters are dice.
 * Copyright (C) 2016 Vladislav Protsenko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.net.InetSocketAddress
import java.nio.charset.Charset
import java.util.concurrent.Executors

import akka.actor._
import com.vlaaad.dice.services.util.ClientServerMessage
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.buffer.{ChannelBuffer, ChannelBuffers}
import org.jboss.netty.channel._
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder

/** Created 18.07.14 by vlaaad */
object TestServer extends App {

  val sys = ActorSystem()

  class Processor extends Actor with ActorLogging {
    val players = collection.mutable.Map[String, ConnectionHandler]()
    val invites = collection.mutable.Map[String, collection.mutable.Set[String]]()
    val sessions = collection.mutable.Map[ConnectionHandler, ConnectionHandler]()


    def loadPlayers(participantId: String) = {
      players(participantId).send(ClientServerMessage.Type.loadPlayersToInvite, data = players.keys.filter(_ != participantId).mkString(","))
    }

    def clean(participantId: String) = {
      val h = players(participantId)
      if (sessions.contains(h)) {
        endSession(participantId)
      }
      players.remove(participantId)
      invites.remove(participantId)
      for ((player, invites) <- invites) {
        if (invites.remove(participantId)) loadInvites(player)
      }
      players.keys.foreach(loadPlayers)
    }


    def loadInvites(participantId: String) = {
      if (invites.contains(participantId)) {
        players(participantId).send(ClientServerMessage.Type.loadInvites, data = invites(participantId).mkString(","))
      }
    }

    def declineInvite(inviterId: String, inviteeId: String) = {
      invites(inviteeId).remove(inviterId)
      loadInvites(inviteeId)
      players(inviterId).send(ClientServerMessage.Type.declineInvite, data = inviteeId, participantId = inviteeId)
    }

    def startSession(id: String, withId: String) = {
      players(id).send(ClientServerMessage.Type.startSession, participantId = withId, data = withId)
    }

    def acceptInvite(inviterId: String, inviteeId: String): Unit = {
      invites(inviteeId).remove(inviterId)
      loadInvites(inviteeId)
      val inviter = players(inviterId)
      inviter.send(ClientServerMessage.Type.acceptInvite, participantId = inviteeId, data = inviteeId)
      val invitee = players(inviteeId)
      sessions += invitee -> inviter
      sessions += inviter -> invitee
      startSession(inviteeId, inviterId)
      startSession(inviterId, inviteeId)
    }

    def endSession(id: String, other: String) = {
      players(id).send(ClientServerMessage.Type.endSession, participantId = other, data = other)
    }

    def endSession(id: String): Unit = {
      val player = players(id)
      sessions.get(player).foreach(other => {
        val otherId = players.find(_._2 == other).get._1
        sessions.remove(player)
        sessions.remove(other)
        endSession(id, otherId)
        endSession(otherId, id)
      })
    }

    override def receive: Receive = {
      case (handler: ConnectionHandler, m: ClientServerMessage) =>
        //        log.info(s"receive message $m")
        if (!players.contains(m.participantId)) {
          players += m.participantId -> handler
          loadPlayers(m.participantId)
        }
        loadInvites(m.participantId)
        m.`type` match {
          case ClientServerMessage.Type.loadInvites =>
            loadInvites(m.participantId)
          case ClientServerMessage.Type.loadPlayersToInvite =>
            loadPlayers(m.participantId)
          case ClientServerMessage.Type.invitePlayer =>
            val invitee = m.data
            val inviter = m.participantId
            if (!players.contains(invitee)) {
              declineInvite(inviter, invitee)
            } else {
              invites.getOrElseUpdate(invitee, collection.mutable.Set[String]()) += inviter
              loadInvites(invitee)
            }
          case ClientServerMessage.Type.declineInvite =>
            val inviterId = m.data
            declineInvite(inviterId, m.participantId)
          case ClientServerMessage.Type.acceptInvite =>
            acceptInvite(m.data, m.participantId)
          case ClientServerMessage.Type.endSession =>
            endSession(m.participantId)
          case ClientServerMessage.Type.sessionMessage =>
            val h = players(m.participantId)
            sessions(h).send(ClientServerMessage.Type.sessionMessage, m.participantId, m.data)
          case ClientServerMessage.Type.disconnect =>
            clean(m.participantId)
          case other => log.error(s"unknown message type: $other")
        }
      case other =>
        log.error(s"unknown message: $other")
    }
  }

  val processor = sys.actorOf(Props[Processor])

  class ConnectionHandler extends SimpleChannelUpstreamHandler {
    var context: Option[ChannelHandlerContext] = None
    var id: Option[String] = None

    override def channelOpen(ctx: ChannelHandlerContext, e: ChannelStateEvent): Unit = {
      context = Option(ctx)
      println(s"$this connected")
    }

    def send(`type`: ClientServerMessage.Type, participantId: String = null, data: String = null) = {
      val m = ClientServerMessage.json.toJson(new ClientServerMessage(participantId, `type`, data))
      context.foreach(ctx => {
        Channels.write(ctx.getChannel, ChannelBuffers.copiedBuffer(s"$m\n", Charset.forName("UTF-8")))
      })
    }

    override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent): Unit = {
      val strings = new String(e.getMessage.asInstanceOf[ChannelBuffer].array()).split("\n")
      strings.filter(_.length != 0).foreach(str => {
        println(s"message received: $str")
        val m = ClientServerMessage.json.fromJson(classOf[ClientServerMessage], str)
        id match {
          case Some(v) => require(v == m.participantId)
          case None => id = Option(m.participantId)
        }
        processor ! this -> m
      })
    }

    override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent): Unit = {
      e.getCause.printStackTrace()
      e.getChannel.close()
    }

    override def channelClosed(ctx: ChannelHandlerContext, e: ChannelStateEvent): Unit = {
      println(s"$this disconnected")
      context = None
      id.foreach(v => processor ! this -> new ClientServerMessage(v, ClientServerMessage.Type.disconnect))
      id = None
    }
  }

  val bootstrap = new ServerBootstrap(
    new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool())
  )

  val delimiter = ChannelBuffers.copiedBuffer("\n", Charset.forName("UTF-8"))

  bootstrap.setPipelineFactory(new ChannelPipelineFactory {
    override def getPipeline: ChannelPipeline = Channels.pipeline(new DelimiterBasedFrameDecoder(100000, delimiter), new ConnectionHandler())
  })
  bootstrap.bind(new InetSocketAddress(1337))
  println("server started!")
}
