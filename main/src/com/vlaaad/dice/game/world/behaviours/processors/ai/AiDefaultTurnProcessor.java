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

package com.vlaaad.dice.game.world.behaviours.processors.ai;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BinaryHeap;
import com.vlaaad.common.util.Function;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.behaviours.RequestProcessor;
import com.vlaaad.dice.game.world.behaviours.params.TurnParams;
import com.vlaaad.dice.game.world.behaviours.responces.TurnResponse;
import com.vlaaad.dice.game.world.view.ViewScroller;

/**
 * Created 15.01.14 by vlaaad
 */
public class AiDefaultTurnProcessor extends RequestProcessor<TurnResponse, TurnParams> implements CellValueCalculator {


    @Override public int preProcess(TurnParams params) {
        return 1;
    }

    @Override public IFuture<TurnResponse> process(final TurnParams params) {
        final Future<TurnResponse> future = new Future<TurnResponse>();
        params.creature.world.stage.addAction(Actions.delay(ViewScroller.CENTER_ON_TIME, Actions.run(new Runnable() {
            @Override public void run() {
                future.happen(new TurnResponse<Grid2D.Coordinate>(
                    TurnResponse.TurnAction.MOVE,
                    selectMoveTarget(params.creature, params.availableCells, AiDefaultTurnProcessor.this)
                ));
            }
        })));
        return future;
    }

    @Override public float getValue(Creature creature, int x, int y) {
        if (creature.getX() == x && creature.getY() == y)
            return 1;
        return 0;
    }


    public static Grid2D.Coordinate selectMoveTarget(Creature creature, Array<Grid2D.Coordinate> coordinates, CellValueCalculator calculator) {
        BinaryHeap<Node> heap = new BinaryHeap<Node>(9, true);
        for (Grid2D.Coordinate c : coordinates) {
            float value = calculator.getValue(creature, c.x(), c.y());
            heap.add(new Node(c.x(), c.y(), value));
        }

        if (heap.size == 0)
            return new Grid2D.Coordinate(creature.getX(), creature.getY());
        Node node = heap.peek();
        return new Grid2D.Coordinate(node.x, node.y);
    }

    public static <T> T selectBest(Array<T> elements, Function<T, Float> calc) {
        BinaryHeap<TypedNode<T>> heap = new BinaryHeap<TypedNode<T>>(9, true);
        for (T t : elements) {
            float value = calc.apply(t);
            heap.add(new TypedNode<T>(t, value));
        }
        if (heap.size == 0)
            return elements.first();
        return heap.peek().t;
    }

    protected static class TypedNode<T> extends BinaryHeap.Node {
        private final T t;

        public TypedNode(T t, float value) {
            super(value);
            this.t = t;
        }
    }

    protected static class Node extends BinaryHeap.Node {

        private final int x;
        private final int y;

        public Node(int x, int y, float value) {
            super(value);
            this.x = x;
            this.y = y;
        }
    }
}
