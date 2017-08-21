#!/usr/bin/env bash

api_url="http://111.93.164.203/CampusPortalSOA"
test_url="https://bunk-testserver.herokuapp.com"

cookies=/tmp/bunk-testserver-cookies.txt

function login {
    curl $1/login -s -c $cookies -X POST \
        -H "Content-Type:application/json;charset=UTF-8" \
        -d "{\"username\":\"$2\", \"password\":\"$3\"}"
}

function attendanceinfo {
    curl $1/attendanceinfo -s -b $cookies -X POST \
        -H "Content-Type:application/json;charset=UTF-8" \
        -d "{\"registerationid\": \"ITERRETD1612A0000002\"}"
}

read -p "username: " username
read -sp "password: " password

echo -e "\n\n[ITER API] login - invalid credentials\n"
login $api_url invalid credentials
echo -e "\n\n[ITER API] login - valid credentials\n"
login $api_url $username $password
echo -e "\n\n[ITER API] attendance\n"
attendanceinfo $api_url

password=password

echo -e "\n\n[TEST SERVER] login - invalid credentials\n"
login $test_url invalid credentials
echo -e "\n\n[TEST SERVER] login - valid credentials\n"
login $test_url $username $password
echo -e "\n\n[TEST SERVER] attendance\n"
attendanceinfo $test_url

echo -e "\n"
rm -f $cookies
