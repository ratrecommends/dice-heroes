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

package com.vlaaad.dice.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.api.services.multiplayer.GameSession;
import com.vlaaad.dice.api.services.multiplayer.IParticipant;
import com.vlaaad.dice.services.util.ClientServerMessage;

/**
 * Created 27.07.14 by vlaaad
 */
public class LocalSession extends GameSession {

    private final LocalMultiplayer multiplayer;
    private final String otherPlayerId;
    private final IParticipant me;
    private final IParticipant other;
    private final ObjectSet<IParticipant> others;
    private final ObjectMap<String, IParticipant> all = new ObjectMap<String, IParticipant>();

    @SuppressWarnings("unchecked")
    public LocalSession(final LocalMultiplayer multiplayer, final String otherPlayerId) {
        this.multiplayer = multiplayer;
        this.otherPlayerId = otherPlayerId;
        setVariant(Config.pvpModes.get("global-2").variant);
        me = new IParticipant() {
            @Override public String getDisplayedName() { return multiplayer.participantId; }
            @Override public String getImageUrl() { return "https://pp.vk.me/c307401/v307401067/e8db/rg1r1GJ-dME.jpg"; }
            @Override public String getId() { return multiplayer.participantId; }
            @Override public String toString() { return getId(); }
        };
        other = new IParticipant() {
            @Override public String getDisplayedName() { return otherPlayerId; }
            @Override public String getImageUrl() { return "https://pp.vk.me/c620127/v620127169/11996/5TkNN6HNbPE.jpg"; }
            @Override public String getId() { return otherPlayerId; }
            @Override public String toString() { return getId(); }
        };
        others = ObjectSet.with(other);
        all.put(me.getId(), me);
        all.put(other.getId(), other);
        disconnectFuture().addListener(new IFutureListener() {
            @Override public void onHappened(Object o) {
                multiplayer.sendToServer(ClientServerMessage.Type.endSession);
            }
        });
    }
    @Override protected IFuture<Boolean> doSendToWithCallback(IParticipant participant, String message) {
        doSendTo(participant, message);
        final Future<Boolean> future = new Future<Boolean>();
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                future.happen(true);
            }
        });
        return future;
    }
    @Override public IParticipant getMe() {
        return me;
    }
    @Override public ObjectSet<? extends IParticipant> getOthers() {
        return others;
    }
    @Override public ObjectMap<String, ? extends IParticipant> getAll() {
        return all;
    }
    @Override protected void doSendTo(IParticipant participant, String message) {
        if (participant == other)
            multiplayer.sendToServer(ClientServerMessage.Type.sessionMessage, message);
        else if (participant == me)
            receiveMessage(participant, message);
    }

    @Override protected void onDisconnected() {

    }

    public void receive(String participantId, String data) {
        receiveMessage(all.get(participantId), data);
    }
}
