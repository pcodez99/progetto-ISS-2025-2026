package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Utility per mostrare il cursore a mano sopra gli elementi cliccabili.
 */
public final class CursorHoverUtil {

    private CursorHoverUtil() {
    }

    public static void applyPointerCursor(Actor actor) {
        actor.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && isInteractive(actor)) {
                    Gdx.graphics.setSystemCursor(SystemCursor.Hand);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    resetDefaultCursor();
                }
            }
        });
    }

    public static void resetDefaultCursor() {
        Gdx.graphics.setSystemCursor(SystemCursor.Arrow);
    }

    private static boolean isInteractive(Actor actor) {
        if (actor == null || !actor.isVisible() || actor.getTouchable() != Touchable.enabled) {
            return false;
        }

        if (actor instanceof Button) {
            return !((Button) actor).isDisabled();
        }

        return true;
    }
}
