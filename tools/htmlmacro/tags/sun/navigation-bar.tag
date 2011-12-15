<?xml version="1.0" encoding="us-ascii" ?>
<!--
  Creates a navigation bar in Sun theme.
-->
<j:jelly xmlns:j="jelly:core" xmlns:x="jelly:xml">
  <!-- parse TOC if necessary -->
  <j:if test="${toc==null}">
    <x:parse var="toc" xml="${tocXml}" />
  </j:if>
  <x:set var="toc" select="$toc/toc" />

  <!-- tabs -->
  <table class="navbar" cellspacing="0">
    <tr>
      <x:forEach var="chapter" select="$toc/chapter">
        <x:set var="active" select="boolean($chapter/document[starts-with(@url,$fileName)])" />
        <j:choose>
          <j:when test="${active}">
            <td class="active">
              <a>
                <x:expr select="$chapter/@name" />
              </a>
            </td>
          </j:when>
          <j:otherwise>
            <td class="inactive">
              <x:set var="href" select="string($chapter/document[1]/@url)" />
              <a href="${href}">
                <x:expr select="$chapter/@name" />
              </a>
            </td>
          </j:otherwise>
        </j:choose>
      </x:forEach>
    </tr>
  </table>
  <!-- sub navigation bar -->
  <x:forEach var="chapter" select="$toc/chapter">
    <x:set var="active" select="boolean($chapter/document[starts-with(@url,$fileName)])" />
    <j:if test="${active}">
      <div class="subnavbar">
        <ul>
          <j:set var="first" value="${true}" />
          <x:forEach var="document" select="$chapter/document">
            <x:set var="activeDocument" select="starts-with($document/@url,$fileName)" />
            <x:element name="li">
              <j:if test="${first}">
                <x:attribute name="class">first</x:attribute>
                <j:set var="first" value="${false}" />
              </j:if>
              <x:set var="href" select="string($document/@url)" />
              <a href="${href}">
                <x:element name="span">
                  <j:if test="${activeDocument}">
                    <x:attribute name="class">active</x:attribute>
                  </j:if>
                  <x:expr select="$document/@name" />
                  <!-- whitespace -->
                  <j:whitespace trim="false"> </j:whitespace>
                </x:element>
              </a>
           </x:element>
          </x:forEach>
        </ul>
      </div>
    </j:if>
  </x:forEach>
</j:jelly>

