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

package com.vlaaad.dice.game.actions;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.util.Function;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.ICondition;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCoordinatesParams;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCreatureParams;
import com.vlaaad.dice.game.world.controllers.BehaviourController;

import java.util.Map;

/**
 * Created 06.10.13 by vlaaad
 */
public abstract class CreatureAction {

    private static final Thesaurus.LocalizationData stub = new Thesaurus.LocalizationData();
    private static final Vector2 tmpVector = new Vector2();
    private static final RelationFilter tmpFilter = new RelationFilter();

    public final Ability owner;
    public String name;
    private Map descriptionParamsMap;

    public CreatureAction(Ability owner) {
        this.owner = owner;
    }

    public final CreatureAction init(Object setup) {
        doInit(setup);
        return this;
    }

    protected final void setDescriptionParamsMap(Map value) {
        descriptionParamsMap = value;
    }

    protected abstract void doInit(Object setup);

    public abstract IFuture<? extends IActionResult> apply(Creature creature, World world);

    public boolean canBeApplied(Creature creature, Thesaurus.LocalizationData reasonData) {
        if (creature.world == null) {
            reasonData.key = "creature-is-not-on-map";
            reasonData.params = new Thesaurus.Params().with("die", creature.description.nameLocKey());
            return false;
        }
        if (owner.type == Ability.Type.profession && !creature.get(Attribute.canUseProfessionAbilities)) {
            reasonData.key = "cant-use-ability";
            reasonData.params = Thesaurus.params()
                .with("die", creature.description.nameLocKey())
                .with("ability", owner.locNameKey());
            return false;
        }
        if (creature.get(Attribute.cooldownFor(owner.name)) != null) {
            reasonData.key = "ability-is-under-cooldown";
            reasonData.params = Thesaurus.params()
                .with("die", creature.description.nameLocKey())
                .with("ability", owner.locNameKey());
            return false;
        }
        return true;
    }

    protected boolean hasNear(Creature creature, Creature.CreatureRelation relation, float radius, Thesaurus.LocalizationData reasonData) {
        if (creatures(creature, relation, radius).size > 0)
            return true;
        reasonData.key = "no-near-creatures-of-relation";
        reasonData.params = Thesaurus.params()
            .with("die", creature.description.nameLocKey())
            .with("radius", String.valueOf(radius))
            .with("relation", relation.toString() + ".many.acc");
        return false;
    }

    protected boolean hasNear(Creature creature, Function<Creature, Boolean> filter, Creature.CreatureRelation relation, float radius, Thesaurus.LocalizationData reasonData) {
        if (creatures(creature, filter, radius).size > 0)
            return true;
        reasonData.key = "no-near-creatures-of-relation";
        reasonData.params = Thesaurus.params()
            .with("die", creature.description.nameLocKey())
            .with("radius", String.valueOf(radius))
            .with("relation", relation.toString() + ".many.acc");
        return false;
    }

    public final boolean canBeApplied(Creature creature) {
        return canBeApplied(creature, stub);
    }

    @Override public final String toString() {
        return getClass().getSimpleName();
    }

    public static CreatureAction doNothing(Ability ability) {
        return new CreatureAction(ability) {
            @Override protected void doInit(Object setup) {
            }

            @Override public IFuture<IActionResult> apply(Creature creature, World world) {
                return Future.completed();
            }
        };
    }

    protected final IFuture<? extends IActionResult> withCreature(Creature creature, Array<Creature> targets, final Function<Creature, IFuture<? extends IActionResult>> function) {
        if (targets.size == 0)
            return Future.completed(IActionResult.NOTHING);
        if (targets.size == 1)
            return function.apply(targets.first());
        final Future<IActionResult> future = new Future<IActionResult>();
        creature.world.getController(BehaviourController.class)
            .get(creature)
            .request(BehaviourRequest.CREATURE, new AbilityCreatureParams(creature, owner, targets))
            .addListener(new IFutureListener<Creature>() {
                @Override public void onHappened(Creature result) {
                    function.apply(result).addListener(future);
                }
            });
        return future;
    }

    protected final IFuture<? extends IActionResult> withCoordinate(Creature creature, Array<Grid2D.Coordinate> targets, final Function<Grid2D.Coordinate, IFuture<? extends IActionResult>> function) {
        if (targets.size == 0)
            return Future.completed(IActionResult.NOTHING);
        if (targets.size == 1)
            return function.apply(targets.first());
        final Future<IActionResult> future = new Future<IActionResult>();
        creature.world.getController(BehaviourController.class)
            .get(creature)
            .request(BehaviourRequest.COORDINATE, new AbilityCoordinatesParams(creature, owner, targets))
            .addListener(new IFutureListener<Grid2D.Coordinate>() {
                @Override public void onHappened(Grid2D.Coordinate result) {
                    function.apply(result).addListener(future);
                }
            });
        return future;
    }

    protected final Array<Grid2D.Coordinate> coordinates(Creature creature, float radius, ICondition<Grid2D.Coordinate> condition) {

        int checkRadius = MathUtils.ceil(radius);
        float radius2 = radius * radius;

        Vector2 position = tmpVector.set(creature.getX(), creature.getY());
        Array<Grid2D.Coordinate> result = new Array<Grid2D.Coordinate>();

        for (int i = creature.getX() - checkRadius; i <= creature.getX() + checkRadius; i++) {
            for (int j = creature.getY() - checkRadius; j <= creature.getY() + checkRadius; j++) {

                if (position.dst2(i, j) > radius2)
                    continue;
                Grid2D.Coordinate coordinate = Grid2D.obtain(i, j);
                if (condition.isSatisfied(coordinate)) {
                    result.add(coordinate);
                } else {
                    Grid2D.free(coordinate);
                }
            }
        }
        return result;
    }

    protected final Array<Creature> creatures(Creature creature, Creature.CreatureRelation relation, float radius) {
        try {
            return creatures(creature, tmpFilter.withRelation(creature, relation), radius);
        } finally {
            tmpFilter.withRelation(null, null);
        }
    }

    protected final Array<Creature> creatures(World world, int x, int y, Function<Creature, Boolean> filter, float radius) {
        Vector2 creaturePos = tmpVector.set(x, y);
        Array<Creature> result = new Array<Creature>();
        for (WorldObject object : world) {
            if (!(object instanceof Creature))
                continue;
            Creature check = (Creature) object;
            if (!check.get(Attribute.canBeSelected) || !filter.apply(check))
                continue;
            if (creaturePos.dst(check.getX(), check.getY()) > radius)
                continue;
            result.add(check);
        }
        return result;
    }

    protected final Array<Creature> creatures(Creature creature, Function<Creature, Boolean> filter, float radius) {
        return creatures(creature.world, creature.getX(), creature.getY(), filter, radius);
    }

    public final CreatureAction withName(String name) {
        this.name = name;
        return this;
    }

    public void fillDescriptionParams(Thesaurus.Params params, Creature creature) {
        if (descriptionParamsMap != null) {
            for (Object key : descriptionParamsMap.keySet()) {
                params.with(String.valueOf(key), String.valueOf(descriptionParamsMap.get(key)));
            }
        }
    }

    private static class RelationFilter implements Function<Creature, Boolean> {

        private Creature creature;
        private Creature.CreatureRelation relation;

        public RelationFilter withRelation(Creature creature, Creature.CreatureRelation relation) {
            this.creature = creature;
            this.relation = relation;
            return this;
        }

        @Override public Boolean apply(Creature that) {
            return creature.inRelation(relation, that);
        }
    }
}
