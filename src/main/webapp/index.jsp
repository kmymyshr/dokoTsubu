<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>どこつぶ</title>
</head>
<body>
<h1>どこつぶへようこそ!</h1>


<form action="Login" method="post">
   <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
   ユーザー名：<input type="text" name="name"><br>
   パスワード：<input type="password" name="pass"><br>
   <input type="submit" value="ログイン">
   </form>
   
   <hr>
   <p>ユーザ登録は<a href="Register">こちら</a></p>
   
</body>
</html>
