package ru.kurepin.bookdepository;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import java.util.UUID;

public class BookActivity extends SingleFragmentActivity {
    private static final String EXTRA_BOOK_ID = "ru.kurepin.bookdepository.book_id";

    public static Intent newIntent(Context packageContext, UUID bookId) {
        Intent intent = new Intent(packageContext, BookActivity.class);
        intent.putExtra(EXTRA_BOOK_ID, bookId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ПОКАЗЫВАЕМ ТУЛБАР (если он был скрыт в родительском классе)
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
    }

    @Override
    protected Fragment createFragment() {
        UUID bookId = (UUID) getIntent().getSerializableExtra(EXTRA_BOOK_ID);
        return BookFragment.newInstance(bookId);
    }
}