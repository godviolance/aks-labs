<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="action" required="true" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="label" required="true" %>
<form method="post" class="remove">
  <input type="hidden" name="token" value="${sessionScope.token}">
  <input type="hidden" name="action" value="${action}">
  <input type="hidden" name="id" value="${id}">
  <button>${label}</button>
</form>
