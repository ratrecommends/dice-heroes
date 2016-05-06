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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.vlaaad.dice.MainActivity;
import com.vlaaad.dice.achievements.Achievement;
import com.vlaaad.dice.api.services.achievements.IGameAchievements;

/**
 * Created 18.05.14 by vlaaad
 */
public class GoogleAchievements implements IGameAchievements {

    public static final int ACHIEVEMENTS_REQUEST_CODE = 666;
    private final MainActivity activity;
    private final GoogleApiClient client;

    public GoogleAchievements(MainActivity activity, GoogleApiClient client) {
        this.activity = activity;
        this.client = client;
    }

    @Override public void start(final Iterable<Achievement> revealed) {
        activity.getMainHandler().post(new Runnable() {
            @Override public void run() {
                for (Achievement achievement : revealed)
                    if (achievement.isUnlocked()) unlock(achievement);
            }
        });
    }

    @Override public void unlock(final Achievement achievement) {
        activity.getMainHandler().post(new Runnable() {
            @Override public void run() {
                Games.Achievements.unlock(client, achievement.id);
            }
        });
    }

    @Override public void showAchievements() {
        activity.getMainHandler().post(new Runnable() {
            @Override public void run() {
                activity.startActivityForResult(
                    Games.Achievements.getAchievementsIntent(client),
                    ACHIEVEMENTS_REQUEST_CODE
                );
            }
        });
    }

    @Override public void setCount(final Achievement achievement, final int count) {
        activity.getMainHandler().post(new Runnable() {
            @Override public void run() {
                Games.Achievements.setSteps(client, achievement.id, count);
            }
        });
    }
}
