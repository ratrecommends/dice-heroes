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

package com.vlaaad.dice.game.world.behaviours.processors.pvp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.DiceHeroes;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.RequestProcessor;
import com.vlaaad.dice.game.world.behaviours.params.AbilityAbilityParams;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCoordinatesParams;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCreatureParams;
import com.vlaaad.dice.game.world.behaviours.responces.TurnResponse;
import com.vlaaad.dice.game.world.view.Tile;
import com.vlaaad.dice.states.PvpPlayState;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

/**
 * Created 29.07.14 by vlaaad
 */
public class PvpUserWrapper<Response, Params> extends RequestProcessor<Response, Params> implements IFutureListener<Response> {

    public static final int ICON_PADDING = 2;
    public static final int BAR_HEIGHT = 3;
    private final RequestProcessor<Response, Params> processor;
    private final PvpPlayState state;
    private Group root = new Group();

    public PvpUserWrapper(RequestProcessor<Response, Params> processor, PvpPlayState state) {
        super();
        this.processor = processor;
        this.state = state;
    }

    @Override public int preProcess(Params params) {
        return processor.preProcess(params);
    }

    @Override public IFuture<Response> process(final Params params) {
        final Future<Response> future = new Future<Response>();
        Stage stage = ((DiceHeroes) Gdx.app.getApplicationListener()).getState().stage;
        Tile icon = new Tile("ui/pvp/timer");
        icon.setPosition(ICON_PADDING, ICON_PADDING);

        Tile progress = new Tile("ui-reward-window-background");
        progress.setTouchable(Touchable.disabled);
        progress.setPosition(
            ICON_PADDING * 2 + icon.getWidth(),
            ICON_PADDING + icon.getHeight() * 0.5f - BAR_HEIGHT * 0.5f
        );
        progress.setSize(
            stage.getWidth() - ICON_PADDING * 3 - icon.getWidth(),
            BAR_HEIGHT
        );
        root = new Group();
        root.addActor(progress);
        root.addActor(icon);

        stage.addActor(root);
        root.addAction(delay(25, forever(sequence(
            visible(false),
            delay(0.1f),
            visible(true),
            delay(0.4f)
        ))));
        progress.addAction(
            sequence(
                sizeTo(0, BAR_HEIGHT, 30f),
                run(new Runnable() {
                    @Override public void run() {
                        root.remove();
                        skip(future, params);
                    }
                })
            )
        );
        processor.process(params).addListener(future);
        return future.addListener(this);
    }

    @SuppressWarnings("unchecked")
    private void skip(Future<Response> future, Params params) {
        processor.cancel();
        if (request == null) throw new NullPointerException("request");
        if (request == BehaviourRequest.TURN) {
            skipTurn(((Future<TurnResponse>) future));
        } else if (request == BehaviourRequest.ABILITY) {
            skipAbility(((AbilityAbilityParams) params), ((Future<Ability>) future));
        } else if (request == BehaviourRequest.CREATURE) {
            skipCreature(((AbilityCreatureParams) params), ((Future<Creature>) future));
        } else if (request == BehaviourRequest.COORDINATE) {
            skipCoordinate(((AbilityCoordinatesParams) params), ((Future<Grid2D.Coordinate>) future));
        } else {
            throw new IllegalStateException("unknown request: " + request);
        }
    }

    private void skipCoordinate(AbilityCoordinatesParams params, Future<Grid2D.Coordinate> future) {
        future.happen(params.coordinates.random());
    }

    private void skipCreature(AbilityCreatureParams params, Future<Creature> future) {
        future.happen(params.available.random());
    }

    private void skipTurn(Future<TurnResponse> future) {
        future.happen(new TurnResponse<Void>(TurnResponse.TurnAction.SKIP, null));
    }

    private void skipAbility(AbilityAbilityParams params, Future<Ability> future) {
        future.happen(params.availableAbilities.random());
    }

    @Override public void onHappened(Response response) {
        root.remove();
        state.sendRoundMessage(request, response);
    }

    @Override public void setRequest(BehaviourRequest<Response, Params> request) {
        super.setRequest(request);
        processor.setRequest(request);
    }
}
