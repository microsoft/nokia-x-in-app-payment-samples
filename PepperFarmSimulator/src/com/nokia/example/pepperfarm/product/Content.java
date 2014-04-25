/**
 * Copyright (c) 2014 Microsoft Mobile and/or its subsidiary(-ies).
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.pepperfarm.product;

import android.content.Context;
import android.content.res.Resources;
import com.nokia.example.pepperfarm.client.R;
import com.nokia.example.pepperfarm.client.util.Purchase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used to represent a collection of Nokia Store IAP items.
 * If you wanted to further improve upon the idea of having an object representing in app purchasable content, you could consider
 * extending Inventory.java in order to not have to manually merge information between your own content objects and the ones
 * provided by Nokia IAP.
 */
public class Content {

    public static boolean products_initialized = false;

    /**
     * An array of items.
     */
    public static List<Product> ITEMS = new ArrayList<Product>();

    /**
     * A map of items, by ID.
     */
    public static Map<String, Product> ITEM_MAP = new HashMap<String, Product>();


    public static void initializeProducts(Resources res) {
        //The first item is already paid for and non-consumable for the time being.
        Product pepper_farm = new Product(Config.PEPPER_FARM_PRODUCTID, res.getString(R.string.pepper_farm), res.getString(R.string.pepper_farm_description));
        pepper_farm.setProductType(Product.PRODUCT_TYPE.CONSUMABLE);

        Product pepper_seeds = new Product(Config.PEPPER_SEEDS_PRODUCTID, res.getString(R.string.pepper_seeds), res.getString(R.string.pepper_seeds_description));
        Product jalapeno = new Product(Config.JALAPENO_PRODUCTID, res.getString(R.string.jalapeno), res.getString(R.string.jalapeno_description));
        Product sweet_bell = new Product(Config.SWEET_BELL_PRODUCTID, res.getString(R.string.sweet_bell), res.getString(R.string.sweet_bell_description));
        Product habenero = new Product(Config.HABENERO_PRODUCTID, res.getString(R.string.habenero), res.getString(R.string.habenero_description));
        Product pepper_spray = new Product(Config.PEPPER_SPRAY_PRODUCTID, res.getString(R.string.pepper_spray), res.getString(R.string.pepper_spray_description));

        //Content.addItem(pepper_farm);
        Content.addItem(pepper_seeds);
        Content.addItem(jalapeno);
        Content.addItem(sweet_bell);
        Content.addItem(habenero);
        Content.addItem(pepper_spray);

        products_initialized = true;
    }

    /**
     * Adds a product to both the item map and item list.
     * We also check for duplicates.
     *
     * @param item
     */
    public static void addItem(Product item) {
        if (ITEM_MAP.get(item.productId) == null) {
            ITEMS.add(item);
            ITEM_MAP.put(item.productId, item);
        }
    }


    /**
     * Returns a list containing all of the products where product.isPurchased() is true.
     *
     * @return A list of Products containing all purchased content.
     */
    public static List<Product> getPurchasedItems() {

        List<Product> purchasedItems = new ArrayList<Product>();
        for (Product product : ITEMS) {
            if (product.isPurchased()) {
                purchasedItems.add(product);
            }
        }

        return purchasedItems;

    }

    /**
     * An item representing a piece of content.
     */
    public static class Product {

        /**
         * The product type represents whether a Product is consumable or non-consumable.
         * A non-consumable product is bought once and kept permanently.
         * A consumable product can be consumed and then bought again. (Think coins or power ups)
         * The default is consumable.
         */
        public static enum PRODUCT_TYPE {
            /**
             * A durable product is bought once and kept permanently.
             */
            NON_CONSUMABLE,
            /**
             * A consumable product can be consumed and then re-purchased.
             */
            CONSUMABLE
        }

        /**
         * The product type. Either Consumable or Durable.
         */
        public PRODUCT_TYPE productType;

        /**
         * True if the item has already been purchased, false otherwise.
         */
        public boolean purchased;

        /**
         * @return The Product Type (DURABLE or CONSUMABLE)
         */
        public PRODUCT_TYPE getProductType() {
            return productType;
        }

        /**
         * @param productType The Product Type (DURABLE or CONSUMABLE)
         */
        public void setProductType(PRODUCT_TYPE productType) {
            this.productType = productType;
        }

        /**
         * The ID of the IAP product. Sometimes referred to as the resource ID.
         */
        public String productId;

        /**
         * The name of the product
         */
        public String title;

        public int count;

        /**
         * @param id          The product's ID.
         * @param title       The name of the product
         * @param description The description of the product
         */
        public Product(String id, String title, String description) {
            productId = id;
            this.title = title;
            purchased = false;
            productType = PRODUCT_TYPE.CONSUMABLE;
            this.description = description;
            price = "";
        }

        /**
         * @param id          The product's ID.
         * @param title       The name of the product
         * @param description The description of the product
         * @param productType Enumeration whose value is either DURABLE or CONSUMANBLE.
         * @param parent      The ID of the parent associated with this product.
         */
        public Product(String id, String title, String description, PRODUCT_TYPE productType) {
            this(id, title, description);
            this.productType = productType;
        }


        /**
         * @return The product id
         */
        public String getProductId() {
            return productId;
        }

        /**
         * @param productId
         */
        public void setProductId(String productId) {
            this.productId = productId;
        }

        /**
         * @return The product's title
         */
        public String getTitle() {
            return title;
        }

        /**
         * @param title
         */
        public void setTitle(String title) {
            this.title = title;
        }

        /**
         * @return The description
         */
        public String getDescription() {
            return description;
        }

        /**
         * @param description
         */
        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * @return The price of the product.
         */
        public String getPrice() {
            return price;
        }

        /**
         * @param price The price of the product, without units. (No $)
         */
        public void setPrice(String price) {
            this.price = price;
        }

        /**
         * The description of the content.
         */
        public String description;

        /**
         * A string representing the price of the content.
         * Set by the Enabler after the first getProductData() request.
         */
        public String price;

        /**
         * A record of the item's purchase, assuming it has been purchased.
         * If the item has not been purchased, then this value will be null.
         */
        private Purchase purchase = null;

        /**
         * @return An object representing the items purchase, or null if the item was never purchased.
         */
        public Purchase getPurchase() {
            return purchase;
        }


        /**
         * Returns the title of the Content
         */
        @Override
        public String toString() {
            return title;
        }

        /**
         * @return True if the item has been purchased, false otherwise.
         */
        public boolean isPurchased() {
            return purchased;
        }

        /**
         * Tells the content that the item has been purchased.
         *
         * @param thePurchase An object representing all the purchase details.
         */
        public void setPurchased(Purchase thePurchase) {
            this.purchased = true;
            this.purchase = thePurchase;
            //count++;
        }


        /**
         * Consumes the purchase by setting purchased = false and nulling out the Purchase records.
         * If you call this on an item that was not purchased or was already consumed, the item will just be
         * marked as consumed without any error checking performed. (There is no real harm in doing this though.)
         */
        public void consumeItem() {
            purchased = false;
            purchase = null;
        }

        /**
         * @return Returns the id of an image resource representing the full resolution available.
         */
        public int getFullImage(Context ctx) {
            int imageResource = R.drawable.farm;

            String title = getTitle();
            if (ctx.getString(R.string.sweet_bell).equals(title)) {
                imageResource = R.drawable.bellpepper;
            } else if (ctx.getString(R.string.pepper_spray).equals(title)) {
                imageResource = R.drawable.pepperspray;
            } else if (ctx.getString(R.string.pepper_farm).equals(title)) {
                imageResource = R.drawable.farm;
            } else if (ctx.getString(R.string.jalapeno).equals(title)) {
                imageResource = R.drawable.jalapeno;
            } else if (ctx.getString(R.string.habenero).equals(title)) {
                imageResource = R.drawable.habenero;
            } else if (ctx.getString(R.string.pepper_seeds).equals(title)) {
                imageResource = R.drawable.pepperseed;
            }
            return imageResource;

        }

    }
}
