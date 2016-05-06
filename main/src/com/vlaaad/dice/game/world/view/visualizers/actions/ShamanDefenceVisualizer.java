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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.gdx.scene2d.events.AnimationListener;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.imp.ShamanDefenceResult;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.events.EffectEvent;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.events.EventListener;
import com.vlaaad.dice.game.world.events.EventType;
import com.vlaaad.dice.game.world.view.AnimationSubView;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.WorldObjectView;
import com.vlaaad.dice.managers.SoundManager;

/**
 * Created 17.06.14 by vlaaad
 */
public class ShamanDefenceVisualizer implements IVisualizer<ShamanDefenceResult> {

    public static final String ANIMATION_NAME = "animation/aura-of-protection";
    private final ResultVisualizer visualizer;

    public ShamanDefenceVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final ShamanDefenceResult result) {
        final Future<Void> future = Future.make();
        final WorldObjectView view = visualizer.viewController.getView(result.creatureToAddEffect);
        final AnimationSubView appear = new AnimationSubView(0.1f, Config.findRegions(ANIMATION_NAME), Animation.PlayMode.NORMAL);
        init(appear);
        view.addSubView(appear);
        SoundManager.instance.playMusicAsSound("ability-" + result.ability.name);
        appear.getActor().addListener(new AnimationListener() {
            @Override protected void onAnimationEvent(AnimationEvent event) {
                view.removeSubView(appear);
                Array<TextureRegion> playRegions = Array.with(appear.animation.getKeyFrames());
                playRegions.removeIndex(0);
                playRegions.add(playRegions.peek());
                final AnimationSubView play = new AnimationSubView(0.5f, playRegions, Animation.PlayMode.LOOP);
                init(play);
                view.addSubView(play);
                result.creature.world.dispatcher.add(Creature.REMOVE_EFFECT, new EventListener<EffectEvent>() {
                    @Override public void handle(EventType<EffectEvent> type, EffectEvent event) {
                        if (event.creature == result.creature && event.effect == result.effectToApply) {
                            result.creature.world.dispatcher.remove(Creature.REMOVE_EFFECT, this);
                            view.removeSubView(play);
                            if (result.creature.isKilled())
                                return;
                            SoundManager.instance.playMusicAsSound("ability-" + result.ability.name);
                            Array<TextureAtlas.AtlasRegion> disappearRegions = new Array<TextureAtlas.AtlasRegion>(Config.findRegions(ANIMATION_NAME));
                            disappearRegions.pop();
                            final AnimationSubView disappear = new AnimationSubView(0.1f, disappearRegions, Animation.PlayMode.REVERSED);
                            init(disappear);
                            view.addSubView(disappear);
                            disappear.getActor().addListener(new AnimationListener() {
                                @Override protected void onAnimationEvent(AnimationEvent event) {
                                    view.removeSubView(disappear);
                                }
                            });

                        }
                    }
                });
                future.happen();

            }
        });
        return future;
    }
    private void init(AnimationSubView subView) {
        subView.priority = -9;
        subView.getActor().setPosition(
            ViewController.CELL_SIZE / 2 - subView.getActor().getWidth() / 2,
            ViewController.CELL_SIZE / 2 - subView.getActor().getHeight() / 2 //+ 2
        );
    }
}
