/**
 * Copyright (c) 2014 Nokia Corporation and/or its subsidiary(-ies).
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.pepperfarm.product.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.nokia.example.pepperfarm.client.ProductListActivity;
import com.nokia.example.pepperfarm.client.R;
import com.nokia.example.pepperfarm.product.Content;
import com.nokia.example.pepperfarm.product.Content.Product;

import java.util.List;

/**
 * The Content Purchase Adapter is used to display a list of Products that can be purchased.
 * If the item is already purchased, it will appear on the list but will be greyed out and unclickable.
 */
public class ContentPurchaseAdapter extends ArrayAdapter<Product> {


    /**
     * Constructor used to set up the ContentPurchaseAdapter
     *
     * @param context  The context
     * @param resource The resource
     * @param objects  The list of Products the ContentPurchaseAdapter will display
     */
    public ContentPurchaseAdapter(Context context,
                                  int resource, List<Product> objects) {
        super(context, resource, objects);
    }

    /**
     * Returns true if the specific content item is enabled. Otherwise returns false.
     *
     * @param position The position in the list of the item to check if enabled
     * @return True if the content item at the supplied position is enabled. False otherwise.
     */
    public boolean isEnabled(int position) {
        if (Content.ITEMS.get(position).isPurchased()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Lets Android know that not all items in the list will always be enabled.
     *
     * @return Always returns false.
     */
    public boolean areAllItemsEnabled() {
        return false;
    }


    /**
     * Does the primary work of setting up the UI for one specific item in the list.
     * An item will have:
     * 1. An image representing the item.
     * 2. A name, as determined by Product.getTitle()
     * 3. A price, which is assumed to have been previously queried through use of a
     * getProductData call to the Nokia In App Payment Library.
     * <p/>
     * If the item has already been purchased, the item will be greyed out and will not be clickable.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = View.inflate(this.getContext(), R.layout.activity_product_list_row_view, null);


        final Product currentProduct = Content.ITEMS.get(position);

        //Set the image
        ImageView image = (ImageView) convertView.findViewById(R.id.list_image);
        image.setVisibility(View.GONE);
        //image.setImageResource(currentProduct.getFullImage());

        //Set the name
        TextView text = (TextView) convertView.findViewById(R.id.iapName);
        text.setText(currentProduct.getTitle());

        //Set the price
        TextView price = (TextView) convertView.findViewById(R.id.price);
        price.setText(currentProduct.getPrice());

        //Set the description
        TextView description = (TextView) convertView.findViewById(R.id.product_description);
        description.setText(currentProduct.getDescription());

        Button buyPepperButton = (Button) convertView.findViewById(R.id.start_purchase);
        buyPepperButton.setOnClickListener
                (new Button.OnClickListener() {
                    public void onClick(View v) {
                        System.out.println("CLICK " + currentProduct.getProductId());
                        try {
                            ProductListActivity.purchaseHandler.startPayment(ProductListActivity.reference, currentProduct.getProductId());
                        } catch (Exception e) {
                            System.out.println("Exception while starting payment: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
                );

        if (currentProduct.isPurchased()) {
            buyPepperButton.setVisibility(View.INVISIBLE);
            price.setVisibility(View.INVISIBLE);
            /*
             * A purchased item:
             * 1. Will not have the arrow
             * 2. Will have a slightly different background color
             * 3. Will have greyed out text
             */
            //	text.setTextColor(Color.GRAY);
            //	price.setTextColor(Color.GRAY);
            //	convertView.setBackgroundColor(Color.parseColor("#EEEEEE"));
        }

        return convertView;

    }


}
