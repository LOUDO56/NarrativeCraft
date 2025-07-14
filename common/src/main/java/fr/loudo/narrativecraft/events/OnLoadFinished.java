package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;

public class OnLoadFinished {

    public static void loadFinished() {
        NarrativeCraftMod.getInstance().getNarrativeCraftLogoRenderer().init();
    }

}
