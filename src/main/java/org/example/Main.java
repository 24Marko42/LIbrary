package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        // Создаем Gson для работы с JSON
        Gson gson = new GsonBuilder().create();

        try (Reader reader = new FileReader("src/main/resources/books.json")) {
            // Определяем тип для десериализации JSON в список посетителей библиотеки
            Type listType = new TypeToken<List<Visitors>>() {}.getType();
            List<Visitors> visitors = gson.fromJson(reader, listType);

            // Задача 1: Вывести всех посетителей и их общее количество
            displayVisitors(visitors);

            // Задача 2: Найти уникальные книги и их общее количество
            displayUniqueBooks(visitors);

            // Задача 3: Отсортировать книги по году публикации и вывести их
            displaySortedBooksByYear(visitors);

            // Задача 4: Проверить наличие книг Джейн Остин у кого-либо
            checkForJaneAustenBooks(visitors);

            // Задача 5: Найти максимальное количество любимых книг у одного посетителя
            findMaxFavoriteBooks(visitors);

            // Задача 6: Генерация SMS-сообщений для подписчиков
            generateSmsMessages(visitors);
        } catch (IOException e) {
            e.printStackTrace(); // Логируем исключение
        }
    }

    // Задача 1: Вывод всех посетителей
    private static void displayVisitors(List<Visitors> visitors) {
        System.out.println("Task 1: Visitors and their count:");
        visitors.forEach(visitor -> System.out.println(visitor.getName() + " " + visitor.getSurname()));
        System.out.println("Total visitors: " + visitors.size());
        System.out.println();
    }

    // Задача 2: Уникальные книги
    private static void displayUniqueBooks(List<Visitors> visitors) {
        System.out.println("Task 2: Unique books and their count:");
        List<Book> uniqueBooks = visitors.stream()
                .flatMap(visitor -> visitor.getFavoriteBooks().stream()) // Собираем книги всех посетителей
                .distinct() // Убираем дубликаты
                .toList(); // Преобразуем в список
        uniqueBooks.forEach(System.out::println); // Выводим книги
        System.out.println("Total unique books: " + uniqueBooks.size()); // Количество уникальных книг
        System.out.println();
    }

    // Задача 3: Сортировка книг по году публикации
    private static void displaySortedBooksByYear(List<Visitors> visitors) {
        System.out.println("Task 3: Sorted book list by publication year:");
        visitors.stream()
                .flatMap(visitor -> visitor.getFavoriteBooks().stream()) // Собираем книги всех посетителей
                .sorted(Comparator.comparingInt(Book::getPublishingYear)) // Сортируем по году
                .forEach(book -> System.out.println(book.getName() + " (" + book.getPublishingYear() + ")")); // Выводим
        System.out.println();
    }

    // Задача 4: Проверка книг Джейн Остин
    private static void checkForJaneAustenBooks(List<Visitors> visitors) {
        System.out.println("Task 4: Checking if any visitor has a book by Jane Austen:");
        boolean hasJaneAustenBook = visitors.stream()
                .anyMatch(visitor -> visitor.getFavoriteBooks().stream() // Проверяем книги каждого посетителя
                        .anyMatch(book -> book.getAuthor().equals("Jane Austen"))); // Ищем книги Джейн Остин
        System.out.println("Does any visitor have a book by Jane Austen? " + hasJaneAustenBook);
        System.out.println();
    }

    // Задача 5: Максимальное количество любимых книг
    private static void findMaxFavoriteBooks(List<Visitors> visitors) {
        System.out.println("Task 5: Maximum number of favorite books:");
        int maxFavoriteBooks = visitors.stream()
                .mapToInt(visitor -> visitor.getFavoriteBooks().size()) // Количество книг у каждого посетителя
                .max() // Находим максимум
                .orElse(0); // Если список пустой, возвращаем 0
        System.out.println("Maximum number of favorite books: " + maxFavoriteBooks);
        System.out.println();
    }

    // Задача 6: Генерация SMS-сообщений
    private static void generateSmsMessages(List<Visitors> visitors) {
        System.out.println("Task 6: SMS messages for newsletter subscribers:");

        // Вычисляем среднее количество книг
        double averageFavoriteBooks = calculateAverageFavoriteBooks(visitors);

        // Генерируем SMS по категориям
        Map<String, List<SMS>> smsMessagesByCategory = visitors.stream()
                .filter(Visitors::isSubscribed) // Оставляем только подписчиков
                .collect(Collectors.groupingBy(
                        visitor -> categorizeVisitor(visitor.getFavoriteBooks().size(), averageFavoriteBooks), // Категоризация
                        Collectors.mapping(visitor -> new SMS(
                                        visitor.getPhone(), // Телефон
                                        generateSmsMessage(visitor.getFavoriteBooks().size(), averageFavoriteBooks)), // Сообщение
                                Collectors.toList())));

        // Выводим результаты
        smsMessagesByCategory.forEach((category, smsMessages) -> {
            System.out.println("Category: " + category);
            smsMessages.forEach(smsMessage -> System.out.println("Phone: " + smsMessage.getPhoneNumber() +
                    ", Message: " + smsMessage.getMessage()));
        });
    }

    // Вычисление среднего количества любимых книг
    private static double calculateAverageFavoriteBooks(List<Visitors> visitors) {
        return visitors.stream()
                .mapToInt(visitor -> visitor.getFavoriteBooks().size()) // Количество книг
                .average() // Среднее значение
                .orElse(0); // Если список пустой, возвращаем 0
    }

    // Генерация SMS сообщения на основе количества книг
    private static String generateSmsMessage(int favoriteBookCount, double averageFavoriteBooks) {
        return switch (favoriteBookCount > averageFavoriteBooks ? 1 :
                favoriteBookCount < averageFavoriteBooks ? 2 : 3) {
            case 1 -> "you are a bookworm"; // Любит читать больше среднего
            case 2 -> "read more"; // Читает меньше среднего
            case 3 -> "fine"; // Средний уровень чтения
            default -> "";
        };
    }

    // Категоризация посетителя
    private static String categorizeVisitor(int favoriteBookCount, double averageFavoriteBooks) {
        if (favoriteBookCount > averageFavoriteBooks) {
            return "bookworm";
        } else if (favoriteBookCount < averageFavoriteBooks) {
            return "read more";
        } else {
            return "fine";
        }
    }
}
