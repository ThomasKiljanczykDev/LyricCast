![LyricCast](docs/images/LyricCast-splash.png "LyricCast")

[<img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="70" title="Coming soon!" alt="Get it on Google Play. Coming soon!">](https://play.google.com/store/apps/details?id=dev.thomas_kiljanczyk.lyriccast)

# LyricCast

*LyricCast* is an Android application that allows you to cast lyrics to your TV screen using Google
Cast.
The main audience for the app is churches, where the lyrics are projected on the screen during the
service.
Current solutions require proprietary hardware and software which are expensive and often slow
responding.
*LyricCast* aims to provide a simple and affordable solution for churches and other organizations
that need to cast
lyrics
to a TV screen using off the shelf devices.
*LyricCast* is localized in English and Polish.

# Overview

*LyricCast* application consists of the following elements:

* [Android client app](android)
* [Google Cast custom receiver app and privacy policy](js)

> [!NOTE]
> Each element is described in a separate ReadMe document.

# Architecture

*LyricCast* consists of a client app and a Google Cast custom receiver app.
This section outlines the architecture of the *LyricCast* ecosystem.

## Repository structure

The app project is modularized to separate concerns and make the codebase more maintainable.

This project consists of modules:

* android - contains the Android client app
* js/iac - contains the AWS CDK infrastructure as code
* js/google-cast-custom-receiver-app - contains the Google Cast custom receiver app
* js/privacy-policy - contains the privacy policy page

## Architecture components

This section presents simplified architecture components of the *LyricCast* ecosystem.

```mermaid
%%{init: {"flowchart": {"defaultRenderer": "elk"}} }%%
flowchart LR
    subgraph androidDevice1[Android Device]
        application1[LyricCast Application]
    end

    subgraph androidDevice2[Android Device]
        application2[LyricCast Application]
    end

    subgraph googleCastDevice[Google Cast Device]
        receiverApplication[LyricCast Custom Receiver]
    end

    application1 -->|Cast| application2
    application1 -->|Cast| receiverApplication
```
