package com.madeinhk.english_chinesedictionary;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.madeinhk.app.AboutFragment;
import com.madeinhk.english_chinesedictionary.service.ECDictionaryService;
import com.madeinhk.model.AppPreference;
import com.madeinhk.model.ECDictionary;
import com.madeinhk.utils.Analytics;

import java.util.Stack;

import de.greenrobot.event.EventBus;

import static com.madeinhk.english_chinesedictionary.R.id.word;


public class DictionaryActivity extends AppCompatActivity {

    private NavigationView mNavigationView;
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

    private static final int[] ITEM_NAMES = new int[]{R.string.dictionary, R.string.favourites,
            R.string.settings, R.string.about};
    private DrawerLayout mDrawerLayout;
    private int mCurrentPage = PagePos.EMPTY;

    private boolean mExpandedSearchView = false;
    private String mWord;

    private ActionBarDrawerToggle mDrawerToggle;

    private boolean mIsVisible;

    private static final String EXTRA_FROM_TOAST = "from_toast";

    private BackStack mBackStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBackStack = new BackStack();

        setContentView(R.layout.activity_dictionary);

        setupToolBar();
        setupDrawerLayout();
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        setupDrawerContent(mNavigationView);

        ECDictionaryService.start(this);

        if (savedInstanceState == null) {
            handleIntent(getIntent());
            Analytics.trackAppLaunch(this);
        } else {
            mCurrentPage = savedInstanceState.getInt(KEY_CURRENT_PAGE);
            mExpandedSearchView = savedInstanceState.getBoolean(KEY_EXPANDED_SEARCH_VIEW);
            selectDrawerItem(mCurrentPage);
        }

        if (!AppPreference.getShowedTutorial(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.see_tut_title);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://www.youtube.com/watch?v=a5nDV2c04Q4"));
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
        mToolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void selectDrawerItem(int pos) {
        setTitle(ITEM_NAMES[pos]);
        int menuItemId = pagePosToMenuId(pos);
        MenuItem menuItem = mNavigationView.getMenu().findItem(menuItemId);
        menuItem.setChecked(true);
        mDrawerLayout.closeDrawers();

    }

    private int pagePosToMenuId(int pos) {
        switch (pos) {
            case PagePos.DICTIONARY:
                return R.id.nav_dictionary;
            case PagePos.FAVOURITE:
                return R.id.nav_favourite;
            case PagePos.SETTINGS:
                return R.id.settings;
            case PagePos.ABOUT:
                return R.id.nav_about;
        }
        throw new IllegalArgumentException("Invalid Pos");
    }

    private void showFragment(Fragment fragment, int page) {
        FragmentManager fragmentManager = DictionaryActivity.this.getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_from_bottom_in, R.anim
                        .slide_from_bottom_out)
                .replace(R.id.content, fragment);
        Fragment topFragment = fragmentManager.findFragmentById(R.id.content);

        if (page == PagePos.FAVOURITE) {
            mBackStack.clear();
        }
        if (mCurrentPage == PagePos.FAVOURITE && page == PagePos.DICTIONARY) {
            mBackStack.push(topFragment);
        }
        transaction.commit();
        mCurrentPage = page;
    }

    private int fragmentToPos(Fragment fragment) {
        Class<? extends Fragment> fragmentClass = fragment.getClass();
        if (fragmentClass.equals(DictionaryFragment.class)) {
            return 0;
        } else if (fragmentClass.equals(FavouriteFragment.class)) {
            return 1;
        } else if (fragmentClass.equals(AboutFragment.class)) {
            return 2;
        }
        throw new IllegalArgumentException("Illegal fragment: " + fragment);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
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
                            case R.id.settings:
                                fragment = new SettingFragment();
                                position = PagePos.SETTINGS;
                        }
                        if (mCurrentPage != position) {
                            showFragment(fragment, position);
                        }
                        return true;
                    }
                });
    }


    private void setupDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                mToolbar,
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /**
             * Called when a drawer has settled in a completely open state.
             */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
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
        } else if (ACTION_SETTINGS.equals(intent.getAction())) {
            Fragment fragment = new SettingFragment();
            showFragment(fragment, PagePos.SETTINGS);
            selectDrawerItem(PagePos.SETTINGS);
        } else {
            showWord(null);
        }
    }

    private void showWord(String word) {
        mWord = word;
        if (mCurrentPage != PagePos.DICTIONARY) {
            Fragment fragment = DictionaryFragment.newInstance(word);
            selectDrawerItem(PagePos.DICTIONARY);
            showFragment(fragment, PagePos.DICTIONARY);
        } else {
            EventBus.getDefault().post(new DictionaryFragment.UpdateWordEvent(word));
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mBackStack.isEmpty()) {
            super.onBackPressed();
        } else {
            Fragment fragment = mBackStack.pop();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content, fragment, null);
            ft.commit();
            int page = fragmentToPos(fragment);
            mCurrentPage = page;
            selectDrawerItem(page);
        }
    }

    public void onEvent(LookupWordEvent event) {
        String word = event.word;
        Fragment fragment = DictionaryFragment.newInstance(word);
        showFragment(fragment, PagePos.DICTIONARY);
        selectDrawerItem(PagePos.DICTIONARY);
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

    static class BackStack {
        Stack<Fragment> mStack;

        public BackStack() {
            mStack = new Stack<Fragment>();
        }

        public void clear() {
            mStack.clear();
        }

        public void push(Fragment fragment) {
            mStack.push(fragment);
        }

        public Fragment pop() {
            return mStack.pop();
        }

        public boolean isEmpty() {
            return mStack.isEmpty();
        }
    }


}
