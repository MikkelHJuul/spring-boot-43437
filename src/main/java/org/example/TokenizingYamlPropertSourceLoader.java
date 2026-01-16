package org.example;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TokenizingYamlPropertSourceLoader extends YamlPropertySourceLoader {
    @Override
    public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
        var parentResult = super.load(name, resource);

        var map = readInputs(resource);
        var markers = new HashMap<String, Object>(1);
        tokenizeEmptyMapToMyToken(map, markers, "");

        if (markers.isEmpty())
            return parentResult;
        var cpy = new ArrayList<>(parentResult);
        cpy.add(new MapPropertySource("tokenizedSource", markers));
        return cpy;
    }

    //same code as YamlProcesser#buildFlattenedMap
    private void tokenizeEmptyMapToMyToken(Map<String, Object> map, HashMap<String, Object> markers, String path) {
        if (map.isEmpty() && !path.isBlank()) {
            // have to be specific here. BindConverter at this stage cannot convert anything to Record-types
            markers.put(path, new SomeConfig.Token());
            return;
        }
        map.forEach((key, v) -> {
            if (key.startsWith("[")) {
                key = path + key;
            } else
                key = path + '.' + key;
            if (v instanceof Map m) {
                tokenizeEmptyMapToMyToken(m, markers, key);
            }
        });
    }

    private Map<String, Object> readInputs(Resource resource) throws IOException {
        Yaml yaml = new Yaml();
        var m = new HashMap<String, Object>();
        try (Reader reader = new UnicodeReader(resource.getInputStream())) {
            for(Object o: yaml.loadAll(reader)) {
                m.putAll(asMap(o));
            }
        }
        return m;
    }


    // copy-paste from YamlProcessor
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<String, Object> asMap(Object object) {
        // YAML can have numbers as keys
        Map<String, Object> result = new LinkedHashMap<>();
        if (!(object instanceof Map map)) {
            // A document can be a text literal
            result.put("document", object);
            return result;
        }

        map.forEach((key, value) -> {
            if (value instanceof Map) {
                value = asMap(value);
            }
            if (key instanceof CharSequence) {
                result.put(key.toString(), value);
            }
            else {
                // It has to be a map key in this case
                result.put("[" + key.toString() + "]", value);
            }
        });
        return result;
    }
}