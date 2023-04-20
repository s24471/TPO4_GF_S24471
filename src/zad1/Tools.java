/**
 *
 *  @author Gocławski Filip S24471
 *
 */

package zad1;


import org.yaml.snakeyaml.Yaml;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

public class Tools {
    public static Options createOptionsFromYaml(String fileName) {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> load = yaml.load(new FileReader(fileName));
            Options options = new Options((String)load.get("host"), (int)load.get("port"), (boolean)load.get("concurMode"), (boolean)load.get("showSendRes"), (Map<String, List<String>>)load.get("clientsMap"));
            return options;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
