package ru.kurepin.bookdepository;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

public class BookListActivity extends SingleFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Убираем скрытие тулбара, чтобы меню отображалось
        // if (getSupportActionBar() != null) {
        //     getSupportActionBar().hide();
        // }
    }

    @Override
    protected Fragment createFragment() {
        return new BookListFragment();
    }
}