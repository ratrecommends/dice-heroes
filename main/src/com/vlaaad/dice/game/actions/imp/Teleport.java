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
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.TeleportResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCoordinatesParams;
import com.vlaaad.dice.game.world.controllers.BehaviourController;

/**
 * Created 06.02.14 by vlaaad
 */
public class Teleport extends CreatureAction {

    private static final Vector2 tmp = new Vector2();

    public Teleport(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {
    }

    @Override public IFuture<? extends IActionResult> apply(final Creature creature, World world) {
        final Future<IActionResult> future = new Future<IActionResult>();
        Array<Grid2D.Coordinate> coordinates = gatherCoordinates(creature, creature.getCurrentLevel());
        if (coordinates.size == 0)
            return Future.completed(IActionResult.NOTHING);
        if (coordinates.size == 1)
            return Future.completed(calcResult(creature, coordinates.first()));
        creature.world.getController(BehaviourController.class)
            .get(creature)
            .request(BehaviourRequest.COORDINATE, new AbilityCoordinatesParams(creature, owner, coordinates))
            .addListener(new IFutureListener<Grid2D.Coordinate>() {
                @Override public void onHappened(Grid2D.Coordinate coordinate) {
                    future.happen(calcResult(creature, coordinate));
                }
            });
        return future;
    }

    public static Array<Grid2D.Coordinate> gatherCoordinates(Creature creature, float radius) {
        Vector2 position = tmp.set(creature.getX(), creature.getY());
        Array<Grid2D.Coordinate> available = new Array<Grid2D.Coordinate>();
        for (int i = creature.getX() - MathUtils.ceil(radius); i <= creature.getX() + MathUtils.ceil(radius); i++) {
            for (int j = creature.getY() - MathUtils.ceil(radius); j <= creature.getY() + MathUtils.ceil(radius); j++) {
                boolean sameCoordinate = i == creature.getX() && j == creature.getY();
                boolean canStep = creature.world.canStepTo(i, j);
                boolean inRadius = position.dst(i, j) <= radius;
                if (inRadius && (sameCoordinate || canStep))
                    available.add(new Grid2D.Coordinate(i, j));
            }
        }
        return available;
    }

    @Override public void fillDescriptionParams(Thesaurus.Params params, Creature creature) {
        params.with("radius", String.valueOf(creature.getCurrentLevel()));
    }

    private IActionResult calcResult(Creature creature, Grid2D.Coordinate coordinate) {
        return new TeleportResult(owner, creature, coordinate);
    }
}
