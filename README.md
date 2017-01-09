# Twas
ADI Final Project

[Google Play Store Link](https://play.google.com/apps/testing/shuvalov.nikita.twas)

What it is:

The World's A Stage(TWAS) is a proximity based social app. The main inspiration behind the app is Nintendo 3DS's streetpass.
What the app does is it emits a token containing the user's id and a string message. Once another phone with the app finds that token, it retrieves
that user's id and downloads the emitting user's profile, if that user set-up their profile, from firebasedatabase as well as storing 
a copy on a local sqldatabase.The string message that was emitted is also used to populate what I call the soapbox feed. 
The soapbox feed contains all retrieved soapbox messages (The string message that was sent in the emission).
That soapbox message is a sort of "status message". It's a message that is sent and retrieved without the need for an internet
connection and can only be heard by nearby users. The idea for the soapbox feed is to alert other users around you if you have anything
interesting going on, like if you're looking for individuals to get a pick-up game of soccer going or sell a ticket 
to a show you can't attend, etc. After the selfuser gets access to a stranger-user's profile, from receiving their id,
that self-user can look at the stranger-user's profile and even start a chat with them in their own chatroom. 
Both users should be emitting and receiving tokens so they can interact with each other. You can always update your profile information
if you want to. The applications for this app can be if you're going to a meet-up and everyone is using it, it can act as a nametag,
or if you have to do some role-playing in a group, you can change your profile to be whatever character your role-playing and
other users wouldn't have to access if your Gorgoth the Unrelenting, they'll just know cause that's what your profile says.
It can be used to help identify people wherever there's a gathering of strangers.

One of the major hurdles I had to overcome, was trying to have the tokens emit/receive in the background within a service, which
I failed because Google Nearby Messages Api by definition doesn't allow that to occur in a service. So my original idea was 
severely altered, but now I think of this more as a feature. Since the emitting/receiving can only occur when the app is open,
users can control when they want to send their profiles to another user. It actually adds a bit of security, albeit it makes it 
less fun of an app. Another major issue, after testing with different phones I found that some phones don't transmit or listen
to the tokens as regularly as others. This can lead to one user getting another user's profile before the other user can reciprocate.
If a chat is started between those two users, there will be a crash if both users don't have a reference to one another.

FirebaseDatabase/Storage and Google Nearby Messages API does all of the heavy lifting for my app. The chatrooms and profiles
are both set up in firebasedatabase, though there's a backup profile list stored local on an SQLliteDatabase in case user doesn't
have an internet connection. Unfortunately, images are not stored locally. They are all stored in firebasestorage, so though it
will load the user's profile it won't load their profile image. 

Home Activity:
![Image1](https://github.com/NikShuvalov/Twas/blob/master/alpha_3_home.png)

Self-Profile Activity:
![Image2](https://github.com/NikShuvalov/Twas/blob/master/alpha_3_self_profile.png
