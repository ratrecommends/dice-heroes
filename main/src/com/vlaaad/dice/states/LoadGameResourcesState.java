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

package com.vlaaad.dice.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.gdx.State;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Abilities;
import com.vlaaad.dice.game.config.items.Items;
import com.vlaaad.dice.game.config.levels.Levels;
import com.vlaaad.dice.game.config.particles.Particles;
import com.vlaaad.dice.game.config.professions.Professions;
import com.vlaaad.dice.game.config.pvp.PvpModes;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.loaders.*;
import com.vlaaad.dice.managers.SoundManager;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created 06.10.13 by vlaaad
 */
public class LoadGameResourcesState extends State {

    private static final float w = 20;
    private static final float h = 28;
    private static final float pad = 10;
    private static final float progressWidth = w * 3 + pad * 2;
    private static final float progressHeight = 3;
    private static final float progressPad = 1;
    private float progressY = 4;
    private static final Color barBackgroundColor = new Color(0.85f, 0.85f, 0.85f, 1f);
    private static final Color barProgressColor = new Color(1f, 1f, 1f, 1f);

    private boolean loaded;
    private final Callback callback;
    private float stateTime = 0;
    private Animation animation;
    private float displayedProgress = 0;

    private Actor actor = new Actor() {
        @Override public void draw(Batch batch, float parentAlpha) {
            batch.draw(
                animation.getKeyFrame(Math.max(stateTime, 0)),
                stage.getWidth() / 2f - w / 2f - w - pad,
                stage.getHeight() / 2f - h / 2f
            );
            batch.draw(
                animation.getKeyFrame(Math.max(stateTime - 0.5f, 0)),
                stage.getWidth() / 2f - w / 2,
                stage.getHeight() / 2f - h / 2f
            );
            batch.draw(
                animation.getKeyFrame(Math.max(stateTime - 1f, 0)),
                stage.getWidth() / 2f + w / 2 + pad,
                stage.getHeight() / 2f - h / 2f
            );
        }
    };

    public static interface Callback {
        void onResourcesLoaded();
    }

    public LoadGameResourcesState(Callback callback) {
        this.callback = callback;
    }

    private Texture texture;

    @Override
    protected void init() {
        texture = new Texture(Gdx.files.internal("loading.png"));
        TextureRegion[][] regions = TextureRegion.split(texture, 20, 28);
        TextureRegion[] frames = new TextureRegion[5];
        System.arraycopy(regions[0], 0, frames, 0, 5);
        animation = new Animation(0.1f, frames);
        stage.addActor(actor);
        progressY = stage.getHeight() / 2f - h / 2f - progressHeight - 4;

        FileHandleResolver resolver = new InternalFileHandleResolver();
        Config.assetManager.setLoader(Professions.class, new ProfessionsLoader(resolver));
        Config.assetManager.setLoader(Abilities.class, new AbilitiesLoader(resolver));
        Config.assetManager.setLoader(Levels.class, new LevelsLoader(resolver));
        Config.assetManager.setLoader(Items.class, new ItemsLoader(resolver));
        Config.assetManager.setLoader(Thesaurus.class, new ThesaurusLoader(resolver));
        Config.assetManager.setLoader(Map.class, new YamlMapLoader(resolver));
        Config.assetManager.setLoader(Array.class, new YamlAllLoader(resolver));
        Config.assetManager.setLoader(PvpModes.class, new PvpModesLoader(resolver));

        Config.assetManager.load("gfx.json", Skin.class);
        Config.assetManager.load("items.yml", Items.class);
        Config.assetManager.load("abilities.yml", Abilities.class);
        Config.assetManager.load("professions.yml", Professions.class);
        Config.assetManager.load("levels.yml", Levels.class);
        Config.assetManager.load("thesaurus.yml", Thesaurus.class, new ThesaurusLoader.ThesaurusParameter("names.yml"));
        Config.assetManager.load("world-map.yml", Map.class);
        Config.assetManager.load("world-map.atlas", TextureAtlas.class);
        Config.assetManager.load("achievements.yml", Array.class);
        Config.assetManager.load("pvp-modes.yml", PvpModes.class);

        for (FileHandle file : Gdx.files.internal("sfx").list()) {
            Config.assetManager.load(file.path(), Sound.class);
        }
        for (FileHandle file : Gdx.files.internal("music").list()) {
            Config.assetManager.load(file.path(), Music.class);
        }
        ParticleEffectLoader.ParticleEffectParameter particleParameter = new ParticleEffectLoader.ParticleEffectParameter();
        particleParameter.atlasFile = "gfx.atlas";
        for (FileHandle file : Gdx.files.internal("particles").list()) {
            Config.assetManager.load(file.path(), ParticleEffect.class, particleParameter);
        }
    }

    @Override
    protected void resume(boolean isStateChange) {

    }

    @Override
    protected void render(float delta) {
        stateTime += delta;
        if (stateTime >= 1.7f) {
            stateTime -= 1.7f;
            if (loaded) {
                callback.onResourcesLoaded();
            }
        }
        if (!loaded && Config.assetManager.update(33)) {
            loaded = true;
            Config.thesaurus = Config.assetManager.get("thesaurus.yml", Thesaurus.class);
            Config.skin = Config.assetManager.get("gfx.json", Skin.class);
            Config.worldMapParams = Config.assetManager.get("world-map.yml", Map.class);
            Drawable back = Config.skin.getDrawable("ui-slider-background");
            back.setLeftWidth(2);
            back.setRightWidth(2);
            ObjectMap<String, Sound> sounds = new ObjectMap<String, Sound>();
            for (FileHandle file : Gdx.files.internal("sfx").list()) {
                Sound sound = Config.assetManager.get(file.path());
                sounds.put(file.nameWithoutExtension(), sound);
            }
            SoundManager.instance.putSounds(sounds);
            ObjectMap<String, Music> musics = new ObjectMap<String, Music>();
            for (FileHandle file : Gdx.files.internal("music").list()) {
                Music music = Config.assetManager.get(file.path());
                musics.put(file.nameWithoutExtension(), music);
            }
            SoundManager.instance.putMusics(musics);
            ObjectMap<String, ParticleEffectPool> particles = new ObjectMap<String, ParticleEffectPool>();
            for (FileHandle file : Gdx.files.internal("particles").list()) {
                ParticleEffect effect = Config.assetManager.get(file.path());
                ParticleEffectPool pool = new ParticleEffectPool(effect, 1, 6);
                particles.put(file.nameWithoutExtension(), pool);
            }
            Config.particles = new Particles(particles);
        }
        Config.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Config.shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        float x = (stage.getWidth() - progressWidth) / 2f;
        displayedProgress = displayedProgress + (Config.assetManager.getProgress() - displayedProgress) * 0.5f;
        Config.shapeRenderer.setColor(barBackgroundColor);
        Config.shapeRenderer.rect(x, progressY, progressWidth, progressHeight);
        Config.shapeRenderer.setColor(barProgressColor);
        Config.shapeRenderer.rect(x + progressPad, progressY + progressPad, (progressWidth - progressPad * 2) * displayedProgress, progressHeight - progressPad * 2);
        Config.shapeRenderer.end();
    }

    @Override
    protected void pause(boolean isStateChange, Stage stage) {

    }

    @Override
    protected Color getBackgroundColor() {
        return Color.WHITE;
    }

    @Override
    protected boolean disposeOnSwitch() {
        return true;
    }

    @Override
    protected void dispose(boolean isStateChange, Stage stage) {
        texture.dispose();
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                LoadGameResourcesState.this.stage.dispose();
            }
        });
    }
}
