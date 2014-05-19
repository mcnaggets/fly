package by.fly.model;

import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "FLY_USER")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class User extends Human {

    private String login;

    private String password;

    @Column(unique = true, nullable = false)
    private String barcode;

    @ManyToMany
    private List<Role> roles = new ArrayList<>();

    @ManyToOne
    private Organization organization;

    User() {
        // for reflection
    }

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
