package io.github.iss_2025_2026.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/**
 * Primitive riusabili del design system per le schermate menu.
 */
public final class GameUiFactory {

    private GameUiFactory() {
    }

    public static Table createScreenRoot(Stage stage, Texture background, Skin skin) {
        Image backgroundActor = new Image(background);
        backgroundActor.setFillParent(true);
        backgroundActor.setScaling(com.badlogic.gdx.utils.Scaling.stretch);
        stage.addActor(backgroundActor);

        Image wash = new Image(skin.newDrawable("white", GameUiTheme.SCREEN_WASH));
        wash.setFillParent(true);
        stage.addActor(wash);

        Table root = new Table();
        root.setFillParent(true);
        root.pad(GameUiTheme.SPACE_4);
        root.center();
        stage.addActor(root);
        return root;
    }

    public static Table createPanel(Skin skin, float padding) {
        Table panel = new Table();
        panel.setBackground(skin.getDrawable(GameUiTheme.DRAWABLE_PANEL));
        panel.pad(padding);
        return panel;
    }

    public static Table createStrongPanel(Skin skin, float padding) {
        Table panel = new Table();
        panel.setBackground(skin.getDrawable(GameUiTheme.DRAWABLE_PANEL_STRONG));
        panel.pad(padding);
        return panel;
    }

    public static Table createHeroBlock(Skin skin, String eyebrow, String title, String subtitle) {
        Table hero = new Table();
        hero.left();
        hero.defaults().left().growX();

        if (eyebrow != null && !eyebrow.isEmpty()) {
            Label eyebrowLabel = new Label(eyebrow, skin, GameUiTheme.LABEL_TAG);
            hero.add(eyebrowLabel).padBottom(GameUiTheme.SPACE_1).row();
        }

        Image accentBar = new Image(skin.getDrawable(GameUiTheme.DRAWABLE_ACCENT_BAR));
        hero.add(accentBar).width(84f).height(4f).padBottom(GameUiTheme.SPACE_2).row();

        Label titleLabel = new Label(title, skin, GameUiTheme.LABEL_TITLE);
        titleLabel.setWrap(true);
        hero.add(titleLabel).padBottom(GameUiTheme.SPACE_2).row();

        if (subtitle != null && !subtitle.isEmpty()) {
            Label subtitleLabel = new Label(subtitle, skin, GameUiTheme.LABEL_MUTED);
            subtitleLabel.setWrap(true);
            hero.add(subtitleLabel).row();
        }

        return hero;
    }

    public static TextButton createButton(String text, Skin skin, String styleName) {
        TextButton button = new TextButton(text, skin, styleName);
        CursorHoverUtil.applyPointerCursor(button);
        return button;
    }

    public static Label createBodyLabel(String text, Skin skin) {
        Label label = new Label(text, skin, GameUiTheme.LABEL_BODY);
        label.setWrap(true);
        return label;
    }

    public static Label createMutedLabel(String text, Skin skin) {
        Label label = new Label(text, skin, GameUiTheme.LABEL_MUTED);
        label.setWrap(true);
        return label;
    }

    public static Table createChip(Skin skin, String text) {
        Table chip = new Table();
        chip.setBackground(skin.getDrawable(GameUiTheme.DRAWABLE_CHIP));
        chip.pad(GameUiTheme.SPACE_1, GameUiTheme.SPACE_2, GameUiTheme.SPACE_1, GameUiTheme.SPACE_2);
        chip.add(new Label(text, skin, GameUiTheme.LABEL_TAG));
        return chip;
    }

    public static Table createStatChip(Skin skin, String label, String value) {
        Table chip = createStrongPanel(skin, GameUiTheme.SPACE_2);
        chip.defaults().left();

        Label labelActor = new Label(label, skin, GameUiTheme.LABEL_TAG);
        Label valueActor = new Label(value, skin, GameUiTheme.LABEL_BODY);
        valueActor.setWrap(true);

        chip.add(labelActor).padBottom(4f).row();
        chip.add(valueActor).growX();
        return chip;
    }
}
