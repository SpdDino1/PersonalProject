# PersonalProject

General Description

The 'BuyCycle' app is a 'cycle-sharing' app that enables students with cycles to give their cycles when they are free, to those who need
it at that point of time. The cycles may be shared for free or by fee.
Hence it is a platform that connects 'live' cycle needers to 'live' cycle givers.

Usage

The Main screen has 2 buttons, one for broadcastin a free cycle and the second for viewing live cycles.

Upon clicking the broadcast button, you will be asked to enter certain details after which you will be broadcaster. You may leave the
app in running in the background, and it will notify you in case anyone is tracking your cycle. BUT DO NOT CLOSE THE APP WHILE BROADCASTING.
Doing so will not trigger a notification.
After clicking the notification (Or any of the 2 buttons (If your app is still on the foreground)) you will be directed to the Tracker
activity. After calibrating with gps, your tracker will show you the live distance and direction of your approaching taker.
If you choose to, you may click the 'block taker' button. Doing so will deny the current taker to track your current cycle, and you will
be rebroadcasted.
After the taker is less than 30m away from you, your tracker will notify you. After handing over your cycle to the taker and clicking the 
share complete button, the transaction is completed.

Upon clicking the View live cycles button(If the internet connection is slow, or the server is slow, a black screen may appear. Pls wait
for a while and the screen will load), you will be redirected to a list of available cycles ('Vikram' is added by default for your 
testing purposes). After clicking a broadcasted cycle, you will eb redirected to a tracker activity where you can follow an arrow pointer 
to your giver. A live distance measurment will also be shown.
However should your giver choose to deny you his cycle, he may block you. If blocked a Toast message will appear stating that 'Your Time
has Expired' and will be redirected to teh list of live cycles. You will still be able to see your previous giver's cycle, but will not be
directed to a tracker page upon clicking it.

Ideal Testing Procedure

>>>The app uses the JobScheduler API and requires Lollipop minimum<<<

Install the app on 2 devices. Ask one of the device holders to go to a far distance and broadcast his/her cycle. Then view his/her cycle
and try tracking/denying etc:

Constraints

>>For most accurate results use the app in open air conditions. This helps get a better gps signal
>>Please use a fast internet connection.

BUGS
**FOR CLOSING THE APP FROM THE GIVER'S PERSPECTIVE, MAKE SURE YOU BACK PRESS TILL YOU REACH THE MAIN ACTIVITY. Closing from any other
activity will not clean the database. I still have to find a fix to this bug
