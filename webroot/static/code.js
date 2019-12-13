const listContainer = document.querySelector('#service-list');
let servicesRequest = new Request('/service');
fetch(servicesRequest)
.then(function(response) { return response.json(); })
.then(function(serviceList) {
  serviceList.forEach(service => {
    var li = document.createElement("li");
    li.appendChild(document.createTextNode('(' + service.timeadded + ') ' + service.name + ' [' + service.url + ']: ' + service.status));
    var deleteBtn = document.createElement('button');
      deleteBtn.innerHTML = 'Delete';
      deleteBtn.onclick = function(){
            fetch('/service', {
            method: 'delete',
            headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
            },
          body: JSON.stringify({url:service.url})
        }).then(res=> location.reload());
      };
    li.appendChild(deleteBtn)
    listContainer.appendChild(li);
  });
});

const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt => {
    let urlName = document.querySelector('#url-name').value;
    let name = document.querySelector('#name').value;
    fetch('/service', {
    method: 'post',
    headers: {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'
    },
  body: JSON.stringify({url:urlName, name:name})
}).then(res=> location.reload());
}