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
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.imp.AddEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.events.EffectEvent;
import com.vlaaad.dice.game.world.events.EventListener;
import com.vlaaad.dice.game.world.events.EventType;
import com.vlaaad.dice.game.world.view.AnimationSubView;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.WorldObjectView;
import com.vlaaad.dice.managers.SoundManager;

public class AddEffectVisualizer implements IVisualizer<AddEffect> {

    public static final float DURATION = 2f;

    private final ResultVisualizer visualizer;

    public AddEffectVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final AddEffect result) {
        final Array<TextureAtlas.AtlasRegion> regions = Config.findRegions("animation/effect-" + result.ability.name);
        if (regions.size == 0)
            return Future.completed();
        final WorldObjectView view = visualizer.viewController.getView(result.getTarget());
        final AnimationSubView subView = new AnimationSubView(0.1f, regions, Animation.PlayMode.LOOP);
        subView.getActor().setPosition(1, 2);
        subView.priority = 1;
        view.addSubView(subView);
        visualizer.viewController.world.dispatcher.add(Creature.REMOVE_EFFECT, new EventListener<EffectEvent>() {
            @Override public void handle(EventType<EffectEvent> type, EffectEvent event) {
                if (event.effect != result.effectToApply || event.creature != result.creatureToAddEffect)
                    return;
                visualizer.viewController.world.dispatcher.remove(Creature.REMOVE_EFFECT, this);
                SoundManager.instance.playMusicAsSound("boss-protection-loss");
                subView.getActor().addAction(Actions.alpha(0, DURATION));
                subView.getActor().addAction(Actions.delay(DURATION, Actions.run(new Runnable() {
                    @Override public void run() {
                        view.removeSubView(subView);
                    }
                })));
            }
        });
        return Future.completed();
    }
}
