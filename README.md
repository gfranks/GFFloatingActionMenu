GFFloatingActionMenu
====================

GFFloatingActionMenu was inspired by https://github.com/futuresimple/android-floating-action-button

A few additions have been made but with the adoption of the support library's FloatingActionButton

If you would like to contribute or have any issues, please use the issue tracker or email me directly at lgfz71@gmail.com


Installation:
------------

### Directly include source into your projects

- Simply copy the source/resource files from the library folder into your project.

### Use binary approach

- Follow these steps to include aar binary in your project:

    1: Copy com.github.gfranks.minimal.notification-2.1.aar into your projects libs/ directory.

    2: Include the following either in your top level build.gradle file or your module specific one:
    ```
      repositories {
         flatDir {
             dirs 'libs'
         }
     }
    ```
    3: Under your dependencies for your main module's build.gradle file, you can reference that aar file like so: 
    ```compile 'com.github.gfranks.minimal.notification:com.github.gfranks.minimal.notification-2.1@aar'```
    
    (NOTE: v1.0 and v2.0 are still available, if you wish to continue using it. Follow the same binary approach but reference 1.0@aar or 2.0@aar)

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