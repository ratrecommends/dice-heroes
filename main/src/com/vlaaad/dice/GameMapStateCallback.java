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

import com.vlaaad.common.util.IStateDispatcher;
import com.vlaaad.common.util.Logger;
import com.vlaaad.dice.game.config.levels.BaseLevelDescription;
import com.vlaaad.dice.game.config.levels.LevelDescription;
import com.vlaaad.dice.game.config.purchases.PurchaseInfo;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.states.GameMapState;

import java.util.Map;

/**
 * Created 20.05.14 by vlaaad
 */
public class GameMapStateCallback implements GameMapState.Callback {
    private final DiceHeroes diceHeroes;
    private final ConflictResolver cloudResolver;

    public GameMapStateCallback(DiceHeroes diceHeroes, ConflictResolver cloudResolver) {
        this.diceHeroes = diceHeroes;
        this.cloudResolver = cloudResolver;
    }

    @Override public void onStartLevel(BaseLevelDescription level) {
        diceHeroes.play(level);
    }
    @Override public void onBuy(PurchaseInfo info) {
        Config.mobileApi.purchase(info);
    }

    @Override public IStateDispatcher<ConflictResolver.ResolverState> dispatcher() {
        return cloudResolver.dispatcher();
    }

    @SuppressWarnings("unchecked")
    @Override public void resolveConflictingState(GameMapState.ConflictResolution resolution) {
        switch (resolution) {
            case useLocal:
                Logger.debug("resolve: use local");
                cloudResolver.resolve(true);
                break;
            case useServer:
                Logger.debug("resolve: use server");
                Map result = getConflictServerData();
                result.put("uuid", diceHeroes.userData.uuid());
                cloudResolver.resolve(false);
                diceHeroes.applyNewUserData(UserData.deserialize(result));
                break;
            default:
                throw new IllegalStateException("unknown resolution: " + resolution);
        }
    }

    @Override public Map getConflictServerData() {
        return cloudResolver.getServerData();
    }

}
