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

import com.badlogic.gdx.graphics.g2d.Animation;
import com.vlaaad.common.gdx.scene2d.events.AnimationListener;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.IAbilityOwner;
import com.vlaaad.dice.game.actions.results.ITargetOwner;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.AnimationSubView;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.WorldObjectView;
import com.vlaaad.dice.managers.SoundManager;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class AnimationFadeVisualizer<T extends IAbilityOwner & ITargetOwner> implements IVisualizer<T> {

    private final ResultVisualizer visualizer;

    public AnimationFadeVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(T data) {
        final Future<Void> future = Future.make();
        final Ability ability = data.getAbility();
        final Creature target = data.getTarget();
        final AnimationSubView animationSubView = new AnimationSubView(
            0.1f,
            Config.findRegions("animation/" + ability.name),
            Animation.PlayMode.NORMAL
        );
        visualizer.viewController.scroller.centerOn(target);
        final WorldObjectView view = visualizer.viewController.getView(target);
        SoundManager.instance.playMusicAsSound("ability-" + ability.name);
        animationSubView.getActor().getColor().a = 0.6f;
        view.addSubView(animationSubView);
        animationSubView.getActor().setPosition(
            ViewController.CELL_SIZE / 2 - animationSubView.getActor().getWidth() / 2,
            ViewController.CELL_SIZE / 2 - animationSubView.getActor().getHeight() / 2 + 3
        );
        view.addListener(new AnimationListener() {
            @Override protected void onAnimationEvent(AnimationEvent event) {
                view.removeListener(this);
                animationSubView.getActor().addAction(sequence(
                    delay(0.5f),
                    alpha(0, 0.5f),
                    run(new Runnable() {
                        @Override public void run() {
                            view.removeSubView(animationSubView);
                            future.happen();
                        }
                    })
                ));
            }
        });
        return future;
    }
}
