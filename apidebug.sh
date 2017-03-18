#!/usr/bin/env bash

read -p "username: " username
read -sp "password: " password

# get session cookies and XSRF token
echo -e "\n\n>>>>> session cookies\n"
curl -s -c /tmp/cookies.txt -I \
    http://111.93.164.203/CampusPortalSOA/index
XSRF=$(grep XSRF-TOKEN /tmp/cookies.txt | awk '{print $7}')

# get login response for invalid credentials
echo -e ">>>>> login with invalid credentials\n"
curl -s -b /tmp/cookies.txt -X POST \
    -H "X-XSRF-TOKEN: $XSRF" \
    -H "Content-Type:application/json;charset=UTF-8" \
    -d "{username: \"invalid\", password: \"invalid\"}" \
    http://111.93.164.203/CampusPortalSOA/login

# get login response for valid credentials
echo -e "\n\n>>>>> login with valid credentials\n"
curl -s -b /tmp/cookies.txt -X POST \
    -H "X-XSRF-TOKEN: $XSRF" \
    -H "Content-Type:application/json;charset=UTF-8" \
    -d "{username: \"$username\", password: \"$password\"}" \
    http://111.93.164.203/CampusPortalSOA/login

# get attendance
echo -e "\n\n>>>>> attendance\n"
curl -s -b /tmp/cookies.txt -X POST \
    -H "X-XSRF-TOKEN: $XSRF" \
    -H "Content-Type:application/json;charset=UTF-8" \
    -d "{registerationid: \"ITERRETD1612A0000002\"}" \
    http://111.93.164.203/CampusPortalSOA/attendanceinfo
echo -e "\n"

# cleanup
rm -f /tmp/cookies.txt
