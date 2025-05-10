package fr.loudo.narrativecraft.narrative.character;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CharacterManager {

    private List<Character> characters;

    public CharacterManager() {
        this.characters = new ArrayList<>();
    }
    
    public void setCharacters(List<Character> characters) {
        this.characters = characters;
    }
}
