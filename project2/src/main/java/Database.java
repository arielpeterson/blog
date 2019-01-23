/******** Code modified from the JDBC Tutorial *********/

/***
 * File        : Database.java
 * Description : Simple program to illustrate concepts in tutorial
 */

import java.util.*;
import java.sql.*;

import p1.Post;

public class Database {
    
    /************************ Constructor ************************/
    public Database(){}

    /************************ Methods ************************/
    public void makeConnection(){
        // Open a connection to the database
        try {
            /* create an instance of a Connection object */
            c = DriverManager.getConnection("jdbc:mysql://localhost:3306/CS144", "cs144", ""); 
        } catch (SQLException ex) {
            System.out.println("SQLException caught");
            System.out.println("---");
            while ( ex != null ) {
                System.out.println("Message   : " + ex.getMessage());
                System.out.println("SQLState  : " + ex.getSQLState());
                System.out.println("ErrorCode : " + ex.getErrorCode());
                System.out.println("---");
                ex = ex.getNextException();
            }
        } 
    }

    public void getPosts(String username, List<Post> usersPosts){
        PreparedStatement preparedStmt = null;
        ResultSet rs = null;
        String title = null;
        Timestamp created = null;
        Timestamp modified = null;
        
        // query using a prepared statement to get the most recent list of postings
        try{
            preparedStmt = c.prepareStatement("SELECT title,created,modified,postid FROM Posts WHERE username = ?");
            preparedStmt.setString(1, username);
            rs = preparedStmt.executeQuery();  
            while(rs.next()){
                title = rs.getString("title");
                created = rs.getTimestamp("created");
                modified = rs.getTimestamp("modified");
                int postid = rs.getInt("postid");
                Post post = new Post(title, created ,modified, postid);
                usersPosts.add(post);  
            }   
        } catch (SQLException ex) {
            System.out.println("SQLException caught");
            System.out.println("---");
            while ( ex != null ) {
                System.out.println("Message   : " + ex.getMessage());
                System.out.println("SQLState  : " + ex.getSQLState());
                System.out.println("ErrorCode : " + ex.getErrorCode());
                System.out.println("---");
                ex = ex.getNextException();
            }
        } finally {
            try { rs.close(); } catch (Exception e) { /* ignored */ }
            try { preparedStmt.close(); } catch (Exception e) { /* ignored */ }
        } 

    }
    public boolean getPost(int postid, Post p, String username){
        PreparedStatement preparedStmt = null;
        ResultSet rs = null;
        String title = null;
        String body = null;
        boolean check = true;
        try{
            preparedStmt = c.prepareStatement("SELECT title, created, modified, body FROM Posts WHERE username = ? AND postid= ?");
            preparedStmt.setString(1, username);
            preparedStmt.setInt(2, postid);
            rs = preparedStmt.executeQuery();

            while(rs.next()){
                check = false;
                title = rs.getString("title");
                body = rs.getString("body");
                p.setTitle(title);
                p.setBody(body); 
            }
        } catch (SQLException ex) {
            System.out.println("SQLException caught");
            System.out.println("---");
            while ( ex != null ) {
                System.out.println("Message   : " + ex.getMessage());
                System.out.println("SQLState  : " + ex.getSQLState());
                System.out.println("ErrorCode : " + ex.getErrorCode());
                System.out.println("---");
                ex = ex.getNextException();
            }
        } finally {
            try { rs.close(); } catch (Exception e) { /* ignored */ }
            try { preparedStmt.close(); } catch (Exception e) { /* ignored */ }
        }
        return check;
    }

    public void removePost(String username, int postid){
        PreparedStatement preparedStmt = null;
        try{
            preparedStmt = c.prepareStatement("DELETE FROM Posts WHERE username = ? AND postid = ?");
            preparedStmt.setString(1, username);
            preparedStmt.setInt(2, postid);
            preparedStmt.executeUpdate();
            // decrease all ids by 1 that are after postid
            preparedStmt = c.prepareStatement("UPDATE Posts SET postid = postid - 1 WHERE (username = ?) AND (postid > ?)");
            preparedStmt.setString(1, username);
            preparedStmt.setInt(2, postid);
            preparedStmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println("SQLException caught");
            System.out.println("---");
            while ( ex != null ) {
                System.out.println("Message   : " + ex.getMessage());
                System.out.println("SQLState  : " + ex.getSQLState());
                System.out.println("ErrorCode : " + ex.getErrorCode());
                System.out.println("---");
                ex = ex.getNextException();
            }
        } finally {
            try { preparedStmt.close(); } catch (Exception e) { /* ignored */ }
        } 
    }


    public void addNewPost(Post p){
        PreparedStatement preparedStmt = null;
        ResultSet rs = null;
        try{
            // Get users last postid
            preparedStmt = c.prepareStatement("SELECT MAX(postid) AS max_id FROM Posts WHERE username= ?");
            preparedStmt.setString(1, p.getUsername());
            rs = preparedStmt.executeQuery();
            int max_postid = 0;
            while(rs.next()){
                max_postid = rs.getInt("max_id");
            }

            preparedStmt = c.prepareStatement(
                    "INSERT INTO Posts VALUES(?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"
            );
            max_postid = max_postid + 1;
            p.setPostID(max_postid);
            preparedStmt.setString(1, p.getUsername());
            preparedStmt.setInt(2, p.getPostID());
            preparedStmt.setString(3, p.getTitle());
            preparedStmt.setString(4, p.getBody());
            preparedStmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("SQLException caught");
            System.out.println("---");
            while ( ex != null ) {
                System.out.println("Message   : " + ex.getMessage());
                System.out.println("SQLState  : " + ex.getSQLState());
                System.out.println("ErrorCode : " + ex.getErrorCode());
                System.out.println("---");
                ex = ex.getNextException();
            }
        } finally {
            try { preparedStmt.close(); } catch (Exception e) { /* ignored */ }
            try { rs.close(); } catch (Exception e) { /* ignored */ }
        }

    }

    public void updatePost(Post p){
        PreparedStatement preparedStmt = null;
        try{
            // check if row exists and if it does update
            preparedStmt = c.prepareStatement(
                    "UPDATE Posts SET title = ?, modified = CURRENT_TIMESTAMP, body = ? WHERE username = ? AND postid = ? AND EXISTS (SELECT title FROM " +
                            "Posts WHERE username = ? AND postid = ?)");
            preparedStmt.setString(1, p.getTitle());
            preparedStmt.setString(2, p.getBody());
            preparedStmt.setString(3, p.getUsername());
            preparedStmt.setInt(4, p.getPostID());
            preparedStmt.setString(5, p.getUsername());
            preparedStmt.setInt(6, p.getPostID());
            preparedStmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println("SQLException caught");
            System.out.println("---");
            while ( ex != null ) {
                System.out.println("Message   : " + ex.getMessage());
                System.out.println("SQLState  : " + ex.getSQLState());
                System.out.println("ErrorCode : " + ex.getErrorCode());
                System.out.println("---");
                ex = ex.getNextException();
            }
        } finally {
            try { preparedStmt.close(); } catch (Exception e) { /* ignored */ }
        } 
    }
    public void closeDatabase(){
        try { c.close(); } catch (Exception e) { /* ignored */ }
        
    }

    /************************ Members ************************/
    private Connection c;
    
    
}
