var express = require('express');
var router = express.Router();
const bodyParser = require('body-parser');
const middlewares = [bodyParser.urlencoded()];
const bcrypt = require('bcrypt');
let jwt = require('jsonwebtoken');


let MongoClient = require('mongodb').MongoClient;// Connection url
const MONGODB_URI = 'mongodb://localhost:27017';
let db;

MongoClient.connect(MONGODB_URI, function(err, client) {
  db = client.db('BlogServer');
});

// check that username is valid in database if not return 404 not found
// check that postid is a valid postid

router.get('/blog/:username', function(req, res) {

    let user_collection = db.collection('Users');
    let name = req.params.username;
    let postid = req.query.start;
    if (name == null){ // check that username is given
        res.status(400).json();
    } else {
        user_collection.findOne({username: name}, function(err,result){ // check that user exists in Users collection
            if (result == null){
                res.status(404).json();
            }
        });
        // check if optional start postid was given
        let post_collection = db.collection('Posts');
        if (isNaN(postid)){ // just return posts starting from first post
            post_collection.find({username: name}).toArray(function(err, result){
                if (err) throw err;
                let count = 0;
                let index = 0;

                res.render('preview2',{result:result, count:count, index:index, username:name});
            });
        }else{ // return posts starting from given start parameter
             if (Number.isInteger(parseInt(postid))){
                post_collection.find({username: name, postid: {$gte:Number(postid)}}).toArray(function(err, result){
                   if (result === undefined || result.length == 0){ // check if start is greater than the number of posts
                        res.status(404).json();
                   }
                   if (err) throw err;
                   let count = 0;
                   let index = 0;
                   res.render('preview2',{result:result,count:count, index:index, username:name});
                });
             } else{
                res.status(400).json();
             }

        }
    }
});

// get the next page (5 posts)
router.get('/blog/next/:username/:index', function(req, res) {
    let name = req.params.username;
    let index = req.params.index;
    var post_collection = db.collection('Posts');
    let count = 0;
    post_collection.find({username: name}).toArray(function(err,result){
        res.render('preview2',{result:result, count:count, index:index, username:name});
    });
});

// get the post for this user
router.get('/blog/:username/:postid', function(req, res) {
    let name = req.params.username;
    let postid = parseInt(req.params.postid);
    if ((Number.isInteger(postid)) && (name != null)){ // check that u_postid is an integer and neither are null

        var post_collection = db.collection('Posts');
        post_collection.findOne({username: name, postid: postid}, function(err,result){
            if (err) throw err;
            if (result == null) {
                res.status(404).json();
            }
            else{
                res.render('preview',{title:result["title"], body:result["body"]});
            }
        });
    } else {
        res.status(400).json();
    }

});

// GET user LOGIN page
router.get('/login', function(req, res){
    // check for optional redirect parameter
        let redirect = req.query.redirect;
        let username = "";
        let password = "";
        res.render('login',{redirect:redirect, username:username, password:password});
    }
);

// POST user LOGIN page
router.post('/login', function(req, res){

    let username = req.body.username;
    let password = req.body.password;
    let redirect = req.body.redirect;
    var user_collection = db.collection('Users');
    if (username == null || password == null){ // not a valid request
        res.status(400).json(); // check error code for bad request
    }
    if (redirect == null){ //optional parameter was not given
        // check username and password in BlogServer
        user_collection.findOne({username:username}, function(err, result){
            if (result == null){ // username does not exist
                res.status(401);
                if (username == null) {
                    res.status(400).json();
                }
                res.render('login', {redirect:redirect, username:username, password:password}); // retry login
            }
            let hash = result["password"];
            bcrypt.compare(password, hash, function(err, hash_res){
                if (hash_res){ // given password matches hash
                    // create jwt cookie
                    // Date.now() is in milliseconds
                    let expiration = Math.floor(Date.now() / 1000) + (60 * 60) + (60 * 60); // expire in 2 hours
                    let token = jwt.sign({"exp": expiration,"usr": username}, 'C-UFRaksvPKhx1txJYFcut3QGxsafPmwCY6SCly3G6c',{header:{"alg": "HS256","typ": "JWT"}});
                    res.cookie('jwt', token);
                    res.status(200).json();
                }
                else{
                    res.status(401);
                    res.render('login',{redirect:redirect,username:username, password:password}); // retry login info
                }
            });
        });
    }
    else{ //optional redirect parameter was given
        // check username and password in BlogServer
        user_collection.findOne({username:username}, function(err, result){

            if (result == null){ // username does not exist
                res.status(401);
                res.render('login', {redirect:redirect, username:username, password:password}); // retry login
            }
            let hash = result["password"];
            bcrypt.compare(password, hash, function(err, hash_res){
                if (hash_res){ // given password matches hash
                    let jwt = require('jsonwebtoken');
                    // Date.now() is in milliseconds
                    let expiration = Math.floor(Date.now() / 1000) + (60 * 60) + (60 * 60); // expire in 2 hours
                    let token = jwt.sign({"exp": expiration,"usr": username}, 'C-UFRaksvPKhx1txJYFcut3QGxsafPmwCY6SCly3G6c',{header:{"alg": "HS256","typ": "JWT"}});
                    res.cookie('jwt', token);
                    // redirect to given page
                    res.redirect(redirect);
                }
                else{
                    res.status(401);
                    res.render('login',{redirect:redirect, username:username, password:password}); // retry login info
                }
            });
        });
    }
});

// Cookie Validation
function validate_cookie(jwt_cookie, username){
    if (jwt_cookie == null){
        return false;
    } else {
        try {
            let decoded_cookie = jwt.verify(jwt_cookie,'C-UFRaksvPKhx1txJYFcut3QGxsafPmwCY6SCly3G6c');
            let expiration = decoded_cookie.exp;
            let cookie_username = decoded_cookie.usr;
            let curr_time = Date.now() / 1000 ; // convert current time to seconds
            // expiration time should be within two hours from the time now
            let time_diff = expiration - curr_time;
            if ((username == cookie_username) && (time_diff >= 0 )) { //
               return true;
            }
            else{
                return false;
            }
        } catch (err){ // error decoding token/cookie -- might be because wrong cookie was given
            return false;
        }
    }
}

// REST API

// GET all posts
router.get('/api/:username', function(req, res){
    // authenticate user
    let username = req.params.username;
    let jwt_cookie = req.cookies.jwt;
    if (jwt_cookie != null){
       let cookie_check = validate_cookie(jwt_cookie,username);
       if (cookie_check){
           let post_collection = db.collection('Posts');
           let user_collection = db.collection('Users');
           user_collection.findOne({username: username}, function(err, result){ // check that user exists
               if (result == null){ // no user by that name
                   res.status(400).json();
               }
               else{ // user exists get posts
                  post_collection.find({username:username}).toArray(function(err,result){
                      if (err) throw err;
                      res.status(200).json(result);
                  });
               }
           });
       } else {
           res.status(401).json(); // cookie verification failed
       }
    } else {
        res.status(401).json();
    }


});

// GET post
// return this blog post for the user
router.get('/api/:username/:postid', function(req, res){
    let username = req.params.username;
    let u_postid = parseInt(req.params.postid);
    if ((u_postid == null) || (username == null)){
        res.status(400).json();
    } else {
        if (Number.isInteger(u_postid)){
            let jwt_cookie = req.cookies.jwt;
            let cookie_check = validate_cookie(jwt_cookie,username);
            if (cookie_check){
                var post_collection = db.collection('Posts');
                post_collection.findOne({username: username, postid: Number(u_postid)}, function(err,result){
                    if (err) throw err;
                    if (result == null) { // postid dne
                        res.status(404).json();
                    }
                    else{
                        // response should be included in the body in JSON
                        res.status(200).json(result);
                    }
                });
             }
            else{
                res.status(401).json(); // cookie verification failed
            }
        } else {
            res.status(400).json();
        }
    }
});

// POST
// create a post for this user with this postid with required title and body parameters
router.post('/api/:username/:postid', function(req, res) {
    let username = req.params.username;
    let u_postid = parseInt(req.params.postid);
    if (Number.isInteger(u_postid)){
        let jwt_cookie = req.cookies.jwt;
        let cookie_check = validate_cookie(jwt_cookie,username);
        if (cookie_check){
        // get request title and body
            let title = req.body.title;
            let body = req.body.body;
            // check if title/body is null
            /* NEED TO CHECK IF FORMAT IS IN JSON */
            if ((title == null) || (body == null)){
                res.status(400).json();
            } else {
                // check if this post already exists
                let post_collection = db.collection('Posts');
                post_collection.findOne({username: username, postid: Number(u_postid)}, function(err, result){
                    if (result != null){ // post for this username with this postid already exists
                        res.status(400).json();
                    } else { // okay to insert post
                        post_collection.save({username:username, postid: Number(u_postid), created: Date.now() , modified: Date.now(), title:title, body: body}, function(err,r){
                            res.status(201).json();  // insert was successful
                        });
                    }
                });
            }
        } else {
            res.status(401).json();
        }
    } else {
        res.status(400).json();
    }



});

// put
router.put('/api/:username/:postid', function(req, res){
    let username = req.params.username;
    let u_postid = parseInt(req.params.postid);
    if (Number.isInteger(u_postid)){
        let jwt_cookie = req.cookies.jwt;
        let cookie_check = validate_cookie(jwt_cookie,username);
        if (cookie_check){ // validate cookie
            let title = req.body.title;
            let body = req.body.body;
            if ((title == null) || (body == null)){ // required params not given
                res.status(400).json();
            } else {
                let post_collection = db.collection('Posts');
                post_collection.findOne({username:username, postid: u_postid}, function(err, result){
                    if (result == null){ // no such post exists
                        res.status(400).json();
                    }
                    else{ // post exists, try to update
                       post_collection.update({username:username, postid: u_postid}, {$set: {title: title, body: body, modified: Date.now()}}, function(err,result){
                          res.status(200).json();
                       });
                    }
                });
            }
        } else {
            res.status(401).json();
        }
    }else {
        res.status(400).json();
    }

});

// delete
router.delete('/api/:username/:postid', function(req, res){
    let username = req.params.username;
    let u_postid = parseInt(req.params.postid);
    if (Number.isInteger(u_postid)){
        let jwt_cookie = req.cookies.jwt;
        let cookie_check = validate_cookie(jwt_cookie,username);
        if (cookie_check){ // validate cookie
            let post_collection = db.collection('Posts');
            post_collection.findOne({username: username, postid: u_postid}, function(err,result){
                if (result == null){ // no post exists
                    res.status(400).json();
                }
                else{ // post exists --  now remove
                    post_collection.remove({username: username, postid: u_postid}, function(err, result){
                        res.status(204).json();
                    });
                }
            });
        } else {
            res.status(401).json();
        }
    } else {
        res.status(400).json();
    }


});


module.exports = router;
