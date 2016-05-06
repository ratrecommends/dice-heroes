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
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.vlaaad.common.gdx.scene2d.events.AnimationListener;
import com.vlaaad.common.util.CountDown;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.imp.BerserkAttackResult;
import com.vlaaad.dice.game.world.view.*;
import com.vlaaad.dice.game.world.view.visualizers.objects.Death;
import com.vlaaad.dice.game.world.view.visualizers.objects.Defence;
import com.vlaaad.dice.managers.SoundManager;

/**
 * Created 08.02.14 by vlaaad
 */
public class BerserkAttackVisualizer implements IVisualizer<BerserkAttackResult> {
    private final ResultVisualizer visualizer;

    public BerserkAttackVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final BerserkAttackResult result) {
        final Future<Void> future = new Future<Void>();
        visualizer.viewController.visualize(new Defence(result.target, result.type));
        int dx = result.target.getX() - result.creature.getX();
        int dy = result.target.getY() - result.creature.getY();
        boolean isOrto = dx == 0 || dy == 0;
        String type = isOrto ? "-ortogonal" : "-diagonal";
        visualizer.viewController.world.dispatcher.dispatch(ResultVisualizer.VISUALIZE_ATTACK, result.creature);
        String animationName = "animation/berserk-" + result.type + "-" + result.attackLevel + type;
        final AnimationSubView animation = new AnimationSubView(0.2f, Config.findRegions(animationName), Animation.PlayMode.NORMAL);
        visualizer.viewController.effectLayer.addActor(animation.getActor());
        final WorldObjectView view = visualizer.viewController.getView(result.creature);
        animation.getActor().setOrigin(15, 15);
        animation.getActor().setPosition(view.getX() - 3, view.getY());
        animation.getActor().setRotation(RotateODImagesSubView.getRotation(dx, dy));
        animation.getActor().addListener(new AnimationListener() {
            @Override protected void onAnimationEvent(AnimationEvent event) {
                String soundName = result.type.toString() + (result.success ? "-kill" : "-miss");
                if (SoundManager.instance.soundExists(soundName)) {
                    SoundManager.instance.playSound(soundName);
                }
                final CountDown countDown = new CountDown(2, new Runnable() {
                    @Override public void run() {
                        future.happen();
                    }
                });
                if (result.success) {
                    visualizer.viewController.visualize(new Death(result.creature, result.target)).addListener(countDown);
                } else {
                    countDown.tick();
                }
                animation.getActor().removeListener(this);
                animation.getActor().addAction(Actions.sequence(
                    Actions.alpha(0, 0.5f),
                    Actions.run(new Runnable() {
                        @Override public void run() {
                            animation.getActor().remove();
                            countDown.tick();
                        }
                    })
                ));
            }
        });

        return future;
    }
}
