package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.controller.CharacterSelectionController;
import io.github.iss_2025_2026.controller.GameController;
import io.github.iss_2025_2026.factory.PlayerFactory;
import io.github.iss_2025_2026.factory.YamlPlayerFactory;
import io.github.iss_2025_2026.model.CharacterSelectionModel;
import io.github.iss_2025_2026.model.CharacterSelectionOption;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.NewGameConfigModel;
import io.github.iss_2025_2026.service.MenuMusicManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacterSelectionScreen implements Screen {
    private static final float ROOT_PADDING_RATIO = 0.03f;
    private static final float SHELL_PADDING_RATIO = 0.04f;
    private static final float SECTION_GAP_RATIO = 0.03f;
    private static final float GRID_GAP_RATIO = 0.018f;
    private static final float CARD_PADDING_RATIO = 0.055f;
    private static final float BUTTON_HEIGHT_RATIO = 0.075f;
    private static final float IMAGE_HEIGHT_RATIO = 1.08f;
    private static final float IMAGE_HEIGHT_COMPACT_RATIO = 0.84f;
    private static final float DETAILS_HEIGHT_RATIO = 0.60f;
    private static final float DETAILS_HEIGHT_COMPACT_RATIO = 0.42f;
    private static final float HERO_HEIGHT_RATIO = 0.20f;
    private static final float CARD_TEXT_RATIO = 0.16f;
    private static final float CARD_TEXT_EXPANDED_RATIO = 0.34f;
    private static final float CARD_CHROME_RATIO = 0.22f;

    private final Main game;
    private final GameModel gameModel;
    private final GameController gameController;
    private final NewGameConfigModel config;
    private final int currentPlayerIndex;
    private final CharacterSelectionModel selectionModel;
    private final CharacterSelectionController selectionController;

    private Stage stage;
    private Skin skin;
    private Texture background;
    private Map<String, Texture> characterTextures;
    private List<CharacterCardView> characterCards;
    private Table root;
    private Table shell;
    private Table grid;
    private Table footer;
    private Cell<?> heroCell;
    private Cell<?> gridCell;
    private Container<Table> shellWrap;
    private TextButton backBtn;
    private TextButton confirmBtn;
    private float currentCardWidth;
    private float currentImageHeight;
    private float currentDetailsHeight;
    private int currentGridColumns;
    private int currentGridRows;
    private float currentCardPadding;
    private float currentButtonHeight;
    private float currentButtonWidth;
    private float currentGridGap;
    private float currentSectionGap;
    private boolean ultraCompactCardCopy;
    private boolean showRoleDescription;
    private boolean showAbilityDescription;
    private boolean stackFooter;

    public CharacterSelectionScreen(Main game, GameModel gameModel, GameController gameController,
            NewGameConfigModel config, int currentPlayerIndex) {
        this.game = game;
        this.gameModel = gameModel;
        this.gameController = gameController;
        this.config = config;
        this.currentPlayerIndex = currentPlayerIndex;
        PlayerFactory playerFactory = new YamlPlayerFactory();
        this.selectionModel = new CharacterSelectionModel(playerFactory);
        this.selectionController = new CharacterSelectionController(game, gameModel, gameController, selectionModel,
                playerFactory, config, currentPlayerIndex);

        this.skin = GameUiTheme.loadSkin();

        if (Gdx.files.internal("select-character-bg.png").exists()) {
            this.background = new Texture(Gdx.files.internal("select-character-bg.png"));
        } else {
            this.background = new Texture(Gdx.files.internal("background_init.png"));
        }

        this.characterTextures = new HashMap<>();
        this.characterCards = new ArrayList<>();
        restorePreviousSelection();
        loadCharacterTextures();

        buildUI();
        updateSelectionUI();
    }

    private void restorePreviousSelection() {
        String selectedCharacterId = currentPlayerIndex == 2
                ? config.getSelectedCharacterPlayerTwo()
                : config.getSelectedCharacterPlayerOne();
        selectionModel.setSelectedCharacterById(selectedCharacterId);
    }

    private void loadCharacterTextures() {
        for (CharacterSelectionOption character : selectionModel.getCharacters()) {
            String id = character.getId();
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

        root = GameUiFactory.createScreenRoot(stage, background, skin);

        shell = GameUiFactory.createPanel(skin, GameUiTheme.SPACE_5);
        shell.defaults().growX().left();
        heroCell = shell.add(GameUiFactory.createHeroBlock(
                skin,
                "ROSTER SELEZIONE",
                getTitle(),
                getSubtitle()))
                .padBottom(GameUiTheme.SPACE_4);
        shell.row();

        grid = new Table();
        grid.center();
        gridCell = shell.add(grid).padBottom(GameUiTheme.SPACE_4);
        shell.row();

        footer = new Table();
        footer.left();
        backBtn = GameUiFactory.createButton("Indietro", skin, GameUiTheme.BUTTON_GHOST);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (config.isMultiplayer() && currentPlayerIndex == 2) {
                    game.setScreen(new PlayerSelectionTransitionScreen(game, gameModel, gameController, config, 2));
                } else {
                    game.setScreen(new NewGameConfigScreen(game, gameModel, gameController, config));
                }
                dispose();
            }
        });

        confirmBtn = GameUiFactory.createButton(getConfirmButtonText(), skin, GameUiTheme.BUTTON_PRIMARY);
        confirmBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectionController.confirmSelection();
            }
        });

        shell.add(footer).row();

        shellWrap = new Container<>(shell);
        root.add(shellWrap).growX().center();
        applyResponsiveLayout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private String getTitle() {
        if (!config.isMultiplayer()) {
            return "Scegli il tuo personaggio";
        }

        return "Giocatore " + currentPlayerIndex + ": scegli il personaggio";
    }

    private String getSubtitle() {
        if (!config.isMultiplayer()) {
            return "Ogni viddano ha un'identita precisa: controlla statistiche, crescita e abilita direttamente sopra il ritratto.";
        }

        if (currentPlayerIndex == 1) {
            return "Il primo giocatore sceglie ora il proprio viddano. Dopo la conferma comparira il passaggio al Giocatore 2.";
        }

        return "Ora tocca al secondo giocatore. Confermando questa scelta verranno istanziati entrambi i player nel game system.";
    }

    private String getConfirmButtonText() {
        if (!config.isMultiplayer()) {
            return "Conferma Personaggio";
        }

        return currentPlayerIndex == 1 ? "Conferma Giocatore 1" : "Conferma Giocatore 2";
    }

    private Table createCharacterCard(CharacterSelectionOption character, int index) {
        Table card = GameUiFactory.createStrongPanel(skin, currentCardPadding);
        card.setBackground(skin.getDrawable(GameUiTheme.DRAWABLE_CARD));
        CursorHoverUtil.applyPointerCursor(card);

        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectionModel.setSelectedIndex(index);
                updateSelectionUI();
            }
        });

        Texture tex = characterTextures.get(character.getId());
        Image image = new Image(tex != null ? tex : background);
        image.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        image.setAlign(Align.bottom);

        Container<Table> detailsSlot = new Container<>();
        Table detailsPanel = createDetailsPanel(character);
        detailsPanel.setVisible(false);
        detailsSlot.setActor(detailsPanel);
        detailsSlot.top().left();

        card.add(detailsSlot).width(currentCardWidth).height(currentDetailsHeight)
                .padBottom(currentCardPadding).row();
        card.add(image).size(currentCardWidth, currentImageHeight).padBottom(currentCardPadding).row();

        Label nameLabel = new Label(character.getName(), skin, GameUiTheme.LABEL_SECTION);
        nameLabel.setAlignment(Align.center);
        nameLabel.setWrap(true);
        card.add(nameLabel).width(currentCardWidth).padBottom(currentCardPadding * 0.5f).row();

        Label roleLabel = null;
        if (showRoleDescription) {
            roleLabel = new Label(character.getDescription(), skin, GameUiTheme.LABEL_MUTED);
            roleLabel.setWrap(true);
            roleLabel.setAlignment(Align.center);
            card.add(roleLabel).width(currentCardWidth).row();
        }

        characterCards.add(new CharacterCardView(card, image, detailsPanel, nameLabel, roleLabel));
        return card;
    }

    private Table createDetailsPanel(CharacterSelectionOption character) {
        float detailsInnerWidth = currentCardWidth - (currentCardPadding * 2f);
        float statChipWidth = (detailsInnerWidth - currentGridGap) / 2f;
        float copyGap = currentCardPadding * 0.45f;

        Table detailsPanel = GameUiFactory.createStrongPanel(skin, currentCardPadding);
        detailsPanel.defaults().left();
        detailsPanel.add(new Label(ultraCompactCardCopy ? "STATS" : "STATS LIVE", skin, GameUiTheme.LABEL_TAG))
                .width(detailsInnerWidth)
                .padBottom(copyGap).row();

        Table statsRow = new Table();
        statsRow.defaults().width(statChipWidth);
        statsRow.add(GameUiFactory.createStatChip(skin, "HP", String.valueOf(character.getMaxHp())))
                .width(statChipWidth).padRight(currentGridGap);
        statsRow.add(GameUiFactory.createStatChip(skin, "ATK", String.valueOf(character.getBaseDamage())))
                .width(statChipWidth);
        detailsPanel.add(statsRow).width(detailsInnerWidth).padBottom(copyGap).row();

        String growthText = ultraCompactCardCopy
                ? "+" + character.getMaxHpGrowth() + "HP / +" + character.getDamageGrowth() + "ATK"
                : "+" + character.getMaxHpGrowth() + " HP / +" + character.getDamageGrowth() + " ATK";
        detailsPanel.add(GameUiFactory.createMutedLabel(growthText, skin))
                .width(detailsInnerWidth).padBottom(copyGap).row();
        String abilityText = ultraCompactCardCopy
                ? "Ab: " + character.getAbilityName()
                : "Abilita: " + character.getAbilityName();
        detailsPanel.add(GameUiFactory.createBodyLabel(abilityText, skin))
                .width(detailsInnerWidth).padBottom(showAbilityDescription ? currentCardPadding * 0.25f : 0f).row();

        if (showAbilityDescription) {
            detailsPanel.add(GameUiFactory.createMutedLabel(character.getAbilityDescription(), skin))
                    .width(detailsInnerWidth).padBottom(copyGap).row();
        }

        detailsPanel.add(GameUiFactory.createMutedLabel(String.join(", ", character.getTraits()), skin))
                .width(detailsInnerWidth).row();
        return detailsPanel;
    }

    private void applyResponsiveLayout(int viewportWidth, int viewportHeight) {
        float viewportBase = Math.min(viewportWidth, viewportHeight);
        float rootPadding = viewportBase * ROOT_PADDING_RATIO;
        float shellPadding = viewportBase * SHELL_PADDING_RATIO;
        currentSectionGap = viewportBase * SECTION_GAP_RATIO;
        currentGridGap = viewportBase * GRID_GAP_RATIO;
        float availableShellWidth = viewportWidth - (rootPadding * 2f);

        root.pad(rootPadding);
        shell.pad(shellPadding);
        heroCell.padBottom(currentSectionGap);
        gridCell.padBottom(currentSectionGap);

        shellWrap.width(availableShellWidth);

        float contentWidth = availableShellWidth - (shellPadding * 2f);
        stackFooter = (viewportWidth / (float) Math.max(1, viewportHeight)) < 0.88f;
        currentButtonHeight = viewportHeight * BUTTON_HEIGHT_RATIO;
        currentButtonWidth = stackFooter ? contentWidth : (contentWidth - currentGridGap) / 2f;

        currentGridColumns = determineGridColumns(selectionModel.getCharacters().size(), viewportWidth, viewportHeight);
        currentGridRows = (int) Math.ceil(selectionModel.getCharacters().size() / (float) currentGridColumns);
        float rowBudget = calculateRowHeightBudget(viewportHeight);
        currentCardWidth = calculateCardWidth(contentWidth, rowBudget, currentGridColumns);
        currentCardPadding = currentCardWidth * CARD_PADDING_RATIO;
        float rowDensity = rowBudget / Math.max(1f, currentCardWidth);
        ultraCompactCardCopy = rowDensity < 1.30f || currentGridRows > 1;
        showRoleDescription = rowDensity > 1.55f && currentGridRows == 1;
        showAbilityDescription = rowDensity > 1.22f;

        currentImageHeight = calculateImageHeight(rowBudget);
        currentDetailsHeight = calculateDetailsHeight(rowBudget);

        rebuildCharacterGrid();
        rebuildFooter();

        shell.invalidateHierarchy();
        shell.layout();
    }

    private int determineGridColumns(int characterCount, int viewportWidth, int viewportHeight) {
        if (characterCount <= 1) {
            return 1;
        }

        float aspectRatio = viewportWidth / (float) Math.max(1, viewportHeight);
        if (characterCount >= 4 && aspectRatio >= 1.12f) {
            return 4;
        }
        return aspectRatio >= 0.68f ? Math.min(2, characterCount) : 1;
    }

    private float calculateCardWidth(float contentWidth, float rowBudget, int columns) {
        float gapWidth = currentGridGap * (columns - 1);
        float availableWidth = contentWidth - gapWidth;
        float widthBasedCardWidth = availableWidth / columns;
        float imageRatio = currentGridRows > 1 ? IMAGE_HEIGHT_COMPACT_RATIO : IMAGE_HEIGHT_RATIO;
        float detailsRatio = currentGridRows > 1 ? DETAILS_HEIGHT_COMPACT_RATIO : DETAILS_HEIGHT_RATIO;
        float textRatio = currentGridRows > 1 ? CARD_TEXT_RATIO : CARD_TEXT_EXPANDED_RATIO;
        float heightBasedCardWidth = rowBudget / (imageRatio + detailsRatio + textRatio + CARD_CHROME_RATIO);
        return Math.min(widthBasedCardWidth, heightBasedCardWidth);
    }

    private float calculateImageHeight(float rowBudget) {
        float preferredImageHeight = currentCardWidth
                * (currentGridRows > 1 ? IMAGE_HEIGHT_COMPACT_RATIO : IMAGE_HEIGHT_RATIO);
        float preferredDetailsHeight = currentCardWidth
                * (currentGridRows > 1 ? DETAILS_HEIGHT_COMPACT_RATIO : DETAILS_HEIGHT_RATIO);
        float textReserve = currentCardWidth * (showRoleDescription ? 0.34f : 0.16f);
        float preferredCardHeight = preferredImageHeight + preferredDetailsHeight + textReserve + (currentCardPadding * 3f);
        float scale = Math.min(1f, rowBudget / preferredCardHeight);
        return preferredImageHeight * scale;
    }

    private float calculateDetailsHeight(float rowBudget) {
        float preferredImageHeight = currentCardWidth
                * (currentGridRows > 1 ? IMAGE_HEIGHT_COMPACT_RATIO : IMAGE_HEIGHT_RATIO);
        float preferredDetailsHeight = currentCardWidth
                * (currentGridRows > 1 ? DETAILS_HEIGHT_COMPACT_RATIO : DETAILS_HEIGHT_RATIO);
        float textReserve = currentCardWidth * (showRoleDescription ? 0.34f : 0.16f);
        float preferredCardHeight = preferredImageHeight + preferredDetailsHeight + textReserve + (currentCardPadding * 3f);
        float scale = Math.min(1f, rowBudget / preferredCardHeight);
        return preferredDetailsHeight * scale;
    }

    private float calculateRowHeightBudget(int viewportHeight) {
        float heroEstimate = viewportHeight * (config.isMultiplayer() ? HERO_HEIGHT_RATIO * 1.08f : HERO_HEIGHT_RATIO);
        float footerEstimate = stackFooter ? (currentButtonHeight * 2f) + currentGridGap : currentButtonHeight;
        float shellVerticalPadding = shell.getPadTop() + shell.getPadBottom();
        float rootVerticalPadding = root.getPadTop() + root.getPadBottom();
        float rowGap = currentGridGap * (currentGridRows - 1);
        float availableGridHeight = viewportHeight - rootVerticalPadding - shellVerticalPadding - heroEstimate
                - footerEstimate - (currentSectionGap * 2f) - rowGap;
        return availableGridHeight / currentGridRows;
    }

    private void rebuildCharacterGrid() {
        grid.clearChildren();
        characterCards.clear();

        List<CharacterSelectionOption> chars = selectionModel.getCharacters();
        for (int i = 0; i < chars.size(); i++) {
            if (i > 0 && i % currentGridColumns == 0) {
                grid.row();
            }

            Cell<Table> cardCell = grid.add(createCharacterCard(chars.get(i), i)).width(currentCardWidth).top()
                    .padBottom(currentGridGap);
            if ((i % currentGridColumns) != currentGridColumns - 1) {
                cardCell.padRight(currentGridGap);
            }
        }

        updateSelectionUI();
    }

    private void rebuildFooter() {
        footer.clearChildren();
        footer.defaults().left();

        if (stackFooter) {
            footer.add(backBtn).growX().height(currentButtonHeight).padBottom(currentGridGap).row();
            footer.add(confirmBtn).growX().height(currentButtonHeight);
            return;
        }

        footer.add(backBtn).width(currentButtonWidth).height(currentButtonHeight).padRight(currentGridGap);
        footer.add(confirmBtn).width(currentButtonWidth).height(currentButtonHeight);
    }

    private void updateSelectionUI() {
        int selected = selectionModel.getSelectedIndex();
        for (int i = 0; i < characterCards.size(); i++) {
            CharacterCardView cardView = characterCards.get(i);
            if (i == selected) {
                cardView.image.setColor(Color.WHITE);
                cardView.detailsPanel.setVisible(true);
                cardView.card.setBackground(skin.getDrawable(GameUiTheme.DRAWABLE_CARD_SELECTED));
                cardView.nameLabel.setColor(GameUiTheme.ACCENT_LIME);
                if (cardView.roleLabel != null) {
                    cardView.roleLabel.setColor(GameUiTheme.TEXT);
                }
            } else {
                cardView.image.setColor(0.4f, 0.4f, 0.4f, 0.7f);
                cardView.detailsPanel.setVisible(false);
                cardView.card.setBackground(skin.getDrawable(GameUiTheme.DRAWABLE_CARD));
                cardView.nameLabel.setColor(GameUiTheme.ACCENT_AMBER);
                if (cardView.roleLabel != null) {
                    cardView.roleLabel.setColor(GameUiTheme.MUTED);
                }
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
        MenuMusicManager.play();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        applyResponsiveLayout(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        CursorHoverUtil.resetDefaultCursor();
        MenuMusicManager.pause();
    }

    @Override
    public void dispose() {
        CursorHoverUtil.resetDefaultCursor();
        stage.dispose();
        skin.dispose();
        background.dispose();
        for (Texture t : characterTextures.values())
            t.dispose();
    }

    private static class CharacterCardView {
        private final Table card;
        private final Image image;
        private final Table detailsPanel;
        private final Label nameLabel;
        private final Label roleLabel;

        private CharacterCardView(Table card, Image image, Table detailsPanel, Label nameLabel, Label roleLabel) {
            this.card = card;
            this.image = image;
            this.detailsPanel = detailsPanel;
            this.nameLabel = nameLabel;
            this.roleLabel = roleLabel;
        }
    }
}
