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

import android.content.Intent;
import com.badlogic.gdx.Gdx;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.games.leaderboard.Leaderboards.LoadPlayerScoreResult;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Invitations;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.vlaaad.common.util.IStateDispatcher;
import com.vlaaad.common.util.Option;
import com.vlaaad.common.util.StateDispatcher;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.MainActivity;
import com.vlaaad.dice.ServicesState;
import com.vlaaad.dice.api.services.IGameServices;
import com.vlaaad.dice.api.services.achievements.IGameAchievements;
import com.vlaaad.dice.api.services.cloud.ICloudSave;
import com.vlaaad.dice.api.services.multiplayer.IMultiplayer;
import com.vlaaad.dice.util.GameHelper;

/**
 * Created 18.05.14 by vlaaad
 */
public class GameServicesHelper implements IGameServices, GameHelper.GameHelperListener {

    private static final int LEADERBOARD_REQUEST_CODE = 9762340;

    private final GameHelper helper;
    private final MainActivity activity;
    private GoogleAchievements achievements;
    private StateDispatcher<ServicesState> dispatcher = new StateDispatcher<ServicesState>(ServicesState.DISCONNECTED);
    private ICloudSave cloudSave;
    private GameServicesMultiplayer multiplayer;
    private boolean invitationListenerRegistered;

    public GameServicesHelper(MainActivity activity) {
        this.activity = activity;
        helper = new GameHelper(activity, GameHelper.CLIENT_GAMES | GameHelper.CLIENT_SNAPSHOT);
        helper.setMaxAutoSignInAttempts(0);
        helper.setup(this);
    }

    @Override public boolean isSignedIn() {
        return helper.isSignedIn();
    }

    @Override public IStateDispatcher<ServicesState> dispatcher() {
        return dispatcher;
    }

    @Override public void signIn() {
        activity.getMainHandler().post(new Runnable() {
            @Override public void run() {
                helper.beginUserInitiatedSignIn();
            }
        });
    }

    @Override public void signOut() {
        activity.getMainHandler().post(new Runnable() {
            @Override public void run() {
                helper.signOut();
            }
        });
    }

    @Override public IGameAchievements gameAchievements() {
        return isSignedIn() ? achievements : null;
    }

    @Override public ICloudSave cloudSave() {
        return isSignedIn() ? cloudSave : null;
    }

    @Override public IMultiplayer multiplayer() {
        return isSignedIn() ? multiplayer : null;
    }

    @Override public void showLeaderboard(final String leaderboardId) {
        activity.getMainHandler().post(new Runnable() {
            @Override public void run() {
                Intent intent = Games.Leaderboards.getLeaderboardIntent(
                    helper.getApiClient(),
                    leaderboardId,
                    LeaderboardVariant.TIME_SPAN_WEEKLY
                );
                activity.startActivityForResult(intent, LEADERBOARD_REQUEST_CODE);
            }
        });
    }

    @Override public IFuture<Boolean> incrementScore(final String leaderboardId, final int by) {
        final Future<Boolean> future = Future.make();
        activity.getMainHandler().post(new Runnable() {
            @Override public void run() {
                Games.Leaderboards
                    .loadCurrentPlayerLeaderboardScore(
                        helper.getApiClient(),
                        leaderboardId,
                        LeaderboardVariant.TIME_SPAN_WEEKLY,
                        LeaderboardVariant.COLLECTION_PUBLIC
                    )
                    .setResultCallback(new ResultCallback<LoadPlayerScoreResult>() {
                        @Override public void onResult(LoadPlayerScoreResult result) {
                            processLoadScoreResult(result, leaderboardId, by, future);
                        }
                    });
            }
        });
        return future;
    }

    private void processLoadScoreResult(LoadPlayerScoreResult result, String leaderboardId, int by, final Future<Boolean> future) {
        if (result.getStatus().isSuccess()) {
            LeaderboardScore score = result.getScore();
            long current = score == null ? 0 : score.getRawScore();
            Games.Leaderboards
                .submitScoreImmediate(
                    helper.getApiClient(),
                    leaderboardId,
                    current + by
                )
                .setResultCallback(new ResultCallback<Leaderboards.SubmitScoreResult>() {
                    @Override public void onResult(Leaderboards.SubmitScoreResult submitResult) {
                        final boolean success = submitResult.getStatus().isSuccess();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override public void run() {
                                future.happen(success);
                            }
                        });
                    }
                });
        } else {
            Gdx.app.postRunnable(new Runnable() {
                @Override public void run() {
                    future.happen(false);
                }
            });
        }
    }

    public void onStart(MainActivity activity) {
        helper.onStart(activity);
    }

    public void onStop() {
        if (multiplayer != null) {
            multiplayer.leaveRoomIfExists(null, Option.<Throwable>none());
        }
        helper.onStop();
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                dispatcher.setState(ServicesState.DISCONNECTED);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        helper.onActivityResult(requestCode, resultCode, data);
        if (multiplayer != null) {
            multiplayer.onActivityResult(requestCode, resultCode, data);
        }
        if ((requestCode == GoogleAchievements.ACHIEVEMENTS_REQUEST_CODE
            || requestCode == LEADERBOARD_REQUEST_CODE)
            && resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
            onSignedOutFromOutside();
        }
    }

    @Override public void onSignInFailed() {
        if (multiplayer != null && invitationListenerRegistered) {
            try {
                Games.Invitations.unregisterInvitationListener(helper.getApiClient());
            } catch (Exception ignored) {
            }
            invitationListenerRegistered = false;
        }
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                dispatcher.setState(ServicesState.DISCONNECTED);
            }
        });
    }

    @Override public void onSignInSucceeded() {
        achievements = new GoogleAchievements(activity, helper.getApiClient());
        cloudSave = new GoogleCloudSave(activity, helper.getApiClient());
        multiplayer = new GameServicesMultiplayer(activity, helper.getApiClient(), helper.getInvitation());
        Games.Invitations
            .loadInvitations(helper.getApiClient())
            .setResultCallback(new ResultCallback<Invitations.LoadInvitationsResult>() {
                @Override public void onResult(final Invitations.LoadInvitationsResult result) {
                    if (multiplayer == null) return;
                    multiplayer.loadInvitations(result.getInvitations());
                }
            });
        invitationListenerRegistered = true;
        Games.Invitations.registerInvitationListener(helper.getApiClient(), new OnInvitationReceivedListener() {
            @Override public void onInvitationReceived(final Invitation invitation) {
                if (multiplayer == null) return;
                multiplayer.onInvitationReceived(invitation);
            }

            @Override public void onInvitationRemoved(final String s) {
                if (multiplayer == null) return;
                multiplayer.onInvitationRemoved(s);
            }
        });
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                dispatcher.setState(ServicesState.CONNECTED);
            }
        });
    }

    public void onSignedOutFromOutside() {
        onSignInFailed();
        helper.disconnect();
    }
}
