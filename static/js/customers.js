$(document).ready(function(){
    $("#add_new_customer").click(function() {
        $.redirect('/add_customer.html', {'login': 'admin', 'pass': 'admin', 'role': 'admin'}, 'GET');
    });

    $.get({
        url: '/endpoint/rest/customers',
        headers: {
            'Authorization': 'Basic ' + btoa('admin' + ':' + 'admin')
        }
    }).done(function(data) {
        var json = data;

        var dataSet = [];
        for(var i = 0; i < json.length; i++) {
            var obj = json[i];
            dataSet.push([obj.firstName, obj.lastName, obj.login, obj.pass, obj.balance])
        }

        $('#customer_list_id')
            .DataTable({
                data: dataSet,
                columns: [
                    { title: "Fist Name" },
                    { title: "Last Name" },
                    { title: "Email" },
                    { title: "Pass" },
                    { title: "Balance" }
                ]
            });
    });
});