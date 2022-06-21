package fr.cnrs.iremus.sherlock.pojo.user.config;

public class UserConfig {
    private String hexColor;
    private String unicodeChar;

    public UserConfig(String hexColor, String unicodeChar) {
        this.hexColor = hexColor;
        this.unicodeChar = unicodeChar;
    }

    public UserConfig() {
    }

    public String getHexColor() {
        return hexColor;
    }

    public void setHexColor(String hexColor) {
        this.hexColor = hexColor;
    }

    public String getUnicodeChar() {
        return unicodeChar;
    }

    public void setUnicodeChar(String unicodeChar) {
        this.unicodeChar = unicodeChar;
    }
}
