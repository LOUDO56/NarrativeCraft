package fr.loudo.narrativecraft.narrative.character;

import com.google.common.io.Files;
import com.mojang.blaze3d.platform.NativeImage;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CharacterSkinController {

    private final CharacterStory characterStory;
    private final List<File> skins;
    private final List<String> cachedSkins;
    private File currentSkin;

    public CharacterSkinController(CharacterStory characterStory) {
        this.characterStory = characterStory;
        skins = new ArrayList<>();
        cachedSkins = new ArrayList<>();
    }

    public void cacheSkins() {
        Minecraft minecraft = Minecraft.getInstance();
        unCacheSkins();
        for(File skin : skins) {
            String path = "character/" + Utils.getSnakeCase(characterStory.getName()) + "/" + Utils.getSnakeCase(skin.getName());
            minecraft.execute(() -> {
                try {
                    byte[] array = Files.toByteArray(skin);
                    NativeImage nativeImage = NativeImage.read(array);
                    DynamicTexture texture = new DynamicTexture(
                            nativeImage
                    );
                    minecraft.getTextureManager().register(
                            ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, path),
                            texture
                    );
                } catch (IOException ignored) {}
            });
            cachedSkins.add(path);
        }
    }

    public void unCacheSkins() {
        Minecraft minecraft = Minecraft.getInstance();
        for(String path : cachedSkins) {
            minecraft.execute(() -> {
                minecraft.getTextureManager().release(ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, path));
            });
        }
        cachedSkins.clear();
    }

    public File getMainSkinFile() {
        for(File skin : skins) {
            if(skin.getName().equals("main.png")) {
                return skin;
            }
        }
        return null;
    }

    public File getSkinFile(String name) {
        for(File skin : skins) {
            if(skin.getName().equals(name)) {
                return skin;
            }
        }
        return null;
    }

    public List<File> getSkins() {
        return skins;
    }

    public File getCurrentSkin() {
        return currentSkin;
    }

    public List<String> getCachedSkins() {
        return cachedSkins;
    }

    public void setCurrentSkin(File skinFile) {
        this.currentSkin = skinFile;
    }
}
