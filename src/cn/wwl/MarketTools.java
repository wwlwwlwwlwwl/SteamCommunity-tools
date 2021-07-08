package cn.wwl;

import cn.wwl.Threads.SteamCommunityManagerThread;
import cn.wwl.Beans.FileBean.ConfigFile;
import cn.wwl.Beans.MarketBean.CSGOItems;
import cn.wwl.Buff.BuffTools;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 主类 Steam CSGO 物品机器人
 */
public class MarketTools {

    /**
     * 禁止实例的构建 保证只拥有一个实例
     */
    private MarketTools() {
        preInit();
    }

    /**
     * 实例
     */
    private static MarketTools instance;

    /**
     * BuffTools的实例
     */
    private static BuffTools buffInstance;

    /**
     * 托盘管理器实例
     */
    private static TrayManager trayManager;

    /**
     * 管理线程 用于Bot的管理
     */
    private static Thread ManagerThread;

    /**
     * 线程池 用于获取磨损时使用
     */
    private static ThreadPoolExecutor executor;

    /**
     * 购买的物品列表
     */
    private final List<CSGOItems> goods = new ArrayList<>();

    /**
     * 在配置文件中的Bot列表 根据[命名,Bot]来保存
     */
    private static final Map<String, MarketBot> bots = new HashMap<>();
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Object buyLock = new Object();
    private static final Logger logger = LogManager.getLogger(MarketTools.class);

    /**
     * 暂停整个系统
     */
    public static boolean pause;

    /**
     * 初始化整个系统
     */
    private void preInit() {
        FileLoader.loadConfig();

        if (executor == null)
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(FileLoader.getConfigs().getMaxThreads());

        if (bots.isEmpty()) {
            for (ConfigFile.Bot bot : FileLoader.getConfigs().getBots()) {
                MarketBot marketBot = new MarketBot(bot);
                logger.info("Init Bot " + bot.getBotName() + " , Enabled : " + bot.isEnabled());
                bots.put(bot.getBotName(), marketBot);
                //FileLoader.loadCache(marketBot,0);
            }

            logger.info("Inited " + bots.size() + " Bots.");
        }

        if (FileLoader.getConfigs().isUseProxy() && FileLoader.getProxyList().isEmpty())
            FileLoader.initProxyList();

        if (FileLoader.getConfigs().isUseTray() && trayManager == null) {
            try {
                trayManager = new TrayManager();
                trayManager.initTray();
            } catch (Exception e) {
                logger.error("Init TrayManager Failed!",e);
            }
        }

        if (FileLoader.getConfigs().isPriceCheckerMode() && buffInstance == null) {
            try {
                buffInstance = new BuffTools();
            } catch (Exception e) {
                logger.error("Init BuffTools Failed!",e);
            }
        }

        if (!FileLoader.getConfigs().isDebug() && FileLoader.getConfigs().isCheckLogin()) {
            String name = LoginManager.getLoginManager().checkLogin();
            if (name == null) {
                logger.info("Account not login!");
                LoginManager.getLoginManager().requestLogin();
            } else {
                logger.info("Account Is Logging In!");
                logger.info("----------------------------------------");
                logger.info("Hello " + name + " !");
                logger.info("----------------------------------------");
            }
        }
    }

    /**
     * 启动所有Bot并启动管理线程 所有Bot将由管理线程管理
     */
    private void startManager() {
        if (FileLoader.getConfigs().isDebug()) {
            long time = System.currentTimeMillis();
            logger.info("--------------------------------------------------");
            logger.info("Debug Mode Enabled!!!");
            logger.info("Start Times : " + time);
            logger.info("--------------------------------------------------");
            test();
            logger.info("--------------------------------------------------");
            logger.info("Debug Done.");
            logger.info("End Times : " + System.currentTimeMillis() + " , Used Time : " + (System.currentTimeMillis() - time));
            logger.info("--------------------------------------------------");
            System.exit(0);
        }


        if (this.ManagerThread == null) {
            if (bots.size() == 0) {
                logger.error("You Not Set Any Bot Enabled! Please Set Enabled Bot First!");
                System.exit(99);
            }
            logger.info("Start All Bots!");
            this.ManagerThread = new Thread(new SteamCommunityManagerThread());
            this.ManagerThread.setName("Bot Manager Thread");
            this.ManagerThread.start();
        } else {
            logger.error("Due Start Request Founded! Thread Already Having!");
        }
    }

    private void startBuffTools() {
        buffInstance.startManager();
    }

    public void startApplication(){
        if (FileLoader.getConfigs().isPriceCheckerMode()) {
            this.startBuffTools();
        } else {
            this.startManager();
        }
    }

    /**
     * 测试分支 用于测试其他功能
     */
    private void test() {
        try {
            /*
            Connection connection = Jsoup.connect("http://localhost/test.php")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true);

            logger.info(connection.get().body().text());
            connection.response().cookies().forEach((k,v) -> {
                logger.info("Key : " + k + " , Value : " + v);
            });
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查物品是否满足要求 满足要求将根据配置文件来决定是否购买
     *
     * @param items 要进行检查的物品
     * @param bot   提交这个物品的Bot
     */
    public void checkItems(CSGOItems items, MarketBot bot) {
        /*
          别问为啥一个物品也要创建List来检查 懒得改了
         */
        List<CSGOItems> list = Stream.of(items)
                .filter((item) -> bot.getTarget().getMaxFloat() == -1.0F || item.getInfo().getFloatvalue() < bot.getTarget().getMaxFloat())
                .filter((item) -> bot.getTarget().getMinFloat() == -1.0F || item.getInfo().getFloatvalue() > bot.getTarget().getMinFloat())
                .filter((item) -> bot.getTarget().getMaxPrice() == -1.0F || item.getItem().getPrices() < bot.getTarget().getMaxPrice())
                .filter((item) -> bot.getTarget().getMinPrice() == -1.0F || item.getItem().getPrices() > bot.getTarget().getMinPrice())
                .filter((item) -> bot.getTarget().getHasStickers() == 0 || item.getInfo().getStickers().size() > bot.getTarget().getHasStickers())
                .filter((item) -> checkPaint(item, bot))
                .collect(Collectors.toList());

        /*
        List<CSGOItems> list = Stream.of(items)
                .filter((item) -> bot.getTarget().getMaxFloat() == -1.0F ? true : item.getInfo().getFloatvalue() < bot.getTarget().getMaxFloat())
                .filter((item) -> bot.getTarget().getMinFloat() == -1.0F ? true : item.getInfo().getFloatvalue() > bot.getTarget().getMinFloat())
                .filter((item) -> bot.getTarget().getMaxPrice() == -1.0F ? true : item.getItem().getPrices() < bot.getTarget().getMaxPrice())
                .filter((item) -> bot.getTarget().getMinPrice() == -1.0F ? true : item.getItem().getPrices() > bot.getTarget().getMinPrice())
                .filter((item) -> bot.getTarget().getHasStickers() == 0 ? true : item.getInfo().getStickers().size() > bot.getTarget().getHasStickers())
                .filter((item) -> checkPaint(item, bot))
                .collect(Collectors.toList());
        */

        if (!list.isEmpty()) {
            logger.info("Find Items : ");
            printItems(items);

            if (bot.getTarget().isAutoBuy()) {
                buyItems(items);
            } else {
                trayManager.tipPlayerBuy(items);
            }

        }

    }

    /**
     * 检查物品的模板是否满足要求
     *
     * @param item 要检查的物品
     * @param bot  提交任务的bot
     * @return 是否满足要求
     */
    private boolean checkPaint(CSGOItems item, MarketBot bot) {
        //TODO 根据特定印花购买武器
        if (bot.getTarget().getPaintSeed().length == 0
                | (bot.getTarget().getPaintSeed().length == 1 && bot.getTarget().getPaintSeed()[0] == 0)) {
            return true;
        }

        for (int i : bot.getTarget().getPaintSeed()) {
            if (item.getInfo().getPaintseed() == i) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将物品的信息输出到控制台
     *
     * @param item 要输出的物品
     */
    public static void printItems(CSGOItems item) {
        logger.info("----------------------------------------");
        logger.info("Item : " + item.getInfo().getFull_item_name());
        logger.info("ID : " + item.getItem().getId());
        logger.info("Price : " + item.getItem().getPrices());
        logger.info("Float : " + item.getInfo().getFloatvalue());
        logger.info("Paint : " + item.getInfo().getPaintseed());
        if (!item.getInfo().getStickers().isEmpty())
            logger.info("Stickers : " + item.getInfo().getStickers());
        logger.info("----------------------------------------");
    }

    /**
     * 购买指定的物品
     *
     * @param item 要购买的物品
     */
    public void buyItems(CSGOItems item) {
        synchronized (buyLock) {
            if (item.isDone() && !item.isBuy()) {

                if (FileLoader.getConfigs().getSessionID().equals("SessionID")
                        | FileLoader.getConfigs().getSteamLoginSecure().equals("SteamLoginSecure")) {
                    logger.error("SessionID Or SteamLoginSecure Not Set! Please Set ID First!");
                    System.exit(99);
                }

                logger.info("Try Buy Item : " + item.getItem().getId());
                String Buy_ID = item.getItem().getListingid();
                String SessionID = FileLoader.getConfigs().getSessionID();
                String SteamLoginSecure = FileLoader.getConfigs().getSteamLoginSecure();
                String SteamRememberLogin = FileLoader.getConfigs().getSteamRememberLogin();
                String webTradeEligibility = URLEncoder.encode("{\"allowed\":1,\"allowed_at_time\":0,\"steamguard_required_days\":15,\"new_device_cooldown_days\":7,\"time_checked\":" + System.currentTimeMillis() / 1000 + "}", StandardCharsets.UTF_8);
                Map<String, String> cookie = new HashMap<>();
                Map<String, String> data = new HashMap<>();
                Map<String, String> headers = new HashMap<>();

                String url = "https://" + FileLoader.getConfigs().getLink() + "/market/buylisting/" + Buy_ID;
                logger.info("Url : " + url);
                Connection connection = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .method(Connection.Method.POST)
                        .ignoreHttpErrors(true)
                        //.followRedirects(true)
                        .timeout(10000);

                headers.put("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
                headers.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
                headers.put("Accept","*/*");
                headers.put("Host", "steamcommunity.com");
                headers.put("Referer","https://steamcommunity.com/market/listings/730/" + URLEncoder.encode(item.getInfo().getFull_item_name(),StandardCharsets.UTF_8));

/*
                if (!FileLoader.getConfigs().getBasic_Auth().equals("NONE")) {
                    String pass = Base64.getEncoder().encodeToString(FileLoader.getConfigs().getBasic_Auth().getBytes());
                    headers.put("Authorization","Basic " + pass);
                }
*/
                cookie.put("sessionid", SessionID);
                cookie.put("steamLoginSecure", SteamLoginSecure);
                cookie.put("webTradeEligibility", webTradeEligibility);
                cookie.put("steamRememberLogin",SteamRememberLogin);
                //cookie.put("steamMachineAuth76561198204046867","FBD7044B07339C1462FABC484E56FF60FCD88058");

                FileLoader.getConfigs().getSteamMachineAuth().forEach(cookie::put);

                data.put("sessionid", SessionID);
                data.put("currency", "23");
                data.put("subtotal", String.valueOf(item.getItem().getConverted_price_per_unit()));
                data.put("fee", String.valueOf(item.getItem().getConverted_fee_per_unit()));
                data.put("total", String.valueOf(item.getItem().getConverted_fee_per_unit() + item.getItem().getConverted_price_per_unit()));
                data.put("quantity", "1");
                data.put("save_my_address", "0");
                data.put("billing_state", "");

                //connection.referrer("https://steamcommunity.com/market/listings/730/" + item.getInfo().getFull_item_name());
/*
                headers.forEach((k,v) -> {
                    logger.info("Header : " + k + " : " + v);
                });

                cookie.forEach((k,v) -> {
                    logger.info("Cookie : " + k + " : " + v);
                });

                data.forEach((k,v) -> {
                    logger.info("Data : " + k + " : " + v);
                });
*/
                connection.headers(headers);
                connection.cookies(cookie);
                connection.data(data);


                Document document;
                try {
                    document = connection.post();

                    connection.response().cookies().forEach((k,v) -> {
                        if (k.contains("steamMachineAuth")) {
                            FileLoader.getConfigs().getSteamMachineAuth().put(k, v);
                            FileLoader.saveConfig();
                        }
                    });

                    if (document != null) {
                        logger.info("Debug Buy msg : " + document.body().text());
                            JsonElement element = JsonParser.parseString(document.body().text());
                            JsonElement message = element.getAsJsonObject().get("message");

                            if (message != null) {
                                String responses = message.getAsString();
                                logger.info("Server Response : " + responses);
                                if (responses.contains("直到前一个操作完成之前，您不能购买任何物品")) {
                                    logger.error("Buy So Fast!!! Wait 30 seconds.");
                                    try {
                                        Thread.sleep(30000);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                logger.info("Buy Success!");
                                logger.info("Item Float : " + item.getInfo().getFloatvalue());
                                logger.info("Item Price : " + item.getItem().getPrices());
                                float balance = 0.0F;
                                try {
                                    balance = Integer.parseInt(element.getAsJsonObject().get("wallet_info").getAsJsonObject().get("wallet_balance").getAsString()) / 100.0F;
                                } catch (Exception e) {
                                    try {
                                        balance = Integer.parseInt(element.getAsJsonObject().get("wallet_balance").getAsString()) / 100.0F;
                                    } catch (Exception ignored) {}
                                }
                                logger.info("Wallet Balance Left : " + (balance == 0.0F ? "Unknown" : balance));

                                if (balance < 10.00) {
                                    logger.info("Low Balance!!!!!!");
                                }

                                goods.add(item);
                                FileLoader.writeGoods(goods);
                                item.setBuy(true);
                                trayManager.trayPostMessage("MarketBot","Buy Item Success! \r\n Item : " + item.getInfo().getFull_item_name() + " \r\n Price : " + item.getItem().getPrices(), TrayIcon.MessageType.INFO);

                            }

                        //logger.info(document.body().text());
                    } else {
                        logger.info("Post Failed!");
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                    logger.error("Buy Failed! Wait Seconds And Retry Buy Items...",e);
                    try {
                        Thread.sleep(10000);
                    } catch (Exception e1) {
                        e.printStackTrace();
                    }
                    buyItems(item);
                }
            }
        }
    }

    /*
    public void getAllFloats(List<CSGOItems> list) {
        synchronized (this) {
            int count = 0;
            for (CSGOItems items : list) {
                if (!items.isDone()) {
                    getFloats(items);
                    logger.info("Start Get Float : " + items);
                    count++;
                }
            }

            logger.info("Float Mission Count : " + count);

            while (executor.getActiveCount() != 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            executor.shutdown();
            logger.info("Get Float Done!");
        }
    }
     */

    /**
     * 获取实例
     *
     * @return 实例
     */
    public static MarketTools getInstance() {
            if (instance == null)
                instance = new MarketTools();
            return instance;
    }

    /**
     * 获取线程池实例
     *
     * @return 线程池
     */
    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    /**
     * 获取BuffTools
     * @throws NullPointerException 如果未启动BuffTools 获取将会返回null
     * @return BuffTools实例
     */
    public static BuffTools getBuffTools() {
        return buffInstance;
    }

    /**
     * 获取所有的Bot列表
     *
     * @return Bot列表
     */
    public Map<String, MarketBot> getBots() {
        return bots;
    }
}
