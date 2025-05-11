package fr.loudo.narrativecraft.files;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.level.storage.LevelResource;

import java.io.*;

public class NarrativeCraftFile {

    public static final String EXTENSION_SCRIPT_FILE = ".ink";
    public static final String EXTENSION_DATA_FILE = ".json";

    private static final String DIRECTORY_NAME = NarrativeCraftMod.MOD_ID;

    private static final String BUILD_DIRECTORY_NAME = "build";
    private static final String CHAPTERS_DIRECTORY_NAME = "chapters";
    private static final String CHARACTERS_DIRECTORY_NAME = "characters";
    private static final String SAVES_DIRECTORY_NAME = "saves";
    private static final String GLOBAL_VAR_INK_NAME = "main" + EXTENSION_SCRIPT_FILE;

    public static File mainDirectory;
    public static File chaptersDirectory;
    public static File characterDirectory;
    public static File savesDirectory;
    public static File buildDirectory;
    public static File globalVarInkFile;

    public static void init(MinecraftServer server) {
        mainDirectory = createDirectory(server.getWorldPath(LevelResource.ROOT).toFile(), DIRECTORY_NAME);
        chaptersDirectory = createDirectory(mainDirectory, CHAPTERS_DIRECTORY_NAME);
        characterDirectory = createDirectory(mainDirectory, CHARACTERS_DIRECTORY_NAME);
        savesDirectory = createDirectory(mainDirectory, SAVES_DIRECTORY_NAME);
        buildDirectory = createDirectory(mainDirectory, BUILD_DIRECTORY_NAME);
        globalVarInkFile = createFile(mainDirectory, GLOBAL_VAR_INK_NAME);
    }

    public static File getDetailsFile(File file) {
        return new File(file.getAbsoluteFile(), "details" + NarrativeCraftFile.EXTENSION_DATA_FILE);
    }

    public static boolean createChapterDirectory(Chapter chapter) {
        File chapterFolder = createDirectory(chaptersDirectory, String.valueOf(chapter.getIndex()));
        if(!chapterFolder.exists()) return false;
        try {
            new File(chapterFolder, "scenes").mkdir();
            File detailsFile = getDetailsFile(chapterFolder);
            detailsFile.createNewFile();
            String content = String.format("{\"name\":\"%s\",\"description\":\"%s\"}", chapter.getName(), chapter.getDescription());
            try(Writer writer = new BufferedWriter(new FileWriter(detailsFile))) {
                writer.write(content);
            }
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't create details file! " + e.getMessage());
            return false;
        }

    }

    private static File createDirectory(File parent, String name) {
        File directory = new File(parent, name);
        if(!directory.exists()) {
            if(!directory.mkdir()) NarrativeCraftMod.LOG.error("Couldn't create directory {}!", name);
        }
        return directory;
    }

    private static File createFile(File parent, String name) {
        File file = new File(parent, name);
        if(!file.exists()) {
            try {
                if(!file.createNewFile()) NarrativeCraftMod.LOG.error("Couldn't create file {}!", file.getAbsolutePath());
            } catch (IOException e) {
                NarrativeCraftMod.LOG.error("Couldn't create file {}! Cause: {}", file.getAbsolutePath(), e.getMessage());
            }
        }
        return file;
    }

}
