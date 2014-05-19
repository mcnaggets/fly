package by.fly.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Table(name = "FLY_CUSTOMER")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Customer extends Human {
}
