package org.leo.dictionary.controller;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.leo.dictionary.entity.Topic;
import org.leo.dictionary.entity.WordCriteria;
import org.leo.dictionary.word.provider.WordProvider;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FilterSidebarFragmentController {
    @FXML
    private ListView<String> languageFromList;
    @FXML
    private ListView<String> languageToList;
    @FXML
    private ListView<Topic> rootTopicsList;
    @FXML
    private ListView<Topic> topicsList;
    @FXML
    private ComboBox<WordCriteria.WordsOrderMode> orderModeCombo;
    @FXML
    private CheckBox useKnowledgeFromCheck;
    @FXML
    private CheckBox useKnowledgeToCheck;
    @FXML
    private Slider knowledgeFromSlider;
    @FXML
    private Slider knowledgeToSlider;
    @FXML
    private Label knowledgeFromValueLabel;
    @FXML
    private Label knowledgeToValueLabel;
    @FXML
    private Button applyFilterButton;
    @FXML
    private Button resetFilterButton;

    private final WordProvider wordProvider;
    private Consumer<WordCriteria> onApply = criteria -> {
    };
    private Supplier<WordCriteria> criteriaSupplier = WordCriteria::new;
    private Runnable onReset = () -> {
    };
    private boolean updatingUi;

    public FilterSidebarFragmentController(WordProvider wordProvider) {
        this.wordProvider = wordProvider;
    }

    @FXML
    public void initialize() {
        languageFromList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        languageToList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        rootTopicsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        topicsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        rootTopicsList.setCellFactory(param -> topicListCell());
        topicsList.setCellFactory(param -> topicListCell());

        orderModeCombo.getItems().setAll(WordCriteria.WordsOrderMode.values());
        orderModeCombo.setValue(WordCriteria.WordsOrderMode.IMPORT_ORDER);

        setupKnowledgeSlider(knowledgeFromSlider, knowledgeFromValueLabel, 0.0);
        setupKnowledgeSlider(knowledgeToSlider, knowledgeToValueLabel, 1.0);
        useKnowledgeFromCheck.selectedProperty().addListener((obs, oldValue, selected) -> knowledgeFromSlider.setDisable(!selected));
        useKnowledgeToCheck.selectedProperty().addListener((obs, oldValue, selected) -> knowledgeToSlider.setDisable(!selected));
        knowledgeFromSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.doubleValue() > knowledgeToSlider.getValue()) {
                knowledgeToSlider.setValue(newValue.doubleValue());
            }
        });
        knowledgeToSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.doubleValue() < knowledgeFromSlider.getValue()) {
                knowledgeFromSlider.setValue(newValue.doubleValue());
            }
        });

        languageFromList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (!updatingUi) {
                refreshLanguageDependentLists(criteriaSupplier.get());
            }
        });
        rootTopicsList.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Topic>) change -> {
            if (!updatingUi) {
                refreshTopics(criteriaSupplier.get());
            }
        });

        applyFilterButton.setOnAction(e -> applyFilter());
        resetFilterButton.setOnAction(e -> {
            onReset.run();
            reloadFromCriteria(criteriaSupplier.get());
        });
    }

    public void init(Consumer<WordCriteria> onApply, Supplier<WordCriteria> criteriaSupplier, Runnable onReset) {
        this.onApply = Objects.requireNonNull(onApply);
        this.criteriaSupplier = Objects.requireNonNull(criteriaSupplier);
        this.onReset = Objects.requireNonNull(onReset);
        reloadFromCriteria(criteriaSupplier.get());
    }

    public void reloadFromCriteria(WordCriteria criteria) {
        updatingUi = true;
        try {
            List<String> languages = new ArrayList<>(wordProvider.languageFrom());
            languageFromList.getItems().setAll(languages);
            String selectedLanguage = criteria.getLanguageFrom();
            if (selectedLanguage == null && !languages.isEmpty()) {
                selectedLanguage = languages.get(0);
            }
            if (selectedLanguage != null) {
                languageFromList.getSelectionModel().select(selectedLanguage);
            } else {
                languageFromList.getSelectionModel().clearSelection();
            }
            refreshLanguageDependentLists(criteria);

            orderModeCombo.setValue(criteria.getWordsOrderMode() != null
                    ? criteria.getWordsOrderMode()
                    : WordCriteria.WordsOrderMode.IMPORT_ORDER);
            useKnowledgeFromCheck.setSelected(criteria.getKnowledgeFrom() != null);
            useKnowledgeToCheck.setSelected(criteria.getKnowledgeTo() != null);
            knowledgeFromSlider.setValue(criteria.getKnowledgeFrom() == null ? 0.0 : clamp(criteria.getKnowledgeFrom()));
            knowledgeToSlider.setValue(criteria.getKnowledgeTo() == null ? 1.0 : clamp(criteria.getKnowledgeTo()));
            knowledgeFromSlider.setDisable(!useKnowledgeFromCheck.isSelected());
            knowledgeToSlider.setDisable(!useKnowledgeToCheck.isSelected());
        } finally {
            updatingUi = false;
        }
    }

    private void refreshLanguageDependentLists(WordCriteria criteria) {
        String selectedLanguage = languageFromList.getSelectionModel().getSelectedItem();
        List<String> languageToValues = selectedLanguage == null
                ? Collections.emptyList()
                : wordProvider.languageTo(selectedLanguage);
        languageToList.getItems().setAll(languageToValues);
        selectLanguageTo(criteria, languageToValues);

        List<Topic> rootTopics = selectedLanguage == null
                ? Collections.emptyList()
                : wordProvider.findTopics(selectedLanguage, 1);
        rootTopicsList.getItems().setAll(rootTopics);
        selectTopics(rootTopicsList, criteria.getRootTopics());

        refreshTopics(criteria);
    }

    private void refreshTopics(WordCriteria criteria) {
        String selectedLanguage = languageFromList.getSelectionModel().getSelectedItem();
        List<Topic> selectedRoots = new ArrayList<>(rootTopicsList.getSelectionModel().getSelectedItems());
        List<Topic> topics = selectedLanguage == null
                ? Collections.emptyList()
                : (selectedRoots.isEmpty()
                ? wordProvider.findTopics(selectedLanguage, 2)
                : wordProvider.findTopicsWithRoot(selectedLanguage, new HashSet<>(selectedRoots), 2));
        topicsList.getItems().setAll(topics);
        selectTopics(topicsList, criteria.getTopicsOr());
    }

    private void applyFilter() {
        WordCriteria criteria = criteriaSupplier.get();
        criteria.setLanguageFrom(languageFromList.getSelectionModel().getSelectedItem());
        criteria.setLanguageTo(languageToList.getSelectionModel().getSelectedItems().isEmpty()
                ? null
                : new HashSet<>(languageToList.getSelectionModel().getSelectedItems()));
        criteria.setRootTopics(rootTopicsList.getSelectionModel().getSelectedItems().isEmpty()
                ? null
                : new HashSet<>(rootTopicsList.getSelectionModel().getSelectedItems()));
        criteria.setTopicsOr(topicsList.getSelectionModel().getSelectedItems().isEmpty()
                ? null
                : new HashSet<>(topicsList.getSelectionModel().getSelectedItems()));
        criteria.setWordsOrderMode(orderModeCombo.getValue());
        criteria.setShuffleRandom(orderModeCombo.getValue() == WordCriteria.WordsOrderMode.SHUFFLE
                ? System.currentTimeMillis()
                : WordCriteria.NOT_SET);
        criteria.setKnowledgeFrom(useKnowledgeFromCheck.isSelected() ? knowledgeFromSlider.getValue() : null);
        criteria.setKnowledgeTo(useKnowledgeToCheck.isSelected() ? knowledgeToSlider.getValue() : null);
        onApply.accept(criteria);
    }

    private void selectLanguageTo(WordCriteria criteria, List<String> values, MultipleSelectionModel<String> selectionModel) {
        selectionModel.clearSelection();
        if (criteria.getLanguageTo() == null) {
            return;
        }
        for (String value : criteria.getLanguageTo()) {
            if (values.contains(value)) {
                selectionModel.select(value);
            }
        }
    }

    private void selectLanguageTo(WordCriteria criteria, List<String> values) {
        selectLanguageTo(criteria, values, languageToList.getSelectionModel());
    }

    private void selectTopics(ListView<Topic> listView, Set<Topic> selectedTopics) {
        listView.getSelectionModel().clearSelection();
        if (selectedTopics == null || selectedTopics.isEmpty()) {
            return;
        }
        Set<Long> selectedIds = selectedTopics.stream().map(Topic::getId).collect(Collectors.toSet());
        for (Topic topic : listView.getItems()) {
            if (selectedIds.contains(topic.getId())) {
                listView.getSelectionModel().select(topic);
            }
        }
    }

    private static ListCell<Topic> topicListCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Topic item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        };
    }

    private void setupKnowledgeSlider(Slider slider, Label valueLabel, double initialValue) {
        slider.setMin(0.0);
        slider.setMax(1.0);
        slider.setValue(initialValue);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.2);
        slider.setBlockIncrement(0.01);
        valueLabel.setText(format(slider.getValue()));
        slider.valueProperty().addListener((obs, oldValue, newValue) -> valueLabel.setText(format(newValue.doubleValue())));
    }

    private static String format(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
