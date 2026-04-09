package com.lab.statistics;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.function.Function;

public class WordStatistics {

    private final String dirPath;
    private final int wordsLimit;

    public WordStatistics(String dirPath, int wordsLimit) {
        this.dirPath = dirPath;
        this.wordsLimit = wordsLimit;
    }

    public void producentTask(BlockingQueue<Optional<Path>> kolejka,
            AtomicBoolean fajrant,
            int liczbaKonsumentow) {
        final String name = Thread.currentThread().getName();
        String info = String.format("PRODUCENT %s URUCHOMIONY ...", name);
        System.out.println(info);

        while (!Thread.currentThread().isInterrupted()) {
            if (fajrant.get()) {
                // Przekazanie poison pills konsumentom i zakończenie działania
                try {
                    for (int i = 0; i < liczbaKonsumentow; i++) {
                        kolejka.put(Optional.empty());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                break;
            } else {
                // Wyszukiwanie plików *.txt i wstawianie do kolejki
                try {
                    Path dir = Paths.get(dirPath);
                    Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                            if (Thread.currentThread().isInterrupted() || fajrant.get()) {
                                return FileVisitResult.TERMINATE;
                            }
                            if (path.toString().toLowerCase().endsWith(".txt")) {
                                try {
                                    Optional<Path> optPath = Optional.ofNullable(path);
                                    kolejka.put(optPath);
                                    String fileInfo = String.format("Producent %s: dodano plik %s do kolejki",
                                            Thread.currentThread().getName(), path.getFileName());
                                    System.out.println(fileInfo);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    return FileVisitResult.TERMINATE;
                                }
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException e) {
                    System.err.println("Błąd podczas przeszukiwania katalogów: " + e.getMessage());
                }
            }

            info = String.format("Producent %s ponownie sprawdzi katalogi za %d sekund", name, 60);
            System.out.println(info);
            try {
                java.util.concurrent.TimeUnit.SECONDS.sleep(60);
            } catch (InterruptedException e) {
                info = String.format("Przerwa producenta %s przerwana!", name);
                System.out.println(info);
                if (!fajrant.get())
                    Thread.currentThread().interrupt();
            }
        }
        info = String.format("PRODUCENT %s SKOŃCZYŁ PRACĘ", name);
        System.out.println(info);
    }

    public void konsumentTask(BlockingQueue<Optional<Path>> kolejka) {
        final String name = Thread.currentThread().getName();
        String info = String.format("KONSUMENT %s URUCHOMIONY ...", name);
        System.out.println(info);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Optional<Path> optPath = kolejka.take();

                if (optPath.isPresent()) {
                    Map<String, Long> countedWords = getLinkedCountedWords(optPath.get(), wordsLimit);
                    info = String.format("Konsument %s - plik: %s\n  Statystyki: %s",
                            name, optPath.get().getFileName(), countedWords);
                    System.out.println(info);
                } else {
                    info = String.format("Konsument %s otrzymał poison pill - kończenie pracy.", name);
                    System.out.println(info);
                    break;
                }
            } catch (InterruptedException e) {
                info = String.format("Oczekiwanie konsumenta %s na nowy element z kolejki przerwane!", name);
                System.out.println(info);
                Thread.currentThread().interrupt();
            }
        }
        info = String.format("KONSUMENT %s ZAKOŃCZYŁ PRACĘ", name);
        System.out.println(info);
    }

    public Map<String, Long> getLinkedCountedWords(Path path, int wordsLimit) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return reader.lines()
                    .map(line -> line.split("\\s+"))
                    .flatMap(Arrays::stream)
                    .map(String::toLowerCase)
                    .map(word -> word.replaceAll("[^a-zA-Z0-9ąęóśćżńźĄĘÓŚĆŻŃŹ]", ""))
                    .filter(word -> word.matches("[a-z0-9ąęóśćżńź]{3,}"))
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(wordsLimit)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (k, v) -> {
                                throw new IllegalStateException(String.format("Błąd! Duplikat klucza %s.", k));
                            },
                            LinkedHashMap::new));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
