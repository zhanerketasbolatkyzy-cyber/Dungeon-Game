package com.narxoz.rpg.dungeon.world;

public class Note {
    private String content;

    public Note(String content) {
        this.content = content;
    }

    public void read() {
        System.out.println("\n📜 You found an old, blood-stained parchment:");
        System.out.println("-------------------------------------------------");
        System.out.println("\"" + content + "\"");
        System.out.println("-------------------------------------------------");
    }
}