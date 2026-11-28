<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" encoding="UTF-8"/>
  <xsl:param name="format" select="'xml'"/>
  <xsl:variable name="api">
    <xsl:choose>
      <xsl:when test="/course or /lesson or /error">..</xsl:when>
      <xsl:otherwise>.</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:template match="/">
    <html lang="ru">
      <head>
        <meta charset="utf-8"/>
        <title>Каталог курсов</title>
        <link rel="stylesheet" href="{$api}/../catalog.css"/>
      </head>
      <body>
        <main>
          <header>
            <h1>Каталог курсов</h1>
            <nav>
              <a href="{$api}/courses?format={$format}">Курсы</a>
              <a href="{$api}/lessons?format={$format}">Занятия</a>
              <a href="{$api}/../">Веб-интерфейс</a>
            </nav>
          </header>
          <xsl:apply-templates select="*"/>
        </main>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="courses">
    <h2>Курсы</h2>
    <xsl:if test="not(course)"><p>Курсов пока нет.</p></xsl:if>
    <xsl:for-each select="course">
      <article><h3><a href="{$api}/courses/{id}?format={$format}"><xsl:value-of select="title"/></a></h3>
        <p><xsl:value-of select="description"/></p></article>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="lessons">
    <h2>Занятия</h2>
    <xsl:if test="not(lesson)"><p>Занятий пока нет.</p></xsl:if>
    <xsl:for-each select="lesson">
      <article><h3><a href="{$api}/lessons/{id}?format={$format}"><xsl:value-of select="topic"/></a></h3>
        <p><a href="{$api}/courses/{courseId}?format={$format}"><xsl:value-of select="courseTitle"/></a></p></article>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="course">
    <article>
      <h2><xsl:value-of select="title"/></h2>
      <p><xsl:value-of select="description"/></p>
      <p><a href="{$api}/lessons?format={$format}&amp;courseId={id}">Занятия курса</a></p>
    </article>
  </xsl:template>

  <xsl:template match="lesson">
    <article>
      <h2><xsl:value-of select="topic"/></h2>
      <p><xsl:value-of select="minutes"/> мин</p>
      <p><a href="{$api}/courses/{courseId}?format={$format}"><xsl:value-of select="courseTitle"/></a></p>
    </article>
  </xsl:template>

  <xsl:template match="error">
    <p role="alert"><xsl:value-of select="message"/></p>
  </xsl:template>
</xsl:stylesheet>
