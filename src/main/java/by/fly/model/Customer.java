package by.fly.model;

import com.mysema.query.annotations.QueryEntity;
import org.springframework.data.mongodb.core.mapping.Document;

@QueryEntity
@Document
public class Customer extends Human {

    public Customer(String name, String phone) {
        setName(name);
        setPhone(phone);
    }

}
