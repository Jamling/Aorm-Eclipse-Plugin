/**
 * 
 */
package cn.ieclipse.aorm.eclipse.helpers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import cn.ieclipse.aorm.eclipse.AormPlugin;

/**
 * @author Jamling
 * 
 */
public class ComponentAttributeTipHelper {

    private static final String NS = "android:";
    private static final String FN_EXT = ".tip";
    private static ComponentAttributeTipHelper instance;

    private Map<String, String> activityMap;
    private Map<String, String> serviceMap;
    private Map<String, String> providerMap;
    private Map<String, String> receiverMap;

    public static ComponentAttributeTipHelper getInstance() {
        if (instance == null) {
            synchronized (ComponentAttributeTipHelper.class) {
                instance = new ComponentAttributeTipHelper();
            }
        }
        return instance;
    }

    private ComponentAttributeTipHelper() {
        // activityMap = load(AdtConstants.ACTIVITY_NODE);
        // serviceMap = load(AdtConstants.SERVICE_NODE);
        // providerMap = load(AdtConstants.PROVIDER_NODE);
        // receiverMap = load(AdtConstants.RECEIVER_NODE);
    }

    public static Map<String, String> load(String nodeName) {

        HashMap<String, String> map = new HashMap<String, String>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    AormPlugin.class.getResourceAsStream("tip/" + nodeName
                            + FN_EXT)));
            String name = "";
            StringBuilder val = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                if (line.startsWith(NS)) {
                    if (val.length() > 0) {//
                        map.put(name, val.toString());
                        val.delete(0, val.length());
                    }
                    name = line.substring(NS.length());
                } else {
                    val.append(line);
                }
                line = br.readLine();
            }
            map.put(name, val.toString());
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return map;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Map<String, String> map = ComponentAttributeTipHelper.getInstance()
                .load(AdtConstants.ACTIVITY_NODE);
        for (String key : map.keySet()) {
            System.out.println(key + ":" + map.get(key));
        }
    }

}
