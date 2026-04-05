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
* `FileChooser`: local file picker.
* `SettingsActivity`/`SettingsFragment`: translator metadata preferences.
* `res/`: layouts, strings, menus and drawables.

How to modernize Vé for newer Android versions
----------------------------------------------

This codebase currently targets Android API level 18 (`targetSdkVersion="18"` in `AndroidManifest.xml`) and predates modern Android build/tooling. A practical upgrade path is:

1. **Migrate to Gradle + Android Gradle Plugin**
    * Replace Eclipse/Ant project files (`.classpath`, `project.properties`) with `build.gradle` and Gradle wrapper.
    * Move to the latest stable `compileSdkVersion`/`targetSdkVersion` and a realistic `minSdk` baseline (for example API 21+).
2. **Migrate support libraries to AndroidX**
   * Replace old support APIs and deprecated framework APIs with AndroidX equivalents.
3. **Update deprecated Activity/Fragment patterns**
   * Replace `startActivityForResult`/`onActivityResult` with Activity Result APIs.
   * Replace legacy preference APIs with `androidx.preference`.
4. **Modernize storage/file access**
   * Replace direct legacy external storage assumptions with modern storage access patterns (SAF/scoped storage friendly flows).
5. **Run compatibility validation**
   * Test on recent Android versions/emulators (Android 12, 13, 14+) and verify open/edit/save PO flows.

How to improve the codebase
---------------------------

For the highest long-term quality and maintainability:

* Add automated testing (unit tests for PO parser and serialization, plus instrumentation tests for file open/save flows).
* Move parsing/saving work off the main thread to avoid UI jank on large files.
* Introduce architecture boundaries (for example ViewModel + repository-style separation) to reduce Activity complexity.
* Replace legacy collections/utilities where practical with modern Java/Kotlin equivalents.
* Add static quality gates (Android Lint, CI workflow, formatting/lint checks).
* Audit and remove dead/disabled features or fully modernize them (for example old billing/backup paths).

Legacy build/testing quick start (current state)
------------------------------------------------

This repository now includes an Ant build pipeline at `build.xml` that can produce a full debug APK using the installed Android command-line toolchain.

1. Configure an Android SDK path using one of:
   * `local.properties` with `sdk.dir=/absolute/path/to/android-sdk`
   * `ANDROID_HOME`
   * `ANDROID_SDK_ROOT`
2. Ensure your SDK contains:
   * `platforms/android-34` (or override with `-Dsdk.api.level=...`)
   * `build-tools/34.0.0` (or override with `-Dbuild.tools.version=...`)
3. Run:
   * `ant -p` to list available targets
   * `ant check-sdk` to validate SDK/tooling wiring
   * `ant debug` to build a full debug APK at `bin/ve-debug.apk`
   * `ant ci-debug` to run the clean CI-style full debug build

APK exposure for testing (artifact/release)
-------------------------------------------

This repository now includes GitHub Actions workflow:

* Workflow file: `.github/workflows/build-apk.yml`
* Trigger manually from **Actions → build-apk → Run workflow**, or via pull requests/pushes.
* Download the APK from workflow artifacts named **ve-debug-apk**.
* If you push a tag like `v0.1.5`, the workflow also uploads `ve-debug.apk` as a release asset.

Contributing to Vé
------------------

As with much other open source software, Vé is based on voluntary work. If you want, you can contribute to the development of Vé. This might be either through reporting bugs in the issue tracker, contributing code, translating Vé into your own language, and/or donating an amount of your choice. Generally, code contributions (except translations) are accepted through Github pull requests.

Translations are handled by The Translation Project, via PO files. Download the PO file of your language from [Vé's TranslationProject page](http://translationproject.org/domain/ve.html) and edit it with your favourite PO editor, such as Vé on Android, [lokalize](http://userbase.kde.org/Lokalize)
or [poedit](http://sourceforge.net/projects/poedit/) on desktop computers. When you're done, the file needs to be sent to the [Translation Project Robot](http://translationproject.org/html/robot.html). Beware that your language might already have a language team, and if so, you should coordinate your efforts with them. The Translation Project [has a list](http://translationproject.org/team/index.html) of translation teams, complete with contact info.

Donations of any amount that you see fit are kindly accepted through Bitcoin address 12FPDWwNYyn6wRfybncM5VcJpM4ZP6QNnM. At some point in the future, in-app donations through Google Play will be possible.

![Donate Bitcoins](https://raw.github.com/pryds/ve/master/various/ve-donations-qr.png)
