package by.fly.model;

import by.fly.util.Utils;
import com.mysema.query.annotations.QueryEntity;
import com.mysema.query.annotations.QueryTransient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@QueryEntity
@Document
public class Organization extends AbstractModel {

    @Indexed(unique = true)
    private String name;

    private String unp;

    private String registrationData;

    private Date registrationDate;

    private String address;

    private String bankDetails;

    private String paymentAccount;

    public Organization(String name) {
        this.name = name;
    }

    public String getRegistrationData() {
        return registrationData;
    }

    public void setRegistrationData(String registrationData) {
        this.registrationData = registrationData;
    }

    @QueryTransient
    public LocalDate getRegistrationDate() {
        return Utils.toLocalDate(registrationDate);
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = Utils.toDate(registrationDate);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(String bankDetails) {
        this.bankDetails = bankDetails;
    }

    public String getPaymentAccount() {
        return paymentAccount;
    }

    public void setPaymentAccount(String paymentAccount) {
        this.paymentAccount = paymentAccount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnp() {
        return unp;
    }

    public void setUnp(String unp) {
        this.unp = unp;
    }
}
