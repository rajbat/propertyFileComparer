package com.view;

import com.comparator.PropertiesComparator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

public class MainView {
    private final VBox root = new VBox(10);
    private final TextField pathField1 = new TextField();
    private final TextField pathField2 = new TextField();
    private final Label statusLabel = new Label();
    private final ProgressIndicator spinner = new ProgressIndicator();

    private final Properties props1 = new Properties();
    private final Properties props2 = new Properties();

    private final ComparisonTableView tableView = new ComparisonTableView();
    private boolean showOnlyMissing = false;

    public MainView(Stage stage) {
        Label title = new Label("Property File Comparator");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox fileRow1 = createFileRow(stage, pathField1, props1, true);
        HBox fileRow2 = createFileRow(stage, pathField2, props2, false);

        Button compareBtn = new Button("Compare");
        Button exportBtn = new Button("Export");
        Button toggleBtn = new Button("Show Missing Only");

        spinner.setVisible(false);
        spinner.setMaxSize(25, 25);

        HBox controlRow = new HBox(10, compareBtn, toggleBtn, exportBtn, spinner);
        controlRow.setAlignment(Pos.CENTER_LEFT);

        compareBtn.setOnAction(e -> runComparisonAsync());

        exportBtn.setOnAction(e -> tableView.saveDifferences(stage, showOnlyMissing));

        toggleBtn.setOnAction(e -> {
            showOnlyMissing = !showOnlyMissing;
            toggleBtn.setText(showOnlyMissing ? "Show All Differences" : "Show Missing Only");
            runComparisonAsync();
        });

        VBox.setVgrow(tableView.getNode(), Priority.ALWAYS);
        root.setPadding(new Insets(15));
        root.getChildren().addAll(title, fileRow1, fileRow2, controlRow, statusLabel, tableView.getNode());
    }

    private HBox createFileRow(Stage stage, TextField pathField, Properties props, boolean isFile1) {
        pathField.setPromptText("Select File " + (isFile1 ? "1" : "2"));
        pathField.setPrefWidth(350);
        pathField.setEditable(true);

//        Image fileIconImage = loadIcon("/icons/file.png");
//        Image reloadIconImage = loadIcon("/icons/reload.png");

        Button browseBtn = new Button();
        pathField.setEditable(false);

//        if (fileIconImage != null) {
//            ImageView fileIcon = new ImageView(fileIconImage);
//            fileIcon.setFitWidth(18);
//            fileIcon.setFitHeight(18);
//            browseBtn.setGraphic(fileIcon);
//        } else {
//            browseBtn.setText("📂");
//        }

        Button reloadBtn = new Button();
        pathField.setEditable(false);

//        if (reloadIconImage != null) {
//            ImageView reloadIcon = new ImageView(reloadIconImage);
//            reloadIcon.setFitWidth(18);
//            reloadIcon.setFitHeight(18);
//            reloadBtn.setGraphic(reloadIcon);
//        } else {
//            reloadBtn.setText("↻");
//        }

        browseBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Properties Files", "*.properties"));
            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                pathField.setText(file.getAbsolutePath());
                PropertiesComparator.loadProperties(props, file);
            }
        });

        reloadBtn.setOnAction(e -> {
            if (!pathField.getText().isEmpty()) {
                File file = new File(pathField.getText());
                if (file.exists()) {
                    PropertiesComparator.loadProperties(props, file);
                } else {
                    showAlert("File Missing", "Cannot reload: File not found.");
                }
            }
        });

        HBox hbox = new HBox(5, pathField, browseBtn, reloadBtn);
        hbox.setAlignment(Pos.CENTER_LEFT);
        return hbox;
    }

    private void runComparisonAsync() {
        tableView.clearData();
        disableButtons(true);
        spinner.setVisible(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<ComparisonRow> rows = new ArrayList<>();
                Set<String> keys = new TreeSet<>();
                keys.addAll(props1.stringPropertyNames());
                keys.addAll(props2.stringPropertyNames());

                final int[] missing = {0};

                for (String key : keys) {
                    String val1 = props1.getProperty(key);
                    String val2 = props2.getProperty(key);
                    boolean isMissing = val1 == null || val2 == null;
                    boolean isDifferent = !Objects.equals(val1, val2);

                    if (showOnlyMissing && !isMissing) continue;
                    if (!showOnlyMissing && !isDifferent) continue;

                    rows.add(new ComparisonRow(
                            key,
                            val1 != null ? val1 : "(missing)",
                            val2 != null ? val2 : "(missing)"
                    ));

                    if (isMissing) missing[0]++;
                }

                Platform.runLater(() -> {
                    tableView.setData(rows);
                    if (rows.isEmpty()) {
                        showAlert("No Differences", "✅ No differences based on current filter.");
                        statusLabel.setText("All keys match.");
                    } else {
                        statusLabel.setText("Differences Found: " + rows.size() + " | Missing: " + missing[0]);
                    }
                    spinner.setVisible(false);
                    disableButtons(false);
                });

                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void disableButtons(boolean disable) {
        root.getChildren().stream()
                .filter(node -> node instanceof Button || node instanceof HBox)
                .flatMap(n -> n instanceof HBox ? ((HBox) n).getChildren().stream() : Stream.of(n))
                .filter(n -> n instanceof Button)
                .forEach(btn -> btn.setDisable(disable));
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private Image loadIcon(String path) {
        try {
            return new Image(Objects.requireNonNull(getClass().getResource(path)).toExternalForm());
        } catch (Exception e) {
            System.err.println("Warning: Could not load icon at " + path);
            return null;
        }
    }

    public Node getRoot() {
        return root;
    }
}
