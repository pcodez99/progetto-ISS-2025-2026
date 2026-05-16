package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Tema condiviso per le schermate menu del gioco.
 */
public final class GameUiTheme {
    private static final float FIELD_INSET_X = 34f;
    private static final float FIELD_INSET_Y = 16f;

    public static final float SPACE_1 = 8f;
    public static final float SPACE_2 = 12f;
    public static final float SPACE_3 = 16f;
    public static final float SPACE_4 = 24f;
    public static final float SPACE_5 = 32f;
    public static final float SPACE_6 = 40f;

    public static final Color TEXT = new Color(0.97f, 0.96f, 0.92f, 1f);
    public static final Color MUTED = new Color(0.72f, 0.77f, 0.79f, 1f);
    public static final Color ACCENT_LIME = new Color(0.73f, 0.90f, 0.33f, 1f);
    public static final Color ACCENT_CYAN = new Color(0.36f, 0.82f, 0.88f, 1f);
    public static final Color ACCENT_AMBER = new Color(0.95f, 0.78f, 0.40f, 1f);
    public static final Color PANEL = new Color(0.05f, 0.08f, 0.10f, 0.84f);
    public static final Color PANEL_STRONG = new Color(0.08f, 0.12f, 0.15f, 0.92f);
    public static final Color SURFACE = new Color(0.11f, 0.16f, 0.19f, 0.94f);
    public static final Color SCREEN_WASH = new Color(0.03f, 0.04f, 0.06f, 0.56f);
    public static final Color CHIP = new Color(0.16f, 0.22f, 0.25f, 0.96f);
    public static final Color CARD_IDLE = new Color(0.08f, 0.11f, 0.13f, 0.82f);
    public static final Color CARD_SELECTED = new Color(0.14f, 0.22f, 0.14f, 0.93f);
    public static final Color SUCCESS = new Color(0.76f, 0.92f, 0.52f, 1f);
    public static final Color DANGER = new Color(0.95f, 0.58f, 0.47f, 1f);

    public static final String LABEL_TITLE = "game-title";
    public static final String LABEL_SECTION = "section-title";
    public static final String LABEL_BODY = "body";
    public static final String LABEL_MUTED = "muted";
    public static final String LABEL_TAG = "tag";

    public static final String BUTTON_PRIMARY = "primary";
    public static final String BUTTON_SECONDARY = "secondary";
    public static final String BUTTON_GHOST = "ghost";

    public static final String TEXT_FIELD_GAME = "game";
    public static final String SELECT_BOX_GAME = "game";
    public static final String SLIDER_GAME = "game";

    public static final String DRAWABLE_PANEL = "panel";
    public static final String DRAWABLE_PANEL_STRONG = "panel-strong";
    public static final String DRAWABLE_CHIP = "chip";
    public static final String DRAWABLE_CARD = "card";
    public static final String DRAWABLE_CARD_SELECTED = "card-selected";
    public static final String DRAWABLE_ACCENT_BAR = "accent-bar";

    private GameUiTheme() {
    }

    public static Skin loadSkin() {
        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        applyTheme(skin);
        return skin;
    }

    public static void applyTheme(Skin skin) {
        skin.add(DRAWABLE_PANEL, tint(skin, "white", PANEL), Drawable.class);
        skin.add(DRAWABLE_PANEL_STRONG, tint(skin, "white", PANEL_STRONG), Drawable.class);
        skin.add(DRAWABLE_CHIP, tint(skin, "white", CHIP), Drawable.class);
        skin.add(DRAWABLE_CARD, tint(skin, "white", CARD_IDLE), Drawable.class);
        skin.add(DRAWABLE_CARD_SELECTED, tint(skin, "white", CARD_SELECTED), Drawable.class);
        skin.add(DRAWABLE_ACCENT_BAR, tint(skin, "white", ACCENT_LIME), Drawable.class);

        skin.add(LABEL_TITLE, new Label.LabelStyle(skin.getFont("window"), TEXT), Label.LabelStyle.class);
        skin.add(LABEL_SECTION, new Label.LabelStyle(skin.getFont("subtitle"), ACCENT_AMBER), Label.LabelStyle.class);
        skin.add(LABEL_BODY, new Label.LabelStyle(skin.getFont("font"), TEXT), Label.LabelStyle.class);
        skin.add(LABEL_MUTED, new Label.LabelStyle(skin.getFont("font"), MUTED), Label.LabelStyle.class);
        skin.add(LABEL_TAG, new Label.LabelStyle(skin.getFont("list"), ACCENT_CYAN), Label.LabelStyle.class);

        skin.add(BUTTON_PRIMARY, createButtonStyle(skin, ACCENT_LIME, new Color(0.85f, 0.98f, 0.58f, 1f),
                new Color(0.56f, 0.74f, 0.23f, 1f), new Color(0.08f, 0.11f, 0.08f, 1f)),
                TextButton.TextButtonStyle.class);
        skin.add(BUTTON_SECONDARY, createButtonStyle(skin, ACCENT_CYAN, new Color(0.55f, 0.92f, 0.98f, 1f),
                new Color(0.22f, 0.60f, 0.67f, 1f), TEXT), TextButton.TextButtonStyle.class);
        skin.add(BUTTON_GHOST, createButtonStyle(skin, new Color(0.18f, 0.22f, 0.25f, 0.95f),
                new Color(0.24f, 0.29f, 0.33f, 1f), new Color(0.12f, 0.15f, 0.18f, 1f), TEXT),
                TextButton.TextButtonStyle.class);

        skin.add(TEXT_FIELD_GAME, createTextFieldStyle(skin), TextField.TextFieldStyle.class);
        skin.add(SELECT_BOX_GAME, createSelectBoxStyle(skin), SelectBox.SelectBoxStyle.class);
        skin.add(SLIDER_GAME, createSliderStyle(skin), Slider.SliderStyle.class);
    }

    private static TextButton.TextButtonStyle createButtonStyle(Skin skin, Color upColor, Color overColor,
            Color downColor, Color fontColor) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = tint(skin, "button-normal", upColor);
        style.over = tint(skin, "button-normal-over", overColor);
        style.down = tint(skin, "button-normal-pressed", downColor);
        style.focused = style.over;
        style.font = skin.getFont("font");
        style.fontColor = fontColor;
        style.overFontColor = fontColor;
        style.downFontColor = fontColor;
        return style;
    }

    private static TextField.TextFieldStyle createTextFieldStyle(Skin skin) {
        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = skin.getFont("subtitle");
        style.fontColor = TEXT;
        style.disabledFontColor = MUTED;
        style.messageFont = skin.getFont("subtitle");
        style.messageFontColor = MUTED;
        style.cursor = tint(skin, "white", ACCENT_LIME);
        style.selection = tint(skin, "white", new Color(ACCENT_CYAN.r, ACCENT_CYAN.g, ACCENT_CYAN.b, 0.35f));
        style.background = tintWithInsets(
                skin,
                "textfield",
                SURFACE,
                FIELD_INSET_X, FIELD_INSET_X,
                FIELD_INSET_Y, FIELD_INSET_Y);
        style.focusedBackground = tintWithInsets(
                skin,
                "textfield-selected",
                new Color(0.18f, 0.25f, 0.19f, 0.98f),
                FIELD_INSET_X, FIELD_INSET_X,
                FIELD_INSET_Y, FIELD_INSET_Y);
        return style;
    }

    private static SelectBox.SelectBoxStyle createSelectBoxStyle(Skin skin) {
        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font = skin.getFont("subtitle");
        listStyle.fontColorSelected = TEXT;
        listStyle.fontColorUnselected = TEXT;
        listStyle.selection = tintWithInsets(
                skin,
                "white",
                new Color(ACCENT_CYAN.r, ACCENT_CYAN.g, ACCENT_CYAN.b, 0.36f),
                FIELD_INSET_X, FIELD_INSET_X,
                FIELD_INSET_Y * 0.75f, FIELD_INSET_Y * 0.75f);
        listStyle.background = tintWithInsets(
                skin,
                "white",
                PANEL_STRONG,
                FIELD_INSET_X, FIELD_INSET_X,
                FIELD_INSET_Y, FIELD_INSET_Y);

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = tint(skin, "white", new Color(0.06f, 0.10f, 0.12f, 0.9f));
        scrollStyle.vScrollKnob = tint(skin, "white", ACCENT_CYAN);
        scrollStyle.hScrollKnob = tint(skin, "white", ACCENT_CYAN);

        SelectBox.SelectBoxStyle style = new SelectBox.SelectBoxStyle();
        style.font = skin.getFont("subtitle");
        style.fontColor = TEXT;
        style.background = tintWithInsets(
                skin,
                "select-box",
                new Color(0.13f, 0.18f, 0.20f, 1f),
                FIELD_INSET_X, FIELD_INSET_X,
                FIELD_INSET_Y, FIELD_INSET_Y);
        style.backgroundOver = tintWithInsets(
                skin,
                "select-box",
                new Color(0.18f, 0.24f, 0.26f, 1f),
                FIELD_INSET_X, FIELD_INSET_X,
                FIELD_INSET_Y, FIELD_INSET_Y);
        style.backgroundOpen = tintWithInsets(
                skin,
                "select-box-open",
                new Color(0.20f, 0.27f, 0.29f, 1f),
                FIELD_INSET_X, FIELD_INSET_X,
                FIELD_INSET_Y, FIELD_INSET_Y);
        style.scrollStyle = scrollStyle;
        style.listStyle = listStyle;
        return style;
    }

    private static Slider.SliderStyle createSliderStyle(Skin skin) {
        Slider.SliderStyle style = new Slider.SliderStyle();
        style.background = tint(skin, "progress-bar-square", new Color(0.15f, 0.20f, 0.22f, 1f));
        style.knob = tint(skin, "slider-knob", ACCENT_LIME);
        style.knobOver = tint(skin, "slider-knob-over", new Color(0.86f, 0.98f, 0.58f, 1f));
        style.knobDown = tint(skin, "slider-knob-over", ACCENT_AMBER);
        return style;
    }

    private static Drawable tint(Skin skin, String baseDrawable, Color color) {
        return skin.newDrawable(baseDrawable, new Color(color));
    }

    private static Drawable tintWithInsets(Skin skin, String baseDrawable, Color color,
            float left, float right, float top, float bottom) {
        Drawable drawable = tint(skin, baseDrawable, color);
        if (drawable instanceof BaseDrawable) {
            BaseDrawable base = (BaseDrawable) drawable;
            base.setLeftWidth(left);
            base.setRightWidth(right);
            base.setTopHeight(top);
            base.setBottomHeight(bottom);
        }
        return drawable;
    }
}
