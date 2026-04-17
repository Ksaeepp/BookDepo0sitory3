package ru.kurepin.bookdepository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import ru.kurepin.bookdepository.database.BookBaseHelper;
import ru.kurepin.bookdepository.database.BookCursorWrapper;
import ru.kurepin.bookdepository.database.BookDbSchema.BookTable;

public class BookLab {
    private static BookLab sBookLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static BookLab get(Context context) {
        if (sBookLab == null) {
            sBookLab = new BookLab(context);
        }
        return sBookLab;
    }

    private BookLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new BookBaseHelper(mContext).getWritableDatabase();
    }

    public void addBook(Book book) {
        ContentValues values = getContentValues(book);
        mDatabase.insert(BookTable.NAME, null, values);
    }

    public void updateBook(Book book) {
        String uuidString = book.getId().toString();
        ContentValues values = getContentValues(book);
        mDatabase.update(BookTable.NAME, values,
                BookTable.Cols.UUID + " = ?", new String[]{uuidString});
    }

    public void deleteBook(Book book) {
        String uuidString = book.getId().toString();
        mDatabase.delete(BookTable.NAME,
                BookTable.Cols.UUID + " = ?", new String[]{uuidString});
    }

    public List<Book> getBooks() {
        List<Book> books = new ArrayList<>();
        BookCursorWrapper cursor = queryBooks(null, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                books.add(cursor.getBook());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return books;
    }

    public Book getBook(UUID id) {
        BookCursorWrapper cursor = queryBooks(
                BookTable.Cols.UUID + " = ?",
                new String[]{id.toString()}
        );
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getBook();
        } finally {
            cursor.close();
        }
    }

    private BookCursorWrapper queryBooks(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                BookTable.NAME,
                null, // все столбцы
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null  // orderBy
        );
        return new BookCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(Book book) {
        ContentValues values = new ContentValues();
        values.put(BookTable.Cols.UUID, book.getId().toString());
        values.put(BookTable.Cols.TITLE, book.getTitle());
        values.put(BookTable.Cols.DATE, book.getDate().getTime());
        values.put(BookTable.Cols.READED, book.isReaded() ? 1 : 0);
        return values;
    }
    public File getPhotoFile(Book book) {
        File externalFilesDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (externalFilesDir == null) {
            return null;
        }
        return new File(externalFilesDir, book.getPhotoFilename());
    }
}