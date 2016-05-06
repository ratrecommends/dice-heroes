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

package com.vlaaad.dice.game.world.util;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BinaryHeap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.world.World;

import java.util.LinkedList;
import java.util.List;

/**
 * Created 08.10.13 by vlaaad
 */
public class AStar {

    public static class Node extends BinaryHeap.Node implements Pool.Poolable {

        private float f;
        public int x;
        public int y;
        private Node parent;
        private boolean closed;
        private int g;
        private boolean visited;
        private float h;

        public Node() {
            super(0);
        }

        public Node set(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        @Override
        public String toString() {
            return "{" + y + ", " + x + '}';
        }

        @Override
        public void reset() {
            parent = null;
            closed = false;
            visited = false;
            g = 0;
            h = 0;
            f = 0;
        }
    }

    private static Grid2D<Node> nodes = new Grid2D<Node>();

    private AStar() {
    }

    private static Node getNode(int x, int y) {
        Node n = nodes.get(x, y);
        if (n == null) {
            n = Pools.obtain(Node.class).set(x, y);
            nodes.put(x, y, n);
        }
        return n;
    }

    public static Array<Coordinate> search(World grid, WorldObject startObject, WorldObject endObject, boolean near) {
        Node start = Pools.obtain(Node.class).set(startObject.getX(), startObject.getY());
        Node end = Pools.obtain(Node.class).set(endObject.getX(), endObject.getY());
        nodes.put(start.x, start.y, start);
        nodes.put(end.x, end.y, end);

        BinaryHeap<Node> openHeap = new BinaryHeap<Node>();
        openHeap.add(start);

        List<Node> neighbours = new LinkedList<Node>();
        Node nearNode = null;
        boolean searchNear = true;
        while (openHeap.size > 0) {
            Node current = openHeap.pop();
            int cx = current.x - end.x;
            int cy = current.y - end.y;
            if (cx == 0 && cy == 0) {
                Array<Coordinate> result = new Array<Coordinate>();
                while (current.parent != null) {
                    result.add(Coordinate.obtain(current.x, current.y));
                    current = current.parent;
                }
                result.reverse();
                cleanUp();
                return result;
            }
            if (near && searchNear && !isValid(grid, end.x, end.y)) {
                nearNode = current;
                if (Math.abs(cx) <= 1 && Math.abs(cy) <= 1) {
                    searchNear = false;
                }
            }

            current.closed = true;
            neighbours.clear();
            int gScore = current.g + 1;
            addNeighbours(grid, neighbours, current);
            for (Node neighbour : neighbours) {
                if (neighbour.closed)
                    continue;
                if (!neighbour.visited || gScore < neighbour.g) {

                    neighbour.parent = current;
                    int dx = neighbour.x - end.x;
                    int dy = neighbour.y - end.y;
                    neighbour.h = (float) (Math.sqrt(dx * dx + dy * dy));
                    neighbour.g = gScore;
                    neighbour.f = neighbour.g + neighbour.h;
                    if (!neighbour.visited) {
                        openHeap.add(neighbour);
                    }
                    openHeap.setValue(neighbour, neighbour.f);
                    neighbour.visited = true;
                }
            }
        }
        if (nearNode != null && !searchNear) {
            Array<Coordinate> result = new Array<Coordinate>();
            while (nearNode.parent != null) {
                result.add(Coordinate.obtain(nearNode.x, nearNode.y));
                nearNode = nearNode.parent;
            }
            result.reverse();
            cleanUp();
            return result;
        }
        cleanUp();
        return null;
    }

    private static void cleanUp() {
        for (Node n : nodes.values()) {
            Pools.free(n);
        }
        nodes.clear();
    }

    private static void addNeighbours(World grid, List<Node> neighbours, Node current) {
        addNeighbourIfFits(grid, neighbours, current.x - 1, current.y - 1);
        addNeighbourIfFits(grid, neighbours, current.x - 1, current.y);
        addNeighbourIfFits(grid, neighbours, current.x - 1, current.y + 1);

        addNeighbourIfFits(grid, neighbours, current.x, current.y - 1);
        addNeighbourIfFits(grid, neighbours, current.x, current.y + 1);

        addNeighbourIfFits(grid, neighbours, current.x + 1, current.y - 1);
        addNeighbourIfFits(grid, neighbours, current.x + 1, current.y);
        addNeighbourIfFits(grid, neighbours, current.x + 1, current.y + 1);
    }

    private static boolean isValid(World grid, int x, int y) {
        return grid.canStepTo(x, y);
    }

    private static void addNeighbourIfFits(World grid, List<Node> neighbours, int x, int y) {
        //there is no wall
        if (isValid(grid, x, y))
            neighbours.add(getNode(x, y));
    }
}
