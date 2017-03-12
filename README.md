# Still App

Still app is an Android and iOS app build in Clojure for React Native using re-natal.

It was commissioned by Brighton based theatre company The Future Is Unwritten as part of their live theatre production "Still" and developed by Henry Garner and Russell Bender.

The Still App was offered to audiences to download before the performance and used during the performance. Audiences were asked to join a local WiFi network running in the venue (and the app has a portcullis in place to prevent further execution if it is not connected to a WiFi network with a "safe" SSID). It then establishes a websocket connection to a server running on that network. (See our Still Server repo for this).

Once connected, it allows audience members to see information about the photographer Vivian Maier and to receive messages and pictures sent by the Still Server during the performance.

It also attempts to collect and send the following information from the user's device to the Still Server (subject to the user agreeing to a Privacy Policy).
 - The user's device name (iPhone) or associated account email addresses (Android)
 - The last 5 pictures saved to the device's gallery/camera roll
 - Any pictures the user takes of the performance using the camera integrated into the app
 - Any pictures the app has taken out of the front facing camera of the user while  they are browsing the app.
 
Still ran at Ovalhouse in London and toured to the Mercury Theatre, Colchester and The Old Market Theatre, Brighton between November 2016 and March 2017. We don't envisage further contributing to this repository unless further development is required for future performances. If you would like to make pull requests, we'll do our best to review them. If you have any questions about the app, frameworks etc feel free to contact us: hello@potentialdifference.org.uk

## Usage

This app will need to be used in conjunction with the Still Server. See our other repositories.

Before making a build, you will need to adjust the settings in config.cljs. In particular:
 - dev-ip or prod-ip will need to point to the ip address of the still server
 - valid-ssids will need to include the SSID of the WiFi network the still server is running on
 - the auth token will need to match the auth token you hvae configured in the still server

## License

Copyright © 2016 Potential Difference

Distributed under the GPL Public License 2.0 
