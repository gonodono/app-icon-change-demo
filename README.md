## Android App Icon Change Demonstration

A simple demonstration of how to implement Todoist's (apparent) app icon change behavior,
as addressed in [this Stack Overflow post](https://stackoverflow.com/q/68576022).

They use the common technique of toggling the enabled state of multiple `<activity-alias>`
components with the various available icons. However, after initially activating the
option and restarting, they manage to change aliases from then on without killing the app.

The "trick" is two-part:

+ After the initial activation that sets the default, initial alias to `DISABLED` explicitly,
the other aliases can be disabled by setting them back to `DEFAULT` instead, as they are all
disabled by default; i.e., they're all `android:enabled="false"` in the manifest. This won't
cause the app to be killed, but it seems to fail on its own without part two:

+ The `Activity` is `recreate()`d immediately after the enable/disable operations. This
appears to substitute sufficiently for killing and restarting the app, as afterward, the
component set to `DEFAULT` will indeed go back to being disabled. Without this, we end up
with multiple aliases active at once.

---

Just to note, the `Alias` enum came from mimicking Todoist's setup at the start. After I'd
stripped this example down to the bare essentials, it became obvious that the enum was
somewhat pointless, as it's basically just being used as a list of `String`s. I left it in,
however, since a real implementation might actually have good reason to use it (and I'm
lazy).