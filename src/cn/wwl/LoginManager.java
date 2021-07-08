package cn.wwl;

import cn.wwl.Beans.FileBean.ConfigFile;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.crypto.Cipher;
import java.io.Console;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Security;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;

/**
 * 进行登录操作
 */
public class LoginManager {

    /*
     * 向 https://store.steampowered.com/login/getrsakey/ 发送Post ->
     * 获取 publickey_mod 和 publickey_exp
     * final RSACrypto rsa = new RSACrypto(new BigInteger(1, SteamWeb.hexToByte(rsaJSON.publickey_exp)), new BigInteger(1, SteamWeb.hexToByte(rsaJSON.publickey_mod)), false);
     * rsa加密密码 然后再base64加密一次
     * 带着用户名密码等信息POST https://store.steampowered.com/login/dologin/
     * 如果requires_twofactor则为需要二步验证
     * 登录成功则会返回login_complete
     * SessionID 无须改变 应该请求官网就能得到
     * SteamLoginSecure为 steamid||token_secure
     */

    private static final Logger logger = LogManager.getLogger(LoginManager.class);
    private static LoginManager loginManager;
    private final ConfigFile config;

    private LoginManager() {
        this.config = FileLoader.getConfigs();
    }

    /**
     * 登录成功
     */
    public static final int LOGIN_SUCCESS = 1000;
    /**
     * 登录失败
     */
    public static final int LOGIN_FAILED = 1001;
    /**
     * 需要二步验证
     */
    public static final int NEED_2FA = 1002;
    /**
     * 密码错误
     */
    public static final int WRONG_PASSWORD = 1003;
    /**
     * 抛出了异常
     */
    public static final int UNKNOWN_ERROR = 9999;

    /**
     * 检查账户是否正常在线
     * @return 如果未null则为未登录 否则返回用户名
     */
    public String checkLogin() {
        Connection connection = Jsoup.connect("https://store.steampowered.com/account/")
                .ignoreHttpErrors(true)
                .ignoreContentType(true)
                .cookie("Steam_Language","english")
                .cookie("steamLoginSecure",config.getSteamLoginSecure())
                .cookie("steamRememberLogin",config.getSteamRememberLogin());

        config.getSteamMachineAuth().forEach(connection::cookie);

        Document document;
        try {
            document = connection.get();
        } catch (Exception e) {
            logger.error("Exception",e);
            return null;
        }
        String response = document.body().text();
        //logger.info(response);
        if (response.contains("Logout")) {
            String[] msg = response.split(" ");

            connection.response().cookies().forEach((k,v) -> {
                if (k.contains("steamMachineAuth")) {
                    config.getSteamMachineAuth().put(k, v);
                }
            });

            return msg[0];
        }

        return null;
    }

    public void requestLogin() {
        int code;
        if (System.console() == null) {
            Scanner scanner = new Scanner(System.in);
            String username, password, twofactor;
            logger.info("Enter Username : ");
            username = scanner.next();
            logger.info("Enter Password : ");
            password = scanner.next();
            logger.info("Enter 2fa : ");
            twofactor = scanner.next();
            code = loginManager.loginAccount(username, password, twofactor);
        } else {
            Console console = System.console();
            String username, password, twofactor;
            logger.info("Enter Username : ");
            username = console.readLine();
            logger.info("Enter Password : ");
            password = new String(console.readPassword());
            logger.info("Enter 2fa : ");
            twofactor = console.readLine();
            code = loginManager.loginAccount(username, password, twofactor);
        }

        switch (code) {
            case LOGIN_SUCCESS: {logger.info("Login Success!");break;}
            case LOGIN_FAILED: {logger.info("Login Failed!"); requestLogin();}
            case NEED_2FA: {logger.info("2FA Required"); requestLogin();}
            case WRONG_PASSWORD: {logger.info("Wrong Password!"); requestLogin();}
            case UNKNOWN_ERROR: {logger.error("Somethings wrong here..."); System.exit(99);}
            default: {logger.error("Unknown Code Error!");System.exit(99);}
        }
    }

    /**
     * 登录目标账户 如果登录成功将会把token写入配置文件
     * @param username 要登录的用户名
     * @param password 要登录的密码
     * @param twofactor 二部验证代码(如果未开启二步验证可以传入null)
     * @return 与上面的定义相同
     */
    public int loginAccount(final String username,final String password,final String twofactor) {
        if (checkLogin() != null) {
            return LOGIN_SUCCESS;
        }

        Connection connection = Jsoup.connect("https://store.steampowered.com/login/getrsakey")
                .timeout(10000)
                .ignoreContentType(true)
                .data("username",username);

        Document document;

        try {
            document = connection.post();
        } catch (Exception e) {
            logger.error("Get RSA Key Failed!",e);
            return LOGIN_FAILED;
        }

        String body = document.body().text();
            //logger.info("GetRSA : " + body);
            JsonObject object = JsonParser.parseString(body).getAsJsonObject();

            if (!object.get("success").getAsBoolean()) {
                logger.error("Success = false!");
                logger.error(body);
                return UNKNOWN_ERROR;
            }

            JsonElement keymod = object.get("publickey_mod");
            JsonElement keyexp = object.get("publickey_exp");
            JsonElement timestamp = object.get("timestamp");

            if (keyexp == null && keymod == null) {
                logger.error("Can not Find keymod or keyexp!");
                logger.error(body);
                return UNKNOWN_ERROR;
            }

            if (timestamp == null) {
                logger.error("Can not Find TimeStamp!");
                logger.error(body);
                return UNKNOWN_ERROR;
            }

            String encryptedPassword = encryptPassword(keymod.getAsString(),keyexp.getAsString(),password);

            Map<String,String> logindata = new HashMap<>();

            Connection login = Jsoup.connect("https://store.steampowered.com/login/dologin")
                    .timeout(10000)
                    .ignoreContentType(true);

            logindata.put("password",encryptedPassword);
            logindata.put("username",username);

            //try {
                logindata.put("rsatimestamp", URLEncoder.encode(timestamp.getAsString(), StandardCharsets.UTF_8));
            //} catch (Exception e) {
            //    logger.error("WTF?",e);
            //    return UNKNOWN_ERROR;
            //}

            //TODO
            logindata.put("captchagid","-1");
            logindata.put("captcha_text","");

            //logindata.put("loginfriendlyname", "");
            logindata.put("emailauth","");
            logindata.put("emailsteamid","");
            logindata.put("remember_login","true");

            logindata.put("twofactorcode", twofactor == null ? "" : twofactor);

            login.data(logindata);

            Document loginDocument;
            try {
                loginDocument = login.post();
            } catch (Exception e) {
                logger.error("Try Login Failed!",e);
                return UNKNOWN_ERROR;
            }

            JsonObject obj2fa = JsonParser.parseString(loginDocument.body().text()).getAsJsonObject();
            //logger.info("Login : " + loginDocument.body().text());

            if (!obj2fa.get("success").getAsBoolean() && obj2fa.get("requires_twofactor").getAsBoolean()) {

                if (obj2fa.get("message") != null && obj2fa.get("message").getAsString().length() != 0) {
                    logger.info("Message : " + obj2fa.get("message").getAsString());
                }

                return NEED_2FA;
            }

            if (obj2fa.get("login_complete").getAsBoolean()) {
                logger.info("Login Success!" + loginDocument.body().text());
                String steamid = null;
                String tokensecure = null;
                JsonObject param = obj2fa.get("transfer_parameters").getAsJsonObject();
                Map<String,String> list = new HashMap<>();
                for (String s : param.keySet()) {
                    JsonElement element = param.get(s);
                    if (s.equals("steamid"))
                        steamid = element.getAsString();

                    if (s.equals("token_secure"))
                        tokensecure = element.getAsString();

                    list.put(s,element.getAsString());
                }
                JsonArray urls = obj2fa.get("transfer_urls").getAsJsonArray();
                urls.forEach((e) -> {
                    String link = e.getAsString();
                    logger.info("Transfer Data To " + link);
                    try {
                         Connection c = Jsoup.connect(link)
                                .data(list)
                                .cookie("steamRememberLogin",config.getSteamRememberLogin());
                        config.getSteamMachineAuth().forEach(c::cookie);
                        c.post();
                    } catch (Exception exception) {
                        logger.info("Post Failed!",exception);
                    }
                });

                String steamSecure = steamid + "||" + tokensecure;

                //TODO Set-Cookie
                login.response().cookies().forEach((k,v) -> {
                    if (k.contains("steamMachineAuth")) {
                        config.getSteamMachineAuth().put(k, v);
                    } else if (k.contains("steamRememberLogin")) {
                        config.setSteamRememberLogin(URLDecoder.decode(v,StandardCharsets.UTF_8));
                    }
                });

                config.setSteamLoginSecure(steamSecure);
                FileLoader.saveConfig();
                return LOGIN_SUCCESS;
            }

            return UNKNOWN_ERROR;
    }

    /**
     * 对密码进行RSA+Base64加密
     * @param publicmod 公钥
     * @param publicexp 公钥
     * @param data 密码
     * @return 加密后的密码
     */
    private String encryptPassword(String publicmod, String publicexp,String data) {
        try {
            //RSAPublicKeySpec publickey = new RSAPublicKeySpec(new BigInteger(publicexp,16),new BigInteger(publicmod,16));
            RSAPublicKeySpec publickey = new RSAPublicKeySpec(new BigInteger(publicmod,16),new BigInteger(publicexp,16));

            KeyFactory factory = KeyFactory.getInstance("RSA");

            RSAPublicKey key = (RSAPublicKey) factory.generatePublic(publickey);

            Security.addProvider(new BouncyCastleProvider());

            //Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
            Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");

            cipher.init(Cipher.ENCRYPT_MODE,key);
            byte[] finalkey = cipher.doFinal(data.getBytes());
            //logger.info("Final : " + new String(finalkey) + " , Base64 : " + new String(base64));
            return Base64.getEncoder().encodeToString(finalkey);
        } catch (Exception e) {
            logger.error("Encrypt Data Failed!",e);
        }
        return null;
    }

    /**
     * 获取登录管理器的实例
     * @return 实例
     */
    public static LoginManager getLoginManager() {
        if (loginManager == null)
            loginManager = new LoginManager();
        return loginManager;
    }
}