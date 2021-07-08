package cn.wwl.Buff.Thread;

import cn.wwl.Buff.Beans.BuffMarketItem;
import cn.wwl.Buff.BuffTools;
import cn.wwl.FileLoader;
import cn.wwl.MarketTools;
import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 管理线程
 */
public class BuffItemManagerThread implements Runnable {

    private static final Logger logger = LogManager.getLogger(BuffItemManagerThread.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private ThreadPoolExecutor executor;

    private int Count;
    private long startTime;
    private static final Random random = new Random();

    public BuffItemManagerThread() {
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(FileLoader.getConfigs().getMaxThreads());

    }

    @Override
    public void run() {
        logger.info("Buff Item Manager Thread Started.");
        List<BuffMarketItem> list = FileLoader.getBuffID(true)
                .stream()
                .filter((i) -> i.getType().contains("步枪")).collect(Collectors.toList());
        StringBuilder builder = new StringBuilder().append("Count,itemEnglish,itemChinese,price_Steam,price_Buff,Percentage").append("\r\n");

        if (list.isEmpty()) {
            logger.info("Item ID List Is Empty! Fetching ID!");
            MarketTools.getBuffTools().FetchItemID();
        }

        for (BuffMarketItem item : list) {
            BuffTools.MarketItemResponse response = null;
            builder.append(Count).append(",").append(item.getEnglish_Name()).append(",").append(item.getChinese_Name()).append(",");

            try {
                logger.info("Start Get Item " + item.getChinese_Name() + " , Count : " + Count++ + " / " + list.size());
                 response = MarketTools.getBuffTools().fetchItemStatus(item);
            } catch (Exception e) {
                try {
                logger.error("Get Item " + item.getChinese_Name() + " #1 Failed! Delay 5s");
                Thread.sleep(5000);
                response = MarketTools.getBuffTools().fetchItemStatus(item);
                } catch (Exception e2) {
                    logger.error("Get Item " + item.getChinese_Name() + " Failed!",e2);
                }
            }

            if (response != null) {
                if (response.getPrice() == null || response.getPrice_Steam() == null) {
                    builder.append("无货").append(",").append("无货").append(",").append("Unknown");
                } else {
                    float percent = 0;
                    try {
                        percent = (float) (Double.parseDouble(response.getPrice_Steam()) / Double.parseDouble(response.getPrice()));
                    } catch (Exception e) {
                        logger.error("Transform Percent Failed!", e);
                        logger.error(response);
                    }
                    builder.append(response.getPrice_Steam()).append(",").append(response.getPrice()).append(",").append(percent == 0 ? "Unknown" : percent);
                }
            } else {
                builder.append("Unknown,Unknown,Unknown");
            }
            builder.append("\r\n");
            FileLoader.writeBuffPrice(builder.toString().trim());
            waitDelay();
        }


    }

    public void waitDelay() {
        startTime = System.currentTimeMillis();
        long count = 2500 + random.nextInt(1000);
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
