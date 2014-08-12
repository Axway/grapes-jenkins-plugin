package org.axway.grapes.jenkins.slicing;

import configurationslicing.Slice;
import hudson.model.Descriptor;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.util.*;

/**
 * Grapes Configuration Slice
 *
 * <p>Optional implementation of an extensiont point provided by Jenkins configuration slicing plugin.
 * Manages Grapes Configuration slice unit for Grapes configuration slicing</p>
 *
 * @author jdcoffre
 */
public class GrapesConfigSlice extends Slice {

    public static final String DISABLED = "Disabled";

    private final Map<String, String> projectConfigurations = new HashMap<String, String>();
    private final List<String> grapesConfigurationNames;

    private GrapesConfigSlice() {
        grapesConfigurationNames = new ArrayList<String>();
    }


    public GrapesConfigSlice(final List<String> grapesConfigurationNames) {
        this.grapesConfigurationNames = grapesConfigurationNames;
    }

    @Override
    public Slice newInstance(final StaplerRequest req, final JSONObject formData) throws Descriptor.FormException {
        final GrapesConfigSlice slice = new GrapesConfigSlice();
        final JSONArray configurations = formData.getJSONArray("serverConfiguration");
        Iterator<JSONObject> configIterator = configurations.iterator();
        while(configIterator.hasNext()){
            final JSONObject config = configIterator.next();
            final String configName = (String) config.keys().next();
            for(String item: getStringList(config, configName)){
                slice.add(item, configName);
            }
        }

        return slice;
    }

    public void add(final String projectName, final String serverConfigurationName) {
        projectConfigurations.put(projectName, serverConfigurationName);
    }

    public List<String> getGrapesServerNames(){
        return grapesConfigurationNames;

    }

    public String getItemNamesString(final String name) {
        final List<String> items = new ArrayList<String>();
        for (Map.Entry<String, String> projectConfig : projectConfigurations.entrySet()) {
            if(projectConfig.getValue().equals(name)){
                items.add(projectConfig.getKey());
            }
        }

        return getListString(items);
    }

    private List<String> getStringList(final JSONObject formData, final String key) {
        final String elements = formData.getString(key);
        final List<String> list = new ArrayList<String>();
        for (Object o: elements.split("\n")) {
            list.add((String) o);
        }
        return list;
    }

    private String getListString(final List<String> list) {
        final StringBuilder sb = new StringBuilder();
        for (String item: list) {
            sb.append(item + "\n");
        }
        return sb.toString();
    }


    public String getConfiguration(final String projectName) {
        return projectConfigurations.get(projectName);
    }
}
