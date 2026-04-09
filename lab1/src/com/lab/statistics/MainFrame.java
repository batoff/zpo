package com.lab.statistics;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.nio.file.Path;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Kontroler aplikacji - warstwa prezentacji (Swing GUI).
 * Logika tworzenia statystyk jest odseparowana w klasie WordStatistics.
 */
public class MainFrame {

    private JFrame frame;

    // ścieżka do katalogu z plikami tekstowymi
    private static final String DIR_PATH = "files";

    // określa ile najczęściej występujących wyrazów bierzemy pod uwagę
    private final int liczbaWyrazowStatystyki;
    private final AtomicBoolean fajrant;
    private final int liczbaProducentow;
    private final int liczbaKonsumentow;

    // pula wątków – obiekt klasy ExecutorService, który zarządza tworzeniem
    // nowych oraz wykonuje 'recykling' zakończonych wątków
    private ExecutorService executor;

    // lista obiektów klasy Future, dzięki którym mamy możliwość nadzoru pracy
    // wątków
    // producenckich np. sprawdzania czy wątek jest aktywny lub jego
    // anulowania/przerywania
    private List<Future<?>> producentFuture;

    // Odseparowana logika statystyk wyrazów
    private final WordStatistics wordStatistics;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainFrame window = new MainFrame();
                    window.frame.pack();
                    window.frame.setAlwaysOnTop(true);
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public MainFrame() {
        liczbaWyrazowStatystyki = 10;
        fajrant = new AtomicBoolean(false);
        liczbaProducentow = 1;
        liczbaKonsumentow = 2;
        executor = Executors.newFixedThreadPool(liczbaProducentow + liczbaKonsumentow);
        producentFuture = new CopyOnWriteArrayList<>(); // ten typ listy zapewnia bezpieczne operacje na jej elementach,
        // nawet przy jednoczesnym dostępie i modyfikacji przez wiele wątków
        wordStatistics = new WordStatistics(DIR_PATH, liczbaWyrazowStatystyki);
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame("Statystyka Wyrazów - Producent/Konsument");
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                executor.shutdownNow();
            }
        });
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.getContentPane().add(panel, BorderLayout.NORTH);

        JButton btnStop = new JButton("Stop");
        btnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fajrant.set(true);
                for (Future<?> f : producentFuture) {
                    f.cancel(true);
                }
            }
        });

        JButton btnStart = new JButton("Start");
        btnStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getMultiThreadedStatistics();
            }
        });

        JButton btnZamknij = new JButton("Zamknij");
        btnZamknij.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                executor.shutdownNow();
                frame.dispose();
            }
        });

        panel.add(btnStart);
        panel.add(btnStop);
        panel.add(btnZamknij);
    }

    /**
     * Statystyka wyrazów (wzorzec PRODUCENT - KONSUMENT korzystający z kolejki
     * blokującej)
     */
    private void getMultiThreadedStatistics() {
        for (Future<?> f : producentFuture) {
            if (!f.isDone()) {
                JOptionPane.showMessageDialog(frame,
                        "Nie można uruchomić nowego zadania!\nPrzynajmniej jeden producent nadal działa!",
                        "OSTRZEŻENIE", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        fajrant.set(false);
        producentFuture.clear();
        final BlockingQueue<Optional<Path>> kolejka = new LinkedBlockingQueue<>(liczbaKonsumentow);

        // Producent - delegacja do odseparowanej klasy WordStatistics
        Runnable producent = () -> wordStatistics.producentTask(kolejka, fajrant, liczbaKonsumentow);

        // Konsument - delegacja do odseparowanej klasy WordStatistics
        Runnable konsument = () -> wordStatistics.konsumentTask(kolejka);

        // Uruchamianie wszystkich wątków-producentów
        for (int i = 0; i < liczbaProducentow; i++) {
            Future<?> pf = executor.submit(producent);
            producentFuture.add(pf);
        }

        // Uruchamianie wszystkich wątków-konsumentów
        for (int i = 0; i < liczbaKonsumentow; i++) {
            executor.execute(konsument);
        }
    }
}
