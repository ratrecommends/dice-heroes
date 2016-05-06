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

package com.vlaaad.dice.game.world.view.visualizers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.ITargetOwner;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.managers.SoundManager;

/**
 * Created 28.03.14 by vlaaad
 */
public class BeamVisualizer implements IVisualizer<ITargetOwner> {

    private final Color color;
    private final ResultVisualizer visualizer;
    private final String soundName;

    public BeamVisualizer(ResultVisualizer visualizer, Color color, String soundName) {
        this.color = color;
        this.visualizer = visualizer;
        this.soundName = soundName;
    }

    @Override public IFuture<Void> visualize(ITargetOwner result) {
        final Future<Void> future = new Future<Void>();
        SoundManager.instance.playSoundIfExists(soundName);
        Image image = new Image(Config.skin, "effect-luck-image");
        image.setColor(color);
        image.setScale(0, 0);
        image.setOrigin(image.getWidth() / 2, image.getHeight() / 2);
        image.setPosition(
            result.getTarget().getX() * ViewController.CELL_SIZE + (ViewController.CELL_SIZE - image.getWidth()) * 0.5f,
            result.getTarget().getY() * ViewController.CELL_SIZE + (ViewController.CELL_SIZE - image.getHeight()) * 0.5f + 6
        );
        visualizer.viewController.effectLayer.addActor(image);
        image.addAction(
            Actions.sequence(
                Actions.parallel(
                    Actions.scaleTo(0.75f, 0.75f, 0.5f, Interpolation.sine),
                    Actions.rotateBy(135, 0.5f)
                ),
                Actions.parallel(
                    Actions.scaleTo(0, 0, 0.5f, Interpolation.sine),
                    Actions.rotateBy(135, 0.5f)
                ),
                Actions.run(future),
                Actions.removeActor()
            )
        );
        return future;
    }
}
