package com.narxoz.rpg.dungeon.character;

import com.narxoz.rpg.dungeon.util.ASCIIArt;

public class EnemyFactory {
    public static GameCharacter createEnemy(String type) {
        if (type.equals("SKELETON")) return new Hero("Skeleton", 30, 5, ASCIIArt.SKELETON);
        if (type.equals("BOSS")) return new Hero("Dragon Boss", 100, 15, ASCIIArt.BOSS);
        return new Hero("Zombie", 40, 7, ASCIIArt.ZOMBIE);
    }
}