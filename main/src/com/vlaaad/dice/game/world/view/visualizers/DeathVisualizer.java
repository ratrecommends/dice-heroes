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

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.players.PlayerRelation;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.visualizers.objects.Death;
import com.vlaaad.dice.game.world.view.visualizers.objects.DroppedItem;

public class DeathVisualizer implements IVisualizer<Death> {
    private final ResultVisualizer visualizer;

    public DeathVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(Death death) {
        final Future<Void> future = Future.make();
        final Creature target = death.target;
        final Creature killer = death.killer;
        visualizer.viewController.getView(target).addAction(Actions.sequence(
            Actions.alpha(0),
            Actions.delay(0.1f),
            Actions.alpha(0.5f),
            Actions.delay(0.1f),
            Actions.alpha(0),
            Actions.delay(0.1f),
            Actions.alpha(0.5f),
            Actions.delay(0.1f),
            Actions.alpha(0f),
            Actions.run(new Runnable() {
                @Override public void run() {
                    if (target.drop.size > 0 && killer.player == visualizer.viewController.world.viewer && killer.player.inRelation(target.player, PlayerRelation.enemy)) {
                        Array<DroppedItem> items = new Array<DroppedItem>();
                        for (Item drop : target.drop.keys()) {
                            items.add(new DroppedItem(target, drop, target.drop.get(drop, 0)));
                        }
                        items.sort(DroppedItem.ORDER_COMPARATOR);
                        visualizer.viewController.visualize(items).addListener(future);
                    } else {
                        future.happen();
                    }
                }
            })
        ));
        return future;
    }
}
