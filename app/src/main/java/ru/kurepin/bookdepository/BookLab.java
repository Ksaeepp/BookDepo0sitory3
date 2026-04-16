package ru.kurepin.bookdepository;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookLab {
    private static BookLab sBookLab;
    private List<Book> mBooks;

    private BookLab(Context context) {
        mBooks = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            Book book = new Book();
            book.setTitle("Книга " + i);
            book.setReaded(i % 2 == 0);
            mBooks.add(book);
        }
    }

    // Получение единственного экземпляра синглетного класса
    public static BookLab get(Context context) {
        if (sBookLab == null) {
            sBookLab = new BookLab(context);
        }
        return sBookLab;
    }

    // Возвращает весь список книг
    public List<Book> getBooks() {
        return mBooks;
    }

    /**
     * Метод для поиска книги по её уникальному идентификатору (UUID)
     * @param id уникальный идентификатор искомой книги
     * @return объект Book с указанным id, либо null, если книга не найдена
     */
    public Book getBook(UUID id) {
        for (Book book : mBooks) {
            if (book.getId().equals(id)) {
                return book;
            }
        }
        return null;
    }
    public void addBook(Book book) {
        mBooks.add(book);
    }
}