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

/**
 * Created 10.07.14 by vlaaad
 */
public interface TutorialProvider {

    TutorialProvider NO_TUTORIALS = new TutorialProvider() {
        @Override public boolean isInitiativeTutorialCompleted() { return true; }
        @Override public void completeInitiativeTutorial() {}
        @Override public boolean isProfessionAbilitiesTutorialCompleted() { return true; }
        @Override public void completeProfessionAbilitiesTutorial() {}
        @Override public boolean isPlayPotionsTutorialCompleted() { return true; }
        @Override public void completePlayPotionsTutorial() {}
        @Override public boolean isSpawnSwipeTutorialCompleted() { return true; }
        @Override public void completeSpawnSwipeTutorial() {}
        @Override public boolean isBossTutorialCompleted() { return true; }
        @Override public void completeBossTutorial() {}
    };

    boolean isInitiativeTutorialCompleted();
    void completeInitiativeTutorial();
    boolean isProfessionAbilitiesTutorialCompleted();
    void completeProfessionAbilitiesTutorial();
    boolean isPlayPotionsTutorialCompleted();
    void completePlayPotionsTutorial();
    boolean isSpawnSwipeTutorialCompleted();
    void completeSpawnSwipeTutorial();
    boolean isBossTutorialCompleted();
    void completeBossTutorial();
}
