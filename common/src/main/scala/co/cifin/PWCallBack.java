package co.cifin;

        import java.io.IOException;

        import java.util.MissingResourceException;

        import java.util.ResourceBundle;

        import javax.security.auth.callback.*;

        import javax.security.auth.callback.Callback;

        import org.apache.ws.security.WSPasswordCallback;

public class PWCallBack implements CallbackHandler {

    public void handle(Callback[] callbacks)

            throws IOException, UnsupportedCallbackException {

        for (int i = 0; i < callbacks.length; i++) {

            if (callbacks[i] instanceof WSPasswordCallback) {

                WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];

                ResourceBundle rs = null;

                try {

                    rs = ResourceBundle.getBundle("seguridad");

                } catch (MissingResourceException ex1) {

                    throw new RuntimeException("Preferencias para WSS no encontradas");

                }

                String pass = rs.getString("org.apache.ws.security.crypto.merlin.keystore.password");

                pc.setPassword(pass);

            } else {

                throw new UnsupportedCallbackException(

                        callbacks[i],

                        "Unrecognized Callback");

            }

        }
    }
}