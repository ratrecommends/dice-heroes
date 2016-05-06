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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.actions.AttackType;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.AreaOfAttackResult;
import com.vlaaad.dice.game.actions.results.imp.GiveExpResult;
import com.vlaaad.dice.game.actions.results.imp.SequenceResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.util.ExpHelper;
import com.vlaaad.dice.game.world.World;

import java.util.Map;

/**
 * Created 22.01.14 by vlaaad
 */
public class AreaOfAttack extends CreatureAction {


    private static final Vector2 tmp1 = new Vector2();
    private static final Vector2 tmp2 = new Vector2();

    protected AttackType type;
    protected int level;
    private float radius;

    public AreaOfAttack(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {
        //{type: weapon, level: 2, radius: 3}
        Map data = (Map) setup;
        type = AttackType.valueOf(MapHelper.get(data, "type", "weapon"));
        level = MapHelper.get(data, "level", Numbers.ONE).intValue();
        radius = MapHelper.get(data, "radius", Numbers.ONE).floatValue();
    }

    @Override public IFuture<IActionResult> apply(Creature creature, World world) {
        return Future.completed(calcResult(creature, world));
    }

    protected IActionResult calcResult(Creature creature, World world) {
        Array<Creature> targets = findTargets(creature, Creature.CreatureRelation.ally);
        if (targets.size == 0)
            return IActionResult.NOTHING;
        return new SequenceResult(
            createResult(creature, targets),
            new GiveExpResult(creature, targets.size * ExpHelper.MIN_EXP)
        );
    }

    protected IActionResult createResult(Creature creature, Array<Creature> targets) {
        return new AreaOfAttackResult(owner, creature, targets, type, level);
    }

    protected Array<Creature> findTargets(Creature creature, Creature.CreatureRelation relation) {
        Vector2 creaturePos = tmp1.set(creature.getX(), creature.getY());
        World world = creature.world;
        Array<Creature> result = new Array<Creature>();
        for (WorldObject object : world) {
            if (!(object instanceof Creature))
                continue;
            Creature check = (Creature) object;
            if (!check.get(Attribute.canBeSelected) || !creature.inRelation(relation, check))
                continue;
            Vector2 checkPos = tmp2.set(check.getX(), check.getY());
            if (checkPos.dst(creaturePos) > radius)
                continue;
            result.add(check);
        }
        return result;
    }
}
