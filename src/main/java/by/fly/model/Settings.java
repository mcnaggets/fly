package by.fly.model;

import com.mysema.query.annotations.QueryEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@QueryEntity
public class Settings {

    public static final String DEFAULT_PRINTER = "default_printer";
    public static final String ITEM_TYPES = "item_types";

    @Id
    private String name;

    private Object value;

    private Object userData;

    @PersistenceConstructor
    public Settings(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public Settings(String name, Object value, Object userData) {
        this(name, value);
        this.userData = userData;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public Object getUserData() {
        return userData;
    }

    public void setUserData(Object userData) {
        this.userData = userData;
    }

    @Override
    public String toString() {
        return "Settings{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }

}
