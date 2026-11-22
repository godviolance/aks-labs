<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="lesson" type="edu.aks.catalog.model.Lesson" required="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<form method="post" class="editor">
  <input type="hidden" name="token" value="${sessionScope.token}">
  <input type="hidden" name="action" value="${empty lesson ? 'lesson-create' : 'lesson-update'}">
  <input type="hidden" name="id" value="${lesson.id}">
  <label>Тема<input name="topic" required maxlength="160" value="${fn:escapeXml(lesson.topic)}"></label>
  <div class="field">
    <label for="lesson-course-${empty lesson ? 'new' : lesson.id}">Курс</label>
    <select id="lesson-course-${empty lesson ? 'new' : lesson.id}" name="courseId" required>
    <option value="">Выберите курс</option>
    <c:forEach items="${requestScope.courses}" var="choice">
      <option value="${choice.id}" ${choice.id == lesson.course.id ? 'selected' : ''}><c:out value="${choice.title}"/></option>
    </c:forEach>
    </select>
  </div>
  <label>Длительность, мин<input type="number" name="minutes" min="1" max="1440" required value="${lesson.minutes}"></label>
  <button>Сохранить</button>
</form>
