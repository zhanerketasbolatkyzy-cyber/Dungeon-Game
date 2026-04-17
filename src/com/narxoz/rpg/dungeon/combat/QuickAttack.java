package com.narxoz.rpg.dungeon.combat;
import com.narxoz.rpg.dungeon.character.GameCharacter;

public class QuickAttack implements CombatStrategy {
    @Override
    public void attack(GameCharacter attacker, GameCharacter defender) {
        int damage = (int) (attacker.getAttackPower() * 0.8);
        defender.takeDamage(damage);
        System.out.println("⚔️ [" + attacker.getName() + "] Quick Strike: " + damage + " damage.");
    }
}