package com.narxoz.rpg.dungeon.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.narxoz.rpg.dungeon.DungeonGame;

public class CharacterSelectScreen implements Screen {

    private DungeonGame game;
    private SpriteBatch batch;
    private Texture pixel;
    private Texture background;

    // Character portraits
    private Texture[] portraits = new Texture[3];

    private BitmapFont titleFont;
    private BitmapFont nameFont;
    private BitmapFont statsFont;
    private BitmapFont hintFont;

    private final String[] names = {"KNIGHT",  "ROGUE",    "PALADIN"};
    private final int[]    hps   = {120,        80,          150};
    private final int[]    atks  = {18,         28,          12};
    private final int[]    defs  = {5,          2,           8};
    private final String[] line1 = {"Balanced warrior.",  "Glass cannon.",    "Holy tank."};
    private final String[] line2 = {"[4] Shield Bash",    "[4] Poison Blade", "[4] Holy Smite"};
    private final Color[]  cols  = {Color.CYAN, Color.ORANGE, Color.GREEN};

    private int selected = 0;

    private static final float CW = 185f, CH = 320f, CY = 140f, GAP = 30f;
    private final float startX;

    public CharacterSelectScreen(DungeonGame game) {
        this.game = game;

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        try { background = new Texture(Gdx.files.internal("menu_bg.png")); }
        catch (Exception e) { background = null; }

        // Load portraits
        try { portraits[0] = new Texture(Gdx.files.internal("knight.png")); } catch (Exception e) {}
        try { portraits[1] = new Texture(Gdx.files.internal("rogue.png")); } catch (Exception e) {}
        try { portraits[2] = new Texture(Gdx.files.internal("paladin.png")); } catch (Exception e) {}

        titleFont = new BitmapFont();  titleFont.getData().setScale(2.4f);
        nameFont  = new BitmapFont();  nameFont.getData().setScale(1.8f);
        statsFont = new BitmapFont();  statsFont.getData().setScale(1.35f);
        hintFont  = new BitmapFont();  hintFont.getData().setScale(1.2f);

        startX = (DungeonGame.WIDTH - (3 * CW + 2 * GAP)) / 2f;
        batch = new SpriteBatch();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.04f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float mx = Gdx.input.getX();
        float my = DungeonGame.HEIGHT - Gdx.input.getY();
        boolean click = Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT);

        batch.begin();
// set coler done 
        if (background != null) {
            batch.setColor(1, 1, 1, 0.30f);
            batch.draw(background, 0, 0, DungeonGame.WIDTH, DungeonGame.HEIGHT);
            batch.setColor(1, 1, 1, 1);
        }

        titleFont.setColor(Color.GOLD);
        titleFont.draw(batch, "CHOOSE YOUR HERO", 155, 565);

        for (int i = 0; i < 3; i++) {
            float cx = startX + i * (CW + GAP);

            if (mx > cx && mx < cx + CW && my > CY && my < CY + CH) {
                selected = i;
            }

            boolean sel = (selected == i);

            // Shadow
            fillRect(0, 0, 0, 0.5f, cx + 4, CY - 4, CW, CH);

            // Card body
            if (sel) fillRect(cols[i].r * 0.25f, cols[i].g * 0.25f, cols[i].b * 0.25f, 0.92f, cx, CY, CW, CH);
            else     fillRect(0.08f, 0.08f, 0.14f, 0.88f, cx, CY, CW, CH);

            // Border
            Color border = sel ? cols[i] : Color.DARK_GRAY;
            fillRect(border.r, border.g, border.b, 1f, cx,      CY + CH - 3, CW, 3);
            fillRect(border.r, border.g, border.b, 1f, cx,      CY,          CW, 3);
            fillRect(border.r, border.g, border.b, 1f, cx,      CY,          3,  CH);
            fillRect(border.r, border.g, border.b, 1f, cx+CW-3, CY,          3,  CH);

            // Portrait
            if (portraits[i] != null) {
                float pSize = 100f;
                float px = cx + (CW - pSize) / 2f;
                float py = CY + CH - 130;
                batch.setColor(1, 1, 1, sel ? 1f : 0.7f);
                batch.draw(portraits[i], px, py, pSize, pSize);
                batch.setColor(1, 1, 1, 1);
            }

            // Name
            nameFont.setColor(sel ? cols[i] : Color.GRAY);
            nameFont.draw(batch, names[i], cx + 14, CY + CH - 16);

            // Divider
            fillRect(border.r, border.g, border.b, 0.6f, cx + 10, CY + CH - 140, CW - 20, 2);

            // Stats
            statsFont.setColor(Color.GREEN);
            statsFont.draw(batch, "HP  : " + hps[i], cx + 14, CY + CH - 156);
            statsFont.setColor(new Color(1f, 0.45f, 0.1f, 1f));
            statsFont.draw(batch, "ATK : " + atks[i], cx + 14, CY + CH - 180);
            statsFont.setColor(Color.SKY);
            statsFont.draw(batch, "DEF : " + defs[i], cx + 14, CY + CH - 204);

            // Description
            statsFont.setColor(Color.LIGHT_GRAY);
            statsFont.draw(batch, line1[i], cx + 14, CY + CH - 234);
            statsFont.draw(batch, line2[i], cx + 14, CY + CH - 258);

            // Selected badge
            if (sel) {
                fillRect(cols[i].r, cols[i].g, cols[i].b, 0.25f, cx + 10, CY + 8, CW - 20, 34);
                nameFont.setColor(Color.GOLD);
                nameFont.draw(batch, "SELECTED", cx + 22, CY + 36);
            }
        }

        float bx = DungeonGame.WIDTH / 2f - 120, by = 56, bw = 240, bh = 58;
        boolean bHov = mx > bx && mx < bx + bw && my > by && my < by + bh;

        fillRect(bHov ? 0.9f : 0.5f, bHov ? 0.7f : 0.4f, 0f, 1f, bx, by, bw, bh);
        fillRect(1f, 0.85f, 0f, 1f, bx, by + bh - 3, bw, 3);

        titleFont.setColor(Color.WHITE);
        titleFont.draw(batch, "START!", bx + 46, by + 42);

        hintFont.setColor(new Color(0.6f, 0.6f, 0.6f, 1f));
        hintFont.draw(batch, "Hover a card to preview  •  Click START! to begin", 95, 28);

        batch.end();

        if (bHov && click) {
            game.setScreen(new GameScreen(game, names[selected], hps[selected], atks[selected]));
        }
    }

    private void fillRect(float r, float g, float b, float a, float x, float y, float w, float h) {
        batch.setColor(r, g, b, a);
        batch.draw(pixel, x, y, w, h);
        batch.setColor(1, 1, 1, 1);
    }

    @Override public void show() {}
    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        pixel.dispose();
        titleFont.dispose();
        nameFont.dispose();
        statsFont.dispose();
        hintFont.dispose();
        if (background != null) background.dispose();
        for (Texture p : portraits) if (p != null) p.dispose();
    }
}
