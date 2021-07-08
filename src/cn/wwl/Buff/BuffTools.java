package cn.wwl.Buff;

import cn.wwl.Buff.Beans.BuffMarketItem;
import cn.wwl.Buff.Thread.BuffItemManagerThread;
import cn.wwl.Buff.Thread.ItemIDFetchThread;
import cn.wwl.FileLoader;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class BuffTools {
    private static final Logger logger = LogManager.getLogger(BuffTools.class);
    /**
     * 单实例锁
     */
    private static boolean inited;

    /**
     * 管理线程
     */
    private Thread managerThread;
    /**
     * BuffTools初始化方法
     * @throws Exception 如果已经被初始化过
     */
    public BuffTools() throws Exception {
        if (inited)
            throw new Exception("Only Can Have Single BuffTools");

        this.preInit();
    }

    private void preInit() {
        logger.info("BuffTools Inited.");
        if (FileLoader.getBuffID(true).isEmpty()) {
            FetchItemID();
        }
        //Nothing To Do...LOL
    }

    /* TODO
       根据Items记录下所有item ID 保存到表 动态读取
       https://buff.163.com/api/market/goods?game=csgo&page_num=1&category=weapon_knife_survival_bowie
     */

    public void startManager() {
        if (this.managerThread == null) {
            this.managerThread = new Thread(new BuffItemManagerThread());
            this.managerThread.setName("BuffItem Manager Thread");
            this.managerThread.start();
        } else {
            logger.error("Due Start Request Founded! Thread Already Having!");
        }
    }

    /**
     * 启动获取ID的线程
     */
    public void FetchItemID() {
        logger.info("Start Fetch ItemID...");
        Thread thread = new Thread(new ItemIDFetchThread());
        thread.setName("Item ID Fetch Thread");
        thread.start();
        while (thread.isAlive()) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public MarketItemResponse fetchItemStatus(BuffMarketItem item) throws IOException {
        MarketItemResponse response = new MarketItemResponse()
                .setItem(item)
                .setStatus(false);

        Connection connection = Jsoup.connect("https://buff.163.com/api/market/goods/sell_order?game=csgo&page_num=1&goods_id=" + item.getId())
                .ignoreContentType(true)
                .ignoreHttpErrors(true);

        Document document = connection.get();

        if (document != null) {
            JsonObject object = null;

            try {
                object = JsonParser.parseString(document.body().text()).getAsJsonObject();
            } catch (Exception e) {
                logger.error("Parse JSON Failed!", e);
                String text = document.body().text();
                logger.error(text);
                if (text.contains("429")) {
                    logger.error("429 So Fast.Sleep.");
                    try {
                        Thread.sleep(10000);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }

            if (object != null) {

                if (object.get("code").getAsString().equalsIgnoreCase("ok")) {

                    JsonObject data = object.get("data").getAsJsonObject();
                    JsonObject goods = data.get("goods_infos").getAsJsonObject();

                    if (goods.keySet().size() != 0) {
                        for (String st : goods.keySet()) {
                            JsonObject info = goods.get(st).getAsJsonObject();
                            response.setPrice_Steam(info.get("steam_price_cny").getAsString());
                            break;
                        }
                    }

                    JsonArray items = data.get("items").getAsJsonArray();

                    for (JsonElement element : items) {
                        JsonObject itemobj = element.getAsJsonObject();
                        response.setPrice(itemobj.get("price").getAsString());
                        break;
                    }
                    response.setStatus(true);
                    return response;
                }
            }
        }
        return response;
    }

    public class MarketItemResponse {
        private BuffMarketItem item;
        private boolean status;
        private String price;
        private String price_Steam;

        public BuffMarketItem getItem() {
            return item;
        }

        public String getPrice() {
            return price;
        }

        public boolean isDone() {
            return status;
        }

        public String getPrice_Steam() {
            return price_Steam;
        }

        public MarketItemResponse setItem(BuffMarketItem item) {
            this.item = item;
            return this;
        }

        public MarketItemResponse setPrice(String price) {
            this.price = price;
            return this;
        }

        public MarketItemResponse setStatus(boolean status) {
            this.status = status;
            return this;
        }

        public MarketItemResponse setPrice_Steam(String price_Steam) {
            this.price_Steam = price_Steam;
            return this;
        }

        @Override
        public String toString() {
            return "MarketItemResponse{" +
                    "item=" + item +
                    ", status=" + status +
                    ", price='" + price + '\'' +
                    ", price_Steam='" + price_Steam + '\'' +
                    '}';
        }
    }
}
