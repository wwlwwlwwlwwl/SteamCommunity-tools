package cn.wwl.Buff.Thread;

import cn.wwl.Buff.Beans.BuffMarketItem;
import cn.wwl.Buff.BuffItems;
import cn.wwl.FileLoader;
import cn.wwl.MarketTools;
import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 用于处理如果BuffID表为空时的方法
 * 配置文件乱死啦 艹 崩溃了 把ID硬编里面吧
 */
public class ItemIDFetchThread implements Runnable {

    private static final String locale_supported = "zh-Hans";
    private static final String game = "csgo";

    private long startTime = 0;
    private static final Logger logger = LogManager.getLogger(ItemIDFetchThread.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private final Random random = new Random();

    private Map<String,String> header;

    private Map<String,String> cookie;

    private int tryCount;


    private List<BuffMarketItem> list;

    public ItemIDFetchThread() {
        FileLoader.loadBuffConfigs();
        header = Map.of(
                "Accept","application/json, text/javascript, */*; q=0.01",
                "X-Requested-With","XMLHttpRequest",
                "Accept-Language","zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"
        );

        cookie = Map.of(
                "csrf_token",FileLoader.getBuffConfig().getCsrf_token(),
                "Device-Id",FileLoader.getBuffConfig().getDevice_Id(),
                "remember_me",FileLoader.getBuffConfig().getRemember_me(),
                "session",FileLoader.getBuffConfig().getSession()
        );
    }

    @Override
    public void run() {
        list = FileLoader.getBuffID(false);

        for (BuffItems value : BuffItems.values()) {
            int maxPage = Integer.MAX_VALUE;
            for (int i = 1; i <= maxPage; i++) {
                String url = "https://buff.163.com/api/market/goods?game=csgo&page_num=" + i + "&category=" + value.getItemName();
                Connection connection = Jsoup.connect(url)
                        .ignoreHttpErrors(true)
                        .ignoreContentType(true)
                        .timeout(10000)
                        .headers(header)
                        .cookies(cookie);

                Document document = null;
                Connection.Response response = null;

                try {
                    document = connection.get();
                    response = connection.response();
                } catch (Exception e) {
                    logger.error("Get Failed! Sleep 10s",e);
                    for (tryCount = 0; tryCount < 5;tryCount++) {
                        try {
                            Thread.sleep(10000);
                            document = connection.get();
                            response = connection.response();
                            break;
                        } catch (Exception e2) {
                            logger.error("Get Failed #" + i + " , Sleep 10s",e2);
                        }
                    }
                }

                logger.info("Start Get " + value.getChineseName() + " , Response : " + response.statusCode() + " , Current Page : " + i + " , Max Page : " + (maxPage == Integer.MAX_VALUE ? "Unknown" : maxPage));

                response.cookies().forEach((k,v) -> {
                    if (k.equalsIgnoreCase("csrf_token") && !v.isEmpty() && !v.equalsIgnoreCase(FileLoader.getBuffConfig().getCsrf_token())) {
                        logger.info("Update csrf_token " + FileLoader.getBuffConfig().getCsrf_token() + " To " + v);
                        FileLoader.getBuffConfig().setCsrf_token(v);
                    }

                    if (k.equalsIgnoreCase("session") && !v.isEmpty() && !v.equalsIgnoreCase(FileLoader.getBuffConfig().getSession())) {
                        logger.info("Update session " + FileLoader.getBuffConfig().getSession() + " To " + v);
                        FileLoader.getBuffConfig().setSession(v);
                    }

                    if (k.equalsIgnoreCase("Device-Id") && !v.isEmpty() && !v.equalsIgnoreCase(FileLoader.getBuffConfig().getDevice_Id())) {
                        logger.info("Update Device-ID " + FileLoader.getBuffConfig().getDevice_Id() + " To " + v);
                        FileLoader.getBuffConfig().setDevice_Id(v);
                    }

                    if (k.equalsIgnoreCase("remember_me") && !v.isEmpty() && !v.equalsIgnoreCase(FileLoader.getBuffConfig().getRemember_me())) {
                        logger.info("Update remember_me " + FileLoader.getBuffConfig().getRemember_me() + " To " + v);
                        FileLoader.getBuffConfig().setRemember_me(v);
                    }
                });

                FileLoader.saveBuffConfigs();

                JsonObject object = JsonParser.parseString(document.body().text()).getAsJsonObject();
                //logger.info(document.body().text());
                if (object.get("code").getAsString().equalsIgnoreCase("ok") && object.get("msg").isJsonNull()) {

                    JsonObject data = object.get("data").getAsJsonObject();
                    maxPage = data.get("total_page").getAsInt();

                    for (JsonElement elements : data.get("items").getAsJsonArray()) {
                        JsonObject obj = elements.getAsJsonObject();
                        JsonObject tags = elements.getAsJsonObject()
                                .get("goods_info").getAsJsonObject()
                                .get("info").getAsJsonObject()
                                .get("tags").getAsJsonObject();

                        BuffMarketItem buffMarketItem = new BuffMarketItem();

                        buffMarketItem
                                .setId(obj.get("id").getAsLong())
                                .setEnglish_Name(obj.get("market_hash_name").getAsString())
                                .setChinese_Name(obj.get("name").getAsString())
                                .setSteam_URL(obj.get("steam_market_url").getAsString());

                        tags.entrySet().forEach((e) -> {
                            try {
                                buffMarketItem.setValue(e.getKey(),e.getValue().getAsJsonObject().get("localized_name").getAsString());
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        });

                        if (!list.contains(buffMarketItem)) {
                            //logger.info("Add Item " + buffMarketItem);
                            list.add(buffMarketItem);
                        }

                        FileLoader.saveBuffID(list);
                    }

                } else {
                    if (object.get("msg") != null) {
                        logger.error("Response msg : " + object.get("msg").getAsString());
                    }
                    logger.error("Response Code != OK!");
                }

                waitDelay();
            }
        }
        logger.info("Fetching Item ID Done.");
    }


    public void waitDelay() {
            startTime = System.currentTimeMillis();
            long count = FileLoader.getConfigs().getBasicDelay() + random.nextInt(FileLoader.getConfigs().getRandomDelay());
            logger.info("System Will Sleep " + count + " ms..");
            while (MarketTools.pause || System.currentTimeMillis() - startTime < (count)) {
                try {
                    Thread.currentThread().sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
}
