package org.kohsuke.rngom.parse.compact;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.net.URL;

import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.IncludedGrammar;
import org.kohsuke.rngom.ast.builder.SchemaBuilder;
import org.kohsuke.rngom.ast.builder.Scope;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.Parseable;
import org.kohsuke.rngom.xml.util.EncodingMap;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

/**
 * RELAX NG schema in the compact syntax.
 */
public class CompactParseable implements Parseable {
  private final InputSource in;
  private final ErrorHandler eh;

  public CompactParseable(InputSource in, ErrorHandler eh) {
    this.in = in;
    this.eh = eh;
  }

  public ParsedPattern parse(SchemaBuilder sb) throws BuildException, IllegalSchemaException {
      ParsedPattern p = new CompactSyntax(this, makeReader(in), in.getSystemId(), sb, eh, "").parse(null);
      return sb.expandPattern(p);
  }

  public ParsedPattern parseInclude(String uri, SchemaBuilder sb, IncludedGrammar g, String inheritedNs)
          throws BuildException, IllegalSchemaException {
    InputSource tem = new InputSource(uri);
    tem.setEncoding(in.getEncoding());
    return new CompactSyntax(this, makeReader(tem), uri, sb, eh, inheritedNs).parseInclude(g);
  }

  public ParsedPattern parseExternal(String uri, SchemaBuilder sb, Scope scope, String inheritedNs)
          throws BuildException, IllegalSchemaException {
    InputSource tem = new InputSource(uri);
    tem.setEncoding(in.getEncoding());
    return new CompactSyntax(this, makeReader(tem), uri, sb, eh, inheritedNs).parse(scope);
  }

  private static final String UTF8 = EncodingMap.getJavaName("UTF-8");
  private static final String UTF16 = EncodingMap.getJavaName("UTF-16");

  private static Reader makeReader(InputSource is) throws BuildException {
    try {
      Reader r = is.getCharacterStream();
      if (r == null) {
        InputStream in = is.getByteStream();
        if (in == null) {
          String systemId = is.getSystemId();
          in = new URL(systemId).openStream();
        }
        String encoding = is.getEncoding();
        if (encoding == null) {
          PushbackInputStream pb = new PushbackInputStream(in, 2);
          encoding = detectEncoding(pb);
          in = pb;
        }
        r = new InputStreamReader(in, encoding);
      }
      return r;
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
  }

  static private String detectEncoding(PushbackInputStream in) throws IOException {
    String encoding = UTF8;
    int b1 = in.read();
    if (b1 != -1) {
      int b2 = in.read();
      if (b2 != -1) {
        in.unread(b2);
        if ((b1 == 0xFF && b2 == 0xFE) || (b1 == 0xFE && b2 == 0xFF))
          encoding = UTF16;
      }
      in.unread(b1);
    }
    return encoding;
  }
}
