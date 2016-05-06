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

package com.vlaaad.dice.game.world.view;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.players.Player;
import com.vlaaad.dice.game.world.players.PlayerRelation;

/**
 * Created 11.05.14 by vlaaad
 */
public class DieSubView implements SubView {
    private final Creature creature;

    private final Tile tile;
    private final Player viewer;
    private final int priority;

    public DieSubView(final Creature creature, Player viewer, int priority) {
        this.creature = creature;
        this.viewer = viewer;
        this.priority = priority;
        tile = new Tile(getViewName()) {
            Player current = creature.player;

            @Override public void draw(Batch batch, float parentAlpha) {
                if (creature.player != current) {
                    tile.setRegion(getViewName());
                    current = creature.player;
                }
                super.draw(batch, parentAlpha);
            }
        };
//        tile.debug();
    }

    private String getViewName() {
        String suffix = viewer.inRelation(creature.player, PlayerRelation.enemy) ? "-enemy" : "";
        String fallbackName = "profession/" + creature.profession.name;
        String name = fallbackName + '-' + creature.description.name;
        if (Config.skin.has(name + suffix, TextureRegion.class))
            return name + suffix;
        return fallbackName + suffix;
    }

    @Override public int getPriority() {
        return priority;
    }

    @Override public void play(String animationName) {
    }

    @Override public Actor getActor() {
        return tile;
    }
}
