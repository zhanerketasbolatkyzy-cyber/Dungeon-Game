package com.narxoz.rpg.dungeon.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.narxoz.rpg.dungeon.DungeonGame;

public class GameScreen implements Screen {

    private DungeonGame game;
    private SpriteBatch batch;
    private Texture pixel;
    private Texture background;
    private Texture heroPortrait;
    private Texture enemySprite;
    private BitmapFont bigFont, font, logFont, smallFont;

    // ==================== HERO STATS ====================
    private String heroName;
    private int heroHp, heroMaxHp, heroAtk, heroLevel = 1, heroXp = 0;
    private int heroDef = 0;
    private boolean isDefending = false;
    private int mana, maxMana;           // MANA SYSTEM
    private int poisonOnEnemy = 0;       // Rogue poison turns remaining
    private int poisonDmgPerTurn = 0;

    // ==================== ENEMY STATS ====================
    private int    enemyHp, enemyMaxHp, enemyAtk, enemyDef;
    private String enemyName;
    private String enemyType;
    private int    enemyTurnCount = 0;

    // ==================== INVENTORY ====================
    private int potionCount = 1;      // Health Potion: heals 40% maxHP
    private int bombCount = 0;        // Fire Bomb: 30-50 true damage
    private int shieldScrollCount = 0;// Shield Scroll: +5 DEF permanent

    // ==================== FLOOR & STATE ====================
    private int floor = 1;
    private static final int MAX_FLOOR = 7;

    private String battleLog = "";
    private String subLog    = "";
    private boolean playerTurn  = true;
    private boolean battleOver  = false;
    private boolean won         = false;
    private float   enemyTimer  = 0f;

    // Damage shake effect
    private float shakeTimer = 0f;
    private float shakeIntensity = 0f;

    // Flash effect for crits
    private float flashTimer = 0f;
    private Color flashColor = Color.WHITE;

    // XP reward per floor
    private int[] floorXpReward = {30, 50, 70, 60, 80, 100, 150};

    // Lore notes for each floor
    private String[] floorNotes = {
        "\"I came to save a princess. The walls whisper my name...\"",
        "\"A corpse in armor identical to mine. I choose not to think about it.\"",
        "\"No monster here. Only a mirror. It smiles when I don't.\"",
        "\"The air burns. Something ancient waits beyond this gate.\"",
        "\"I found a journal. It says: 'The 7th hero arrived today.'\"",
        "\"Blood on the walls spells a name. MY name.\"",
        ""
    };
    private boolean noteShown = false;
    private boolean noteDismissed = false;

    // Item drop after victory
    private boolean showItemDrop = false;
    private String droppedItemName = "";

    // Action mode: "battle" or "item"
    private String actionMode = "battle";

    public GameScreen(DungeonGame game, String heroName, int heroHp, int heroAtk) {
        this.game     = game;
        this.heroName = heroName;
        this.heroHp   = heroHp;
        this.heroMaxHp= heroHp;
        this.heroAtk  = heroAtk;

        // Character-specific base stats
        if (heroName.equals("KNIGHT"))       { heroDef = 5;  mana = 30; maxMana = 30; }
        else if (heroName.equals("PALADIN")) { heroDef = 8;  mana = 40; maxMana = 40; }
        else                                 { heroDef = 2;  mana = 25; maxMana = 25; } // ROGUE

        batch = new SpriteBatch();

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE); pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        try { background = new Texture(Gdx.files.internal("battle_bg.png")); }
        catch (Exception e) { background = null; }

        String heroFile = heroName.toLowerCase() + ".png";
        try { heroPortrait = new Texture(Gdx.files.internal(heroFile)); }
        catch (Exception e) { heroPortrait = null; }

        bigFont   = new BitmapFont(); bigFont.getData().setScale(2f);
        font      = new BitmapFont(); font.getData().setScale(1.5f);
        logFont   = new BitmapFont(); logFont.getData().setScale(1.3f);
        smallFont = new BitmapFont(); smallFont.getData().setScale(1.1f);

        loadFloor(1);
    }

    // ==================== 7 FLOORS WITH ENEMIES ====================
    private void loadFloor(int f) {
        noteShown     = false;
        noteDismissed = (f > floorNotes.length || floorNotes[f - 1].isEmpty());
        battleOver    = false;
        won           = false;
        playerTurn    = true;
        isDefending   = false;
        showItemDrop  = false;
        actionMode    = "battle";
        battleLog     = "";
        subLog        = "";
        enemyTurnCount = 0;
        poisonOnEnemy = 0;
        poisonDmgPerTurn = 0;

        switch (f) {
            case 1:
                enemyName="SKELETON"; enemyType="skeleton";
                enemyHp=50;  enemyMaxHp=50;  enemyAtk=8;  enemyDef=2;
                break;
            case 2:
                enemyName="ZOMBIE"; enemyType="zombie";
                enemyHp=70;  enemyMaxHp=70;  enemyAtk=10; enemyDef=4;
                break;
            case 3:
                enemyName="DARK MAGE"; enemyType="dark_mage";
                enemyHp=55;  enemyMaxHp=55;  enemyAtk=14; enemyDef=1;
                break;
            case 4:
                enemyName="GOBLIN CHIEF"; enemyType="skeleton";
                enemyHp=65;  enemyMaxHp=65;  enemyAtk=12; enemyDef=3;
                break;
            case 5:
                enemyName="VAMPIRE"; enemyType="dark_mage";
                enemyHp=90;  enemyMaxHp=90;  enemyAtk=16; enemyDef=3;
                break;
            case 6:
                enemyName="LICH"; enemyType="zombie";
                enemyHp=110; enemyMaxHp=110; enemyAtk=18; enemyDef=5;
                break;
            case 7:
                enemyName="DRAGON BOSS"; enemyType="dragon_boss";
                enemyHp=180; enemyMaxHp=180; enemyAtk=22; enemyDef=7;
                break;
        }

        // Load enemy sprite
        try {
            if (enemySprite != null) enemySprite.dispose();
            enemySprite = new Texture(Gdx.files.internal(enemyType + ".png"));
        } catch (Exception e) { enemySprite = null; }

        // Mana regen between floors
        mana = Math.min(mana + (int)(maxMana * 0.3f), maxMana);
    }

    // ==================== DAMAGE FORMULA ====================
    private int calcDamage(int baseAtk, int targetDef) {
        float variance = 0.85f + (float)(Math.random() * 0.30f);
        int rawDmg = (int)(baseAtk * variance);
        return Math.max(rawDmg - targetDef, 1);
    }

    private boolean isCriticalHit() {
        float critChance = heroName.equals("ROGUE") ? 0.25f : 0.15f;
        return Math.random() < critChance;
    }

    // ==================== RENDER ====================
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.04f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (shakeTimer > 0) shakeTimer -= delta;
        if (flashTimer > 0) flashTimer -= delta;

        if (!playerTurn && !battleOver) {
            enemyTimer -= delta;
            if (enemyTimer <= 0) doEnemyTurn();
        }

        batch.begin();

        // Shake offset
        float sx = 0, sy = 0;
        if (shakeTimer > 0) {
            sx = (float)(Math.random() - 0.5) * shakeIntensity * 2;
            sy = (float)(Math.random() - 0.5) * shakeIntensity * 2;
        }

        // Background
        if (background != null) {
            batch.setColor(1, 1, 1, 0.5f);
            batch.draw(background, sx, sy, DungeonGame.WIDTH, DungeonGame.HEIGHT);
            batch.setColor(1, 1, 1, 1);
        }

        // Flash overlay for crits
        if (flashTimer > 0) {
            float alpha = flashTimer * 2f;
            fillRect(flashColor.r, flashColor.g, flashColor.b, Math.min(alpha, 0.4f),
                0, 0, DungeonGame.WIDTH, DungeonGame.HEIGHT);
        }

        // ==================== HUD PANEL ====================
        fillRect(0, 0, 0, 0.7f, 0, DungeonGame.HEIGHT - 90, DungeonGame.WIDTH, 90);

        bigFont.setColor(Color.GOLD);
        bigFont.draw(batch, "FLOOR " + floor + "/" + MAX_FLOOR, 16, DungeonGame.HEIGHT - 8);

        // Hero HP bar & portrait
        if (heroPortrait != null) {
            batch.draw(heroPortrait, 16, DungeonGame.HEIGHT - 85, 50, 50);
        }
        drawBar(72, DungeonGame.HEIGHT - 48, 180, 14, heroHp, heroMaxHp, getHpColor(heroHp, heroMaxHp));
        font.setColor(Color.WHITE);
        font.draw(batch, heroName + " " + heroHp + "/" + heroMaxHp, 72, DungeonGame.HEIGHT - 52);

        // Mana bar
        drawBar(72, DungeonGame.HEIGHT - 68, 180, 10, mana, maxMana, new Color(0.3f, 0.5f, 1f, 1f));
        smallFont.setColor(new Color(0.5f, 0.7f, 1f, 1f));
        smallFont.draw(batch, "MP " + mana + "/" + maxMana, 72, DungeonGame.HEIGHT - 70);

        // Stats line
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, "ATK " + heroAtk + "  DEF " + heroDef + "  LVL " + heroLevel, 72, DungeonGame.HEIGHT - 83);

        if (isDefending) {
            font.setColor(Color.SKY);
            font.draw(batch, "SHIELD", 260, DungeonGame.HEIGHT - 52);
        }

        // XP bar
        drawBar(270, DungeonGame.HEIGHT - 20, 80, 6, heroXp, 100, new Color(0.6f, 0.4f, 1f, 1f));
        smallFont.setColor(new Color(0.7f, 0.5f, 1f, 1f));
        smallFont.draw(batch, "XP " + heroXp + "/100", 270, DungeonGame.HEIGHT - 22);

        // Inventory display (top right corner)
        smallFont.setColor(Color.GOLD);
        smallFont.draw(batch, "ITEMS:", DungeonGame.WIDTH - 200, DungeonGame.HEIGHT - 10);
        smallFont.setColor(Color.GREEN);
        smallFont.draw(batch, "Potion:" + potionCount, DungeonGame.WIDTH - 200, DungeonGame.HEIGHT - 28);
        smallFont.setColor(Color.ORANGE);
        smallFont.draw(batch, "Bomb:" + bombCount, DungeonGame.WIDTH - 120, DungeonGame.HEIGHT - 28);
        smallFont.setColor(Color.SKY);
        smallFont.draw(batch, "Scroll:" + shieldScrollCount, DungeonGame.WIDTH - 200, DungeonGame.HEIGHT - 46);

        // Enemy HP bar & sprite
        if (enemyMaxHp > 0) {
            if (enemySprite != null) {
                float spriteSize = (enemyType.equals("dragon_boss")) ? 120 : 90;
                float esx = sx * 0.5f, esy = sy * 0.5f;
                batch.draw(enemySprite, DungeonGame.WIDTH - 145 + esx, DungeonGame.HEIGHT - 85 + esy,
                    spriteSize, spriteSize);
            }
            drawBar(DungeonGame.WIDTH - 280, DungeonGame.HEIGHT - 48, 180, 14, enemyHp, enemyMaxHp,
                getHpColor(enemyHp, enemyMaxHp));
            font.setColor(new Color(1f, 0.4f, 0.4f, 1f));
            font.draw(batch, enemyName + " " + Math.max(enemyHp, 0) + "/" + enemyMaxHp,
                DungeonGame.WIDTH - 280, DungeonGame.HEIGHT - 52);

            // Poison indicator
            if (poisonOnEnemy > 0) {
                smallFont.setColor(new Color(0.4f, 1f, 0.2f, 1f));
                smallFont.draw(batch, "POISONED (" + poisonOnEnemy + " turns)",
                    DungeonGame.WIDTH - 280, DungeonGame.HEIGHT - 68);
            }
        }

        // ==================== LORE NOTE ====================
        if (!noteDismissed && floor <= floorNotes.length && !floorNotes[floor - 1].isEmpty()) {
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

        // ==================== ITEM DROP SCREEN ====================
        if (showItemDrop) {
            fillRect(0, 0, 0, 0.7f, 100, 200, DungeonGame.WIDTH - 200, 140);
            fillRect(0.7f, 0.6f, 0.1f, 1f, 100, 336, DungeonGame.WIDTH - 200, 3);
            bigFont.setColor(Color.GOLD);
            bigFont.draw(batch, "LOOT!", 330, 330);
            logFont.setColor(Color.WHITE);
            logFont.draw(batch, "You found: " + droppedItemName, 160, 290);
            logFont.setColor(Color.GOLD);
            logFont.draw(batch, "[ ENTER ] continue", 280, 220);
            batch.end();
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                showItemDrop = false;
                nextFloor();
            }
            return;
        }

        // ==================== BATTLE LOG ====================
        fillRect(0, 0, 0, 0.55f, 0, 130, DungeonGame.WIDTH, 75);
        logFont.setColor(Color.WHITE);
        logFont.draw(batch, battleLog, 16, 192);
        logFont.setColor(Color.LIGHT_GRAY);
        logFont.draw(batch, subLog, 16, 162);

        // ==================== ACTION BUTTONS ====================
        if (playerTurn && !battleOver) {
            if (actionMode.equals("battle")) {
                drawActionBtn("[1] QUICK",   10,  80, Color.CYAN,   Input.Keys.NUM_1, 1);
                drawActionBtn("[2] HEAVY",  130,  80, Color.ORANGE, Input.Keys.NUM_2, 2);
                drawActionBtn("[3] DEFEND", 250,  80, Color.SKY,    Input.Keys.NUM_3, 3);
                drawActionBtn("[4] SPECIAL",370,  80, new Color(1f, 0.3f, 1f, 1f), Input.Keys.NUM_4, 4);
                drawActionBtn("[5] ITEMS",  490,  80, Color.GOLD,   Input.Keys.NUM_5, 10);

                // Heal shortcut
                drawSmallBtn("[H] HEAL", 610, 80, Color.GREEN, Input.Keys.H, 5);
            } else if (actionMode.equals("item")) {
                // Item sub-menu
                logFont.setColor(Color.GOLD);
                logFont.draw(batch, "USE ITEM:  [1] Potion(" + potionCount + ")  [2] Bomb("
                    + bombCount + ")  [3] Scroll(" + shieldScrollCount + ")  [ESC] Back", 16, 74);

                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) useItem(1);
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) useItem(2);
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) useItem(3);
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) actionMode = "battle";
            }
        }

        // ==================== BATTLE OVER ====================
        if (battleOver) {
            if (won) {
                if (floor == MAX_FLOOR) {
                    fillRect(0, 0, 0, 0.85f, 60, 140, DungeonGame.WIDTH - 120, 300);
                    bigFont.setColor(Color.GOLD);
                    bigFont.draw(batch, "THE BOSS FALLS...", 150, 410);
                    logFont.setColor(Color.WHITE);
                    logFont.draw(batch, "His helmet shatters. You see YOUR face. Older. Exhausted.", 80, 370);
                    logFont.draw(batch, "Boss: \"Run... She doesn't need saving. She needs a vessel...\"", 80, 340);
                    logFont.setColor(new Color(0.8f, 0.4f, 0.9f, 1f));
                    logFont.draw(batch, "Princess: \"Perfect. You are perfect. Come to the throne...\"", 80, 305);
                    logFont.setColor(Color.GOLD);
                    logFont.draw(batch, "[ ENTER ] - Accept your fate", 230, 200);
                    logFont.setColor(Color.LIGHT_GRAY);
                    logFont.draw(batch, "Floor " + floor + " | Level " + heroLevel + " | "
                            + heroName + " | Items used: " + (3 - potionCount - bombCount - shieldScrollCount),
                        100, 170);
                    batch.end();
                    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER))
                        game.setScreen(new GameOverScreen(game, true, floor, heroLevel, heroName));
                    return;
                } else {
                    bigFont.setColor(Color.GOLD);
                    bigFont.draw(batch, "VICTORY!", 280, 120);
                    font.setColor(Color.WHITE);
                    font.draw(batch, "[ ENTER ] next floor", 270, 80);
                    batch.end();
                    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                        rollItemDrop();
                    }
                    return;
                }
            } else {
                batch.end();
                game.setScreen(new GameOverScreen(game, false, floor, heroLevel, heroName));
                return;
            }
        }

        batch.end();
    }

    // ==================== ITEM DROP SYSTEM ====================
    private void rollItemDrop() {
        double roll = Math.random();
        if (roll < 0.40) {
            // 40% chance: Health Potion
            potionCount++;
            droppedItemName = "Health Potion (+1)";
            showItemDrop = true;
        } else if (roll < 0.65) {
            // 25% chance: Fire Bomb
            bombCount++;
            droppedItemName = "Fire Bomb (+1)";
            showItemDrop = true;
        } else if (roll < 0.80) {
            // 15% chance: Shield Scroll
            shieldScrollCount++;
            droppedItemName = "Shield Scroll (+1)";
            showItemDrop = true;
        } else if (roll < 0.90) {
            // 10% chance: ATK boost
            heroAtk += 2;
            droppedItemName = "Ancient Blade (ATK +2 permanent!)";
            showItemDrop = true;
        } else {
            // 10% chance: nothing
            nextFloor();
        }
    }

    // ==================== USE ITEM ====================
    private void useItem(int item) {
        if (item == 1 && potionCount > 0) {
            potionCount--;
            int heal = (int)(heroMaxHp * 0.40f);
            heroHp = Math.min(heroHp + heal, heroMaxHp);
            battleLog = "Used Health Potion! +" + heal + " HP!";
            subLog = heroName + " HP: " + heroHp + "/" + heroMaxHp;
            actionMode = "battle";
            playerTurn = false;
            enemyTimer = 1.0f;
        } else if (item == 2 && bombCount > 0) {
            bombCount--;
            int dmg = 30 + (int)(Math.random() * 20); // 30-50 true damage, ignores DEF
            enemyHp -= dmg;
            battleLog = "Threw Fire Bomb! " + dmg + " TRUE damage!";
            shakeTimer = 0.3f; shakeIntensity = 5f;
            flashTimer = 0.3f; flashColor = Color.ORANGE;
            actionMode = "battle";
            if (enemyHp <= 0) {
                int xpGain = floorXpReward[floor - 1];
                subLog = enemyName + " defeated! +" + xpGain + " XP";
                gainXp(xpGain);
                battleOver = true;
                won = true;
            } else {
                subLog = enemyName + " HP: " + Math.max(enemyHp, 0);
                playerTurn = false;
                enemyTimer = 1.0f;
            }
        } else if (item == 3 && shieldScrollCount > 0) {
            shieldScrollCount--;
            heroDef += 5;
            battleLog = "Used Shield Scroll! DEF +" + 5 + " (permanent)!";
            subLog = "DEF is now " + heroDef;
            actionMode = "battle";
            playerTurn = false;
            enemyTimer = 1.0f;
        } else {
            battleLog = "You don't have that item!";
            subLog = "";
        }
    }

    // ==================== HP BAR COLOR ====================
    private Color getHpColor(int cur, int max) {
        float pct = (float) cur / max;
        if (pct > 0.6f) return Color.GREEN;
        if (pct > 0.3f) return Color.YELLOW;
        return Color.RED;
    }

    private void drawActionBtn(String label, float x, float y, Color c, int key, int action) {
        float mx = Gdx.input.getX(), my = DungeonGame.HEIGHT - Gdx.input.getY();
        float btnW = 115, btnH = 44;
        boolean hov = mx > x && mx < x + btnW && my > y && my < y + btnH;

        fillRect(c.r * (hov ? 0.4f : 0.18f), c.g * (hov ? 0.4f : 0.18f), c.b * (hov ? 0.4f : 0.18f),
            0.9f, x, y, btnW, btnH);
        fillRect(c.r, c.g, c.b, 1f, x, y + 41, btnW, 3);

        font.setColor(hov ? Color.WHITE : c);
        font.draw(batch, label, x + 4, y + 33);

        boolean clicked = Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT);
        if ((hov && clicked) || Gdx.input.isKeyJustPressed(key)) {
            if (action == 10) { actionMode = "item"; }
            else onAction(action);
        }
    }

    private void drawSmallBtn(String label, float x, float y, Color c, int key, int action) {
        float mx = Gdx.input.getX(), my = DungeonGame.HEIGHT - Gdx.input.getY();
        float btnW = 80, btnH = 44;
        boolean hov = mx > x && mx < x + btnW && my > y && my < y + btnH;

        fillRect(c.r * (hov ? 0.4f : 0.18f), c.g * (hov ? 0.4f : 0.18f), c.b * (hov ? 0.4f : 0.18f),
            0.9f, x, y, btnW, btnH);
        fillRect(c.r, c.g, c.b, 1f, x, y + 41, btnW, 3);

        smallFont.setColor(hov ? Color.WHITE : c);
        smallFont.draw(batch, label, x + 4, y + 30);

        boolean clicked = Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT);
        if ((hov && clicked) || Gdx.input.isKeyJustPressed(key)) onAction(action);
    }

    private void drawBar(float x, float y, float w, float h, int cur, int max, Color c) {
        fillRect(0.15f, 0.15f, 0.15f, 1f, x, y, w, h);
        float pct = Math.max(0, (float) cur / max);
        fillRect(c.r, c.g, c.b, 1f, x, y, w * pct, h);
    }

    private void fillRect(float r, float g, float b, float a, float x, float y, float w, float h) {
        batch.setColor(r, g, b, a);
        batch.draw(pixel, x, y, w, h);
        batch.setColor(1, 1, 1, 1);
    }

    // ==================== PLAYER ACTIONS ====================
    private void onAction(int action) {
        if (!playerTurn || battleOver) return;
        isDefending = false;
        actionMode = "battle";

        if (action == 1) {
            // QUICK STRIKE
            int dmg = calcDamage(heroAtk, enemyDef);
            boolean crit = isCriticalHit();
            if (crit) { dmg = (int)(dmg * 1.5f); flashTimer = 0.2f; flashColor = Color.YELLOW; }
            enemyHp -= dmg;
            battleLog = crit ? "CRITICAL Quick Strike! " + dmg + " damage!!"
                : "Quick Strike! " + dmg + " damage.";

        } else if (action == 2) {
            // HEAVY BLOW - 20% miss
            boolean miss = Math.random() < 0.20;
            if (miss) {
                battleLog = "Heavy Blow MISSED!";
                subLog = "The enemy dodged your attack.";
                playerTurn = false; enemyTimer = 1.0f;
                return;
            }
            int dmg = calcDamage((int)(heroAtk * 1.6f), enemyDef);
            boolean crit = isCriticalHit();
            if (crit) { dmg = (int)(dmg * 1.5f); flashTimer = 0.3f; flashColor = Color.RED; }
            enemyHp -= dmg;
            shakeTimer = 0.3f; shakeIntensity = 4f;
            battleLog = crit ? "CRITICAL HEAVY BLOW!! " + dmg + " DAMAGE!!!"
                : "HEAVY BLOW! " + dmg + " damage!";

        } else if (action == 3) {
            // DEFEND
            isDefending = true;
            battleLog = heroName + " raises their guard!";
            subLog = "Incoming damage -60% this turn.";
            playerTurn = false; enemyTimer = 1.0f;
            return;

        } else if (action == 4) {
            // SPECIAL ABILITY (costs mana)
            doSpecialAbility();
            return;

        } else if (action == 5) {
            // HEAL (costs mana)
            int manaCost = 15;
            if (mana < manaCost) {
                battleLog = "Not enough mana! Need " + manaCost + " MP.";
                subLog = "Current MP: " + mana + "/" + maxMana;
                return;
            }
            mana -= manaCost;
            int heal;
            if (heroName.equals("PALADIN"))      heal = (int)(heroMaxHp * 0.30f);
            else if (heroName.equals("KNIGHT"))   heal = (int)(heroMaxHp * 0.20f);
            else                                  heal = (int)(heroMaxHp * 0.15f);
            heroHp = Math.min(heroHp + heal, heroMaxHp);
            battleLog = "Healed +" + heal + " HP! (-" + manaCost + " MP)";
            subLog = heroName + " HP: " + heroHp + "/" + heroMaxHp;
            playerTurn = false; enemyTimer = 1.0f;
            return;
        }

        subLog = enemyName + " HP: " + Math.max(enemyHp, 0);
        checkEnemyDeath();
    }

    // ==================== SPECIAL ABILITIES ====================
    private void doSpecialAbility() {
        int manaCost = 20;
        if (mana < manaCost) {
            battleLog = "Not enough mana! Need " + manaCost + " MP.";
            subLog = "Current MP: " + mana + "/" + maxMana;
            return;
        }
        mana -= manaCost;

        if (heroName.equals("KNIGHT")) {
            // SHIELD BASH: damage + stun (enemy skips next turn)
            int dmg = calcDamage((int)(heroAtk * 1.2f), enemyDef);
            enemyHp -= dmg;
            battleLog = "SHIELD BASH! " + dmg + " dmg + enemy STUNNED!";
            shakeTimer = 0.25f; shakeIntensity = 3f;
            subLog = enemyName + " HP: " + Math.max(enemyHp, 0) + " (-" + manaCost + " MP)";
            if (enemyHp <= 0) { checkEnemyDeath(); return; }
            // Stun: enemy turn passes, back to player
            playerTurn = true;
            return;

        } else if (heroName.equals("ROGUE")) {
            // POISON BLADE: damage + 3 turns of poison
            int dmg = calcDamage(heroAtk, enemyDef);
            enemyHp -= dmg;
            poisonOnEnemy = 3;
            poisonDmgPerTurn = 5 + heroLevel * 2;
            battleLog = "POISON BLADE! " + dmg + " dmg + POISON (" + poisonDmgPerTurn + "/turn)!";
            flashTimer = 0.2f; flashColor = new Color(0.2f, 1f, 0.2f, 1f);
            subLog = enemyName + " HP: " + Math.max(enemyHp, 0) + " (-" + manaCost + " MP)";
            checkEnemyDeath();
            if (!battleOver) { playerTurn = false; enemyTimer = 1.0f; }
            return;

        } else if (heroName.equals("PALADIN")) {
            // HOLY SMITE: massive damage, heals self
            int dmg = calcDamage((int)(heroAtk * 2.0f), enemyDef);
            enemyHp -= dmg;
            int selfHeal = (int)(heroMaxHp * 0.15f);
            heroHp = Math.min(heroHp + selfHeal, heroMaxHp);
            battleLog = "HOLY SMITE! " + dmg + " dmg + healed " + selfHeal + " HP!";
            flashTimer = 0.3f; flashColor = Color.GOLD;
            shakeTimer = 0.3f; shakeIntensity = 5f;
            subLog = enemyName + " HP: " + Math.max(enemyHp, 0) + " (-" + manaCost + " MP)";
            checkEnemyDeath();
            if (!battleOver) { playerTurn = false; enemyTimer = 1.0f; }
            return;
        }
    }

    private void checkEnemyDeath() {
        if (enemyHp <= 0) {
            int xpGain = floorXpReward[floor - 1];
            battleLog = enemyName + " defeated!";
            subLog = "+" + xpGain + " XP";
            gainXp(xpGain);
            battleOver = true;
            won = true;
        } else {
            playerTurn = false;
            enemyTimer = 1.0f;
        }
    }

    // ==================== ENEMY AI ====================
    private void doEnemyTurn() {
        enemyTurnCount++;

        // Apply poison damage BEFORE enemy attacks
        if (poisonOnEnemy > 0) {
            enemyHp -= poisonDmgPerTurn;
            poisonOnEnemy--;
            subLog = "Poison deals " + poisonDmgPerTurn + " damage! ";
            if (enemyHp <= 0) {
                int xpGain = floorXpReward[floor - 1];
                battleLog = enemyName + " died from POISON!";
                subLog = "+" + xpGain + " XP";
                gainXp(xpGain);
                battleOver = true;
                won = true;
                return;
            }
        }

        int dmg = calcDamage(enemyAtk, heroDef);
        if (isDefending) {
            dmg = Math.max((int)(dmg * 0.40f), 1);
        }

        // Enemy-specific AI
        if (enemyType.equals("skeleton")) {
            if (enemyTurnCount % 4 == 0) {
                dmg = calcDamage((int)(enemyAtk * 1.3f), heroDef);
                if (isDefending) dmg = Math.max((int)(dmg * 0.40f), 1);
                battleLog = enemyName + " throws a BONE SPEAR! " + dmg + " damage!";
            } else {
                battleLog = enemyName + " hits you for " + dmg + " damage!";
            }
        } else if (enemyType.equals("zombie")) {
            if (enemyTurnCount % 3 == 0 && enemyHp < enemyMaxHp) {
                int heal = 12 + floor * 2;
                enemyHp = Math.min(enemyHp + heal, enemyMaxHp);
                battleLog = enemyName + " regenerates! +" + heal + " HP";
                subLog += enemyName + " HP: " + enemyHp + "/" + enemyMaxHp;
                playerTurn = true;
                isDefending = false;
                return;
            }
            battleLog = enemyName + " hits you for " + dmg + " damage!";
        } else if (enemyType.equals("dark_mage")) {
            double roll = Math.random();
            if (roll < 0.15) {
                int drain = calcDamage((int)(enemyAtk * 0.8f), heroDef);
                if (isDefending) drain = Math.max((int)(drain * 0.40f), 1);
                dmg = drain;
                enemyHp = Math.min(enemyHp + drain, enemyMaxHp);
                battleLog = enemyName + " LIFE DRAIN! " + drain + " dmg, heals " + drain + "!";
            } else if (roll < 0.55) {
                dmg = calcDamage((int)(enemyAtk * 1.8f), heroDef);
                if (isDefending) dmg = Math.max((int)(dmg * 0.40f), 1);
                battleLog = enemyName + " casts DARK BOLT! " + dmg + " damage!!";
                shakeTimer = 0.2f; shakeIntensity = 3f;
            } else {
                battleLog = enemyName + " attacks for " + dmg + " damage.";
            }
        } else if (enemyType.equals("dragon_boss")) {
            double roll = Math.random();
            if (roll < 0.25) {
                dmg = calcDamage((int)(enemyAtk * 2.5f), heroDef);
                if (isDefending) dmg = Math.max((int)(dmg * 0.40f), 1);
                battleLog = enemyName + " BREATHES FIRE! " + dmg + " DAMAGE!!!";
                shakeTimer = 0.4f; shakeIntensity = 6f;
                flashTimer = 0.3f; flashColor = Color.RED;
            } else if (roll < 0.40) {
                dmg = calcDamage((int)(enemyAtk * 1.5f), heroDef);
                if (isDefending) dmg = Math.max((int)(dmg * 0.40f), 1);
                battleLog = enemyName + " TAIL SWEEP! " + dmg + " damage!";
                shakeTimer = 0.25f; shakeIntensity = 4f;
            } else {
                battleLog = enemyName + " claws you for " + dmg + " damage.";
            }
        } else {
            battleLog = enemyName + " hits you for " + dmg + " damage!";
        }

        heroHp -= dmg;
        subLog += heroName + " HP: " + Math.max(heroHp, 0) + "/" + heroMaxHp;
        if (isDefending) subLog += " (DEFENDED!)";

        // Mana regen on getting hit
        mana = Math.min(mana + 3, maxMana);

        if (heroHp <= 0) {
            battleOver = true;
            won = false;
        } else {
            playerTurn = true;
        }
        isDefending = false;
    }

    // ==================== LEVEL UP ====================
    private void gainXp(int amount) {
        heroXp += amount;
        if (heroXp >= 100) {
            heroXp -= 100;
            heroLevel++;
            heroAtk += 3;
            heroMaxHp += 15;
            heroDef += 1;
            maxMana += 5;
            mana = maxMana;
            heroHp = heroMaxHp;
            battleLog = "LEVEL UP! Lvl " + heroLevel + " - ATK " + heroAtk
                + " DEF " + heroDef + " HP " + heroMaxHp + " MP " + maxMana;
        }
    }

    private void nextFloor() {
        floor++;
        if (floor > MAX_FLOOR) {
            game.setScreen(new GameOverScreen(game, true, MAX_FLOOR, heroLevel, heroName));
            return;
        }
        loadFloor(floor);
    }

    @Override public void show() {}
    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        batch.dispose(); pixel.dispose();
        bigFont.dispose(); font.dispose(); logFont.dispose(); smallFont.dispose();
        if (background != null) background.dispose();
        if (heroPortrait != null) heroPortrait.dispose();
        if (enemySprite != null) enemySprite.dispose();
    }
}
