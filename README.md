#  Dungeon RPG

> *"I came to save a princess. The walls whisper my name..."*

A turn-based dungeon RPG built with **LibGDX** (Java). Fight through 4 floors of increasingly dangerous bosses, uncover a dark twist ending, and discover that the dungeon had its own plans for you all along.

---

##  Gameplay

You descend into a dungeon to rescue a princess — but nothing is as it seems.

- **Move** your hero across the battlefield with `←→` or `WAD`
- **Get close** to the enemy before attacking — range matters
- **Choose your actions** through a tab-based combat panel
- **Chain attacks** to build combos and unlock critical hits
- **Survive** 4 floors of bosses, each with unique abilities
- **Discover** the true nature of the dungeon in the final cutscene

---

##  Combat System

### Action Tabs

| Tab | Key | Skills |
|-----|-----|--------|
| ATTACK | `A` | `[1]` Quick Strike · `[2]` Heavy Blow |
| MAGIC  | `M` | `[3]` Drain · `[4]` Fire Bolt |
| ITEMS  | `I` | `[H]` Heal Potion · `[5]` Scroll of Destruction |

### Combo System
- Each consecutive attack builds your **combo counter**
- Every stack adds **+10% damage**
- At **3+ combo** → 25% chance for a **CRITICAL HIT** (×2 damage)
- Using a potion breaks your combo

### Attack Range
- You must **walk close** to the enemy to deal damage
- A range indicator (green/red line) shows whether you're in range
- If too far: *"TOO FAR! Move closer!"* — your turn is not consumed

---

## Bosses

| Floor | Boss | HP | ATK | Special |
|-------|------|----|-----|---------|
| 1 | **Skeleton** | 35 | 6 | — |
| 2 | **Zombie** | 50 | 8 | Regenerates HP every 3 turns |
| 3 | **Dark Mage** | 65 | 12 | 50% chance to cast Dark Bolt (×2 dmg) |
| 4 | **Dragon Boss** | 120 | 18 | 30% chance to Breathe Fire (×3 dmg) |

---

##  Heroes
Choose your fighter before entering the dungeon:

| Hero | HP | ATK | DEF | Playstyle |
|------|----|-----|-----|-----------|
| **Knight** | 120 | 18 | 5 | Balanced warrior |
| **Rogue** | 80 | 28 | 2 | Glass cannon |
| **Paladin** | 150 | 12 | 8 | Holy tank |

---

##  Story & Lore

Each floor begins with a **blood-stained parchment** — fragments of someone who came before you.

> *"A corpse in armor identical to mine. I choose not to think about it."*

Defeat all four bosses to reach the true ending — and learn what the dungeon really wanted from you.

---

##  Audio

| Sound | Trigger |
|-------|---------|
| `attack.wav` | Player attacks |
| `hit.wav` | Player takes damage |
| `death.wav` | Boss or hero dies |
| `heal.wav` | Potion used |
| `step.wav` | Hero walks |
| `bg_dungeon.ogg` | Background music (loops) |

---

##  Project Structure

```
dungeon-rpg/
├── core/src/main/java/com/narxoz/rpg/dungeon/
│   ├── DungeonGame.java              # Entry point
│   └── screens/
│       ├── MainMenuScreen.java       # Main menu
│       ├── CharacterSelectScreen.java # Hero selection
│       ├── GameScreen.java           # Core gameplay
│       └── GameOverScreen.java       # Death / ending
├── lwjgl3/                           # Desktop launcher
└── assets/
    ├── battle_bg.png
    ├── Hero_idle.png
    ├── Hero_walk/
    ├── Hero_attack/
    ├── Boss_attack/  Boss2_attack/  Boss3_attack/  Boss4_attack/
    └── sounds/
        ├── attack.wav  hit.wav  death.wav  heal.wav  step.wav
        └── bg_dungeon.ogg
```

---

##  Running the Game

### Requirements
- Java 11+
- Gradle

### Run
```bash
./gradlew lwjgl3:run
```

### Build JAR
```bash
./gradlew lwjgl3:jar
java -jar lwjgl3/build/libs/dungeon-rpg.jar
```

---

##  Built With

- [LibGDX](https://libgdx.com/) — Java game framework
- [LWJGL3](https://www.lwjgl.org/) — Desktop backend
- Java 11

---

##  Authors

Narxoz University — Final Project  
`com.narxoz.rpg.dungeon`
