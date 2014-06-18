package by.fly.ui.controller;

import by.fly.model.Organization;
import by.fly.model.User;
import by.fly.service.OrganizationService;
import by.fly.service.UserService;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;

@Component
public class OrganizationController extends AbstractController {

    public TextField organizationName;
    public TextField organizationUnp;
    public ImageView organizationLogo;
    public ListView<Node> userList;
    public TextField registrationData;
    public DatePicker registrationDate;
    public TextField address;
    public TextField bankDetails;
    public TextField paymentAccount;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserService userService;

    private Organization organization;

    public void createNewUser() {
        User user = new User("", null, userService.generateMasterBarcode(), organizationService.getRootOrganization());
        userService.save(user);
        addUserItem(user);
    }

    private void addUserItem(User user) {
        final TextField userNameText = createUserNameText(user);
        final TextField barcodeText = createBarcodeText(user);
        final Button deleteButton = createDeleteUserButton(user);

        final HBox hBox = createUserControlArea(user);
        hBox.getChildren().addAll(
                new Label(resourceBundle.getString("master")), userNameText,
                new Label(resourceBundle.getString("code")), barcodeText,
                deleteButton);

        userList.getItems().add(hBox);
    }

    private HBox createUserControlArea(User user) {
        final HBox hBox = new HBox(10);
        hBox.setPadding(new Insets(5));
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setUserData(user);
        return hBox;
    }

    private TextField createBarcodeText(User user) {
        final TextField barcodeText = new TextField(user.getBarcode());
        barcodeText.setEditable(false);
        barcodeText.textProperty().addListener(event -> user.setBarcode(barcodeText.getText()));
        return barcodeText;
    }

    private TextField createUserNameText(User user) {
        final TextField userNameText = new TextField(user.getName());
        userNameText.textProperty().addListener(event -> user.setName(userNameText.getText()));
        return userNameText;
    }

    private Button createDeleteUserButton(User user) {
        final Button deleteButton = new Button(resourceBundle.getString("delete"));
        deleteButton.setOnAction(event -> {
            userService.delete(user);
            userList.getItems().remove(deleteButton.getParent());
        });
        return deleteButton;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        bindRootOrganization();
    }

    public void loadOrganizationLogo() throws IOException {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(resourceBundle.getString("images"), "*.jpg", "*.png", "*.gif", "*.bmp");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(null);
        if (file != null && organizationService.saveOrganizationLogo(organization, file)) {
            showOrganizationLogo(Files.newInputStream(file.toPath()));
        }
    }

    private void showOrganizationLogo(InputStream imageStream) {
        try {
            BufferedImage bufferedImage = ImageIO.read(imageStream);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            organizationLogo.setImage(image);
        } catch (IOException ex) {
            LOGGER.error("Error while reading logo", ex);
        }
    }

    public void organizationCancel() {
        bindRootOrganization();
    }

    private void bindRootOrganization() {
        organization = organizationService.getRootOrganization();
        organizationName.setText(organization.getName());
        organizationUnp.setText(organization.getUnp());
        address.setText(organization.getAddress());
        bankDetails.setText(organization.getBankDetails());
        paymentAccount.setText(organization.getPaymentAccount());
        registrationData.setText(organization.getRegistrationData());
        registrationDate.setValue(organization.getRegistrationDate());
        organizationUnp.setText(organization.getUnp());
        InputStream logo = organizationService.findOrganizationLogo(organization);
        if (logo != null) {
            showOrganizationLogo(logo);
        }
        userList.getItems().clear();
        userService.findAll().forEach(this::addUserItem);
    }

    public void organizationSave() {
        userList.getItems().forEach(node -> userService.save((User) node.getUserData()));
        organization.setName(organizationName.getText());
        organization.setUnp(organizationUnp.getText());
        organization.setAddress(address.getText());
        organization.setBankDetails(bankDetails.getText());
        organization.setPaymentAccount(paymentAccount.getText());
        organization.setRegistrationData(registrationData.getText());
        organization.setRegistrationDate(registrationDate.getValue());
        organizationService.save(organization);
    }

}
