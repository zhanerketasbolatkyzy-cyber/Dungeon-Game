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

public class GameOverScreen implements Screen {
    private DungeonGame game;
    private SpriteBatch batch;
    private Texture pixel;
    private BitmapFont font;
    private BitmapFont bigFont;
    private BitmapFont smallFont;

    private boolean didWin;
    private int finalFloor;
    private int finalLevel;
    private String heroName;

    public GameOverScreen(DungeonGame game, boolean didWin, int finalFloor, int finalLevel, String heroName) {
        this.game = game;
        this.didWin = didWin;
        this.finalFloor = finalFloor;
        this.finalLevel = finalLevel;
        this.heroName = heroName;

        batch = new SpriteBatch();

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE); pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        font = new BitmapFont();
        font.getData().setScale(1.5f);
        bigFont = new BitmapFont();
        bigFont.getData().setScale(3f);
        smallFont = new BitmapFont();
        smallFont.getData().setScale(1.2f);
    }

    // Legacy constructor for compatibility
    public GameOverScreen(DungeonGame game) {
        this(game, false, 1, 1, "UNKNOWN");
    }

    @Override
    public void render(float delta) {
        if (didWin) {
            Gdx.gl.glClearColor(0.02f, 0.02f, 0.08f, 1);
        } else {
            Gdx.gl.glClearColor(0.08f, 0.01f, 0.01f, 1);
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        if (didWin) {
            // VICTORY SCREEN
            bigFont.setColor(Color.GOLD);
            bigFont.draw(batch, "YOU CONQUERED", 80, 450);
            bigFont.draw(batch, "THE DUNGEON!", 110, 390);

            font.setColor(new Color(0.8f, 0.6f, 1f, 1f));
            font.draw(batch, "But at what cost? The cycle begins anew...", 110, 330);

            // Stats
            fillRect(0.1f, 0.08f, 0.15f, 0.8f, 150, 160, 500, 140);

            font.setColor(Color.WHITE);
            font.draw(batch, "FINAL STATS", 310, 290);

            smallFont.setColor(Color.GOLD);
            smallFont.draw(batch, "Hero: " + heroName, 180, 260);
            smallFont.draw(batch, "Level: " + finalLevel, 180, 235);
            smallFont.draw(batch, "Floor reached: " + finalFloor + "/7", 180, 210);

            smallFont.setColor(Color.GREEN);
            smallFont.draw(batch, "STATUS: DUNGEON CLEARED!", 180, 185);

            font.setColor(Color.GOLD);
            font.draw(batch, "Press ENTER to return to menu", 160, 130);
        } else {
            // DEFEAT SCREEN
            bigFont.setColor(Color.RED);
            bigFont.draw(batch, "GAME OVER", 180, 430);

            font.setColor(Color.WHITE);
            font.draw(batch, "The dungeon claims another soul...", 150, 350);

            // Stats
            fillRect(0.12f, 0.05f, 0.05f, 0.8f, 150, 170, 500, 140);

            font.setColor(Color.WHITE);
            font.draw(batch, "FINAL STATS", 310, 300);

            smallFont.setColor(new Color(1f, 0.5f, 0.5f, 1f));
            smallFont.draw(batch, "Hero: " + heroName, 180, 270);
            smallFont.draw(batch, "Level: " + finalLevel, 180, 245);
            smallFont.draw(batch, "Fell on Floor: " + finalFloor + "/7", 180, 220);

            smallFont.setColor(Color.GRAY);
            smallFont.draw(batch, "STATUS: DEFEATED", 180, 195);

            font.setColor(Color.WHITE);
            font.draw(batch, "Press ENTER to try again", 200, 130);
        }

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new MainMenuScreen(game));
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
    @Override public void dispose() {
        batch.dispose(); font.dispose(); bigFont.dispose(); smallFont.dispose(); pixel.dispose();
    }
}
