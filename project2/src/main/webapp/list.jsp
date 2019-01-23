<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="p1.Post" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.sql.*" %>


<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>List</title>
    <style>
        table {
            width: 100%;
        }
        th,tr, td {
            text-align: left;
        }
        th {
            background-color: #1A85E2;
        }
        th, td{
            padding: 8px;
        }

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
        table{
            width: 100%;
            height: calc(100%-100px);
            position: static;
            top: 2em;
            left: 0am;
        }
    </style>

    <h1><br>&nbsp&nbsp&nbsp&nbsp&nbsp&nbspList</h1>
</head>
<body>
    <div>
    <% String user = (String)request.getAttribute("username"); %>
        <form action="/editor/post" method="post">
            <input type="hidden" name="action" value="open">
            <input type="hidden" name="username" value="<%= user %>">
            <input type="hidden" name="postid" value=0>
            <button type="submit">New Post</button><br>
        </form>
    </div>
    <br><br><br>
    <div>
        <table>
            <tr>
                <th class="GeorgiaFont">Title</th>
                <th class="GeorgiaFont">Created</th>
                <th class="GeorgiaFont">Last Modified</th>
                <th class="GeorgiaFont"></th>
                <th class="GeorgiaFont"></th>

            </tr>

        <%
            for (Post p: (ArrayList<Post>)request.getAttribute("posts")){
                String title = p.getTitle();
                Timestamp created = p.getCreate();
                Timestamp modified = p.getMod();
                int postid = p.getPostID();
                
                %> 
                    <tr>
                        <td class="GeorgiaFont"><%= title %></td>
                        <td class="GeorgiaFont"><%= created %></td>
                        <td class="GeorgiaFont"><%= modified %></td>
                        <td>
                            <form action="/editor/post" method="get">
                            <input type="hidden" name="action" value="open">
                            <input type="hidden" name="username" value="<%= user%>">
                            <input type="hidden" name="postid" value=<%= postid%> >
                            <button type="submit">Open</button>
                            </form>
                        </td>
                        <td>
                            <form action="/editor/post" method="post">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="username" value="<%= user%>">
                                <input type="hidden" name="postid" value=<%= postid%>>
                                <button type="submit">Delete</button>
                            </form>
                        </td>
                    </tr>
           <% } %>
            </table>
    </div>
 
</body>
</html>