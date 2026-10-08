package de.freddi;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.io.File;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class App extends Application {

    @Override
    public void start(Stage stage) {

        // VBox erstellen
        // (10) bedeutet 10 Pixel Abstand zwischen den Elementen
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);

        // Überschrift
        Label lblTitle = new Label("QR-Code Generator");
        lblTitle.setStyle("-fx-font-size: 24px;" + "-fx-font-weight: bold;");
        Label lblUntertitel = new Label("URL eingeben und QR-Code erstellen");
        lblUntertitel.setStyle("-fx-font-size: 18px;");

        // Textfeld für die URL-Eingabe
        TextField txtUrl = new TextField();
        txtUrl.setPromptText("URL eingeben");
        txtUrl.setMaxWidth(300);
        txtUrl.setStyle("-fx-font-size: 14px;");

        // Anzeige für den QR-Code
        ImageView imgQrCode = new ImageView();
        final WritableImage[] currentQrCode = new WritableImage[1];
        final String[] currentUrl = new String[1];
        imgQrCode.setFitWidth(300);
        imgQrCode.setFitHeight(300);
        imgQrCode.setPreserveRatio(true);

        // Status-Lable
        Label lblStatus = new Label();
        lblStatus.setStyle("-fx-font-size: 16px;" + "-fx-font-weight: bold;");

        // Buttons erstellen
        // Button zum Generieren
        Button btnGenerate = new Button("QR-Code erzeugen");
        txtUrl.setOnAction(e -> btnGenerate.fire());
        
        // Buttoon zum speichern
        Button btnSave = new Button("QR-Code speichern");
        btnSave.setVisible(false);

        // Button zum Löschen
        Button btnClear = new Button("Zurücksetzen");
        btnClear.setVisible(false);

        btnGenerate.setPrefWidth(180);
        btnSave.setPrefWidth(180);
        btnClear.setPrefWidth(180);

        // Aktion beim Anklicken der Buttons
        // Generierungsbutton
        btnGenerate.setOnAction(e -> {
            
            // URL aus dem Textfeld auslesen
            String url = txtUrl.getText();
            
            // Leeres Textfeld
            if (url.isBlank()) {
                lblStatus.setText("Bitte eine URL eingeben.");
                lblStatus.setTextFill(Color.RED);
                return;
            }

            //URL muss mit https oder http beginnen
            if (!url.startsWith("http://") &&
                !url.startsWith("https://")) {

                lblStatus.setText("URL muss mit http:// oder https:// beginnen.");
                lblStatus.setTextFill(Color.RED);

                return;
            }

            // Testausgabe im Terminal
            System.out.println(url);

            try {
                // QR-Code-Generator erstellen
                QRCodeWriter writer = new QRCodeWriter();

                // URL in eine 300 x 300 Pixel große BitMatrix umwandeln
                BitMatrix matrix = writer.encode(
                    url,
                    BarcodeFormat.QR_CODE,
                    300,
                    300
                );

                // Leeres JavaFX-Bild erstellen
                WritableImage image = new WritableImage(300, 300);

                // PixelWriter zum Einfärben der Bildpunkte abrufen
                PixelWriter pixelWriter = image.getPixelWriter();

                // Jeden Bildpunkt durchlaufen
                for (int x = 0; x < 300; x++) {

                    for (int y = 0; y < 300; y++) {

                        // Schwarze und weiße QR-Code-Felder setzen
                        if (matrix.get(x, y)) {
                            pixelWriter.setColor(x, y, Color.BLACK);
                        } else {
                            pixelWriter.setColor(x, y, Color.WHITE);
                        }
                    }
                }

                // Fertiges Bild in der ImageView anzeigen
                imgQrCode.setImage(image);
                currentQrCode[0] = image;
                currentUrl[0] = url;
                lblStatus.setText("QR-Code erfolgreich erstellt.");
                lblStatus.setTextFill(Color.GREEN);

                // Speicher- & Löschbuttpn einblenden
                btnSave.setVisible(true);
                btnClear.setVisible(true);


            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        // Speicher-Button
        btnSave.setOnAction(e -> {

            FileChooser fileChooser = new FileChooser();

            fileChooser.setTitle("QR-Code speichern");
            
            //Namensvorschlag aus eingegebener URL erstellen
            String fileName = getFileNameFromUrl(currentUrl[0]);
            
            fileChooser.setInitialFileName(fileName + "_qrcode.png");

            FileChooser.ExtensionFilter pngFilter =
                new FileChooser.ExtensionFilter(
                    "PNG-Bild (*.png)",
                    "*.png"
            );

            FileChooser.ExtensionFilter allFilter =
                new FileChooser.ExtensionFilter(
                    "Alle Dateien (*.*)",
                    "*.*"
            );


            fileChooser.getExtensionFilters().addAll(
                    allFilter,
                    pngFilter
            );

            fileChooser.setSelectedExtensionFilter(allFilter);
            

            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {

                try {
                    ImageIO.write(
                        SwingFXUtils.fromFXImage(currentQrCode[0], null),
                        "png",
                        file
                    );

                    System.out.println("QR-Code gespeichert: " + file.getAbsolutePath()); //Konsolenanzeige zum Test
                    lblStatus.setText("✅ QR-Code erfolgreich gespeichert.");
                    lblStatus.setTextFill(Color.GREEN);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        // Lösch-Button
        btnClear.setOnAction(e -> {

            imgQrCode.setImage(null);
            txtUrl.clear();
            txtUrl.requestFocus();

            currentQrCode[0] = null;
            currentUrl[0] = null;

            btnSave.setVisible(false);
            btnClear.setVisible(false);

            lblStatus.setText("Anwendung zurückgesetzt.");
            lblStatus.setTextFill(Color.ORANGE);

        });

        // Elemente zur VBox hinzufügen
        root.getChildren().addAll(
                lblTitle,
                lblUntertitel,
                lblStatus,
                txtUrl,
                btnGenerate,
                imgQrCode,
                btnSave,
                btnClear
        );

        // Fensterinhalt mit einer Größe von 420 x 600 Pixeln
        Scene scene = new Scene(root, 420, 600);

        // Fenster konfigurieren und anzeigen
        stage.setTitle("QR Generator");
        stage.setScene(scene);
        stage.setMinWidth(420);
        stage.setMinHeight(600);
        
            //Icon
        Image icon = new Image(
        getClass().getResourceAsStream("/qricon.png"));
        stage.getIcons().add(icon);
        
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private String getFileNameFromUrl(String url) {

        String fileName = url
                .replace("https://", "")
                .replace("http://", "");

        int slashIndex = fileName.indexOf("/");

        if (slashIndex != -1) {
            fileName = fileName.substring(0, slashIndex);
        }

        fileName = fileName.replace(".", "_");

        return fileName;
    }
}