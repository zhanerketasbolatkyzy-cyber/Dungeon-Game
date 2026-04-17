package com.narxoz.rpg.dungeon.combat;
import com.narxoz.rpg.dungeon.character.GameCharacter;

public class StrongAttack implements CombatStrategy {
    @Override
    public void attack(GameCharacter attacker, GameCharacter defender) {
        int damage = attacker.getAttackPower() * 2;
        defender.takeDamage(damage);
        System.out.println("🔥 [" + attacker.getName() + "] CRITICAL HIT: " + damage + " damage!");
    }
}