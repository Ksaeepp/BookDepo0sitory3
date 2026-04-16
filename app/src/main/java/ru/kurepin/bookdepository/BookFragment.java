package ru.kurepin.bookdepository;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.util.Date;
import java.util.UUID;

public class BookFragment extends Fragment {

    private static final String ARG_BOOK_ID = "book_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final int REQUEST_DATE = 0;

    private Book mBook;
    private EditText mTitleField;
    private CheckBox mReadedCheckBox;
    private TextView mDateTextView;
    private Button mDateButton;
    private Button mReportButton; // Кнопка для отправки отчёта

    public static BookFragment newInstance(UUID bookId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_BOOK_ID, bookId);
        BookFragment fragment = new BookFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        UUID bookId = (UUID) getArguments().getSerializable(ARG_BOOK_ID);
        mBook = BookLab.get(getActivity()).getBook(bookId);
    }

    /**
     * Обновляет отображение даты в TextView
     * Использует android.text.format.DateFormat для форматирования даты
     */
    private void updateDate() {
        String dateString = DateFormat.getDateFormat(getActivity()).format(mBook.getDate());
        mDateTextView.setText(dateString);
    }

    // ==================== ШАГ 4: Метод getBookReport() ====================
    /**
     * Формирует текстовый отчёт о книге для отправки через неявный Intent
     * Использует форматную строку с заполнителями из strings.xml
     *
     * @return String - отформатированный текст отчёта
     */
    private String getBookReport() {
        // Определяем статус прочтения
        String readedString;
        if (mBook.isReaded()) {
            readedString = getString(R.string.book_report_readed);
        } else {
            readedString = getString(R.string.book_report_unreaded);
        }

        // Форматируем дату
        String dateString = DateFormat.getDateFormat(getActivity()).format(mBook.getDate());

        // Формируем отчёт с использованием форматной строки
        String report = getString(R.string.book_report,
                mBook.getTitle(),   // %1$s - название книги
                dateString,        // %2$s - дата
                readedString);     // %3$s - статус прочтения

        return report;
    }
    // ==================== КОНЕЦ ШАГА 4 ====================

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != getActivity().RESULT_OK) return;
        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mBook.setDate(date);
            updateDate();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_book, container, false);

        // Инициализация поля для ввода названия книги
        mTitleField = v.findViewById(R.id.book_title);
        mTitleField.setText(mBook.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBook.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Инициализация кнопки и текстового поля для даты
        mDateButton = v.findViewById(R.id.book_date_button);
        mDateTextView = v.findViewById(R.id.book_date);
        updateDate();

        // Обработчик нажатия на кнопку выбора даты
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mBook.getDate());
                dialog.setTargetFragment(BookFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        // Инициализация CheckBox для статуса прочтения
        mReadedCheckBox = v.findViewById(R.id.book_readed);
        mReadedCheckBox.setChecked(mBook.isReaded());
        mReadedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBook.setReaded(isChecked);
            }
        });

        // Кнопка возврата назад
        Button backButton = v.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });


        mReportButton = v.findViewById(R.id.book_report);


        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_SEND);

                intent.setType("text/plain");

                intent.putExtra(Intent.EXTRA_TEXT, getBookReport());

                intent.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.book_report_subject));


                PackageManager packageManager = getActivity().getPackageManager();


                if (intent.resolveActivity(packageManager) != null) {

                    Intent chooserIntent = Intent.createChooser(intent,
                            getString(R.string.send_report));

                    startActivity(chooserIntent);
                } else {
                    Toast.makeText(getActivity(),
                            "Нет приложений для отправки сообщений",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });


        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_book, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_delete_book) {
            BookLab.get(getActivity()).deleteBook(mBook);
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Сохранение изменений при уходе с экрана
    @Override
    public void onPause() {
        super.onPause();
        BookLab.get(getActivity()).updateBook(mBook);
    }
}