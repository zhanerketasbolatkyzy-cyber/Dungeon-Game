package com.narxoz.rpg.dungeon;

import com.badlogic.gdx.Game;
import com.narxoz.rpg.dungeon.screens.MainMenuScreen;

public class DungeonGame extends Game {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    @Override
    public void create() {
        setScreen(new MainMenuScreen(this));
    }
}
