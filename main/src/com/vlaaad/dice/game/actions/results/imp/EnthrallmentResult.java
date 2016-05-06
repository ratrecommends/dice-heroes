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
import com.vlaaad.dice.game.effects.EnthrallmentEffect;
import com.vlaaad.dice.game.objects.Creature;

/**
 * Created 10.05.14 by vlaaad
 */
public class EnthrallmentResult extends AddEffect {
    public final Ability ability;
    public final Creature creature;
    public final Creature target;
    public final int turns;

    public EnthrallmentResult(Ability ability, Creature creature, Creature target, int turns) {
        super(ability, target, new EnthrallmentEffect(creature.player, ability, turns));
        this.ability = ability;
        this.creature = creature;
        this.target = target;
        this.turns = turns;
    }
}
