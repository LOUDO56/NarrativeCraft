package fr.loudo.narrativecraft.narrative.character;

import com.google.gson.Gson;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.world.entity.LivingEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class CharacterManager {

    private List<CharacterStory> characterStories;

    public CharacterManager() {
        this.characterStories = new ArrayList<>();
    }

    public void init() {
        characterStories = new ArrayList<>();
        File characterDirectory = NarrativeCraftFile.characterDirectory;
        File[] characterFolders = characterDirectory.listFiles();
        if(characterFolders != null) {
            for(File characterFolder : characterFolders) {
                try {
                    String content = Files.readString(new File(characterFolder, "data" + NarrativeCraftFile.EXTENSION_DATA_FILE).toPath());
                    CharacterStory characterStory = new Gson().fromJson(content, CharacterStory.class);
                    characterStory.setSkins(new ArrayList<>());
                    characterStories.add(characterStory);
                } catch (IOException e) {
                    throw new RuntimeException("Character " + characterFolder.getName()  + " couldn't be loaded!: " + e);
                }
            }
        }
        reloadSkin();
    }

    public List<CharacterStory> getCharacterStories() {
        return characterStories;
    }

    public void setCharacters(List<CharacterStory> characterStories) {
        this.characterStories = characterStories;
    }

    public LivingEntity getCharacterEntity(String name) {
        for(CharacterStory characterStory : characterStories) {
            if(characterStory.getName().equalsIgnoreCase(name)) {
                return characterStory.getEntity();
            }
        }
        return null;
    }

    public boolean characterExists(String name) {
        for(CharacterStory characterStory : characterStories) {
            if(characterStory.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public CharacterStory getCharacter(String name) {
        for(CharacterStory characterStory : characterStories) {
            if(characterStory.getName().equalsIgnoreCase(name)) {
                return characterStory;
            }
        }
        return null;
    }

    public void addCharacter(CharacterStory characterStory) {
        characterStories.add(characterStory);
    }

    public void removeCharacter(CharacterStory characterStory) {
        characterStories.remove(characterStory);
    }

    public void reloadSkin() {
        for(CharacterStory characterStory : characterStories) {
            File characterFolder = new File(NarrativeCraftFile.characterDirectory, Utils.getSnakeCase(characterStory.getName()));
            File skins = new File(characterFolder, "skins");
            File[] skinFiles = skins.listFiles();
            if(skinFiles != null) {
                characterStory.getSkins().clear();
                for(File skin : skinFiles) {
                    characterStory.getSkins().add(skin);
                }
            }
        }
    }
}
