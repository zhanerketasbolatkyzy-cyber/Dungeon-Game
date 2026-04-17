package com.narxoz.rpg.dungeon.world;
import com.narxoz.rpg.dungeon.character.GameCharacter;

public class Room {
    private String name;
    private GameCharacter enemy;
    private boolean hasTrap;
    private Note note; // Жаңа айнымалы

    public Room(String name, GameCharacter enemy, boolean hasTrap, Note note) {
        this.name = name;
        this.enemy = enemy;
        this.hasTrap = hasTrap;
        this.note = note;
    }

    public void onEnter(GameCharacter hero) {
        System.out.println("\n========================================");
        System.out.println("📍 Floor: " + name);
        if (hasTrap) {
            System.out.println("⚠️ TRAP! You stepped on hidden spikes!");
            hero.takeDamage(10);
        }
        if (note != null) {
            note.read(); // Бөлмеге кіргенде хат оқылады
        }
        if (enemy != null) {
            System.out.println("⚔️ A " + enemy.getName() + " blocks your path!");
        }
    }

    public GameCharacter getEnemy() { return enemy; }
    public boolean isCleared() { return enemy == null || !enemy.isAlive(); }
}