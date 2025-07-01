package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;

public class OnAttack {

    public static boolean cancelAttack() {
        return NarrativeCraftMod.getInstance().isCutsceneMode();
    }

}
