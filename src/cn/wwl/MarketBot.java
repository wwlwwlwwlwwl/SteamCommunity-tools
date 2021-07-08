package cn.wwl;

import cn.wwl.Threads.FloatMakeThread;
import cn.wwl.Beans.FileBean.ConfigFile;
import cn.wwl.Beans.MarketBean.CSGOItems;
import cn.wwl.Beans.MarketBean.MarketItems;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.Proxy;
import java.util.*;


/**
 * 这波啊 这波是机器人 对应config
 */
public class MarketBot {
    /**用于区分机器人 这个是机器人的目标 */
    private final ConfigFile.Bot target;
    /** 机器人物品缓存 */
    private List<CSGOItems> list = new ArrayList<>();

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final Logger logger = LogManager.getLogger(MarketBot.class);

    /**重试计数器 */
    private int retry;
    /***
     * 当前饰品的总数 当页面目标高于该数值时将标记该机器人为完成 数值将会在获取时更新
     */
    private int total_count;
    /** 用于标记该机器人是否已经完成 当页面的page*100 > <code>total_count</code>时更新 */
    public boolean done;

    /**
     * 初始化Bot的方式
     * @param target 为该机器人的目标
     */
    public MarketBot(ConfigFile.Bot target) {
        this.target = target;
    }

    /**
     * 设置的Proxy 还在测试
     */
    public Proxy proxy;

    /**
     * 获取市场的饰品页 并且解析装入<code>list</code> 中
     * @param page 目标页面
     * @return 是否获取成功 返回<code>true</code>为成功
     */
    public boolean getMarketItems(int page) {
        try {

            //TODO Cache Mode
            if (FileLoader.getConfigs().isDebug()) {
                if (page == 0 && !list.isEmpty()) {
                    logger.info("Find " + list.size() + " Item From Cache. Use Cache Data...");
                    for (CSGOItems item : list) {
                        MarketTools.getInstance().checkItems(item, this);
                    }
                    return true;
                }
            }

            if (page * 100 < total_count || this.total_count == 0) {
                logger.info("Bot " + this.target.getBotName() + " Start Get Page " + page + " ...");
                Map<String,String> Headers = new HashMap<>();
                Connection connection = Jsoup.connect("https://" + FileLoader.getConfigs().getLink() + "/market/listings/730/" + target.getItemsName() + "/render/?query=&start=" + page * 100 + "&count=100&country=CN&language=schinese&currency=23")
                        .timeout(10000)
                        .ignoreContentType(true);

                if (!FileLoader.getConfigs().getBasic_Auth().equals("NONE")) {
                    String pass = Base64.getEncoder().encodeToString(FileLoader.getConfigs().getBasic_Auth().getBytes());
                    Headers.put("Authorization","Basic " + pass);
                }

                if (!Headers.isEmpty()) {
                    connection.headers(Headers);
                }

                if (FileLoader.getConfigs().isUseProxy()) {
                    this.proxy = FileLoader.getRandomProxy();
                    connection.proxy(proxy);
                    connection.timeout(4000);
                }

                Document document = null;
                try {
                    document = connection.get();
                } catch (HttpStatusException e) {
                    if (e.getStatusCode() == 429) {
                        logger.warn("Warning > Response == 429! Thread Will Sleep!");
                        try {
                            Thread.sleep(30000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        return false;
                    }
                }

                connection.response().cookies().forEach((k,v) -> {
                    if (k.contains("steamMachineAuth")) {
                        FileLoader.getConfigs().getSteamMachineAuth().put(k, v);
                        FileLoader.saveConfig();
                    }
                });

                if (document == null) {
                    logger.error("Document == null!");
                    System.exit(99);
                }

                //logger.info(document.body().text());

                JsonObject object = JsonParser.parseString(document.body().text()).getAsJsonObject();

                if (object.get("success").getAsBoolean()) {
                    JsonObject lists = object.get("listinginfo").getAsJsonObject();
                    int count = 0;
                    this.total_count = object.get("total_count").getAsInt();
                    for (String s : lists.keySet()) {
                        JsonObject obj = JsonParser.parseString(lists.get(s).toString()).getAsJsonObject();
                        String listingid = obj.get("listingid").getAsString();

                        int price = obj.get("price").getAsInt();
                        int fee = obj.get("fee").getAsInt();
                        if (price != 0 && fee != 0) {
                            MarketItems items = gson.fromJson(obj.get("asset"), MarketItems.class);
                            items.setListingid(listingid);
                            String link = obj.get("asset").getAsJsonObject()
                                    .get("market_actions").getAsJsonArray()
                                    .get(0).getAsJsonObject().get("link").getAsString();

                            items.setInspectLink(link.replaceAll("%listingid%", items.getListingid()).replaceAll("%assetid%", items.getId()));

                            items.setPrice(price);
                            items.setFee(fee);
                            items.setConverted_fee_per_unit(obj.get("converted_fee_per_unit").getAsInt());
                            items.setConverted_price_per_unit(obj.get("converted_price_per_unit").getAsInt());

                            items.setPrices(items.getConverted_price_per_unit() + items.getConverted_fee_per_unit());

                            //System.out.println(items.getPrices());
                            CSGOItems csgoitem = new CSGOItems();
                            csgoitem.setItem(items);
                            //logger.info(csgoitem);
                            list.add(csgoitem);
                            getFloats(csgoitem);
                            count++;
                        }
                    }
                    //logger.info("Count : " + count + " Get Page Done.");
                    retry = 0;
                    return true;
                    //FileLoader.saveCache(this);
                } else {
                    logger.info("Success != True!");
                }
            }
            this.done = true;
            return false;
        } catch (Exception e) {
            //e.printStackTrace();
            if (!FileLoader.getConfigs().isUseProxy()) {
                if (retry < 3) {
                    logger.info("Bot " + target.getBotName() + " Get Page " + page + " Failed! Retry #" + retry++ + " , Delay 10 sec.",e);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    getMarketItems(page);
                } else {
                    logger.error("Bot " + target.getBotName() + " Get Page " + page + " Failed! Retry #3 Failed! Mission Canceled.");
                    logger.info("Thread Will Sleep 30 Sec...");
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            } else {
                getMarketItems(page);
            }
            return false;
        }
    }

    /**
     * 将任务提交到获取磨损的任务列表中 获取成功后将磨损装载入对象中
     * @param item 获取磨损的目标
     */
    public void getFloats(CSGOItems item) {
        if (!item.isDone())
            MarketTools.getInstance().getExecutor().execute(new FloatMakeThread(item,this));
    }

    /*
    public String getItemName() {
        if (this.target != null) {
            try {
                return URLEncoder.encode(this.target.getItemsName(), "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
                return this.target.getItemsName();
            }
        }
        return null;
    }
*/

    public ConfigFile.Bot getTarget() {
        return target;
    }

    public List<CSGOItems> getList() {
        return list;
    }

    public void setList(List<CSGOItems> list) {
        this.list = list;
    }

    public int getTotalItemCount() {
        return total_count;
    }
}
