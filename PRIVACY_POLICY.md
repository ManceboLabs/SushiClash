# Privacy Policy — Sushi Clash

**Effective date:** August 29, 2026

**Developer:** Mancebo Labs  
**App:** Sushi Clash (`com.mancebolabs.sushiclash`)  
**Contact:** jmanceboapps@gmail.com


This Privacy Policy describes how Sushi Clash handles information when you use the Android app. It is written to reflect what the app actually does today.

---

## Summary

Sushi Clash is a local sushi counter and party game. **Mancebo Labs does not operate servers for this app, does not create user accounts, and does not receive, collect, or transmit your game data to Mancebo Labs.**

Information you enter or generate while playing is stored **on your device**, except where Android’s system backup or device-transfer features copy allowed app data to services managed by Google or your device manufacturer, as described below.

---

## Information stored on your device

Sushi Clash stores the following data locally to provide its features:

### Game and session data

- **Active game state** — whether a game is in progress, session identifier, game mode (Solo or Group), player counters, roulette settings, and roulette participant names during group games.
- **Finished game history** — for Solo games: date, sushi count, and roulette settings; for Group games: date, player names you entered, each player’s sushi count, and roulette settings.
- **Frequent players** — names you used in saved Group games, kept to speed up starting new games.

### Achievements and progress

- Achievement unlock status, unlock timestamps, and aggregated counters (for example total games completed, total roulette spins, lifetime sushi totals, and peak sushi in a single game).
- Achievement data does **not** include player names.

### Settings and preferences

- Light or dark theme.
- App language override (or follow system language).
- Onboarding/tutorial completion status.
- Sound and vibration feedback preferences.

All of the above is stored in a local preferences file on the device (`DataStore`). Your language choice is also stored in a standard Android AppCompat locale preference.

---

## Information we do not collect

Mancebo Labs **does not**:

- Require an account or login.
- Collect your name, email address, phone number, or contact list.
- Collect precise location, photos, microphone audio, or device contacts.
- Use analytics, advertising, or crash-reporting SDKs in the app.
- Send your game data, player names, history, or achievements to Mancebo Labs or to any server operated by Mancebo Labs.

The app declares **no Android permissions** in its manifest (for example, it does not request Internet, location, or contacts access directly).

---

## Android backup and device transfer

Sushi Clash allows Android backup (`allowBackup` is enabled). Only the following app data is included in backup and device-to-device transfer rules:

- The local preferences file that contains game history, achievements, settings, frequent players, and related app data.
- The app language preference.

Other app files — including a small on-device install marker used for safe restore handling — are **not** included in backup.

If you use **Google backup**, **manufacturer backup**, or **device-to-device transfer**, copies of the included data may be stored and restored through services operated by **Google or your device manufacturer**, not by Mancebo Labs. Mancebo Labs does not access those backups.

**After a backup restore on a new install**, Sushi Clash automatically clears any restored *in-progress* game on first launch so you do not resume a stale session from another device. Saved history, achievements, theme, language, and other preserved settings remain.

Whether backup is enabled, and where backup copies are stored, is controlled by your **Android system and Google account settings**, not by Sushi Clash.

---

## Third-party services

### Google Play services — downloadable fonts

Sushi Clash uses AndroidX **Google Fonts** support to load typography (Plus Jakarta Sans, Inter, Noto Sans JP, and Noto Sans SC) through **Google Play services** when needed. This may cause your device to download font files from Google infrastructure. **Your game data, player names, and history are not sent as part of font requests.**

Mancebo Labs does not control Google Play services or Google’s handling of technical requests made by your device. Please refer to [Google’s Privacy Policy](https://policies.google.com/privacy) for information about Google services on your device.

### Google Play Store

If you install or update the app from Google Play, your interaction with the store is governed by Google’s policies and your Google account settings. That is separate from data stored inside Sushi Clash.

---

## How we use information

Because data stays on your device (except for OS-managed backup as described above), Mancebo Labs uses it only **inside the app on your device** to:

- Run Solo and Group counting games and the roulette.
- Show history and rankings.
- Track and display achievements.
- Remember your theme, language, and feedback preferences.
- Suggest frequent player names when starting a new Group game.

We do not use your information for advertising, profiling, or sale to third parties.

---

## Data retention

Data remains on your device until you delete it using the options below or remove the app. Mancebo Labs does not retain copies on its own systems.

If Android backup is enabled, retained copies in your backup are managed by your platform provider under their retention rules.

---

## Your choices and data deletion

### In the app (Settings)

- **Clear history** — deletes all saved Solo and Group game history. Does not delete frequent players, achievements, or other settings.
- **Clear achievements** — resets all achievement progress and unlocks. Does not delete game history or frequent players.

Other actions affect active gameplay but are not full data deletion:

- **Finish game → Don’t save** — ends the current game without adding it to history.
- **Reset counter** (long press on a counter) — resets that player’s count in the current game.
- **Remove participant** (roulette screen) — removes a name from the current roulette list.

There is **no separate in-app control** to delete only frequent players, only theme/language settings, or all app data at once.

### On your device

To remove **all** Sushi Clash data, including history, achievements, frequent players, settings, and active games:

- Uninstall the app, or
- Use **Android Settings → Apps → Sushi Clash → Storage → Clear storage / Clear data**.

Removing backup copies, if any, may additionally require managing your **Google or device backup settings**.

---

## Children’s privacy

Sushi Clash is a general-purpose game utility and is not directed specifically at children. It does not knowingly collect personal information from anyone, because it does not collect personal information on its servers. Player names are optional labels you type for local gameplay.

If you are a parent or guardian and believe a child has entered personal information into the app on a shared device, you can delete app data using the steps above.

---

## Security

Sushi Clash stores data in the app’s private on-device storage area provided by Android. The app does not implement its own cloud sync or account system. Protection of data at rest and in backup depends on your device security (screen lock, encryption, and backup account security).

---

## International users

Sushi Clash is available in multiple languages. Regardless of where you use the app, the data practices described here are the same: local storage on your device, with optional OS-managed backup, and no collection by Mancebo Labs.

---

## Changes to this policy

We may update this Privacy Policy if the app’s data practices change. When we do, we will revise the effective date at the top and publish the updated policy where the app is distributed (for example, the Google Play listing or a linked web page).

Continued use of the app after an update means you accept the revised policy.

---

## Contact

If you have questions about this Privacy Policy or Sushi Clash data practices, contact:

**Mancebo Labs**  
Email: jmanceboapps@gmail.com

---

*This document describes Sushi Clash version 1.0 as implemented in the open-source project maintained by Mancebo Labs. It is intended for publication alongside the Google Play release.*
