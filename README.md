# Android App Icon Change Demonstration

A simple example showing how to implement Todoist's app icon change behavior, as
discussed in [this Stack Overflow post][SO-post].

Todoist uses the common method of toggling the enabled state of multiple
`<activity-alias>` components with the various available icons. However, after
initially activating the option and restarting, they manage to change aliases
from then on without killing the app. The trick is two-part:

- After the initial activation that sets the default alias to `DISABLED`
  explicitly, the other aliases can be disabled by setting them back to
  `DEFAULT` instead, as they are all disabled by default; i.e., they're all
  `android:enabled="false"` in the manifest. This won't cause the app to be
  killed, but it seems to fail on its own without part two:

- The `Activity` is `recreate()`d immediately after the enable/disable
  operations. This appears to substitute sufficiently for killing and restarting
  the app, as afterward the component set to `DEFAULT` will indeed go back to
  being disabled. Without this, we end up with multiple aliases active at once.

## Main design points

- **Initial Alias:** The only `<activity-alias>` that is enabled by default.
  Appropriately named `InitialAlias` here.

- **Initial Alias Clone:** Similar to the Initial Alias, but disabled by
  default. This one is enabled during activation, to look like nothing's really
  changed yet. Named `CloneInitialAlias` in this example, for consistency.

- **Activation** simply means explicitly disabling the Initial Alias, enabling
  the Clone, and restarting. After this, switching icons involves only the
  aliases that were disabled by default, thus allowing the "no-kill" swaps.

- **Deactivation** entails resetting all of our aliases back to their default
  enabled states and restarting. Apparently `recreate()` is sufficient, but the
  demo does a restart in order to keep the code and semantics symmetric with
  activation.

## Overview

The main code functionality has been consolidated in the
[`IconChangeManager`][IconChangeManager] class to clearly separate it from the
incidental example UI. Upon instantiation, it uses `PackageManager` to retrieve
info about the app's manifest components, finds our `<activity-alias>` elements,
determines which are the initial and clone aliases, and figures out the current
state from the default and runtime enabled settings.

All alias info is gleaned from the manifest and other `PackageManager` data.
Todoist defines an enum for the aliases, holding additional data for each in the
instances, and seemingly tracks the state separately in settings/storage (though
I might be mistaken on the specifics). I didn't like the idea of coordinating
those two things, so everything is read fresh each time, ensuring the correct
current state (provided nothing else falls down). The specific setup in the
manifest is therefore pretty crucial, but this method allows any alias-specific
changes to be made there and in resources, without having to modify anything in
code.

## Manifest setup

To aid explanation, [the manifest element][manifest] for the Initial Alias looks
as follows:

```xml
<activity-alias
    android:name=".InitialAlias"
    android:exported="true"
    android:icon="@drawable/ic_launcher_initial"
    android:targetActivity=".MainActivity">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
    <meta-data
        android:name="${applicationId}.ALIAS_TITLE"
        android:value="@string/initial_alias" />
</activity-alias>
```

`IconChangeManager` looks for the `ALIAS_TITLE` meta-data to distinguish the
relevant entries from any other `<activity-alias>`es you might have. We need
only an icon and title for this demo, so the meta-data is a simple string. If
anything more complex is needed, [the docs][metadata-docs] recommend supplying
an `android:resource` instead, pointing to some other resource that holds it all
– e.g. a `res/xml/` file – instead of using multiple `<meta-data>` tags. The
extra processing necessary for that, though, might make it more trouble than
it's worth for just one more string, for instance. You could also consider
something like Todoist's enum arrangement.

As mentioned, the Clone is mostly identical to the Initial Alias except that it
is `android:enabled="false"` by default, and has a different title string,
though that's not strictly necessary. Indeed, `InitialAlias`'s title isn't even
used for this demo. `CloneInitialAlias`'s icon, however, must be the exact same
one as `InitialAlias`'s in order for `IconChangeManager` to correctly identify
them both.

The rest of the `<activity-alias>` entries are the "regular" ones, with the
various selectable icons. Their `ALIAS_TITLE` values are displayed alongside
their icons in the example `Activity`'s selection list.

## Details

`IconChangeManager`, as is, has the following specific requirements:

- Exactly one Initial Alias. That is, one of our `<activity-alias>`es enabled by
  default, either implicitly or with an explicit `android:enabled="true"`
  attribute value.

- Exactly one Initial Alias Clone. That is, specifically one `<activity-alias>`
  that is disabled by default, and has the exact same `android:icon` value as
  the Initial Alias.

- Exactly one of our aliases currently enabled. (Might need to change this one
  if the `Activity` can be otherwise started without an active alias.)

If any of those aren't met, it throws an `IllegalStateException`. We make no
attempt to catch that here, but you may wish to modify things to fail silently
instead, falling back to standard behavior and disabling and/or hiding any
related parts of the UI.

After initialization, `IconChangeManager` will have ready a `List` of our
selectable aliases, the one that's currently active, and whether the feature has
been activated. Properties are available to change the activation state and
current alias. Additionally, two helper functions are included to assist in
determining if the `Activity` is restarting due to an activation or icon change.

- `determineStartMode()` can be called in `onCreate()` to see if the `Activity`
  is starting normally, or is restarting/recreating due to an activation or icon
  change.

- `onSaveInstanceState()` should be called from the `Activity`'s corresponding
  function with the `Bundle` passed there. This is required only if
  `determineStartMode()` is used.

## Notes

- I don't necessarily recommend offering this feature in general. I originally
  investigated this just to see if I could figure out Todoist's technique, and
  that's all this project is: a simple demonstration of that particular
  implementation.

  The `<activity-alias>` solution has always been a hacky workaround for this
  missing functionality in Android, and the only bonus here is possibly being
  able to effect a switch without restarting. It still has the same caveats and
  glitchy behavior that the base approach has always had, including, but not
  limited to:

  - The alias `name`s can't ever be changed after the first release, or it may
    break existing installations.

  - The launcher app, Settings pages, etc., may not work well with this,
    possibly taking a while to update, or requiring a device restart, or
    breaking in unforeseen ways.

  - It might not work at all in certain devices or environments.

- This was originally written in Java. If you would prefer that version, [the
  commit starting with `bdfbdea`][commit] is the last one in that language.

# License

MIT License

Copyright (c) 2024 Mike M.

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


  [SO-post]: https://stackoverflow.com/q/68576022

  [IconChangeManager]: app/src/main/kotlin/com/gonodono/appiconchangedemo/IconChangeManager.kt

  [manifest]: app/src/main/AndroidManifest.xml#L18

  [metadata-docs]: https://developer.android.com/guide/topics/manifest/meta-data-element

  [commit]: https://github.com/gonodono/app-icon-change-demo/commits/bdfbdea621f32ab4a39696da12b4267ea64682b5/