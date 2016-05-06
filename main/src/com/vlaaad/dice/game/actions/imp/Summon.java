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

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.SummonResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.professions.ProfessionDescription;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCoordinatesParams;
import com.vlaaad.dice.game.world.controllers.BehaviourController;
import com.vlaaad.dice.game.world.controllers.RandomController;

import java.util.Map;

/**
 * Created 06.02.14 by vlaaad
 */
public class Summon extends CreatureAction {
    private String professionName;
    private Iterable<? extends String> abilityNames;
    private String summonedName;

    public Summon(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {
        Map data = (Map) setup;
        professionName = MapHelper.get(data, "profession");
        abilityNames = MapHelper.get(data, "abilities");
        summonedName = MapHelper.get(data, "name", "dale");
    }

    @Override public boolean canBeApplied(Creature creature, Thesaurus.LocalizationData reasonData) {
        return super.canBeApplied(creature, reasonData) && hasPlaceToSummon(creature, reasonData);
    }

    private boolean hasPlaceToSummon(Creature creature, Thesaurus.LocalizationData reasonData) {
        if (creature.world == null) {
            reasonData.key = "creature-is-not-on-map";
            reasonData.params = new Thesaurus.Params().with("die", creature.description.nameLocKey());
            return false;
        }
        int x = creature.getX();
        int y = creature.getY();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (creature.world.canStepTo(i, j))
                    return true;
            }
        }
        reasonData.key = "can-not-place-summoned";
        reasonData.params = new Thesaurus.Params().with("die", creature.description.nameLocKey());
        return false;
    }

    @Override public IFuture<? extends IActionResult> apply(final Creature creature, World world) {
        Array<Grid2D.Coordinate> available = new Array<Grid2D.Coordinate>();
        int x = creature.getX();
        int y = creature.getY();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (creature.world.canStepTo(i, j)) {
                    available.add(new Grid2D.Coordinate(i, j));
                }
            }
        }
        if (available.size == 0)
            return Future.completed(IActionResult.NOTHING);
        else if (available.size == 1) {
            return Future.completed(calcResult(creature, available.first()));
        } else {
            final Future<IActionResult> future = new Future<IActionResult>();
            creature.world.getController(BehaviourController.class)
                .get(creature)
                .request(BehaviourRequest.COORDINATE, new AbilityCoordinatesParams(creature, owner, available))
                .addListener(new IFutureListener<Grid2D.Coordinate>() {
                    @Override public void onHappened(Grid2D.Coordinate coordinate) {
                        future.happen(calcResult(creature, coordinate));
                    }
                });
            return future;
        }
    }

    private IActionResult calcResult(Creature creature, Grid2D.Coordinate coordinate) {
        ProfessionDescription profession = Config.professions.get(professionName);

        Array<Ability> abilities = new Array<Ability>();
        for (String abilityName : abilityNames) {
            abilities.add(Config.abilities.get(abilityName));
        }

        Die die = new Die(profession, summonedName, 10000000, abilities, new ObjectIntMap<Ability>());
        Creature summoned = new Creature(die, creature.player, creature.player.fraction + "@s-" + creature.player.nextSummonedId());
        summoned.set(Attribute.canBeResurrected, Boolean.FALSE);
        return new SummonResult(owner, creature, coordinate, summoned);
    }
}
