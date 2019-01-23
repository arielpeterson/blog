use BlogServer
db.createCollection("Posts")
db.createCollection("Users")
var user_file = cat('./users.json')
var post_file = cat('./posts.json')
var uf = JSON.parse(user_file);
var pf = JSON.parse(post_file);
db.Users.insert(uf)
db.Posts.insert(pf)

