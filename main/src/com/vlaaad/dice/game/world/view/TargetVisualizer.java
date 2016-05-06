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
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.ITargetOwner;
import com.vlaaad.dice.game.config.professions.ProfessionDescription;
import com.vlaaad.dice.game.objects.Creature;

public class TargetVisualizer<T extends ITargetOwner> implements IVisualizer<T> {

    public static <T extends ITargetOwner> TargetVisualizer<T> withDefault(IVisualizer<? super T> defaultVisualizer) {
        return new TargetVisualizer<T>(defaultVisualizer);
    }

    private final IVisualizer<? super T> defaultVisualizer;
    private final Array<TargetChecker<T>> data = new Array<TargetChecker<T>>();

    public TargetVisualizer() {
        this(Visualizers.empty());
    }

    public TargetVisualizer(IVisualizer<? super T> defaultVisualizer) {
        this.defaultVisualizer = defaultVisualizer;
    }

    @Override public IFuture<Void> visualize(T t) {
        for (TargetChecker<T> checker : data) {
            if (checker.predicate.evaluate(t.getTarget()))
                return checker.visualizer.visualize(t);
        }
        return defaultVisualizer.visualize(t);
    }

    public TargetVisualizer<T> with(TargetChecker<T> checker) {
        data.add(checker);
        return this;
    }

    public static final class TargetChecker<T extends ITargetOwner> {

        private final Predicate<Creature> predicate;
        private final IVisualizer<? super T> visualizer;

        private TargetChecker(Predicate<Creature> predicate, IVisualizer<? super T> visualizer) {
            this.predicate = predicate;
            this.visualizer = visualizer;
        }

        public static <T extends ITargetOwner> TargetChecker<T> make(Predicate<Creature> predicate, IVisualizer<? super T> visualizer) {
            return new TargetChecker<T>(predicate, visualizer);
        }

        public static <T extends ITargetOwner> TargetChecker<T> withName(final String name, IVisualizer<? super T> visualizer) {
            return new TargetChecker<T>(new Predicate<Creature>() {
                @Override public boolean evaluate(Creature creature) {
                    return name.equals(creature.description.name);
                }
            }, visualizer);
        }

        public static <T extends ITargetOwner> TargetChecker<T> withProfession(final String name, IVisualizer<? super T> visualizer) {
            return withProfession(Config.professions.get(name), visualizer);
        }

        public static <T extends ITargetOwner> TargetChecker<T> withProfession(final ProfessionDescription profession, IVisualizer<? super T> visualizer) {
            return new TargetChecker<T>(new Predicate<Creature>() {
                @Override public boolean evaluate(Creature creature) {
                    return profession == creature.description.profession;
                }
            }, visualizer);
        }

    }
}
