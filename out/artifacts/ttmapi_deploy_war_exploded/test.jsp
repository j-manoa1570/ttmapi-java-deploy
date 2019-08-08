<%--
  Created by IntelliJ IDEA.
  User: Jester
  Date: 8/5/2019
  Time: 1:34 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
    <head>
        <title>test</title>
    </head>
    <body>
        <h1>Hello World!</h1>
        <form action="/ttmapi_deploy_war_exploded/processUser" method="post">
            <p>
                Username:
                <input type="text" name="loginname">
                <br/>
                Password:
                <input type="password" name="loginpass">
            </p>
            <input type="submit" value="Login"/>
        </form>
        <hr/>
        <p>Don't have an account? Create one <a href="account_setup.jsp">here</a>.</p>

</body>
</html>
