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
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.util.Logger;
import com.vlaaad.common.util.Option;
import com.vlaaad.common.util.Tuple2;
import com.vlaaad.common.util.Tuple3;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.api.services.multiplayer.GameSession;
import com.vlaaad.dice.api.services.multiplayer.IParticipant;
import com.vlaaad.dice.game.config.levels.BaseLevelDescription;
import com.vlaaad.dice.game.config.rewards.Reward;
import com.vlaaad.dice.game.config.rewards.results.RewardResult;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.world.LevelResult;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.controllers.RoundController;
import com.vlaaad.dice.game.world.players.Fraction;
import com.vlaaad.dice.game.world.players.Player;
import com.vlaaad.dice.game.world.players.PlayerRelation;
import com.vlaaad.dice.states.PvpPlayState;
import com.vlaaad.dice.ui.windows.DisconnectedWindow;
import com.vlaaad.dice.ui.windows.LoseWindow;
import com.vlaaad.dice.ui.windows.PvpWinWindow;

/**
 * Created 28.07.14 by vlaaad
 */
public class PvpPlayStateCallback implements PvpPlayState.Callback {

    private final DiceHeroes app;
    private PvpWinWindow winWindow;
    private LoseWindow loseWindow;

    public PvpPlayStateCallback(DiceHeroes app) {
        this.app = app;
    }

    @Override public void onWin(BaseLevelDescription level, LevelResult result, ObjectMap<Player, IParticipant> playersToParticipants, final PvpPlayState.RestartCallback callback) {
        Array<RewardResult> rewards = app.applyLevelResult(level, result, true);
        Array<IParticipant> opponents = new Array<IParticipant>();
        for (ObjectMap.Entry<Player, IParticipant> e : playersToParticipants.entries()) {
            if (e.key.inRelation(result.viewer, PlayerRelation.enemy)) {
                opponents.add(e.value);
            }
        }
        String shareText = Config.thesaurus.localize(
            "pvp-share",
            Thesaurus.params()
                .with("opponents", Thesaurus.Util.enumerate(Config.thesaurus, opponents, IParticipant.STRINGIFIER))
                .with("pvp-cant-stop-me", opponents.size > 1 ? "pvp-cant-stop-me.many" : "pvp-cant-stop-me.one")
        );
        Config.mobileApi.services().incrementScore("CgkIsNnQ2ZcKEAIQFw", 1).addListener(new IFutureListener<Boolean>() {
            @Override public void onHappened(Boolean success) {
                Logger.debug("todo");
            }
        });
        winWindow = new PvpWinWindow();
        winWindow.show(new PvpWinWindow.Params(rewards, shareText, result, new PvpWinWindow.Callback() {

            @Override public void onClose() {
                winWindow = null;
                app.setState(app.gameMapState);
            }

            @Override public void onRestart() {
                winWindow = null;
                callback.onRestart();
            }
        }, app.userData));
    }

    @Override public void onLose(BaseLevelDescription level, LevelResult result, final PvpPlayState.RestartCallback callback) {
        app.applyLevelResult(level, result, false);
        loseWindow = new LoseWindow();
        loseWindow.show(new LoseWindow.Params(result, new LoseWindow.Callback() {
            @Override public void onClose() {
                loseWindow = null;
                app.setState(app.gameMapState);
            }

            @Override public void onRestart() {
                loseWindow = null;
                callback.onRestart();
            }
        }));
    }

    @Override public void onCancel(GameSession session, Tuple3<Boolean, Option<String>, Option<Throwable>> reason, Option<World> world, Option<ObjectMap<Player, IParticipant>> participants) {
        Logger.log("disconnect because cancelled game");
        session.disconnect(false);
        app.save();
        if (loseWindow != null) {
            loseWindow.disableRestart();
        } else if (winWindow != null) {
            winWindow.disableRestart();
        } else {
            if (reason._1 && world.isDefined() && participants.isDefined()) {
                walkOverVictory(session, world.get(), participants.get());
            } else {
                app.setState(app.gameMapState);
                if (reason._2.isDefined() || reason._3.isDefined()) {
                    new DisconnectedWindow().show(reason);
                }
            }
        }
    }

    private void walkOverVictory(GameSession session, World world, ObjectMap<Player, IParticipant> participants) {
        onWin(session.getMode().metaLevel, RoundController.createResult(world), participants, new PvpPlayState.RestartCallback() {
            @Override public void onRestart() {
                throw new IllegalStateException("restarts not allowed");
            }
        });
        winWindow.disableRestart();
    }
}
