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

package com.vlaaad.common.ui;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

/**
 * Created 07.11.13 by vlaaad
 */
public class WindowListener implements EventListener {
    @Override public final boolean handle(Event event) {
        if (event instanceof WindowEvent) {
            WindowEvent windowEvent = (WindowEvent) event;
            return notify(windowEvent);
        }
        return false;
    }

    private boolean notify(WindowEvent event) {
        switch (event.eventType) {
            case show:
                show(event);
                break;
            case shown:
                shown(event);
                break;
            case hide:
                hide(event);
                break;
            case hidden:
                hidden(event);
                break;
        }
        return true;
    }

    protected void show(WindowEvent event) {
    }

    protected void shown(WindowEvent event) {
    }

    protected void hide(WindowEvent event) {
    }

    protected void hidden(WindowEvent event) {
    }

    public static class WindowEvent extends Event {
        private EventType eventType;
        private GameWindow gameWindow;

        public WindowEvent setEventType(EventType type) {
            eventType = type;
            return this;
        }

        public EventType getEventType() {
            return eventType;
        }

        @Override public void reset() {
            super.reset();
            eventType = null;
            gameWindow = null;
        }

        public GameWindow getWindow() {
            return gameWindow;
        }

        public WindowEvent setWindow(GameWindow gameWindow) {
            this.gameWindow = gameWindow;
            return this;
        }
    }

    public static enum EventType {
        show, shown, hide, hidden
    }
}
