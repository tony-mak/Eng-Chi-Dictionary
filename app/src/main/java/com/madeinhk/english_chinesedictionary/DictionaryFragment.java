package com.madeinhk.english_chinesedictionary;


import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.madeinhk.model.ECDictionary;
import com.madeinhk.model.Favourite;
import com.madeinhk.model.Word;

import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DictionaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DictionaryFragment extends Fragment implements TextToSpeech.OnInitListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_WORD = "word";
    private static final String TAG = DictionaryFragment.class.getName();


    private TextView mWordTextView;
    private TextView mDetailTextView;

    private ImageButton mPronounceButton;

    private TextToSpeech mTts;
    private ECDictionary mECDictionary;
    private Word mWord;
    private Context mContext;
    private int mAccentColor;
    private MenuItem mFavouriteItem;

    public DictionaryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param word Word to be queried
     * @return A new instance of fragment DictionaryFragment.
     */
    public static DictionaryFragment newInstance(String word) {
        DictionaryFragment fragment = new DictionaryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_WORD, word);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mAccentColor = getResources().getColor(R.color.colorAccent);
        mECDictionary = new ECDictionary(mContext);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        initTts();
        String searchWord = null;
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            searchWord = savedInstanceState.getString(SearchManager.QUERY);
        } else if (getArguments() != null) {
            searchWord = getArguments().getString(ARG_WORD);
        }
        executeQueryTask(searchWord);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getApplicationContext();
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dictionary, container, false);
        mPronounceButton = (ImageButton) view.findViewById(R.id.pronounce);

        mWordTextView = (TextView) view.findViewById(R.id.word);
        mDetailTextView = (TextView) view.findViewById(R.id.detail);
        mDetailTextView.setMovementMethod(new ScrollingMovementMethod());
        return view;
    }


    private void initTts() {
        mTts = new TextToSpeech(mContext,
                this  // TextToSpeech.OnInitListener
        );
    }


    // Implements TextToSpeech.OnInitListener.
    public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            // Set preferred language to US english.
            // Note that a language may not be available, and the result will indicate this.
            int result = mTts.setLanguage(Locale.US);
            // Try this someday for some interesting results.
            // int result mTts.setLanguage(Locale.FRANCE);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Language data is missing or the language is not supported.
                Log.e(TAG, "Language is not available.");
            } else {
                // Check the documentation for other possible result codes.
                // For example, the language may be available for the locale,
                // but not for the specified country and variant.
                mPronounceButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mTts.speak(mWord.mWord, TextToSpeech.QUEUE_FLUSH, null);
                    }
                });
            }
        } else {
            // Initialization failed.
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown!
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
        super.onDestroy();
    }


    private void executeQueryTask(String query) {
        new AsyncTask<String, Void, Word>() {
            @Override
            protected Word doInBackground(String... params) {
                String query = params[0];
                return mECDictionary.lookup(query);
            }

            @Override
            protected void onPostExecute(Word word) {
                buildHtmlFromDictionary(word);
            }
        }.execute(query);
    }

    private void appendStyled(SpannableStringBuilder builder, String str, Object... spans) {
        builder.append(str);
        for (Object span : spans) {
            builder.setSpan(span, builder.length() - str.length(), builder.length(), 0);
        }
    }

    private void updateFavouriteMenuItem(Word word) {
        if (word != null) {
            Favourite favourite = Favourite.fromWord(word);
            boolean alreadyMarked = favourite.isExists(mContext);
            if (mFavouriteItem != null) {
                mFavouriteItem.setIcon((alreadyMarked) ? R.drawable.ic_star_white_48dp : R.drawable.ic_star_outline_white_48dp);
                mFavouriteItem.setChecked(alreadyMarked);
                mFavouriteItem.setVisible(true);
            }
        } else {
            mFavouriteItem.setVisible(false);
        }
    }

    private void buildHtmlFromDictionary(Word word) {
        if (word != null) {
            mWord = word;
            updateFavouriteMenuItem(word);
            mWordTextView.setText(word.mWord);
            List<Word.TypeEntry> typeEntries = word.mTypeEntry;
            SpannableStringBuilder builder = new SpannableStringBuilder();
            for (Word.TypeEntry typeEntry : typeEntries) {
                appendStyled(builder, typeEntry.getTypeDescription(), new ForegroundColorSpan(mAccentColor));
                builder.append("\n");
                builder.append(typeEntry.mMeaning);
                builder.append("\n");
                if (!TextUtils.isEmpty(typeEntry.mEngExample) && !(TextUtils.isEmpty(typeEntry.mChiExample))) {
                    builder.append(Html.fromHtml(typeEntry.mEngExample));
                    builder.append("\n");
                    builder.append(typeEntry.mChiExample);
                    builder.append("\n");
                }
                builder.append("\n");
            }
            mDetailTextView.setText(builder);
            mPronounceButton.setVisibility(View.VISIBLE);
        } else {
            mWordTextView.setText("No such word :(");
            mDetailTextView.setText("");
            mPronounceButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.word, menu);
        mFavouriteItem = menu.findItem(R.id.favourite);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        updateFavouriteMenuItem(mWord);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.favourite:
                if (item.isChecked()) {
                    Favourite.fromWord(mWord).delete(mContext);
                } else {
                    Favourite.fromWord(mWord).save(mContext);
                }
                updateFavouriteMenuItem(mWord);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mWord != null) {
            outState.putString(SearchManager.QUERY, mWord.mWord);
        }
    }

    public static class UpdateWordEvent {
        public String mWord;
        public UpdateWordEvent(String word) {
            mWord = word;
        }
    }

    public void onEvent(UpdateWordEvent event) {
        String word = event.mWord;
        executeQueryTask(word);
    }

}
