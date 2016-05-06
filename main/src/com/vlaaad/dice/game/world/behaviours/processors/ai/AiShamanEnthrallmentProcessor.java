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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.util.ExpHelper;

import java.util.Comparator;

/**
 * Created 27.05.14 by vlaaad
 */
public class AiShamanEnthrallmentProcessor extends AiDefaultProfessionAbilityProcessor {

    public static final Comparator<? super Creature> VALUE_COMPARATOR = new Comparator<Creature>() {
        @Override public int compare(Creature o1, Creature o2) {
            return ExpHelper.getTotalCost(o1) - ExpHelper.getTotalCost(o2);
        }
    };
    private static final int radius = 5;
    private static final Vector2 tmpVec = new Vector2();
    private static final Array<Creature> tmp = new Array<Creature>();

    public AiShamanEnthrallmentProcessor() {
        super("shaman", "enthrallment");
    }

    @Override protected int preProcess(Creature creature, Ability ability) {
        Array<Creature> enemies = tmp;
        for (Creature c : creature.world.byType(Creature.class)) {
            if (!c.inRelation(Creature.CreatureRelation.enemy, creature))
                continue;
            enemies.add(c);
        }
        if (enemies.size == 0)
            return -1;
        enemies.sort(VALUE_COMPARATOR);
        if (enemies.size > 1) {
            enemies.truncate(enemies.size / 2);
        }
        for (Creature e : enemies) {
            if (tmpVec.set(e.getX(), e.getY()).dst2(creature.getX(), creature.getY()) <= radius * radius) {
                tmp.clear();
                return 3;
            }
        }
        tmp.clear();
        return -1;
    }

}
