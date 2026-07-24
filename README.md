Vé
==

[![Get it on Google Play](https://developer.android.com/images/brand/en_generic_rgb_wo_45.png)](http://play.google.com/store/apps/details?id=eu.pryds.ve)

Vé is an editor for translating gettext PO files on Android. It is still in an early development phase, so you might still experience bugs and missing features. If you do, you are very welcome to help out by reporting the bug or missing feature through Vé's issue tracker.

PO files are the de-facto standard for localization (translation) of especially open source programs that run on several platforms, including Linux, Windows and MacOS, but also Android apps may be localized by means of PO files (including this very app).

Vé enables you to load such a PO file into a simple interface, fully (or partially) translate apps or programs into your own language, and save an updated PO file for implementation by the app/program developer. While providing the essential features for translation and localization of apps and programs, Vé is simple in design to compensate for the limitations of a mobile platform like Android (small screen, no physical keyboard, etc.)

Vé is named after one of [Odin's two brothers](http://en.wikipedia.org/wiki/Vili_and_V%C3%A9) in Norse mythology. The three brothers, Odin, Vili and Vé were thought to be the Gods who gave life to the first humans, Ask and Embla. Vé was the one who gave them speech, among other capabilities.

Vé is open source software, released under the GPL licence, which means you can use it for free, for personal or other purposes. You can even redistribute it, modified or not, provided you do so under the same licence, which means you have to provide your source files for any changes. You can read the [full licence text here](https://github.com/pryds/ve/blob/master/COPYING).

Project structure
-----------------

At a high level, the app is made of:

* `MainActivity`: the main translation editor screen.
* `TranslatableString` and `TranslatableStringCollection`: PO parsing/writing model layer.
* `SettingsActivity`/`SettingsFragment`: translator metadata preferences.
* `res/`: layouts, strings, menus and drawables.

Manual smoke test (baseline)
----------------------------

Run this checklist after each small modernization change:

1. **Open PO file**
   * Use **Open file** and load a valid `.po` file.
   * Confirm original/translated text areas are populated.
2. **Navigate strings**
   * Tap **Previous** and **Next**.
   * Confirm string number and content update correctly.
3. **Edit translation**
   * Change text in translated field.
   * Confirm metadata counters update.
4. **Save file**
   * Tap **Save file** and verify success toast.
   * Reopen the file and verify edited text persists.
5. **Settings read/write**
   * Open **Settings**, edit translator fields, save/back out.
   * Return and confirm values persist and are used on save.

Current architecture map
------------------------

* **UI layer (Java + framework widgets)**:
  * `MainActivity` drives the translation editor.
  * `SettingsActivity` + `SettingsFragment` handle translator identity/preferences.
* **Model/parsing layer**:
  * `TranslatableString` stores PO entry/header data.
  * `TranslatableStringCollection` parses PO files and serializes back to PO output.
* **Resources/config**:
  * `res/layout/activity_main.xml` is the primary editor screen.
  * `res/menu/main_activity_actions.xml` defines top app actions.
* **Build system**:
  * Gradle-based project (`settings.gradle`, root `build.gradle`, `app/build.gradle`).

Current limitations
-------------------

* File parsing/saving is still done on the main thread (can cause UI jank on large files).
* Storage now uses SAF document URIs for open/save flows.
* `MainActivity` contains multiple responsibilities (navigation, editing, persistence trigger, UI state).
* Automated tests are still limited; manual smoke testing remains important.

Modernization roadmap (incremental)
-----------------------------------

1. **UI polish without behavior changes**
   * Improve spacing/typography/section labeling on main editor.
2. **MainActivity maintainability refactor**
   * Cache views, split screen-update helpers, preserve current behavior.
3. **Editor UX improvements**
   * Avoid unnecessary text resets, preserve cursor position, clarify plural-form state.
4. **Architecture and async improvements**
   * Move parse/save work off main thread and continue decoupling UI/model responsibilities.

How to modernize Vé for newer Android versions
----------------------------------------------

This codebase now builds with Gradle, targets Android API level 34 (`targetSdk 34`), and uses a modern minimum API level 24 (`minSdk 24`).

1. **Maintain modern Android baseline**
   * Keep dependencies and build tooling current.
2. **Continue modern platform cleanup**
   * Replace remaining legacy utilities and disabled legacy features.
3. **Run compatibility validation**
    * Test on recent Android versions/emulators (Android 12, 13, 14+) and verify open/edit/save PO flows.

How to improve the codebase
---------------------------

For the highest long-term quality and maintainability:

* Add automated testing (unit tests for PO parser and serialization, plus instrumentation tests for file open/save flows).
* Move parsing/saving work off the main thread to avoid UI jank on large files.
* Introduce architecture boundaries (for example ViewModel + repository-style separation) to reduce Activity complexity.
* Continue reducing synchronous file/parse work on UI thread and validate UX on large files.
* Add static quality gates (Android Lint, CI workflow, formatting/lint checks).
* Audit and remove dead/disabled features or fully modernize them (for example legacy in-app billing/backups).

Build/testing quick start (Gradle)
----------------------------------

This repository now uses Gradle (`gradlew`, root `build.gradle`, `app/build.gradle`) as the build system.

1. Configure an Android SDK path using one of:
   * `local.properties` with `sdk.dir=/absolute/path/to/android-sdk`
   * `ANDROID_HOME`
   * `ANDROID_SDK_ROOT`
2. Ensure your SDK contains:
   * `platforms;android-34`
   * `build-tools;34.0.0`
   * App currently uses `targetSdkVersion` 34 and `minSdk` 24.
   * File open/save flows use SAF-based document URIs.
3. Run:
   * `./gradlew :app:assembleDebug` to build a debug APK
   * Output APK path: `app/build/outputs/apk/debug/app-debug.apk`
   * Debug package name is `eu.pryds.ve.debug` (via `applicationIdSuffix ".debug"`), so it can install alongside release builds and avoid update/signature conflicts.

APK exposure for testing (artifact/release)
-------------------------------------------

This repository now includes GitHub Actions workflow:

* Workflow file: `.github/workflows/build-apk.yml`
* Trigger manually from **Actions → build-apk → Run workflow**, or via pull requests/pushes.
* Download the APK from workflow artifacts named **ve-debug-apk**.
* If you push a tag like `v0.1.5`, the workflow also uploads `ve-debug.apk` as a release asset.

Install notes for debug APKs
----------------------------

If Android shows **“invalid package”** while installing a debug APK over an existing install, uninstall the previously installed app variant first and then install the new APK. This typically happens when signatures differ between old and new builds, or when installing a build with a different applicationId/package variant.

Contributing to Vé
------------------

As with much other open source software, Vé is based on voluntary work. If you want, you can contribute to the development of Vé. This might be either through reporting bugs in the issue tracker, contributing code, translating Vé into your own language, and/or donating an amount of your choice. Generally, code contributions (except translations) are accepted through Github pull requests.

Translations are handled by The Translation Project, via PO files. Download the PO file of your language from [Vé's TranslationProject page](http://translationproject.org/domain/ve.html) and edit it with your favourite PO editor, such as Vé on Android, [lokalize](http://userbase.kde.org/Lokalize)
or [poedit](http://sourceforge.net/projects/poedit/) on desktop computers. When you're done, the file needs to be sent to the [Translation Project Robot](http://translationproject.org/html/robot.html). Beware that your language might already have a language team, and if so, you should coordinate your efforts with them. The Translation Project [has a list](http://translationproject.org/team/index.html) of translation teams, complete with contact info.

Donations of any amount that you see fit are kindly accepted through Bitcoin address 12FPDWwNYyn6wRfybncM5VcJpM4ZP6QNnM. At some point in the future, in-app donations through Google Play will be possible.

![Donate Bitcoins](https://raw.github.com/pryds/ve/master/various/ve-donations-qr.png)

Contributor workflow (step-by-step)
-----------------------------------

1. **Sync and branch**
   * Pull latest changes and create a focused branch for one small change set.
2. **Build**
   * Run `./gradlew :app:assembleDebug`.
3. **Run targeted checks**
   * Run any relevant existing checks for your change area.
4. **Install and verify**
   * Install debug APK and execute the **Manual smoke test (baseline)** checklist above.
5. **Submit small PR**
   * Keep PR scope narrow (one modernization slice), include test notes and smoke-test results.
