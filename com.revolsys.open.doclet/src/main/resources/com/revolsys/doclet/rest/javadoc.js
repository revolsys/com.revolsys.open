$(document).ready(function() {
  $('div.simpleDataTable table').dataTable({
    "bInfo" : false,
    "bJQueryUI" : true,
    "bPaginate" : false,
    "bSort" : false,
    "bFilter" : false
  });
  
  $('div.restMethod').accordion({
    icons : {
      header : "ui-icon-triangle-1-e",
      headerSelected : "ui-icon-triangle-1-s"
    },
    collapsible : true,
    active : 1,
    autoHeight : false
  });

});