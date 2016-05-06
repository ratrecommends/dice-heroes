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
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.gdx.scene2d.events.AnimationListener;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.effects.IDefenceEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.view.*;
import com.vlaaad.dice.game.world.view.visualizers.objects.Defence;

public class DefenceEffectActionVisualizer implements IVisualizer<DefenceVisualizer.DefenceEffectAction> {
    private final ResultVisualizer visualizer;

    public DefenceEffectActionVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(DefenceVisualizer.DefenceEffectAction action) {
        final Defence defence = action.defence;
        final IDefenceEffect defenceEffect = action.effect;
        Creature target = defence.target;
        visualizer.viewController.world.dispatcher.dispatch(ResultVisualizer.VISUALIZE_DEFENCE, target);
        final WorldObjectView targetView = visualizer.viewController.getView(target);
        String fallbackName = "animation/" + defenceEffect.toString();
        String abilityName = "animation/" + defenceEffect.getAbility().name;
        boolean useAbilityName = Config.skin.getAtlas().findRegion(abilityName) != null;
        String baseName = useAbilityName ? abilityName : fallbackName;
        final Array<TextureAtlas.AtlasRegion> appearRegions = Config.findRegions(baseName);
        if (appearRegions.size == 0) {
            return Future.completed();
        }
        final AnimationSubView appear = new AnimationSubView(0.05f, appearRegions, Animation.PlayMode.NORMAL);
        targetView.addSubView(appear);
        final Future<Void> future = Future.make();
        targetView.addListener(new AnimationListener() {
            @Override protected void onAnimationEvent(AnimationEvent event) {
                targetView.removeListener(this);
                final ImageSubView blink = new ImageSubView("animation/defence-blink", -9);
                targetView.addSubView(blink);
                blink.getActor().addAction(Actions.sequence(
                    Actions.alpha(0),
                    Actions.alpha(1, 0.05f),
                    Actions.delay(0.2f),
                    Actions.alpha(0, 0.05f),
                    Actions.run(new Runnable() {
                        @Override public void run() {
                            targetView.removeSubView(blink);
                            targetView.removeSubView(appear);
                            Array<TextureAtlas.AtlasRegion> disappearRegions = new Array<TextureAtlas.AtlasRegion>(appearRegions);
                            if (disappearRegions.size > 1)
                                disappearRegions.removeIndex(1);
                            disappearRegions.reverse();
                            final AnimationSubView disappear = new AnimationSubView(0.05f, disappearRegions, Animation.PlayMode.NORMAL);
                            targetView.addSubView(disappear);
                            targetView.addListener(new AnimationListener() {
                                @Override protected void onAnimationEvent(AnimationEvent event) {
                                    targetView.removeListener(this);
                                    targetView.removeSubView(disappear);
                                    future.happen();
                                }
                            });
                        }
                    })
                ));
            }
        });
        return future;
    }
}
