package cn.wwl.Threads;

import cn.wwl.MarketBot;
import cn.wwl.MarketTools;
import cn.wwl.Beans.MarketBean.CSGOItems;
import cn.wwl.Beans.MarketBean.ItemInfo;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * 用于获取磨损的线程
 */
public class FloatMakeThread implements Runnable {

    private static final Logger logger = LogManager.getLogger(FloatMakeThread.class);

    /**
     * 目标物品
     */
    public CSGOItems item;

    /**
     * 申请任务的Bot
     */
    public MarketBot bot;

    /**
     * 重试次数
     */
    private int count;

    /**
     * 新建一个抓取磨损的实例
     * @param item 要求抓取磨损的饰品
     * @param bot 申请任务的Bot
     */
    public FloatMakeThread(CSGOItems item, MarketBot bot) {
        this.item = item;
        this.bot = bot;
    }

    /**
     * 在CSGOFloats通过检视链接获取详细信息 获取完成后将任务提交向后端检测是否进行购买
     */
    @Override
    public void run() {
        try {
            Document document = Jsoup.connect("https://api.csgofloat.com/?url=" + item.getItem().getInspectLink())
                    .timeout(5000)
                    .ignoreContentType(true)
                    .get();

            //logger.info(document.body().text());

            JsonObject object = JsonParser.parseString(document.body().text()).getAsJsonObject();
            JsonElement element = object.get("error");

            if (element != null) {
                logger.error("Get Float Failed! " + element.getAsString());
                return;
            }

            ItemInfo info = MarketTools.gson.fromJson(object.get("iteminfo"),ItemInfo.class);
            item.setInfo(info);
            item.setDone(true);

            //FileLoader.saveCache(bot);
            MarketTools.getInstance().checkItems(item,bot);
        } catch (Exception e) {
            //e.printStackTrace();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            if (count++ < 5) {
                bot.getFloats(item);
            } else {
                logger.warn("Get Float " + item + " Retry 5 Failed!");
            }
        }
    }
}
