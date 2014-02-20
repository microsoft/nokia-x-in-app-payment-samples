Nokia In-App Payment API sample: Pepper Farm Simulator
======================================================

This Nokia sample application demonstrates how to implement the support for
Nokia In-App Payment enabler. This application contains sample code to:

* Connect to Nokia In-App Payment enabler service
* Check if your application supports In-App billing
* Return product information
* Purchase content
* Consume content
* Restore content


Instructions
--------------------------------------------------------------------------------

Import the project to your IDE. The recommended build target for the project is
Android 4.1.2, API level 16.


Implementation
--------------------------------------------------------------------------------

Description of the classes:

* Package `com.nokia.example.pepperfarm.client`:
    * `MainScreenPepperListFragment`: Fragment for the main screen of the
      Pepper Farm Simulator. Contains all purchased peppers in a list.
    * `PepperFarmMainScreenActivity`: This is the first screen that loads in the
      Pepper Farm application. It shows you a list of peppers you have already
      purchased and includes buttons allowing you to purchase, restore and
      consume peppers.
    * `PepperFarmSplashScreen`: Splash screen that is shown when application is
      launched for the first time.
    * `ProductListActivity`: Activity representing a list of purchasable products.
    * `ProductListFragment`: Fragment containing a list of all purchasable peppers.

* Package `com.nokia.example.pepperfarm.client.util`:
    * `ProductDetails`: Holds product details response data.
    * `Purchase`: Holds purchase and restoration response data. 

* Package `com.nokia.example.pepperfarm.iap`:
    * `Payment`: This class creates a `ServiceConnection` with Nokia In-App
      Payment enabler. Used to do all Nokia In-App Payment enabler specific
      actions.

* Package `com.nokia.example.pepperfarm.product`:
    * `Config`: Product ID mapping for all purchasable content.
    * `Content`: Used to represent a collection of Nokia Store IAP items.

* Package `com.nokia.example.pepperfarm.product.adapters`:
    * `ContentListAdapter`: An adapter used to show a list of Peppers you
      currently own.
    * `ContentPurchaseAdapter`: This adapter is used to display a list of
      products that can be purchased.


Known issues
--------------------------------------------------------------------------------

None.


License
--------------------------------------------------------------------------------

See the separate license file provided with this project.
