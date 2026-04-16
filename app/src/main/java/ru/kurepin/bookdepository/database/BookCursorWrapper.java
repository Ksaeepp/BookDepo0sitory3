package ru.kurepin.bookdepository.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import java.util.Date;
import java.util.UUID;
import ru.kurepin.bookdepository.Book;
import ru.kurepin.bookdepository.database.BookDbSchema.BookTable;

public class BookCursorWrapper extends CursorWrapper {
    public BookCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Book getBook() {
        String uuidString = getString(getColumnIndex(BookTable.Cols.UUID));
        String title = getString(getColumnIndex(BookTable.Cols.TITLE));
        long date = getLong(getColumnIndex(BookTable.Cols.DATE));
        int isReaded = getInt(getColumnIndex(BookTable.Cols.READED));

        Book book = new Book(UUID.fromString(uuidString));
        book.setTitle(title);
        book.setDate(new Date(date));
        book.setReaded(isReaded != 0);
        return book;
    }
}