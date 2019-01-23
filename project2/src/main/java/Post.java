package p1;

import java.sql.*;
public class Post {


	public Post(){}
	public Post(String title, Timestamp created, Timestamp modified, int postid) {
		this.title = title;
		this.created = created;
		this.modified = modified;
		this.postid = postid;
	}
	// Getters 
	public String getTitle(){
		return this.title;
	}
	public Timestamp getCreate(){
		return this.created;
	}
	public Timestamp getMod(){
		return this.modified;
	}
	public int getPostID(){
		return this.postid;
	}
	public String getBody(){
		return this.body;
	}
	public String getUsername(){
		return this.username;
	}
	public int getPostCount(){ return this.postcount; }

	// Setters 
	public void setBody(String body){
		this.body = body;
	}
	public void setTitle(String title){
		this.title = title;
	}
	public void setCreate(Timestamp created){
		this.created = created;
	}
	public void setMod(Timestamp modified){
		this.modified = modified;
	}
	public void setPostID(int postid){
		this.postid = postid;
	}
	public void setUsername(String username){
		this.username = username;
	}

	public void updatePostCount(String inc_dec){
		if (inc_dec.equals("increase")){
			this.postcount++;
		}
		if (inc_dec.equals("decrease")) {
			this.postcount--;
		}
	}

	private String username;
	private String title;
	private Timestamp created;
	private Timestamp modified;
	private int postid;
	private static int postcount = 0;
	private String body;
	

}