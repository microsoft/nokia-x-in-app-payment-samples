/**
 * Copyright (c) 2014 Nokia Corporation and/or its subsidiary(-ies).
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.pepperfarm.product.adapters;


import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.nokia.example.pepperfarm.client.MainScreenPepperListFragment;
import com.nokia.example.pepperfarm.client.PepperFarmMainScreenActivity;
import com.nokia.example.pepperfarm.client.ProductListFragment;
import com.nokia.example.pepperfarm.client.R;
import com.nokia.example.pepperfarm.product.Content;
import com.nokia.example.pepperfarm.product.Content.Product;

import java.util.List;

/**
 * An adapter used to show a list of Peppers you currently own.
 * Used for the MainScreenPepeprListFragment and by association, PepperFarmMainScreenActivity.
 */
public class ContentListAdapter extends ArrayAdapter<Product> {


    /**
     * Constructor used to take in a context, resource, and a List of products.
     *
     * @param context  A context
     * @param resource The resource id of the caller
     * @param objects  A list of products to be displayed
     */
    public ContentListAdapter(Context context,
                              int resource, List<Product> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    /**
     * The context to use
     */
    Context context;


    /**
     * For the Content List Adapter, all items are considered enabled.
     *
     * @param position
     * @return Always returns true
     */
    public boolean isEnabled(int position) {
        return true;
    }

    /**
     * Returns true as all items are considered enabled in the list of peppers purchased.
     *
     * @return true
     */
    public boolean areAllItemsEnabled() {
        return true;
    }

    /**
     * Generates the view representing a pepper that the user owns.
     * <p/>
     * The view will have:
     * 1. An image or placeholder image representing the pepper
     * 2. The name of the pepper
     * 3. A description of the pepper
     * 4. If the pepper is consumable, it will also have a button the user can press to consume it.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = View.inflate(this.getContext(), R.layout.pepper_list_row_view, null);
        final Product currentProduct = Content.getPurchasedItems().get(position);

        //Set the image
        ImageView image = (ImageView) convertView.findViewById(R.id.list_image);
        image.setImageResource(currentProduct.getFullImage(context));
        image.setVisibility(View.GONE);

        //Set the name
        TextView text = (TextView) convertView.findViewById(R.id.pepperName);
        text.setText(currentProduct.getTitle());

        //Set the description
        TextView description = (TextView) convertView.findViewById(R.id.pepper_description);
        description.setText(currentProduct.getDescription());
        description.setVisibility(View.GONE);

        final Button consumeButton = (Button) convertView.findViewById(R.id.eat_pepper);

        //Listener for the Purchase Requisition button
        consumeButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(final View v) {
                        final String text = consumeButton.getText().toString();
                        consumebuttonLoad(consumeButton);

                        AsyncTask<Void, String, Void> consumeTask = new AsyncTask<Void, String, Void>() {
                            @Override
                            protected void onProgressUpdate(String... values) {
                                if (values[0] != null) {
                                    if (values[0].equals("0")) {
                                        notifyDataSetChanged();
                                        successAlert();
                                    } else {
                                        consumebuttonFail(consumeButton, text);
                                        if (MainScreenPepperListFragment.reference.adapter != null) {
                                            MainScreenPepperListFragment.reference.adapter.notifyDataSetChanged();
                                        }
                                    }
                                }

                            }

                            @Override
                            protected Void doInBackground(Void... params) {
                                boolean consumeOperation = PepperFarmMainScreenActivity.purchaseHandler.consumeProduct(currentProduct.getPurchase().getProductId(), currentProduct.getPurchase().getToken());
                                System.out.println("Consume response: " + consumeOperation);
                                if (consumeOperation) {
                                    Product consumedProduct = Content.ITEM_MAP.get(currentProduct.getPurchase().getProductId());
                                    consumedProduct.consumeItem();

                                    String response[] = new String[1];
                                    response[0] = "0";
                                    this.publishProgress(response);
                                    return null;
                                }

                                String response[] = new String[1];
                                response[0] = "1";
                                this.publishProgress(response);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void result) {
                                super.onPostExecute(result);
                                if (ProductListFragment.purchaseListAdapter != null) {
                                    ProductListFragment.purchaseListAdapter.notifyDataSetChanged();
                                }
                            }

                        };
                        consumeTask.execute();

                    }
                }
        );
        if (currentProduct.getProductType().equals(Product.PRODUCT_TYPE.NON_CONSUMABLE)) {
            //Non-consumable products should not have a consume button
            consumeButton.setVisibility(View.INVISIBLE);
        }


        return convertView;

    }

    public void successAlert() {
        new AlertDialog.Builder(context).setMessage("We hope you enjoyed your peppers.").setNeutralButton("Ok", null).show();
    }

    public void consumebuttonFail(Button button, String text) {
        button.setText(text);
        button.setClickable(true);
        button.setEnabled(true);
        new AlertDialog.Builder(context).setTitle("Error").setMessage("Unable to consume item.").setNeutralButton("Close", null).show();
    }

    public void consumebuttonLoad(Button button) {
        button.setText("Loading...");
        button.setClickable(false);
        button.setEnabled(false);
    }

    /**
     * No filter available for the ContentListAdapter, so this will always return null.
     *
     * @return Always returns null
     */
    public Filter getFilter() {
        return null;
    }

    /**
     * @return The number of items in the list.
     */
    @Override
    public int getCount() {
        return Content.getPurchasedItems().size();
    }
}
