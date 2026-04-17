package com.narxoz.rpg.dungeon.combat;
import com.narxoz.rpg.dungeon.character.GameCharacter;

public interface CombatStrategy {
    void attack(GameCharacter attacker, GameCharacter defender);
}