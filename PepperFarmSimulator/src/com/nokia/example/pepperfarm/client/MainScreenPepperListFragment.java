/**
 * Copyright (c) 2014 Microsoft Mobile and/or its subsidiary(-ies).
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.pepperfarm.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.nokia.example.pepperfarm.product.Content;
import com.nokia.example.pepperfarm.product.Content.Product;
import com.nokia.example.pepperfarm.product.adapters.ContentListAdapter;

import java.util.List;

/**
 * This is the Fragment for the main screen of the Pepper Farm Simulator.
 * Mostly it's job is to set up the ContentListAdapter which does the real work of determining how to
 * display the list of purchased products.
 */
public class MainScreenPepperListFragment extends ListFragment {


    public static TextView getpeppers;
    public static View fetch_peppers_progressbar;
    public static TextView fetching_peppers;

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;
    public static MainScreenPepperListFragment reference;

    private final static String LOG_TAG = MainScreenPepperListFragment.class.getCanonicalName();

    /**
     * The adapter used to display the list of peppers that have already been purchased.
     */
    public ArrayAdapter<Product> adapter;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         *
         * @param id
         */
        public void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MainScreenPepperListFragment() {
    }


    /**
     * Renders a Fragment representing one pepper in the list that has already been purchased by the user.
     * Also is responsible for updating the list
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reference = this;
        List<Product> purchasedItems = Content.getPurchasedItems();

        if (purchasedItems.isEmpty()) {
            //We need to display some text saying you have no peppers.
            adapter = new ContentListAdapter(getActivity(), R.layout.pepper_list_row_view, purchasedItems);
            setListAdapter(adapter);
        } else {
            adapter = new ContentListAdapter(getActivity(), R.layout.pepper_list_row_view, purchasedItems);
            setListAdapter(adapter);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fetching_peppers = (TextView) view.findViewById(R.id.fetching_peppers_text);
        fetch_peppers_progressbar = view.findViewById(R.id.fetching_peppers_progressbar);
        //Get new peppers linkbutton
        getpeppers = (TextView) view.findViewById(R.id.get_new_peppers_link);
        getpeppers.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.PRODUCT_LIST");
                startActivity(intent);
            }

        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.main_screen_pepper_list_fragment, container, false);
    }

    /**
     * Used for error checking to make sure that any Activity attaching to this fragment properly supports
     * Callbacks.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    /**
     * Notifies the activity that an item has been selected.
     */
    @Override
    public void onListItemClick(ListView listView, View view, int position,
                                long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(Content.ITEMS.get(position).productId);
    }

    /**
     * On resume of this Fragment, we need to refresh the list of peppers available to the user.
     * This is because peppers could have been bought and the view may not have an updated list of purchased peppers.
     * This is a little inefficient but simple enough for demonstration purposes. If you had a large list of items to display,
     * you may want to consider having a listener that gets updated when items are purchased. The listener could then update the adapter
     * only when items are purchased or consumed.
     */
    @Override
    public void onResume() {

        super.onResume();
        if (adapter != null) {
            adapter.clear();
            adapter.addAll(Content.getPurchasedItems());
            adapter.notifyDataSetChanged();
        } else {
            System.out.println("Adapter is null");
        }
        Log.d(LOG_TAG, "List Frag Resumed.");

    }


}
