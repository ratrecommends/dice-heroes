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

package com.vlaaad.dice.game.config.thesaurus;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.vlaaad.dice.Config;

/**
 * Created 27.10.13 by vlaaad
 */
public class Thesaurus {
    public static final ObjectMap<String, String> EMPTY = new ObjectMap<String, String>();

    public final ObjectMap<String, ThesaurusData> data;
    private String language = Config.preferences == null ? "en" : Config.preferences.getLanguage();
    private final ObjectMap<Localizable, LocalizationData> registrations = new ObjectMap<Localizable, LocalizationData>();
    private final Array<ThesaurusData> values;

    public Thesaurus(ObjectMap<String, ThesaurusData> data) {
        this.data = data;
        values = new Array<ThesaurusData>(0);
    }

    public Array<ThesaurusData> values() {
        if (data.size > 0 && values.size == 0) {
            values.addAll(data.values().toArray());
        }
        return values;
    }

    // language

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        if (language == null)
            throw new IllegalArgumentException("language can't be null");
        if (language.equals(this.language))
            return;
        this.language = language;
        for (Localizable label : registrations.keys()) {
            LocalizationData registration = registrations.get(label);
            label.localize(localize(registration.key, registration.params));
        }
    }

    public ThesaurusData getData(String locKey) {
        return data.get(locKey);
    }

    // registrations


    public void register(Localizable localizable, String key) {
        register(localizable, key, EMPTY);
    }

    public void register(Localizable localizable, String key, ObjectMap<String, String> params) {
        LocalizationData registration = registrations.get(localizable);
        if (registration != null) {
            if (registration.key.equals(key) && registration.params.equals(params))
                return;
            registration.key = key;
            registration.params = params == null ? EMPTY : params;
            localizable.localize(localize(key, params));
            return;
        }
        registrations.put(localizable, new LocalizationData(key, params == null ? EMPTY : params));
        localizable.localize(localize(key, params));
    }

    public void unregister(Localizable localizable) {
        registrations.remove(localizable);
    }

    // localization

    public String localize(LocalizationData data) {
        return localize(data.key, data.params == null ? EMPTY : data.params);
    }

    public String localize(String key) {
        return localize(key, EMPTY);
    }

    public String localize(String key, ObjectMap<String, String> params) {
        if (params == null)
            params = EMPTY;
        ThesaurusData thesaurusData = data.get(key);
        String localized = getLocalized(thesaurusData);
        if (localized == null) {
            if (!key.contains(" ") && key.contains(".") && !key.contains("{")) {
                String fallback = key.split("\\.")[0];
                return localize(fallback);
            }
            localized = key;
        }
        while (localized.contains("{")) {
            int end = localized.indexOf('}');
            if (end == -1)
                return localized;
            int start = localized.indexOf('{');
            String replaceKey = localized.substring(start + 1, end);
            String replaceValue = params.get(replaceKey);
            if (replaceValue == null) {
                replaceValue = localize(replaceKey, params);
            } else {
                replaceValue = localize(replaceValue, params);
            }
            localized = localized.substring(0, start) + replaceValue + localized.substring(end + 1);
        }
        return localized;
    }

    private String getLocalized(ThesaurusData data) {
        return getLocalized(data, language);
    }

    private String getLocalized(ThesaurusData data, String language) {
        if (data == null)
            return null;
        if (language.equals("ru")) {
            return data.ru == null ? data.en : data.ru;
        } else {
            return data.en;
        }
    }

    public static Params params() {
        return new Params();
    }

    public boolean keyExists(String key) {
        return data.containsKey(key);
    }

    public static LocalizationData data() {
        return new LocalizationData();
    }

    public static class Params extends ObjectMap<String, String> {
        public Params with(String key, String value) {
            put(key, value);
            return this;
        }
        public Params withAll(ObjectMap<String, String> params) {
            putAll(params);
            return this;
        }
    }

    public static class LocalizationData {
        public String key;
        public ObjectMap<String, String> params;

        public LocalizationData() {
        }

        private LocalizationData(String key, ObjectMap<String, String> params) {
            this.key = key;
            this.params = params;
        }
    }

    public static class Keys {

        public static String enumeration(int count) {
            if (count < 0)
                throw new IllegalArgumentException();
            if (count == 0)
                return "";
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < count; i++) {
                builder.append('{').append(i).append('}');
                if (i == count - 2) {
                    builder.append("{enum-and}");
                } else if (i != count - 1) {
                    builder.append("{enum-comma}");
                }
            }
            return builder.toString();
        }
    }

    public static class Util {
        public static <T> String enumerate(Thesaurus thesaurus, Array<T> elements, Stringifier<T> stringifier) {
            String key = Keys.enumeration(elements.size);
            Params params = Thesaurus.params();
            for (int i = 0, n = elements.size; i < n; i++) {
                params.put(String.valueOf(i), stringifier.toString(elements.get(i)));
            }
            return thesaurus.localize(key, params);
        }

        public static <T> String enumerate(Thesaurus thesaurus, Thesaurus.Params params, Array<T> elements, Stringifier<T> stringifier) {
            String key = Keys.enumeration(elements.size);
            for (int i = 0, n = elements.size; i < n; i++) {
                params.put(String.valueOf(i), stringifier.toString(elements.get(i)));
            }
            return thesaurus.localize(key, params);
        }

        /**
         * one -> coin / монету
         * many-one -> coins / монету
         * many-two -> coins / монеты
         * many-five -> coins / монет
         */
        public static String countForm(int count) {
            count = Math.abs(count);
            if (count == 1)
                return "one";
            count = count % 100;
            int dozens = count % 10;
            if (count > 10 && count < 20) return "many-five";
            if (dozens > 1 && dozens < 5) return "many-two";
            if (dozens == 1) return "many-one";
            return "many-five";
        }

        public static interface Stringifier<T> {
            public String toString(T t);
        }
    }
}
