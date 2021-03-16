package me.alexutzzu.botdiscord.utils.constants;

public enum Emoji {

    /**
     * Emoji for errors, success and warnings
     */
    SUCCESS("\u2705", "Success", null),
    WARNING("\u26A0", "Warning", null),
    ERROR("\u274C", "Error", null),

    /**
     *  Emoji for ticket categories
     */
    OTHER("\u2754", "Other", "For any ticket that does not fit in the other categories."),
    BOT_ISSUES("\uD83E\uDD16", "Bot Issues", "If your problem relates to this bot, use this category."),
    STAFF_REPORT("\uD83D\uDEE0", "Staff Report", "Found a Staff Member misbehaving? You can open a private ticket and it will be looked into"),
    USER_REPORT("\uD83D\uDEAB", "User Report", "Use this category to report a user who is breaking the server's rules."),
    REQUEST_FEATURE("\uD83D\uDCE2", "Request Feature", "Have an idea of a feature that would be useful for the bot? Then you can use this category.")
    ;


    private final String unicode;
    private final String name;
    private final String description;
    Emoji(String unicode, String name, String description) {
        this.unicode = unicode;
        this.name = name;
        this.description = description;
    }

    /**
     * Use this method to get the String unicode in UTF-8 for the specified emoji
     * @return The Unicode for the specified emoji
     */
    public String getUnicode() {
        return this.unicode;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
