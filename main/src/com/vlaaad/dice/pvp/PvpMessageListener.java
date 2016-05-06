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

package com.vlaaad.dice.pvp;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.util.Logger;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.api.services.multiplayer.GameSession;
import com.vlaaad.dice.api.services.multiplayer.IMessageListener;
import com.vlaaad.dice.api.services.multiplayer.IParticipant;
import com.vlaaad.dice.pvp.messaging.IPvpMessage;
import com.vlaaad.dice.states.PvpPlayState;

/**
 * Created 28.07.14 by vlaaad
 */
public abstract class PvpMessageListener implements IMessageListener {

    protected final GameSession session;
    protected final PvpPlayState state;
    private final Array<? extends IParticipant> all;
    private final Array<IParticipant> others;
    private ObjectMap<Class, IMessageProcessor> processors = new ObjectMap<Class, IMessageProcessor>();
    private ObjectIntMap<IParticipant> sentMessagesIndexes = new ObjectIntMap<IParticipant>();
    private ObjectIntMap<IParticipant> receivedMessagesIndexes = new ObjectIntMap<IParticipant>();
    private ObjectMap<IParticipant, Array<IPvpMessage>> queue = new ObjectMap<IParticipant, Array<IPvpMessage>>();

    public PvpMessageListener(PvpPlayState state) {
        this.session = state.session;
        this.state = state;
        this.all = session.getAll().values().toArray();
        this.others = new Array<IParticipant>(session.getOthers().size);
        for (IParticipant participant : session.getOthers()) {
            others.add(participant);
        }
        init(state);
    }

    protected abstract void init(PvpPlayState state);

    public final IFuture<Boolean> sendToWithCallback(IParticipant participant, IPvpMessage message) {
        message.packetIdx = sentMessagesIndexes.getAndIncrement(participant, 0, 1);
        if (participant == session.getMe()) {
            receiveMessage(participant, message);
            return Future.completed(true);
        } else {
            return session.sendToWithCallback(participant, Config.json.toJson(message, (Class) null));
        }
    }

    public final void sendToServer(IPvpMessage message) {
        sendTo(state.server, message);
    }

    public final void sendToOthers(IPvpMessage message) {
        for (int i = 0; i < others.size; i++) {
            sendTo(others.get(i), message);
        }
    }

    public final void sendToAll(IPvpMessage message) {
        // we need firstly to send everything to others, because we can listen to this message
        // and in listener send another messages.
        // if we receive message before it's send to other, and then send another, it'll break order of messages
        sendToOthers(message);
        sendTo(session.getMe(), message);
    }

    public final void sendTo(IParticipant participant, IPvpMessage message) {
        message.packetIdx = sentMessagesIndexes.getAndIncrement(participant, 0, 1);
        if (participant == session.getMe()) {
            receiveMessage(participant, message);
        } else {
            session.sendTo(participant, Config.json.toJson(message, (Class) null));
        }
    }

    protected final <T extends IPvpMessage> void register(Class<T> messageType, IMessageProcessor<T> processor) {
        if (processors.put(messageType, processor) != null)
            throw new IllegalStateException("there was already a listener for messages of type " + messageType);
    }

    public final void unregister(Class<? extends IPvpMessage> messageType) {
        processors.remove(messageType);
    }

    @Override public final void receive(IParticipant from, String message) {
        Logger.debug("pvp message listener received message from " + from + ": " + message);
        IPvpMessage m = Config.json.fromJson(null, message);
        if (m.packetIdx == -1)
            throw new IllegalStateException("message " + message + " has no defined packetIdx!");
        int neededIndex = receivedMessagesIndexes.get(from, 0);
        if (m.packetIdx == neededIndex) {
            receiveMessage(from, m);
            checkQueue(from);
        } else {
            Logger.log("**************************************************************************");
            Logger.log("received message with id " + m.packetIdx + " while waiting for " + neededIndex + ", adding to queue");
            Logger.log("**************************************************************************");
            Array<IPvpMessage> messages = queue.get(from);
            if (messages == null) {
                messages = new Array<IPvpMessage>(1);
                queue.put(from, messages);
            }
            messages.add(m);
            messages.sort(IPvpMessage.COMPARATOR);
        }

    }
    private void checkQueue(IParticipant from) {
        Array<IPvpMessage> messages = queue.get(from);
        if (messages == null || messages.size == 0)
            return;
        int neededIndex = receivedMessagesIndexes.get(from, 0);
        if (messages.first().packetIdx == neededIndex) {
            IPvpMessage m = messages.removeIndex(0);
            receiveMessage(from, m);
            checkQueue(from);
        }
    }

    @SuppressWarnings("unchecked")
    private void receiveMessage(IParticipant from, IPvpMessage m) {
        try {
            if (m.packetIdx != receivedMessagesIndexes.get(from, 0))
                throw new IllegalStateException(
                    "received message " + m + " with packet index = " + m.packetIdx +
                        ", but was waiting for message with index = " + receivedMessagesIndexes.get(from, 0)
                );
            receivedMessagesIndexes.put(from, m.packetIdx + 1);

            Class<? extends IPvpMessage> type = m.getClass();
            IMessageProcessor processor = processors.get(type);
            if (processor != null)
                processor.receive(from, m);
            else
                Logger.error("no processor for message " + m + " in " + this);
        } catch (Exception e) {
            session.disconnect(false, Config.thesaurus.localize("disconnect-exception-on-receive"), e);
        }
    }
}
