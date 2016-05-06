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

import com.badlogic.gdx.utils.Array;
import com.vlaaad.dice.game.actions.AttackType;
import com.vlaaad.dice.game.actions.imp.Shot;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.objects.Creature;

/**
 * Created 12.02.14 by vlaaad
 */
public class PoisonDartProcessor extends AiDefaultProfessionAbilityProcessor {
    public PoisonDartProcessor() {
        super("archer", "poison-dart");
    }

    @Override protected int preProcess(Creature creature, Ability ability) {
        Array<Creature> targets = Shot.findTargets(creature, Creature.CreatureRelation.enemy, creature.getX(), creature.getY(), creature.world, 3);
        for (Creature c : targets) {
            if (c.get(Attribute.defenceFor(AttackType.weapon)) > 0)
                return 2;
        }
        return 0;
    }
}
