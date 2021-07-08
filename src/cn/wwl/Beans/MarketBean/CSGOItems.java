package cn.wwl.Beans.MarketBean;

/**
 * CSGO饰品的实例
 */
public class CSGOItems {
    private MarketItems item;
    private ItemInfo info;
    private boolean done;
    private boolean buy;

    public ItemInfo getInfo() {
        return info;
    }

    public void setInfo(ItemInfo info) {
        this.info = info;
    }

    public MarketItems getItem() {
        return item;
    }

    public void setItem(MarketItems items) {
        this.item = items;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isBuy() {
        return buy;
    }

    public void setBuy(boolean buy) {
        this.buy = buy;
    }

    @Override
    public String toString() {
        return "CSGOItems{" +
                "item=" + item +
                ", info=" + info +
                ", done=" + done +
                ", buy=" + buy +
                '}';
    }

    /**
     * 测试方法 生成一个测试用的物品
     */
    public static CSGOItems getExample() {
        CSGOItems items = new CSGOItems();
        ItemInfo itemInfo = new ItemInfo();
        MarketItems item = new MarketItems();
        item.setAppid(730);
        item.setPrice(50);
        item.setPrices(50);
        itemInfo.setFloatvalue(0.3707220);
        itemInfo.setItem_name("WDNMD");
        itemInfo.setFull_item_name("FN57 | WDNMD");
        itemInfo.setPaintseed(233);
        itemInfo.setPaintindex(233);
        items.setInfo(itemInfo);
        items.setItem(item);
        items.done = true;
        return items;
    }
}
