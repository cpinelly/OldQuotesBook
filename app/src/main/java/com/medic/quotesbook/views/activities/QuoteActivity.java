package com.medic.quotesbook.views.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.ShareActionProvider;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.medic.quotesbook.AppController;
import com.medic.quotesbook.R;
import com.medic.quotesbook.models.Quote;
import com.medic.quotesbook.utils.AdsKeys;
import com.medic.quotesbook.utils.GAK;
import com.medic.quotesbook.utils.QuotesStorage;
import com.medic.quotesbook.views.widgets.RoundedImageNetworkView;
import com.tappx.TAPPXAdBanner;


public class QuoteActivity extends AdActivity {

    final String TAG = this.getClass().getSimpleName();

    static final String SCREEN_NAME = "Quote Details";
    public static final String SCREEN_NAME_DAYQUOTE = "Day Quote";

    public static final String QUOTE_KEY = "quotesbook.quote";
    public static final String DAYQUOTE_KEY = "quotesbook.day_quote"; // Indica si es la cita del día

    TextView quoteBodyView;
    TextView authorNameView;
    RoundedImageNetworkView authorPictureView;
    TextView authorDescriptionView;


    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    private Quote quote;

    Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote);

        initAds();

        tracker = getAppCtrl().getDefaultTracker();

        quoteBodyView = (TextView) findViewById(R.id.quote_body);
        authorNameView = (TextView) findViewById(R.id.author_name);
        authorDescriptionView = (TextView) findViewById(R.id.author_description);
        authorPictureView = (RoundedImageNetworkView) findViewById(R.id.author_picture);

    }

    public void setupContent(){

        quoteBodyView.setText(quote.getBody());

        if (quote.getAuthor() != null){
            authorNameView.setText("- "  + quote.getAuthor().getFullName());
            authorDescriptionView.setText(quote.getAuthor().getShortDescription());
        }

        authorPictureView.setImageUrl(quote.getAuthor().getFullPictureURL(), imageLoader);
    }

    public void setSavedIcon(FloatingActionButton btn){
        btn.setImageResource(R.drawable.ic_star_white_24dp);
    }

    public void setUnsavedIcon(FloatingActionButton btn){
        btn.setImageResource(R.drawable.ic_star_border_white_24dp);
    }

    public void setupFAB(){

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        final Context ctx = this;
        final boolean savedIcon;

        final QuotesStorage qStorage = new QuotesStorage(QuotesStorage.QUOTESBOOK_FILE, this);

        if (qStorage.findQuote(quote.getKey()) != -1 ){
            savedIcon = true;
            setSavedIcon(fab);
        }else{
            savedIcon= false;
        }


        fab.setOnClickListener(new OnClickListener(){

            private boolean saved = savedIcon;

            @Override
            public void onClick(View view) {

                HitBuilders.EventBuilder event = new HitBuilders.EventBuilder();

                FloatingActionButton btn = (FloatingActionButton) view;

                if (saved == false){

                    qStorage.addQuoteTop(quote);
                    qStorage.commit();

                    Toast toast = Toast.makeText(ctx, R.string.message_quote_saved, Toast.LENGTH_SHORT);
                    toast.show();

                    setSavedIcon(btn);

                    event.setCategory(GAK.CATEGORY_QUOTESBOOK);
                    event.setAction(GAK.ACTION_QUOTE_SAVED);

                    tracker.send(event.build());

                }else{

                    qStorage.removeQuote(quote.getKey());
                    qStorage.commit();

                    Toast toast = Toast.makeText(ctx, R.string.message_quote_unsaved, Toast.LENGTH_SHORT);
                    toast.show();

                    btn.setImageResource(R.drawable.ic_star_border_white_24dp);

                    setUnsavedIcon(btn);

                    event.setCategory(GAK.CATEGORY_QUOTESBOOK);
                    event.setAction(GAK.ACTION_QUOTE_UNSAVED);

                    tracker.send(event.build());
                }

                saved = !saved;

            }
        });

        // If no Ads

        if (!getAppCtrl().isAdsActive()){

            //// dp to px
            Resources r = this.getResources();
            int px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    25,
                    r.getDisplayMetrics()
            );
            ////

            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) fab.getLayoutParams();
            lParams.bottomMargin = px;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        quote = this.getIntent().getParcelableExtra(QUOTE_KEY);

        setupContent();
        setupFAB();

        getSupportActionBar().setTitle(quote.getAuthor().getFullName());

        if (this.getIntent().getBooleanExtra(DAYQUOTE_KEY, false))
            tracker.setScreenName(SCREEN_NAME_DAYQUOTE);
        else
            tracker.setScreenName(SCREEN_NAME);

        tracker.send(new HitBuilders.EventBuilder().build());

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_quote, menu);

        MenuItem item = menu.findItem(R.id.action_share);

        ShareActionProvider actionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, quote.getShareable());
        intent.setType("text/plain");
        Intent.createChooser(intent, "QuotesBook");

        actionProvider.setShareIntent(intent);

        return true;
    }



    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    */
}
