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

package com.vlaaad.dice.game.actions.imp;

import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.GiveExpResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;

/**
 * Created 07.03.14 by vlaaad
 */
public class LevelUp extends CreatureAction {

    public LevelUp(Ability owner) {
        super(owner);
    }

    @Override public boolean canBeApplied(Creature creature, Thesaurus.LocalizationData reasonData) {
        return super.canBeApplied(creature, reasonData) && isNotMaxLevel(creature, reasonData);
    }

    private boolean isNotMaxLevel(Creature creature, Thesaurus.LocalizationData reasonData) {
        int exp = creature.getCurrentExp();
        int current = creature.profession.getLevel(exp);
        int neededExp = creature.profession.getExpForLevel(current + 1);
        if (neededExp <= exp) {
            reasonData.key = "die-is-max-level";
            reasonData.params = Thesaurus.params().with("die", creature.description.nameLocKey());
            return false;
        }
        return true;
    }

    @Override protected void doInit(Object setup) {}

    @Override public IFuture<? extends IActionResult> apply(Creature creature, World world) {
        int exp = creature.getCurrentExp();
        int current = creature.profession.getLevel(exp);
        int neededExp = creature.profession.getExpForLevel(current + 1);
        if (neededExp <= exp) {
            return Future.completed(IActionResult.NOTHING);
        } else {
            return Future.completed(new GiveExpResult(creature, neededExp - exp));
        }
    }
}
