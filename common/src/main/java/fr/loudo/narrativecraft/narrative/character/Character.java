package fr.loudo.narrativecraft.narrative.character;

public class Character {

    private String name;
    private String description;
    private int age;
    private String birthdate;
    //TODO: custom skin, one string attr to means either a player name, mineskin url or local skin path.
    //TODO: Custom dialogue of character options here?
    //TODO: CharacterLayer class to enable certain part of body of the character


    public Character(String name) {
        this.name = name;
    }

    public Character(String name, String description, int age, String birthdate) {
        this.name = name;
        this.description = description;
        this.age = age;
        this.birthdate = birthdate;
    }


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getAge() {
        return age;
    }

    public String getBirthdate() {
        return birthdate;
    }
}
