package com.narxoz.rpg.dungeon.character;

public class Hero extends GameCharacter {
    private int experience = 0;
    private int level = 1;

    public Hero(String name, int hp, int atk, String art) {
        super(name, hp, atk, art);
    }

    public void gainExperience(int amount) {
        this.experience += amount;
        System.out.println("✨ Gained " + amount + " XP!");
        if (this.experience >= 100) {
            levelUp();
        }
    }

    private void levelUp() {
        this.level++;
        this.experience = 0;
        this.attackPower += 5; // Күші артады
        this.health += 20;     // HP қосылады
        System.out.println("🎊 LEVEL UP! You are now Level " + level);
        System.out.println("⚔️ Attack increased to " + attackPower);
    }
}