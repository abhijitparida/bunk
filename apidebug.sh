#!/usr/bin/env bash

cookies=/tmp/bunk-testserver-cookies.txt
api_url="http://136.233.14.3:8282/CampusPortalSOA"
if [ "$1" == "-t" ] || [ "$1" == "--testserver" ] ; then
    api_url="https://bunk-testserver.herokuapp.com"
fi

function login {
    curl $1/login -s -c $cookies -X POST \
        -H "Content-Type:application/json;charset=UTF-8" \
        -d "{\"username\":\"$2\", \"password\":\"$3\",\"MemberType\":\"S\"}"
}

function registerationid {
    curl $1/studentSemester/lov -s -b $cookies -X POST \
        -H "Content-Type:application/json;charset=UTF-8"
}

function attendanceinfo {
    curl $1/attendanceinfo -s -b $cookies -X POST \
        -H "Content-Type:application/json;charset=UTF-8" \
        -d "{\"registerationid\": \"$2\"}"
}

read -p "username: " username
read -sp "password: " password

echo -e "\n\nlogin - invalid credentials\n"
login $api_url invalid credentials

echo -e "\n\nregistration ids\n"
registerationid $api_url

echo -e "\n\nlogin - valid credentials\n"
login $api_url $username $password

echo -e "\n\nregistration ids\n"
registerationid $api_url

echo -e "\n"
read -p "registrationid: " registerationid

echo -e "\nattendance\n"
attendanceinfo $api_url $registerationid

echo -e "\n"
rm -f $cookies
