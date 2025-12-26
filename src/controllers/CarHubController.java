package controllers;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Car;
import views.CarCardView;
import utils.SessionManager;
import utils.ExportUtil;
import utils.LoggerUtil;
import utils.NotificationUtil;
import services.FavoritesService;

import java.sql.*;
import java.io.File;
import java.util.Objects;

public class CarHubController {

    @FXML
    private TableView<Car> carTable;
    @FXML
    private TableColumn<Car, Void> colFavorite;
    @FXML
    private TableColumn<Car, String> colName;
    @FXML
    private TableColumn<Car, String> colModel;
    @FXML
    private TableColumn<Car, Double> colPrice;
    // Новые колонки (могут отсутствовать в некоторых FXML)
    @FXML
    private TableColumn<Car, String> colBrand;
    @FXML
    private TableColumn<Car, Integer> colYear;
    @FXML
    private TableColumn<Car, Integer> colMileage;
    @FXML
    private TextField searchField;
    @FXML
    private FlowPane cardsContainer; // Для карточного представления

    // Новые поля для сворачиваемых фильтров
    @FXML
    private ComboBox<String> filterBrand;
    @FXML
    private TextField filterYearFrom;
    @FXML
    private TextField filterYearTo;
    @FXML
    private TextField filterPriceFrom;
    @FXML
    private TextField filterPriceTo;
    @FXML
    private Label filterResultsLabel;

    // Старые фильтры (могут быть в других FXML)
    @FXML
    private ComboBox<String> brandFilter;
    @FXML
    private TextField minPriceField;
    @FXML
    private TextField maxPriceField;
    @FXML
    private ComboBox<Integer> minYearFilter;
    @FXML
    private ComboBox<Integer> maxYearFilter;
    @FXML
    private Button sortPriceBtn;
    @FXML
    private Button sortYearBtn;
    @FXML
    private Button sortMileageBtn;
    @FXML
    private Label countLabel;

    private final ObservableList<Car> carsList = FXCollections.observableArrayList();
    private FilteredList<Car> filteredCars;
    private Car selectedCar; // Выбранная карточка
    private static boolean isAdminMode = true; // По умолчанию админ режим

    // Состояние сортировки: 0 = нет, 1 = по возрастанию, -1 = по убыванию
    private int priceSortState = 0;
    private int yearSortState = 0;
    private int mileageSortState = 0;

    public static void setAdminMode(boolean isAdmin) {
        isAdminMode = isAdmin;
    }

    // Инициализация таблицы и фильтрации
    public void initialize() {
        loadCarsFromDatabase();

        // Инициализация фильтров
        initializeFilters();

        // Если есть таблица - инициализируем табличное представление
        if (carTable != null) {
            initializeTableView();
        }

        // Если есть карточки - инициализируем карточное представление
        if (cardsContainer != null) {
            initializeCardView();
        }

        // Обновляем счётчик
        updateCount();
    }

    private void initializeFilters() {
        // Инициализация FilteredList
        filteredCars = new FilteredList<>(carsList, c -> true);

        // Фильтр по брендам (НОВЫЙ)
        if (filterBrand != null) {
            ObservableList<String> brands = FXCollections.observableArrayList("Все бренды");
            carsList.stream()
                .map(Car::getBrand)
                .filter(b -> b != null && !b.isBlank())
                .distinct()
                .sorted()
                .forEach(brands::add);
            filterBrand.setItems(brands);
            filterBrand.setValue("Все бренды");
        }

        // Фильтр по брендам (СТАРЫЙ)
        if (brandFilter != null) {
            ObservableList<String> brands = FXCollections.observableArrayList("Все бренды");
            carsList.stream()
                .map(Car::getBrand)
                .filter(b -> b != null && !b.isBlank())
                .distinct()
                .sorted()
                .forEach(brands::add);
            brandFilter.setItems(brands);
            brandFilter.setValue("Все бренды");
        }

        // Фильтр по годам
        if (minYearFilter != null && maxYearFilter != null) {
            ObservableList<Integer> years = FXCollections.observableArrayList();
            years.add(null); // "Любой"
            carsList.stream()
                .map(Car::getYear)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .forEach(years::add);
            minYearFilter.setItems(years);
            maxYearFilter.setItems(years);
        }
    }

    private void initializeTableView() {
        // Колонка избранного с кнопкой
        if (colFavorite != null) {
            colFavorite.setCellFactory(param -> new TableCell<>() {
                private final Button favoriteBtn = new Button();

                {
                    favoriteBtn.setStyle(
                        "-fx-background-color: transparent; " +
                        "-fx-font-size: 18px; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 5;"
                    );
                    favoriteBtn.setOnAction(event -> {
                        Car car = getTableView().getItems().get(getIndex());
                        toggleFavorite(car, favoriteBtn);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getIndex() >= getTableView().getItems().size()) {
                        setGraphic(null);
                    } else {
                        Car car = getTableView().getItems().get(getIndex());
                        updateFavoriteButton(car, favoriteBtn);
                        setGraphic(favoriteBtn);
                        setAlignment(Pos.CENTER);
                    }
                }
            });
        }

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Форматирование цены в таблице
        colPrice.setCellFactory(column -> new TableCell<Car, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(utils.PriceFormatter.format(price));
                }
                setAlignment(Pos.CENTER_RIGHT);
            }
        });

        if (colBrand != null) colBrand.setCellValueFactory(new PropertyValueFactory<>("brand"));
        if (colYear != null) colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        if (colMileage != null) colMileage.setCellValueFactory(new PropertyValueFactory<>("mileage"));

        // Фильтрация
        FilteredList<Car> filtered = new FilteredList<>(carsList, c -> true);
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> {
                String q = newV == null ? "" : newV.trim().toLowerCase();
                filtered.setPredicate(car -> filterCar(car, q));
            });
        }
        SortedList<Car> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(carTable.comparatorProperty());
        carTable.setItems(sorted);

        // Открытие деталей по двойному клику + контекстное меню
        carTable.setRowFactory(tv -> {
            TableRow<Car> row = new TableRow<>();

            // Двойной клик - детали
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openDetails(row.getItem());
                }
            });

            // Контекстное меню по правой кнопке
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    ContextMenu contextMenu = createTableContextMenu(row.getItem());
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });

            return row;
        });
    }

    private void initializeCardView() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> {
                applyFilters();
            });
        }

        updateCardsView(filteredCars);
    }

    private boolean filterCar(Car car, String query) {
        if (query.isEmpty()) return true;
        String brand = car.getBrand() == null ? "" : car.getBrand().toLowerCase();
        return car.getName().toLowerCase().contains(query)
                || car.getModel().toLowerCase().contains(query)
                || brand.contains(query)
                || String.valueOf(car.getPrice()).contains(query);
    }

    private void updateCardsView(FilteredList<Car> filteredCars) {
        cardsContainer.getChildren().clear();
        for (Car car : filteredCars) {
            CarCardView card = new CarCardView(car);

            // Клик и выделение
            card.setOnMouseClicked(e -> {
                if (e.getClickCount() == 1) {
                    selectedCar = car;
                    // Подсветка выбранной карточки
                    cardsContainer.getChildren().forEach(node -> {
                        if (node instanceof CarCardView) {
                            CarCardView c = (CarCardView) node;
                            if (c.getCar().getId() == selectedCar.getId()) {
                                c.setStyle("-fx-background-color: #E3F2FD; " +
                                        "-fx-background-radius: 10; " +
                                        "-fx-effect: dropshadow(gaussian, rgba(33,150,243,0.5), 15, 0, 0, 4); " +
                                        "-fx-cursor: hand;");
                            }
                        }
                    });
                } else if (e.getClickCount() == 2) {
                    openDetails(car);
                }
            });

            // Контекстное меню по правой кнопке
            card.setOnContextMenuRequested(event -> {
                ContextMenu contextMenu = createCarContextMenu(car, card);
                contextMenu.show(card, event.getScreenX(), event.getScreenY());
            });

            cardsContainer.getChildren().add(card);
        }
    }

    // Применение всех фильтров (УНИВЕРСАЛЬНЫЙ МЕТОД)
    @FXML
    protected void applyFilters() {
        try {
            // Парсим значения новых фильтров
            String selectedBrand = (filterBrand != null && filterBrand.getValue() != null)
                ? filterBrand.getValue() : null;

            Integer yearFrom = null;
            Integer yearTo = null;
            Double priceFrom = null;
            Double priceTo = null;

            // Парсим год от
            if (filterYearFrom != null && !filterYearFrom.getText().trim().isEmpty()) {
                try {
                    yearFrom = Integer.parseInt(filterYearFrom.getText().trim());
                } catch (NumberFormatException e) {
                    NotificationUtil.showWarning("Неверный формат года (от)");
                    return;
                }
            }

            // Парсим год до
            if (filterYearTo != null && !filterYearTo.getText().trim().isEmpty()) {
                try {
                    yearTo = Integer.parseInt(filterYearTo.getText().trim());
                } catch (NumberFormatException e) {
                    NotificationUtil.showWarning("Неверный формат года (до)");
                    return;
                }
            }

            // Парсим цену от
            if (filterPriceFrom != null && !filterPriceFrom.getText().trim().isEmpty()) {
                try {
                    priceFrom = Double.parseDouble(filterPriceFrom.getText().trim());
                } catch (NumberFormatException e) {
                    NotificationUtil.showWarning("Неверный формат цены (от)");
                    return;
                }
            }

            // Парсим цену до
            if (filterPriceTo != null && !filterPriceTo.getText().trim().isEmpty()) {
                try {
                    priceTo = Double.parseDouble(filterPriceTo.getText().trim());
                } catch (NumberFormatException e) {
                    NotificationUtil.showWarning("Неверный формат цены (до)");
                    return;
                }
            }

            // Финальные значения для лямбды
            final String brand = selectedBrand;
            final Integer minYear = yearFrom;
            final Integer maxYear = yearTo;
            final Double minPrice = priceFrom;
            final Double maxPrice = priceTo;

            // Применяем фильтры
            filteredCars.setPredicate(car -> {
                // Поиск
                String query = searchField != null ? searchField.getText() : "";
                if (!query.isBlank()) {
                    String q = query.trim().toLowerCase();
                    String carBrand = car.getBrand() == null ? "" : car.getBrand().toLowerCase();
                    boolean matchesSearch = car.getName().toLowerCase().contains(q)
                            || car.getModel().toLowerCase().contains(q)
                            || carBrand.contains(q)
                            || String.valueOf(car.getPrice()).contains(q);
                    if (!matchesSearch) return false;
                }

                // Фильтр по бренду (новый)
                if (brand != null && !"Все бренды".equals(brand)) {
                    if (car.getBrand() == null || !car.getBrand().equalsIgnoreCase(brand)) {
                        return false;
                    }
                }

                // Фильтр по бренду (старый)
                if (brandFilter != null && brandFilter.getValue() != null) {
                    String selectedBrandOld = brandFilter.getValue();
                    if (!"Все бренды".equals(selectedBrandOld)) {
                        if (car.getBrand() == null || !car.getBrand().equals(selectedBrandOld)) {
                            return false;
                        }
                    }
                }

                // Фильтр по году (новый)
                if (minYear != null && car.getYear() != null) {
                    if (car.getYear() < minYear) return false;
                }
                if (maxYear != null && car.getYear() != null) {
                    if (car.getYear() > maxYear) return false;
                }

                // Фильтр по году (старый)
                if (minYearFilter != null && minYearFilter.getValue() != null) {
                    Integer minYearOld = minYearFilter.getValue();
                    if (car.getYear() == null || car.getYear() < minYearOld) return false;
                }
                if (maxYearFilter != null && maxYearFilter.getValue() != null) {
                    Integer maxYearOld = maxYearFilter.getValue();
                    if (car.getYear() == null || car.getYear() > maxYearOld) return false;
                }

                // Фильтр по цене (новый)
                if (minPrice != null) {
                    if (car.getPrice() < minPrice) return false;
                }
                if (maxPrice != null) {
                    if (car.getPrice() > maxPrice) return false;
                }

                // Фильтр по цене (старый)
                if (minPriceField != null && !minPriceField.getText().isBlank()) {
                    try {
                        double minPriceOld = Double.parseDouble(minPriceField.getText());
                        if (car.getPrice() < minPriceOld) return false;
                    } catch (NumberFormatException ignored) {}
                }
                if (maxPriceField != null && !maxPriceField.getText().isBlank()) {
                    try {
                        double maxPriceOld = Double.parseDouble(maxPriceField.getText());
                        if (car.getPrice() > maxPriceOld) return false;
                    } catch (NumberFormatException ignored) {}
                }

                return true;
            });

            // Применяем сортировку
            applySorting();

            // Обновляем представление
            if (cardsContainer != null) {
                updateCardsView(filteredCars);
            }

            // Обновляем счётчик
            updateFilterResults();
            updateCount();

            // Уведомление (только если есть новые фильтры)
            if (filterBrand != null || filterYearFrom != null || filterPriceFrom != null) {
                LoggerUtil.action("Применены фильтры");
            }

        } catch (Exception e) {
            LoggerUtil.error("Ошибка применения фильтров", e);
            NotificationUtil.showError("Ошибка применения фильтров");
        }
    }

    // Сортировка
    @FXML
    protected void sortByPrice() {
        resetOtherSorts("price");
        priceSortState = (priceSortState + 2) % 3 - 1; // Цикл: 0 -> 1 -> -1 -> 0
        if (sortPriceBtn != null) {
            sortPriceBtn.setText(priceSortState == 1 ? "По цене ↑" : priceSortState == -1 ? "По цене ↓" : "По цене");
        }
        applySorting();
    }

    @FXML
    protected void sortByYear() {
        resetOtherSorts("year");
        yearSortState = (yearSortState + 2) % 3 - 1;
        if (sortYearBtn != null) {
            sortYearBtn.setText(yearSortState == 1 ? "По году ↑" : yearSortState == -1 ? "По году ↓" : "По году");
        }
        applySorting();
    }

    @FXML
    protected void sortByMileage() {
        resetOtherSorts("mileage");
        mileageSortState = (mileageSortState + 2) % 3 - 1;
        if (sortMileageBtn != null) {
            sortMileageBtn.setText(mileageSortState == 1 ? "По пробегу ↑" : mileageSortState == -1 ? "По пробегу ↓" : "По пробегу");
        }
        applySorting();
    }

    @FXML
    protected void resetSort() {
        priceSortState = yearSortState = mileageSortState = 0;
        if (sortPriceBtn != null) sortPriceBtn.setText("По цене ↑");
        if (sortYearBtn != null) sortYearBtn.setText("По году ↑");
        if (sortMileageBtn != null) sortMileageBtn.setText("По пробегу ↑");
        applySorting();
    }

    private void resetOtherSorts(String keepSort) {
        if (!"price".equals(keepSort)) priceSortState = 0;
        if (!"year".equals(keepSort)) yearSortState = 0;
        if (!"mileage".equals(keepSort)) mileageSortState = 0;

        if (sortPriceBtn != null && !"price".equals(keepSort)) sortPriceBtn.setText("По цене ↑");
        if (sortYearBtn != null && !"year".equals(keepSort)) sortYearBtn.setText("По году ↑");
        if (sortMileageBtn != null && !"mileage".equals(keepSort)) sortMileageBtn.setText("По пробегу ↑");
    }

    private void applySorting() {
        ObservableList<Car> sortedList = FXCollections.observableArrayList(filteredCars);

        if (priceSortState != 0) {
            sortedList.sort((c1, c2) -> priceSortState * Double.compare(c1.getPrice(), c2.getPrice()));
        } else if (yearSortState != 0) {
            sortedList.sort((c1, c2) -> {
                Integer y1 = c1.getYear() != null ? c1.getYear() : 0;
                Integer y2 = c2.getYear() != null ? c2.getYear() : 0;
                return yearSortState * y1.compareTo(y2);
            });
        } else if (mileageSortState != 0) {
            sortedList.sort((c1, c2) -> {
                Integer m1 = c1.getMileage() != null ? c1.getMileage() : 0;
                Integer m2 = c2.getMileage() != null ? c2.getMileage() : 0;
                return mileageSortState * m1.compareTo(m2);
            });
        }

        if (cardsContainer != null) {
            updateCardsViewFromList(sortedList);
        }
    }

    @FXML
    protected void clearFilters() {
        if (filterBrand != null) filterBrand.setValue(null);
        if (filterYearFrom != null) filterYearFrom.clear();
        if (filterYearTo != null) filterYearTo.clear();
        if (filterPriceFrom != null) filterPriceFrom.clear();
        if (filterPriceTo != null) filterPriceTo.clear();
        applyFilters();
    }

    // ========== МЕТОДЫ ДЛЯ НОВОЙ ПАНЕЛИ ФИЛЬТРОВ ==========


    @FXML
    protected void resetFilters() {
        try {
            // Очищаем поля фильтров
            if (filterBrand != null) {
                filterBrand.setValue("Все бренды");
            }
            if (filterYearFrom != null) {
                filterYearFrom.clear();
            }
            if (filterYearTo != null) {
                filterYearTo.clear();
            }
            if (filterPriceFrom != null) {
                filterPriceFrom.clear();
            }
            if (filterPriceTo != null) {
                filterPriceTo.clear();
            }

            // Сбрасываем фильтр
            filteredCars.setPredicate(car -> true);

            // Обновляем счётчик
            updateFilterResults();

            NotificationUtil.showInfo("Фильтры сброшены");
            LoggerUtil.action("Фильтры сброшены");

        } catch (Exception e) {
            LoggerUtil.error("Ошибка сброса фильтров", e);
        }
    }

    private void updateFilterResults() {
        int count = filteredCars.size();
        if (filterResultsLabel != null) {
            filterResultsLabel.setText("Найдено автомобилей: " + count);
        }
        updateCount();
    }

    private void updateCount() {
        if (countLabel != null) {
            countLabel.setText("Всего: " + filteredCars.size());
        }
    }

    private void updateCardsViewFromList(ObservableList<Car> cars) {
        cardsContainer.getChildren().clear();
        for (Car car : cars) {
            CarCardView card = new CarCardView(car);

            // Клик и выделение
            card.setOnMouseClicked(e -> {
                if (e.getClickCount() == 1) {
                    selectedCar = car;
                    cardsContainer.getChildren().forEach(node -> {
                        if (node instanceof CarCardView) {
                            CarCardView c = (CarCardView) node;
                            if (c.getCar().getId() == selectedCar.getId()) {
                                c.setStyle("-fx-background-color: #E3F2FD; " +
                                        "-fx-background-radius: 10; " +
                                        "-fx-effect: dropshadow(gaussian, rgba(33,150,243,0.5), 15, 0, 0, 4); " +
                                        "-fx-cursor: hand;");
                            }
                        }
                    });
                } else if (e.getClickCount() == 2) {
                    openDetails(car);
                }
            });

            // Контекстное меню по правой кнопке
            card.setOnContextMenuRequested(event -> {
                ContextMenu contextMenu = createCarContextMenu(car, card);
                contextMenu.show(card, event.getScreenX(), event.getScreenY());
            });

            cardsContainer.getChildren().add(card);
        }
    }

    // Переключение между представлениями
    @FXML
    protected void switchToTableView() {
        try {
            Stage stage = (Stage) (cardsContainer != null ? cardsContainer.getScene().getWindow() :
                                   searchField.getScene().getWindow());
            String fxmlFile = isAdminMode ? "/resources/carhub-admin-view.fxml" : "/resources/carhub-user-view.fxml";
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            stage.setScene(new Scene(root));
            stage.setTitle(isAdminMode ? "CarHub — Панель администратора (Таблица)" : "CarHub — Каталог (Таблица)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void switchToCardView() {
        try {
            Stage stage = (Stage) (carTable != null ? carTable.getScene().getWindow() :
                                   searchField.getScene().getWindow());
            String fxmlFile = isAdminMode ? "/resources/carhub-admin-cards-filtered.fxml" : "/resources/carhub-user-cards-filtered.fxml";
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            stage.setScene(new Scene(root));
            stage.setTitle(isAdminMode ? "CarHub — Панель администратора (Карточки)" : "CarHub — Каталог (Карточки)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void openFavorites() {
        if (!SessionManager.isLoggedIn()) {
            new Alert(Alert.AlertType.WARNING, "Войдите в систему для просмотра избранного!").show();
            return;
        }

        try {
            Stage stage = (Stage) (carTable != null ? carTable.getScene().getWindow() :
                                   cardsContainer != null ? cardsContainer.getScene().getWindow() :
                                   searchField.getScene().getWindow());
            Parent root = FXMLLoader.load(getClass().getResource("/resources/favorites-view.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("CarHub — Избранное");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void openStatistics() {
        if (!SessionManager.isAdmin()) {
            new Alert(Alert.AlertType.WARNING, "Доступ запрещён!").show();
            return;
        }

        try {
            Stage stage = (Stage) (carTable != null ? carTable.getScene().getWindow() :
                                   cardsContainer != null ? cardsContainer.getScene().getWindow() :
                                   searchField.getScene().getWindow());
            Parent root = FXMLLoader.load(getClass().getResource("/resources/statistics-view.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("CarHub — Статистика");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void openOrders() {
        if (!SessionManager.isAdmin()) {
            new Alert(Alert.AlertType.WARNING, "Доступ запрещён!").show();
            return;
        }

        try {
            LoggerUtil.action("Открыта панель управления заявками");

            Stage stage = (Stage) (carTable != null ? carTable.getScene().getWindow() :
                                   cardsContainer != null ? cardsContainer.getScene().getWindow() :
                                   searchField.getScene().getWindow());
            Parent root = FXMLLoader.load(getClass().getResource("/resources/admin-orders-view.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("CarHub — Управление заявками");
            stage.setMaximized(true);
        } catch (Exception e) {
            LoggerUtil.error("Ошибка открытия панели заявок", e);
            e.printStackTrace();
        }
    }

    @FXML
    protected void openUsersManagement() {
        if (!SessionManager.isAdmin()) {
            new Alert(Alert.AlertType.WARNING, "Доступ запрещён!").show();
            return;
        }

        try {
            LoggerUtil.action("Открыт экран управления пользователями");

            Stage stage = (Stage) (carTable != null ? carTable.getScene().getWindow() :
                                   cardsContainer != null ? cardsContainer.getScene().getWindow() :
                                   searchField.getScene().getWindow());
            Parent root = FXMLLoader.load(getClass().getResource("/resources/users-management-view.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("CarHub — Управление пользователями");
        } catch (Exception e) {
            LoggerUtil.error("Ошибка открытия управления пользователями", e);
            e.printStackTrace();
        }
    }

    @FXML
    protected void openProfile() {
        try {
            LoggerUtil.action("Открыт личный кабинет: " + SessionManager.getCurrentUsername());

            Stage stage = (Stage) (carTable != null ? carTable.getScene().getWindow() :
                                   cardsContainer != null ? cardsContainer.getScene().getWindow() :
                                   searchField.getScene().getWindow());
            Parent root = FXMLLoader.load(getClass().getResource("/resources/profile-view.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("CarHub — Личный кабинет");

            // Фиксированный размер
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.centerOnScreen();
        } catch (Exception e) {
            LoggerUtil.error("Ошибка открытия личного кабинета", e);
            e.printStackTrace();
        }
    }

    private void openDetails(Car car) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/car_details.fxml"));
            Parent root = loader.load();
            CarDetailsController controller = loader.getController();
            controller.setCar(car);

            Stage stage = new Stage();
            stage.setTitle("Информация об автомобиле");

            Scene scene = new Scene(root, 900, 700); // Фиксированный размер
            stage.setScene(scene);

            // ✅ ЦЕНТРАЛИЗАЦИЯ ОКНА
            stage.centerOnScreen();

            // Минимальный размер
            stage.setMinWidth(800);
            stage.setMinHeight(600);

            stage.show();

            // ✅ СБРОС СКРОЛЛА НАВЕРХ
            javafx.application.Platform.runLater(() -> {
                ScrollPane sp = (ScrollPane) scene.getRoot();
                if (sp != null) {
                    sp.setVvalue(0); // Скролл на самый верх
                }
            });

            LoggerUtil.action("Открыты детали автомобиля: " + car.getName());
        } catch (Exception e) {
            LoggerUtil.error("Ошибка открытия деталей автомобиля", e);
            NotificationUtil.showError("Ошибка открытия деталей автомобиля");
        }
    }

    // Загрузка всех авто из базы
    private void loadCarsFromDatabase() {
        carsList.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                new Alert(Alert.AlertType.ERROR, "Нет подключения к базе данных").show();
                return;
            }
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Cars");
            ResultSetMetaData meta = rs.getMetaData();
            boolean hasBrand = false, hasYear = false, hasMileage = false, hasDesc = false, hasImage = false;
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                String col = meta.getColumnLabel(i);
                if (col.equalsIgnoreCase("brand")) hasBrand = true;
                if (col.equalsIgnoreCase("year")) hasYear = true;
                if (col.equalsIgnoreCase("mileage")) hasMileage = true;
                if (col.equalsIgnoreCase("description")) hasDesc = true;
                if (col.equalsIgnoreCase("imageUrl") || col.equalsIgnoreCase("image_url")) hasImage = true;
            }
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String model = rs.getString("model");
                double price = rs.getDouble("price");
                if (hasBrand || hasYear || hasMileage || hasDesc || hasImage) {
                    String brand = hasBrand ? rs.getString("brand") : null;
                    Integer year = hasYear ? (Integer) rs.getObject("year") : null;
                    Integer mileage = hasMileage ? (Integer) rs.getObject("mileage") : null;
                    String description = hasDesc ? rs.getString("description") : null;
                    String imageUrl = null;
                    if (hasImage) {
                        if (hasColumn(meta, "imageUrl")) imageUrl = rs.getString("imageUrl");
                        else if (hasColumn(meta, "image_url")) imageUrl = rs.getString("image_url");
                    }

                    // ✅ ЗАГРУЖАЕМ ГЛАВНОЕ ФОТО ИЗ ТАБЛИЦЫ CarImages
                    String mainImageUrl = getMainImageUrl(conn, id);
                    if (mainImageUrl != null && !mainImageUrl.isEmpty()) {
                        imageUrl = mainImageUrl;
                    }

                    carsList.add(new Car(id, name, model, price, brand, year, mileage, description, imageUrl));
                } else {
                    carsList.add(new Car(id, name, model, price));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Получает URL главной фотографии автомобиля из таблицы CarImages
     */
    private String getMainImageUrl(Connection conn, int carId) {
        try {
            String sql = "SELECT image_url FROM CarImages WHERE car_id = ? AND is_main = 1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, carId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("image_url");
            }
        } catch (SQLException e) {
            // Если таблицы CarImages нет - пропускаем
        }
        return null;
    }

    private boolean hasColumn(ResultSetMetaData meta, String column) throws SQLException {
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (meta.getColumnLabel(i).equalsIgnoreCase(column)) return true;
        }
        return false;
    }

    // Добавить авто (отдельное окно)
    @FXML
    protected void addCar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/add_car.fxml"));
            Parent root = loader.load();
            CarFormController controller = loader.getController();
            controller.setOnSaveCallback(() -> {
                loadCarsFromDatabase();
                initializeFilters();
                applyFilters();
            });

            Stage stage = new Stage();
            stage.setTitle("Добавить автомобиль");

            Scene scene = new Scene(root, 850, 750); // Фиксированный размер
            stage.setScene(scene);

            // ✅ ЦЕНТРАЛИЗАЦИЯ ОКНА
            stage.centerOnScreen();

            // Минимальный размер
            stage.setMinWidth(750);
            stage.setMinHeight(650);

            stage.show();

            LoggerUtil.action("Открыто окно добавления автомобиля");
        } catch (Exception e) {
            LoggerUtil.error("Ошибка открытия формы добавления", e);
            NotificationUtil.showError("Ошибка открытия формы добавления");
        }
    }

    // Редактировать авто
    @FXML
    protected void editCar() {
        Car selected = null;
        if (carTable != null) {
            selected = carTable.getSelectionModel().getSelectedItem();
        } else if (selectedCar != null) {
            selected = selectedCar;
        }

        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Выберите автомобиль для редактирования!").show();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/edit_car.fxml"));
            Parent root = loader.load();
            CarFormController controller = loader.getController();
            controller.setCar(selected);
            controller.setOnSaveCallback(this::loadCarsFromDatabase);

            Stage stage = new Stage();
            stage.setTitle("Редактировать автомобиль");

            Scene scene = new Scene(root, 850, 750); // Фиксированный размер
            stage.setScene(scene);

            // ✅ ЦЕНТРАЛИЗАЦИЯ ОКНА
            stage.centerOnScreen();

            // Минимальный размер
            stage.setMinWidth(750);
            stage.setMinHeight(650);

            stage.show();

            LoggerUtil.action("Открыто окно редактирования автомобиля: " + selected.getName());
        } catch (Exception e) {
            LoggerUtil.error("Ошибка открытия формы редактирования", e);
            NotificationUtil.showError("Ошибка открытия формы редактирования");
        }
    }

    // Удалить авто
    @FXML
    protected void deleteCar() {
        Car selected = null;
        if (carTable != null) {
            selected = carTable.getSelectionModel().getSelectedItem();
        } else if (selectedCar != null) {
            selected = selectedCar;
        }

        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Выберите автомобиль для удаления!").show();
            return;
        }

        // ✅ УЛУЧШЕННОЕ ПОДТВЕРЖДЕНИЕ УДАЛЕНИЯ
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Вы уверены, что хотите удалить этот автомобиль?");
        confirm.setContentText(String.format(
            "🚗 %s %s\n💰 Цена: %.2f ₸\n📅 Год: %s",
            selected.getName(),
            selected.getModel(),
            selected.getPrice(),
            selected.getYear() != null ? selected.getYear() : "не указан"
        ));

        ButtonType btnYes = new ButtonType("Да, удалить", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNo = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnYes, btnNo);

        if (confirm.showAndWait().orElse(btnNo) != btnYes) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                new Alert(Alert.AlertType.ERROR, "❌ Нет подключения к базе данных").show();
                return;
            }

            // Сначала удаляем из избранного (если есть таблица Favorites)
            try {
                String sqlFav = "DELETE FROM Favorites WHERE car_id = ?";
                PreparedStatement stmtFav = conn.prepareStatement(sqlFav);
                stmtFav.setInt(1, selected.getId());
                stmtFav.executeUpdate();
            } catch (Exception ignored) {
                // Таблица Favorites может не существовать
            }

            // Удаляем автомобиль
            String sql = "DELETE FROM Cars WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selected.getId());
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Успешно");
                success.setHeaderText(null);
                success.setContentText("✅ Автомобиль успешно удалён!");
                success.showAndWait();
            }

            loadCarsFromDatabase();
            initializeFilters();
            applyFilters();
        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Ошибка");
            error.setHeaderText("Не удалось удалить автомобиль");
            error.setContentText("Причина: " + e.getMessage());
            error.showAndWait();
            e.printStackTrace();
        }
    }

    // ✅ ЭКСПОРТ ДАННЫХ В CSV
    @FXML
    protected void exportToCSV() {
        if (filteredCars.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Нет данных для экспорта!").show();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить список автомобилей");
        fileChooser.setInitialFileName("carhub_cars_" + System.currentTimeMillis() + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV файл", "*.csv")
        );

        File file = fileChooser.showSaveDialog(
            carTable != null ? carTable.getScene().getWindow() :
            cardsContainer != null ? cardsContainer.getScene().getWindow() :
            searchField.getScene().getWindow()
        );

        if (file != null) {
            try {
                ExportUtil.exportCarsToCSV(filteredCars, file);

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Экспорт завершён");
                success.setHeaderText("✅ Данные успешно экспортированы!");
                success.setContentText(String.format(
                    "Файл: %s\nЭкспортировано записей: %d",
                    file.getName(),
                    filteredCars.size()
                ));
                success.showAndWait();
            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Ошибка экспорта");
                error.setHeaderText("Не удалось экспортировать данные");
                error.setContentText("Причина: " + e.getMessage());
                error.showAndWait();
                e.printStackTrace();
            }
        }
    }

    // Выход в экран логина
    @FXML
    protected void logout() {
        try {
            // ✅ ЛОГИРУЕМ ВЫХОД
            String username = SessionManager.getCurrentUsername();
            LoggerUtil.logLogout(username != null ? username : "Неизвестный");

            // Очищаем сессию
            SessionManager.logout();

            Parent root = FXMLLoader.load(getClass().getResource("/resources/login-view.fxml"));
            Stage stage = (Stage) (carTable != null ? carTable.getScene().getWindow() :
                                   cardsContainer != null ? cardsContainer.getScene().getWindow() :
                                   searchField.getScene().getWindow());
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("CarHub — Вход");

            // Фиксированный размер
            stage.setWidth(600);
            stage.setHeight(700);
            stage.centerOnScreen();
            stage.setTitle("CarHub — Вход");
            stage.show();
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при выходе", e);
        }
    }

    // ========== КОНТЕКСТНОЕ МЕНЮ ==========

    /**
     * Создание контекстного меню для карточки автомобиля
     */
    private ContextMenu createCarContextMenu(Car car, javafx.scene.Node targetNode) {
        ContextMenu contextMenu = new ContextMenu();

        // 👁 Посмотреть детали
        MenuItem viewDetails = new MenuItem("👁 Посмотреть детали");
        viewDetails.setOnAction(e -> openDetails(car));

        // ❤ Добавить/Убрать из избранного
        MenuItem toggleFav = new MenuItem();
        if (SessionManager.isLoggedIn()) {
            boolean isFav = FavoritesService.isFavorite(SessionManager.getCurrentUserId(), car.getId());
            toggleFav.setText(isFav ? "💔 Убрать из избранного" : "❤ Добавить в избранное");
            toggleFav.setOnAction(e -> {
                toggleFavoriteFromContext(car);
                // Обновляем представление
                if (cardsContainer != null) {
                    updateCardsView(filteredCars);
                }
                if (carTable != null) {
                    carTable.refresh();
                }
            });
        } else {
            toggleFav.setText("❤ Войдите для избранного");
            toggleFav.setDisable(true);
        }

        // 📅 Забронировать
        MenuItem reserve = new MenuItem("📅 Забронировать");
        reserve.setOnAction(e -> {
            // Открываем детали и показываем форму бронирования
            openDetailsAndShowReservation(car);
        });

        // 💳 Купить
        MenuItem purchase = new MenuItem("💳 Купить");
        purchase.setOnAction(e -> {
            // Открываем детали и показываем форму покупки
            openDetailsAndShowPurchase(car);
        });

        // 📋 Копировать информацию
        MenuItem copyInfo = new MenuItem("📋 Копировать информацию");
        copyInfo.setOnAction(e -> {
            String info = String.format("%s %s\nГод: %d\nЦена: %s₸\nПробег: %d км",
                car.getBrand(), car.getModel(), car.getYear(),
                utils.PriceFormatter.format(car.getPrice()), car.getMileage());

            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(info);
            clipboard.setContent(content);

            NotificationUtil.showSuccess("Информация скопирована в буфер обмена");
            LoggerUtil.action("Скопирована информация о: " + car.getName());
        });

        // Добавляем пункты меню
        contextMenu.getItems().addAll(viewDetails, toggleFav);

        // Разделитель
        contextMenu.getItems().add(new SeparatorMenuItem());

        contextMenu.getItems().addAll(reserve, purchase);

        // Разделитель
        contextMenu.getItems().add(new SeparatorMenuItem());

        contextMenu.getItems().add(copyInfo);

        // Только для админа - редактирование и удаление
        if (isAdminMode) {
            contextMenu.getItems().add(new SeparatorMenuItem());

            // ✏ Редактировать
            MenuItem edit = new MenuItem("✏ Редактировать");
            edit.setOnAction(e -> editCarFromContext(car));

            // 🗑 Удалить
            MenuItem delete = new MenuItem("🗑 Удалить");
            delete.setStyle("-fx-text-fill: #F44336;");
            delete.setOnAction(e -> deleteCarFromContext(car));

            contextMenu.getItems().addAll(edit, delete);
        }

        return contextMenu;
    }

    /**
     * Создание контекстного меню для строки таблицы
     */
    private ContextMenu createTableContextMenu(Car car) {
        return createCarContextMenu(car, carTable);
    }

    // Вспомогательные методы для действий из контекстного меню

    private void toggleFavoriteFromContext(Car car) {
        if (!SessionManager.isLoggedIn()) {
            NotificationUtil.showWarning("Войдите в систему для управления избранным");
            return;
        }

        int userId = SessionManager.getCurrentUserId();
        boolean isFavorite = FavoritesService.isFavorite(userId, car.getId());

        if (isFavorite) {
            FavoritesService.removeFromFavorites(userId, car.getId());
            NotificationUtil.showSuccess("Убрано из избранного");
        } else {
            FavoritesService.addToFavorites(userId, car.getId());
            NotificationUtil.showSuccess("Добавлено в избранное");
        }
    }

    private void editCarFromContext(Car car) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/edit_car.fxml"));
            Parent root = loader.load();
            CarFormController controller = loader.getController();
            controller.setCar(car);
            controller.setOnSaveCallback(() -> {
                loadCarsFromDatabase();
                initializeFilters();
                applyFilters();
            });

            Stage stage = new Stage();
            stage.setTitle("Редактировать автомобиль");
            Scene scene = new Scene(root, 850, 750);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setMinWidth(750);
            stage.setMinHeight(650);
            stage.show();

            LoggerUtil.action("Открыто окно редактирования: " + car.getName());
        } catch (Exception e) {
            LoggerUtil.error("Ошибка открытия формы редактирования", e);
            NotificationUtil.showError("Не удалось открыть форму редактирования");
        }
    }

    private void deleteCarFromContext(Car car) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Удаление автомобиля");
        confirm.setHeaderText("Вы уверены?");
        confirm.setContentText("Удалить " + car.getName() + " " + car.getModel() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "DELETE FROM Cars WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, car.getId());
                    stmt.executeUpdate();

                    NotificationUtil.showSuccess("Автомобиль удалён");
                    LoggerUtil.action("Удалён автомобиль: " + car.getName());

                    loadCarsFromDatabase();
                    initializeFilters();
                    applyFilters();
                } catch (SQLException e) {
                    LoggerUtil.error("Ошибка удаления автомобиля", e);
                    NotificationUtil.showError("Не удалось удалить автомобиль: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Открыть детали автомобиля и показать форму бронирования
     */
    private void openDetailsAndShowReservation(Car car) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/car_details.fxml"));
            Parent root = loader.load();
            CarDetailsController controller = loader.getController();
            controller.setCar(car);

            Stage stage = new Stage();
            stage.setTitle("Бронирование: " + car.getName());
            Scene scene = new Scene(root, 900, 700);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.show();

            // После открытия окна - программно открываем форму бронирования
            javafx.application.Platform.runLater(() -> {
                try {
                    // Используем рефлексию для вызова приватного метода openReservationForm
                    java.lang.reflect.Method method = controller.getClass().getDeclaredMethod("openReservationForm");
                    method.setAccessible(true);
                    method.invoke(controller);
                } catch (Exception e) {
                    LoggerUtil.error("Ошибка открытия формы бронирования", e);
                    NotificationUtil.showInfo("Нажмите кнопку 'Забронировать' для оформления");
                }
            });

            LoggerUtil.action("Открыто окно бронирования: " + car.getName());
        } catch (Exception e) {
            LoggerUtil.error("Ошибка открытия деталей для бронирования", e);
            NotificationUtil.showError("Не удалось открыть форму бронирования");
        }
    }

    /**
     * Открыть детали автомобиля и показать форму покупки
     */
    private void openDetailsAndShowPurchase(Car car) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/car_details.fxml"));
            Parent root = loader.load();
            CarDetailsController controller = loader.getController();
            controller.setCar(car);

            Stage stage = new Stage();
            stage.setTitle("Покупка: " + car.getName());
            Scene scene = new Scene(root, 900, 700);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.show();

            // После открытия окна - программно открываем форму покупки
            javafx.application.Platform.runLater(() -> {
                try {
                    // Используем рефлексию для вызова приватного метода openPurchaseForm
                    java.lang.reflect.Method method = controller.getClass().getDeclaredMethod("openPurchaseForm");
                    method.setAccessible(true);
                    method.invoke(controller);
                } catch (Exception e) {
                    LoggerUtil.error("Ошибка открытия формы покупки", e);
                    NotificationUtil.showInfo("Нажмите кнопку 'Купить' для оформления");
                }
            });

            LoggerUtil.action("Открыто окно покупки: " + car.getName());
        } catch (Exception e) {
            LoggerUtil.error("Ошибка открытия деталей для покупки", e);
            NotificationUtil.showError("Не удалось открыть форму покупки");
        }
    }

    // ========== МЕТОДЫ ДЛЯ РАБОТЫ С ИЗБРАННЫМ ==========

    /**
     * Переключение состояния избранного
     */
    private void toggleFavorite(Car car, Button btn) {
        if (!SessionManager.isLoggedIn()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Авторизация");
            alert.setHeaderText(null);
            alert.setContentText("Войдите в систему для добавления в избранное");
            alert.showAndWait();
            return;
        }

        int userId = SessionManager.getCurrentUserId();
        boolean isFavorite = FavoritesService.isFavorite(userId, car.getId());

        if (isFavorite) {
            FavoritesService.removeFromFavorites(userId, car.getId());
            LoggerUtil.action("Удалено из избранного: " + car.getName());
            NotificationUtil.showInfo("Удалено из избранного");
        } else {
            FavoritesService.addToFavorites(userId, car.getId());
            LoggerUtil.action("Добавлено в избранное: " + car.getName());
            NotificationUtil.showSuccess("Добавлено в избранное!");
        }

        updateFavoriteButton(car, btn);
    }

    /**
     * Обновление внешнего вида кнопки избранного
     */
    private void updateFavoriteButton(Car car, Button btn) {
        if (!SessionManager.isLoggedIn()) {
            btn.setText("❤");
            btn.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-font-size: 18px; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 5; " +
                "-fx-text-fill: #ccc;"
            );
            return;
        }

        int userId = SessionManager.getCurrentUserId();
        boolean isFavorite = FavoritesService.isFavorite(userId, car.getId());

        if (isFavorite) {
            btn.setText("❤");
            btn.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-font-size: 18px; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 5; " +
                "-fx-text-fill: #E91E63;"
            );
        } else {
            btn.setText("❤");
            btn.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-font-size: 18px; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 5; " +
                "-fx-text-fill: #ccc;"
            );
        }
    }
}

