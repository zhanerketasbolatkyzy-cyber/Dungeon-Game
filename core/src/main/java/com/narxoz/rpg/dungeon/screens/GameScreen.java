package com.narxoz.rpg.dungeon.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.audio.Music;
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

    // ── АНИМАЦИЯ ──────────────────────────────────────────────────
    private Texture heroIdle;
    private Texture[] heroWalkFrames;
    private Texture[] heroAttackFrames;
    private Texture[] enemyFrames;
    private int currentHeroFrame = 0;
    private int currentEnemyFrame = 0;
    private float animationTimer = 0f;
    private final float FRAME_DURATION = 0.15f;

    // ── HERO STATE ────────────────────────────────────────────────
    private enum HeroState { IDLE, WALK, ATTACK }
    private HeroState heroState = HeroState.IDLE;
    private boolean isEnemyAttacking = false;
    private boolean isHeroAttacking = false;

    // ── HERO POSITION ─────────────────────────────────────────────
    private float heroX = 120f;
    private float heroY = 220f;
    private float heroSpeed = 250f;

    // ── ENEMY POSITION ────────────────────────────────────────────
    private float enemyScreenX;
    private float enemyWidth = 250f;

    // ── ATTACK RANGE ──────────────────────────────────────────────
    // Hero героға жақын болғанда ғана damage береді
    private static final float ATTACK_RANGE = 320f; // пиксель
    private boolean tooFarMessage = false;
    private float tooFarTimer = 0f;

    // ── ZOMBIE CURSE CUTSCENE ──────────────────────────────────────
    private boolean showCurseScene = false;      // floor 2 женгенде сцена
    private float curseTimer = 0f;               // сцена уақыты
    private int cursePhase = 0;                  // 0,1,2,3 — мәтін фазалары
    private boolean heroIsUndead = false;         // curse қабылданды ма
    private Color heroTint = new Color(1,1,1,1); // герой түсі (undead = жасыл)

    // ── ЗВУКТАР ────────────────────────────────────────────────────
    private Sound soundAttack;
    private Sound soundHit;
    private Sound soundDeath;
    private Sound soundStep;
    private Sound soundHeal;
    private Music bgMusic;

    private BitmapFont bigFont, font, logFont, tabFont;

    // Hero stats
    private String heroName;
    private String heroDisplayName; // undead болса өзгереді
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

    // ── COMBO ─────────────────────────────────────────────────────
    private int comboCount = 0;
    private float comboTimer = 0f;
    private final float COMBO_TIMEOUT = 3.0f;

    // ── HIT FLASH + SHAKE ─────────────────────────────────────────
    private float hitFlashTimer = 0f;
    private boolean heroHitFlash = false;
    private boolean enemyHitFlash = false;
    private float shakeTimer = 0f;
    private float shakeOffX = 0f, shakeOffY = 0f;

    // ── TAB PANEL ─────────────────────────────────────────────────
    private int activeTab = -1;
    private static final float TAB_Y = 10f, TAB_H = 46f, TAB_W = 180f, TAB_GAP = 8f;
    private static final float TAB_START_X = (DungeonGame.WIDTH - (3 * TAB_W + 2 * TAB_GAP)) / 2f;
    private static final float SUB_PANEL_H = 64f, SUB_Y = TAB_Y + TAB_H + 6f, SUB_H = 52f;
    private static final String[] TAB_LABELS = {"[A] ATTACK", "[M] MAGIC", "[I] ITEMS"};
    private static final Color[] TAB_COLORS = {Color.CYAN, new Color(0.7f,0.3f,1f,1f), Color.ORANGE};
    private static final String[][] SUB_LABELS = {
        {"[1] QUICK", "[2] HEAVY"},
        {"[3] DRAIN", "[4] FIRE BOLT"},
        {"[H] HEAL", "[5] SCROLL"}
    };
    private static final Color[][] SUB_COLORS = {
        {Color.CYAN, Color.ORANGE},
        {new Color(0.5f,1f,0.5f,1f), new Color(1f,0.4f,0.2f,1f)},
        {Color.GREEN, new Color(1f,0.85f,0.2f,1f)}
    };
    private static final int[][] SUB_ACTIONS = {{1,2},{3,4},{5,6}};

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
        this.heroDisplayName = heroName;
        this.heroHp = heroHp;
        this.heroMaxHp = heroHp;
        this.heroAtk = heroAtk;

        batch = new SpriteBatch();

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE); pm.fill();
        pixel = new Texture(pm); pm.dispose();

        try { background = new Texture(Gdx.files.internal("battle_bg.png")); }
        catch (Exception e) { background = null; }
        try { heroPortrait = new Texture(Gdx.files.internal(heroName.toLowerCase() + ".png")); }
        catch (Exception e) { heroPortrait = null; }
        try { heroIdle = new Texture(Gdx.files.internal("Hero_idle.png")); }
        catch (Exception e) { heroIdle = null; }

        heroWalkFrames = new Texture[5];
        for (int i = 0; i < 5; i++) {
            try { heroWalkFrames[i] = new Texture(Gdx.files.internal("Hero_walk/Hero_walk" + (i+1) + ".png")); }
            catch (Exception e) { heroWalkFrames[i] = null; }
        }
        heroAttackFrames = new Texture[5];
        for (int i = 0; i < 5; i++) {
            try { heroAttackFrames[i] = new Texture(Gdx.files.internal("Hero_attack/hero_attack" + (i+1) + ".png")); }
            catch (Exception e) { heroAttackFrames[i] = null; }
        }

        try { soundAttack = Gdx.audio.newSound(Gdx.files.internal("sounds/attack.wav")); } catch (Exception e) {}
        try { soundHit    = Gdx.audio.newSound(Gdx.files.internal("sounds/hit.wav")); }    catch (Exception e) {}
        try { soundDeath  = Gdx.audio.newSound(Gdx.files.internal("sounds/death.wav")); }  catch (Exception e) {}
        try { soundStep   = Gdx.audio.newSound(Gdx.files.internal("sounds/step.wav")); }   catch (Exception e) {}
        try { soundHeal   = Gdx.audio.newSound(Gdx.files.internal("sounds/heal.wav")); }   catch (Exception e) {}
        try {
            bgMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/bg_dungeon.ogg"));
            bgMusic.setLooping(true);
            bgMusic.setVolume(0.35f);
            bgMusic.play();
        } catch (Exception e) { bgMusic = null; }

        bigFont = new BitmapFont(); bigFont.getData().setScale(2f);
        font    = new BitmapFont(); font.getData().setScale(1.5f);
        logFont = new BitmapFont(); logFont.getData().setScale(1.35f);
        tabFont = new BitmapFont(); tabFont.getData().setScale(1.5f);

        loadFloor(1);
    }

    private void playSound(Sound s) { if (s != null) s.play(1.0f); }

    private void loadFloor(int f) {
        noteDismissed = false;
        battleOver = false; won = false; playerTurn = true;
        battleLog = "Floor " + f + " starts! Your turn.";
        subLog = "";
        enemyTurnCount = 0; activeTab = -1;
        currentEnemyFrame = 0; currentHeroFrame = 0;
        heroState = HeroState.IDLE;
        isHeroAttacking = false; isEnemyAttacking = false;
        heroX = 120f;
        comboCount = 0; comboTimer = 0f;
        showCurseScene = false; curseTimer = 0f; cursePhase = 0;
        tooFarMessage = false; tooFarTimer = 0f;

        if (enemyFrames != null)
            for (Texture t : enemyFrames) if (t != null) t.dispose();

        int frameCount = 6;
        switch (f) {
            case 1: enemyName="SKELETON";   enemyType="boss";  enemyHp=35;  enemyMaxHp=35;  enemyAtk=6;  frameCount=6; enemyWidth=250f; break;
            case 2: enemyName="ZOMBIE";     enemyType="boss2"; enemyHp=50;  enemyMaxHp=50;  enemyAtk=8;  frameCount=4; enemyWidth=250f; break;
            case 3: enemyName="DARK MAGE";  enemyType="boss3"; enemyHp=65;  enemyMaxHp=65;  enemyAtk=12; frameCount=6; enemyWidth=250f; break;
            case 4: enemyName="DRAGON BOSS";enemyType="boss4"; enemyHp=120; enemyMaxHp=120; enemyAtk=18; frameCount=5; enemyWidth=300f; break;
        }

        enemyScreenX = DungeonGame.WIDTH - enemyWidth - 120;

        enemyFrames = new Texture[frameCount];
        for (int i = 0; i < frameCount; i++) {
            String path = "";
            if (f==1)      path = "Boss_attack/boss_attack"   + (i+1) + ".png";
            else if (f==2) path = "Boss2_attack/boss2_attack" + (i+1) + ".png";
            else if (f==3) path = "Boss3_attack/boss3_attack" + (i+1) + ".png";
            else if (f==4) path = "Boss4_attack/boss4_attack" + (i+1) + ".png";
            try { enemyFrames[i] = new Texture(Gdx.files.internal(path)); }
            catch (Exception e) { enemyFrames[i] = null; }
        }
    }

    // ── RENDER ────────────────────────────────────────────────────
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.04f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // ── ZOMBIE CURSE CUTSCENE ──────────────────────────────────
        if (showCurseScene) {
            renderCurseScene(delta);
            return;
        }

        // Timers
        if (comboCount > 0) { comboTimer -= delta; if (comboTimer <= 0) comboCount = 0; }
        if (hitFlashTimer > 0) {
            hitFlashTimer -= delta;
            if (hitFlashTimer <= 0) { heroHitFlash = false; enemyHitFlash = false; }
        }
        if (shakeTimer > 0) {
            shakeTimer -= delta;
            shakeOffX = (float)(Math.random()*8-4);
            shakeOffY = (float)(Math.random()*8-4);
        } else { shakeOffX=0; shakeOffY=0; }
        if (tooFarTimer > 0) { tooFarTimer -= delta; if (tooFarTimer <= 0) tooFarMessage = false; }

        // ── MOVEMENT ──
        if (noteDismissed && playerTurn && !battleOver) {
            boolean moving = false;
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                heroX -= heroSpeed * delta; moving = true;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                heroX += heroSpeed * delta; moving = true;
            }
            if (!isHeroAttacking) heroState = moving ? HeroState.WALK : HeroState.IDLE;
            if (heroX < 10f) heroX = 10f;
            float collisionLimit = enemyScreenX - 250f - 30f;
            if (heroX > collisionLimit) heroX = collisionLimit;
        }

        // ── ANIMATION ──
        animationTimer += delta;
        if (animationTimer >= FRAME_DURATION) {
            animationTimer = 0f;
            if (heroState == HeroState.WALK)
                currentHeroFrame = (currentHeroFrame+1) % heroWalkFrames.length;
            if (heroState == HeroState.ATTACK) {
                currentHeroFrame++;
                if (currentHeroFrame >= heroAttackFrames.length) {
                    currentHeroFrame = 0; isHeroAttacking = false; heroState = HeroState.IDLE;
                }
            }
            if (isEnemyAttacking && enemyFrames != null) {
                currentEnemyFrame++;
                if (currentEnemyFrame >= enemyFrames.length) {
                    currentEnemyFrame = 0; isEnemyAttacking = false;
                }
            }
        }

        if (!playerTurn && !battleOver) { enemyTimer -= delta; if (enemyTimer <= 0) doEnemyTurn(); }

        // ── DRAW ──
        batch.begin();

        if (background != null) {
            batch.setColor(1,1,1, heroIsUndead ? 0.35f : 0.5f);
            batch.draw(background, shakeOffX, shakeOffY, DungeonGame.WIDTH, DungeonGame.HEIGHT);
            batch.setColor(1,1,1,1);
        }

        if (noteDismissed) {
            // HERO
            Texture heroTex = null;
            if (heroState == HeroState.IDLE)        heroTex = heroIdle;
            else if (heroState == HeroState.WALK)   heroTex = heroWalkFrames[currentHeroFrame];
            else if (heroState == HeroState.ATTACK) heroTex = heroAttackFrames[currentHeroFrame];

            if (heroTex != null) {
                // Undead hero — жасыл-сұр түс
                if (heroHitFlash)       batch.setColor(1f, 0.2f, 0.2f, 1f);
                else if (heroIsUndead)  batch.setColor(0.5f, 1f, 0.5f, 0.9f);
                batch.draw(heroTex, heroX+shakeOffX, heroY+shakeOffY, 250, 250);
                batch.setColor(1,1,1,1);
            }

            // ENEMY
            if (enemyFrames != null && enemyFrames[currentEnemyFrame] != null && enemyHp > 0) {
                float size = (floor==4) ? 300f : 250f;
                float eX = enemyScreenX + shakeOffX;
                float eY = 210 + shakeOffY;
                if (enemyHitFlash) batch.setColor(1f, 0.3f, 0.3f, 1f);
                if (floor == 1) batch.draw(enemyFrames[currentEnemyFrame], eX, eY, size, size);
                else            batch.draw(enemyFrames[currentEnemyFrame], eX+size, eY, -size, size);
                batch.setColor(1,1,1,1);
            }

            // ── ATTACK RANGE INDICATOR ─────────────────────────────
            // Hero жақын болса жасыл, алыс болса қызыл сызық
            if (playerTurn && !battleOver) {
                float heroCenter = heroX + 125f;
                float distToEnemy = enemyScreenX - (heroX + 250f);
                boolean inRange = distToEnemy <= ATTACK_RANGE;
                Color rangeColor = inRange ? new Color(0f,1f,0f,0.25f) : new Color(1f,0f,0f,0.15f);
                fillRect(rangeColor.r, rangeColor.g, rangeColor.b, rangeColor.a,
                    heroX+250f, heroY+100f, Math.min(distToEnemy, ATTACK_RANGE), 6f);

                // "TOO FAR!" хабарламасы
                if (tooFarMessage) {
                    font.setColor(new Color(1f,0.3f,0.3f,1f));
                    font.draw(batch, "TOO FAR! Move closer!", heroX - 20f, heroY + 280f);
                }
            }
        }

        // ── TOP HUD ──
        fillRect(0f,0f,0f,0.75f, 0, DungeonGame.HEIGHT-90, DungeonGame.WIDTH, 90);
        fillRect(0.4f,0.35f,0.1f,1f, 0, DungeonGame.HEIGHT-91, DungeonGame.WIDTH, 2);

        bigFont.setColor(Color.GOLD);
        bigFont.draw(batch, "FLOOR "+floor, DungeonGame.WIDTH/2f-50, DungeonGame.HEIGHT-8);

        // Undead болса HUD атауы өзгереді
        float portraitSize=64f, portraitX=12f, portraitY=DungeonGame.HEIGHT-82f;
        if (heroPortrait != null) {
            fillRect(heroIsUndead ? 0.1f : 0.2f,
                heroIsUndead ? 0.3f : 0.5f,
                heroIsUndead ? 0.1f : 0.2f, 1f,
                portraitX-2, portraitY-2, portraitSize+4, portraitSize+4);
            if (heroIsUndead) batch.setColor(0.5f,1f,0.5f,1f);
            batch.draw(heroPortrait, portraitX, portraitY, portraitSize, portraitSize);
            batch.setColor(1,1,1,1);
        }

        float heroInfoX = portraitX+portraitSize+10f;
        font.setColor(heroIsUndead ? new Color(0.4f,1f,0.4f,1f) : Color.WHITE);
        font.draw(batch, heroDisplayName+"  LVL "+heroLevel, heroInfoX, DungeonGame.HEIGHT-14);
        drawBar(heroInfoX, DungeonGame.HEIGHT-44, 210, 14, heroHp, heroMaxHp,
            heroIsUndead ? new Color(0.3f,1f,0.3f,1f) : Color.GREEN);
        logFont.setColor(new Color(0.5f,1f,0.5f,1f));
        logFont.draw(batch, "HP "+heroHp+"/"+heroMaxHp, heroInfoX, DungeonGame.HEIGHT-52);
        logFont.setColor(new Color(1f,0.7f,0.2f,1f));
        logFont.draw(batch, "ATK "+heroAtk, heroInfoX+110, DungeonGame.HEIGHT-52);

        // ENEMY HUD
        if (enemyMaxHp > 0) {
            float hudSz=64f, spX=DungeonGame.WIDTH-hudSz-12f, spY=DungeonGame.HEIGHT-82f;
            if (enemyFrames != null && enemyFrames[currentEnemyFrame] != null) {
                fillRect(0.5f,0.1f,0.1f,1f, spX-2,spY-2,hudSz+4,hudSz+4);
                if (floor==1) batch.draw(enemyFrames[currentEnemyFrame], spX,spY,hudSz,hudSz);
                else          batch.draw(enemyFrames[currentEnemyFrame], spX+hudSz,spY,-hudSz,hudSz);
            }
            float barX = spX-10f-210f;
            font.setColor(new Color(1f,0.35f,0.35f,1f));
            font.draw(batch, enemyName, barX, DungeonGame.HEIGHT-14);
            drawBar(barX, DungeonGame.HEIGHT-44, 210, 14, enemyHp, enemyMaxHp, Color.RED);
            logFont.setColor(new Color(1f,0.5f,0.5f,1f));
            logFont.draw(batch, "HP "+Math.max(enemyHp,0)+"/"+enemyMaxHp, barX, DungeonGame.HEIGHT-52);
        }

        // COMBO
        if (comboCount >= 2) {
            float alpha = Math.min(1f, comboTimer/COMBO_TIMEOUT);
            Color cc = comboCount>=5 ? new Color(1f,0.2f,0.2f,alpha)
                : comboCount>=3 ? new Color(1f,0.6f,0f,alpha)
                : new Color(1f,1f,0f,alpha);
            bigFont.setColor(cc);
            bigFont.draw(batch, comboCount+"x COMBO!", DungeonGame.WIDTH/2f-70, DungeonGame.HEIGHT-110);
        }

        // LORE NOTE
        if (!noteDismissed) {
            fillRect(0.08f,0.06f,0.03f,0.92f, 80,180,DungeonGame.WIDTH-160,180);
            fillRect(0.7f,0.6f,0.1f,1f, 80,356,DungeonGame.WIDTH-160,3);
            logFont.setColor(new Color(0.9f,0.85f,0.6f,1f));
            logFont.draw(batch, "You found a blood-stained parchment:", 110, 348);
            logFont.setColor(Color.WHITE);
            logFont.draw(batch, floorNotes[floor-1], 100, 318);
            logFont.setColor(Color.GOLD);
            logFont.draw(batch, "[ Press ENTER to continue ]", 240, 200);
            batch.end();
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) noteDismissed = true;
            return;
        }

        // BATTLE LOG
        fillRect(0,0,0,0.55f, 0,130,DungeonGame.WIDTH,75);
        logFont.setColor(Color.WHITE);
        logFont.draw(batch, battleLog, 20, 192);
        logFont.setColor(Color.LIGHT_GRAY);
        logFont.draw(batch, subLog, 20, 162);

        if (playerTurn && !battleOver) drawTabPanel();

        // BATTLE OVER
        if (battleOver) {
            if (won) {
                if (floor == 4) {
                    // Dragon Boss женгенде curse сценасы өзі іске қосылады
                    // (onAction ішінде showCurseScene = true деп қойылған)
                    batch.end();
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

    // ══════════════════════════════════════════════════════════════
    // ZOMBIE CURSE CUTSCENE — floor 2 женгенде
    // ══════════════════════════════════════════════════════════════
    private void renderCurseScene(float delta) {
        curseTimer += delta;

        // Фаза алмасу уақыттары
        if (cursePhase == 0 && curseTimer > 2.0f) { cursePhase = 1; curseTimer = 0; }
        if (cursePhase == 1 && curseTimer > 2.5f) { cursePhase = 2; curseTimer = 0; }
        if (cursePhase == 2 && curseTimer > 2.5f) { cursePhase = 3; curseTimer = 0; }

        // Фаза 3 = герой curse қабылдады, ENTER күту
        boolean waitingForEnter = (cursePhase == 3);

        batch.begin();

        // Қара фон, жасыл тұман
        float greenPulse = 0.15f + (float)Math.sin(curseTimer*3) * 0.05f;
        fillRect(0f, greenPulse*0.3f, 0f, 1f, 0, 0, DungeonGame.WIDTH, DungeonGame.HEIGHT);

        // Жасыл тұман шетінде
        fillRect(0f, 0.4f, 0f, 0.3f, 0, 0, DungeonGame.WIDTH, 80);
        fillRect(0f, 0.4f, 0f, 0.3f, 0, DungeonGame.HEIGHT-80, DungeonGame.WIDTH, 80);

        // Dragon Boss текстурасы — сол жақта күйреген
        if (enemyFrames != null && enemyFrames[0] != null) {
            float dSize = 220f;
            float dX = DungeonGame.WIDTH/2f - dSize - 60;
            float dY = 180f;
            // Dragon өліп бара жатыр — қызылдан қараға fade
            float dr = 0.6f - cursePhase*0.15f;
            batch.setColor(dr, 0.1f, 0.1f, 0.6f);
            batch.draw(enemyFrames[0], dX+dSize, dY, -dSize, dSize);
            batch.setColor(1,1,1,1);
        }

        // Hero текстурасы — оң жақта, жасыл curse жайылады
        if (heroIdle != null) {
            float hX = DungeonGame.WIDTH/2f + 30;
            float hY = 180f;
            float pulse = 0.5f + (float)Math.sin(curseTimer*4)*0.2f;
            float greenIntensity = 0.2f + cursePhase*0.25f;
            batch.setColor(0.2f, greenIntensity * pulse + 0.3f, 0.2f, 0.95f);
            batch.draw(heroIdle, hX, hY, 210, 210);
            batch.setColor(1,1,1,1);
        }

        // Мәтін блогы
        fillRect(0f, 0.04f, 0f, 0.90f, 40, 118, DungeonGame.WIDTH-80, 155);
        fillRect(0f, 0.7f, 0.2f, 1f, 40, 270, DungeonGame.WIDTH-80, 2);

        if (cursePhase == 0) {
            // Dragon жеңілді — zombie рухы оянды
            logFont.setColor(new Color(1f, 0.3f, 0.3f, 1f));
            logFont.draw(batch, "The Dragon crumbles. The dungeon falls silent.", 70, 258);
            logFont.setColor(Color.LIGHT_GRAY);
            logFont.draw(batch, "But deep below... a familiar presence stirs.", 70, 225);
            logFont.draw(batch, "The Zombie you once defeated... remembers.", 70, 195);
        } else if (cursePhase == 1) {
            // Zombie рухы сөйлейді
            logFont.setColor(new Color(0.4f, 1f, 0.4f, 1f));
            logFont.draw(batch, "Zombie's Spirit: \"You fought well, warrior.\"", 70, 258);
            logFont.setColor(Color.WHITE);
            logFont.draw(batch, "\"But the dungeon needs no hero. It needs a guardian.\"", 70, 225);
            logFont.setColor(Color.LIGHT_GRAY);
            logFont.draw(batch, "\"The princess was never a prisoner. She is the dungeon.\"", 70, 195);
        } else if (cursePhase == 2) {
            // Curse кіреді
            logFont.setColor(new Color(0.3f, 1f, 0.4f, 1f));
            logFont.draw(batch, "A cold mist crawls up your armor.", 70, 258);
            logFont.setColor(Color.WHITE);
            logFont.draw(batch, "You try to resist. But your legs won't move.", 70, 225);
            logFont.setColor(new Color(0.5f, 1f, 0.5f, 1f));
            logFont.draw(batch, "The curse doesn't hurt. It feels like... home.", 70, 195);
        } else {
            // Финал — герой undead guardian болды
            bigFont.setColor(new Color(0.1f, 1f, 0.3f, 1f));
            bigFont.draw(batch, "UNDEAD GUARDIAN", DungeonGame.WIDTH/2f-140, 275);
            logFont.setColor(Color.WHITE);
            logFont.draw(batch, "You are no longer a hero. You are the dungeon's protector.", 55, 232);
            logFont.setColor(new Color(0.4f, 1f, 0.4f, 1f));
            logFont.draw(batch, "The Zombie's curse flows through you. You are complete.", 62, 200);
            logFont.setColor(Color.GOLD);
            logFont.draw(batch, "[ ENTER ] — Accept your true purpose", DungeonGame.WIDTH/2f-155, 148);
        }

        batch.end();

        // ENTER басқанда герой undead guardian болады → GameOver экраны
        if (waitingForEnter && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            showCurseScene = false;
            game.setScreen(new GameOverScreen(game));
        }
    }

    // ── ATTACK RANGE CHECK ────────────────────────────────────────
    private boolean isInAttackRange() {
        float distToEnemy = enemyScreenX - (heroX + 250f);
        return distToEnemy <= ATTACK_RANGE;
    }

    private void drawTabPanel() {
        float mx = Gdx.input.getX();
        float my = DungeonGame.HEIGHT - Gdx.input.getY();
        boolean click = Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) activeTab = (activeTab==0)?-1:0;
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) activeTab = (activeTab==1)?-1:1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) activeTab = (activeTab==2)?-1:2;

        for (int i = 0; i < 3; i++) {
            float tx = TAB_START_X + i*(TAB_W+TAB_GAP);
            boolean hov = mx>tx && mx<tx+TAB_W && my>TAB_Y && my<TAB_Y+TAB_H;
            boolean sel = (activeTab==i);
            Color c = TAB_COLORS[i];
            float bg = sel?0.35f:(hov?0.25f:0.12f);
            fillRect(c.r*bg,c.g*bg,c.b*bg,0.95f, tx,TAB_Y,TAB_W,TAB_H);
            fillRect(c.r,c.g,c.b,1f, tx,TAB_Y+TAB_H-(sel?4f:2f),TAB_W,(sel?4f:2f));
            if (sel) fillRect(c.r,c.g,c.b,0.8f, tx,TAB_Y,3,TAB_H);
            tabFont.setColor(sel?Color.WHITE:(hov?c:new Color(c.r*0.7f,c.g*0.7f,c.b*0.7f,1f)));
            tabFont.draw(batch, TAB_LABELS[i], tx+14, TAB_Y+33);
            if (hov && click) activeTab = sel?-1:i;
        }

        if (activeTab >= 0) {
            int t = activeTab;
            fillRect(0f,0f,0f,0.75f, 0,SUB_Y,DungeonGame.WIDTH,SUB_PANEL_H);
            fillRect(TAB_COLORS[t].r,TAB_COLORS[t].g,TAB_COLORS[t].b,0.6f, 0,SUB_Y+SUB_PANEL_H-2,DungeonGame.WIDTH,2);

            int count = SUB_LABELS[t].length;
            float totalW = count*220f+(count-1)*12f;
            float startX = (DungeonGame.WIDTH-totalW)/2f;

            for (int j = 0; j < count; j++) {
                float sx = startX+j*(220f+12f);
                float sy = SUB_Y+(SUB_PANEL_H-SUB_H)/2f;
                Color sc = SUB_COLORS[t][j];
                boolean hov = mx>sx && mx<sx+220f && my>sy && my<sy+SUB_H;
                fillRect(sc.r*(hov?0.3f:0.15f),sc.g*(hov?0.3f:0.15f),sc.b*(hov?0.3f:0.15f),0.92f, sx,sy,220f,SUB_H);
                fillRect(sc.r,sc.g,sc.b,1f, sx,sy+SUB_H-3,220f,3);
                tabFont.setColor(hov?Color.WHITE:sc);
                tabFont.draw(batch, SUB_LABELS[t][j], sx+16, sy+37);
                if (hov && click) { onAction(SUB_ACTIONS[t][j]); activeTab=-1; }
            }
            handleSubKeys(t);
        }
    }

    private void handleSubKeys(int t) {
        if (t==0) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) { onAction(1); activeTab=-1; }
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) { onAction(2); activeTab=-1; }
        } else if (t==1) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) { onAction(3); activeTab=-1; }
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) { onAction(4); activeTab=-1; }
        } else if (t==2) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.H)) { onAction(5); activeTab=-1; }
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) { onAction(6); activeTab=-1; }
        }
    }

    private void onAction(int action) {
        if (!playerTurn || battleOver) return;

        // ── RANGE CHECK — heal/scroll ауқымсыз, қалғаны жақын керек ──
        boolean needsRange = (action != 5); // 5 = heal, range керек емес
        if (needsRange && !isInAttackRange()) {
            tooFarMessage = true;
            tooFarTimer = 2.0f;
            battleLog = "Move closer to attack!";
            subLog = "Use arrow keys / WASD to approach the enemy.";
            return; // damage болмайды, turn өтпейді
        }

        comboCount++;
        comboTimer = COMBO_TIMEOUT;
        float comboMult = 1f + (comboCount-1)*0.1f;
        boolean isCrit = (comboCount>=3 && Math.random()<0.25);

        isHeroAttacking = true;
        heroState = HeroState.ATTACK;
        currentHeroFrame = 0;
        playSound(soundAttack);

        switch (action) {
            case 1: {
                int dmg = (int)(heroAtk*0.8f*comboMult);
                if (isCrit) { dmg*=2; battleLog="CRITICAL Quick Strike! "+dmg+" damage!!"; }
                else         battleLog="Quick Strike! "+dmg+" damage.";
                enemyHp -= dmg; triggerEnemyHitFlash(); break;
            }
            case 2: {
                int dmg = (int)(heroAtk*2*comboMult);
                if (isCrit) { dmg*=2; battleLog="CRITICAL HEAVY BLOW!! "+dmg+" damage!!!"; triggerShake(0.3f); }
                else         { battleLog="HEAVY BLOW! "+dmg+" damage!"; triggerShake(0.2f); }
                enemyHp -= dmg; triggerEnemyHitFlash(); break;
            }
            case 3: {
                int dmg = (int)(heroAtk*comboMult);
                enemyHp -= dmg;
                int drain = dmg/2;
                heroHp = Math.min(heroHp+drain, heroMaxHp);
                battleLog="DRAIN! "+dmg+" dmg, +"+drain+" HP stolen.";
                triggerEnemyHitFlash(); break;
            }
            case 4: {
                int dmg = (int)(heroAtk*1.5f*comboMult);
                if (isCrit) { dmg*=2; battleLog="CRITICAL FIRE BOLT!! "+dmg+" damage!!!"; triggerShake(0.25f); }
                else         battleLog="FIRE BOLT! "+dmg+" damage!";
                enemyHp -= dmg; triggerEnemyHitFlash(); break;
            }
            case 5: {
                int heal = 20;
                heroHp = Math.min(heroHp+heal, heroMaxHp);
                battleLog="You use a Potion. +"+heal+" HP.";
                subLog=heroDisplayName+" HP: "+heroHp+"/"+heroMaxHp;
                playerTurn=false; enemyTimer=1.0f;
                isHeroAttacking=false; comboCount=0;
                playSound(soundHeal); return;
            }
            case 6: {
                int dmg = (int)(heroAtk*3*comboMult);
                battleLog="SCROLL OF DESTRUCTION! "+dmg+" damage!";
                enemyHp -= dmg; triggerShake(0.4f); triggerEnemyHitFlash(); break;
            }
        }

        subLog = enemyName+" HP: "+Math.max(enemyHp,0);
        if (enemyHp <= 0) {
            battleLog = enemyName+" defeated!";
            subLog = "+50 XP";
            gainXp(50);
            battleOver = true; won = true;
            playSound(soundDeath);

            // ── FLOOR 4 (DRAGON BOSS) женгенде финал сценасы ──
            if (floor == 4) {
                showCurseScene = true;
                cursePhase = 0;
                curseTimer = 0f;
                battleOver = false; won = false;
            }
        } else {
            playerTurn=false; enemyTimer=1.0f;
        }
    }

    private void triggerEnemyHitFlash() { enemyHitFlash=true; hitFlashTimer=0.15f; }
    private void triggerHeroHitFlash()  { heroHitFlash=true;  hitFlashTimer=0.20f; }
    private void triggerShake(float d)  { shakeTimer=d; }

    private void doEnemyTurn() {
        enemyTurnCount++;
        int dmg = enemyAtk;
        isEnemyAttacking=true; currentEnemyFrame=0;
        playSound(soundHit); triggerHeroHitFlash();

        if (enemyType.equals("boss2")) {
            if (enemyTurnCount%3==0 && enemyHp<enemyMaxHp) {
                int heal=15; enemyHp=Math.min(enemyHp+heal,enemyMaxHp);
                battleLog=enemyName+" regenerates! +"+heal+" HP";
                subLog=enemyName+" HP: "+enemyHp+"/"+enemyMaxHp;
                playerTurn=true; return;
            }
        } else if (enemyType.equals("boss3")) {
            if (Math.random()<0.5) { dmg*=2; battleLog=enemyName+" casts DARK BOLT! "+dmg+" damage!!"; triggerShake(0.2f); }
            else battleLog=enemyName+" attacks for "+dmg+" damage.";
        } else if (enemyType.equals("boss4")) {
            if (Math.random()<0.3) { dmg*=3; battleLog=enemyName+" BREATHES FIRE! CRITICAL "+dmg+" damage!!!"; triggerShake(0.4f); }
            else battleLog=enemyName+" claws you for "+dmg+" damage.";
        } else {
            battleLog=enemyName+" hits you for "+dmg+" damage!";
        }

        // Undead болса — zombie DMG 30% аз (curse қорғайды)
        if (heroIsUndead && enemyType.equals("boss2")) dmg = (int)(dmg*0.7f);

        heroHp -= dmg;
        subLog = heroDisplayName+" HP: "+Math.max(heroHp,0)+"/"+heroMaxHp;
        if (heroHp <= 0) { battleOver=true; won=false; playSound(soundDeath); }
        else playerTurn=true;
    }

    private void gainXp(int amount) {
        heroXp += amount;
        if (heroXp >= 100) {
            heroXp=0; heroLevel++; heroAtk+=5; heroMaxHp+=20; heroHp=heroMaxHp;
            battleLog="LEVEL UP! Level "+heroLevel+" — ATK "+heroAtk;
        }
    }

    private void nextFloor() {
        floor++;
        if (floor > 4) { game.setScreen(new GameOverScreen(game)); return; }
        loadFloor(floor);
    }

    private void drawBar(float x,float y,float w,float h,int cur,int max,Color c) {
        fillRect(0.2f,0.2f,0.2f,1f, x,y,w,h);
        fillRect(c.r,c.g,c.b,1f, x,y,w*Math.max(0,(float)cur/max),h);
    }

    private void fillRect(float r,float g,float b,float a,float x,float y,float w,float h) {
        batch.setColor(r,g,b,a);
        batch.draw(pixel,x,y,w,h);
        batch.setColor(1,1,1,1);
    }

    @Override public void show() {}
    @Override public void resize(int w,int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose(); pixel.dispose();
        bigFont.dispose(); font.dispose(); logFont.dispose(); tabFont.dispose();
        if (background!=null) background.dispose();
        if (heroPortrait!=null) heroPortrait.dispose();
        if (heroIdle!=null) heroIdle.dispose();
        if (heroWalkFrames!=null) for (Texture t:heroWalkFrames) if(t!=null) t.dispose();
        if (heroAttackFrames!=null) for (Texture t:heroAttackFrames) if(t!=null) t.dispose();
        if (enemyFrames!=null) for (Texture t:enemyFrames) if(t!=null) t.dispose();
        if (soundAttack!=null) soundAttack.dispose();
        if (soundHit!=null) soundHit.dispose();
        if (soundDeath!=null) soundDeath.dispose();
        if (soundStep!=null) soundStep.dispose();
        if (soundHeal!=null) soundHeal.dispose();
        if (bgMusic!=null) { bgMusic.stop(); bgMusic.dispose(); }
    }
}
