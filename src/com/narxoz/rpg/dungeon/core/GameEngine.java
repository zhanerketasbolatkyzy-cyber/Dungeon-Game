package com.narxoz.rpg.dungeon.core;

import com.narxoz.rpg.dungeon.character.*;
import com.narxoz.rpg.dungeon.combat.*;
import com.narxoz.rpg.dungeon.util.ASCIIArt;
import com.narxoz.rpg.dungeon.world.Room;
import com.narxoz.rpg.dungeon.world.Note;
import java.util.*;

public class GameEngine {
    private static GameEngine instance;
    private Hero hero;
    private List<Room> rooms = new ArrayList<>();
    private Scanner sc = new Scanner(System.in);

    private GameEngine() {}

    public static GameEngine getInstance() {
        if (instance == null) instance = new GameEngine();
        return instance;
    }

    public void start() {
        System.out.println(ASCIIArt.TITLE);
        System.out.print("Enter your name, Knight: ");
        String name = sc.nextLine();
        hero = new Hero(name, 100, 15, ASCIIArt.HERO);

        generateDungeon();
        gameLoop();
    }

    private void generateDungeon() {
        // Осы жердегі Note ішіндегі тексттерді өзіңнің идеяңа сай өзгертіп жазып ал
        rooms.add(new Room("Floor 1: The Cold Entrance", EnemyFactory.createEnemy("SKELETON"), false,
                new Note("Текст 1: Мұндағы хатта батырдың бастапқы ойы туралы жаз (өзің жазасың)")));

        rooms.add(new Room("Floor 2: The Bloodied Hall", EnemyFactory.createEnemy("ZOMBIE"), true,
                new Note("Текст 2: Бұл жерде күмәнді ойлар пайда болады (өзің жазасың)")));

        rooms.add(new Room("Floor 3: The Broken Mind", null, false,
                new Note("Текст 3: Босс туралы шындыққа жақын тұспал (өзің жазасың)")));

        rooms.add(new Room("Floor 4: THE THRONE ROOM", EnemyFactory.createEnemy("BOSS"), false, null));
    }

    private void gameLoop() {
        int floorCounter = 1;
        for (Room room : rooms) {
            if (!hero.isAlive()) break;
            room.onEnter(hero);
            hero.displayStatus();

            if (room.getEnemy() != null) {
                battle(room.getEnemy());
            }

            // Твист логикасы: Егер Боссты жеңсе
            if (room.getEnemy() != null && !room.getEnemy().isAlive() && room.getEnemy().getName().contains("Boss")) {
                playTwistEnding();
                return; // Ойын осы жерден тоқтайды
            }

            // Принцессаның әсері
            if (hero.isAlive() && floorCounter < 4) {
                princessWhisper(floorCounter);
            }
            floorCounter++;
        }

        if (!hero.isAlive()) System.out.println(ASCIIArt.GAME_OVER);
    }

    private void princessWhisper(int floor) {
        String[] whispers = {
                "Come closer, my brave knight... The darkness is near.",
                "You are much stronger than the last one who tried to 'save' me...",
                "The throne is waiting for a new master... I am waiting for you."
        };
        if (floor <= whispers.length) {
            System.out.println("\n A cold whisper echoes in your mind: '" + whispers[floor-1] + "'");
        }
    }

    private void playTwistEnding() {
        System.out.println("\n========================================");
        System.out.println("The Boss falls to his knees. His dark helmet shatters...");
        System.out.println("You look at his face. He looks EXACTLY like you. An older, exhausted version of you.");
        System.out.println("Boss (whispering): 'Run... She doesn't need to be saved... She needs a vessel...'");
        System.out.println("Suddenly, the room grows cold. A beautiful, terrifying voice speaks from the shadows.");
        System.out.println("Princess: 'Do not listen to him, my sweet knight. He was just... weak. But you... you are perfect.'");
        System.out.println("You feel a dark curse binding your soul. You cannot move. You walk towards the throne...");

        System.out.println(ASCIIArt.THRONE);

        System.out.println("\n[NEW ENDING UNLOCKED: THE ETERNAL WARDEN]");
        System.out.println("You are the new Dungeon Overlord. Waiting for the next 'hero' to arrive.");
        System.out.println("========================================");
    }

    private void battle(GameCharacter enemy) {
        while (hero.isAlive() && enemy.isAlive()) {
            System.out.println("\n[1] Quick Attack | [2] Heavy Attack | [3] Heal");
            System.out.print("Your move: ");
            String choice = sc.nextLine();

            CombatStrategy strategy;
            if (choice.equals("1")) strategy = new QuickAttack();
            else if (choice.equals("2")) strategy = new StrongAttack();
            else {
                hero.takeDamage(-20); // Heal logic (negative damage heals)
                System.out.println("Resting... +20 HP");
                strategy = null;
            }

            if (strategy != null) strategy.attack(hero, enemy);

            if (enemy.isAlive()) {
                hero.takeDamage(enemy.getAttackPower());
                System.out.println("<<< [" + enemy.getName() + "] hits you for " + enemy.getAttackPower() + " damage!");
            }
        }

        if (!enemy.isAlive()) {
            System.out.println("\n>> " + enemy.getName() + " has been defeated! <<");
        }
    }
}