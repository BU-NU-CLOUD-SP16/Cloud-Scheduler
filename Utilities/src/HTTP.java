import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by kovit on 3/10/2016.
 */
public final class HTTP {

    private HTTP() {}

    public static String executeRequest(String url) throws IOException {
        URLConnection conn = new URL(url).openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String strTemp = "";
        StringBuilder sb = new StringBuilder();
        while (null != (strTemp = br.readLine())) {
            sb.append(strTemp);
        }
        return sb.toString();
    }
}
