package com.madeinhk.english_chinesedictionary;


import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.madeinhk.model.AppPreference;
import com.madeinhk.model.ECDictionary;
import com.madeinhk.model.Favourite;
import com.madeinhk.model.Word;
import com.madeinhk.view.LevelIndicator;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private TextView mPhoneticTextView;
    private LevelIndicator mCommonnessBar;
    private CoordinatorLayout mRootView;

    private ImageButton mPronounceButton;

    private TextToSpeech mTts;
    private ECDictionary mECDictionary;
    private Word mWord;
    private Context mContext;
    private int mAccentColor;
    private FloatingActionButton mFavButton;
    private Typeface sNotoFont;

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
        if (!TextUtils.isEmpty(word)) {
            Bundle args = new Bundle();
            args.putString(ARG_WORD, word);
            fragment.setArguments(args);
        }
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
        } else {
            if (getArguments() != null) {
                searchWord = getArguments().getString(ARG_WORD);
            } else {
                String lastWord = AppPreference.getKeyLastWord(mContext);
                if (!TextUtils.isEmpty(lastWord)) {
                    searchWord = lastWord;
                }
            }
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
        mRootView = (CoordinatorLayout) view.findViewById(R.id.rootLayout);
        mPronounceButton = (ImageButton) view.findViewById(R.id.pronounce);

        mWordTextView = (TextView) view.findViewById(R.id.word);
        mPhoneticTextView = (TextView) view.findViewById(R.id.phonetic_string);
        mDetailTextView = (TextView) view.findViewById(R.id.detail);

        if (sNotoFont == null) {
            sNotoFont = Typeface.createFromAsset(mContext.getAssets(), "NotoSans-Regular.ttf");
        }
        mDetailTextView.setTypeface(sNotoFont);

        mCommonnessBar = (LevelIndicator) view.findViewById(R.id.commonness_bar);
        mCommonnessBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int stringResId = R.string.common_word;
                switch (mCommonnessBar.getLevel()) {
                    case 0:
                        stringResId = R.string.very_rare_word;
                        break;
                    case 1:
                        stringResId = R.string.rare_word;
                        break;
                    case 2:
                        stringResId = R.string.medium_word;
                        break;
                    case 3:
                        stringResId = R.string.common_word;
                        break;
                    case 4:
                        stringResId = R.string.very_common_word;
                        break;
                }
                Snackbar.make(mRootView, stringResId, Snackbar.LENGTH_LONG).show();
            }
        });

        mFavButton = (FloatingActionButton) view.findViewById(R.id.fav_button);
        mFavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Favourite fav = Favourite.fromWord(mWord);
                boolean isFav = fav.isExists(mContext);
                if (isFav) {
                    fav.delete(mContext);
                } else {
                    fav.save(mContext);
                }
                updateFavFab(mWord);
            }
        });
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
                if (isAdded()) {
                    buildHtmlFromDictionary(word);
                }
            }
        }.execute(query);
    }

    private void appendStyled(SpannableStringBuilder builder, String str, Object... spans) {
        builder.append(str);
        for (Object span : spans) {
            builder.setSpan(span, builder.length() - str.length(), builder.length(), Spanned
                    .SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }

    private void boldKeyWord(SpannableStringBuilder builder, String str, String keyword) {
        String patternString = "\\b" + keyword + "\\b";
        Pattern boldKeyWordPattern = Pattern.compile(patternString);
        Matcher boldKeyWordMatcher = boldKeyWordPattern.matcher(str);

        while (boldKeyWordMatcher.find()) {
            int start = builder.length() - str.length() + boldKeyWordMatcher.start();
            int end = start + keyword.length();
            builder.setSpan(new android.text.style.StyleSpan
                    (Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }

    private void updateFavFab(Word word) {
        if (word != null) {
            Favourite favourite = Favourite.fromWord(word);
            boolean alreadyMarked = favourite.isExists(mContext);
            mFavButton.setImageResource((alreadyMarked) ? R.drawable.ic_favorite_white_48dp : R
                    .drawable.ic_favorite_border_white_48dp);
            mFavButton.show();
        } else {
            mFavButton.hide();
        }
    }

    final static int INDENTATION_MEANING_LEFT = 50;
    final static int INDENTATION_EXAMPLE_LEFT = 130;

    private void buildHtmlFromDictionary(Word word) {
        mWord = word;
        updateFavFab(word);
        if (word != null) {
            mWordTextView.setText(word.mWord);

            if (!TextUtils.isEmpty(mWord.mPhoneticString)) {
                mPhoneticTextView.setText(mWord.mPhoneticString);
            }

            if (mWord.mDifficulty > 0) {
                mCommonnessBar.setVisibility(View.VISIBLE);
                mCommonnessBar.setLevel((5 - mWord.mDifficulty));
            } else {
                mCommonnessBar.setVisibility(View.GONE);
            }
            List<Word.TypeEntry> typeEntries = word.mTypeEntry;
            SpannableStringBuilder builder = new SpannableStringBuilder();
            int prevType = -1;
            boolean firstEntry = true;
            for (Word.TypeEntry typeEntry : typeEntries) {
                if (prevType != typeEntry.mType) {
                    if (!firstEntry) {
                        builder.append("\n");
                    }
                    firstEntry = false;
                    appendStyled(builder, typeEntry.getTypeDescription() + "\n",
                            new ForegroundColorSpan(mAccentColor));
                    prevType = typeEntry.mType;
                }
                appendStyled(builder, "• " + typeEntry.mMeaning + "\n", new LeadingMarginSpan
                        .Standard(INDENTATION_MEANING_LEFT, INDENTATION_EXAMPLE_LEFT));

                if (!TextUtils.isEmpty(typeEntry.mEngExample)) {
                    appendStyled(builder, typeEntry.mEngExample + "\n", new LeadingMarginSpan
                            .Standard(INDENTATION_EXAMPLE_LEFT));
                    boldKeyWord(builder, typeEntry.mEngExample + "\n", word.mWord);
                }
                if (!TextUtils.isEmpty(typeEntry.mChiExample)) {
                    appendStyled(builder, typeEntry.mChiExample + "\n",
                            new LeadingMarginSpan.Standard(INDENTATION_EXAMPLE_LEFT));
                }
            }
            mDetailTextView.setText(builder);
            mPronounceButton.setVisibility(View.VISIBLE);
            mPhoneticTextView.setVisibility(View.VISIBLE);
            AppPreference.saveLastWord(mContext, word.mWord);
        } else {
            mWordTextView.setText("No such word :(");
            mCommonnessBar.setVisibility(View.GONE);
            mDetailTextView.setText("");
            mPronounceButton.setVisibility(View.GONE);
            mPhoneticTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
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
        if (TextUtils.isEmpty(word)) {
            word = AppPreference.getKeyLastWord(mContext);
        }
        if (!word.equals(mWord)) {
            executeQueryTask(word);
        }
    }
}
