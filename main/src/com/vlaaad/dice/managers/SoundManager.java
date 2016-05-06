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

package com.vlaaad.dice.managers;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.vlaaad.common.util.Logger;

/**
 * Created 22.10.13 by vlaaad
 */
public class SoundManager {

    public static final SoundManager instance = new SoundManager();
    private boolean usesMusic = true;

    private SoundManager() {
    }


    private final ObjectMap<String, Sound> sounds = new ObjectMap<String, Sound>();
    private float volume = 1f;
    private final ObjectMap<String, Music> musics = new ObjectMap<String, Music>();
    private final ObjectMap<Music, Action> actions = new ObjectMap<Music, Action>();
    private final ObjectSet<Music> playingMusics = new ObjectSet<Music>();
    private final ObjectSet<Music> disabledMusics = new ObjectSet<Music>();


    public void putSounds(ObjectMap<String, Sound> sounds) {
        this.sounds.putAll(sounds);
    }


    public void putMusics(ObjectMap<String, Music> musics) {
        this.musics.putAll(musics);
    }

    public long playSound(String soundName) {
        Sound sound = sounds.get(soundName);
        if (sound == null) {
            Logger.error("there is no sound for " + soundName);
            return -1;
        }
        return sound.play(volume);
    }

    public void dispose() {
        sounds.clear();
        musics.clear();
        actions.clear();
        disabledMusics.clear();
        playingMusics.clear();
        usesMusic = true;
        volume = 1f;
    }

    public boolean soundExists(String name) {
        return sounds.containsKey(name);
    }

    public boolean musicExists(String name) {
        return musics.containsKey(name);
    }

    public void playMusicAsSound(String name) {
        Music music = musics.get(name);
        if (music == null) {
            Logger.error("there is no music for " + name);
            return;
        }
        music.setVolume(volume);
        music.play();
    }

    public void stopMusic(String name) {
        Music music = musics.get(name);
        if (music == null) {
            Logger.error("there is no music for " + name);
            return;
        }
        music.stop();
        playingMusics.remove(music);
        disabledMusics.remove(music);
    }


    public void playFirstExistingMusicAsSound(String... names) {
        for (String name : names) {
            if (musicExists(name)) {
                playMusicAsSound(name);
                return;
            }
        }
    }

    public void playFirstExistingSound(String... soundNames) {
        for (String soundName : soundNames) {
            if (soundExists(soundName)) {
                playSound(soundName);
                return;
            }
        }
    }

    public void playSoundIfExists(String soundName) {
        if (soundExists(soundName))
            playSound(soundName);
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        if (volume < 0)
            volume = 0;
        if (volume > 1)
            volume = 1;
        this.volume = volume;
        for (Music music : musics.values()) {
            music.setVolume(volume);
        }
    }

    public void playMusicBeautifully(String name, Stage stage) {
        final Music music = musics.get(name);
        if (music == null) {
            Logger.error("there is no music for " + name);
            return;
        }
        music.setVolume(0);
        if (!usesMusic) {
            disabledMusics.add(music);
        } else {
            music.play();
        }
        music.setLooping(true);
        playingMusics.add(music);
        Action action = new TemporalAction(5f, Interpolation.linear) {
            @Override protected void update(float percent) {
                music.setVolume(percent * volume);
            }
        };
        stage.addAction(action);
        replaceAction(music, action);
    }

    private void replaceAction(Music music, Action action) {
        Action prev = actions.put(music, action);
        if (prev != null && prev.getActor() != null)
            prev.getActor().removeAction(prev);
    }

    public void stopMusicBeautifully(String name, Stage stage) {
        final Music music = musics.get(name);
        if (music == null) {
            Logger.error("there is no music for " + name);
            return;
        }
        final float initialVolume = music.getVolume();
        Action action = new TemporalAction(2f, Interpolation.linear) {
            @Override protected void update(float percent) {
                music.setVolume(initialVolume - percent * initialVolume);
            }

            @Override protected void end() {
                music.stop();
                playingMusics.remove(music);
                disabledMusics.remove(music);
            }
        };
        stage.addAction(action);
        replaceAction(music, action);
    }

    public void setUsesMusic(boolean usesMusic) {
        if (usesMusic == this.usesMusic)
            return;
        this.usesMusic = usesMusic;
        if (usesMusic) {
            for (Music music : disabledMusics) {
                music.play();
                playingMusics.add(music);
            }
            disabledMusics.clear();
        } else {
            for (Music music : playingMusics) {
                disabledMusics.add(music);
                music.pause();
            }
            playingMusics.clear();
        }
    }
}
