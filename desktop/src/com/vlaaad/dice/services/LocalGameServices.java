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

import com.badlogic.gdx.backends.lwjgl.LwjglPreferences;
import com.vlaaad.common.util.IStateDispatcher;
import com.vlaaad.common.util.StateDispatcher;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.api.services.cloud.ICloudSave;
import com.vlaaad.dice.api.services.achievements.IGameAchievements;
import com.vlaaad.dice.api.services.IGameServices;
import com.vlaaad.dice.ServicesState;
import com.vlaaad.dice.api.services.multiplayer.IMultiplayer;
import com.vlaaad.dice.ui.windows.MessageWindow;

/**
 * Created 18.05.14 by vlaaad
 */
public class LocalGameServices implements IGameServices {

    public static final String KEY = "signed-in";
    private final LwjglPreferences prefs;
    private IGameAchievements achievements = new LocalAchievements();
    private final StateDispatcher<ServicesState> dispatcher = new StateDispatcher<ServicesState>(ServicesState.DISCONNECTED);
    private ICloudSave cloudSave = new LocalCloudSave();
    private IMultiplayer multiplayer = new LocalMultiplayer();

    public LocalGameServices() {
        prefs = new LwjglPreferences("dice.local.game-services", ".prefs/");
        dispatcher.setState(ServicesState.valueOf(prefs.getString(KEY, ServicesState.DISCONNECTED.toString())));
    }

    @Override public void signIn() {
        signedIn(ServicesState.CONNECTED);
    }

    @Override public void signOut() {
        signedIn(ServicesState.DISCONNECTED);
    }

    private void signedIn(ServicesState state) {
        if (dispatcher.setState(state)) {
            prefs.putString(KEY, state.toString());
            prefs.flush();
        }
    }

    @Override public boolean isSignedIn() {
        return dispatcher.getState() == ServicesState.CONNECTED;
    }

    @Override public IStateDispatcher<ServicesState> dispatcher() {
        return dispatcher;
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

    @Override public void showLeaderboard(String leaderboardId) {
        new MessageWindow().show("leaderboard!");
    }

    @Override public IFuture<Boolean> incrementScore(String leaderboardId, int by) {
        return Future.completed();
    }
}
