<img src="https://github.com/choiman1559/SyncTAK/blob/master/app/src/main/ic_launcher-playstore.png" width="100" height="100"/>

# SyncTAK

An SyncTAK is a software compilibility layer for the Android Team Awareness Kit (ATAK) that allows multiple ATAK devices to share data using Syncprotocol P2P protocol, which have backend as FCM (And other push messaging technologies) 

## Features

- No Radio Needed: The only thing you need to communicate is just Internet connection! (This app uses ![FCM](https://firebase.google.com/docs/cloud-messaging?hl=ko) to communicate)
- Military-Grade AES-256-CBC (PKCS5Padding) then HMAC-SHA-256 Encryption
- Abbreviate & Non-Abbreviate COT Switch
- Communication Groupping with Muilitple Profile (Communication possible only between groups of the same group: extension of FCM’s Topic function)
- Custom Third-party network provider (To enable CoT communication using a personal XMPP/MQTT server easily without developing complex ATAK plugins)
 
## Screenshots

<img src="https://github.com/choiman1559/SyncTAK/assets/43315227/b3f99da9-a0e1-4bfb-9ae2-5c2dae267728" width="200"/>
<img src="https://github.com/choiman1559/SyncTAK/assets/43315227/ad3882ec-cfea-4807-a8c4-22cd3f270fe7" width="200"/>
<img src="https://github.com/choiman1559/SyncTAK/assets/43315227/c684fa0c-5785-4bf3-97bd-134aae52acef" width="200"/>
</br>
<img src="https://github.com/choiman1559/SyncTAK/assets/43315227/7ca65aca-49e3-45a8-a69f-c8867e3721f4" height="200"/>
<img src="https://github.com/choiman1559/SyncTAK/assets/43315227/2f1d3463-4ec3-4c98-b65d-1f8b54cf0127" height="200"/>

## Third-party Network provider

This app's third-party network provider uses the NetworkProvider API from the NotiSender plugin library.

Check out ![NotiSender Plugin Docs](https://github.com/choiman1559/NotiSender-PluginLibrary) to know how-to-use these APIs.

## Notice

 - **DO NOT USE THIS APPLICATION IN PRATICAL OR EMERGENCY SITUATIONS;** This application is designed for research and training purposes only and therefore cannot be guaranteed to be reliable or safe.
 - This source code was approved for public release on October 7, 2020 via case number 88ABW-2020-3096. A copyright of the public release notification is included in the repository.
 - This Project is Free-software under GNU GPL-3.0 License.
