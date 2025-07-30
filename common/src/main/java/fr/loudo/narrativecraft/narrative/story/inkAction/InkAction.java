package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.InkTagType;
import fr.loudo.narrativecraft.narrative.story.inkAction.validation.ErrorLine;

public abstract class InkAction {

    protected String name;
    protected String[] command;
    protected InkTagType inkTagType;
    protected StoryHandler storyHandler;

    public InkAction(StoryHandler storyHandler, InkTagType inkTagType, String command) {
        this.storyHandler = storyHandler;
        this.name = "";
        this.inkTagType = inkTagType;
        this.command = command.split(" ");
    }

    public abstract InkActionResult execute();
    abstract void sendDebugDetails();
    public abstract ErrorLine validate(String[] command, int line, String lineText, Scene scene);

    public String getName() {
        return name;
    }

    public String getCommand() {
        return String.join(" ", command);
    }

    public StoryHandler getStoryHandler() {
        return storyHandler;
    }

    public void setStoryHandler(StoryHandler storyHandler) {
        this.storyHandler = storyHandler;
    }

    public static String parseName(String[] command, int index) {
        String name = command[index];
        if (name.startsWith("\"")) {
            StringBuilder builder = new StringBuilder();
            for (int i = index; i < command.length; i++) {
                builder.append(command[i]);
                if(command[i].endsWith("\"")) break;
                if (i < command.length - 1) builder.append(" ");
            }
            name = builder.toString();
            if (name.startsWith("\"") && name.endsWith("\"") && name.length() >= 2) {
                name = name.substring(1, name.length() - 1);
            }
        }
        return name;
    }

    public static int getNewIndexFromName(String[] command, int index) {
        String name = command[index];
        if (name.startsWith("\"")) {
            for (int i = index; i < command.length; i++) {
                if (command[i].endsWith("\"")) {
                    return i;
                }
            }
        }
        return index;
    }
}
