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
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.util.Option;
import com.vlaaad.common.util.StateDispatcher;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.api.services.multiplayer.GameSession;
import com.vlaaad.dice.api.services.multiplayer.IMultiplayer;
import com.vlaaad.dice.services.ui.InvitesWindow;
import com.vlaaad.dice.services.util.ClientServerMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created 24.07.14 by vlaaad
 */
public class LocalMultiplayer implements IMultiplayer {

    private boolean initialized = false;
    private Socket socket;
    public final String participantId = UUID.randomUUID().toString();
    private InvitesWindow invitesWindow;// = new InvitesWindow();
    private InvitesWindow playersWindow;// = new InvitesWindow();
    private Future<Void> future;

    public void init() {
        if (initialized)
            return;
        invitesWindow = new InvitesWindow();
        playersWindow = new InvitesWindow();
        initialized = true;
        try {
            socket = Gdx.net.newClientSocket(Net.Protocol.TCP, "localhost", 1337, new SocketHints());
        } catch (Exception e) {
            return;
        }

        new Thread(new Runnable() {
            @Override public void run() {
                final BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (true) {
                    try {
                        final String msg = r.readLine();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override public void run() {
                                ClientServerMessage message = ClientServerMessage.json.fromJson(ClientServerMessage.class, msg);
                                receive(message);
                            }
                        });
                    } catch (IOException ignored) {
                        System.out.println(ignored.getMessage());
                    }
                }
            }
        }).start();
        sendToServer(ClientServerMessage.Type.loadPlayersToInvite);
        sendToServer(ClientServerMessage.Type.loadInvites);
    }

    private void receive(ClientServerMessage message) {
//        Logger.debug("received message " + message);
        switch (message.type) {
            case loadInvites:
                if (message.data == null)
                    break;
                Array<String> invitesList = Array.with(message.data.split(","));
                invitesWindow.setPlayers(invitesList);
                invites.setState(invitesList.size);
                break;
            case loadPlayersToInvite:
                if (message.data == null)
                    break;
                Array<String> otherPLayers = Array.with(message.data.split(","));
                playersWindow.setPlayers(otherPLayers);
                break;
            case acceptInvite:
                break;
            case declineInvite:
                future.happen();
                break;
            case startSession:
                session.setState(Option.<GameSession>some(new LocalSession(this, message.data)));
                future.happen();
                break;
            case sessionMessage:
                if (session.getState().isDefined())
                    ((LocalSession) session.getState().get()).receive(message.participantId, message.data);
                break;
            case endSession:
                if (session.getState().isDefined())
                    session.getState().get().disconnect(true, Config.thesaurus.localize("disconnect-peer-left"));
                break;
            default:
                throw new IllegalStateException("unknown message type: " + message.type);
        }
    }

    public void sendToServer(ClientServerMessage.Type type) {
        sendToServer(type, null);
    }

    public void sendToServer(ClientServerMessage.Type type, String data) {
        if (socket == null)
            return;
        try {
            ClientServerMessage m = new ClientServerMessage(participantId, type, data);
            socket.getOutputStream().write((ClientServerMessage.json.toJson(m) + "\n").getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private final StateDispatcher<Integer> invites = new StateDispatcher<Integer>(0);
    private final StateDispatcher<Option<GameSession>> session = new StateDispatcher<Option<GameSession>>(Option.<GameSession>none());

    @Override public StateDispatcher<Integer> invites() {
        init();
        return invites;
    }
    @Override public StateDispatcher<Option<GameSession>> currentSession() {
        init();
        return session;
    }

    @Override public IFuture<Void> inviteFriends(int playersToInvite, int variant) {
        init();
        sendToServer(ClientServerMessage.Type.loadPlayersToInvite);
        future = new Future<Void>();
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                playersWindow.show(new InvitesWindow.Callback() {
                    @Override public void onSelected(String player) {
                        sendToServer(ClientServerMessage.Type.invitePlayer, player);
                    }
                    @Override public void onCancelled() {
                        future.happen();
                    }
                });
            }
        });
        return future;
    }

    @Override public IFuture<Void> displayInvitations() {
        init();
        future = new Future<Void>();
        sendToServer(ClientServerMessage.Type.loadInvites);
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                invitesWindow.show(new InvitesWindow.Callback() {
                    @Override public void onSelected(String player) {
                        sendToServer(ClientServerMessage.Type.acceptInvite, player);
                    }
                    @Override public void onCancelled() {
                        future.happen();
                    }
                });
            }
        });
        return future;
    }
    @Override public IFuture<Void> quickMatch(int playersToInvite, int variant) {
        return inviteFriends(playersToInvite, variant);
    }
}
