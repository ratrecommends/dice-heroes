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

package com.vlaaad.common.gdx.scene2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Pools;
import com.vlaaad.dice.Config;

/**
 * Created 09.01.14 by vlaaad
 */
public class ParticleActor extends Actor {
    public final ParticleEffectPool.PooledEffect effect;

    public ParticleActor(ParticleEffectPool.PooledEffect effect) {
        this.effect = effect;
    }

    public ParticleActor(String effectName) {
        effect = Config.particles.get(effectName).obtain();
    }

    @Override public void draw(Batch batch, float parentAlpha) {
        effect.setPosition(getX(), getY());
        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
        effect.draw(batch, Gdx.graphics.getDeltaTime());
        if (effect.isComplete()) {
            ChangeListener.ChangeEvent event = Pools.obtain(ChangeListener.ChangeEvent.class);
            fire(event);
            Pools.free(event);
        }
    }

    public ParticleActor freeOnComplete() {
        addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                remove();
                effect.free();
            }
        });
        return this;
    }
}
