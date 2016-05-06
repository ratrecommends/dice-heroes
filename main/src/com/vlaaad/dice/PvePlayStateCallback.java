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

package com.vlaaad.dice;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.vlaaad.dice.achievements.events.EventType;
import com.vlaaad.dice.achievements.events.imp.EarnEvent;
import com.vlaaad.dice.achievements.events.imp.EndLevelEvent;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.config.levels.LevelDescription;
import com.vlaaad.dice.game.config.rewards.Reward;
import com.vlaaad.dice.game.config.rewards.results.RewardResult;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.world.LevelResult;
import com.vlaaad.dice.states.PvePlayState;
import com.vlaaad.dice.ui.windows.LoseWindow;
import com.vlaaad.dice.ui.windows.RewardWindow;

/**
 * Created 20.05.14 by vlaaad
 */
public class PvePlayStateCallback implements PvePlayState.Callback {

    private final LevelDescription level;
    private final DiceHeroes diceHeroes;

    public PvePlayStateCallback(DiceHeroes diceHeroes, LevelDescription level) {
        this.diceHeroes = diceHeroes;
        this.level = level;
    }

    @Override public void onWin(LevelResult levelResult) {
        Array<RewardResult> rewards = diceHeroes.applyLevelResult(level, levelResult, true);
        String shareKey = "share-level-" + level.name;
        if (!Config.thesaurus.keyExists(shareKey))
            shareKey = "share-level-default";
        new RewardWindow().show(new RewardWindow.Params(
            rewards,
            Config.thesaurus.localize(shareKey, Thesaurus.params().with("level", String.valueOf(level.getLevelNumber()))),
            levelResult,
            new RewardWindow.Callback() {
                @Override public void onClose() {
                    diceHeroes.setState(diceHeroes.gameMapState);
                }
            },
            diceHeroes.userData
        ));
    }

    @Override public void onLose(LevelResult levelResult) {
        diceHeroes.applyLevelResult(level, levelResult, false);
        new LoseWindow().show(new LoseWindow.Params(levelResult, new LoseWindow.Callback() {
            @Override public void onRestart() {
                diceHeroes.play(level);
            }

            @Override public void onClose() {
                diceHeroes.setState(diceHeroes.gameMapState);
            }
        }));
    }

    @Override public void onCancel() {
        diceHeroes.setState(diceHeroes.gameMapState);
    }

    @Override public void onRestart() {
        diceHeroes.play(level);
    }
}
