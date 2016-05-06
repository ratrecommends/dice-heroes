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

package com.vlaaad.dice.ui.components;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

/**
 * Created 12.11.13 by vlaaad
 */
public class DiePaneListener implements EventListener {
    @Override public boolean handle(Event event) {
        return event instanceof PaneEvent && notify((PaneEvent) event);
    }

    private boolean notify(PaneEvent event) {
        switch (event.type) {
            case minimize:
                onMinimize(event);
                return true;
            case minimized:
                onMinimized(event);
                return true;
            case maximize:
                onMaximize(event);
                return true;
            case maximized:
                onMaximized(event);
                return true;
            default:
                return false;
        }
    }

    protected void onMaximized(PaneEvent event) {}

    protected void onMaximize(PaneEvent event) {}

    protected void onMinimized(PaneEvent event) {}

    protected void onMinimize(PaneEvent event) {}

    public static class PaneEvent extends Event {
        private EventType type;
        private DiePane pane;

        public EventType getType() { return type; }

        public PaneEvent setType(EventType type) {
            this.type = type;
            return this;
        }

        public DiePane getPane() { return pane; }

        public PaneEvent setPane(DiePane pane) {
            this.pane = pane;
            return this;
        }

        @Override public void reset() {
            super.reset();
            pane = null;
        }
    }

    public static enum EventType {
        minimize, minimized, maximize, maximized
    }
}
