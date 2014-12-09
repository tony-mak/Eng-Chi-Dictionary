package com.madeinhk.english_chinesedictionary;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.madeinhk.english_chinesedictionary.service.ClipboardService;
import com.madeinhk.model.ECDictionary;

import de.greenrobot.event.EventBus;


public class DictionaryActivity extends ActionBarActivity {
    private static interface PagePos {
        public static final int DICTIONARY = 0;
        public static final int FAVOURITE = 1;
        public static final int SETTING = 2;
    }

    private static final String TAG = "DictionaryActivity";
    public static final String ACTION_VIEW_WORD = "android.intent.action.VIEW_WORD";

    private static final String DEFAULT_WORD = "welcome";

    private static final String[] ITEM_NAMES = new String[]{"Dictionary", "Favourite"};
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private boolean mIsVisible = false;

    private ActionBarDrawerToggle mDrawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        setupToolBar();
        setupDrawerLayout();

        ClipboardService.start(this);

        if (savedInstanceState == null) {
            handleIntent(getIntent());
        } else {
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mIsVisible = true;
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mIsVisible = false;
        EventBus.getDefault().unregister(this);
    }

    private void setupToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = DictionaryActivity.this.getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
    }


    private void selectDrawerItem(int pos) {
        mDrawerList.setItemChecked(pos, true);
        setTitle(ITEM_NAMES[pos]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void setupDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, ITEM_NAMES));

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Fragment fragment = null;
                switch (i) {
                    case PagePos.DICTIONARY:
                        fragment = DictionaryFragment.newInstance(DEFAULT_WORD);
                        break;
                    case PagePos.FAVOURITE:
                        fragment = FavouriteFragment.newInstance();
                        break;
                }
                selectDrawerItem(i);
                showFragment(fragment);
            }
        });
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (mIsVisible) {
            setIntent(intent);
            handleIntent(intent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.search);
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
        Fragment fragment = null;
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String word = intent.getStringExtra(SearchManager.QUERY);
            fragment = DictionaryFragment.newInstance(word);
            selectDrawerItem(PagePos.DICTIONARY);
        } else if (ACTION_VIEW_WORD.equals(intent.getAction())) {
            Uri data = intent.getData();
            ECDictionary ecDictionary = new ECDictionary(this);
            String word = ecDictionary.lookupFromId(data.getLastPathSegment()).mWord;
            fragment = DictionaryFragment.newInstance(word);
            selectDrawerItem(PagePos.DICTIONARY);
        } else {
            fragment = DictionaryFragment.newInstance(DEFAULT_WORD);
            selectDrawerItem(PagePos.DICTIONARY);
        }
        showFragment(fragment);
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

    public void onEvent(LookupWordEvent event) {
        String word = event.word;
        Fragment fragment = DictionaryFragment.newInstance(word);
        showFragment(fragment);
        selectDrawerItem(PagePos.DICTIONARY);
    }

    public static class LookupWordEvent {
        public String word;

        public LookupWordEvent(String word) {
            this.word = word;
        }
    }

}
