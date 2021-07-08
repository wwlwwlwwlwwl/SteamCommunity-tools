package cn.wwl.Buff.Thread;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class ItemListFetchThread implements Callable<List<String>> {

    @Override
    public List<String> call()  {

/*
        List<String> list = new ArrayList<>();
        list.add("ID,Name,SteamPrice,BuffPrice");
        for (int i = startpage; i < maxpage; i++) {
            Future<String> future = this.executor.submit(new BuffItemManagerThread(i));
            try {
                while (!future.isDone()) {
                    Thread.sleep(50);
                }
            } catch (Exception e) {
                logger.error("AWait Failed!",e);
            }

            String str = null;
            try {
                str = future.get();
            } catch (Exception e) {
                logger.error("Get Failed!",e);
            }
            if (str == null | str.isEmpty()) {
                str = "Unknown,Unknown,Unknown";
            }
            list.add(i + "," + str);

            writeFile(list);
            try {
                Thread.sleep(2000 + new Random().nextInt(1000));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
*/
        return null;
    }
}
