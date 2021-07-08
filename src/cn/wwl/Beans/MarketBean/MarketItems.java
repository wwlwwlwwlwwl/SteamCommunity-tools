package cn.wwl.Beans.MarketBean;

/**
 * 在市场上的饰品
 */
public class MarketItems {
    private int appid;
    private int price;
    private int fee;
    private String id;
    private String amount;

    private int converted_price_per_unit; //Subtotal
    private int converted_fee_per_unit; //fee


    private String listingid;
    private String inspectLink;
    private int prices;

    public void setAppid(int appid) {
        this.appid = appid;
    }

    public int getAppid() {
        return appid;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAmount() {
        return amount;
    }

    public String getListingid() {
        return listingid;
    }

    public void setListingid(String listingid) {
        this.listingid = listingid;
    }

    public String getInspectLink() {
        return inspectLink;
    }

    public void setInspectLink(String inspectLink) {
        this.inspectLink = inspectLink;
    }

    public int getConverted_fee_per_unit() {
        return converted_fee_per_unit;
    }

    public int getConverted_price_per_unit() {
        return converted_price_per_unit;
    }

    public void setConverted_fee_per_unit(int converted_fee_per_unit) {
        this.converted_fee_per_unit = converted_fee_per_unit;
    }

    public void setConverted_price_per_unit(int converted_price_per_unit) {
        this.converted_price_per_unit = converted_price_per_unit;
    }

    public int getFee() {
        return fee;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setFee(int fee) {
        this.fee = fee;
    }

    /**
     * 获取计算完成后的价格
     * @return 计算后的价格
     */
    public float getPrices() {
        return prices / 100.0F;
    }

    public void setPrices(int prices) {
        this.prices = prices;
    }

    @Override
    public String toString() {
        return "MarketItems{" +
                "appid=" + appid +
                ", id='" + id + '\'' +
                ", amount='" + amount + '\'' +
                ", price=" + price +
                ", fee=" + fee +
                ", converted_price_per_unit=" + converted_price_per_unit +
                ", converted_fee_per_unit=" + converted_fee_per_unit +
                ", listingid='" + listingid + '\'' +
                ", inspectLink='" + inspectLink + '\'' +
                ", prices=" + prices +
                '}';
    }
}