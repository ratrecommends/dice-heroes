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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.game.actions.AttackType;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.FirestormResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.levels.LevelElementType;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.util.ExpHelper;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCoordinatesParams;
import com.vlaaad.dice.game.world.controllers.BehaviourController;

import java.util.Map;

/**
 * Created 14.01.14 by vlaaad
 */
public class Firestorm extends CreatureAction {
    private static final Vector2 tmp = new Vector2();

    private float radius;
    private int attackLevel;
    private int epicenterAttackLevel;
    private AttackType attackType;

    public Firestorm(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {
        Map data = (Map) setup;
        radius = MapHelper.get(data, "radius", Numbers.ONE).floatValue();
        attackType = AttackType.valueOf(MapHelper.get(data, "type", "weapon"));
        attackLevel = MapHelper.get(data, "level", Numbers.ONE).intValue();
        epicenterAttackLevel = MapHelper.get(data, "epicenterLevel", Numbers.ONE).intValue();
    }

    @Override public IFuture<IActionResult> apply(final Creature creature, World world) {
        int level = creature.getCurrentLevel();
        Vector2 position = tmp.set(creature.getX(), creature.getY());
        Array<Grid2D.Coordinate> available = new Array<Grid2D.Coordinate>();
        for (int i = creature.getX() - level; i <= creature.getX() + level; i++) {
            for (int j = creature.getY() - level; j <= creature.getY() + level; j++) {
                if (position.dst(i, j) <= level && world.level.exists(LevelElementType.tile, i, j)) {
                    available.add(new Grid2D.Coordinate(i, j));
                }
            }
        }
        final Future<IActionResult> future = new Future<IActionResult>();
        world.getController(BehaviourController.class)
            .get(creature)
            .request(BehaviourRequest.COORDINATE, new AbilityCoordinatesParams(creature, owner, available))
            .addListener(new IFutureListener<Grid2D.Coordinate>() {
                @Override public void onHappened(Grid2D.Coordinate coordinate) {
                    future.happen(calcResult(creature, coordinate));
                }
            });
        return future;
    }

    private IActionResult calcResult(Creature creature, Grid2D.Coordinate cell) {
        Vector2 position = tmp.set(cell.x(), cell.y());
        Array<Creature> underAttack = new Array<Creature>();
        Array<Creature> killed = new Array<Creature>();
        ObjectIntMap<Creature> expResults = new ObjectIntMap<Creature>();
        for (int i = cell.x() - MathUtils.ceil(radius); i <= cell.x() + radius; i++) {
            for (int j = cell.y() - MathUtils.ceil(radius); j <= cell.y() + radius; j++) {
                if (position.dst(i, j) <= radius) {
                    WorldObject object = creature.world.get(i, j);
                    if (object instanceof Creature && ((Creature) object).get(Attribute.canBeSelected)) {
                        underAttack.add((Creature) object);
                    }
                }
            }
        }
        for (Creature c : underAttack) {
            int attackLevel = (c.getX() == cell.x() && c.getY() == cell.y()) ? this.epicenterAttackLevel : this.attackLevel;
            int defenceLevel = c.get(Attribute.defenceFor(attackType));
            if (attackLevel > defenceLevel) {
                killed.add(c);
                if (creature.inRelation(Creature.CreatureRelation.enemy, c)) {
                    expResults.getAndIncrement(creature, 0, ExpHelper.expForKill(creature, c));
                }
            } else {
                if (creature.inRelation(Creature.CreatureRelation.enemy, c)) {
                    expResults.put(c, ExpHelper.expForDefence(creature, c));
                } else {
                    expResults.put(c, ExpHelper.MIN_EXP);
                }
            }
        }
        return new FirestormResult(creature, owner, underAttack, killed, expResults, cell);
    }
}
