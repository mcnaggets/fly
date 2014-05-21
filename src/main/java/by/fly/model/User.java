package by.fly.model;

import com.mysema.query.annotations.QueryEntity;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@QueryEntity
@Document
public class User extends Human {

    private String login;

    private String password;

    @Indexed(unique = true)
    private String barcode;

    @DBRef
    private List<Role> roles = new ArrayList<>();

    @DBRef
    private Organization organization;

    public User(String login, @Nullable String password, @Nullable String barcode, Organization organization) {
        this.login = login;
        this.password = password;
        this.barcode = barcode;
        this.organization = organization;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getLogin() {
        return login;
    }

    public Organization getOrganization() {
        return organization;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
