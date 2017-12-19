$(document).ready(function(){
    $("#login").click(function(){
        var email = $("#email").val();
        var password = $("#password").val();
        // Checking for blank fields.
        if(email =='' || password =='') {
            $('input[type="text"],input[type="password"]').css("border","2px solid red");
            $('input[type="text"],input[type="password"]').css("box-shadow","0 0 3px red");
            alert("Email or password is empty");
        } else {
            $.get({
                url: '/endpoint/rest/get_role',
                headers: {
                    'Authorization': 'Basic ' + btoa(email + ':' + password)
                }
            })
                .done(function(data) {
                    var dataObj = $.parseJSON(data);
                    if(dataObj['role']=='admin') {
                        $.redirect('customers.html', {'login': email, 'pass': password, 'role': 'admin'}, 'GET');
                    } else if(dataObj['role']=='unknown') {
                        $('input[type="text"],input[type="password"]').css({"border":"2px solid red","box-shadow":"0 0 3px red"});
                        alert("Email or password is incorrect");
                    }
                });
        }
    });
});