package cn.wwl.Beans.MarketBean;
import java.util.List;

/**
 * 在CSGOFloat解析后的物品信息 将同步装载到物品中
 */
public class ItemInfo {

    private int origin;
    private int quality;
    private int rarity;
    private String a;
    private String d;
    private int paintseed;
    private int defindex;
    private int paintindex;
    private List<Stickers> stickers;
    private String floatid;
    private double floatvalue;
    private String s;
    private String m;
    private String imageurl;
    private double min;
    private double max;
    private String weapon_type;
    private String item_name;
    private String rarity_name;
    private String quality_name;
    private String origin_name;
    private String wear_name;
    private String full_item_name;
    public void setOrigin(int origin) {
        this.origin = origin;
    }
    public int getOrigin() {
        return origin;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }
    public int getQuality() {
        return quality;
    }

    public void setRarity(int rarity) {
        this.rarity = rarity;
    }
    public int getRarity() {
        return rarity;
    }

    public void setA(String a) {
        this.a = a;
    }
    public String getA() {
        return a;
    }

    public void setD(String d) {
        this.d = d;
    }
    public String getD() {
        return d;
    }

    public void setPaintseed(int paintseed) {
        this.paintseed = paintseed;
    }
    public int getPaintseed() {
        return paintseed;
    }

    public void setDefindex(int defindex) {
        this.defindex = defindex;
    }
    public int getDefindex() {
        return defindex;
    }

    public void setPaintindex(int paintindex) {
        this.paintindex = paintindex;
    }
    public int getPaintindex() {
        return paintindex;
    }

    public void setStickers(List<Stickers> stickers) {
        this.stickers = stickers;
    }
    public List<Stickers> getStickers() {
        return stickers;
    }

    public void setFloatid(String floatid) {
        this.floatid = floatid;
    }
    public String getFloatid() {
        return floatid;
    }

    public void setFloatvalue(double floatvalue) {
        this.floatvalue = floatvalue;
    }
    public double getFloatvalue() {
        return floatvalue;
    }

    public void setS(String s) {
        this.s = s;
    }
    public String getS() {
        return s;
    }

    public void setM(String m) {
        this.m = m;
    }
    public String getM() {
        return m;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
    public String getImageurl() {
        return imageurl;
    }

    public void setMin(double min) {
        this.min = min;
    }
    public double getMin() {
        return min;
    }

    public void setMax(double max) {
        this.max = max;
    }
    public double getMax() {
        return max;
    }

    public void setWeapon_type(String weapon_type) {
        this.weapon_type = weapon_type;
    }
    public String getWeapon_type() {
        return weapon_type;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }
    public String getItem_name() {
        return item_name;
    }

    public void setRarity_name(String rarity_name) {
        this.rarity_name = rarity_name;
    }
    public String getRarity_name() {
        return rarity_name;
    }

    public void setQuality_name(String quality_name) {
        this.quality_name = quality_name;
    }
    public String getQuality_name() {
        return quality_name;
    }

    public void setOrigin_name(String origin_name) {
        this.origin_name = origin_name;
    }
    public String getOrigin_name() {
        return origin_name;
    }

    public void setWear_name(String wear_name) {
        this.wear_name = wear_name;
    }
    public String getWear_name() {
        return wear_name;
    }

    public void setFull_item_name(String full_item_name) {
        this.full_item_name = full_item_name;
    }
    public String getFull_item_name() {
        return full_item_name;
    }


    @Override
    public String toString() {
        return "ItemInfo{" +
                "origin=" + origin +
                ", quality=" + quality +
                ", rarity=" + rarity +
                ", a='" + a + '\'' +
                ", d='" + d + '\'' +
                ", paintseed=" + paintseed +
                ", defindex=" + defindex +
                ", paintindex=" + paintindex +
                ", stickers=" + stickers +
                ", floatid='" + floatid + '\'' +
                ", floatvalue=" + floatvalue +
                ", s='" + s + '\'' +
                ", m='" + m + '\'' +
                ", imageurl='" + imageurl + '\'' +
                ", min=" + min +
                ", max=" + max +
                ", weapon_type='" + weapon_type + '\'' +
                ", item_name='" + item_name + '\'' +
                ", rarity_name='" + rarity_name + '\'' +
                ", quality_name='" + quality_name + '\'' +
                ", origin_name='" + origin_name + '\'' +
                ", wear_name='" + wear_name + '\'' +
                ", full_item_name='" + full_item_name + '\'' +
                '}';
    }

    public class Stickers {

        private int stickerId;
        private int slot;
        private String codename;
        private String material;
        private String name;
        public void setStickerId(int stickerId) {
            this.stickerId = stickerId;
        }
        public int getStickerId() {
            return stickerId;
        }

        public void setSlot(int slot) {
            this.slot = slot;
        }
        public int getSlot() {
            return slot;
        }

        public void setCodename(String codename) {
            this.codename = codename;
        }
        public String getCodename() {
            return codename;
        }

        public void setMaterial(String material) {
            this.material = material;
        }
        public String getMaterial() {
            return material;
        }

        public void setName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Stickers{" +
                    "stickerId=" + stickerId +
                    ", slot=" + slot +
                    ", codename='" + codename + '\'' +
                    ", material='" + material + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}