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

package com.vlaaad.dice.game.world.behaviours.processors.ai;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.professions.ProfessionDescription;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.behaviours.RequestProcessor;
import com.vlaaad.dice.game.world.behaviours.params.TurnParams;
import com.vlaaad.dice.game.world.behaviours.responces.TurnResponse;
import com.vlaaad.dice.game.world.view.ViewScroller;

/**
 * Created 12.02.14 by vlaaad
 */
public abstract class AiDefaultProfessionAbilityProcessor extends RequestProcessor<TurnResponse, TurnParams> {

    private final Ability ability;
    protected final ProfessionDescription profession;

    public AiDefaultProfessionAbilityProcessor(String professionName, String abilityName) {
        profession = Config.professions.get(professionName);
        Array<Ability> abilities = profession.getAvailableAbilities(profession.getMaxLevel());
        for (Ability ability : abilities) {
            if (ability.name.equals(abilityName)) {
                this.ability = ability;
                return;
            }
        }
        ability = null;
    }

    @Override public int preProcess(TurnParams params) {
        if (params.creature.profession != profession)
            return -1;
        Array<Ability> abilities = profession.getAvailableAbilities(params.creature.getCurrentLevel());
        if (!abilities.contains(ability, true))
            return -1;
        if (!ability.action.canBeApplied(params.creature))
            return -1;
        return preProcess(params.creature, ability);
    }

    @Override public IFuture<TurnResponse> process(TurnParams params) {
        final Future<TurnResponse> future = new Future<TurnResponse>();
        params.creature.world.stage.addAction(Actions.delay(ViewScroller.CENTER_ON_TIME, Actions.run(new Runnable() {
            @Override public void run() {
                future.happen(new TurnResponse<Ability>(TurnResponse.TurnAction.PROFESSION_ABILITY, ability));
            }
        })));
        return future;
    }

    protected abstract int preProcess(Creature creature, Ability ability);
}
