package by.fly.model;

import com.mysema.query.annotations.QueryEntity;
import org.springframework.data.mongodb.core.index.Indexed;

@QueryEntity
public class Role extends AbstractModel {

    @Indexed(unique = true)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
