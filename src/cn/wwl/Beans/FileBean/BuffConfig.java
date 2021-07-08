package cn.wwl.Beans.FileBean;

public class BuffConfig {
    /*
private String Device_Id = "jeRPRky29UsRB9O9id7p";
private String remember_me = "U1098779305|YDJnsJ1looJg1oEEfhHdoi8lJ9nHrD68";
private String session = "1-acaZuLJdWb5Hyn0dZp-2kO0mpkp1CgfB-oH1XUdK3GEI2041615857";
private String csrf_token = "IjQ3ODQ4MDZmYTFkMDExYWQwYWExODVlMGFjY2M4ZDYwMzBjYjJlYzQi.Eo12Vg.Zyemd_cvwK4vmk9g-zaU4oZVvuw";
*/
    private String Device_Id = "Device_ID";
    private String remember_me = "Remember_me";
    private String session = "SessionID";
    private String csrf_token = "csrf_Token";

    public void setCsrf_token(String csrf_token) {
        this.csrf_token = csrf_token;
    }

    public void setDevice_Id(String device_Id) {
        Device_Id = device_Id;
    }

    public void setRemember_me(String remember_me) {
        this.remember_me = remember_me;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getCsrf_token() {
        return csrf_token;
    }

    public String getDevice_Id() {
        return Device_Id;
    }

    public String getRemember_me() {
        return remember_me;
    }

    public String getSession() {
        return session;
    }
}
