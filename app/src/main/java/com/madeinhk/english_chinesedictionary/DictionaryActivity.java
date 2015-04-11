package com.madeinhk.english_chinesedictionary;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.madeinhk.app.AboutFragment;
import com.madeinhk.english_chinesedictionary.service.ClipboardService;
import com.madeinhk.model.AppPreference;
import com.madeinhk.model.ECDictionary;
import com.madeinhk.utils.Analytics;

import de.greenrobot.event.EventBus;


public class DictionaryActivity extends ActionBarActivity {
    private static interface PagePos {
        public static final int EMPTY = -1;
        public static final int DICTIONARY = 0;
        public static final int FAVOURITE = 1;
        public static final int ABOUT = 2;
    }

    private static final String TAG = "DictionaryActivity";
    public static final String ACTION_VIEW_WORD = "android.intent.action.VIEW_WORD";

    private static final String KEY_CURRENT_PAGE = "current_page";

    private static final String[] ITEM_NAMES = new String[]{"Dictionary", "Saved words", "About"};
    private DrawerLayout mDrawerLayout;
    private RecyclerView mDrawerList;
    private int mCurrentPage = PagePos.EMPTY;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean mIsVisible;

    private static final String EXTRA_FROM_TOAST = "from_toast";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        setupToolBar();
        setupDrawerLayout();

        ClipboardService.start(this);

        if (savedInstanceState == null) {
            handleIntent(getIntent());
            Analytics.trackAppLaunch(this);
        } else {
            mCurrentPage = savedInstanceState.getInt(KEY_CURRENT_PAGE);
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
    }

    private void setupToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void showFragment(Fragment fragment, int page) {
        FragmentManager fragmentManager = DictionaryActivity.this.getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.animator.slide_from_bottom_in, R.animator.slide_from_bottom_out)
                .replace(R.id.content, fragment)
                .commit();
        mCurrentPage = page;
    }


    private void selectDrawerItem(int pos) {
//        mDrawerList.setItemChecked(pos, true);
        setTitle(ITEM_NAMES[pos]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void setupDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (RecyclerView) findViewById(R.id.left_drawer);


        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerList.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mDrawerList.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        String[] texts = new String[]{"Dictionary", "Saved words", "About"};
        int[] icons = new int[]{R.drawable.ic_magnify_grey600_24dp, R.drawable.ic_star_grey600_24dp, R.drawable.ic_information_grey600_24dp};


        OnItemClickListener onItemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Fragment fragment = null;
                switch (position) {
                    case PagePos.DICTIONARY:
                        fragment = DictionaryFragment.newInstance(null);
                        break;
                    case PagePos.FAVOURITE:
                        fragment = FavouriteFragment.newInstance();
                        break;
                    case PagePos.ABOUT:
                        fragment = new AboutFragment();
                }
                selectDrawerItem(position);
                if (mCurrentPage != position) {
                    showFragment(fragment, position);
                }
            }
        };

        MyAdapter mAdapter = new MyAdapter(texts, icons, onItemClickListener);
        mDrawerList.setAdapter(mAdapter);

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
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        if (mCurrentPage == PagePos.DICTIONARY && Intent.ACTION_MAIN.equals(getIntent().getAction())) {
            searchItem.expandActionView();
        }
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
        String word = null;
        if (Intent.ACTION_SEARCH.equals(intent.getAction()) || "com.google.android.gms.actions.SEARCH_ACTION".equals(intent.getAction())) {
            word = intent.getStringExtra(SearchManager.QUERY);
        } else if (ACTION_VIEW_WORD.equals(intent.getAction())) {
            Uri data = intent.getData();
            ECDictionary ecDictionary = new ECDictionary(this);
            word = ecDictionary.lookupFromId(data.getLastPathSegment()).mWord;
        }
        showWord(word);
    }

    private void showWord(String word) {
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

    interface OnItemClickListener {
        void onItemClick(View v, int position);
    }


    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private String[] mTexts;
        private int[] mImageRes;
        private OnItemClickListener mOnItemClickListener;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public static class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView mTextView;
            public ImageView mImageView;

            public ViewHolder(View v, final OnItemClickListener listener) {
                super(v);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(v, getAdapterPosition());
                    }
                });
                mTextView = (TextView) v.findViewById(R.id.text);
                mImageView = (ImageView) v.findViewById(R.id.icon);
            }

        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(String[] text, int[] imageRes, OnItemClickListener onItemClickListener) {
            mTexts = text;
            mImageRes = imageRes;
            mOnItemClickListener = onItemClickListener;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.drawer_list_item, parent, false);
            // set the view's size, margins, paddings and layout parameters
            ViewHolder vh = new ViewHolder(v, mOnItemClickListener);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.mTextView.setText(mTexts[position]);
            holder.mImageView.setImageResource(mImageRes[position]);

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mTexts.length;
        }
    }

}
