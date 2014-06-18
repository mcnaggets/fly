package by.fly.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Settings {

    public static final String DEFAULT_PRINTER = "default_printer";
    public static final String ITEM_TYPES = "item_types";

    @Id
    private String name;

    private Object value;

    @PersistenceConstructor
    public Settings(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Settings{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
