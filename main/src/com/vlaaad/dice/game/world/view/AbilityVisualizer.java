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

package com.vlaaad.dice.game.world.view;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Predicate;
import com.vlaaad.common.util.Predicates;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.IAbilityOwner;
import com.vlaaad.dice.game.config.abilities.Ability;

public class AbilityVisualizer<T extends IAbilityOwner> implements IVisualizer<T> {

    public static <T extends IAbilityOwner> AbilityVisualizer<T> withDefault(IVisualizer<? super T> defaultVisualizer) {
        return new AbilityVisualizer<T>(defaultVisualizer);
    }

    public static <T extends IAbilityOwner> AbilityVisualizer<T> make() {
        return new AbilityVisualizer<T>();
    }

    private final Array<AbilityChecker<T>> data = new Array<AbilityChecker<T>>();
    private final IVisualizer<? super T> defaultVisualizer;

    public AbilityVisualizer() {
        this(Visualizers.empty());
    }

    public AbilityVisualizer(IVisualizer<? super T> defaultVisualizer) {
        this.defaultVisualizer = defaultVisualizer;
    }

    public AbilityVisualizer<T> with(AbilityChecker<T> checker) {
        data.add(checker);
        return this;
    }

    public AbilityVisualizer<T> with(String abilityName, IVisualizer<? super T> visualizer) {
        return with(Config.abilities.get(abilityName), visualizer);
    }

    public AbilityVisualizer<T> with(Ability ability, IVisualizer<? super T> visualizer) {
        return with(AbilityChecker.make(Predicates.eq(ability), visualizer));
    }

    @Override public IFuture<Void> visualize(T t) {
        for (AbilityChecker<T> checker : data) {
            if (checker.predicate.evaluate(t.getAbility()))
                return checker.visualizer.visualize(t);
        }
        return defaultVisualizer.visualize(t);
    }

    public static final class AbilityChecker<T extends IAbilityOwner> {

        private final Predicate<Ability> predicate;
        private final IVisualizer<? super T> visualizer;

        private AbilityChecker(Predicate<Ability> predicate, IVisualizer<? super T> visualizer) {
            this.predicate = predicate;
            this.visualizer = visualizer;
        }

        public static <T extends IAbilityOwner> AbilityChecker<T> make(Predicate<Ability> predicate, IVisualizer<? super T> visualizer) {
            return new AbilityChecker<T>(predicate, visualizer);
        }

        public static StartsWith startsWith(String str) {
            return new StartsWith(str);
        }

        public static class StartsWith implements Predicate<Ability> {
            private final String str;

            private StartsWith(String str) {
                this.str = str;
            }

            @Override public boolean evaluate(Ability arg0) {
                return arg0.name.startsWith(str);
            }
        }
    }
}
