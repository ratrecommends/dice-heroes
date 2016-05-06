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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.players.Player;
import com.vlaaad.dice.game.world.players.util.PlayerColors;
import com.vlaaad.dice.ui.scene2d.LocLabel;

/**
 * Created 13.10.13 by vlaaad
 */
public class NameSubView implements SubView {
    private static final Color ENEMY = new Color(0.964705882f, 0.509803922f, 0.478431373f, 1);
    private final Creature creature;
    private final LocLabel label;
    private final Player viewer;
    private final PlayerColors colors;

    private Group group = new Group() {
        private Player player;

        @Override public void draw(Batch batch, float parentAlpha) {
            Player newPlayer = creature.player;
            if (player != null && newPlayer != player) {
//                label.setColor(getDisplayedColor(newFraction));
                label.getStyle().fontColor = getDisplayedColor(newPlayer);
            }
            player = newPlayer;
            super.draw(batch, parentAlpha);
        }
    };

    public NameSubView(Creature creature, Player viewer, PlayerColors colors, String nameKey) {
        this.creature = creature;
        this.viewer = viewer;
        this.colors = colors;
        Image background = new Image(Config.skin, "creature-name-background");
        background.setY(ViewController.CELL_SIZE - 3);
        label = new LocLabel(nameKey, getDisplayedColor(creature.player));
        label.invalidate();
        label.validate();
        label.setY(ViewController.CELL_SIZE);
        label.setX(ViewController.CELL_SIZE / 2f - label.getTextBounds().x / 2f);
        group.addActor(background);
        group.addActor(label);
        group.setTouchable(Touchable.disabled);
    }

    private Color getDisplayedColor(Player player) {
        return colors.getColor(viewer, player);
    }

    @Override public int getPriority() {
        return -10;
    }

    @Override public void play(String animationName) {
    }

    @Override public Actor getActor() {
        return group;
    }
}
