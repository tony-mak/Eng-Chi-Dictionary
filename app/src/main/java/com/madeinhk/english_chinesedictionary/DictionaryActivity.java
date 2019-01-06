package com.madeinhk.english_chinesedictionary;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.madeinhk.app.AboutFragment;
import com.madeinhk.model.AppPreference;
import com.madeinhk.model.ECDictionary;
import com.madeinhk.utils.Analytics;

import de.greenrobot.event.EventBus;


public class DictionaryActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private interface PagePos {
        int EMPTY = -1;
        int DICTIONARY = 0;
        int FAVOURITE = 1;
        int SETTINGS = 2;
        int ABOUT = 3;
    }

    private static final String TAG = "DictionaryActivity";
    public static final String ACTION_VIEW_WORD = "android.intent.action.VIEW_WORD";
    public static final String ACTION_SETTINGS = "android.intent.action.SETTINGS";

    private static final String KEY_CURRENT_PAGE = "current_page";
    private static final String KEY_EXPANDED_SEARCH_VIEW = "expanded_search_view";

    private int mCurrentPage = PagePos.EMPTY;

    private boolean mExpandedSearchView = false;
    private String mWord;

    private boolean mIsVisible;

    private static final String EXTRA_FROM_TOAST = "from_toast";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        setupToolBar();
        setupNavigationView();

        if (savedInstanceState == null) {
            handleIntent(getIntent());
            Analytics.trackAppLaunch(this);
        } else {
            mCurrentPage = savedInstanceState.getInt(KEY_CURRENT_PAGE);
            mExpandedSearchView = savedInstanceState.getBoolean(KEY_EXPANDED_SEARCH_VIEW);
        }

        if (!AppPreference.getShowedTutorial(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.see_tut_title);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://youtu.be/NaqPMAnXcJU"));
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
            AppPreference.saveShowedTutorial(DictionaryActivity.this, true);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        if (getIntent().getBooleanExtra(EXTRA_FROM_TOAST, false)) {
            handleIntent(getIntent());
        }
        mIsVisible = true;
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mIsVisible = false;
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_PAGE, mCurrentPage);
        outState.putBoolean(KEY_EXPANDED_SEARCH_VIEW, mExpandedSearchView);

    }

    private void setupToolBar() {
        mToolbar = findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(mToolbar);
    }

    private void showFragment(Fragment fragment, int page) {
        FragmentManager fragmentManager = DictionaryActivity.this.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager
                .beginTransaction()
                .replace(R.id.content, fragment);
        transaction.commit();
        mCurrentPage = page;
    }

    private void setupNavigationView() {
        BottomNavigationView navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        Fragment fragment = null;
                        int position = PagePos.EMPTY;
                        switch (menuItem.getItemId()) {
                            case R.id.nav_dictionary:
                                fragment = DictionaryFragment.newInstance(null);
                                position = PagePos.DICTIONARY;
                                break;
                            case R.id.nav_favourite:
                                fragment = FavouriteFragment.newInstance();
                                position = PagePos.FAVOURITE;
                                break;
                            case R.id.nav_about:
                                fragment = new AboutFragment();
                                position = PagePos.ABOUT;
                                break;
                        }
                        if (mCurrentPage != position) {
                            showFragment(fragment, position);
                        }
                        return true;
                    }
                });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if (mIsVisible) {
            handleIntent(intent);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.search);
        if (!mExpandedSearchView && TextUtils.isEmpty(mWord)) {
            mExpandedSearchView = true;
            MenuItemCompat.expandActionView(searchItem);
        }
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // FIXME: How to clear search icon focus
                MenuItemCompat.collapseActionView(searchItem);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                MenuItemCompat.collapseActionView(searchItem);
                return false;
            }
        });
        return true;
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction()) || ("com.google.android.gms.actions" +
                ".SEARCH_ACTION").equals(intent.getAction())) {
            String word = intent.getStringExtra(SearchManager.QUERY);
            showWord(word);
        } else if (ACTION_VIEW_WORD.equals(intent.getAction())) {
            Uri data = intent.getData();
            ECDictionary ecDictionary = new ECDictionary(this);
            String word = ecDictionary.lookupFromId(data.getLastPathSegment()).mWord;
            showWord(word);
        } else {
            showWord(null);
        }
    }

    private void showWord(String word) {
        mWord = word;
        if (mCurrentPage != PagePos.DICTIONARY) {
            Fragment fragment = DictionaryFragment.newInstance(word);
            showFragment(fragment, PagePos.DICTIONARY);
        } else {
            EventBus.getDefault().post(new DictionaryFragment.UpdateWordEvent(word));
        }
    }

    public void onEvent(LookupWordEvent event) {
        String word = event.word;
        Fragment fragment = DictionaryFragment.newInstance(word);
        showFragment(fragment, PagePos.DICTIONARY);
    }

    public static class LookupWordEvent {
        public String word;

        public LookupWordEvent(String word) {
            this.word = word;
        }
    }

    public static Intent getIntent(Context context, String word) {
        Intent intent = new Intent(context, DictionaryActivity.class);
        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(SearchManager.QUERY, word);
        intent.putExtra(EXTRA_FROM_TOAST, true);
        return intent;
    }
}
