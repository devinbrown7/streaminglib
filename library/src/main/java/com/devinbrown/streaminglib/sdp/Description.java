package com.devinbrown.streaminglib.sdp;

import java.util.ArrayList;
import java.util.List;

public abstract class Description {
    public String information;
    public Connection connection;
    public List<Bandwidth> bandwidths = new ArrayList<>();
    public Key key;
    public List<Attribute> attributes = new ArrayList<>();

    public List<String> getAttributeValues(String key) {
        List<String> values = new ArrayList<>();
        for (Attribute a : attributes) {
            if (a.attribute.toLowerCase().equals(key.toLowerCase())) {
                values.add(a.value);
            }
        }
        return values;
    }

    public void setAttributeValue(String key, String value) {
        attributes.add(new Attribute(key, value));
    }
}
