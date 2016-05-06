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

package com.vlaaad.dice.api.services;

import com.vlaaad.common.util.IStateDispatcher;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.ServicesState;
import com.vlaaad.dice.api.services.cloud.ICloudSave;
import com.vlaaad.dice.api.services.achievements.IGameAchievements;
import com.vlaaad.dice.api.services.multiplayer.IMultiplayer;

/**
 * Created 17.05.14 by vlaaad
 */
public interface IGameServices {

    public void signIn();

    public void signOut();

    public boolean isSignedIn();

    public IStateDispatcher<ServicesState> dispatcher();

    /**
     * returns null when not signed in
     */
    public IGameAchievements gameAchievements();

    public ICloudSave cloudSave();

    public IMultiplayer multiplayer();

    void showLeaderboard(String leaderboardId);

    IFuture<Boolean> incrementScore(String leaderboardId, int by);
}
