package org.kohsuke.rngom.parse.xml;

import java.io.IOException;

import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.IncludedGrammar;
import org.kohsuke.rngom.ast.builder.SchemaBuilder;
import org.kohsuke.rngom.ast.builder.Scope;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.Parseable;
import org.kohsuke.rngom.xml.sax.JAXPXMLReaderCreator;
import org.kohsuke.rngom.xml.sax.XMLReaderCreator;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * RELAX NG schema in the XML syntax.
 * 
 */
public class SAXParseable implements Parseable {
  private final InputSource in;

  final XMLReaderCreator xrc;
  final ErrorHandler eh;

  public SAXParseable(InputSource in, ErrorHandler eh, XMLReaderCreator xrc) {
      this.xrc = xrc;
      this.eh = eh;
    this.in = in;
  }

  public SAXParseable(InputSource in, ErrorHandler eh) {
      this(in,eh,new JAXPXMLReaderCreator());
  }
  
  public ParsedPattern parse(SchemaBuilder schemaBuilder) throws BuildException, IllegalSchemaException {
    try {
      XMLReader xr = xrc.createXMLReader();
      SchemaParser sp = new SchemaParser(this, xr, eh, schemaBuilder, null, null,"");
      xr.parse(in);
      ParsedPattern p = sp.getParsedPattern();
      return schemaBuilder.expandPattern(p);
    }
    catch (SAXException e) {
      throw toBuildException(e);
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
  }

      public ParsedPattern parseInclude(String uri, SchemaBuilder schemaBuilder, IncludedGrammar g, String inheritedNs)
              throws BuildException, IllegalSchemaException {
        try {
          XMLReader xr = xrc.createXMLReader();
          SchemaParser sp = new SchemaParser(this, xr, eh, schemaBuilder, g, g, inheritedNs);
          xr.parse(makeInputSource(xr, uri));
          return sp.getParsedPattern();
        }
        catch (SAXException e) {
         throw SAXParseable.toBuildException(e);
        }
        catch (IOException e) {
         throw new BuildException(e);
        }
      }

      public ParsedPattern parseExternal(String uri, SchemaBuilder schemaBuilder, Scope s, String inheritedNs)
              throws BuildException, IllegalSchemaException {
        try {
          XMLReader xr = xrc.createXMLReader();
          SchemaParser sp = new SchemaParser(this, xr, eh, schemaBuilder, null, s, inheritedNs);
          xr.parse(makeInputSource(xr, uri));
          return sp.getParsedPattern();
        }
        catch (SAXException e) {
          throw SAXParseable.toBuildException(e);
        }
        catch (IOException e) {
          throw new BuildException(e);
        }
      }

      private static InputSource makeInputSource(XMLReader xr, String systemId) throws IOException, SAXException {
        EntityResolver er = xr.getEntityResolver();
        if (er != null) {
          InputSource inputSource = er.resolveEntity(null, systemId);
          if (inputSource != null)
        return inputSource;
        }
        return new InputSource(systemId);
      }

      static BuildException toBuildException(SAXException e) {
        Exception inner = e.getException();
        if (inner instanceof BuildException)
          throw (BuildException)inner;
        throw new BuildException(e);
      }
    }
