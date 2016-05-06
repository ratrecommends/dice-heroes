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

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Pools;
import com.vlaaad.common.gdx.scene2d.events.ResizeListener;
import com.vlaaad.dice.Config;

/**
 * Created 11.10.13 by vlaaad
 */
public abstract class GameWindow<I> extends Group {

    private boolean shown;
    private boolean isHiding;
    private boolean initialized;
    public final Image background;
    public final Table table;

    public GameWindow() {
        background = new Image(Config.skin, "ui-window-background");
        background.getColor().a = 0;

        table = new Table(Config.skin);
        table.setTransform(true);
        table.setScale(0);
        table.getColor().a = 0f;

        addActor(background);
        addActor(table);

        if (canBeClosed()) {
            background.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    hide();
                }
            });
        }
        addListener(new EventListener() {
            @Override public boolean handle(Event event) {
                if (event instanceof InputEvent)
                    event.stop();
                return true;
            }
        });
    }

    private EventListener resizeListener = new ResizeListener() {
        @Override protected void resize() {
            float w = useParentSize() ? getParent().getWidth() : getStage().getWidth();
            float h = useParentSize() ? getParent().getHeight() : getStage().getHeight();

            table.setSize(w, h);
            background.setSize(getStage().getWidth(), getStage().getHeight());
        }
    };

    public final GameWindow<I> show(I i) {
        if (shown)
            return this;

        WindowManager.instance.add(this); //so event can be cancelled if someone wants it
        WindowListener.WindowEvent event = Pools
            .obtain(WindowListener.WindowEvent.class)
            .setEventType(WindowListener.EventType.show)
            .setWindow(this);
        boolean cancelled = fire(event);
        Pools.free(event);
        if (cancelled) {
            WindowManager.instance.remove(this);
            return this;
        }

        float w = useParentSize() ? getParent().getWidth() : getStage().getWidth();
        float h = useParentSize() ? getParent().getHeight() : getStage().getHeight();
        getStage().addListener(resizeListener);

        background.setSize(getStage().getWidth(), getStage().getHeight());
        table.setSize(w, h);
        table.setPosition(w / 2, h / 2);
        table.setScale(0);

        if (!initialized) {
            initialize();
            initialized = true;
        }
        shown = true;

        background.clearActions();
        table.clearActions();
        if (isHiding) {
            onHide();
            isHiding = false;
        }

        doShow(i);

        background.addAction(Actions.alpha(backgroundMaxAlpha(), 0.3f));
        table.addAction(Actions.sequence(
            Actions.parallel(
                Actions.scaleTo(1f, 1f, 0.4f, Interpolation.swingOut),
                Actions.moveTo(0, 0, 0.4f, Interpolation.swingOut),
                Actions.alpha(1f, 0.4f)
            ),
            Actions.run(new Runnable() {
                @Override public void run() {
                    WindowListener.WindowEvent event = Pools
                        .obtain(WindowListener.WindowEvent.class)
                        .setEventType(WindowListener.EventType.shown)
                        .setWindow(GameWindow.this);
                    fire(event);
                    Pools.free(event);
                }
            })
        ));
        return this;
    }

    protected float backgroundMaxAlpha() {
        return 0.75f;
    }

    public final void hide() {
        if (!shown)
            return;
        WindowListener.WindowEvent event = Pools
            .obtain(WindowListener.WindowEvent.class)
            .setEventType(WindowListener.EventType.hide)
            .setWindow(this);
        boolean cancelled = fire(event);
        Pools.free(event);
        if (cancelled) {
            return;
        }

        shown = false;

        background.clearActions();
        table.clearActions();

        WindowManager.instance.remove(this);


        float w = useParentSize() ? getParent().getWidth() : getStage().getWidth();
        float h = useParentSize() ? getParent().getHeight() : getStage().getHeight();
        isHiding = true;
        table.addAction(Actions.sequence(
            Actions.parallel(
                Actions.scaleTo(0f, 0f, 0.4f, Interpolation.swingIn),
                Actions.moveTo(w / 2, h / 2, 0.4f, Interpolation.swingIn),
                Actions.alpha(0f, 0.4f)
            ),
            Actions.scaleTo(0, 0)
        ));

        background.addAction(Actions.sequence(
            Actions.alpha(0, 0.3f),
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    WindowListener.WindowEvent event = Pools.obtain(WindowListener.WindowEvent.class)
                        .setEventType(WindowListener.EventType.hidden)
                        .setWindow(GameWindow.this);
                    fire(event);
                    Pools.free(event);
                    getStage().removeListener(resizeListener);
                    GameWindow.this.remove();
                    onHide();
                    isHiding = false;
                }
            })
        ));
    }


    public boolean handleBackPressed() {
        if (shown && canBeClosed()) {
            hide();
            return true;
        }
        return false;
    }

    @Override public boolean notify(Event event, boolean capture) {
        return super.notify(event, capture);
    }

    protected void initialize() {}

    protected abstract void doShow(I i);

    protected void onHide() {}

    protected boolean canBeClosed() { return true; }

    public final boolean isShown() {return shown;}

    public Group getTargetParent() {
        return null;
    }

    public boolean useParentSize() {
        return false;
    }
}
