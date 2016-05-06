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
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.gdx.scene2d.ParticleActor;
import com.vlaaad.common.gdx.scene2d.events.AnimationListener;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.imp.RestrictUseAbilitiesResult;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.events.EffectEvent;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.events.EventListener;
import com.vlaaad.dice.game.world.events.EventType;
import com.vlaaad.dice.game.world.view.*;
import com.vlaaad.dice.managers.SoundManager;

/**
 * Created 11.05.14 by vlaaad
 */
public class RestrictUseAbilitiesVisualizer implements IVisualizer<RestrictUseAbilitiesResult> {

    private final ResultVisualizer visualizer;

    public RestrictUseAbilitiesVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final RestrictUseAbilitiesResult result) {
        final Future<Void> future = Future.make();
        SoundManager.instance.playMusicAsSound("ability-" + result.ability.name);

        final AnimationSubView appear = new AnimationSubView(0.1f, Config.findRegions("animation/" + result.ability.name), Animation.PlayMode.NORMAL);
        appear.getActor().setPosition(
            ViewController.CELL_SIZE / 2 - appear.getActor().getWidth() / 2,
            ViewController.CELL_SIZE / 2 - appear.getActor().getHeight() / 2 + 2
        );
        final WorldObjectView targetView = visualizer.viewController.getView(result.getTarget());
        final WorldObjectView creatureView = visualizer.viewController.getView(result.creature);
        creatureView.addSubView(appear);
        appear.getActor().addListener(new AnimationListener() {
            @Override protected void onAnimationEvent(AnimationEvent event) {
                final ParticleActor particles = new ParticleActor("ability-" + result.ability.name);
                particles.freeOnComplete();
                particles.setPosition(
                    ViewController.CELL_SIZE * 0.5f,
                    ViewController.CELL_SIZE * 0.5f + 3
                );
                targetView.addSubView(new ActorSubView(particles, -11));
                result.target.world.dispatcher.add(Creature.REMOVE_EFFECT, new EventListener<EffectEvent>() {
                    @Override public void handle(EventType<EffectEvent> type, EffectEvent event) {
                        if (event.creature != result.target || event.effect != result.effect)
                            return;
                        result.target.world.dispatcher.remove(Creature.REMOVE_EFFECT, this);
                        particles.effect.allowCompletion();
                    }
                });


                creatureView.removeSubView(appear);
                Array<TextureAtlas.AtlasRegion> frames = new Array<TextureAtlas.AtlasRegion>(Config.findRegions("animation/" + result.ability.name));
                frames.removeIndex(1);
                frames.reverse();
                final AnimationSubView disappear = new AnimationSubView(0.1f, frames, Animation.PlayMode.NORMAL);
                disappear.getActor().setPosition(
                    ViewController.CELL_SIZE / 2 - appear.getActor().getWidth() / 2,
                    ViewController.CELL_SIZE / 2 - appear.getActor().getHeight() / 2 + 2
                );
                creatureView.addSubView(disappear);
                disappear.getActor().addListener(new AnimationListener() {
                    @Override protected void onAnimationEvent(AnimationEvent event) {
                        creatureView.removeSubView(disappear);
                        future.happen();
                    }
                });
            }
        });
        return future;
    }
}
