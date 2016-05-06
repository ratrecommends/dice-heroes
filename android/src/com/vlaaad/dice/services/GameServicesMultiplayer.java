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

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pools;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.multiplayer.*;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.vlaaad.common.util.*;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.MainActivity;
import com.vlaaad.dice.R;
import com.vlaaad.dice.api.services.multiplayer.GameSession;
import com.vlaaad.dice.api.services.multiplayer.IMultiplayer;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.config.thesaurus.ThesaurusData;

import java.util.ArrayList;


/**
 * Created 24.07.14 by vlaaad
 */
public class GameServicesMultiplayer implements IMultiplayer, OnInvitationReceivedListener {
    private static final int REQUEST_SELECT_FRIENDS = 108032;
    private static final int REQUEST_INVITATION_INBOX = 108033;
    private static final int REQUEST_WAITING_ROOM = 108034;


    public final MainActivity activity;
    public final GoogleApiClient client;
    private final ObjectSet<String> invites = new ObjectSet<String>();
    private final StateDispatcher<Integer> invitesDispatcher = new StateDispatcher<Integer>(0);
    private final Invitation initialInvitation;
    private Future<Void> future;
    private RoomController controller;
    private final StateDispatcher<Option<GameSession>> currentSession = new StateDispatcher<Option<GameSession>>(Option.<GameSession>none());
    private int variant;

    public GameServicesMultiplayer(MainActivity activity, GoogleApiClient client, Invitation invitation) {
        this.activity = activity;
        this.client = client;
        if (invitation != null) {
            initialInvitation = invitation;
            joinRoom(invitation);
        } else {
            initialInvitation = null;
        }
    }

    @Override public IStateDispatcher<Integer> invites() {
        return invitesDispatcher;
    }

    @Override public IStateDispatcher<Option<GameSession>> currentSession() {
        return currentSession;
    }

    @Override public IFuture<Void> quickMatch(final int playersToInvite, final int variant) {
        if (future != null && !future.isHappened())
            throw new IllegalStateException("called invite players while future of previous invite didn't happened!");
        future = new Future<Void>();
        this.variant = variant;
        activity.getMainHandler().post(new Runnable() {
            @Override public void run() {
                controller = createController();
                RoomConfig config = RoomConfig.builder(controller)
                    .setAutoMatchCriteria(RoomConfig.createAutoMatchCriteria(playersToInvite, playersToInvite, 0))
                    .setRoomStatusUpdateListener(controller)
                    .setMessageReceivedListener(controller)
                    .setVariant(variant)
                    .build();
                Games.RealTimeMultiplayer.create(client, config);
            }
        });
        return future;
    }

    /**
     * you must block game until future happens (do not call invite friends and display invitations until)
     * called from ui thread
     */
    @Override public synchronized IFuture<Void> inviteFriends(final int playersToInvite, int variant) {
        if (future != null && !future.isHappened())
            throw new IllegalStateException("called invite players while future of previous invite didn't happened!");
        future = new Future<Void>();
        this.variant = variant;
        activity.getMainHandler().post(new Runnable() {
            @Override public void run() {
                Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(client, playersToInvite, playersToInvite, false);
                activity.startActivityForResult(intent, REQUEST_SELECT_FRIENDS);
            }
        });
        return future;
    }


    /**
     * you must block game until future happens (do not call invite friends and display invitations until)
     * called from ui thread!
     */
    @Override public synchronized IFuture<Void> displayInvitations() {
        if (future != null && !future.isHappened())
            throw new IllegalStateException("called show invitations while future of previous show invitations call didn't happened!");
        future = new Future<Void>();
        activity.getMainHandler().post(new Runnable() {
            @Override public void run() {
                Intent intent = Games.Invitations.getInvitationInboxIntent(client);
                activity.startActivityForResult(intent, REQUEST_INVITATION_INBOX);
            }
        });
        return future;
    }

    //on main thread
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_FRIENDS:
                onFriendsSelected(resultCode, data);
                break;
            case REQUEST_INVITATION_INBOX:
                onInbox(resultCode, data);
                break;
            case REQUEST_WAITING_ROOM:
                onWaitingRoom(resultCode);
                break;
        }
    }

    /**
     * on main thread
     */
    private void onFriendsSelected(int resultCode, Intent data) {
        if (resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
            activity.getGameServicesHelper().onSignedOutFromOutside();
            if (future != null) {
                Gdx.app.postRunnable(future);
                future = null;
            }
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            if (future != null) {
                Gdx.app.postRunnable(future);
                future = null;
            }
            return;
        }
        if (controller != null) {
            throw new IllegalStateException("tried to create new game, but there was already another!");
        }
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        controller = createController();
        Games.RealTimeMultiplayer.create(
            client,
            RoomConfig.builder(controller)
                .addPlayersToInvite(invitees)
                .setRoomStatusUpdateListener(controller)
                .setMessageReceivedListener(controller)
                .setVariant(variant)
                .build()
        );

    }

    /**
     * on main thread
     */
    private void onInbox(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            if (data != null && data.hasExtra(Multiplayer.EXTRA_INVITATION)) {
                Invitation invitation = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);
                onInvitationRemoved(invitation.getInvitationId());
            }
            if (future != null) {
                future.happen();
                future = null;
            }
            updateInvites();
            return;
        }
        final RoomController c = controller;
        if (c != null) {
            throw new IllegalStateException("tried to create new game, but there was already another!");
        }

        Invitation invitation = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);
        joinRoom(invitation);
        updateInvites();
    }

    /**
     * on main thread
     */
    private void joinRoom(final Invitation invitation) {
//        onInvitationRemoved(invitation.getInvitationId());
        controller = createController();
        Games.RealTimeMultiplayer.join(
            client,
            RoomConfig.builder(controller)
                .setRoomStatusUpdateListener(controller)
                .setMessageReceivedListener(controller)
                .setVariant(invitation.getVariant())
                .setInvitationIdToAccept(invitation.getInvitationId())
                .build()
        );
    }

    /**
     * on main thread
     */
    private void onWaitingRoom(final int resultCode) {
        if (resultCode != Activity.RESULT_OK) {
            Logger.log("waiting room result != ok, leaving room");
            leaveRoomIfExists(null, Option.<Throwable>none());
        } else {
            setSession(controller.session);
        }
        if (future != null && !future.isHappened()) {
            Gdx.app.postRunnable(future);
        }
        future = null;
    }

    /**
     * on main thread
     */
    public void updateInvites() {
        Games.Invitations.loadInvitations(client).setResultCallback(new ResultCallback<Invitations.LoadInvitationsResult>() {
            @Override public void onResult(final Invitations.LoadInvitationsResult result) {
                loadInvitations(result.getInvitations());
            }
        });
    }

    /**
     * on main thread
     */
    private void setSession(final GameSession session) {
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                Option<GameSession> option = currentSession.getState();
                if (option.isDefined()) {
                    final GameSession previous = option.get();
                    if (previous == session) {
                        return;
                    }
                    previous.disconnect(false);
                }
                currentSession.setState(Option.some(session));
            }
        });
    }

    /**
     * on main thread
     */
    private RoomController createController() {
        final RoomController result = new RoomController(this, future);
        result.session.disconnectFuture().addListener(new IFutureListener<Tuple3<Boolean, Option<String>, Option<Throwable>>>() {
            //happens on ui thread
            @Override public void onHappened(Tuple3<Boolean, Option<String>, Option<Throwable>> aVoid) {
                if (currentSession.getState().isDefined() && currentSession.getState().get() != result.session) {
                    Logger.error("Disconnected from wrong session :(");
                }
                currentSession.setState(Option.<GameSession>none());
                activity.getMainHandler().post(new Runnable() {
                    @Override public void run() {
                        if (controller == result) {
                            controller = null;
                        }
                    }
                });
            }
        });
        return result;
    }

    /**
     * on main thread
     */
    @Override public void onInvitationReceived(final Invitation invitation) {
        if (initialInvitation != null && initialInvitation.getInvitationId().equals(invitation.getInvitationId()))
            return;
        boolean shouldShowInvite = invites.add(invitation.getInvitationId());
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                invitesDispatcher.setState(invites.size);
            }
        });
        if (shouldShowInvite) {
            showInvitation(invitation);
        }
    }

    //on main thread
    @Override public void onInvitationRemoved(String invitationId) {
        invites.remove(invitationId);
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                invitesDispatcher.setState(invites.size);
            }
        });
        Logger.debug("invitations updated: " + invites);
    }

    private void showInvitation(final Invitation invitation) {
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                Thesaurus thesaurus;
                if (Config.thesaurus != null) {
                    thesaurus = Config.thesaurus;
                } else {
                    // can happen if invitation received invite outside of game, invite is here before config is loaded
                    ObjectMap<String, ThesaurusData> data = new ObjectMap<String, ThesaurusData>();
                    data.put("multiplayer-invitation", new ThesaurusData(
                        "multiplayer-invitation",
                        "{user} invites you to play!",
                        "{user} приглашает сыграть!"
                    ));
                    thesaurus = new Thesaurus(data);
                }

                final String text = thesaurus.localize(
                    "multiplayer-invitation",
                    Thesaurus.params()
                        .with("user", invitation.getInviter().getDisplayName())
                );

                activity.getMainHandler().post(new Runnable() {
                    @Override public void run() {
                        final Dialog dialog = new Dialog(activity);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.invite);
                        dialog.show();
                        ((TextView) dialog.findViewById(R.id.invite_text)).setText(text);
                        dialog.findViewById(R.id.view_invitation).setOnClickListener(new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                dialog.dismiss();
                                Gdx.app.postRunnable(new Runnable() {
                                    @Override public void run() {
                                        displayInvitations();
                                    }
                                });
                            }
                        });

                    }
                });
            }
        });
    }

    //on main thread
    public void showWaitingRoom(final Room room) {
        Intent intent = Games.RealTimeMultiplayer.getWaitingRoomIntent(client, room, Integer.MAX_VALUE);
        activity.startActivityForResult(intent, REQUEST_WAITING_ROOM);
    }

    //main thread
    public void leaveRoomIfExists(final String reason, final Option<Throwable> throwableOption) {
        final RoomController c = controller;
        if (c != null) {
            Gdx.app.postRunnable(new Runnable() {
                @Override public void run() {
                    c.session.disconnect(false, reason, throwableOption);
                }
            });
        }
        controller = null;
    }

    @SuppressWarnings("unchecked")
    public void loadInvitations(InvitationBuffer invitations) {
        ObjectSet<String> tmp = Pools.obtain(ObjectSet.class);
        tmp.addAll(invites);
        invites.clear();
        for (Invitation invitation : invitations) {
            invites.add(invitation.getInvitationId());
            if (!tmp.contains(invitation.getInvitationId())) {
                showInvitation(invitation);
            }
        }
        tmp.clear();
        Pools.free(tmp);
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                invitesDispatcher.setState(invites.size);
            }
        });
    }
}
