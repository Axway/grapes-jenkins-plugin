Extend Grapes Jenkins plugin
=============================

Grapes Jenkins plugin has been designed to send notification to Grapes server. This way, you can plug Grapes directly to your own CI. Getting dependencies information as soon as your project is built by a CI is the best way to keep up-to-date information.
To make you able to send your own information to the server, Grapes Jenkins plugin provides an extension point on notification.

By extended the plugin, you can create your own notifications and this way, fill the Grapes database with your own information.

If you need to better understand what an Jenkins extension point is, go and see [Jenkins documentation](https://wiki.jenkins-ci.org/display/JENKINS/Defining+a+new+extension+point)

How to extend?
--------------
 * extend GrapesNotification and its descriptor ([documentation](https://wiki.jenkins-ci.org/display/JENKINS/Defining+a+new+extension+point))
 * ... and that is it!
 
 The Jenkins plugin will instantiate your implementation of GrapesNotification at the notification time and will send it to the Grapes server using it own configuration.

Currently, one kind of notification is available

 * POST_MODULE notification
 
 
POST_MODULE notification
------------------------

A POST_MODULE notification sends Module object to a Grapes server. This will create or perform an update of the module information into Grapes database.

_**Default implementation:**_ the default implementation of this notification is done to send Grapes Maven plugin reports. You can activate it in the job configuration using "Manage Grapes Maven plugin Notification".
For more information see the [Grapes Jenkins plugin usage](usage.html) page.
