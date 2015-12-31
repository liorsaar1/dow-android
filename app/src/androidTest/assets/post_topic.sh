curl \
  -H "Content-Type:application/json" \
  -H "Authorization:key=AIzaSyAfBvnry23IJJUJW5OQHrF4AaBgn78NYvc" \
  -X POST -d '{ "data": { "message": "New Content !" }, "to" : "/topics/general" }' \
  https://gcm-http.googleapis.com/gcm/send


