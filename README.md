GFFloatingActionMenu
====================

GFFloatingActionMenu was inspired by https://github.com/futuresimple/android-floating-action-button

A few additions have been made but with the adoption of the support library's FloatingActionButton.

If you would like to contribute or have any issues, please use the issue tracker or email me directly at lgfz71@gmail.com

![alt tag](https://raw.githubusercontent.com/gfranks/GFFloatingActionMenu/master/images/fam.gif)

Usage:
------

```java
<com.github.gfranks.floatingactionmenu.GFFloatingActionMenu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="bottom|end"
    android:layout_margin="@dimen/fab_margin"
    app:fam_rippleColor="@color/fab_color_dark"
    app:fam_backgroundTint="@color/fab_color"
    app:fam_icon="@drawable/ic_plus"
    app:fam_iconTint="@color/white"
    app:fam_expandDirection="arcLeftUp">

    <android.support.design.widget.FloatingActionButton
        android:id="@+id/fab_1"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:tint="@color/white"
        app:srcCompat="@drawable/fab_1_icon"
        app:rippleColor="@color/fab_color_dark"
        app:backgroundTint="@color/fab_color" />

    <android.support.design.widget.FloatingActionButton
        android:id="@+id/fab_2"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:tint="@color/white"
        app:srcCompat="@drawable/fab_2_icon"
        app:rippleColor="@color/fab_color_dark"
        app:backgroundTint="@color/fab_color" />

    <android.support.design.widget.FloatingActionButton
        android:id="@+id/fab_3"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:tint="@color/white"
        app:srcCompat="@drawable/fab_3_icon"
        app:rippleColor="@color/fab_color_dark"
        app:backgroundTint="@color/fab_color" />

</com.github.gfranks.floatingactionmenu.GFFloatingActionMenu>
```

Customization:
----------------

 * `fam_rippleColor` Color for the ripple effect on the menu FloatingActionButton
 * `fam_backgroundTint` Color for the menu FloatingActionButton
 * `fam_iconTint` Color for the default/specified menu icon
 * `fam_icon` Drawable resource for the menu icon, defaults to a plus (vector image supported)
 * `fam_elevation` Elevation for the menu FloatingActionButton
 * `fam_expandIconRotation` Expand rotation of the icon when expanding the menu
 * `fam_collapseIconRotation` Collapsed rotation of the icon when expanding the menu
 * `fam_expandDirection` Direction the menu will open (up, down, left, right, arcLeftUp, arcLeftDown, arcRightUp, arcRightDown)
 
 Note: GFFloatingActionButton comes with a default CoordinatorLayout.Behavior supporting both AppBarLayout anchoring and bottom sheets. You may override this for your own Behaviors.  

Callback Methods:
-----------------

```java
/**
 * OnFloatingActionsMenuUpdateListener
 */
void onMenuExpanded();
void onMenuCollapsed();
```
    
Installation:
------------

### Directly include source into your projects

- Simply copy the source/resource files from the library folder into your project.

### Use binary approach

- Follow these steps to include aar binary in your project:

    1: Copy com.github.gfranks.floatingactionmenu-1.0.aar into your projects libs/ directory.

    2: Include the following either in your top level build.gradle file or your module specific one:
    ```
      repositories {
         flatDir {
             dirs 'libs'
         }
     }
    ```
    3: Under your dependencies for your main module's build.gradle file, you can reference that aar file like so: 
    ```compile 'com.github.gfranks.floatingactionmenu:com.github.gfranks.floatingactionmenu-1.0@aar'```
    
License
-------
Copyright (c) 2015 Garrett Franks. All rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.