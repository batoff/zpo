package com.project.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.dao.ProjektDAO;
import com.project.model.Projekt;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ProjectController {
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter dateTimeFormater = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Zmienne do obsługi stronicowania i wyszukiwania
    private String search4;
    private Integer pageNo;
    private Integer pageSize;

    private ExecutorService wykonawca;
    private ProjektDAO projektDAO;
    private ObservableList<Projekt> projekty;

    // Automatycznie wstrzykiwane komponenty GUI
    @FXML
    private ChoiceBox<Integer> cbPageSizes;
    @FXML
    private TableView<Projekt> tblProjekt;
    @FXML
    private TableColumn<Projekt, Integer> colId;
    @FXML
    private TableColumn<Projekt, String> colNazwa;
    @FXML
    private TableColumn<Projekt, String> colOpis;
    @FXML
    private TableColumn<Projekt, LocalDateTime> colDataCzasUtworzenia;
    @FXML
    private TableColumn<Projekt, LocalDate> colDataOddania;
    @FXML
    private TextField txtSzukaj;
    @FXML
    private Button btnDalej;
    @FXML
    private Button btnWstecz;
    @FXML
    private Button btnPierwsza;
    @FXML
    private Button btnOstatnia;

    public ProjectController() { // Utworzeniu konstruktora jest obligatoryjne
    }

    public ProjectController(ProjektDAO projektDAO) {
        this.projektDAO = projektDAO;
        wykonawca = Executors.newFixedThreadPool(1);
    }

    @FXML
    public void initialize() {
        search4 = "";
        pageNo = 0;
        pageSize = 10;
        cbPageSizes.getItems().addAll(5, 10, 20, 50, 100);
        cbPageSizes.setValue(pageSize);

        // Nasłuchiwanie zmian rozmiaru strony
        cbPageSizes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                pageSize = newVal;
                pageNo = 0;
                wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
            }
        });

        // Inicjalizacja kolumn tabeli
        colId.setCellValueFactory(new PropertyValueFactory<Projekt, Integer>("projektId"));
        colNazwa.setCellValueFactory(new PropertyValueFactory<Projekt, String>("nazwa"));
        colOpis.setCellValueFactory(new PropertyValueFactory<Projekt, String>("opis"));
        colDataCzasUtworzenia.setCellValueFactory(new PropertyValueFactory<Projekt, LocalDateTime>("dataCzasUtworzenia"));
        colDataOddania.setCellValueFactory(new PropertyValueFactory<Projekt, LocalDate>("dataOddania"));

        // Formatowanie daty i czasu
        colDataCzasUtworzenia.setCellFactory(column -> new TableCell<Projekt, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(dateTimeFormater.format(item));
                }
            }
        });

        colDataOddania.setCellFactory(column -> new TableCell<Projekt, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(item));
                }
            }
        });

        // Utworzenie nowej kolumny z przyciskami Edytuj, Usuń, Zadania
        TableColumn<Projekt, Void> colEdit = new TableColumn<>("Edycja");
        colEdit.setCellFactory(column -> new TableCell<Projekt, Void>() {
            private final GridPane pane;
            { // Blok inicjalizujący w anonimowej klasie wewnętrznej
                Button btnEdit = new Button("Edycja");
                Button btnRemove = new Button("Usuń");
                Button btnTask = new Button("Zadania");
                btnEdit.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                btnRemove.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                btnTask.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                btnEdit.setOnAction(event -> {
                    edytujProjekt(getCurrentProjekt());
                });
                btnRemove.setOnAction(event -> {
                    usunProjekt(getCurrentProjekt());
                });
                btnTask.setOnAction(event -> {
                    // TODO wywoływać metodę openZadanieFrame(getCurrentProjekt()); po jej utworzeniu
                });
                pane = new GridPane();
                pane.setAlignment(Pos.CENTER);
                pane.setHgap(10);
                pane.setVgap(10);
                pane.setPadding(new Insets(5, 5, 5, 5));
                pane.add(btnTask, 0, 0);
                pane.add(btnEdit, 0, 1);
                pane.add(btnRemove, 0, 2);
            }

            private Projekt getCurrentProjekt() {
                int index = this.getTableRow().getIndex();
                return this.getTableView().getItems().get(index);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        // Dodanie kolumny do tabeli
        tblProjekt.getColumns().add(colEdit);

        // Ustawienie względnej szerokości poszczególnych kolumn
        colId.setMaxWidth(5000);
        colNazwa.setMaxWidth(10000);
        colOpis.setMaxWidth(10000);
        colDataCzasUtworzenia.setMaxWidth(9000);
        colDataOddania.setMaxWidth(7000);
        colEdit.setMaxWidth(7000);

        projekty = FXCollections.observableArrayList();
        // Powiązanie tabeli z listą typu ObservableList przechowującą projekty
        tblProjekt.setItems(projekty);

        // Załadowanie pierwszej strony
        wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
    }

    // Grupa metod do obsługi przycisków
    @FXML
    private void onActionBtnSzukaj(ActionEvent event) {
        search4 = txtSzukaj.getText().trim();
        pageNo = 0;
        wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
    }

    @FXML
    private void onActionBtnDalej(ActionEvent event) {
        int totalRows = getTotalRows();
        int maxPage = Math.max(0, (totalRows - 1) / pageSize);
        if (pageNo < maxPage) {
            pageNo++;
            wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
        }
    }

    @FXML
    private void onActionBtnWstecz(ActionEvent event) {
        if (pageNo > 0) {
            pageNo--;
            wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
        }
    }

    @FXML
    private void onActionBtnPierwsza(ActionEvent event) {
        pageNo = 0;
        wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
    }

    @FXML
    private void onActionBtnOstatnia(ActionEvent event) {
        int totalRows = getTotalRows();
        int maxPage = Math.max(0, (totalRows - 1) / pageSize);
        pageNo = maxPage;
        wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
    }

    @FXML
    private void onActionBtnDodaj(ActionEvent event) {
        edytujProjekt(new Projekt());
    }

    private int getTotalRows() {
        try {
            if (search4 != null && !search4.isEmpty()) {
                if (search4.matches("[0-9]+")) {
                    return 1;
                } else if (search4.matches("^[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$")) {
                    return projektDAO.getRowsNumberWhereDataOddaniaIs(LocalDate.parse(search4, dateFormatter));
                } else {
                    return projektDAO.getRowsNumberWhereNazwaLike(search4);
                }
            } else {
                return projektDAO.getRowsNumber();
            }
        } catch (RuntimeException e) {
            logger.error("Błąd podczas pobierania liczby wierszy.", e);
            return 0;
        }
    }

    private void loadPage(String search4, Integer pageNo, Integer pageSize) {
        try {
            final List<Projekt> projektList = new ArrayList<>();
            if (search4 != null && !search4.isEmpty()) {
                if (search4.matches("[0-9]+")) {
                    // IDENTYFIKATOR
                    Projekt p = projektDAO.getProjekt(Integer.parseInt(search4));
                    if (p != null) {
                        projektList.add(p);
                    }
                } else if (search4.matches("^[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$")) {
                    // DATA
                    projektList.addAll(projektDAO.getProjektyWhereDataOddaniaIs(
                            LocalDate.parse(search4, dateFormatter), pageNo * pageSize, pageSize));
                } else {
                    // NAZWA
                    projektList.addAll(projektDAO.getProjektyWhereNazwaLike(search4, pageNo * pageSize, pageSize));
                }
            } else {
                projektList.addAll(projektDAO.getProjekty(pageNo * pageSize, pageSize));
            }
            Platform.runLater(() -> {
                projekty.clear();
                projekty.addAll(projektList);
            });
        } catch (RuntimeException e) {
            String errMsg = "Błąd podczas pobierania listy projektów.";
            logger.error(errMsg, e);
            String errDetails = e.getCause() != null ?
                    e.getMessage() + "\n" + e.getCause().getMessage()
                    : e.getMessage();
            Platform.runLater(() -> showError(errMsg, errDetails));
        }
    }

    private void edytujProjekt(Projekt projekt) {
        Dialog<Projekt> dialog = new Dialog<>();
        dialog.setTitle("Edycja");
        if (projekt.getProjektId() != null) {
            dialog.setHeaderText("Edycja danych projektu");
        } else {
            dialog.setHeaderText("Dodawanie projektu");
        }
        dialog.setResizable(true);

        Label lblId = getRightLabel("Id: ");
        Label lblNazwa = getRightLabel("Nazwa: ");
        Label lblOpis = getRightLabel("Opis: ");
        Label lblDataCzasUtworzenia = getRightLabel("Data utworzenia: ");
        Label lblDataOddania = getRightLabel("Data oddania: ");
        Label txtId = new Label();
        if (projekt.getProjektId() != null)
            txtId.setText(projekt.getProjektId().toString());
        TextField txtNazwa = new TextField();
        if (projekt.getNazwa() != null)
            txtNazwa.setText(projekt.getNazwa());
        TextArea txtOpis = new TextArea();
        txtOpis.setPrefRowCount(6);
        txtOpis.setPrefColumnCount(40);
        txtOpis.setWrapText(true);
        if (projekt.getOpis() != null)
            txtOpis.setText(projekt.getOpis());
        Label txtDataUtworzenia = new Label();
        if (projekt.getDataCzasUtworzenia() != null)
            txtDataUtworzenia.setText(dateTimeFormater.format(projekt.getDataCzasUtworzenia()));
        DatePicker dtDataOddania = new DatePicker();
        dtDataOddania.setPromptText("RRRR-MM-DD");
        dtDataOddania.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? dateFormatter.format(date) : null;
            }

            @Override
            public LocalDate fromString(String text) {
                return text == null || text.trim().isEmpty() ? null : LocalDate.parse(text, dateFormatter);
            }
        });
        dtDataOddania.getEditor().focusedProperty().addListener((obsValue, oldFocus, newFocus) -> {
            if (!newFocus) {
                try {
                    dtDataOddania.setValue(dtDataOddania.getConverter().fromString(
                            dtDataOddania.getEditor().getText()));
                } catch (DateTimeParseException e) {
                    dtDataOddania.getEditor().setText(dtDataOddania.getConverter()
                            .toString(dtDataOddania.getValue()));
                }
            }
        });
        if (projekt.getDataOddania() != null) {
            dtDataOddania.setValue(projekt.getDataOddania());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(5, 5, 5, 5));
        grid.add(lblId, 0, 0);
        grid.add(txtId, 1, 0);
        grid.add(lblDataCzasUtworzenia, 0, 1);
        grid.add(txtDataUtworzenia, 1, 1);
        grid.add(lblNazwa, 0, 2);
        grid.add(txtNazwa, 1, 2);
        grid.add(lblOpis, 0, 3);
        grid.add(txtOpis, 1, 3);
        grid.add(lblDataOddania, 0, 4);
        grid.add(dtDataOddania, 1, 4);

        dialog.getDialogPane().setContent(grid);
        ButtonType buttonTypeOk = new ButtonType("Zapisz", ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("Anuluj", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        dialog.setResultConverter(new Callback<ButtonType, Projekt>() {
            @Override
            public Projekt call(ButtonType butonType) {
                if (butonType == buttonTypeOk) {
                    projekt.setNazwa(txtNazwa.getText().trim());
                    projekt.setOpis(txtOpis.getText().trim());
                    projekt.setDataOddania(dtDataOddania.getValue());
                    return projekt;
                }
                return null;
            }
        });

        Optional<Projekt> result = dialog.showAndWait();
        if (result.isPresent()) {
            wykonawca.execute(() -> {
                try {
                    projektDAO.setProjekt(projekt);
                    Platform.runLater(() -> {
                        if (tblProjekt.getItems().contains(projekt)) {
                            tblProjekt.refresh();
                        } else {
                            tblProjekt.getItems().add(0, projekt);
                        }
                    });
                } catch (RuntimeException e) {
                    String errMsg = "Błąd podczas zapisywania danych projektu!";
                    logger.error(errMsg, e);
                    String errDetails = e.getCause() != null ?
                            e.getMessage() + "\n" + e.getCause().getMessage()
                            : e.getMessage();
                    Platform.runLater(() -> showError(errMsg, errDetails));
                }
            });
        }
    }

    private void usunProjekt(Projekt projekt) {
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Potwierdzenie");
        confirmAlert.setHeaderText("Usuwanie projektu");
        confirmAlert.setContentText("Czy na pewno chcesz usunąć projekt: " + projekt.getNazwa() + "?");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            wykonawca.execute(() -> {
                try {
                    projektDAO.deleteProjekt(projekt.getProjektId());
                    Platform.runLater(() -> {
                        projekty.remove(projekt);
                    });
                } catch (RuntimeException e) {
                    String errMsg = "Błąd podczas usuwania projektu!";
                    logger.error(errMsg, e);
                    String errDetails = e.getCause() != null ?
                            e.getMessage() + "\n" + e.getCause().getMessage()
                            : e.getMessage();
                    Platform.runLater(() -> showError(errMsg, errDetails));
                }
            });
        }
    }

    private Label getRightLabel(String text) {
        Label lbl = new Label(text);
        lbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lbl.setAlignment(Pos.CENTER_RIGHT);
        return lbl;
    }

    /** Metoda pomocnicza do prezentowania użytkownikowi informacji o błędach */
    private void showError(String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void shutdown() {
        if (wykonawca != null) {
            wykonawca.shutdown();
            try {
                if (!wykonawca.awaitTermination(5, TimeUnit.SECONDS))
                    wykonawca.shutdownNow();
            } catch (InterruptedException e) {
                wykonawca.shutdownNow();
            }
        }
    }
}
