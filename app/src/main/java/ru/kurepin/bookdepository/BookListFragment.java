package ru.kurepin.bookdepository;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import android.content.Intent;

public class BookListFragment extends Fragment {

    private RecyclerView mBookRecyclerView;
    private BookAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);
        mBookRecyclerView = view.findViewById(R.id.book_recycler_view);
        mBookRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return view;
    }

    private void updateUI() {
        BookLab bookLab = BookLab.get(getActivity());
        List<Book> books = bookLab.getBooks();
        if (mAdapter == null) {
            mAdapter = new BookAdapter(books);
            mBookRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private class BookHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Book mBook;
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private CheckBox mReadedCheckBox;

        public BookHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_book, parent, false));
            mTitleTextView = itemView.findViewById(R.id.list_item_book_title_text_view);
            mDateTextView = itemView.findViewById(R.id.list_item_book_date_text_view);
            mReadedCheckBox = itemView.findViewById(R.id.list_item_book_readed_check_box);
            itemView.setOnClickListener(this);
        }

        public void bind(Book book) {
            mBook = book;
            mTitleTextView.setText(mBook.getTitle());
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            mDateTextView.setText(sdf.format(mBook.getDate()));
            mReadedCheckBox.setChecked(mBook.isReaded());
        }

        @Override
        public void onClick(View v) {

            Intent intent = BookPagerActivity.newIntent(getActivity(), mBook.getId());
            startActivity(intent);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private class BookAdapter extends RecyclerView.Adapter<BookHolder> {
        private List<Book> mBooks;

        public BookAdapter(List<Book> books) {
            mBooks = books;
        }

        @Override
        public BookHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new BookHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(BookHolder holder, int position) {
            Book book = mBooks.get(position);
            holder.bind(book);
        }

        @Override
        public int getItemCount() {
            return mBooks.size();
        }
    }
}