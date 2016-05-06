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

package com.vlaaad.dice.ui.windows;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.common.util.MathHelper;
import com.vlaaad.common.util.signals.ISignalListener;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.game.world.view.Tile;
import com.vlaaad.dice.ui.components.AbilityIconCounter;
import com.vlaaad.dice.ui.components.CraftingPane;
import com.vlaaad.dice.ui.components.IngredientsPane;
import com.vlaaad.dice.ui.components.ItemIcon;
import com.vlaaad.dice.ui.scene2d.LocLabel;

/**
 * Created 09.03.14 by vlaaad
 */
public class PotionsWindow extends GameWindow<UserData> implements ISignalListener<Ability> {

    private static final Vector2 tmp = new Vector2();
    private static final Thesaurus.Params params = Thesaurus.params();

    private final Group parentGroup;
    private final Container ingredientsContainer = new Container(new Actor());
    public CraftingPane craftingPane;
    private UserData userData;
    private final ObjectMap<Ability, AbilityIconCounter> potionIcons = new ObjectMap<Ability, AbilityIconCounter>();
    public Table potionsList;
    public LocLabel potionDescription;
    public IngredientsPane ingredients;
    private ObjectMap<Ability, ActorGestureListener> iconListeners = new ObjectMap<Ability, ActorGestureListener>();
    public Table ingredientsTable;
    private ScrollPane scrollPane;
    public LocLabel potionName;

    public PotionsWindow(Group parentGroup) {
        this.parentGroup = parentGroup;
    }

    @Override protected void initialize() {
        Table content = new Table(Config.skin);
        content.setBackground("ui-creature-info-background");
        content.setTouchable(Touchable.enabled);
        content.defaults().pad(2);
        Array<Ability> potions = new Array<Ability>();
        for (Ability ability : Config.abilities.byType(Ability.Type.potion)) {
            potions.add(ability);
        }
        potions.sort(Ability.COST_COMPARATOR);

        potionsList = new Table();
        Image selection = new Image(Config.skin.getDrawable("selection/turn"));
        potionsList.addActor(selection);

        potionName = new LocLabel("");
        potionDescription = new LocLabel("");
        potionDescription.setAlignment(Align.center | Align.top);
        potionDescription.setWrap(true);

        ingredientsTable = new Table(Config.skin);
        ingredientsTable.defaults().pad(2);

        Table viewRow = new Table();
        potionsList.add(viewRow).row();
        viewRow.defaults().pad(2);
        int i = 0;
        ActorGestureListener l = null;
        for (Ability ability : potions) {
            AbilityIconCounter icon = new AbilityIconCounter(ability, 0);
            icon.image.setOrigin(icon.image.getWidth() / 2, icon.image.getHeight() / 2);
            potionIcons.put(ability, icon);
            ActorGestureListener listener = createPotionTapListener(icon, ability, selection, potionsList, potionName, potionDescription, ingredientsTable);
            if (l == null) {
                l = listener;
            }
            iconListeners.put(ability, listener);
            icon.addListener(listener);
            i++;
            viewRow.add(icon);
            if (i % 5 == 0) {
                viewRow = new Table();
                viewRow.defaults().pad(2);
                potionsList.add(viewRow).row();
            }
        }

        craftingPane = new CraftingPane(3, potions);

        Table craftTable = new Table(Config.skin);
        craftTable.setBackground("ui-craft-content");
        craftTable.defaults().pad(4);
        craftTable.add(craftingPane).row();
        craftTable.add(ingredientsContainer).row();

        content.add(potionsList).row();
        content.add(new Tile("ui-creature-info-line")).size(80, 1).row();
        content.add(potionName).row();
        content.add(ingredientsTable).padTop(5).padBottom(1).row();
        content.add(potionDescription).size(130, 48).row();
        content.add(craftTable).expandX().fillX().pad(-2).row();

        Container contentContainer = new Container(content);
        contentContainer.width(content.getPrefWidth());
        scrollPane = new ScrollPane(contentContainer);
        scrollPane.setOverscroll(false, false);
        scrollPane.setCancelTouchFocus(false);
        table.add(scrollPane).width(contentContainer.getMaxWidth());

        if (l != null) {
            table.invalidate();
            table.validate();
            l.tap(null, 0, 0, 0, 0);
        }
    }

    @Override protected void doShow(UserData userData) {
        this.userData = userData;
        refreshPotionIcons(userData);
        userData.onPotionCountChanged.add(this);
        craftingPane.clearListeners();
        ingredients = new IngredientsPane(userData, craftingPane, this);
        ingredientsContainer.setActor(ingredients);
    }

    private void refreshPotionIcons(UserData data) {
        for (Ability potion : Config.abilities.byType(Ability.Type.potion)) {
            setCount(potion, userData);
        }
    }

    private void setCount(Ability potion, UserData data) {
        int count = data.getPotionCount(potion);
        AbilityIconCounter icon = potionIcons.get(potion);
        icon.setCount(count);
        icon.getColor().a = count == 0 ? 0.3f : 1f;
    }


    //potion count changed
    @Override public void handle(Ability ability) {
        AbilityIconCounter icon = potionIcons.get(ability);
        icon.image.addAction(Actions.sequence(
            Actions.scaleBy(0.5f, 0.5f, 0.2f, Interpolation.swingOut),
            Actions.scaleBy(-0.5f, -0.5f, 0.2f, Interpolation.elastic)
        ));
        setCount(ability, userData);
    }

    @Override protected void onHide() {
        userData.onPotionCountChanged.remove(this);
        userData = null;
    }

    private ActorGestureListener createPotionTapListener(final AbilityIconCounter icon, final Ability ability, final Image selection, final Table viewsList, final LocLabel name, final LocLabel desc, final Table ingredientsTable) {
        return new ActorGestureListener() {
            @Override public void tap(InputEvent event, float x, float y, int tapCount, int button) {
                Vector2 pos = icon.localToAscendantCoordinates(viewsList, tmp.set(0, 0));
                selection.setPosition(pos.x, pos.y);
                name.setKey(ability.locNameKey());
                desc.setKey(ability.locDescKey());
                desc.setParams(ability.fillDescriptionParams(params, null));
                Array<ItemIcon> icons = new Array<ItemIcon>();
                for (Item item : ability.ingredients.keys()) {
                    int count = ability.ingredients.get(item, 0);
                    for (int i = 0; i < count; i++) {
                        icons.add(new ItemIcon(item));
                    }
                }
                icons.sort(ItemIcon.ORDER_COMPARATOR);
                ingredientsTable.clearChildren();
                int i = 0;
                for (final ItemIcon itemIcon : icons) {
                    i++;
                    ingredientsTable.add(itemIcon).pad(-4);
                    itemIcon.addListener(ingredientListener(itemIcon.item));
                    if (i != icons.size) {
                        ingredientsTable.add(new Tile("ui-plus"));
                    }
                }
            }
        };
    }

    private EventListener ingredientListener(final Item item) {
        return new ActorGestureListener() {
            @Override public void tap(InputEvent event, float x, float y, int count, int button) {
                ingredients.showIngredientWindow(item);
            }
        };
    }

    @Override public Group getTargetParent() {
        return parentGroup;
    }

    @Override public boolean useParentSize() {
        return true;
    }

    public void switchTo(Ability potion) {
        iconListeners.get(potion).tap(null, 0, 0, 0, 0);
    }

    public void scrollTo(Actor contentElement, Runnable onEnd) {
        Vector2 pos = contentElement.localToAscendantCoordinates(scrollPane, tmp.set(0, 0));
        scrollPane.scrollTo(pos.x, pos.y, contentElement.getWidth(), contentElement.getHeight(), true, true);
        scrollPane.addAction(Actions.sequence(
            new Action() {
                @Override public boolean act(float delta) {
                    return MathHelper.equal(scrollPane.getScrollX(), scrollPane.getVisualScrollX(), 1f)
                        && MathHelper.equal(scrollPane.getScrollY(), scrollPane.getVisualScrollY(), 1f);
                }
            },
            Actions.run(onEnd)
        ));
    }
}
