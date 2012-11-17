WLANAudit 3
===========
An application to scan for WLAN Access points and to audit their security and capabilities
The application shows relevant diagnostic information about an access point such as MAC Address, encryption, signal strength, etc.

On some access points the application tries to guess the access point default password via if a public known algorithm exists.

This app is **NOT MEANT** to be used as a hacking utility, so I'm not responsible of the usage you make of it.

Getting the sourcecode
======================
To get the source code clone the repository with the following commands
    
    $ git clone git://github.com/RobertoEstrada/WLANAudit-Android.git

Once clone has finished
    
    $ git submodule update --init

Building the sourcecode
=======================
In order to build the sourcecode you need to have installed and configured Java, the Android SDK and Apache Ant. You need to set the environment variable ANDROID_HOME and have the Android tools and Ant in your path.

Then in a shell at the project root, use the following command

    $ ant build-debug

Working with Eclipse
====================
In a shell at the project root, use the following command to generate eclipse project files for all the dependencies.

    $ ant eclipse 

When done, open eclipse and select *File / Import / Existing projects into workspace* and then select the project root as the base directory. Select all the projects except those that have *'Sample'* on their names.

Work as usual.

Contributing to the project
=============================
Fork the project, make your changes and send a pull request for code review. Your pull requests must be self contained, otherwise they will be rejected until they meet that criteria.

More info on how to work with pull requests can be found in the article ['Using Pull Requests'] (https://help.github.com/articles/using-pull-requests)

Developed by
============
* Roberto Estrada

Contributors
============
* Jesús Manzano Camino

License
=======
>Copyright 2012 Roberto Estrada
>
>Licensed under the Apache License, Version 2.0 (the "License");
>you may not use this file except in compliance with the License.
>You may obtain a copy of the License at
>
>   http://www.apache.org/licenses/LICENSE-2.0
>
>Unless required by applicable law or agreed to in writing, software
>distributed under the License is distributed on an "AS IS" BASIS,
>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
>See the License for the specific language governing permissions and
>limitations under the License.



