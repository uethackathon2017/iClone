package com.iClone.AlarmChallenge;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.webkit.WebView;

import com.viewpagerindicator.TitlePageIndicator;

public class AboutTabbedActivity extends AppCompatActivity {

    
    private SectionsPagerAdapter mSectionsPagerAdapter;

    
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.setTheme(getBaseContext(), AboutTabbedActivity.this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about_tabbed);

        setTitle(getString(R.string.about));

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        
        
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager());

        
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TitlePageIndicator titlePageIndicator = (TitlePageIndicator)
                findViewById(R.id.about_titles);

        titlePageIndicator.setViewPager(mViewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    
    public static class PlaceholderFragment extends Fragment {
        
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_about_tabbed,
                    container, false);

            WebView webView = (WebView) rootView.findViewById(
                    R.id.section_about_wb);

            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    webView.loadUrl("file:///android_asset/about.html");
                    break;
                case 2:
                    webView.loadUrl("file:///android_asset/license.html");
                    break;
                case 3:
                    webView.loadUrl("file:///android_asset/sources.html");
                    break;
                case 4:
                    webView.loadUrl("file:///android_asset/libraries.html");
                    break;
                case 5:
                    webView.loadUrl("file:///android_asset/art.html");
                    break;
                case 6:
                    webView.loadUrl("file:///android_asset/apache_license.html");
                    break;
                default:
                    webView.loadUrl("file:///android_asset/about.html");
            }

            return rootView;
        }
    }

    
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            
            
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            
            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "ABOUT";
                case 1:
                    return "LICENSE";
                case 2:
                    return "SOURCE CODE";
                case 3:
                    return "LIBRARIES";
                case 4:
                    return "ART";
                case 5:
                    return "APACHE LICENSE";
            }
            return null;
        }
    }
}
