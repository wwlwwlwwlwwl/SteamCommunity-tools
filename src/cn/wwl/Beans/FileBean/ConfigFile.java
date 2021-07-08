package cn.wwl.Beans.FileBean;

import cn.wwl.FileLoader;

import java.util.*;

/**
 * 配置文件 用于自定义功能
 */
public class ConfigFile {
    private String CONFIG_VERSION = FileLoader.VERSION;
    private boolean Debug = false;
    private String Link = "steamcommunity.com";
    private String Basic_Auth = "NONE";
    private String SessionID = "SessionID";
    private String SteamLoginSecure = "SteamLoginSecure";
    private String steamRememberLogin = "SteamRememberLogin";
    private Map<String,String> steamMachineAuth = new HashMap<>();
    private ScanMode scanMode = ScanMode.Switch;
    private boolean PriceCheckerMode = false;
    private int randomCount = 100;
    private int BasicDelay = 10000;
    private int RandomDelay = 5000;
    private int MaxThreads = 32;
    private boolean EnableLog = true;
    private boolean useProxy = false;
    private boolean useTray = true;
    private boolean checkLogin = true;
    private List<Bot> Bots = setDefault();

    public String getCONFIG_VERSION() {
        return CONFIG_VERSION;
    }

    public String getSessionID() {
        return SessionID;
    }

    public String getSteamLoginSecure() {
        return SteamLoginSecure;
    }

    public String getLink() {
        return Link;
    }

    public void setLink(String link) {
        Link = link;
    }

    public int getBasicDelay() {
        return BasicDelay;
    }

    public int getRandomDelay() {
        return RandomDelay;
    }

    public int getMaxThreads() {
        return MaxThreads;
    }

    public List<Bot> getBots() {
        return Bots;
    }

    public boolean isDebug() {
        return Debug;
    }

    public String getBasic_Auth() {
        return Basic_Auth;
    }

    public void setBasic_Auth(String basic_Auth) {
        Basic_Auth = basic_Auth;
    }

    public void setDebug(boolean debug) {
        Debug = debug;
    }

    public void setCONFIG_VERSION(String CONFIG_VERSION) {
        this.CONFIG_VERSION = CONFIG_VERSION;
    }

    public void setSessionID(String sessionID) {
        SessionID = sessionID;
    }

    public void setSteamLoginSecure(String steamLoginSecure) {
        SteamLoginSecure = steamLoginSecure;
    }

    public void setBasicDelay(int basicDelay) {
        BasicDelay = basicDelay;
    }

    public void setRandomDelay(int randomDelay) {
        RandomDelay = randomDelay;
    }

    public void setMaxThreads(int maxThreads) {
        MaxThreads = maxThreads;
    }

    public void setBots(List<Bot> bots) {
        Bots = bots;
    }

    public boolean isEnableLog() {
        return EnableLog;
    }

    public void setEnableLog(boolean enableLog) {
        EnableLog = enableLog;
    }

    public boolean isUseProxy() {
        return useProxy;
    }

    public void setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
    }

    public boolean isUseTray() {
        return useTray;
    }

    public void setUseTray(boolean useTray) {
        this.useTray = useTray;
    }

    public void setSteamMachineAuth(Map<String, String> steamMachineAuth) {
        this.steamMachineAuth = steamMachineAuth;
    }

    public void setSteamRememberLogin(String steamRememberLogin) {
        this.steamRememberLogin = steamRememberLogin;
    }

    public Map<String, String> getSteamMachineAuth() {
        return steamMachineAuth;
    }

    public String getSteamRememberLogin() {
        return steamRememberLogin;
    }

    public void setScanMode(ScanMode scanMode) {
        this.scanMode = scanMode;
    }

    public ScanMode getScanMode() {
        return scanMode;
    }

    public int getRandomCount() {
        return randomCount;
    }

    public void setRandomCount(int randomCount) {
        this.randomCount = randomCount;
    }

    public boolean isPriceCheckerMode() {
        return PriceCheckerMode;
    }

    public void setPriceCheckerMode(boolean priceCheckerMode) {
        PriceCheckerMode = priceCheckerMode;
    }

    public boolean isCheckLogin() {
        return checkLogin;
    }

    public void setCheckLogin(boolean checkLogin) {
        this.checkLogin = checkLogin;
    }

    /**
     * 载入列表示例
     * @return Bot列表
     */
    private List<Bot> setDefault() {
        Bot awp = new Bot();
        awp.setItemsName("AWP | Safari Mesh (Battle-Scarred)");

        awp.setMaxFloat(-1F);
        awp.setMinFloat(0.7F);
        awp.setMaxPrice(2.0F);
        awp.setMinPrice(-1F);
        awp.setBotName("AWP High Float");
/*
        int[] paint = new int[]{537,847,157,167,628,403,479,244,999,376,432,910,293,623,906,740,331,114,155,427,961,989,77,384,881,836,389,634};
        Bot fnFN = new Bot();
        fnFN.setBotName("FN57 FN Paint");
        fnFN.setItemsName("Five-SeveN | Kami (Factory New)");
        fnFN.setMaxPrice(8.0F);
        fnFN.setPaintSeed(paint);
        Bot fnMW = new Bot();
        fnMW.setBotName("FN57 MW Paint");
        fnMW.setItemsName("Five-SeveN | Kami (Minimal Wear)");
        fnMW.setMaxPrice(6.0F);
        fnMW.setPaintSeed(paint);
        Bot fnFT = new Bot();
        fnFT.setBotName("FN57 FT Paint");
        fnFT.setItemsName("Five-SeveN | Kami (Field-Tested)");
        fnFT.setMaxPrice(5.5F);
        fnFT.setPaintSeed(paint);
        fnFT.setMaxFloat(0.2F);
 */
        return Collections.singletonList(awp);
        //return Arrays.asList(awp,fnFN,fnMW,fnFT);
    }

    @Override
    public String toString() {
        return "ConfigFile{" +
                "CONFIG_VERSION='" + CONFIG_VERSION + '\'' +
                ", Debug=" + Debug +
                ", Link='" + Link + '\'' +
                ", Basic_Auth='" + Basic_Auth + '\'' +
                ", SessionID='" + SessionID + '\'' +
                ", SteamLoginSecure='" + SteamLoginSecure + '\'' +
                ", steamRememberLogin='" + steamRememberLogin + '\'' +
                ", steamMachineAuth=" + steamMachineAuth +
                ", scanMode=" + scanMode +
                ", PriceCheckerMode=" + PriceCheckerMode +
                ", randomCount=" + randomCount +
                ", BasicDelay=" + BasicDelay +
                ", RandomDelay=" + RandomDelay +
                ", MaxThreads=" + MaxThreads +
                ", EnableLog=" + EnableLog +
                ", useProxy=" + useProxy +
                ", useTray=" + useTray +
                ", checkLogin=" + checkLogin +
                ", Bots=" + Bots +
                '}';
    }

    public static class Bot {
        private boolean Enabled;
        private String BotName = "Bot";
        private String ItemsName = "AWP | Safari Mesh (Battle-Scarred)";

        private boolean AutoBuy = false;
        private float MaxFloat = -1F;
        private float MinFloat = -1F;
        private float MaxPrice = -1F;
        private float MinPrice = -1F;
        private int HasStickers = 0;
        private int[] PaintSeed = new int[]{0};

        public boolean isEnabled() {
            return Enabled;
        }

        public void setEnabled(boolean enabled) {
            Enabled = enabled;
        }

        public String getBotName() {
            return BotName;
        }

        public String getItemsName() {
            return ItemsName;
        }

        public boolean isAutoBuy() {
            return AutoBuy;
        }

        public float getMaxFloat() {
            return MaxFloat;
        }

        public float getMinFloat() {
            return MinFloat;
        }

        public float getMaxPrice() {
            return MaxPrice;
        }

        public float getMinPrice() {
            return MinPrice;
        }

        public int getHasStickers() {
            return HasStickers;
        }

        public void setBotName(String botName) {
            BotName = botName;
        }

        public void setItemsName(String itemsName) {
            ItemsName = itemsName;
        }

        public void setAutoBuy(boolean autoBuy) {
            AutoBuy = autoBuy;
        }

        public void setMaxFloat(float maxFloat) {
            MaxFloat = maxFloat;
        }

        public void setMinFloat(float minFloat) {
            MinFloat = minFloat;
        }

        public void setMaxPrice(float maxPrice) {
            MaxPrice = maxPrice;
        }

        public void setMinPrice(float minPrice) {
            MinPrice = minPrice;
        }

        public void setHasStickers(int hasStickers) {
            HasStickers = hasStickers;
        }

        public int[] getPaintSeed() {
            return PaintSeed;
        }

        public void setPaintSeed(int[] paintSeed) {
            PaintSeed = paintSeed;
        }

        @Override
        public String toString() {
            return "Bot{" +
                    "Enabled=" + Enabled +
                    ", BotName='" + BotName + '\'' +
                    ", ItemsName='" + ItemsName + '\'' +
                    ", AutoBuy=" + AutoBuy +
                    ", MaxFloat=" + MaxFloat +
                    ", MinFloat=" + MinFloat +
                    ", MaxPrice=" + MaxPrice +
                    ", MinPrice=" + MinPrice +
                    ", HasStickers=" + HasStickers +
                    ", PaintSeed=" + Arrays.toString(PaintSeed) +
                    '}';
        }
    }

    public static enum ScanMode {
        Random,
        Switch;
    }
}
