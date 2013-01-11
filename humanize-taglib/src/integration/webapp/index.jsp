<%@ page language="Java" pageEncoding="UTF-8" contentType="text/html" import="java.util.Date" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="hmnz" uri="http://mfornos.github.com/humanize/taglib" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout>
    <jsp:attribute name="header">
      <h1>Humanize Taglib Example</h1>
    </jsp:attribute>
    <jsp:attribute name="footer">
      <p id="copyright">Humanize for Java. <a href="http://github.com/mfornos">Github</a>.</p>
    </jsp:attribute>
    <jsp:body>
    
    <h2>Directive</h2>
    
<pre><code class="javascript">&lt;%@ taglib prefix="hmnz" uri="http://mfornos.github.com/humanize/taglib" %&gt;</code></pre>
    
    <h3>Load bundle</h3>
<pre><code>&lt;%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %&gt;   
&lt;fmt:setBundle basename="test.sample" scope="application" /&gt;</code></pre>
    
    <h2>Messages</h2>
    
    <h3>Source</h3>
<pre><code class="html">&lt;hmnz:message key="ho" var="ho" /&gt;
&lt;hmnz:message key="hi"&gt;
  &lthmnz:param value="\${ho}"/&gt;
&lt/hmnz:message&gt;
      
msg = '\${ho}'</code></pre>
    
    <h3>Output</h3>
    
    <pre class="out">
    <hmnz:message key="ho" var="ho" />
    <hmnz:message key="hi"><hmnz:param value="${ho}"/></hmnz:message>
    
    msg = '${ho}'
    </pre>
    
    <h2>Plurals</h2>
    
    <h3>Source</h3>
    
<pre><code class="html">&lt;hmnz:message key="plural.template" var="ptmpl" /&gt;
&lt;hmnz:pluralizeMessage value="3"&gt;\${ptmpl}&lt;/hmnz:pluralizeMessage&gt;
&lt;hmnz:pluralizeMessage value="1"&gt;\${ptmpl}&lt;/hmnz:pluralizeMessage&gt;
&lt;hmnz:pluralizeMessage value="0" template="\${ptmpl}" /&gt;
template = '\${ptmpl}'
    
&lt;hmnz:pluralizeMessage value="3"&gt;
  nothing::something::{0} things
&lt;/hmnz:pluralizeMessage&gt;
&lt;hmnz:pluralizeMessage value="1"&gt;something::{0} things&lt;/hmnz:pluralizeMessage&gt;
&lt;hmnz:pluralizeMessage value="0" template="nothing::something::{0} things" /&gt;

&lt;hmnz:pluralizeMessage value="0" template="no{1}::some{1}::{0} {1}s" args="thing" /&gt;
&lt;hmnz:pluralizeMessage value="1" template="no{1}::some{1}::{0} {1}s" args="thing" /&gt;
&lt;hmnz:pluralizeMessage value="2" template="no{1}::some{1}::{0} {1}s" args="thing" /&gt;
    
&lt;hmnz:pluralizeMessage value="1" template="no{1} {2}::some{1} {2}::{0} {1}s {2}" 
    args="thing,extra" /&gt;
&lt;hmnz:pluralizeMessage value="1" template="no{1} {2}::some{1} {2} {3}::{0} {1}s {2}" 
    args="thing, extra1, extra2" /&gt;</code></pre>
    
    <h3>Output</h3>
    
    <pre class="out">
    <hmnz:message key="plural.template" var="ptmpl" />
    <hmnz:pluralizeMessage value="3">${ptmpl}</hmnz:pluralizeMessage>
    <hmnz:pluralizeMessage value="1">${ptmpl}</hmnz:pluralizeMessage>
    <hmnz:pluralizeMessage value="0" template="${ptmpl}" />
    template = '${ptmpl}'
    
    <hmnz:pluralizeMessage value="3">nothing::something::{0} things</hmnz:pluralizeMessage>
    <hmnz:pluralizeMessage value="1">something::{0} things</hmnz:pluralizeMessage>
    <hmnz:pluralizeMessage value="0" template="nothing::something::{0} things" />
    
    <hmnz:pluralizeMessage value="0" template="no{1}::some{1}::{0} {1}s" args="thing" />
    <hmnz:pluralizeMessage value="1" template="no{1}::some{1}::{0} {1}s" args="thing" />
    <hmnz:pluralizeMessage value="2" template="no{1}::some{1}::{0} {1}s" args="thing" />
    
    <hmnz:pluralizeMessage value="1" template="no{1} {2}::some{1} {2}::{0} {1}s {2}" args="thing,extra" />
    <hmnz:pluralizeMessage value="1" template="no{1} {2}::some{1} {2} {3}::{0} {1}s {2}" args="thing, extra1, extra2" />
    </pre>
    
    <h3>Source</h3>
    
<pre><code class="html">&lt;hmnz:pluralize one="something" many="{0} things" value="1" /&gt;
&lt;hmnz:pluralize one="something" many="{0} things" value="2" /&gt;
&lt;hmnz:pluralize one="something" many="{0} things" none="nothing" value="0" /&gt;
&lt;hmnz:pluralize one="something" many="{0} things" none="nothing" value="1" /&gt;
&lt;hmnz:pluralize one="something" many="{0} things" none="nothing" value="2" /&gt;
&lt;hmnz:pluralize one="one {1}" many="{0} {1}s" none="no {1}s" value="0" args="disk" /&gt;
&lt;hmnz:pluralize one="one {1}" many="{0} {1}s" none="no {1}s" value="1" args="disk" /&gt;
&lt;hmnz:pluralize one="one {1}" many="{0} {1}s" none="no {1}s" value="2" args="disk" /&gt;</code></pre>
    
    <h3>Output</h3>
    
    <pre class="out">
    <hmnz:pluralize one="something" many="{0} things" value="1" />
    <hmnz:pluralize one="something" many="{0} things" value="2" />
    <hmnz:pluralize one="something" many="{0} things" none="nothing" value="0" />
    <hmnz:pluralize one="something" many="{0} things" none="nothing" value="1" />
    <hmnz:pluralize one="something" many="{0} things" none="nothing" value="2" />
    <hmnz:pluralize one="one {1}" many="{0} {1}s" none="no {1}s" value="0" args="disk" />
    <hmnz:pluralize one="one {1}" many="{0} {1}s" none="no {1}s" value="1" args="disk" />
    <hmnz:pluralize one="one {1}" many="{0} {1}s" none="no {1}s" value="2" args="disk" />
    </pre>
    
    <h2>Text</h2>

    <h3>Source</h3>
<pre><code class="html">&lt;hmnz:slugify value="àlicion Aléòio 23" /&gt;
&lt;hmnz:slugify&gt;\${ho}àlicion Aléòio 23&lt;/hmnz:slugify&gt;</code></pre>
    
    <h3>Output</h3>
    
    <pre class="out">
    
    <hmnz:slugify value="àlicion Aléòio 23" />
    <hmnz:slugify>${ho}àlicion Aléòio 23</hmnz:slugify>
    </pre>
    
    <h2>Number</h2>

    <h3>Source</h3>
<pre><code class="html">&lt;hmnz:binaryPrefix&gt;1024&lt;/hmnz:binaryPrefix&gt;
&lt;hmnz:binaryPrefix value="10240" var="size" /&gt;
&lt;hmnz:metricPrefix value="100000000" /&gt;
    
&lt;hmnz:ordinal value="2" /&gt;
&lt;hmnz:spellBigNumber value="2908093808308303" /&gt;
&lt;hmnz:spellDigit value="2" /&gt;
    
size = '${size}'</code></pre>

    <h3>Output</h3>
    
    <pre class="out">
    <hmnz:binaryPrefix value="10240" var="size" />
    <hmnz:binaryPrefix>1024</hmnz:binaryPrefix>
    <hmnz:metricPrefix value="100000000" />
    
    <hmnz:ordinal value="2" />
    <hmnz:spellBigNumber value="2908093808308303" />
    <hmnz:spellDigit value="2" />
    
    size = '${size}'
    </pre>
    
    <h2>Date &amp; Time</h2>
    
    <h3>Source</h3>
    
<pre><code class="html">&lt;hmnz:nanoTime value="898734" var="nanos" /&gt;
&lt;hmnz:duration value="898734" /&gt;
&lt;hmnz:duration value="898734" style="french_decimal" /&gt;
&lt;hmnz:naturalTime from="&lt;%= new Date(0) %&gt;" to="&lt;%= new Date(10000000L) %&gt;" /&gt;
&lt;hmnz:naturalTime to="&lt;%= new Date(10000000L) %&gt;" /&gt;
&lt;hmnz:naturalDay value="&lt;%= new Date() %&gt;" /&gt;
    
    nanos = '\${nanos}'</code></pre>
    
    <h3>Output</h3>
    
    <pre class="out">
    <hmnz:nanoTime value="898734" var="nanos" />
    <hmnz:duration value="898734" />
    <hmnz:duration value="898734" style="french_decimal" />
    <hmnz:naturalTime from="<%= new Date(0) %>" to="<%= new Date(10000000L) %>" />
    <hmnz:naturalTime to="<%= new Date(10000000L) %>" />
    <hmnz:naturalDay value="<%= new Date() %>" />
    
    nanos = '${nanos}'
    </pre>     
    
    </jsp:body>
</t:layout>
