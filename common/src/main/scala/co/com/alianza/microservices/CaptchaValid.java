package co.com.alianza.microservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

/* GP960 - Reemplazo de Captcha Autoregistro AF/AV*/

public class CaptchaValid {


    public static CaptchaResponse isCaptchaValid(String secretKey, String response) {
        try {
            String url = "https://www.google.com/recaptcha/api/siteverify?"
                    + "secret=" + secretKey
                    + "&response=" + response;
            InputStream res = new URL(url).openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(res, Charset.forName("UTF-8")));

            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            String jsonText = sb.toString();
            res.close();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            CaptchaResponse captchaResponse = gson.fromJson(jsonText,CaptchaResponse.class);
            return captchaResponse;
        } catch (Exception e) {
            return null;
        }
    }
}
