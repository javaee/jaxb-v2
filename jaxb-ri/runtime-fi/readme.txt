runtime code that supports FastInfoset (http://fi.dev.java.net/)
we don't want the whole runtime to require FI, so we created this separate module
to make sure that the wrong dependency won't creep in.

These classes will be still bundled into the same jar as the rest of jar files.