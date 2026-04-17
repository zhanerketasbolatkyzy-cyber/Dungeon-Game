package com.narxoz.rpg.dungeon.character;

public abstract class GameCharacter {
    protected String name;
    protected int health;
    protected int attackPower;
    protected String art;

    public GameCharacter(String name, int health, int attackPower, String art) {
        this.name = name;
        this.health = health;
        this.attackPower = attackPower;
        this.art = art;
    }

    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getAttackPower() { return attackPower; }
    public boolean isAlive() { return health > 0; }

    public void takeDamage(int dmg) { this.health -= dmg; }
    public void displayStatus() {
        System.out.println(art);
        System.out.println(name + " HP: " + health);
    }
}