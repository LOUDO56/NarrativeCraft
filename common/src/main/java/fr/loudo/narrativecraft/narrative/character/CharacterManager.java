package fr.loudo.narrativecraft.narrative.character;

import fr.loudo.narrativecraft.Constants;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;

import java.io.IOException;
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
        try {
            characters.add(newCharacter);
            NarrativeCraftFile.saveCharacter(newCharacter);
            return true;
        } catch (IOException e) {
            Constants.LOG.warn("Couldn't save character to file: " + e);
            return false;
        }
    }

    public boolean removeCharacter(Character character) {
        characters.remove(character);
        return true;
    }

    public void setCharacters(List<Character> characters) {
        this.characters = characters;
    }
}
