<j:jelly xmlns:j="jelly:core" xmlns:d="jelly:define" xmlns:x="jelly:xml">
  <HEAD>
    <!-- parse TOC and export it -->
    <x:parse var="toc" xml="toc.xml" />
    <j:set scope="parent" var="toc" value="${toc}" />

    <!-- also export the caption -->
    <j:set scope="parent" var="caption" value="${caption}" />

    <d:invokeBody />
    <title>
      <x:expr select="$toc/toc/@name"/> ${IMPL_VERSION} ${IMPL_VERSION_SUFFIX} -- ${caption}
    </title>
  </HEAD>
</j:jelly>