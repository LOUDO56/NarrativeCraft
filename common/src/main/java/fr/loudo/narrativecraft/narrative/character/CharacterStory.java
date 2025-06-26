package fr.loudo.narrativecraft.narrative.character;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.dialog.DialogData;
import fr.loudo.narrativecraft.screens.storyManager.characters.CharactersScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.npcs.NpcScreen;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class CharacterStory extends NarrativeEntry {

    private transient LivingEntity entity;
    private transient EntityType<?> entityType;
    private transient CharacterSkinController characterSkinController;
    private transient Scene scene;
    private int entityTypeId;
    private PlayerSkin.Model model;
    private String birthdate;
    private CharacterType characterType;
    //TODO: custom skin, one string attr to means either a player name, mineskin url or local skin path.
    //TODO: Custom dialogue of character options here?
    //TODO: CharacterLayer class to enable certain part of body of the character

    public CharacterStory(String name) {
        super(name, "");
        characterType = CharacterType.MAIN;
        characterSkinController = new CharacterSkinController(this);
        entityType = EntityType.PLAYER;
        entityTypeId = BuiltInRegistries.ENTITY_TYPE.getId(entityType);
    }

    public CharacterStory(String name, String description, PlayerSkin.Model model, CharacterType characterType, String day, String month, String year) {
        super(name, description);
        this.name = name;
        this.description = description;
        this.birthdate = day + "/" + month + "/" + year;
        this.model = model;
        this.characterType = characterType;
        characterSkinController = new CharacterSkinController(this);
        entityType = EntityType.PLAYER;
        entityTypeId = BuiltInRegistries.ENTITY_TYPE.getId(entityType);
    }

    public CharacterStory(String name, String description, LivingEntity entity, EntityType<?> entityType, CharacterSkinController characterSkinController, Scene scene, int entityTypeId, PlayerSkin.Model model, String birthdate, CharacterType characterType) {
        super(name, description);
        this.entity = entity;
        this.entityType = entityType;
        this.characterSkinController = characterSkinController;
        this.scene = scene;
        this.entityTypeId = entityTypeId;
        this.model = model;
        this.birthdate = birthdate;
        this.characterType = characterType;
    }

    public void update(String name, String description, String day, String month, String year) {
        String oldName = this.name;
        String oldDescription = this.description;
        String oldBirthdate = this.birthdate;
        this.name = name;
        this.description = description;
        this.birthdate = day + "/" + month + "/" + year;
        boolean result = characterType == CharacterType.MAIN ? NarrativeCraftFile.updateCharacterFolder(oldName, name) : NarrativeCraftFile.updateNpcSceneFolder(oldName, name, scene);
        if(!result) {
            this.name = oldName;
            this.description = oldDescription;
            this.birthdate = oldBirthdate;
            ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("screen.characters_manager.update.failed", name));
            return;
        }
        characterSkinController.unCacheSkins();
        ScreenUtils.sendToast(Translation.message("global.info"), Translation.message("toast.description.updated"));
        Minecraft.getInstance().setScreen(reloadScreen());
    }

    public void updateEntityType(EntityType<?> entityType) {
        int oldEntityId = entityTypeId;
        EntityType<?> oldEntityType = this.entityType;
        this.entityType = entityType;
        entityTypeId = BuiltInRegistries.ENTITY_TYPE.getId(entityType);
        boolean result = characterType == CharacterType.MAIN ? NarrativeCraftFile.updateCharacterFolder(this) : NarrativeCraftFile.updateNpcSceneFolder(this, scene);
        if(!result) {
            this.entityType = oldEntityType;
            entityTypeId = oldEntityId;
            ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("screen.characters_manager.update.failed", name));
            return;
        }
        Minecraft.getInstance().setScreen(reloadScreen());
    }

    @Override
    public void update(String name, String description) {}

    @Override
    public void remove() {
        if(characterType == CharacterType.MAIN) {
            NarrativeCraftMod.getInstance().getCharacterManager().removeCharacter(this);
            NarrativeCraftFile.removeCharacterFolder(this);
        } else if (characterType == CharacterType.NPC) {
            NarrativeCraftFile.removeNpcFolder(this);
            scene.removeNpc(this);
        }
        for(Chapter chapter : NarrativeCraftMod.getInstance().getChapterManager().getChapters()) {
            for(Scene scene1 : chapter.getSceneList()) {
                for(Animation animation : scene1.getAnimationList()) {
                    if(animation.getCharacter().getName().equalsIgnoreCase(name)) {
                        animation.setCharacter(null);
                        NarrativeCraftFile.updateAnimationFile(animation);
                    }
                }
                for(CameraAngleGroup cameraAngleGroup : scene1.getCameraAngleGroupList()) {
                    cameraAngleGroup.getCharacterStoryDataList().removeIf(characterStoryData -> characterStoryData.getCharacterStory().getName().equals(name));
                    NarrativeCraftFile.updateCameraAnglesFile(scene1);
                }
            }
        }
    }

    @Override
    public Screen reloadScreen() {
        if(characterType == CharacterType.MAIN) {
            return new CharactersScreen();
        } else if(characterType == CharacterType.NPC) {
            return new NpcScreen(scene);
        } else {
            return null;
        }
    }

    public void kill() {
        if(entity != null) {
            entity.remove(Entity.RemovalReason.KILLED);
            if(entity instanceof FakePlayer fakePlayer) {
                NarrativeCraftMod.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoRemovePacket(List.of(fakePlayer.getUUID())));
            }
        }
    }

    public String getBirthdate() {
        return birthdate;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public void setEntity(LivingEntity entity) {
        this.entity = entity;
    }

    public CharacterType getCharacterType() {
        return characterType;
    }

    public void setCharacterType(CharacterType characterType) {
        this.characterType = characterType;
    }

    public PlayerSkin.Model getModel() {
        return model;
    }

    public void setModel(PlayerSkin.Model model) {
        this.model = model;
    }

    public CharacterSkinController getCharacterSkinController() {
        return characterSkinController;
    }

    public void setCharacterSkinController(CharacterSkinController characterSkinController) {
        this.characterSkinController = characterSkinController;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public int getEntityTypeId() {
        return entityTypeId;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType<?> entityType) {
        this.entityType = entityType;
    }

    public enum CharacterType {
        MAIN,
        NPC
    }
}
