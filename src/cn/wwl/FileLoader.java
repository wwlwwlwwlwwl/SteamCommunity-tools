package cn.wwl;

import cn.wwl.Beans.FileBean.BuffConfig;
import cn.wwl.Beans.FileBean.ConfigFile;
import cn.wwl.Beans.MarketBean.CSGOItems;
import cn.wwl.Buff.Beans.BuffMarketItem;
import cn.wwl.Buff.Thread.ItemIDFetchThread;
import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 用于载入配置文件和写入缓存等功能
 */
public class FileLoader {

    /**
     * 版本号 用于同步配置文件 当配置文件与该版本号不同步时要求更新配置文件
     */
    public static final String VERSION = "1.0.8";

    public static final String START_DATE = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private static final Logger logger = LogManager.getLogger(FileLoader.class);
    private static final Object Lock = new Object();

    /**
     * 配置文件实例
     */
    public static final File configFile = new File("Config.json");

    /**
     * Buff的物品ID对应表
     */
    public static final File buffIDFile = new File("Buff_ID.json");

    /**
     * Buff的Cookie设置表
     */
    public static final File buffConfigFile = new File("BuffConfig.json");

    /**
     * BuffTools的输出
     */
    public static final File buffOutputFile = new File("BuffPrice_" + START_DATE + ".csv");

    /**
     * 载入后的配置文件实例
     */
    private static ConfigFile configs;

    /**
     * BuffTools的配置文件
     */
    private static BuffConfig buffConfig;
    /**
     * Buff的ID对应表
     */
    private static final List<BuffMarketItem> BuffIDMap = Collections.synchronizedList(new ArrayList<>());

    /**
     * 载入缓存并导入Bot中
     * @param bot 目标Bot
     * @param count 配置文件计数 用于保证不重复 默认从0开始写入
     */
    public static void loadCache(MarketBot bot,int count) {
        synchronized (Lock) {
            try {
                File cache = new File("Cache_" + bot.getTarget().getBotName() + "_" + count + ".json");

                if (!cache.exists()) {
                    logger.warn("Bot " + bot.getTarget().getBotName() + " Cache File Not Exists!");
                    return;
                }

                List<CSGOItems> items = new ArrayList<>();
                FileReader read = new FileReader(cache);
                BufferedReader reader = new BufferedReader(read);
                StringBuilder builder = new StringBuilder();

                reader.lines().forEach((s) -> builder.append(s.trim()));

                //logger.info(builder.toString());
                JsonArray object = JsonParser.parseString(builder.toString()).getAsJsonArray();
                int c = 0;
                for (JsonElement element : object) {
                    items.add(gson.fromJson(element, CSGOItems.class));
                    c++;
                }
                logger.info("Loaded " + c + " Market Item From Bot " + bot.getTarget().getBotName() + " Cache!");
                bot.setList(items);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将Bot的饰品列表写入缓存
     * @param bot 要进行写入的Bot
     */
    public static void saveCache(MarketBot bot) {
        if (configs.isEnableLog()) {
            synchronized (Lock) {
                try {
                    File cache = null;
                    for (int i = 0; i < Integer.MAX_VALUE; i++) {
                        File tmp = new File("Cache_" + bot.getTarget().getBotName() + "_" + i + ".json");
                        if (!tmp.exists()) {
                            cache = tmp;
                            break;
                        }
                    }
                    //File cache = new File("Cache_" + bot.getTarget().getBotName() + ".json");
                    if (cache == null) {
                        logger.error("Count > Integer.MAX_VALUE ? wtf???");
                        System.exit(99);
                    }
                    FileWriter writer = new FileWriter(cache);
                    writer.write(gson.toJson(bot.getList()));
                    writer.flush();
                    writer.close();
                    logger.info("Save Bot " + bot.getTarget().getBotName() + " Cache File Done!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取配置文件
     */
    public static void loadConfig() {
        synchronized (Lock) {
            try {
                //Config File Already Inited
                if (configs != null)
                    return;

            if (!configFile.exists() | configFile.length() == 0) {
                logger.warn("Config File Not Found! Please Set Config To Continue!");
                initConfig();
                System.exit(99);
            }

            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            StringBuilder builder = new StringBuilder();

            reader.lines().forEach(builder::append);

            try {
                configs = gson.fromJson(builder.toString(),ConfigFile.class);
            } catch (Exception e) {
                logger.warn("Init Config File Failed! Reset Config File...");
                initConfig();
                e.printStackTrace();
                System.exit(99);
            }

                if (configs.getSessionID().equals("SessionID")
                        | configs.getSteamLoginSecure().equals("SteamLoginSecure")) {
                    logger.error("SessionID Or SteamLoginSecure Not Set! You Must Set ID To Buy Items!");
                }

            if (!configs.getCONFIG_VERSION().equals(VERSION)) {
                updateConfig();
            }

            logger.info("Config File Inited!");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存配置文件 几乎未使用
     */
    public static void saveConfig() {
        synchronized (Lock) {
            try {
                if (configs == null)
                    return;

                if (!configFile.exists())
                    configFile.createNewFile();

                FileWriter writer = new FileWriter(configFile);
                writer.write(gson.toJson(configs));
                writer.flush();
                writer.close();

                logger.info("Config File Save Done.");
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
    }

    /**
     * 重置配置文件
     */
    private static void initConfig() {
        synchronized (Lock) {
            try {
                configFile.createNewFile();
                FileWriter writer = new FileWriter(configFile);
                writer.write(gson.toJson(new ConfigFile()));
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 提示用户配置文件需要更新
     */
    private static void updateConfig() {
        try {
            logger.error("Config File Need Update! Please Update File And Run Again!");

            FileWriter writer = new FileWriter(new File("Config.json"));

            configs.setCONFIG_VERSION(VERSION);
            writer.write(gson.toJson(configs));
            writer.flush();
            writer.close();

            System.exit(99);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将物品购买记录写入文件中
     * @param item 成功购买的物品列表
     */
    public static void writeGoods(List<CSGOItems> item) {
        try {
            File goodsFile = new File("Goods_" + START_DATE + ".json");

            if (!goodsFile.exists())
                goodsFile.createNewFile();

            FileWriter writer = new FileWriter(goodsFile);
            writer.write(gson.toJson(item));
            writer.flush();
            writer.close();

            logger.info("Goods File Save Done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取配置文件实例
     * @return 返回配置文件
     */
    public static ConfigFile getConfigs() {
        if (configs == null)
            loadConfig();
        return configs;
    }

    private static final List<Proxy> proxy_List = new ArrayList<>();
    private static final Random random = new Random();

    /**
     * 随机获取一个代理
     * @return 一个随机获取的代理
     */
    public static Proxy getRandomProxy() {
        return proxy_List.get(random.nextInt(proxy_List.size() - 1));
    }

    /**
     * 获取代理列表
     * @return 代理列表
     */
    public static List<Proxy> getProxyList() {
        return proxy_List;
    }

    /**
     * 载入代理列表
     */
    public static void initProxyList() {
        try {
            proxy_List.clear();
            File file = new File("proxy.txt");
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));

                reader.lines().forEach(FileLoader::PackageText);

                logger.info("Loaded " + proxy_List.size() + " Proxy.");
            } else {
                logger.error("proxy.txt Not Found! Make Sure Have proxy.txt And Format is *.*.*.*:*");
                logger.error("If You Not Wanna Use Proxy. Please Set Config File useProxy To False.");
                System.exit(99);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将代理封装入列表中
     * @param msg 代理的配置信息
     */
    private static void PackageText(String msg) {
        String[] ip = msg.split(":");
        FileLoader.proxy_List.add(new Proxy(Proxy.Type.HTTP,new InetSocketAddress(ip[0], Integer.parseInt(ip[1]))));
    }

    /**
     * 将maps写入BuffID配置表
     * @param list 对应的ID表
     */
    public static void saveBuffID(List<BuffMarketItem> list) {
        synchronized (Lock) {
            try {
                FileWriter writer = new FileWriter(buffIDFile);
                writer.write(gson.toJson(list));
                writer.flush();
                writer.close();
                //logger.info("Write Buff ID List Done.");
            } catch (Exception e) {
                logger.error("Write Buff ItemID Failed!",e);
            }
        }
    }

    /**
     * 从配置文件中读取ID表
     * @param ForceReload 强制重载
     * @return ID表
     */
    public static List<BuffMarketItem> getBuffID(boolean ForceReload) {
        synchronized (Lock) {
            try {

                if (!buffIDFile.exists())
                    buffIDFile.createNewFile();

                if (buffIDFile.length() == 0)
                    return BuffIDMap;

                if (ForceReload) {
                    BuffIDMap.clear();
                } else {
                    if (!BuffIDMap.isEmpty())
                        return BuffIDMap;

                }

                BufferedReader reader = new BufferedReader(new FileReader(buffIDFile));
                JsonElement element = JsonParser.parseReader(reader);

                element.getAsJsonArray().forEach((a) -> BuffIDMap.add(gson.fromJson(a,BuffMarketItem.class)));

                return BuffIDMap;
            } catch (Exception e) {
                logger.error("Write Buff ItemID Failed!",e);
            }
            return BuffIDMap;
        }
    }

    /**
     * 保存Buff的Cookie数据文件
     */
    public static void saveBuffConfigs() {
        synchronized (Lock) {
            try {
                if (!buffConfigFile.exists())
                    buffConfigFile.createNewFile();

                if (buffConfig == null) {
                    if (buffConfigFile.exists()) {
                        loadBuffConfigs();
                    } else {
                        buffConfig = new BuffConfig();
                    }
                }

                FileWriter writer = new FileWriter(buffConfigFile);
                writer.write(gson.toJson(buffConfig));
                writer.flush();
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 载入Buff的Cookie文件
     */
    public static void loadBuffConfigs() {
        synchronized (Lock) {
            try {
                if (!buffConfigFile.exists())
                    buffConfigFile.createNewFile();

                FileReader reader = new FileReader(buffConfigFile);
                buffConfig = gson.fromJson(reader, BuffConfig.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取Buff的配置文件
     * @return Buff配置文件
     */
    public static BuffConfig getBuffConfig() {
        return buffConfig;
    }


    public static void writeBuffPrice(String s) {
        synchronized (Lock) {
            try {
            if (!buffOutputFile.exists())
                buffOutputFile.createNewFile();

            FileWriter writer = new FileWriter(buffOutputFile);
            writer.write(s);
            writer.flush();
            writer.close();
            } catch (Exception e) {
                logger.error("Write Price Failed!",e);
            }
        }
    }
}
