package ru.kurepin.bookdepository;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.util.Date;
import java.util.UUID;

public class BookFragment extends Fragment {

    private static final String ARG_BOOK_ID = "book_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_PHOTO = 1;
    // ==================== ШАГ 14: Добавляем константу для запроса разрешения ====================
    private static final int REQUEST_CAMERA_PERMISSION = 2;
    // ==================== КОНЕЦ ШАГА 14 ====================

    private Book mBook;
    private File mPhotoFile;
    private EditText mTitleField;
    private CheckBox mReadedCheckBox;
    private TextView mDateTextView;
    private Button mDateButton;
    private Button mReportButton;
    private Button mWebButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;

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
        mPhotoFile = BookLab.get(getActivity()).getPhotoFile(mBook);
    }

    private void updateDate() {
        String dateString = DateFormat.getDateFormat(getActivity()).format(mBook.getDate());
        mDateTextView.setText(dateString);
    }

    private String getBookReport() {
        String readedString;
        if (mBook.isReaded()) {
            readedString = getString(R.string.book_report_readed);
        } else {
            readedString = getString(R.string.book_report_unreaded);
        }

        String dateString = DateFormat.getDateFormat(getActivity()).format(mBook.getDate());
        return getString(R.string.book_report, mBook.getTitle(), dateString, readedString);
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

    // ==================== ШАГ 14: Метод для запроса разрешения на камеру ====================
    /**
     * Запрашивает разрешение на использование камеры (для Android 6.0+)
     */
    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Проверяем, есть ли уже разрешение
            if (getActivity().checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Запрашиваем разрешение
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
            } else {
                // Разрешение уже есть - запускаем камеру
                startCamera();
            }
        } else {
            // Android 6.0 и ниже - разрешение дано при установке
            startCamera();
        }
    }

    /**
     * Запускает приложение камеры
     */
    private void startCamera() {
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Настраиваем URI для сохранения фото
        if (mPhotoFile != null) {
            Uri uri;
            if (Build.VERSION.SDK_INT < 24) {
                uri = Uri.fromFile(mPhotoFile);
            } else {
                uri = FileProvider.getUriForFile(getActivity(),
                        "ru.kurepin.bookdepository.provider", mPhotoFile);
            }
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }

        startActivityForResult(captureImage, REQUEST_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(getActivity(), "Разрешение на камеру получено", Toast.LENGTH_SHORT).show();
                startCamera();
            } else {

                Toast.makeText(getActivity(),
                        "Для добавления фото требуется разрешение на камеру",
                        Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != getActivity().RESULT_OK) return;

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mBook.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_PHOTO) {
            updatePhotoView();
            if (mPhotoFile != null && mPhotoFile.exists()) {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(mPhotoFile));
                getActivity().sendBroadcast(mediaScanIntent);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_book, container, false);

        // Инициализация полей
        mTitleField = v.findViewById(R.id.book_title);
        mTitleField.setText(mBook.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBook.setTitle(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        mDateButton = v.findViewById(R.id.book_date_button);
        mDateTextView = v.findViewById(R.id.book_date);
        updateDate();

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mBook.getDate());
                dialog.setTargetFragment(BookFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mReadedCheckBox = v.findViewById(R.id.book_readed);
        mReadedCheckBox.setChecked(mBook.isReaded());
        mReadedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBook.setReaded(isChecked);
            }
        });

        // ==================== НАСТРОЙКА КАМЕРЫ ====================
        mPhotoButton = v.findViewById(R.id.book_camera);
        mPhotoView = v.findViewById(R.id.book_photo);

        // Создаём папку для фото
        if (mPhotoFile != null) {
            File parentDir = mPhotoFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
        }


        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestCameraPermission();
            }
        });


        mPhotoButton.setEnabled(true);
        // ==================== КОНЕЦ НАСТРОЙКИ КАМЕРЫ ====================

        // Отображаем существующее фото
        updatePhotoView();

        // ==================== ЗАДАНИЕ 1: ОТКРЫТИЕ УВЕЛИЧЕННОГО ИЗОБРАЖЕНИЯ ====================
        // При нажатии на миниатюру открывается диалог с увеличенным фото
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhotoFile != null && mPhotoFile.exists()) {
                    FullPhotoFragment dialog = FullPhotoFragment.newInstance(mPhotoFile);
                    dialog.show(getFragmentManager(), "FullPhoto");
                } else {
                    Toast.makeText(getActivity(), "Нет фотографии для просмотра", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // ==================== КОНЕЦ ЗАДАНИЯ 1 ====================

        // Кнопка "Открыть книгу в браузере"
        mWebButton = v.findViewById(R.id.book_web);
        mWebButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bookTitle = mBook.getTitle();
                String encodedTitle = Uri.encode(bookTitle);
                Uri webPage = Uri.parse("https://www.google.com/search?q=" + encodedTitle);
                Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
                startActivity(intent);
            }
        });

        // Кнопка отправки отчёта
        mReportButton = v.findViewById(R.id.book_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getBookReport());
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.book_report_subject));

                PackageManager pm = getActivity().getPackageManager();
                if (intent.resolveActivity(pm) != null) {
                    Intent chooserIntent = Intent.createChooser(intent, getString(R.string.send_report));
                    startActivity(chooserIntent);
                } else {
                    Toast.makeText(getActivity(), "Нет приложений для отправки сообщений", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Кнопка назад
        Button backButton = v.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
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

    @Override
    public void onPause() {
        super.onPause();
        BookLab.get(getActivity()).updateBook(mBook);
    }
}