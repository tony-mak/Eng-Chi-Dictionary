package com.madeinhk.english_chinesedictionary;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.madeinhk.model.ECDictionary;
import com.madeinhk.model.Word;
import com.mikepenz.lollipopshowcase.itemanimator.CustomItemAnimator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.greenrobot.event.EventBus;

public class FavouriteFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private CardListAdapter mAdapter;
    private View mEmptyView;

    public FavouriteFragment() {

    }

    public static FavouriteFragment newInstance() {
        FavouriteFragment fragment = new FavouriteFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favourite, container, false);
        mEmptyView = view.findViewById(R.id.empty_view);


        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);

        // use a linear layout manager
        mAdapter = new CardListAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRecyclerView.setItemAnimator(new CustomItemAnimator());

        return view;
    }

    private class UpdateFavTask extends AsyncTask<Void, Void, List<Word>> {

        @Override
        protected List<Word> doInBackground(Void... params) {
            ECDictionary ecDictionary = new ECDictionary(getActivity().getApplicationContext());
            List<Word> favouriteWords = ecDictionary.getAllFavouriteWords();
            favouriteWords = new AlgebraicOrderSorter().sort(favouriteWords);
            return favouriteWords;
        }

        @Override
        protected void onPostExecute(List<Word> wordList) {
            showHideEmptyView(wordList.size() > 0);
            mAdapter.setData(wordList);
            mAdapter.notifyItemRangeInserted(0, wordList.size());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void showHideEmptyView(boolean hasData) {
        if (hasData) {
            mEmptyView.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        new UpdateFavTask().execute();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public static class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.ViewHolder> {
        private List<SectionItem> mSections = new ArrayList<>();

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public static class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public RecyclerView mRecycleView;

            public ViewHolder(View v) {
                super(v);
                mRecycleView = (RecyclerView) v.findViewById(R.id.words_list);
            }
        }

        private Context mContext;

        public CardListAdapter(Context context) {
            mContext = context;
        }

        public void setData(List<Word> wordList) {
            mSections.clear();
            Character lastInitial = null;
            List<Word> tmpList = new ArrayList<>();
            for (Word word : wordList) {
                Character initial = Character.toLowerCase(word.mWord.charAt(0));
                if (!initial.equals(lastInitial)) {
                    if (tmpList.size() > 0) {
                        mSections.add(new SectionItem(lastInitial, tmpList));
                    }
                    tmpList = new ArrayList<>();
                    lastInitial = initial;
                }
                tmpList.add(word);
            }
            if (tmpList.size() > 0) {
                mSections.add(new SectionItem(lastInitial, tmpList));
            }
        }

        // Create new views (invoked by the layout manager)
        @Override
        public CardListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
            // create a new view
            CardView v = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.favourite_card_item, parent, false);

            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            // use a linear layout manager
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
            holder.mRecycleView.setLayoutManager(mLayoutManager);
            holder.mRecycleView.setHasFixedSize(true);
            holder.mRecycleView.setAdapter(new WordListAdapter(mSections.get(position)));

            LinearLayoutManager mgr = new LinearLayoutManager(mContext);
            holder.mRecycleView.setLayoutManager(mgr);
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mSections.size();
        }

        static class SectionItem {
            public SectionItem(Character initial, List<Word> words) {
                this.mInitial = initial;
                this.mWords = words;
            }

            public Character mInitial;
            public List<Word> mWords;
        }

        public static class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.ViewHolder> {
            static final int TYPE_HEADER = 0;
            static final int TYPE_DATA = 1;

            private CardListAdapter.SectionItem mSection;

            public WordListAdapter(CardListAdapter.SectionItem sectionItem) {
                mSection = sectionItem;
            }

            public static class ViewHolder extends RecyclerView.ViewHolder {
                private TextView mTextView;

                public ViewHolder(TextView v) {
                    super(v);
                    mTextView = v;
                }
            }

            @Override
            public WordListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                TextView textView;
                if (viewType == TYPE_HEADER) {
                    textView = (TextView) LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.favourite_word_item_header, parent, false);
                } else {
                    textView = (TextView) LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.favourite_word_item, parent, false);
                }
                return new ViewHolder(textView);
            }

            @Override
            public int getItemViewType(int position) {
                return (position == 0) ? TYPE_HEADER : TYPE_DATA;
            }

            @Override
            public void onBindViewHolder(WordListAdapter.ViewHolder holder, int position) {
                int itemType = getItemViewType(position);
                if (itemType == TYPE_HEADER) {
                    TextView tv = holder.mTextView;
                    tv.setText(String.valueOf(Character.toUpperCase(mSection.mInitial)));
                } else {
                    TextView tv = holder.mTextView;
                    final String word = mSection.mWords.get(position - 1).mWord;
                    tv.setText(word);
                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EventBus.getDefault().post(new DictionaryActivity.LookupWordEvent(word));
                        }
                    });
                }
            }

            @Override
            public int getItemCount() {
                return mSection.mWords.size() + 1;
            }
        }
    }

    interface WordSorter {
        public List<Word> sort(List<Word> words);
    }

    static class AlgebraicOrderSorter implements WordSorter {
        @Override
        public List<Word> sort(List<Word> words) {
            Collections.sort(words, new Comparator<Word>() {
                @Override
                public int compare(Word lhs, Word rhs) {
                    return lhs.mWord.compareToIgnoreCase(rhs.mWord);
                }
            });
            return words;
        }
    }
}
