/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

import java.io.*;

/**
 * Create and Read Serialization streams.
 * 
 * Will only compile under JDK 1.2.
 * Will run under JVM 1.1. 
 * [Note: Less information is output using the verbose switch 
 * under JVM 1.1 since the verbose switch depends on methods
 * added in JDK 1.2.]
 * 
 * Simple usage: java SerialBox <class name>
 *
 * SerialBox will create an instance of <class name>, serialize it a
 * byte stream and deserialize it from a byte stream. SerialBox 
 * provides an environment to quickly verify the serialization and
 * deserialization of a Serializable class.
 *
 * Options:
 * -v - verbose option dumps class and instance information out during
 *      serialization and deserialization. 
 *
 * -nX  - serialize 'X' new instances of Serializable class. 
 *        (Switch for benchmarking.)
 *
 * -s[file] - Only serialize instance of <class name>. If optional 'file'
 *            is not specified, serialize into a bytestream.
 * 
 * -d[file] - Deserialize from 'file'. 
 */
public class SerialBox {
    static String testClassName = null;
    static Class testClass = null;
    static int instances = 1; // number of instances to read/write from stream.
    static private int protocolVersion = ObjectStreamConstants.PROTOCOL_VERSION_2;
    static private boolean serialize = false;
    static private boolean deserialize = false;

    /*use ByteArray if not specified.*/
    static String serialStreamFileName = null;

    public static void usage() {
        System.err.println(
            "Usage:\n"+
            "java SerialBox [options] <class>\n"+
            "    create a new instance of a class, serialize it, and then de-serialize it back\n"+
            "java SerialBox [options] <class> -s<filename>\n"+
            "    create a new instance of a class and serialize and write it to a file\n"+
            "java SerialBox [options] -d<filename>\n"+
            "    deserialize an object from a given file\n"+
            "\n"+
            "  -n <n> : the number of instances to be serialized/deserialized.\n"+
            "  -v     : be verbose\n"+
            "  -p <n> : specify the protocol version\n" 
            );
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException {

        /*
         * process command line arguments
         */

        if (args.length < 1) {
            usage();
            return;
        }

        boolean verbose = false;

        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                switch (arg.charAt(1)) {
                    case 'p' :
                        protocolVersion = Integer.decode(arg.substring(2)).intValue();
                        break;
                        
                    case 's' :
                        serialize = true;
                        serialStreamFileName = arg.substring(2);
                        break;

                    case 'd' :
                        deserialize = true;
                        serialStreamFileName = arg.substring(2);
                        break;
                        
                    case 'n' :
                        instances = Integer.decode(arg.substring(2)).intValue();
                        instances = Math.min( instances, 1 );
                        break;
                        
                    case 'v' :
                        verbose = true;
                        break;

                    default :
                        System.err.println("unknown option \"" + arg + "\"");
                        usage();
                        return;
                }
            } else {
                if( testClassName==null )
                    testClassName = arg;
                else {
                    System.err.println("invalid argument \"" + arg + "\"");
                    return;
                }
            }
        }

        Class testClass = null;
        if( testClassName!=null )
            try {
                testClass = Class.forName(testClassName);
            } catch (ClassNotFoundException e) {
            }


        if (serialStreamFileName == null) {
            // If no filename specified only makes sense to perform
            // both serialization and deserialization.
            serialize = deserialize = true;
        }

        /*
         * create object to be serialized
         */

        ByteArrayOutputStream baos = null;
        if (serialize) {
            OutputStream os = null;
            if (serialStreamFileName == null) {
                os = baos = new ByteArrayOutputStream(1024);
            } else
                os = new FileOutputStream(serialStreamFileName);

            try {
                Object obj;
                ObjectOutputStream out = new VerboseObjectOutputStream(os, verbose);
                try {
                    out.useProtocolVersion(protocolVersion);
                } catch (NoSuchMethodError e) {
                    // JVM 1.1 does not have this method.
                }
                if (testClass == null) {
                    if (testClassName != null)
                        throw new ClassNotFoundException(testClassName);
                    else
                        throw new Error("No classname provided to serialize");
                }
                for (int i = 0; i < instances; i++) {
                    try {
                        obj = testClass.newInstance();
                        long start = System.currentTimeMillis();
                        out.writeObject(obj);
                        long duration = System.currentTimeMillis() - start;
                        System.out.println("Time " + duration + " millisecs " + "(" + (duration / 1000.0) + " secs)");
                        System.out.println("Serialize " + obj.toString());
                    } catch (IllegalAccessException e) {
                        System.err.println("Class " + testClassName + " does not have a public constructor.");
                        return;
                    } catch (InstantiationException e) {
                        System.err.println("Exception creating instance of " + testClassName + ":");
                        e.printStackTrace();
                        return;
                    }
                }

                out.close();
            } catch (IOException e) {
                System.err.println("Exception occurred during serialization: ");
                e.printStackTrace();
                return;
            }
        }

        if (deserialize) {
            InputStream is = null;
            if (baos != null) {
                is = new ByteArrayInputStream(baos.toByteArray());
            } else if (serialStreamFileName != null) {
                is = new FileInputStream(serialStreamFileName);
            } else {
                System.err.println("No input stream specified on commandline.");
                return;
            }

            ObjectInputStream in = null;
            try {
                in = new VerboseObjectInputStream(is, verbose);
                long start = System.currentTimeMillis();
                for (int i = 0; i < instances; i++) {
                    try {
                        Object obj = in.readObject();
                        System.out.println("DeSerialize " + obj.toString());
                    } catch (OptionalDataException e) {
                        if (!e.eof)
                            System.out.println("Skipping " + e.length + " bytes");
                        in.skip(e.length);
                    }
                }
                long duration = System.currentTimeMillis() - start;
                System.out.println("Time " + duration + " millisecs " + "(" + (duration / 1000.0) + " secs)");
            } catch (EOFException e) {
            } catch (IOException e) {
                System.err.println("Exception occurred reading object. ");
                e.printStackTrace();
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
        System.exit(0);
    }

    static void print(ObjectStreamClass desc) {
        System.out.println("Class name      : " + desc.getName());
        System.out.println("SerialVersionUID: " + desc.getSerialVersionUID());
        System.out.println("ObjectStreamFields");
        ObjectStreamField[] fields = desc.getFields();
        int numPrim = 0;
        int numObj = 0;
        for (int i = 0; i < fields.length; i++) {
            ObjectStreamField f = fields[i];
            String fieldName = "<fieldName unknown>";
            try {
                fieldName = f.getName();
            } catch (NoSuchMethodError e) {
                // ignore. ObjectStreamField.getName did not exist in JDK 1.1
            }

            if (f.isPrimitive()) {
                numPrim++;
                System.out.println("" + i + ". " + f.getType().getName() + " " + fieldName);
            } else {
                numObj++;
                String ts = "<unknown>";
                try {
                    ts = f.getTypeString();
                } catch (NoSuchMethodError e) {
                    // ignore. ObjectStreamField.getTypeString did not exist in JDK 1.1
                    ts = "<field type unknown>";
                }
                System.out.println("" + i + ". " + ts + " " + fieldName);
            }
        }
        System.out.println("# primitive fields:" + numPrim + " # object ref fields:" + numObj);
    }

    static class VerboseObjectOutputStream extends ObjectOutputStream {
        private boolean verbose = false;

        VerboseObjectOutputStream(OutputStream out, boolean verbose) throws IOException {
            super(out);
            this.verbose = verbose;
            enableReplaceObject(verbose);
        }

        protected void annotateClass(Class cl) throws IOException {
            super.annotateClass(cl);
            if (verbose) {
                ObjectStreamClass desc = ObjectStreamClass.lookup(cl);
                System.out.println("******************");
                System.out.println("annotateClass(" + cl.getName() + ")");
                System.out.println("******************");
                SerialBox.print(desc);
            }
        }

        protected Object replaceObject(Object obj) throws IOException {
            if (verbose) {
                System.out.println(
                    "##replaceObject(" + obj.toString() + ", System.idHashCode=" + System.identityHashCode(obj) + ")");
            }
            Object subobj = super.replaceObject(obj);
            if (verbose && subobj != obj) {
                System.out.println("##replaced with " + subobj.toString());
            }
            return subobj;
        }

        public void reset() throws IOException {
            System.out.println("XXX   ObjectOutputStream.reset called XXXX");
            super.reset();
        }

    };

    static class VerboseObjectInputStream extends ObjectInputStream {
        boolean verbose = false;

        VerboseObjectInputStream(InputStream in, boolean verbose) throws IOException, StreamCorruptedException {
            super(in);
            this.verbose = verbose;
            enableResolveObject(verbose);
        }

        public Object resolveObject(Object obj) throws IOException {
            if (verbose) {
                System.out.println("@@resolveObject(" + obj.toString() + ")");
            }
            Object subobj = super.resolveObject(obj);
            if (verbose && subobj != obj) {
                System.out.println("@@replaced with " + subobj.toString());
            }

            return subobj;
        }

        protected Class resolveClass(ObjectStreamClass v) throws ClassNotFoundException, IOException {
            if (verbose) {
                System.out.println("*******************************BEGIN");
                System.out.println("**Stream ObjectStreamClass Descriptor");
                SerialBox.print(v);
            }
            Class cl = super.resolveClass(v);
            if (verbose) {
                ObjectStreamClass localDesc = ObjectStreamClass.lookup(cl);
                if (localDesc != null) {
                    System.out.println("**Local JVM ObjectStreamClass Descriptor");
                    SerialBox.print(localDesc);
                }
                System.out.println("*******************************END ");
            }
            return cl;
        }
    };
}
