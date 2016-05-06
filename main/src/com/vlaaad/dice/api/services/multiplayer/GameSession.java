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

package com.vlaaad.dice.api.services.multiplayer;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.vlaaad.common.util.Logger;
import com.vlaaad.common.util.Option;
import com.vlaaad.common.util.Tuple2;
import com.vlaaad.common.util.Tuple3;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.pvp.PvpMode;

/**
 * Created 25.07.14 by vlaaad
 */
public abstract class GameSession {

    private IMessageListener listener;
    private final Array<Tuple2<IParticipant, String>> messageQueue = new Array<Tuple2<IParticipant, String>>();
    private final Future<Tuple3<Boolean, Option<String>, Option<Throwable>>> disconnectFuture = new Future<Tuple3<Boolean, Option<String>, Option<Throwable>>>();
    private int variant = -1111111;

    public final void sendTo(IParticipant participant, String message) {
        if (disconnectFuture.isHappened())
            return;
        doSendTo(participant, message);
    }
    public final IFuture<Boolean> sendToWithCallback(IParticipant participant, String message) {
        if (disconnectFuture.isHappened())
            return Future.completed(false);
        return doSendToWithCallback(participant, message);
    }
    protected abstract IFuture<Boolean> doSendToWithCallback(IParticipant participant, String message);
    public abstract IParticipant getMe();
    public abstract ObjectSet<? extends IParticipant> getOthers();
    public abstract ObjectMap<String, ? extends IParticipant> getAll();
    protected abstract void doSendTo(IParticipant participant, String message);

    public void setVariant(int variant) {
        this.variant = variant;
    }

    public final void receiveMessage(IParticipant from, String message) {
        if (listener == null) {
            messageQueue.add(Tuple2.make(from, message));
        } else {
            listener.receive(from, message);
        }
    }

    public final void disconnect(boolean otherUserLeft) {
        disconnect(otherUserLeft, null, Option.<Throwable>none());
    }

    public final void disconnect(boolean otherUserLeft, Throwable throwable) {
        disconnect(otherUserLeft, null, throwable);
    }

    public final void disconnect(boolean otherUserLeft, String reason) {
        disconnect(otherUserLeft, reason, Option.none(Throwable.class));
    }

    public final void disconnect(boolean otherUserLeft, String reason, Throwable throwable) {
        disconnect(otherUserLeft, reason, Option.option(throwable));
    }

    public final void disconnect(boolean otherUserLeft, String reason, Option<Throwable> throwableOption) {
        if (disconnectFuture.isHappened())
            return;
//        Logger.debug("disconnecting, reason: " + reason + ", exception: " + throwableOption);
        disconnectFuture.happen(Tuple3.make(otherUserLeft, Option.option(reason), throwableOption));
        onDisconnected();
    }

    protected abstract void onDisconnected();

    public void setMessageListener(IMessageListener listener) {
        this.listener = listener;
        if (listener != null) {
            for (Tuple2<IParticipant, String> message : messageQueue) {
                listener.receive(message._1, message._2);
            }
            messageQueue.clear();
        }
    }

    public IFuture<Tuple3<Boolean, Option<String>, Option<Throwable>>> disconnectFuture() {
        return disconnectFuture;
    }

    public PvpMode getMode() { return Config.pvpModes.get(variant); }
}
