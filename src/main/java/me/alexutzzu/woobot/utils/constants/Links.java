package me.alexutzzu.woobot.utils.constants;

public enum Links {
    SUPPORT_SERVER("https://discord.gg/DWFCgXw9"),
    CONTROL_PANEL("Coming soon!");
    private final String link;
    Links(String link) {
        this.link=link;
    }

    public String getLink() {
        return link;
    }
}
