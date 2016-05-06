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

package com.vlaaad.dice.game.config.levels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.config.items.drop.Drop;
import com.vlaaad.dice.game.config.items.drop.Range;
import com.vlaaad.dice.game.config.items.drop.RangeItemCount;
import com.vlaaad.dice.game.config.items.drop.Roll;
import com.vlaaad.dice.game.config.rewards.Reward;
import com.vlaaad.dice.game.config.rewards.RewardFactory;
import com.vlaaad.dice.game.world.players.Fraction;
import com.vlaaad.dice.game.world.players.FractionRelation;

import java.util.*;

/**
 * Created 06.10.13 by vlaaad
 */
public class LevelDescription extends BaseLevelDescription {

    public final int width;
    public final int height;
    public final Array<String> backgroundObjects = new Array<String>();
    public Color backgroundColor;

    public final ObjectSet<Fraction> fractions = new ObjectSet<Fraction>();
    public final ObjectMap<Fraction, ObjectMap<Fraction, FractionRelation>> relations = new ObjectMap<Fraction, ObjectMap<Fraction, FractionRelation>>();

    private ObjectMap<LevelElementType, Grid2D> map = new ObjectMap<LevelElementType, Grid2D>();

    public LevelDescription(Map data) {
        super(data);
        Map<String, HashMap<String, Object>> shortcuts = MapHelper.get(data, "shortcuts");
        Map<String, Object> defaults = MapHelper.get(data, "defaults");
        List<ArrayList<String>> mapInfo = MapHelper.get(data, "map");
        List<String> fractionsInfo = MapHelper.get(data, "fractions");
        if (fractionsInfo != null) {
            for (String name : fractionsInfo) {
                fractions.add(Fraction.valueOf(name));
            }
        }
        Map<String, String> relationsInfo = MapHelper.get(data, "relations");
        if (relationsInfo != null) {
            for (Fraction fraction : fractions) {
                ObjectMap<Fraction, FractionRelation> m = new ObjectMap<Fraction, FractionRelation>();
                relations.put(fraction, m);
                for (String key : relationsInfo.keySet()) {
                    String[] names = key.split(":");
                    boolean interested = false;
                    for (String name : names) {
                        if (name.equals(fraction.name)) {
                            interested = true;
                            break;
                        }
                    }
                    if (interested) {
                        FractionRelation relation = FractionRelation.valueOf(relationsInfo.get(key));
                        for (String name : names) {
                            if (!name.equals(fraction.name)) {
                                m.put(Fraction.valueOf(name), relation);
                            }
                        }
                    }
                }
            }
        }
        if (mapInfo != null) {
            int w = 0;
            int h = mapInfo.size();
            for (int i = 0; i < mapInfo.size(); i++) {
                List<String> row = mapInfo.get(i);
                w = Math.max(w, row.size());
                for (int j = 0; j < row.size(); j++) {
                    String mark = row.get(j);
                    if (mark == null)
                        continue;
                    Map<String, Object> info = shortcuts.get(mark);
                    if (defaults != null) {
                        for (String elementType : defaults.keySet()) {
                            if (info != null && info.containsKey(elementType))
                                continue;
                            LevelElementType<?> type = LevelElementType.valueOf(elementType);
                            add(j, mapInfo.size() - i - 1, type, defaults.get(elementType));
                        }
                    }
                    if (info == null && !mark.equals("_"))
                        throw new IllegalStateException("unknown mark " + mark + " in map " + name);
                    if (info == null)
                        continue;
                    for (String elementType : info.keySet()) {
                        LevelElementType<?> type = LevelElementType.valueOf(elementType);
                        add(j, mapInfo.size() - i - 1, type, info.get(elementType));
                    }
                }
            }
            width = w;
            height = h;
        } else {
            width = 0;
            height = 0;
        }

        HashMap<String, Object> background = MapHelper.get(data, "background");
        if (background != null) {
            ArrayList<String> objects = MapHelper.get(background, "objects");
            if (objects != null) {
                for (String s : objects) {
                    backgroundObjects.add("background-object/" + s);
                }
            }
            Object colorObject = background.get("color");
            if (colorObject instanceof Map) {
                Map color = (Map) colorObject;
                this.backgroundColor = new Color(
                    MapHelper.get(color, "r", Numbers.ZERO).floatValue() / 255f,
                    MapHelper.get(color, "g", Numbers.ZERO).floatValue() / 255f,
                    MapHelper.get(color, "b", Numbers.ZERO).floatValue() / 255f,
                    MapHelper.get(color, "a", Numbers.TWO_HUNDRED_FIFTY_FIVE).floatValue() / 255f
                );
            } else if (colorObject instanceof String) {
                this.backgroundColor = Color.valueOf((String) colorObject);
            }

        }
    }

    @SuppressWarnings("unchecked")
    private <T> void add(int x, int y, LevelElementType<T> type, Object rawValue) {
        Grid2D<T> grid = map.get(type);
        if (grid == null) {
            grid = new Grid2D<T>();
            map.put(type, grid);
        }
        grid.put(x, y, type.decoder.decode(rawValue));
    }

    @SuppressWarnings("unchecked")
    public <T> Set<Map.Entry<Grid2D.Coordinate, T>> getElements(LevelElementType<T> type) {
        Grid2D<T> grid2D = map.get(type);
        if (grid2D == null)
            return Collections.emptySet();
        return grid2D.entries();
    }

    @SuppressWarnings("unchecked")
    public <T> Set<Grid2D.Coordinate> getCoordinates(LevelElementType<T> type) {
        Grid2D<T> grid2D = map.get(type);
        if (grid2D == null)
            return Collections.emptySet();
        return grid2D.keys();
    }

    public boolean exists(LevelElementType type, int x, int y) {
        Grid2D grid2D = map.get(type);
        return grid2D != null && grid2D.hasAt(x, y);
    }

    @SuppressWarnings("unchecked")
    public <T> T getElement(LevelElementType<T> type, int x, int y) {
        Grid2D grid2D = map.get(type);
        return grid2D == null ? null : (T) grid2D.get(x, y);
    }

    @Override public String toString() {
        return name;
    }
}
