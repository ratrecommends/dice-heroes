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

import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.vlaaad.common.gdx.scene2d.ParticleActor;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.AttackType;
import com.vlaaad.dice.game.actions.results.imp.RangedDamageResult;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.visualizers.objects.Death;
import com.vlaaad.dice.game.world.view.visualizers.objects.Defence;
import com.vlaaad.dice.managers.SoundManager;

/**
 * Created 14.03.14 by vlaaad
 */
public class RangedDamageVisualizer implements IVisualizer<RangedDamageResult> {

    private final ResultVisualizer visualizer;

    public RangedDamageVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(RangedDamageResult result) {
        final Future<Void> future = new Future<Void>();
        SoundManager.instance.playSound("ability-firestorm");
        if (Config.particles.exists("ability-" + result.ability.name + "-hit")) {
            ParticleEffectPool effectPool = Config.particles.get("ability-" + result.ability.name + "-hit");
            ParticleActor actor = new ParticleActor(effectPool.obtain());
            visualizer.viewController.effectLayer.addActor(actor);
            actor.freeOnComplete();
            actor.setPosition(
                result.target.getX() * ViewController.CELL_SIZE + ViewController.CELL_SIZE / 2,
                result.target.getY() * ViewController.CELL_SIZE + ViewController.CELL_SIZE / 2
            );
        }
        if (result.success) {
            visualizer.viewController.visualize(new Death(result.creature, result.target)).addListener(future);
        } else {
            visualizer.viewController.visualize(new Defence(result.target, AttackType.weapon)).addListener(future);
        }
        return future;
    }
}
