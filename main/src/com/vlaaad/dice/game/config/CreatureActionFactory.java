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

package com.vlaaad.dice.game.config;

import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.imp.*;
import com.vlaaad.dice.game.config.abilities.Ability;

import java.util.Map;

/**
 * Created 06.10.13 by vlaaad
 */
public class CreatureActionFactory {
    private static ObjectMap<String, Class<? extends CreatureAction>> types = new ObjectMap<String, Class<? extends CreatureAction>>();

    static {
        types.put("attack", Attack.class);
        types.put("defence", DefenceAction.class);
        types.put("cleave", Cleave.class);
        types.put("shot", Shot.class);
        types.put("skip", Skip.class);
        types.put("speed", Speed.class);
        types.put("sequence", Sequence.class);
        types.put("fireball", Fireball.class);
        types.put("freeze", Freeze.class);
        types.put("chain-lightning", ChainLightning.class);
        types.put("firestorm", Firestorm.class);
        types.put("cleric-defence", ClericDefence.class);
        types.put("area-of-attack", AreaOfAttack.class);
        types.put("area-of-defence", AreaOfDefence.class);
        types.put("ignore-defence", IgnoreDefence.class);
        types.put("cooldown", Cooldown.class);
        types.put("transform-to-obstacle", TransformToObstacle.class);
        types.put("poison-shot", PoisonShot.class);
        types.put("concentration", Concentration.class);
        types.put("summon", Summon.class);
        types.put("teleport", Teleport.class);
        types.put("resurrect", Resurrect.class);
        types.put("berserk-attack", BerserkAttack.class);
        types.put("antidote", Antidote.class);
        types.put("area-of-antidote", AreaOfAntidote.class);
        types.put("potion", Potion.class);
        types.put("levelUp", LevelUp.class);
        types.put("ranged-damage", RangedDamage.class);
        types.put("self-antidote", SelfAntidote.class);
        types.put("transform-target-to-obstacle", TransformTargetToObstacle.class);
        types.put("viscosity", Viscosity.class);
        types.put("stupefaction", Stupefaction.class);
        types.put("random-of", RandomOf.class);
        types.put("teleport-self", TeleportSelf.class);
        types.put("random-teleport-self", RandomTeleportSelf.class);
        types.put("apply-to-target", ApplyToTarget.class);
        types.put("end-turn", EndTurn.class);
        types.put("extra-turn", ExtraTurn.class);
        types.put("restrict-use-abilities", RestrictUseAbilities.class);
        types.put("teleport-target", TeleportTarget.class);
        types.put("restrict-resurrect", RestrictResurrect.class);
        types.put("poison-area", PoisonArea.class);
        types.put("decrease-attack-and-defence", DecreaseAttackAndDefence.class);
        types.put("enthrallment", Enthrallment.class);
        types.put("shaman-defence", ShamanDefence.class);
        types.put("ice-storm", IceStorm.class);
        types.put("set", SetAttributes.class);
    }

    public static CreatureAction create(String name, Object setup, Ability ability) {
        try {
            Class<? extends CreatureAction> type = types.get(name);
            if (type == null)
                throw new IllegalArgumentException("there is no such action: " + name);
            return type.getConstructor(Ability.class).newInstance(ability).init(setup).withName(name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static CreatureAction createFromActionSetup(Map actionSetup, Ability ability) {
        String action = MapHelper.get(actionSetup, "action");
        Object setup = MapHelper.get(actionSetup, "setup");
        return create(action, setup, ability);
    }
}
