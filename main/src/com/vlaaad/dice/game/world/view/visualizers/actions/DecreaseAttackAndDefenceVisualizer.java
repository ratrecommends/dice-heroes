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

package com.vlaaad.dice.game.world.view.visualizers.actions;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.gdx.scene2d.events.AnimationListener;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.imp.DecreaseAttackAndDefenceResult;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.*;
import com.vlaaad.dice.managers.SoundManager;

/**
 * Created 08.06.14 by vlaaad
 */
public class DecreaseAttackAndDefenceVisualizer implements IVisualizer<DecreaseAttackAndDefenceResult> {
    private final ResultVisualizer visualizer;

    public DecreaseAttackAndDefenceVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final DecreaseAttackAndDefenceResult result) {
        final Future<Void> future = Future.make();
        SoundManager.instance.playMusicAsSound("ability-" + result.ability.name);
        visualizer.viewController.scroller.centerOn(result.creature);
        final WorldObjectView view = visualizer.viewController.getView(result.creature);
        final AnimationSubView appear = new AnimationSubView(0.1f, Config.findRegions("animation/" + result.ability.name), Animation.PlayMode.NORMAL);
        appear.getActor().setPosition(
            ViewController.CELL_SIZE / 2 - appear.getActor().getWidth() / 2,
            ViewController.CELL_SIZE / 2 - appear.getActor().getHeight() / 2 + 2
        );
        view.addSubView(appear);
        appear.getActor().addListener(new AnimationListener() {
            @Override protected void onAnimationEvent(AnimationEvent event) {
                view.removeSubView(appear);
                Array<TextureAtlas.AtlasRegion> frames = new Array<TextureAtlas.AtlasRegion>(Config.findRegions("animation/" + result.ability.name));
                frames.removeIndex(1);
                frames.reverse();
                final AnimationSubView disappear = new AnimationSubView(0.1f, frames, Animation.PlayMode.NORMAL);
                disappear.getActor().setPosition(
                    ViewController.CELL_SIZE / 2 - appear.getActor().getWidth() / 2,
                    ViewController.CELL_SIZE / 2 - appear.getActor().getHeight() / 2 + 2
                );
                view.addSubView(disappear);
                for (Creature target : result.targets) {
                    Tile tile = new Tile("game-" + result.ability.name + "-get-icon");
                    tile.setPosition(
                        (target.getX() + 0.5f) * ViewController.CELL_SIZE - tile.getWidth() / 2,
                        (target.getY() + 1) * ViewController.CELL_SIZE
                    );
                    visualizer.viewController.effectLayer.addActor(tile);
                    tile.addAction(Actions.sequence(
                        Actions.parallel(
                            Actions.moveBy(0, ViewController.CELL_SIZE / 2f, 1f),
                            Actions.alpha(0, 1f)
                        ),
                        Actions.removeActor()
                    ));
                }
                disappear.getActor().addListener(new AnimationListener() {
                    @Override protected void onAnimationEvent(AnimationEvent event) {
                        view.removeSubView(disappear);
                        future.happen();
                    }
                });
            }
        });
        return future;
    }
}
