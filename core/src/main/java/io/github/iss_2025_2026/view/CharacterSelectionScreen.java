package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.controller.CharacterSelectionController;
import io.github.iss_2025_2026.controller.GameController;
import io.github.iss_2025_2026.factory.CharacterFactory;
import io.github.iss_2025_2026.model.CharacterSelectionModel;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.NewGameConfigModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacterSelectionScreen implements Screen {

    private final Main game;
    private final GameModel gameModel;
    private final GameController gameController;
    private final CharacterSelectionModel selectionModel;
    private final CharacterSelectionController selectionController;

    private Stage stage;
    private Skin skin;
    private SpriteBatch batch;
    private Texture background;
    private Map<String, Texture> characterTextures;
    private List<Image> characterImages;

    public CharacterSelectionScreen(Main game, GameModel gameModel, GameController gameController,
            NewGameConfigModel config) {
        this.game = game;
        this.gameModel = gameModel;
        this.gameController = gameController;
        CharacterFactory factory = new CharacterFactory();
        this.selectionModel = new CharacterSelectionModel(factory);
        this.selectionController = new CharacterSelectionController(game, selectionModel, config);

        this.batch = new SpriteBatch();
        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        if (Gdx.files.internal("select-character-bg.png").exists()) {
            this.background = new Texture(Gdx.files.internal("select-character-bg.png"));
        } else {
            this.background = new Texture(Gdx.files.internal("background_init.png"));
        }

        this.characterTextures = new HashMap<>();
        this.characterImages = new ArrayList<>();
        loadCharacterTextures();

        buildUI();
        updateSelectionUI();
    }

    private void loadCharacterTextures() {
        for (Map<String, Object> data : selectionModel.getCharacters()) {
            String id = (String) data.get("id");
            String path = getImagePath(id);
            if (Gdx.files.internal(path).exists()) {
                characterTextures.put(id, new Texture(Gdx.files.internal(path)));
            }
        }
    }

    private String getImagePath(String id) {
        switch (id) {
            case "papa":
                return "father.png";
            case "mamma":
                return "mom.png";
            case "bambino":
                return "child.png";
            default:
                return id + ".png";
        }
    }

    private void buildUI() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Responsive background
        Image backgroundActor = new Image(background);
        backgroundActor.setFillParent(true);
        backgroundActor.setScaling(com.badlogic.gdx.utils.Scaling.stretch);
        stage.addActor(backgroundActor);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label title = new Label("SCEGLI IL TUO PERSONAGGIO", skin);
        title.setAlignment(Align.center);
        root.add(title).colspan(4).padBottom(40).row();

        Table grid = new Table();
        List<Map<String, Object>> chars = selectionModel.getCharacters();
        chars.sort(Comparator.comparingInt((Map<String, Object> c) -> {
            Object val = c.get("baseDamage");
            return val instanceof Number ? ((Number) val).intValue() : 0;
        }).reversed());
        for (int i = 0; i < chars.size(); i++) {
            final int index = i;
            Map<String, Object> charData = chars.get(i);
            String id = (String) charData.get("id");

            Table card = new Table();

            Texture tex = characterTextures.get(id);
            final Image img = new Image(tex != null ? tex : background);
            img.setScaling(com.badlogic.gdx.utils.Scaling.fit);
            img.setAlign(Align.bottom);

            // Add click listener to select
            img.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectionModel.setSelectedIndex(index);
                    updateSelectionUI();
                }
            });

            characterImages.add(img);
            // We wrap the image in a container to give it a fixed area but let Scaling.fit
            // do its work
            card.add(img).size(220, 320).padBottom(10).row();

            Label nameLabel = new Label((String) charData.get("name"), skin);
            nameLabel.setAlignment(Align.center);
            card.add(nameLabel).row();

            grid.add(card).pad(15);
        }
        root.add(grid).row();

        // Footer Buttons
        Table footer = new Table();
        TextButton backBtn = new TextButton("Indietro", skin);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new NewGameConfigScreen(game, gameModel, gameController));
                dispose();
            }
        });

        TextButton confirmBtn = new TextButton("CONFERMA", skin);
        confirmBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectionController.confirmSelection();
            }
        });

        footer.add(backBtn).width(150).padRight(50);
        footer.add(confirmBtn).width(200);
        root.add(footer).colspan(4).padTop(40);
    }

    private void updateSelectionUI() {
        int selected = selectionModel.getSelectedIndex();
        for (int i = 0; i < characterImages.size(); i++) {
            Image img = characterImages.get(i);
            if (i == selected) {
                img.setColor(Color.WHITE);
            } else {
                img.setColor(0.4f, 0.4f, 0.4f, 0.7f); // Oscurato e leggermente trasparente
            }
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        batch.dispose();
        background.dispose();
        for (Texture t : characterTextures.values())
            t.dispose();
    }
}
