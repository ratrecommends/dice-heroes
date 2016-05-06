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
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.professions.ProfessionDescription;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.util.DistanceFiller;

/**
 * Created 27.05.14 by vlaaad
 */
public class AiShamanHeavyDefenceProcessor extends AiDefaultProfessionAbilityProcessor {
    private static final Vector2 tmp = new Vector2();

    public AiShamanHeavyDefenceProcessor() {
        super("shaman", "shaman-heavy-defence");
    }

    @Override protected int preProcess(Creature creature, Ability ability) {
        ProfessionDescription archer = Config.professions.get("archer");
        ProfessionDescription warrior = Config.professions.get("warrior");
        ProfessionDescription mage = Config.professions.get("mage");

        for (Creature c : creature.world.byType(Creature.class)) {
            if (creature.inRelation(Creature.CreatureRelation.ally, c))
                continue;
            if (c.profession == warrior) {
                int dst = DistanceFiller.getDistance(c, creature);
                if (dst < 3)
                    return 2;
            }
            float dst = tmp.set(c.getX(), c.getY()).dst(creature.getX(), creature.getY());
            if (c.profession == archer && dst < 5)
                return 2;
            if (c.profession == mage && dst < 7)
                return 2;

        }
        return 0;
    }
}
