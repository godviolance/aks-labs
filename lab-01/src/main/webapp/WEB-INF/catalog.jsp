<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ru">
<head><meta charset="utf-8"><title>Каталог курсов</title></head>
<body>
<h1>Каталог курсов</h1>
<h2>Курсы</h2>
<c:forEach items="${courses}" var="course">
  <article><h3><c:out value="${course.title}"/></h3><p><c:out value="${course.description}"/></p></article>
</c:forEach>
<h2>Занятия</h2>
<c:forEach items="${lessons}" var="lesson">
  <article><h3><c:out value="${lesson.topic}"/></h3>
    <p><c:out value="${lesson.course.title}"/> · ${lesson.minutes} мин</p>
  </article>
</c:forEach>
</body>
</html>
