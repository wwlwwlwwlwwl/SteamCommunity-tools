package cn.wwl.Buff.Beans;

import java.lang.reflect.Field;
import java.util.Objects;

public class BuffMarketItem {
    /**
     * 磨损
     */
    private String exterior;
    /**
     * 类型
     */
    private String quality;
    /**
     * 等级
     */
    private String rarity;
    /**
     * 类型
     */
    private String type;
    /**
     * 武器名称
     */
    private String weapon;

    private long id;
    private String english_Name;
    private String chinese_Name;
    private String steam_URL;

    public BuffMarketItem setChinese_Name(String chinese_Name) {
        this.chinese_Name = chinese_Name;
        return this;
    }

    public BuffMarketItem setEnglish_Name(String english_Name) {
        this.english_Name = english_Name;
        return this;
    }

    public BuffMarketItem setId(long id) {
        this.id = id;
        return this;
    }

    public BuffMarketItem setQuality(String quality) {
        this.quality = quality;
        return this;
    }

    public BuffMarketItem setRarity(String rarity) {
        this.rarity = rarity;
        return this;
    }

    public BuffMarketItem setSteam_URL(String steam_URL) {
        this.steam_URL = steam_URL;
        return this;
    }

    public BuffMarketItem setType(String type) {
        this.type = type;
        return this;
    }

    public BuffMarketItem setWeapon(String weapon) {
        this.weapon = weapon;
        return this;
    }

    public BuffMarketItem setExterior(String exterior) {
        this.exterior = exterior;
        return this;
    }

    public long getId() {
        return id;
    }

    public String getChinese_Name() {
        return chinese_Name;
    }

    public String getEnglish_Name() {
        return english_Name;
    }

    public String getQuality() {
        return quality;
    }

    public String getRarity() {
        return rarity;
    }

    public String getSteam_URL() {
        return steam_URL;
    }

    public String getType() {
        return type;
    }

    public String getWeapon() {
        return weapon;
    }

    public String getExterior() {
        return exterior;
    }


    /**
     * 通过反射直接设置包内部的值
     * @param key 名称
     * @param value 值
     * @return 本体
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public BuffMarketItem setValue(String key,String value) throws NoSuchFieldException, IllegalAccessException {

        Field f = this.getClass().getDeclaredField(key);
        f.setAccessible(true);
        f.set(this,value);

        return this;
    }

    @Override
    public String toString() {
        return "BuffMarketItem{" +
                "exterior='" + exterior + '\'' +
                ", quality='" + quality + '\'' +
                ", rarity='" + rarity + '\'' +
                ", type='" + type + '\'' +
                ", weapon='" + weapon + '\'' +
                ", id=" + id +
                ", english_Name='" + english_Name + '\'' +
                ", chinese_Name='" + chinese_Name + '\'' +
                ", steam_URL='" + steam_URL + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BuffMarketItem that = (BuffMarketItem) o;

        if (id != that.id) return false;
        return Objects.equals(english_Name, that.english_Name);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (english_Name != null ? english_Name.hashCode() : 0);
        return result;
    }
}
