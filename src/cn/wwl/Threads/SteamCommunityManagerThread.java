package cn.wwl.Threads;

import cn.wwl.FileLoader;
import cn.wwl.MarketBot;
import cn.wwl.MarketTools;
import cn.wwl.Beans.FileBean.ConfigFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 管理线程 用于管理所有Bot 至于为什么不在主线程工作嘛..为了移植手机更加方便(咕咕咕)
 */
public class SteamCommunityManagerThread implements Runnable {

    /**
     * 上次执行的时间 用于进行延时
     */
    private long startTime = 0;

    private final Random random = new Random();
    private static final Logger logger = LogManager.getLogger(SteamCommunityManagerThread.class);


    /**
     * 将要使用的Bot 下次获取时将使用该Bot
     */
    private int UsedBot;

    /**
     * 页面目标 管理线程将抓取所有Bot的当前页面
     */
    private int page;

    /**
     * Random模式下已检查次数
     */
    private int checkCounts;

    /**
     * 开始线程 抓取的方式是一个类似Switch的模式 准备开发当使用Proxy时使用代理直接抓取
     */
    @Override
    public void run() {
        //List<MarketBot> botList = new ArrayList<>(MarketTools.getInstance().getBots().values());

        while (!Thread.currentThread().isInterrupted()) {
            List<MarketBot> botList = new ArrayList<>(MarketTools.getInstance().getBots().values());
            MarketBot bot = botList.get(this.UsedBot);
            if (bot.getTarget().isEnabled()) {
                //logger.info("UsedBot : " + this.UsedBot + " List : " + botList.size());
                if (bot.getMarketItems(this.page)) {
                    this.waitDelay();
                }
                //this.updateCount(botList);
            } else {
                //logger.info("Bot : " + bot.getTarget().getBotName() + " , Force Add Before : " + this.UsedBot);
                //this.UsedBot = (this.UsedBot >= botList.size() - 1) ? 0 : this.UsedBot + 1;
                //logger.info("After : " + this.UsedBot);
                if (FileLoader.getConfigs().getScanMode() == ConfigFile.ScanMode.Random)
                this.checkCounts--;
            }
            this.updateCount(botList);
        }
        logger.info("Oops. Run Done.");
    }

    /**
     * 计数器更新 更新下一个目标
     * @param list 上面的List实例
     */
    private void updateCount(List<MarketBot> list) {
        switch (FileLoader.getConfigs().getScanMode()) {
            case Random: {
                if (this.checkCounts >= FileLoader.getConfigs().getRandomCount()) {
                    this.checkCounts = 0;
                    MarketTools.getInstance().getBots().values().forEach(FileLoader::saveCache);
                    MarketTools.getInstance().getBots().values().forEach((bot) -> bot.getList().clear());
                    logger.info("Reset Page Count && GC System!");
                    System.gc();
                    return;
                }
                //list = list.stream().filter((b) -> b.getTarget().isEnabled()).collect(Collectors.toList());

                //list.forEach((b) -> logger.info(b.getTarget().getBotName()));

                this.UsedBot = (this.UsedBot >= list.size() - 1) ? 0 : this.UsedBot + 1;
                //this.UsedBot = random.nextInt(list.size() - 1);
                MarketBot bot = list.get(this.UsedBot);
                this.page = (bot.getTotalItemCount() / 100 == 0) ? 0 : random.nextInt(bot.getTotalItemCount() / 100);
                //logger.info("Weapon : " + bot.getTarget().getBotName() + " , CheckCount : " + this.checkCounts + " , RandomCount : " + FileLoader.getConfigs().getRandomCount() + " , TotalCount : " + bot.getTotalItemCount() + ", Random : " + this.page);
                this.checkCounts++;
                logger.info("CheckCount : " + this.checkCounts);
                break;
            }

            case Switch: {
                int doneCount = 0;
                for (MarketBot bot : list) {
                    if (bot.done) {
                        doneCount++;
                    }
                }
                //System.out.println("DoneCount : " + doneCount + " , Size : " + list.size());
                if (doneCount == list.size()) {
                    this.page = 0;
                    MarketTools.getInstance().getBots().values().forEach(FileLoader::saveCache);
                    MarketTools.getInstance().getBots().values().forEach((bot) -> bot.getList().clear());
                    logger.info("Reset Page Count && GC System!");
                    System.gc();
                }
                //System.out.println("Count : " + count + " , Size : " + (list.size() - 1));
                if (this.UsedBot >= list.size() - 1) {
                    //System.out.println("Reset Count");
                    this.UsedBot = 0;
                    this.page++;
                } else {
                    this.UsedBot++;
                }
                break;
            }
            default: {
                logger.error("Unknown Mode!");
                System.exit(99);
                break;
            }
        }
    }

    /**
     * 让系统休眠 避免速度过快被V社屏蔽
     */
    public void waitDelay() {
        if (FileLoader.getConfigs().isUseProxy()) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
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
}
