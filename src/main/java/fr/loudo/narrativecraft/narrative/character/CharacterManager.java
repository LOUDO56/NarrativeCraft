package fr.loudo.narrativecraft.narrative.character;

import java.util.ArrayList;
import java.util.List;

public class CharacterManager {

    private List<Character> characters;

    public CharacterManager() {
        this.characters = new ArrayList<>();
    }

    public Character getCharacterByName(String characterName) {
        for(Character character : characters) {
            if(character.getName().equalsIgnoreCase(characterName)) {
                return character;
            }
        }
        return null;
    }

    public boolean characterExists(String characterName) {
        for(Character character : characters) {
            if(character.getName().equalsIgnoreCase(characterName)) {
                return true;
            }
        }
        return false;
    }

    public boolean addCharacter(Character newCharacter) {
        if(characters.contains(newCharacter)) return false;
        characters.add(newCharacter);
        return true;
    }

    public boolean removeCharacter(Character character) {
        characters.remove(character);
        return true;
    }
}
