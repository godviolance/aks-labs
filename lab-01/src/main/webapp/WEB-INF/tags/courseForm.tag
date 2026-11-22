<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="course" type="edu.aks.catalog.model.Course" required="false" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<form method="post" class="editor">
  <input type="hidden" name="token" value="${sessionScope.token}">
  <input type="hidden" name="action" value="${empty course ? 'course-create' : 'course-update'}">
  <input type="hidden" name="id" value="${course.id}">
  <label>Название<input name="title" required maxlength="120" value="${fn:escapeXml(course.title)}"></label>
  <label>Описание<textarea name="description" maxlength="1000" rows="3">${fn:escapeXml(course.description)}</textarea></label>
  <button>Сохранить</button>
</form>
