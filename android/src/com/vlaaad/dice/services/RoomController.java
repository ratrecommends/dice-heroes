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
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.*;
import com.vlaaad.common.util.Logger;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.api.services.multiplayer.GameSession;
import com.vlaaad.dice.api.services.multiplayer.IParticipant;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created 24.07.14 by vlaaad
 */
public class RoomController implements RoomUpdateListener, RealTimeMessageReceivedListener, RoomStatusUpdateListener/*, RoomStatusUpdateListener */ {

    public final GameSession session;
    private final GameServicesMultiplayer multiplayer;
    private final Future<Void> future;
    private Room room;
    private ObjectMap<String, MultiplayerParticipant> all = new ObjectMap<String, MultiplayerParticipant>();
    private ObjectSet<MultiplayerParticipant> others = new ObjectSet<MultiplayerParticipant>();
    private MultiplayerParticipant me;
    private ObjectSet<IParticipant> publicOthers;
    private ObjectMap<String, IParticipant> publicAll;

    /**
     * Created on main thread!
     */
    public RoomController(final GameServicesMultiplayer multiplayer, final Future<Void> future) {
        this.multiplayer = multiplayer;
        this.future = future;
        session = new AndroidGameSession(multiplayer, future);
    }

    private String toString(Room room) {
        return room == null ? "null" : "{creatorId: " + room.getCreatorId() +
            ", description: " + room.getDescription() +
            ", roomId: " + room.getRoomId() +
            ", participantIds: " + room.getParticipantIds() +
            ", status: " + room.getStatus() +
            ", variant: " + room.getVariant() +
            "}";
    }

    @Override public void onRoomCreated(final int statusCode, final Room room) {
        this.room = room;
        Logger.log("room created, status ok: " + (statusCode == GamesStatusCodes.STATUS_OK));
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Gdx.app.postRunnable(new Runnable() {
                @Override public void run() {
                    die(false, Config.thesaurus.localize("disconnect-game-services-error"));
                }
            });
        } else {
            multiplayer.showWaitingRoom(room);
        }
    }
    @Override public void onJoinedRoom(final int statusCode, final Room room) {
        this.room = room;
        Logger.log("joined room, status ok: " + (statusCode == GamesStatusCodes.STATUS_OK));
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Gdx.app.postRunnable(new Runnable() {
                @Override public void run() {
                    die(false, Config.thesaurus.localize("disconnect-game-services-error"));
                }
            });
        } else {
            multiplayer.showWaitingRoom(room);
        }
    }


    @Override public void onLeftRoom(final int statusCode, final String roomId) {
        Logger.log("left room, status ok: " + (statusCode == GamesStatusCodes.STATUS_OK));
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                if (statusCode != GamesStatusCodes.STATUS_OK) {
                    die(false, Config.thesaurus.localize("disconnect-game-services-error"));
                } else {
                    session.disconnect(false);
                }
            }
        });
    }

    @Override public void onRoomConnected(final int statusCode, final Room room) {
        Logger.log("room connected, status ok: " + (statusCode == GamesStatusCodes.STATUS_OK));
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            die(false, Config.thesaurus.localize("disconnect-game-services-error"));
            return;
        }
        RoomController.this.room = room;
        session.setVariant(room.getVariant());
        String playerId = room.getParticipantId(Games.Players.getCurrentPlayerId(multiplayer.client));

        for (Participant participant : room.getParticipants()) {
            if (participant.getStatus() != Participant.STATUS_JOINED)
                continue;
            if (participant.getParticipantId().equals(playerId)) {
                me = new MultiplayerParticipant(participant);
            } else {
                others.add(new MultiplayerParticipant(participant));
            }
        }
        all.put(me.getId(), me);
        for (MultiplayerParticipant p : others) {
            all.put(p.getId(), p);
        }
        publicOthers = new ObjectSet<IParticipant>(others);
        publicAll = new ObjectMap<String, IParticipant>(all);

        multiplayer.updateInvites();
        Logger.log("  - room: " + RoomController.this.toString(room));
    }

    @Override public void onRealTimeMessageReceived(final RealTimeMessage message) {
        Logger.log("rtm received: " + message);
        Logger.log("  - senderId: " + message.getSenderParticipantId());
        final String str;
        try {
            str = new String(message.getMessageData(), "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            Gdx.app.postRunnable(new Runnable() {
                @Override public void run() {
                    session.disconnect(false, Config.thesaurus.localize("disconnect-utf-8"), e);
                }
            });
            return;
        }
        Logger.log("  - message: " + str);
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                session.receiveMessage(all.get(message.getSenderParticipantId()), str);
            }
        });
    }
    @Override public void onRoomConnecting(Room room) {}
    @Override public void onRoomAutoMatching(Room room) {}
    @Override public void onPeerInvitedToRoom(Room room, List<String> strings) {}
    @Override public void onPeerJoined(Room room, List<String> strings) {}
    @Override public void onConnectedToRoom(Room room) {}
    @Override public void onPeersConnected(Room room, List<String> strings) {}
    @Override public void onP2PConnected(String s) {}
    @Override public void onP2PDisconnected(String s) {
        die(true, Config.thesaurus.localize("disconnect-peer-left"));

    }
    @Override public void onPeerLeft(Room room, List<String> strings) {
        die(true, Config.thesaurus.localize("disconnect-peer-left"));

    }
    @Override public void onPeerDeclined(Room room, List<String> strings) {
        die(true, Config.thesaurus.localize("disconnect-peer-left"));

    }
    @Override public void onDisconnectedFromRoom(Room room) {
        if (this.room != null && room == this.room) {
            die(false, null);
        }
    }
    @Override public void onPeersDisconnected(Room room, List<String> strings) {
        die(true, Config.thesaurus.localize("disconnect-peer-left"));

    }
    private void die(final boolean otherPLayerLeft, final String reason) {
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                if (future != null && !future.isHappened()) future.happen();
                session.disconnect(otherPLayerLeft, reason);
            }
        });
    }

    private class AndroidGameSession extends GameSession {
        private final GameServicesMultiplayer multiplayer;
        private final Future<Void> future;

        public AndroidGameSession(GameServicesMultiplayer multiplayer, Future<Void> future) {
            this.multiplayer = multiplayer;
            this.future = future;
        }

        @Override public IParticipant getMe() { return me; }

        @Override public ObjectSet<IParticipant> getOthers() { return publicOthers; }

        @Override public ObjectMap<String, IParticipant> getAll() { return publicAll; }

        //called from ui thread thread
        @Override protected IFuture<Boolean> doSendToWithCallback(final IParticipant participant, final String message) {
            Logger.debug("send to " + participant.getDisplayedName() + " with callback: " + message);
            if (room == null) {
                return Future.completed(false);
            }
            final byte[] bytes;
            try {
                bytes = message.getBytes("UTF-8");
            } catch (final UnsupportedEncodingException e) {
                disconnect(false, Config.thesaurus.localize("disconnect-utf-8"), e);
                return Future.completed(false);
            }
            final Future<Boolean> future = new Future<Boolean>();
            multiplayer.activity.getMainHandler().post(new Runnable() {
                @Override public void run() {
                    if (room == null) {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override public void run() {
                                future.happen(false);
                            }
                        });
                        return;
                    }
                    try {
                        Games.RealTimeMultiplayer.sendReliableMessage(multiplayer.client, new RealTimeMultiplayer.ReliableMessageSentCallback() {
                            @Override public void onRealTimeMessageSent(final int statusCode, int i2, String s) {
                                Gdx.app.postRunnable(new Runnable() {
                                    @Override public void run() {
                                        future.happen(statusCode == GamesStatusCodes.STATUS_OK);
                                    }
                                });
                            }
                        }, bytes, room.getRoomId(), participant.getId());
                    } catch (final Exception e) {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override public void run() {
                                disconnect(false, Config.thesaurus.localize("disconnect-error-on-send"), e);
                            }
                        });
                    }

                }
            });

            return future;
        }

        /**
         * called from ui thread
         */
        @Override protected void doSendTo(final IParticipant participant, final String message) {
            Logger.debug("send to " + participant.getDisplayedName() + ": " + message);
            if (room == null)
                return;
            final byte[] bytes;
            try {
                bytes = message.getBytes("UTF-8");
            } catch (final UnsupportedEncodingException e) {
                disconnect(false, Config.thesaurus.localize("disconnect-utf-8"), e);
                return;
            }
            multiplayer.activity.getMainHandler().post(new Runnable() {
                @Override public void run() {
                    if (room == null) return;
                    try {
                        Games.RealTimeMultiplayer.sendReliableMessage(multiplayer.client, null, bytes, room.getRoomId(), participant.getId());
                    } catch (final Exception e) {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override public void run() {
                                disconnect(false, Config.thesaurus.localize("disconnect-error-on-send"), e);
                            }
                        });
                    }
                }
            });
        }

        //called from ui thread
        @Override protected void onDisconnected() {
            multiplayer.activity.getMainHandler().post(new Runnable() {
                @Override public void run() {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override public void run() {
                            if (future != null && !future.isHappened()) future.happen();
                        }
                    });
                    if (room == null) {
                        return;
                    }
                    try {
                        if (multiplayer.client.isConnected()) {
                            Games.RealTimeMultiplayer.leave(multiplayer.client, RoomController.this, room.getRoomId());
                        }
                    } catch (Exception ignored) {

                    }

                    room = null;
                }
            });
        }
    }
}
