<j:jelly xmlns:j="jelly:core" xmlns:d="jelly:define" xmlns:x="jelly:xml">
  <dl class="sample-list">
    
    <!-- this is kind of ugly -->
    <j:new var="dir" className="java.io.File">
      <j:arg value="${attrs.href}"/>
    </j:new>
    
    <j:forEach var="file" items="${dir.getAbsoluteFile().listFiles()}">
      <j:if test="${file.isDirectory()}">
        <!-- load sample.meta -->
		    <j:new var="metaFile" className="java.io.File">
		      <j:arg value="${file}"/>
		      <j:arg value="/sample.meta"/>
		    </j:new>
        <x:parse var="meta" xml="${metaFile.toURL()}"/>

        <dt>
          <a href="../samples/{file.name}">
            <x:expr select="$meta/sample/title" />
          </a>
          <!-- put 'new' icon on samples introduced in this release -->
          <x:if select="$meta/sample[@since=$IMPL_VERSION]">
            <img src="new.gif" />
          </x:if>
        </dt>
        <dd>
          <x:expr select="$meta/sample/description" />
        </dd>
      </j:if>
    </j:forEach>
  </dl>
</j:jelly>