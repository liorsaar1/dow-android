curl \
  -H "Content-Type:application/json" \
  -H "Authorization:key=AIzaSyAfBvnry23IJJUJW5OQHrF4AaBgn78NYvc" \
  -X POST -d '{ "data": { "message": "Alert !", "url": "http://www.districtofwonders.com" }, "to" : "/topics/global" }' \
  https://gcm-http.googleapis.com/gcm/send


