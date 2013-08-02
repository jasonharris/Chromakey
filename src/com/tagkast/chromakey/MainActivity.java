
package com.tagkast.chromakey;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends FragmentActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Fragment fragment = new DummySectionFragment();
            Bundle args = new Bundle();
            args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
            TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
            final int sectionNum = getArguments().getInt(ARG_SECTION_NUMBER);
            dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            getArguments().getInt(ARG_SECTION_NUMBER);
            
            ImageView imageView = (ImageView) rootView.findViewById(R.id.imageView);
            switch (sectionNum) {
                case 1:
                    imageView.setImageResource(R.drawable.test_chromakey);
                    break;
                case 2:
                    imageView.setImageResource(R.drawable.landscape1);
                    break;
                case 3:
                    imageView.setImageBitmap(getChromakey(getActivity()));
                    break;
            }
            return rootView;
        }
    }

    public static Bitmap getChromakey(Context c) {
        Bitmap chroma = BitmapFactory.decodeResource(c.getResources(), R.drawable.test_chromakey);
        Bitmap bg = BitmapFactory.decodeResource(c.getResources(), R.drawable.landscape1);
        
        Bitmap composite = Bitmap.createBitmap(bg.getWidth(), bg.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(composite); 
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        //canvas.drawBitmap(bg, 0, 0, paint); 
        // Slow method: scan all input (layer) image pixels and corresponding background pixels.
        // Calculate its "greenness" and translucency and recreate the pixels' values, plotting
        // them over the background.
        int bgPixel,lPixel;
        float targetHue = 97f/360f;
        float tolerance = 0.1f;
        int bgRed,bgGreen,bgBlue,lRed,lGreen,lBlue,oRed,oGreen,oBlue;
        for(int w = 0; w < bg.getWidth(); w++)
          for(int h = 0; h < bg.getHeight(); h++)
            {
            // Background pixels.
            bgPixel = bg.getPixel(w, h);
            bgRed   = Color.red(bgPixel); // Red level
            bgGreen = Color.green(bgPixel);  // Green level
            bgBlue  = Color.blue(bgPixel);       // Blue level
            // Layer pixels.
            lPixel = chroma.getPixel(w,h);
            lRed   = Color.red(lPixel); // Red level
            lGreen = Color.green(lPixel);  // Green level
            lBlue  = Color.blue(lPixel);       // Blue level
            float[] lHSB = new float[3];
                    
            Color.RGBToHSV(lRed, lGreen, lBlue, lHSB);
            // Calculate the translucency, based on the green value of the layer, using HSB coordinates.
            // To make calculations easier, let's assume that the translucency is a value between 0 
            // (invisible) and 1 (opaque).
            float deltaHue = Math.abs((lHSB[0]/360)-targetHue);
            float translucency = (deltaHue/tolerance);
            translucency = Math.min(translucency,1f);
            // Recalculate the RGB coordinates of the layer and background pixels, using the translucency
            // as a weight.
            oRed = (int)(translucency*lRed+(1-translucency)*bgRed);
            oGreen = (int)(translucency*lGreen+(1-translucency)*bgGreen);
            oBlue = (int)(translucency*lBlue+(1-translucency)*bgBlue);
            // Set the pixel on the output image's raster.
            //raster.setPixel(w+shiftX,h+shiftY,new int[]{oRed,oGreen,oBlue,255});  
            paint = new Paint();
            paint.setColor(Color.rgb(oRed, oGreen, oBlue));
            canvas.drawPoint(w, h, paint);
            } // end for
        return composite;
    }
}
