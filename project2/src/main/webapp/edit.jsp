<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="java.sql.*" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Edit Post</title>
    <style>
        h1 {
           width: 100%;
           height: 100px;
           position: static;
           top: 1em;
           left: 0em;
           background-color: #f2f2f2;
           margin: 0em 0em 0em 0em;

        }
        body{
            margin: 0em 0em 0em 0em;
        }
        #text{
            width: 100%
            height: 100px;
            position: static;
        }

    </style>
</head>
<body>
    <div><h1><br>&nbsp&nbsp&nbsp&nbsp&nbsp&nbspEdit Post</h1></div>
    <% 
        String title = (String)request.getAttribute("title"); 
        String data = (String)request.getAttribute("body");
        String username = (String)request.getParameter("username");
        int postid = Integer.parseInt(request.getParameter("postid"));
    %>

    <form action="/editor/post" method="post" id="form1">
        <input type="hidden" name="username" value="<%= username %>">
        <input type="hidden" name="postid" value=<%= postid %> >
       <div>
           <label for="title">Title</label>
           <br><input type="text" id="text" name="title" value="<%= title %>">
       </div>
    </form>
    <div>
        <label for="body">Body</label>
        <br><textarea rows="10" cols="100" name="body" id="body" form="form1"> <%= data %> </textarea>
    </div>
    &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp<button type="submit" name="action" value="save" form="form1">Save</button>
    &nbsp&nbsp&nbsp<button type="submit" name="action" value="preview" form="form1">Preview</button>
    <form action="/editor/post" method="post">
        <input type="hidden" name="action" value ="list">
        <input type="hidden" name="username" value="<%= username %>">
        &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp<button type="submit">Close</button>
    </form>
    <form action="/editor/post" method="post">
        <input type="hidden" name="action" value="delete">
        <input type="hidden" name="username" value="<%= username%>">
        <input type="hidden" name="postid" value=<%= postid%>>
        &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp<button type="submit">Delete</button>
    </form>

</body>
</html>
