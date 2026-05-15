# Asasfans

Asasfans is an Android client for A-SOUL fans. It brings together video browsing, Bilibili playback, account login, read-only comments, subscription management, blacklist filtering, music, tools, calendar, and app settings in one Material 3 based interface.

This project keeps the history of the earlier open source project [A-SoulFan/as-as-fans](https://github.com/A-SoulFan/as-as-fans). Later maintenance and feature work are continued in this repository under the same GPL-2.0 license.

Asasfans is an unofficial fan project. It is not affiliated with Bilibili, A-SOUL, or their related companies.

## Features

- Browse A-SOUL related Bilibili video feeds.
- Open videos either inside the app or by jumping to Bilibili.
- Log in to Bilibili with QR code or official WebView login.
- Play videos in the app with selectable quality where available.
- View read-only comment lists.
- Manage subscribed UP accounts and show their video feed.
- Manage blacklist words, blocked UP accounts, and blocked videos.
- Use built-in music, tools, calendar, and settings pages from the side drawer.

## Build

Use the Gradle wrapper from the repository root:

```sh
./gradlew assembleDebug
```

Common checks:

```sh
./gradlew testDebugUnitTest
./gradlew lintDebug
```

The project uses one Android application module, `app`, with package name `com.example.asasfans`.

## Feedback

Please use [GitHub Issues](https://github.com/LEN5010/Asasfans/issues) for bug reports and feature requests.

## License

This project is released under the GNU General Public License v2.0. See [LICENSE](./LICENSE).

If you distribute modified APKs or other binary builds, provide the corresponding source code and keep the original copyright and license notices.
