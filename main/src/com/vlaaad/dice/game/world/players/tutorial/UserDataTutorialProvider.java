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

package com.vlaaad.dice.game.world.players.tutorial;

import com.vlaaad.dice.game.user.UserData;

/**
 * Created 10.07.14 by vlaaad
 */
public class UserDataTutorialProvider implements TutorialProvider {
    private final UserData userData;

    public UserDataTutorialProvider(UserData userData) {
        this.userData = userData;
    }

    @Override public boolean isInitiativeTutorialCompleted() {
        return userData.initiativeTutorialCompleted;
    }
    @Override public void completeInitiativeTutorial() {
        userData.initiativeTutorialCompleted = true;
    }
    @Override public boolean isProfessionAbilitiesTutorialCompleted() {
        return userData.professionAbilitiesTutorialCompleted;
    }
    @Override public void completeProfessionAbilitiesTutorial() {
        userData.professionAbilitiesTutorialCompleted = true;
    }
    @Override public boolean isPlayPotionsTutorialCompleted() {
        return userData.playPotionsTutorialCompleted;
    }
    @Override public void completePlayPotionsTutorial() {
        userData.playPotionsTutorialCompleted = true;
    }
    @Override public boolean isSpawnSwipeTutorialCompleted() { return userData.spawnSwipeTutorialCompleted; }
    @Override public void completeSpawnSwipeTutorial() { userData.spawnSwipeTutorialCompleted = true; }
    @Override public boolean isBossTutorialCompleted() {
        return userData.bossTutorialCompleted;
    }
    @Override public void completeBossTutorial() {
        userData.bossTutorialCompleted = true;
    }
}
