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

package com.vlaaad.dice.game.actions.results.imp;

import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.effects.CreatureEffect;
import com.vlaaad.dice.game.effects.PoisonEffect;
import com.vlaaad.dice.game.objects.Creature;

/**
 * Created 03.02.14 by vlaaad
 */
public class PoisonShotResult extends AddEffect {

    public final Creature creature;
    public final Creature target;
    public final Ability ability;

    public PoisonShotResult(Creature creature, Creature target, int turnCount, Ability ability) {
        super(ability, target, new PoisonEffect(creature, ability, turnCount));
        this.creature = creature;
        this.target = target;
        this.ability = ability;
    }
}
