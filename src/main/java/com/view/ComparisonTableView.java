package com.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

public class ComparisonTableView {

    private final TableView<ComparisonRow> tableView = new TableView<>();
    private final ObservableList<ComparisonRow> fullData = FXCollections.observableArrayList();
    private final FilteredList<ComparisonRow> filteredData = new FilteredList<>(fullData, p -> true);
    private final TextField searchField = new TextField();
    private final VBox container = new VBox(10);

    public ComparisonTableView() {
        setupTable();
        setupSearch();

        container.setPadding(new Insets(10));
        container.getChildren().addAll(new Label("Search:"), searchField, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
    }

    private void setupTable() {
        TableColumn<ComparisonRow, String> keyCol = new TableColumn<>("Key");
        keyCol.setCellValueFactory(new PropertyValueFactory<>("key"));
        keyCol.setPrefWidth(200);

        TableColumn<ComparisonRow, String> val1Col = new TableColumn<>("File 1 Value");
        val1Col.setCellValueFactory(new PropertyValueFactory<>("value1"));
        val1Col.setPrefWidth(250);

        TableColumn<ComparisonRow, String> val2Col = new TableColumn<>("File 2 Value");
        val2Col.setCellValueFactory(new PropertyValueFactory<>("value2"));
        val2Col.setPrefWidth(250);

        tableView.getColumns().setAll(keyCol, val1Col, val2Col);
        tableView.setItems(filteredData);

        tableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ComparisonRow row, boolean empty) {
                super.updateItem(row, empty);
                if (row == null || empty) {
                    setStyle("");
                } else if (!Objects.equals(row.getValue1(), row.getValue2())) {
                    setStyle("-fx-background-color: #ffe6e6;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void setupSearch() {
        searchField.setPromptText("Filter by key or value");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase();
            filteredData.setPredicate(row ->
                    row.getKey().toLowerCase().contains(lower)
                            || row.getValue1().toLowerCase().contains(lower)
                            || row.getValue2().toLowerCase().contains(lower)
            );
        });
    }

    public void setData(List<ComparisonRow> rows) {
        fullData.setAll(rows);
    }

    public void clearData() {
        fullData.clear();
    }

    public ObservableList<ComparisonRow> getDisplayedData() {
        return filteredData;
    }

    public Node getNode() {
        return container;
    }

    public void saveDifferences(Stage stage, boolean showOnlyMissing, FileDiffFilter fileDiffFilter) {
        if (filteredData.isEmpty()) {
            showAlert("No Data to Export", "Nothing to export based on current filter.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Differences");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = chooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.printf("# Filter: %s | Missing Only: %s%n", fileDiffFilter, showOnlyMissing);
                pw.println("# Format: key = file1_value | file2_value");

                for (ComparisonRow row : filteredData) {
                    boolean isMissing = "(missing)".equals(row.getValue1()) || "(missing)".equals(row.getValue2());

                    if (showOnlyMissing && !isMissing)
                        continue;

                    if (fileDiffFilter == FileDiffFilter.FILE1_ONLY && !"(missing)".equals(row.getValue2()))
                        continue;

                    if (fileDiffFilter == FileDiffFilter.FILE2_ONLY && !"(missing)".equals(row.getValue1()))
                        continue;

                    pw.printf("%s = %s | %s%n", row.getKey(), row.getValue1(), row.getValue2());
                }
            } catch (Exception e) {
                showAlert("Export Failed", "Failed to write to file.");
            }
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
