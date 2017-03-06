#!/usr/bin/env bash

# get session cookies and XSRF token
curl -s -c /tmp/cookies.txt > /dev/null \
    http://111.93.164.203/CampusPortalSOA/index
XFRF=$(grep XSRF-TOKEN /tmp/cookies.txt | awk '{print $7}')

read -p "username: " username
read -sp "password: " password
echo -e "\nlogging in $username\n"

# login
curl -s -b /tmp/cookies.txt -X POST \
    -H "X-XSRF-TOKEN: $XFRF" \
    -H "Content-Type:application/json;charset=UTF-8" \
    -d "{username: \"$username\", password: \"$password\"}" \
    http://111.93.164.203/CampusPortalSOA/login

# uncomment to download student photo
# curl -s -b /tmp/cookies.txt > /tmp/profile.jpg \
#     http://111.93.164.203/CampusPortalSOA/image/studentPhoto

# cleanup
rm -f /tmp/cookies.txt
