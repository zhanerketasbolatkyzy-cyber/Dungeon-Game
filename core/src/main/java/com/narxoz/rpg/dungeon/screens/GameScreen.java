package com.narxoz.rpg.dungeon.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.narxoz.rpg.dungeon.DungeonGame;

public class GameScreen implements Screen {

    private DungeonGame game;
    private SpriteBatch batch;
    private Texture pixel;
    private Texture background;
    private Texture heroPortrait;

    // ── АНИМАЦИЯ ҮШІН ЖАҢА АЙНЫМАЛЫЛАР ───────────────────────────
    private Texture heroIdle;

    private Texture[] heroWalkFrames;
    private Texture[] heroAttackFrames;
    private Texture[] enemyFrames;
    private int currentHeroFrame = 0;
    private int currentEnemyFrame = 0;
    private float animationTimer = 0f;
    private final float FRAME_DURATION = 0.15f; // Кадрлардың ауысу жылдамдығы

    // ТРИГГЕРЛЕР: Шабуыл анимациясын бақылау
    private enum HeroState {
        IDLE,
        WALK,
        ATTACK
    }

    private HeroState heroState = HeroState.IDLE;
    private boolean isEnemyAttacking = false;

    // ── КЕЙІПКЕРДІҢ ОРНЫ МЕН ЖЫЛДАМДЫҒЫ (ҚОЗҒАЛЫС) ──────────────
    private float heroX = 120f; // Бастапқы Х орны
    private float heroY = 220f; // Бастапқы Ү орны
    private float heroSpeed = 250f; // Жылдамдығы (пиксель/секунд)

    private BitmapFont bigFont, font, logFont, tabFont;

    // Hero stats
    private String heroName;
    private int heroHp, heroMaxHp, heroAtk, heroLevel = 1, heroXp = 0;

    // Enemy stats
    private int enemyHp, enemyMaxHp, enemyAtk;
    private String enemyName;
    private String enemyType;
    private int enemyTurnCount = 0;

    // Floor
    private int floor = 1;

    // Battle state
    private String battleLog = "";
    private String subLog = "";
    private boolean playerTurn = true;
    private boolean battleOver = false;
    private boolean won = false;
    private float enemyTimer = 0f;

    // ── TAB PANEL SYSTEM ──────────────────────────────────────────
    private int activeTab = -1;

    private static final float TAB_Y = 10f;
    private static final float TAB_H = 46f;
    private static final float TAB_W = 180f;
    private static final float TAB_GAP = 8f;
    private static final float TAB_START_X = (DungeonGame.WIDTH - (3 * TAB_W + 2 * TAB_GAP)) / 2f;

    private static final float SUB_PANEL_H = 64f;
    private static final float SUB_Y = TAB_Y + TAB_H + 6f;
    private static final float SUB_H = 52f;

    private static final String[] TAB_LABELS = {"[A] ATTACK", "[M] MAGIC", "[I] ITEMS"};
    private static final Color[] TAB_COLORS = {Color.CYAN, new Color(0.7f, 0.3f, 1f, 1f), Color.ORANGE};
    private boolean isHeroAttacking = false;
    private static final String[][] SUB_LABELS = {
        {"[1] QUICK", "[2] HEAVY"},          // ATTACK tab
        {"[3] DRAIN", "[4] FIRE BOLT"},       // MAGIC tab
        {"[H] HEAL", "[5] SCROLL"}           // ITEMS tab
    };
    private static final Color[][] SUB_COLORS = {
        {Color.CYAN, Color.ORANGE},
        {new Color(0.5f, 1f, 0.5f, 1f), new Color(1f, 0.4f, 0.2f, 1f)},
        {Color.GREEN, new Color(1f, 0.85f, 0.2f, 1f)}
    };
    private static final int[][] SUB_ACTIONS = {
        {1, 2},
        {3, 4},
        {5, 6}
    };

    // Lore notes
    private String[] floorNotes = {
        "\"I came to save a princess. The walls whisper my name...\"",
        "\"A corpse in armor identical to mine. I choose not to think about it.\"",
        "\"No monster here. Only a mirror. It smiles when I don't.\"",
        "\"The core of the dungeon. Whatever waits ahead... it's too late to turn back.\""
    };
    private boolean noteDismissed = false;

    // ── CONSTRUCTOR ───────────────────────────────────────────────
    public GameScreen(DungeonGame game, String heroName, int heroHp, int heroAtk) {
        this.game = game;
        this.heroName = heroName;
        this.heroHp = heroHp;
        this.heroMaxHp = heroHp;
        this.heroAtk = heroAtk;

        batch = new SpriteBatch();

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        try {
            background = new Texture(Gdx.files.internal("battle_bg.png"));
        } catch (Exception e) {
            background = null;
        }

        try {
            heroPortrait = new Texture(Gdx.files.internal(heroName.toLowerCase() + ".png"));
        } catch (Exception e) {
            heroPortrait = null;
        }

        // ── HERO IDLE ─────────────────────────────
        try {
            heroIdle = new Texture(Gdx.files.internal("Hero_idle.png"));
        } catch (Exception e) {
            heroIdle = null;
        }

// ── HERO WALK ─────────────────────────────
        heroWalkFrames = new Texture[5];

        for (int i = 0; i < 5; i++) {

            try {

                heroWalkFrames[i] = new Texture(
                    Gdx.files.internal("Hero_walk/Hero_walk" + (i + 1) + ".png")
                );

            } catch (Exception e) {

                heroWalkFrames[i] = null;
            }
        }

// ── HERO ATTACK ───────────────────────────
        heroAttackFrames = new Texture[5];

        for (int i = 0; i < 5; i++) {

            try {

                heroAttackFrames[i] = new Texture(
                    Gdx.files.internal("Hero_attack/hero_attack" + (i + 1) + ".png")
                );

            } catch (Exception e) {

                heroAttackFrames[i] = null;
            }
        }

        bigFont = new BitmapFont();
        bigFont.getData().setScale(2f);
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        logFont = new BitmapFont();
        logFont.getData().setScale(1.35f);
        tabFont = new BitmapFont();
        tabFont.getData().setScale(1.5f);

        loadFloor(1);
    }

    private void loadFloor(int f) {
        noteDismissed = floorNotes[f - 1].isEmpty();
        battleOver = false;
        won = false;
        playerTurn = true;
        battleLog = "Floor " + f + " starts! Your turn.";
        subLog = "";
        enemyTurnCount = 0;
        activeTab = -1;
        currentEnemyFrame = 0;
        currentHeroFrame = 0;
        heroState = HeroState.IDLE;
        isHeroAttacking = false;
        isEnemyAttacking = false;
        heroX = 120f; // Қабат ауысқанда бастапқы орнына қайтару

        if (enemyFrames != null) {
            for (Texture t : enemyFrames) {
                if (t != null) t.dispose();
            }
        }

        int frameCount = 6;

        switch (f) {
            case 1:
                enemyName = "SKELETON";
                enemyType = "boss";
                enemyHp = 35;
                enemyMaxHp = 35;
                enemyAtk = 6;
                frameCount = 6;
                break;
            case 2:
                enemyName = "ZOMBIE";
                enemyType = "boss2";
                enemyHp = 50;
                enemyMaxHp = 50;
                enemyAtk = 8;
                frameCount = 4;
                break;
            case 3:
                enemyName = "DARK MAGE";
                enemyType = "boss3";
                enemyHp = 65;
                enemyMaxHp = 65;
                enemyAtk = 12;
                frameCount = 6;
                break;
            case 4:
                enemyName = "DRAGON BOSS";
                enemyType = "boss4";
                enemyHp = 120;
                enemyMaxHp = 120;
                enemyAtk = 18;
                frameCount = 5;
                break;
        }

        enemyFrames = new Texture[frameCount];
        for (int i = 0; i < frameCount; i++) {
            String path = "";
            if (f == 1) path = "Boss_attack/boss_attack" + (i + 1) + ".png";
            else if (f == 2) path = "Boss2_attack/boss2_attack" + (i + 1) + ".png";
            else if (f == 3) path = "Boss3_attack/boss3_attack" + (i + 1) + ".png";
            else if (f == 4) path = "Boss4_attack/boss4_attack" + (i + 1) + ".png";

            try {
                enemyFrames[i] = new Texture(Gdx.files.internal(path));
            } catch (Exception e) {
                enemyFrames[i] = null;
            }
        }
    }

    // ── RENDER ────────────────────────────────────────────────────
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.04f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (noteDismissed && playerTurn && !battleOver) {

            boolean moving = false;

            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) ||
                Gdx.input.isKeyPressed(Input.Keys.A)) {

                heroX -= heroSpeed * delta;
                moving = true;
            }

            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) ||
                Gdx.input.isKeyPressed(Input.Keys.D)) {

                heroX += heroSpeed * delta;
                moving = true;
            }

            if (!isHeroAttacking) {

                if (moving) {
                    heroState = HeroState.WALK;
                } else {
                    heroState = HeroState.IDLE;
                }
            }

            if (heroX < 10f)
                heroX = 10f;

            if (heroX > DungeonGame.WIDTH - 200f)
                heroX = DungeonGame.WIDTH - 200f;
        }
        animationTimer += delta;
        if (animationTimer >= FRAME_DURATION) {
            animationTimer = 0f;

            // ── WALK ─────────────────────────────
            if (heroState == HeroState.WALK) {

                currentHeroFrame++;

                if (currentHeroFrame >= heroWalkFrames.length) {

                    currentHeroFrame = 0;
                }
            }

// ── ATTACK ───────────────────────────
            if (heroState == HeroState.ATTACK) {

                currentHeroFrame++;

                if (currentHeroFrame >= heroAttackFrames.length) {

                    currentHeroFrame = 0;

                    isHeroAttacking = false;

                    heroState = HeroState.IDLE;
                }
            }

            // БОСС: Тек шабуыл кезінде ғана анимация жүреді
            if (isEnemyAttacking && enemyFrames != null && enemyFrames.length > 0) {
                currentEnemyFrame++;
                if (currentEnemyFrame >= enemyFrames.length) {
                    currentEnemyFrame = 0;
                    isEnemyAttacking = false; // Шабуыл бітті, қатып тұру күйі
                }
            }
        }

        if (!playerTurn && !battleOver) {
            enemyTimer -= delta;
            if (enemyTimer <= 0) doEnemyTurn();
        }

        batch.begin();

        // Background
        if (background != null) {
            batch.setColor(1, 1, 1, 0.5f);
            batch.draw(background, 0, 0, DungeonGame.WIDTH, DungeonGame.HEIGHT);
            batch.setColor(1, 1, 1, 1);
        }

        // ── ҰРЫС АЛАҢЫ ──
        if (noteDismissed) {
            Texture currentHeroTexture = null;

            if (heroState == HeroState.IDLE) {

                currentHeroTexture = heroIdle;
            } else if (heroState == HeroState.WALK) {

                currentHeroTexture = heroWalkFrames[currentHeroFrame];
            } else if (heroState == HeroState.ATTACK) {

                currentHeroTexture = heroAttackFrames[currentHeroFrame];
            }

            if (currentHeroTexture != null) {

                batch.draw(currentHeroTexture, heroX, heroY, 250, 250);
            }
            if (enemyFrames != null && enemyFrames[currentEnemyFrame] != null && enemyHp > 0) {
                float size = floor == 4 ? 300f : 250f;
                float enemyX = DungeonGame.WIDTH - size - 120;
                float enemyY = 210;

                Texture currentBossTxt = enemyFrames[currentEnemyFrame];

                if (floor >= 2 && floor <= 4) {
                    batch.draw(currentBossTxt, enemyX + size, enemyY, -size, size);
                } else {
                    batch.draw(currentBossTxt, enemyX, enemyY, size, size);
                }
            }
        }

        // ── TOP HUD ──────────────────────────────────────────────────
        fillRect(0f, 0f, 0f, 0.75f, 0, DungeonGame.HEIGHT - 90, DungeonGame.WIDTH, 90);
        fillRect(0.4f, 0.35f, 0.1f, 1f, 0, DungeonGame.HEIGHT - 91, DungeonGame.WIDTH, 2);

        // FLOOR label
        bigFont.setColor(Color.GOLD);
        bigFont.draw(batch, "FLOOR " + floor, DungeonGame.WIDTH / 2f - 50, DungeonGame.HEIGHT - 8);

        // HERO HUD block
        float portraitSize = 64f;
        float portraitX = 12f;
        float portraitY = DungeonGame.HEIGHT - 82f;
        if (heroPortrait != null) {
            fillRect(0.2f, 0.5f, 0.2f, 1f, portraitX - 2, portraitY - 2, portraitSize + 4, portraitSize + 4);
            batch.draw(heroPortrait, portraitX, portraitY, portraitSize, portraitSize);
        }

        float heroInfoX = portraitX + portraitSize + 10f;
        font.setColor(Color.WHITE);
        font.draw(batch, heroName + "  LVL " + heroLevel, heroInfoX, DungeonGame.HEIGHT - 14);
        drawBar(heroInfoX, DungeonGame.HEIGHT - 44, 210, 14, heroHp, heroMaxHp, Color.GREEN);
        logFont.setColor(new Color(0.5f, 1f, 0.5f, 1f));
        logFont.draw(batch, "HP " + heroHp + "/" + heroMaxHp, heroInfoX, DungeonGame.HEIGHT - 52);
        logFont.setColor(new Color(1f, 0.7f, 0.2f, 1f));
        logFont.draw(batch, "ATK " + heroAtk, heroInfoX + 110, DungeonGame.HEIGHT - 52);

        // ENEMY HUD block
        if (enemyMaxHp > 0) {
            float hudSpriteSize = 64f;
            float spriteX = DungeonGame.WIDTH - hudSpriteSize - 12f;
            float spriteY = DungeonGame.HEIGHT - 82f;

            if (enemyFrames != null && enemyFrames[currentEnemyFrame] != null) {
                fillRect(0.5f, 0.1f, 0.1f, 1f, spriteX - 2, spriteY - 2, hudSpriteSize + 4, hudSpriteSize + 4);
                if (floor >= 2 && floor <= 4) {
                    batch.draw(enemyFrames[currentEnemyFrame], spriteX + hudSpriteSize, spriteY, -hudSpriteSize, hudSpriteSize);
                } else {
                    batch.draw(enemyFrames[currentEnemyFrame], spriteX, spriteY, hudSpriteSize, hudSpriteSize);
                }
            }

            float enemyInfoRight = spriteX - 10f;
            float enemyBarW = 210f;
            float enemyBarX = enemyInfoRight - enemyBarW;

            font.setColor(new Color(1f, 0.35f, 0.35f, 1f));
            font.draw(batch, enemyName, enemyBarX, DungeonGame.HEIGHT - 14);
            drawBar(enemyBarX, DungeonGame.HEIGHT - 44, enemyBarW, 14, enemyHp, enemyMaxHp, Color.RED);
            logFont.setColor(new Color(1f, 0.5f, 0.5f, 1f));
            logFont.draw(batch, "HP " + Math.max(enemyHp, 0) + "/" + enemyMaxHp, enemyBarX, DungeonGame.HEIGHT - 52);
        }

        // ── LORE NOTE ──
        if (!noteDismissed) {
            fillRect(0.08f, 0.06f, 0.03f, 0.92f, 80, 180, DungeonGame.WIDTH - 160, 180);
            fillRect(0.7f, 0.6f, 0.1f, 1f, 80, 356, DungeonGame.WIDTH - 160, 3);
            logFont.setColor(new Color(0.9f, 0.85f, 0.6f, 1f));
            logFont.draw(batch, "You found a blood-stained parchment:", 110, 348);
            logFont.setColor(Color.WHITE);
            logFont.draw(batch, floorNotes[floor - 1], 100, 318);
            logFont.setColor(Color.GOLD);
            logFont.draw(batch, "[ Press ENTER to continue ]", 240, 200);
            batch.end();
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) noteDismissed = true;
            return;
        }

        // ── BATTLE LOG ──
        fillRect(0, 0, 0, 0.55f, 0, 130, DungeonGame.WIDTH, 75);
        logFont.setColor(Color.WHITE);
        logFont.draw(batch, battleLog, 20, 192);
        logFont.setColor(Color.LIGHT_GRAY);
        logFont.draw(batch, subLog, 20, 162);

        // ── ACTION PANEL ──
        if (playerTurn && !battleOver) {
            drawTabPanel();
        }

        // ── BATTLE OVER ──
        if (battleOver) {
            if (won) {
                if (floor == 4) {
                    fillRect(0, 0, 0, 0.85f, 60, 150, DungeonGame.WIDTH - 120, 280);
                    bigFont.setColor(Color.GOLD);
                    bigFont.draw(batch, "THE BOSS FALLS...", 150, 400);
                    logFont.setColor(Color.WHITE);
                    logFont.draw(batch, "His helmet shatters. You see YOUR face. Older. Exhausted.", 80, 360);
                    logFont.draw(batch, "Boss: \"Run... She doesn't need saving. She needs a vessel...\"", 80, 330);
                    logFont.setColor(new Color(0.8f, 0.4f, 0.9f, 1f));
                    logFont.draw(batch, "Princess: \"Perfect. You are perfect. Come to the throne...\"", 80, 295);
                    logFont.setColor(Color.GOLD);
                    logFont.draw(batch, "[ ENTER ] — Accept your fate", 230, 200);
                    batch.end();
                    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER))
                        game.setScreen(new GameOverScreen(game));
                    return;
                } else {
                    bigFont.setColor(Color.GOLD);
                    bigFont.draw(batch, "VICTORY!", 290, 120);
                    font.setColor(Color.WHITE);
                    font.draw(batch, "[ ENTER ] next floor", 280, 80);
                    batch.end();
                    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) nextFloor();
                    return;
                }
            } else {
                batch.end();
                game.setScreen(new GameOverScreen(game));
                return;
            }
        }

        batch.end();
    }

    private void drawTabPanel() {
        float mx = Gdx.input.getX();
        float my = DungeonGame.HEIGHT - Gdx.input.getY();
        boolean click = Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) activeTab = (activeTab == 0) ? -1 : 0;
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) activeTab = (activeTab == 1) ? -1 : 1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) activeTab = (activeTab == 2) ? -1 : 2;

        for (int i = 0; i < 3; i++) {
            float tx = TAB_START_X + i * (TAB_W + TAB_GAP);
            boolean hov = mx > tx && mx < tx + TAB_W && my > TAB_Y && my < TAB_Y + TAB_H;
            boolean sel = (activeTab == i);
            Color c = TAB_COLORS[i];

            float bg = sel ? 0.35f : (hov ? 0.25f : 0.12f);
            fillRect(c.r * bg, c.g * bg, c.b * bg, 0.95f, tx, TAB_Y, TAB_W, TAB_H);

            float lineH = sel ? 4f : 2f;
            fillRect(c.r, c.g, c.b, 1f, tx, TAB_Y + TAB_H - lineH, TAB_W, lineH);

            if (sel) fillRect(c.r, c.g, c.b, 0.8f, tx, TAB_Y, 3, TAB_H);

            tabFont.setColor(sel ? Color.WHITE : (hov ? c : new Color(c.r * 0.7f, c.g * 0.7f, c.b * 0.7f, 1f)));
            tabFont.draw(batch, TAB_LABELS[i], tx + 14, TAB_Y + 33);

            if (hov && click) activeTab = sel ? -1 : i;
        }

        if (activeTab >= 0) {
            int t = activeTab;
            fillRect(0f, 0f, 0f, 0.75f, 0, SUB_Y, DungeonGame.WIDTH, SUB_PANEL_H);
            fillRect(TAB_COLORS[t].r, TAB_COLORS[t].g, TAB_COLORS[t].b, 0.6f, 0, SUB_Y + SUB_PANEL_H - 2, DungeonGame.WIDTH, 2);

            int count = SUB_LABELS[t].length;
            float totalW = count * 220f + (count - 1) * 12f;
            float startX = (DungeonGame.WIDTH - totalW) / 2f;

            for (int j = 0; j < count; j++) {
                float sx = startX + j * (220f + 12f);
                float sy = SUB_Y + (SUB_PANEL_H - SUB_H) / 2f;
                Color sc = SUB_COLORS[t][j];
                boolean hov = mx > sx && mx < sx + 220f && my > sy && my < sy + SUB_H;

                fillRect(sc.r * (hov ? 0.3f : 0.15f), sc.g * (hov ? 0.3f : 0.15f), sc.b * (hov ? 0.3f : 0.15f), 0.92f, sx, sy, 220f, SUB_H);
                fillRect(sc.r, sc.g, sc.b, 1f, sx, sy + SUB_H - 3, 220f, 3);

                tabFont.setColor(hov ? Color.WHITE : sc);
                tabFont.draw(batch, SUB_LABELS[t][j], sx + 16, sy + 37);

                if (hov && click) {
                    onAction(SUB_ACTIONS[t][j]);
                    activeTab = -1;
                }
            }
            handleSubKeys(t);
        }
    }

    private void handleSubKeys(int t) {
        if (t == 0) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
                onAction(1);
                activeTab = -1;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
                onAction(2);
                activeTab = -1;
            }
        } else if (t == 1) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
                onAction(3);
                activeTab = -1;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
                onAction(4);
                activeTab = -1;
            }
        } else if (t == 2) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
                onAction(5);
                activeTab = -1;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
                onAction(6);
                activeTab = -1;
            }
        }
    }

    private void onAction(int action) {
        if (!playerTurn || battleOver) return;
        isHeroAttacking = true;
        heroState = HeroState.ATTACK;
        currentHeroFrame = 0;
        switch (action) {
            case 1: {
                int dmg = (int) (heroAtk * 0.8f);
                enemyHp -= dmg;
                battleLog = "Quick Strike! " + dmg + " damage.";
                break;
            }
            case 2: {
                int dmg = heroAtk * 2;
                enemyHp -= dmg;
                battleLog = "HEAVY BLOW! " + dmg + " damage!";
                break;
            }
            case 3: {
                int dmg = heroAtk;
                enemyHp -= dmg;
                int drain = dmg / 2;
                heroHp = Math.min(heroHp + drain, heroMaxHp);
                battleLog = "DRAIN! " + dmg + " dmg, +" + drain + " HP stolen.";
                break;
            }
            case 4: {
                int dmg = (int) (heroAtk * 1.5f);
                enemyHp -= dmg;
                battleLog = "FIRE BOLT! " + dmg + " damage!";
                break;
            }
            case 5: {
                int heal = 20;
                heroHp = Math.min(heroHp + heal, heroMaxHp);
                battleLog = "You use a Potion. +" + heal + " HP.";
                subLog = heroName + " HP: " + heroHp + "/" + heroMaxHp;
                playerTurn = false;
                enemyTimer = 1.0f;
                isHeroAttacking = false; // Потция ішкенде шабуыл анимациясы керек емес
                return;
            }
            case 6: {
                int dmg = heroAtk * 3;
                enemyHp -= dmg;
                battleLog = "SCROLL OF DESTRUCTION! " + dmg + " damage!";
                break;
            }
        }

        subLog = enemyName + " HP: " + Math.max(enemyHp, 0);
        if (enemyHp <= 0) {
            battleLog = enemyName + " defeated!";
            subLog = "+50 XP";
            gainXp(50);
            battleOver = true;
            won = true;
        } else {
            playerTurn = false;
            enemyTimer = 1.0f;
        }
    }

    private void doEnemyTurn() {
        enemyTurnCount++;
        int dmg = enemyAtk;

        isEnemyAttacking = true;
        currentEnemyFrame = 0;

        if (enemyType.equals("boss2")) {
            if (enemyTurnCount % 3 == 0 && enemyHp < enemyMaxHp) {
                int heal = 15;
                enemyHp = Math.min(enemyHp + heal, enemyMaxHp);
                battleLog = enemyName + " regenerates! +" + heal + " HP";
                subLog = enemyName + " HP: " + enemyHp + "/" + enemyMaxHp;
                playerTurn = true;
                return;
            }
        } else if (enemyType.equals("boss3")) {
            if (Math.random() < 0.5) {
                dmg *= 2;
                battleLog = enemyName + " casts DARK BOLT! " + dmg + " damage!!";
            } else {
                battleLog = enemyName + " attacks for " + dmg + " damage.";
            }
        } else if (enemyType.equals("boss4")) {
            if (Math.random() < 0.3) {
                dmg *= 3;
                battleLog = enemyName + " BREATHES FIRE! CRITICAL " + dmg + " damage!!!";
            } else {
                battleLog = enemyName + " claws you for " + dmg + " damage.";
            }
        } else {
            battleLog = enemyName + " hits you for " + dmg + " damage!";
        }

        heroHp -= dmg;
        subLog = heroName + " HP: " + Math.max(heroHp, 0) + "/" + heroMaxHp;
        if (heroHp <= 0) {
            battleOver = true;
            won = false;
        } else {
            playerTurn = true;
        }
    }

    private void gainXp(int amount) {
        heroXp += amount;
        if (heroXp >= 100) {
            heroXp = 0;
            heroLevel++;
            heroAtk += 5;
            heroMaxHp += 20;
            heroHp = heroMaxHp;
            battleLog = "LEVEL UP! Level " + heroLevel + " — ATK " + heroAtk;
        }
    }

    private void nextFloor() {
        floor++;
        if (floor > 4) {
            game.setScreen(new GameOverScreen(game));
            return;
        }
        loadFloor(floor);
    }

    private void drawBar(float x, float y, float w, float h, int cur, int max, Color c) {
        fillRect(0.2f, 0.2f, 0.2f, 1f, x, y, w, h);
        float pct = Math.max(0, (float) cur / max);
        fillRect(c.r, c.g, c.b, 1f, x, y, w * pct, h);
    }

    private void fillRect(float r, float g, float b, float a, float x, float y, float w, float h) {
        batch.setColor(r, g, b, a);
        batch.draw(pixel, x, y, w, h);
        batch.setColor(1, 1, 1, 1);
    }

    @Override
    public void show() {
    }

    @Override
    public void resize(int w, int h) {
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
        batch.dispose();
        pixel.dispose();
        bigFont.dispose();
        font.dispose();
        logFont.dispose();
        tabFont.dispose();
        if (background != null) background.dispose();
        if (heroPortrait != null) heroPortrait.dispose();

        if (heroIdle != null)
            heroIdle.dispose();

        if (heroWalkFrames != null) {

            for (Texture t : heroWalkFrames) {

                if (t != null)
                    t.dispose();
            }
        }

        if (heroAttackFrames != null) {

            for (Texture t : heroAttackFrames) {

                if (t != null)
                    t.dispose();
            }
        }
    }
}
