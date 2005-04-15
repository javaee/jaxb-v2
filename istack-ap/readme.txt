AnnootationProcessor driver for the integrated stack.

This code is just meant to do the boot-strapping and nothing else.
Eventually this module should be moved outside the JAXB RI, but
I couldn't find a suitable place for it, so I decided to put
this into the RI workspace just for now.

The JAXB specific processing should be written inside XJC.