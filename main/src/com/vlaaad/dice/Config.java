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

package com.vlaaad.dice;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.util.Function;
import com.vlaaad.dice.achievements.AchievementManager;
import com.vlaaad.dice.api.IMobileApi;
import com.vlaaad.dice.game.config.abilities.Abilities;
import com.vlaaad.dice.game.config.items.Items;
import com.vlaaad.dice.game.config.levels.Levels;
import com.vlaaad.dice.game.config.particles.Particles;
import com.vlaaad.dice.game.config.professions.ProfessionDescription;
import com.vlaaad.dice.game.config.professions.Professions;
import com.vlaaad.dice.game.config.pvp.PvpModes;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.util.DicePreferences;

import java.util.Map;

/**
 * Created 06.10.13 by vlaaad
 */
public class Config {

    public static final Json json = new Json();
    static {
        json.setOutputType(JsonWriter.OutputType.minimal);
        json.setTypeName("type");
        json.setUsePrototypes(false);
        json.setIgnoreUnknownFields(false);
    }

    public static AssetManager assetManager;
    public static ShapeRenderer shapeRenderer;
    public static Skin skin;
    public static Professions professions;
    public static Abilities abilities;
    public static Levels levels;
    public static Items items;
    public static DicePreferences preferences;
    public static IMobileApi mobileApi;
    public static Thesaurus thesaurus;

    private static final ObjectMap<String, Array<TextureAtlas.AtlasRegion>> foundRegions = new ObjectMap<String, Array<TextureAtlas.AtlasRegion>>();
    public static Map worldMapParams;
    public static Particles particles;
    public static AchievementManager achievements;
    public static PvpModes pvpModes;

    public static Array<TextureAtlas.AtlasRegion> findRegions(String regionName) {
        Array<TextureAtlas.AtlasRegion> result = foundRegions.get(regionName);
        if (result == null) {
            result = skin.getAtlas().findRegions(regionName);
            foundRegions.put(regionName, result);
        }
        return result;
    }

    public static void clearRegions() {
        foundRegions.clear();
    }
}
