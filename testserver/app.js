var express = require('express');
var bodyParser = require('body-parser');

var port = process.env.PORT || 3000;
var app = express();
app.use(bodyParser.urlencoded({
    extended: true
}));
app.use(bodyParser.json());

app.get('/', function (req, res) {
    res.send('<pre>testserver for github.com/abhijitparida/bunk</pre>');
});

app.post('/login', function(req, res) {
    var username = req.body.username;
    var password = req.body.password;

    if (username.length === 10 && password === 'password') {
        res.send({
            'status': 'success',
            'name': 'test user ' + username.substr(-3)
        });
    } else {
        res.send({
            'status': 'error',
            'name': ''
        });
    }
});

app.post('/attendanceinfo', function(req, res) {
    var minutes = new Date().getMinutes();
    if (minutes === 0) {
        res.send({});
    } else {
        res.send({
            'griddata': [
                {
                    'Latt': (minutes / 2 + 5).toFixed() + ' / 35',
                    'Patt': (minutes / 6).toFixed() + ' / 10',
                    'subject': 'Computer Organisation and Architecture',
                    'subjectcode': 'CSE2011'
                },
                {
                    'Latt': (minutes / 2).toFixed() + ' / 30',
                    'Patt': 'Not Applicable',
                    'subject': 'Introduction to Number Theory',
                    'subjectcode': 'CSE2031'
                },
                {
                    'Latt': 'Not Applicable',
                    'Patt': (minutes / 4).toFixed() + ' / 15',
                    'subject': 'Programming Practice 1',
                    'subjectcode': 'CSE2041'
                },
                {
                    'Latt': (minutes / 3).toFixed() + ' / 20',
                    'Patt': (minutes / 5).toFixed() + ' / 12',
                    'subject': 'Algorithms Design I',
                    'subjectcode': 'CSE3131'
                },
                {
                    'Latt': (minutes / 3).toFixed() + ' / 20',
                    'Patt': 'Not Applicable',
                    'subject': 'Numerical Methods',
                    'subjectcode': 'MTH4002'
                },
            ]
        });
    }
});

app.listen(port, function () {
    console.log('bunk-testserver listening on port ' + port);
});
