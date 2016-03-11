import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by kovit on 3/10/2016.
 */
public final class HTTP {

    private HTTP() {}

    public static String executeRequest(String url) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
        String strTemp = "";
        StringBuilder sb = new StringBuilder();
        while (null != (strTemp = br.readLine())) {
            sb.append(strTemp);
        }
        return sb.toString();
    }
}
