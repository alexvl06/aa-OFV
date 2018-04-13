package co.com.alianza.microservices;

import java.util.Date;

/* GP960 - Reemplazo de Captcha Autoregistro AF/AV*/

public class CaptchaResponse {

    private boolean success;
    private Date challenge_ts;
    private String hostname;
    private String[] errorcodes;


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Date getChallenge_ts() {
        return challenge_ts;
    }

    public void setChallenge_ts(Date challenge_ts) {
        this.challenge_ts = challenge_ts;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String[] getErrorcodes() {
        return errorcodes;
    }

    public void setErrorcodes(String[] errorcodes) {
        this.errorcodes = errorcodes;
    }
}
