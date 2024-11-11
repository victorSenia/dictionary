package org.leo.dictionary;

import org.leo.dictionary.entity.Word;
import org.leo.dictionary.entity.WordCriteria;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class WindowsApp {

    private final JList<Word> wordList = new JList<>();
    private PlayService playService;
    private ExternalWordProvider wordProvider;
    public static void main(String[] args) {
        DaggerWindowsAppComponent.create().buildWindowsApp().showUi();
    }

    public void setPlayService(PlayService playService) {
        this.playService = playService;
    }

    public void showUi() {
        List<Word> words = findAndSetWords(new WordCriteria());
        JFrame frame = new JFrame("Dictionary");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel panel = controlButtons();
        frame.setLayout(new BorderLayout(50, 50));
        frame.add(panel, BorderLayout.CENTER);

        updateWithWords(words);
        wordList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(Word.formatWord((Word) value));
                return component;
            }
        });
        wordList.setVisibleRowCount(10);

        JPopupMenu menu = new JPopupMenu();
        JMenuItem action = new JMenuItem("play from selected");
        action.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (wordList.getSelectedIndex() != -1) {
                    playService.playFrom(wordList.getSelectedIndex());
                }
            }
        });
        menu.add(action);
        wordList.setComponentPopupMenu(menu);

        JScrollPane scrollPane = new JScrollPane(wordList);
        frame.add(scrollPane, BorderLayout.SOUTH);

        JLabel state = new JLabel(" ");
        state.setHorizontalAlignment(SwingConstants.CENTER);
        state.setFont(new FontExtend(state.getFont(), 2));
        state.setSize(50, 100);
        playService.setUiUpdater((word, index) -> SwingUtilities.invokeLater(() -> {
            state.setText(Word.formatWord(word));
            wordList.setSelectedValue(word, true);
        }));
        frame.add(state, BorderLayout.NORTH);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                playService.pause();
            }
        });
        frame.setVisible(true);
    }

    private void updateWithWords(List<Word> words) {
        wordList.setListData(words.toArray(new Word[0]));
        wordList.setSelectedIndex(0);
    }

    private JPanel controlButtons() {
        JButton playStop = new JButton("play Stop");
        playStop.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!playService.isPlaying()) {
                    playService.play();
                } else {
                    playService.pause();
                }
            }
        });
        JButton next = new JButton("next");
        next.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playService.next();
            }
        });
        JButton previous = new JButton("previous");
        previous.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playService.previous();
            }
        });

        JList<String> topics = new JList<>();
        topics.setAutoscrolls(true);
        topics.setListData(wordProvider.findTopics("de").toArray(new String[0]));
        topics.setVisibleRowCount(3);
        topics.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JPopupMenu menu = new JPopupMenu();
        JMenuItem orTopics = new JMenuItem("select for any of topics");
        orTopics.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WordCriteria criteria = new WordCriteria();
                criteria.setTopicsOr(topics.getSelectedValuesList());
                List<Word> words = findAndSetWords(criteria);
                SwingUtilities.invokeLater(() -> updateWithWords(words));
            }
        });
        menu.add(orTopics);
        JMenuItem andTopics = new JMenuItem("select for all topics");
        andTopics.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WordCriteria criteria = new WordCriteria();
                criteria.setTopicsAnd(topics.getSelectedValuesList());
                List<Word> words =  findAndSetWords(criteria);
                SwingUtilities.invokeLater(() -> updateWithWords(words));
            }
        });
        menu.add(andTopics);
        topics.setComponentPopupMenu(menu);

        JPanel panel = new JPanel(new GridLayout(1, 3));
        panel.add(previous);
        panel.add(playStop);
        panel.add(next);
        panel.add(new JScrollPane(topics));
        return panel;
    }

    private List<Word> findAndSetWords(WordCriteria criteria) {
        List<Word> words = wordProvider.findWords(criteria);
        playService.setWords(words);
        return words;
    }

    public void setExternalWordProvider(ExternalWordProvider wordProvider) {
        this.wordProvider = wordProvider;
    }

    static class FontExtend extends Font {
        FontExtend(Font font, double sizeScale) {
            super(font);
            super.size = (int) (super.size * sizeScale);
            super.pointSize = (float) (super.pointSize * sizeScale);
        }
    }
}

