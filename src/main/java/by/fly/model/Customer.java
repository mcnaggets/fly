package by.fly.model;

import com.mysema.query.annotations.QueryEntity;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@QueryEntity
@Document
@CompoundIndex(name = "name_idx", def = "{'name': 1, 'phone': 1}", unique = true)
public class Customer extends Human {

    public Customer(String name, String phone) {
        setName(name);
        setPhone(phone);
    }

}
