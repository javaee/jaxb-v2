<j:jelly xmlns:j="jelly:core" xmlns:d="jelly:define" xmlns:x="jelly:xml">
  <BODY>
    <h1>
      <x:copyOf select="$toc/toc/banner" />

      <br />
      ${caption} <!-- exported by head.tag -->
    </h1>
    <center>
      <j:if test="${SPEC_VERSION!=null}">
        <b>Specification Version:</b> ${SPEC_VERSION}<br />
      </j:if>
      <j:if test="${IMPL_VERSION!=null}">
        <b>Implementation Version:</b> ${IMPL_VERSION} ${IMPL_VERSION_SUFFIX}<br />
      </j:if>
    </center>

    <navigation-bar toc="${toc}" />

    <d:invokeBody />
  </BODY>
</j:jelly>