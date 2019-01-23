import java.io.IOException;
import java.sql.* ;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.PrintWriter;

import p1.Post;
/**
 * Servlet implementation class for Servlet: ConfigurationTest
 *
 */
public class Editor extends HttpServlet {
    /**
     * The Servlet constructor
     *
     * @see javax.servlet.http.HttpServlet#HttpServlet()
     */
    public Editor() {
    }

    public void init() throws ServletException {
        /*  write any servlet initialization code here or remove this function */
    }

    public void destroy() {
        /*  write any servlet cleanup code here or remove this function */
    }

    /**
     * Handles HTTP GET requests
     *
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request,
     * HttpServletResponse response)
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        PrintWriter out = response.getWriter();
        if (action.equals("list")) {
            // check for required parameters
            String username = request.getParameter("username");
            if (username == null || username.length() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            } else {
                List<Post> posts = new ArrayList<Post>();
                Database db = new Database();
                db.makeConnection();
                db.getPosts(username, posts);
                // call JSP file to dynamically generate page
                request.setAttribute("posts", posts);
                request.setAttribute("username", username);
                request.getRequestDispatcher("/list.jsp").include(request, response);
                db.closeDatabase();
            }
        }

        if (action.equals("open")) {
            String username = request.getParameter("username");
            String c_postid = request.getParameter("postid");

            if (username == null || username.length() == 0 || c_postid == null || c_postid.length() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            } else {
                try {
                    int postid = Integer.parseInt(request.getParameter("postid"));
                    // Check if optional title and body parameters were passed
                    String title = request.getParameter("title");
                    String body = request.getParameter("body");
                    Post post = new Post();
                    if ((title == null || title.length() == 0) && (body == null || body.length() == 0)) {
                        request.setAttribute("username", username);
                        request.setAttribute("postid", postid);
                        Database db = new Database();
                        db.makeConnection();

                        // get Post title and body
                        if (postid == 0) {
                            request.setAttribute("title", "");
                            request.setAttribute("body", "");
                        } else {
                            boolean check = db.getPost(postid, post, username);
                            if (check == true) {
                                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not Found");
                            } else {
                                request.setAttribute("title", post.getTitle());
                                request.setAttribute("body", post.getBody());
                            }
                        }
                        request.getRequestDispatcher("/edit.jsp").include(request, response);
                        db.closeDatabase();
                    }
                    else if ((title != null && title.length() != 0) && (body != null && body.length() != 0)) { // title and body are set
                        request.setAttribute("title", title);
                        request.setAttribute("body", body);
                        request.getRequestDispatcher("/edit.jsp").include(request, response);
                    }
                    else{
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
                    }
                }
                catch (NumberFormatException err){
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Bad Request");
                }

            }
        }

        if (action.equals("preview")) {
            String username = request.getParameter("username");
            String c_postid = request.getParameter("postid");
            String c_title = request.getParameter("title");
            String c_body = request.getParameter("body");

            if (username == null || username.length() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            } else if (c_postid == null || c_postid.length() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            } else if (c_title == null || c_title.length() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            } else if (c_body == null || c_body.length() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            } else {
                try {
                    int postid = Integer.parseInt(request.getParameter("postid"));
                    String title = "<h1>" + request.getParameter("title") + "</h1>";
                    String body = "<div>" + request.getParameter("body") + "</div>";
                    String button = "<body><form action=\"/editor/post\" method=\"get\">\n" +
                            "        <input type=\"hidden\" name=\"action\" value =\"open\">\n" +
                            "        <input type=\"hidden\" name=\"username\" value=\"" + username + "\">\n" +
                            "        <input type=\"hidden\" name=\"postid\" value="+postid+">" +
                            "        <input type=\"hidden\" name=\"title\" value=\"" + request.getParameter("title") + "\">\n" +
                            "        <input type=\"hidden\" name=\"body\" value=\"" + request.getParameter("body") + "\">\n" +
                            "        <button type=\"submit\">Close Preview</button>\n" +
                            "    </form></body>";
                    // show html rendering of given title and body
                    Parser parser = Parser.builder().build();
                    HtmlRenderer renderer = HtmlRenderer.builder().build();
                    String button_md = renderer.render(parser.parse(button));
                    String title_markdown = renderer.render(parser.parse(title));
                    String body_md = renderer.render(parser.parse(body));
                    out.println(button_md);
                    out.println(title_markdown);
                    out.println(body_md);

                } catch (NumberFormatException err){
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Bad Request");
                }

            }


        }
    }

    /**
     * Handles HTTP POST requests
     *
     * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request,
     * HttpServletResponse response)
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // implement your POST method handling code here
        // currently we simply show the page generated by "edit.jsp"
        String action = request.getParameter("action");
        PrintWriter out = response.getWriter();

        if (action.equals("delete")) {
            String username = request.getParameter("username");
            String c_postid = request.getParameter("postid");
            Post p = new Post();
            if (username == null || username.length() == 0 || c_postid == null || c_postid.length() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            } else {
                try{
                    int postid = Integer.parseInt(request.getParameter("postid"));
                    Database db = new Database();
                    db.makeConnection();
                    db.removePost(username, postid);
                    p.updatePostCount("decrease");
                    List<Post> posts = new ArrayList<Post>();
                    db.getPosts(username, posts);
                    request.setAttribute("posts", posts);
                    request.setAttribute("username", username);
                    request.getRequestDispatcher("/list.jsp").include(request, response);
                    db.closeDatabase();
                }
                catch (NumberFormatException e){
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
                }

            }

        }

        if (action.equals("save")) {
            String username = request.getParameter("username");
            String c_postid = request.getParameter("postid");
            String title = request.getParameter("title");
            String body = request.getParameter("body");
            if (username == null || username.length() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            } else if (c_postid == null || c_postid.length() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            } else if (title == null || title.length() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            } else if (body == null || body.length() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            } else {
                try{
                    int postid = Integer.parseInt(request.getParameter("postid"));
                    Database db = new Database();
                    Post post = new Post();

                    //out.println(postid);
                    db.makeConnection();

                    if (postid <= 0) {
                        post.updatePostCount("increase");
                        int newpostid = post.getPostCount();
                        post.setPostID(newpostid);
                        post.setBody(body);
                        post.setTitle(title);
                        post.setUsername(username);
                        db.addNewPost(post); // add post to database
                        List<Post> posts = new ArrayList<Post>();
                        db.getPosts(username, posts); // get all posts including new one
                        // call JSP file to dynamically generate page
                        request.setAttribute("posts", posts);
                        request.setAttribute("username", username);
                        request.getRequestDispatcher("/list.jsp").include(request, response); // list all posts
                    } else if (postid > 0) {
                        // checks if post exists and updates
                        post.setPostID(postid);
                        post.setBody(body);
                        post.setTitle(title);
                        post.setUsername(username);
                        db.updatePost(post);
                        // return to list page
                        List<Post> posts = new ArrayList<Post>();
                        db.getPosts(username, posts);
                        // call JSP file to dynamically generate page
                        request.setAttribute("posts", posts);
                        request.setAttribute("username", username);
                        request.getRequestDispatcher("/list.jsp").include(request, response);
                    }
                    db.closeDatabase();
                } catch (NumberFormatException e){
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Bad Request");
                }

            }
        }

        if (action.equals("open")) {
            String username = request.getParameter("username");
            String c_postid = request.getParameter("postid");

            if (username == null || username.length() == 0 || c_postid == null || c_postid.length() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            } else {
                try{
                    int postid = Integer.parseInt(request.getParameter("postid"));
                    // Check if optional title and body parameters were passed
                    String title = request.getParameter("title");
                    String body = request.getParameter("body");
                    Post post = new Post();
                    if ((title == null || title.length() == 0) && (body == null || body.length() == 0)) {
                        request.setAttribute("username", username);
                        request.setAttribute("postid", postid);
                        Database db = new Database();
                        db.makeConnection();
                        // get Post title and body
                        if (postid == 0) {
                            request.setAttribute("title", "");
                            request.setAttribute("body", "");
                        } else {
                            boolean check = db.getPost(postid, post, username);
                            if (check == true) {
                                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                            } else {
                                request.setAttribute("title", post.getTitle());
                                request.setAttribute("body", post.getBody());
                            }
                        }
                        request.getRequestDispatcher("/edit.jsp").include(request, response);
                        db.closeDatabase();
                    }
                    else if ((title != null || title.length() != 0) && (body != null || body.length() != 0)) { // body are set
                        request.setAttribute("title", title);
                        request.setAttribute("body", body);
                        request.getRequestDispatcher("/edit.jsp").include(request, response);
                    }
                    else{
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
                    }
                }catch (NumberFormatException e){
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
                }
            }
        }

        if (action.equals("list")) {
            String username = request.getParameter("username");
            if (username == null || username.length() == 0) {
                out.println("bad request");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            } else {
                List<Post> posts = new ArrayList<Post>();
                Database db = new Database();
                db.makeConnection();
                db.getPosts(username, posts);
                // call JSP file to dynamically generate page
                request.setAttribute("posts", posts);
                request.setAttribute("username", username);
                request.getRequestDispatcher("/list.jsp").include(request, response);
                db.closeDatabase();
            }
        }

        if (action.equals("preview")) {

            String username = request.getParameter("username");
            String c_postid = request.getParameter("postid");
            String c_title = request.getParameter("title");
            String c_body = request.getParameter("body");

            if (username == null || username.length() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            } else if (c_postid == null || c_postid.length() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            } else if (c_title == null || c_title.length() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            } else if (c_body == null || c_body.length() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            } else {
                try{
                    int postid = Integer.parseInt(request.getParameter("postid"));
                    String title = "<h1>" + request.getParameter("title") + "</h1>";
                    String body = "<div>" + request.getParameter("body") + "</div>";
                    String button = "<body><form action=\"/editor/post\" method=\"get\">\n" +
                            "        <input type=\"hidden\" name=\"action\" value =\"open\">\n" +
                            "        <input type=\"hidden\" name=\"username\" value=\"" + username + "\">\n" +
                            "        <input type=\"hidden\" name=\"postid\" value="+postid+">" +
                            "        <input type=\"hidden\" name=\"title\" value=\"" + request.getParameter("title") + "\">\n" +
                            "        <input type=\"hidden\" name=\"body\" value=\"" + request.getParameter("body") + "\">\n" +
                            "        <button type=\"submit\">Close Preview</button>\n" +
                            "    </form></body>";
                    // show html rendering of given title and body
                    Parser parser = Parser.builder().build();
                    HtmlRenderer renderer = HtmlRenderer.builder().build();
                    String button_md = renderer.render(parser.parse(button));
                    String title_markdown = renderer.render(parser.parse(title));
                    String body_md = renderer.render(parser.parse(body));
                    out.println(button_md);
                    out.println(title_markdown);
                    out.println(body_md);
                }catch (NumberFormatException e){
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
                }
            }
        }
    }
}

