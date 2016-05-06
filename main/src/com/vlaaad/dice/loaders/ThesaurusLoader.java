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

package com.vlaaad.dice.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.config.thesaurus.ThesaurusData;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * Created 27.10.13 by vlaaad
 */
public class ThesaurusLoader extends AsynchronousAssetLoader<Thesaurus, ThesaurusLoader.ThesaurusParameter> {

    private Thesaurus thesaurus;

    public ThesaurusLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override public void loadAsync(AssetManager manager, String fileName, FileHandle file, ThesaurusLoader.ThesaurusParameter parameter) {
        Constructor constructor = new Constructor(ThesaurusData.class);
        Yaml yaml = new Yaml(constructor);
        ObjectMap<String, ThesaurusData> data = new ObjectMap<String, ThesaurusData>();
        for (Object o : yaml.loadAll(resolve(fileName).read())) {
            ThesaurusData description = (ThesaurusData) o;
            data.put(description.key, description);
        }
        if (parameter != null && parameter.other.length > 0) {
            for (String depName : parameter.other) {
                Thesaurus dep = manager.get(depName);
                data.putAll(dep.data);
            }
        }
        thesaurus = new Thesaurus(data);
    }

    @Override public Thesaurus loadSync(AssetManager manager, String fileName, FileHandle file, ThesaurusLoader.ThesaurusParameter parameter) {
        return thesaurus;
    }

    @Override public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, ThesaurusLoader.ThesaurusParameter parameter) {
        if (parameter != null && parameter.other.length > 0) {
            Array<AssetDescriptor> descriptors = new Array<AssetDescriptor>();
            for (String depName : parameter.other) {
                descriptors.add(new AssetDescriptor<Thesaurus>(depName, Thesaurus.class));
            }
            return descriptors;
        }
        return null;
    }

    public static class ThesaurusParameter extends AssetLoaderParameters<Thesaurus> {
        private final String[] other;
        public ThesaurusParameter(String... other) {
            this.other = other;
        }
    }

}
