<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="ru">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Каталог курсов</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/catalog.css">
</head>
<body>
<main>
  <header><h1>Каталог курсов</h1><nav aria-label="Разделы"><a href="#courses">Курсы</a><a href="#lessons">Занятия</a></nav></header>
  <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
  <div class="columns">
    <section id="courses">
      <h2>Курсы <small>${fn:length(courses)}</small></h2>
      <details class="add"><summary>Добавить курс</summary><ui:courseForm/></details>
      <c:if test="${empty courses}"><p>Курсов пока нет.</p></c:if>
      <c:forEach items="${courses}" var="course">
        <article>
          <h3><c:out value="${course.title}"/></h3><p><c:out value="${course.description}"/></p>
          <details><summary>Изменить курс</summary><ui:courseForm course="${course}"/></details>
          <ui:remove action="course-delete" id="${course.id}" label="Удалить курс"/>
        </article>
      </c:forEach>
    </section>
    <section id="lessons">
      <h2>Занятия <small>${fn:length(lessons)}</small></h2>
      <c:choose>
        <c:when test="${empty courses}"><p>Добавьте курс, чтобы запланировать первое занятие.</p></c:when>
        <c:otherwise><details class="add"><summary>Добавить занятие</summary><ui:lessonForm/></details></c:otherwise>
      </c:choose>
      <c:if test="${not empty courses and empty lessons}"><p>Занятий пока нет.</p></c:if>
      <c:forEach items="${lessons}" var="lesson">
        <article>
          <h3><c:out value="${lesson.topic}"/></h3>
          <p><c:out value="${lesson.course.title}"/> · ${lesson.minutes} мин</p>
          <details><summary>Изменить занятие</summary><ui:lessonForm lesson="${lesson}"/></details>
          <ui:remove action="lesson-delete" id="${lesson.id}" label="Удалить занятие"/>
        </article>
      </c:forEach>
    </section>
  </div>
</main>
</body>
</html>
