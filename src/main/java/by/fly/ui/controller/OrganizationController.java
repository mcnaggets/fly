package by.fly.ui.controller;

import by.fly.model.Organization;
import by.fly.model.User;
import by.fly.repository.OrganizationRepository;
import by.fly.repository.UserRepository;
import by.fly.service.OrganizationService;
import by.fly.service.UserService;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

    public TextField organizationName;
    public TextField organizationInn;
    public ImageView organizationLogo;
    public ListView<Node> userList;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private Organization organization;

    public void handleNewUser(ActionEvent actionEvent) {
        User user = new User("", null, userService.generateBarcode(), organizationService.getRootOrganization());
        userRepository.save(user);
        addUserItem(user);
    }

    private void addUserItem(User user) {
        final TextField userNameText = new TextField(user.getName());
        userNameText.addEventFilter(InputEvent.ANY, event -> user.setName(userNameText.getText()));

        final TextField barcodeText = new TextField(user.getBarcode());
        barcodeText.setEditable(false);
        barcodeText.addEventFilter(InputEvent.ANY, event -> user.setBarcode(barcodeText.getText()));

        final HBox hBox = new HBox(
                new Label(resourceBundle.getString("master")),
                userNameText,
                new Label(resourceBundle.getString("code")),
                barcodeText
        );
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setUserData(user);

        final Button deleteButton = new Button(resourceBundle.getString("delete"));
        deleteButton.setOnAction(event -> {
            userRepository.delete(user);
            userList.getItems().remove(hBox);
        });
        hBox.getChildren().add(deleteButton);

        userList.getItems().add(hBox);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        bindOrganization();
    }

    public void loadOrganizationLogo(ActionEvent actionEvent) throws IOException {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);

        //Show open file dialog
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

    public void organizationCancel(ActionEvent actionEvent) {
        bindOrganization();
    }

    private void bindOrganization() {
        organization = organizationService.getRootOrganization();
        organizationName.setText(organization.getName());
        organizationInn.setText(organization.getInn());
        InputStream logo = organizationService.findOrganizationLogo(organization);
        if (logo != null) {
            showOrganizationLogo(logo);
        }
        userList.getItems().clear();
        userRepository.findAll().forEach(this::addUserItem);
    }

    public void organizationSave(ActionEvent actionEvent) {
        userList.getItems().forEach(node -> {
            userRepository.save((User) node.getUserData());
        });
        organization.setName(organizationName.getText());
        organization.setInn(organizationInn.getText());
        organizationRepository.save(organization);
    }


}
