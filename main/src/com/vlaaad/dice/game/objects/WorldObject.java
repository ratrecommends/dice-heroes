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

package com.vlaaad.dice.game.objects;

import com.badlogic.gdx.utils.ArrayMap;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.players.Player;
import com.vlaaad.dice.game.world.players.util.PlayerColors;
import com.vlaaad.dice.game.world.view.ImageSubView;
import com.vlaaad.dice.game.world.view.SubView;
import com.vlaaad.dice.game.world.view.WorldObjectView;

/**
 * Created 06.10.13 by vlaaad
 */
public class WorldObject implements Grid2D.ICell {

    public final String worldObjectName;
    public World world;
    public int viewPriority;

    public WorldObject(String worldObjectName) {
        this.worldObjectName = worldObjectName;
    }

    private int x;
    private int y;

    /**
     * should not be called directly, used by Grid2D
     */
    @Override public void setPosition(int x, int y) {
        if (this.x == x && this.y == y)
            return;
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }

    public int getY() { return y; }

    public ArrayMap<Object, SubView> createSubViews(Player viewer, PlayerColors colors) {
        ArrayMap<Object, SubView> result = new ArrayMap<Object, SubView>();
        result.put(this, new ImageSubView(worldObjectName));
        return result;
    }

    public boolean isPassable() {
        return false;
    }

    public void onRemoved() {}
    public void onAdded() {}
    public void afterAdded() {}

    public void initView(WorldObjectView view) {}
}
