package cn.wwl;

import cn.wwl.Beans.MarketBean.CSGOItems;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

/**
 * 托盘管理
 */
public class TrayManager {

    public TrayManager() throws Exception {
        if (inited)
            throw new Exception("Only Can Have Single TrayManager");
    }

    private static final Logger logger = LogManager.getLogger(TrayManager.class);

    /**
     * 托盘实例
     */
    private static TrayIcon tray = null;

    /**
     * 用于保证只有一个实例
     */
    private static boolean inited;

    /**
     * 进行托盘的装载
     */
    public void initTray() {
        logger.info("Try init Tray...");
        if (!SystemTray.isSupported()) {
            logger.error("Tray Not Supported! Please Close Tray!");
            System.exit(99);
        }
        URL res = MarketTools.class.getResource("/icon.png");

        if (res == null) {
            logger.error("Icon Not Found! Tray init Failed!");
            System.exit(99);
        }

        tray = new TrayIcon(new ImageIcon(res).getImage());
        //PopupMenu popupMenu = new PopupMenu("Menu");

        //setpopList(tray,popupMenu);
        updatePop();

        tray.setImageAutoSize(true);
        //tray.setPopupMenu(popupMenu);
        tray.setToolTip("SteamCommunity Tools");

        try {
            SystemTray.getSystemTray().add(tray);
        } catch (Exception e) {
            logger.error("Add Tray Failed!", e);
        }
        logger.info("Tray Inited Done.");
    }

    /**
     * 更新托盘的列表
     */
    private void updatePop() {
        if (tray == null) {
            logger.error("Tray Not Inited!");
            System.exit(99);
        }

        if (tray.getPopupMenu() == null) {
            tray.setPopupMenu(setpopList());
            return;
        }

        tray.setPopupMenu(setpopList());
        FileLoader.saveConfig();
    }

    /**
     * 通过托盘询问用户是否要购买该饰品 通过点击提示来确认
     * @param item 要提示的物品
     */
    public void tipPlayerBuy(CSGOItems item) {
        if (FileLoader.getConfigs().isUseTray()) {
            if (tray == null)
                logger.error("Tray Not Inited!");

            tray.addActionListener(e -> {
                if (e.getID() == ActionEvent.ACTION_PERFORMED) {
                    logger.info("Popup Clicked Buy Items!");
                    MarketTools.getInstance().buyItems(item);
                    //System.out.println("Buy Item");
                    //清空点击处理方法 避免误购买
                    Arrays.stream(tray.getActionListeners()).forEach(tray::removeActionListener);
                }
            });
            tray.displayMessage("Finded Items!","Item : " + item.getInfo().getFull_item_name() + " \nPrice : " + item.getItem().getPrices() + " \nFloat : " + item.getInfo().getFloatvalue() + " \nClick To Buy Items!", TrayIcon.MessageType.INFO);
        }
    }

    /**
     * 对托盘发送提示的一个封装
     * @param title 全部同displayMessage
     * @param msg 同DisplayMessage
     * @param type 同DisplayMessage
     */
    public void trayPostMessage(String title, String msg, TrayIcon.MessageType type) {
        if (FileLoader.getConfigs().isUseTray()) {
            if (tray == null)
                logger.error("Tray Not Inited!");

            tray.displayMessage(title,msg,type);
        }
    }

    /**
     * 根据配置文件生成新的列表
     * @return 生成的列表
     */
    private PopupMenu setpopList() {
        if (tray == null) {
            logger.error("Tray Not Inited!");
            System.exit(99);
        }
        PopupMenu popupMenu = new PopupMenu("Menu");
        popupMenu.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        for (MarketBot bot : MarketTools.getInstance().getBots().values()) {
            PopupMenu botMenu = new PopupMenu(bot.getTarget().getBotName());
            botMenu.add(new MenuItem(bot.getTarget().isEnabled() ? "-> Disable Bot" : "-> Enable Bot")).addActionListener(s -> {
                //logger.info("Bot Enable = " + !bot.getTarget().isEnabled());
                bot.getTarget().setEnabled(!bot.getTarget().isEnabled());
                updatePop();
            });
            botMenu.add(new MenuItem(bot.getTarget().isAutoBuy() ? "-> Disable AutoBuy" : "-> Enable AutoBuy")).addActionListener(s -> {
                //logger.info("Bot AutoBuy = " + !bot.getTarget().isAutoBuy());
                bot.getTarget().setAutoBuy(!bot.getTarget().isAutoBuy());
                updatePop();
            });
            popupMenu.add(botMenu);
            popupMenu.addSeparator();
        }

        popupMenu.add(new MenuItem("Open SteamTools Settings")).addActionListener(e -> {
            if (!Desktop.isDesktopSupported()) {
                tray.displayMessage("Error", "Open Desktop Failed!", TrayIcon.MessageType.ERROR);
            }
            try {
                Desktop.getDesktop().open(FileLoader.configFile);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        if (FileLoader.getConfigs().isPriceCheckerMode()) {
            popupMenu.add(new MenuItem("Open BuffTools Settings")).addActionListener(e -> {
                if (!Desktop.isDesktopSupported()) {
                    tray.displayMessage("Error", "Open Desktop Failed!", TrayIcon.MessageType.ERROR);
                }
                try {
                    Desktop.getDesktop().open(FileLoader.buffConfigFile);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });
        }

        popupMenu.add(new MenuItem(MarketTools.pause ? "Resume" : "Pause")).addActionListener(e -> {
            logger.info("Popup " + (MarketTools.pause ? "Resume" : "Pause"));
            MarketTools.pause = !MarketTools.pause;
            updatePop();
        });

        popupMenu.add(new MenuItem("Exit")).addActionListener(e -> {
            logger.info("Popup Click Exit!");
            System.exit(0);
            updatePop();
        });
        return popupMenu;
    }
}
