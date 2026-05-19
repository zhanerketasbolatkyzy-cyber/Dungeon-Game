package com.narxoz.rpg.dungeon.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.narxoz.rpg.dungeon.DungeonGame;

public class MainMenuScreen implements Screen {
    private DungeonGame game;
    private SpriteBatch batch;
    private Texture background;

    // Button zones (match the PNG positions)
    private final float btnStartX = 240, btnStartY = 190, btnStartW = 320, btnStartH = 90;
    private final float btnExitX  = 240, btnExitY  =  90, btnExitW  = 320, btnExitH  = 90;

    public MainMenuScreen(DungeonGame game) {
        this.game = game;
        batch = new SpriteBatch();
        background = new Texture(Gdx.files.internal("menu_bg.png"));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float mx = Gdx.input.getX();
        float my = DungeonGame.HEIGHT - Gdx.input.getY();
        boolean click = Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT);

        batch.begin();
        batch.draw(background, 0, 0, DungeonGame.WIDTH, DungeonGame.HEIGHT);
        batch.end();

        // START → CharacterSelect
        if (mx > btnStartX && mx < btnStartX + btnStartW
            && my > btnStartY && my < btnStartY + btnStartH && click) {
            game.setScreen(new CharacterSelectScreen(game));
        }

        // EXIT
        if (mx > btnExitX && mx < btnExitX + btnExitW
            && my > btnExitY && my < btnExitY + btnExitH && click) {
            Gdx.app.exit();
        }
    }

    @Override public void show() {}
    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { batch.dispose(); background.dispose(); }
}
